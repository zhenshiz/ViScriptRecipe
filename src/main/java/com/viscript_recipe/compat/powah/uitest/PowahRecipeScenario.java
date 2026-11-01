package com.viscript_recipe.compat.powah.uitest;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.compat.jei.powah.PowahJeiCanvas;
import com.viscript_recipe.compat.powah.PowahRecipeEditorTypes;
import com.viscript_recipe.compat.powah.PowahRecipeFactory;
import com.viscript_recipe.compat.powah.PowahRecipeImporter;
import com.viscript_recipe.compat.powah.data.PowahRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.compat.jei.JeiEnergizingCategory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

/** 验证充能配方的原生往返、六槽编辑、长整数能量和 JEI 增量刷新。 */
@LDLRegisterClient(name = "powah_energizing", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = PowahRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class PowahRecipeScenario implements UIScenario {
    private RecipeProject project;
    private RecipeEntry entry;
    private String before;
    private List<RecipeHolder<?>> originalRecipes;
    private RecipeHolder<?> lastRecipe;
    private long revision = 100;
    private float replaceX, replaceY;

    @Override public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "powah"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI is ready", context -> LDLibJEIPlugin.jeiRuntime != null)
                .step("native import, persistence, network and boundaries", context -> verifyNativeRecipes())
                .openModularUI("open energizing editor", context -> { entry = fixture(); before = canonical(); return openEditor(); })
                .awaitElement("#powah_canvas").ticks(2)
                .check("native JEI renderer is available", context -> context.el("#powah_canvas").element().getChildren().stream()
                        .anyMatch(child -> child instanceof PowahJeiCanvas canvas && canvas.isAvailable()))
                .check("canvas is horizontally centered", context -> {
                    var card = context.el("#powah_canvas").element(); var parent = context.el("#powah_canvas_container").element();
                    return Math.abs(card.getPositionX() + card.getSizeWidth() / 2 - parent.getPositionX() - parent.getSizeWidth() / 2) < 1;
                })
                .check("unchanged recipe survives opening", context -> before.equals(canonical()))
                .click("#powah_input_0").awaitElement("#powah_item_stack")
                .checkCount("#powah_stack_count", 0)
                .step("fill all six inputs", context -> {
                    for (int i = 0; i < 6; i++) ((ItemSlot) context.el("#powah_input_" + i).element()).setValue(new ItemStack(Items.DIAMOND, 64), true);
                }).ticks(2)
                .check("six one-item ingredients match the native machine", context -> {
                    var recipe = compiled();
                    return recipe.getIngredients().size() == 6 && recipe.matches(new RecipeInput() {
                        @Override public ItemStack getItem(int index) { return index == 0 ? ItemStack.EMPTY : Items.DIAMOND.getDefaultInstance(); }
                        @Override public int size() { return 7; }
                    }, Minecraft.getInstance().level);
                })
                .rightClick("#powah_input_2").ticks(2)
                .check("clearing a middle slot removes one material", context -> compiled().getIngredients().size() == 5)
                .step("refill middle slot", context -> ((ItemSlot) context.el("#powah_input_2").element()).setValue(Items.EMERALD.getDefaultInstance(), true))
                .ticks(2).check("middle position stays editable", context -> compiled().getIngredients().get(2).test(Items.EMERALD.getDefaultInstance()))
                .click("#powah_output").awaitElement("#powah_stack_count").focus("#powah_stack_count")
                .key(GLFW_KEY_END).key(GLFW_KEY_HOME, GLFW_MOD_SHIFT).type("32").key(GLFW_KEY_ENTER)
                .waitUntil("VSL output count is saved", context -> compiled().getResultItem().getCount() == 32)
                .step("set output components", context -> {
                    var result = new ItemStack(Items.AMETHYST_SHARD, 3); result.set(DataComponents.CUSTOM_NAME, Component.literal("Charged output"));
                    ((ItemSlot) context.el("#powah_output").element()).setValue(result, true);
                }).ticks(2)
                .check("output components survive compilation", context -> compiled().getResultItem().getHoverName().getString().equals("Charged output"))
                .step("select energy", context -> RecipeCanvas.selectRecipe()).awaitElement("#powah_energy")
                .focus("#powah_energy").key(GLFW_KEY_END).key(GLFW_KEY_HOME, GLFW_MOD_SHIFT)
                .type("9223372036854775807").key(GLFW_KEY_ENTER)
                .waitUntil("maximum long energy retains every digit", context -> compiled().getEnergy() == Long.MAX_VALUE)
                .focus("#powah_energy").key(GLFW_KEY_END).key(GLFW_KEY_HOME, GLFW_MOD_SHIFT)
                .type("5000000000").key(GLFW_KEY_ENTER)
                .waitUntil("64-bit energy is saved", context -> compiled().getEnergy() == 5_000_000_000L)
                .hover("#powah_energy").screenshot("powah_energizing_editor").screenshotElement("powah_energizing_card", "#powah_canvas")
                .rightClick("#powah_output").ticks(2)
                .check("empty output remains a draft", context -> root().getAsJsonObject("result").isEmpty())
                .closeScreen().openModularUI("reopen incomplete draft", context -> openEditor()).awaitElement("#powah_output")
                .step("restore output", context -> ((ItemSlot) context.el("#powah_output").element()).setValue(Items.DIAMOND.getDefaultInstance(), true))
                .ticks(2).check("restored draft compiles", context -> compiled().getResultItem().is(Items.DIAMOND)).closeScreen()
                .openModularUI("open tags and compound alternatives", context -> {
                    entry = fixture(); var root = root();
                    root.add("ingredients", JsonParser.parseString("[{\"tag\":\"c:ingots/iron\"},{\"type\":\"neoforge:compound\",\"ingredients\":[{\"item\":\"minecraft:diamond\"},{\"item\":\"minecraft:emerald\"}]},{\"type\":\"neoforge:compound\",\"ingredients\":[{\"tag\":\"c:ingots/gold\"},{\"item\":\"minecraft:copper_ingot\"}]},{\"tag\":\"viscript_recipe:empty_powah_test\"}]"));
                    data().setRecipeJson(root.toString()); before = canonical(); return openEditor();
                }).awaitElement("#powah_input_0").ticks(25)
                .step("save cycling ingredients", context -> project.saveCurrentVisualState())
                .check("tags and alternatives remain intact", context -> before.equals(canonical()))
                .click("#powah_input_1").awaitElement("#powah_replace_ingredient")
                .check("opening compound properties is lossless", context -> before.equals(canonical()))
                .hover("#powah_replace_ingredient")
                .step("press ingredient replacement", context -> {
                    var bounds = context.el("#powah_replace_ingredient").bounds();
                    replaceX = bounds.centerX(); replaceY = bounds.centerY();
                    context.input().mouseDown(replaceX, replaceY, GLFW_MOUSE_BUTTON_LEFT);
                })
                // 按下后属性栏会重建，松开时使用原坐标，不再查找已移除的按钮。
                .step("release ingredient replacement", context -> context.input().mouseUp(replaceX, replaceY, GLFW_MOUSE_BUTTON_LEFT)).ticks(2)
                .check("replacement changes only the selected slot", context -> root().getAsJsonArray("ingredients").get(1).getAsJsonObject().get("item").getAsString().equals("minecraft:iron_ingot")
                        && root().getAsJsonArray("ingredients").get(0).getAsJsonObject().has("tag")
                        && root().getAsJsonArray("ingredients").get(2).getAsJsonObject().has("type"))
                .rightClick("#powah_input_3").ticks(2)
                .check("empty tag can be removed explicitly", context -> root().getAsJsonArray("ingredients").size() == 3)
                .closeScreen()
                .step("add recipe to runtime", context -> {
                    originalRecipes = List.copyOf(Minecraft.getInstance().level.getRecipeManager().getRecipes());
                    entry = fixture(); applyDelta(true);
                }).waitUntil("JEI displays added recipe", context -> !jeiOutput().isEmpty())
                .step("change recipe output", context -> {
                    var root = root(); root.add("result", PowahRecipeFactory.encode(ItemStack.CODEC, new ItemStack(Items.EMERALD, 5)));
                    data().setRecipeJson(root.toString()); applyDelta(true);
                }).waitUntil("JEI displays updated recipe", context -> jeiOutput().is(Items.EMERALD) && jeiOutput().getCount() == 5)
                .step("disable recipe", context -> applyDelta(false))
                .waitUntil("JEI removes disabled recipe", context -> jeiOutput().isEmpty())
                .teardown("restore runtime recipes", context -> {
                    if (originalRecipes != null) Minecraft.getInstance().level.getRecipeManager().replaceRecipes(originalRecipes);
                });
    }

    private RecipeEntry fixture() {
        var result = new RecipeEntry().setType(PowahRecipeEditorTypes.ENERGIZING); result.applyDefaultData(); return result;
    }

    private ModularUI openEditor() {
        project = new RecipeProject(); project.getRecipeFile().getEntries().add(entry);
        var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null); project.refreshAiViews(); return new ModularUI(UI.of(editor));
    }

    private PowahRecipeData data() { return entry.getData(); }
    private com.google.gson.JsonObject root() { return JsonParser.parseString(data().getRecipeJson()).getAsJsonObject(); }
    private EnergizingRecipe compiled() { return (EnergizingRecipe) entry.compile(); }
    private String canonical() { return PowahRecipeFactory.encode(entry.compile(), Platform.getFrozenRegistry()); }

    private void applyDelta(boolean add) {
        var id = ViScriptRecipe.id("powah_delta_test"); RecipeHolder<?> current = new RecipeHolder<>(id, entry.compile());
        var recipes = new java.util.ArrayList<RecipeHolder<?>>(originalRecipes); if (add) recipes.add(current);
        Minecraft.getInstance().level.getRecipeManager().replaceRecipes(recipes);
        RecipeDeltaJeiSynchronizer.applyDelta(++revision, Set.of(id), lastRecipe == null ? Map.of() : Map.of(id, lastRecipe),
                add ? Map.of(id, current) : Map.of(), lastRecipe == null ? Map.of() : Map.of(id, entry.getType()), add ? Map.of(id, entry.getType()) : Map.of());
        lastRecipe = add ? current : null;
    }

    private ItemStack jeiOutput() {
        var runtime = LDLibJEIPlugin.jeiRuntime; if (runtime == null) return ItemStack.EMPTY;
        return runtime.getRecipeManager().createRecipeLookup(JeiEnergizingCategory.TYPE.get()).get()
                .filter(holder -> holder.id().getPath().endsWith("powah_delta_test"))
                .map(holder -> holder.value().getResultItem()).findFirst().orElse(ItemStack.EMPTY);
    }

    private void roundTrip(RecipeHolder<?> holder) {
        var imported = PowahRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry());
        var expected = PowahRecipeFactory.encode(holder.value(), Platform.getFrozenRegistry());
        if (!expected.equals(PowahRecipeFactory.encode(imported.entry().compile(), Platform.getFrozenRegistry()))) throw new AssertionError("Import changed " + holder.id());
        var copy = new RecipeEntry(); copy.deserializeNBT(Platform.getFrozenRegistry(), imported.entry().serializeNBT(Platform.getFrozenRegistry()));
        if (!expected.equals(PowahRecipeFactory.encode(copy.compile(), Platform.getFrozenRegistry()))) throw new AssertionError("Persistence changed " + holder.id());
        var delta = new RecipeDeltaSnapshot(1, 2, false, false, List.of(), List.of(holder), Map.of(), Map.of());
        var decoded = RecipeDeltaSnapshot.deserialize(Platform.getFrozenRegistry(), delta.serialize(Platform.getFrozenRegistry()));
        if (!expected.equals(PowahRecipeFactory.encode(decoded.upsertedRecipes().getFirst().value(), Platform.getFrozenRegistry()))) throw new AssertionError("Network changed " + holder.id());
    }

    private void verifyNativeRecipes() {
        int tested = 0;
        for (var holder : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
            if (!PowahRecipeImporter.INSTANCE.canImport(holder)) continue;
            roundTrip(holder); tested++;
        }
        if (tested == 0) throw new AssertionError("No Powah recipes loaded");
        entry = fixture(); var root = root();
        for (long energy : new long[]{1, 5_000_000_000L, Long.MAX_VALUE}) {
            root.addProperty("energy", energy); data().setRecipeJson(root.toString());
            roundTrip(new RecipeHolder<>(ViScriptRecipe.id("powah_long_energy"), compiled()));
        }
        for (var invalid : List.of("{\"energy\":0}", "{\"ingredients\":[]}", "{\"result\":{}}", "{\"result\":{\"id\":\"minecraft:stone\",\"count\":0}}",
                "{\"ingredients\":[" + String.join(",", java.util.Collections.nCopies(7, "{\"item\":\"minecraft:stone\"}")) + "]}")) {
            entry = fixture(); root = root();
            var invalidFields = JsonParser.parseString(invalid).getAsJsonObject();
            for (var field : invalidFields.entrySet()) root.add(field.getKey(), field.getValue());
            data().setRecipeJson(root.toString());
            try { entry.compile(); throw new AssertionError("Invalid recipe accepted: " + invalid); }
            catch (IllegalArgumentException | IllegalStateException expected) { }
        }
        ViScriptRecipe.LOGGER.info("Powah native import/persistence/network round trips: {}", tested);
    }
}
