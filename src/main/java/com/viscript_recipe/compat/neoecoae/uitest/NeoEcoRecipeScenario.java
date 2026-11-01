package com.viscript_recipe.compat.neoecoae.uitest;

import cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipes;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.compat.jei.neoecoae.NeoEcoJeiCanvas;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeEditorTypes;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeImporter;
import com.viscript_recipe.compat.neoecoae.data.NeoEcoRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 验证原生定义往返、槽位编辑、流体标签与大型工作站派生配方刷新。 */
@LDLRegisterClient(name = "neoecoae_recipes", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = NeoEcoRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class NeoEcoRecipeScenario implements UIScenario {
    private RecipeProject project;
    private RecipeEntry entry;
    private String before;
    private List<RecipeHolder<?>> originalRecipes;
    private long deltaRevision = 100;

    @Override public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "neoecoae"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI is ready", context -> LDLibJEIPlugin.jeiRuntime != null)
                .step("round-trip every native definition and packet", context -> verifyNativeImports());
        for (var type : NeoEcoRecipeEditorTypes.TYPES) {
            scenario.openModularUI("open " + type, context -> { entry = fixture(type); before = canonical(); return openEditor(); })
                    .awaitElement("#neoecoae_canvas").ticks(2)
                    .check("native JEI drawing is ready", context -> context.el("#neoecoae_jei_panel").element().getChildren().stream().anyMatch(child -> child instanceof NeoEcoJeiCanvas canvas && canvas.isAvailable()))
                    .check("canvas is horizontally centered", context -> {
                        var body = context.el("#neoecoae_canvas").element(); var parent = context.el("#neoecoae_canvas_container").element();
                        return Math.abs(body.getPositionX() + body.getSizeWidth() / 2 - parent.getPositionX() - parent.getSizeWidth() / 2) < 1;
                    })
                    .step("save unchanged", context -> project.saveCurrentVisualState())
                    .check("native fields are unchanged", context -> before.equals(canonical()));
            if (!type.equals("cooling")) {
                scenario.click("#neoecoae_input_0").awaitElement("#neoecoae_stack_count")
                        .focus("#neoecoae_stack_count").key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                        .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                        .type("4096").key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                        .waitUntil("VSL count is saved", context -> root().getAsJsonArray("inputItems").get(0).getAsJsonObject().get("count").getAsInt() == 4096)
                        .checkCount("#neoecoae_item_stack #neoecoae_stack_count", 1)
                        .step("fill last workstation input", context -> ((ItemSlot) context.el("#neoecoae_input_" + (type.startsWith("large") ? 11 : 8)).element()).setValue(new ItemStack(Items.DIAMOND, 2), true))
                        .ticks(2).check("last slot compiles", context -> root().getAsJsonArray("inputItems").size() == 2)
                        .step("edit output components", context -> {
                            var stack = new ItemStack(Items.EMERALD, 3); stack.set(DataComponents.CUSTOM_NAME, Component.literal("ECO output"));
                            ((ItemSlot) context.el("#neoecoae_output").element()).setValue(stack, true);
                        }).ticks(2).check("output components survive", context -> entry.compile().getResultItem(Platform.getFrozenRegistry()).getHoverName().getString().equals("ECO output"));
            }
            String parameter = type.equals("cooling") ? "coolant" : "energy";
            scenario.step("select recipe parameters", context -> com.viscript_recipe.gui.canvas.RecipeCanvas.selectRecipe())
                    .awaitElement("#neoecoae_" + parameter).focus("#neoecoae_" + parameter)
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                    .type("7000").key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                    .waitUntil("edited parameter is persisted", context -> root().get(parameter).getAsInt() == 7000)
                    .step("drop fluid on input", context -> ((FluidSlot) context.el("#neoecoae_fluid_input").element()).setValue(new FluidStack(Fluids.WATER, 2000), true))
                    .step("drop fluid on output", context -> ((FluidSlot) context.el("#neoecoae_fluid_output").element()).setValue(new FluidStack(Fluids.LAVA, 500), true))
                    .ticks(2)
                    .check("input amount is saved", context -> root().getAsJsonObject(type.equals("cooling") ? "input" : "inputFluid").get("amount").getAsInt() == 2000)
                    .check("output amount is saved", context -> root().getAsJsonObject(type.equals("cooling") ? "output" : "fluidOutput").get("amount").getAsInt() == 500)
                    .step("compile edited definition", context -> entry.compile())
                    .checkHidden("#neoecoae_validation").hover("#neoecoae_" + parameter).screenshot("neoecoae_" + type)
                    .screenshotElement("neoecoae_card_" + type, "#neoecoae_canvas")
                    .rightClick("#neoecoae_fluid_input").ticks(2)
                    .check("cleared fluid input is saved", context -> !root().has(type.equals("cooling") ? "input" : "inputFluid"))
                    .step("restore fluid input", context -> ((FluidSlot) context.el("#neoecoae_fluid_input").element()).setValue(new FluidStack(Fluids.WATER, 1000), true))
                    .ticks(2).step("compile after restoring fluid", context -> entry.compile())
                    .checkHidden("#neoecoae_validation").closeScreen();
        }
        scenario.openModularUI("compound items and fluid tags", context -> {
                    entry = fixture("integrated_working_station"); var root = root();
                    root.add("inputItems", JsonParser.parseString("[{\"type\":\"neoforge:compound\",\"ingredients\":[{\"tag\":\"c:ingots/iron\"},{\"item\":\"minecraft:copper_ingot\"}],\"count\":3}]"));
                    root.add("inputFluid", JsonParser.parseString("{\"tag\":\"minecraft:water\",\"amount\":3000}"));
                    data().setRecipeJson(root.toString()); before = canonical(); return openEditor();
                }).awaitElement("#neoecoae_canvas").ticks(25)
                .step("save cycling displays", context -> project.saveCurrentVisualState())
                .check("tags and compound ingredients survive", context -> before.equals(canonical()))
                .click("#neoecoae_fluid_input").ticks(2)
                .check("opening fluid properties keeps tag", context -> root().getAsJsonObject("inputFluid").get("tag").getAsString().equals("minecraft:water"))
                .screenshot("neoecoae_tags")
                .rightClick("#neoecoae_output").ticks(2)
                .check("empty outputs remain a draft", context -> root().getAsJsonObject("itemOutput").isEmpty())
                .closeScreen().openModularUI("reopen incomplete draft", context -> openEditor())
                .awaitElement("#neoecoae_output")
                .step("restore item output", context -> ((ItemSlot) context.el("#neoecoae_output").element()).setValue(Items.DIAMOND.getDefaultInstance(), true))
                .ticks(2).check("draft becomes valid", context -> entry.compile().getResultItem(Platform.getFrozenRegistry()).is(Items.DIAMOND))
                .checkHidden("#neoecoae_validation").closeScreen();
        scenario.step("install a workstation source recipe", context -> {
                    originalRecipes = List.copyOf(Minecraft.getInstance().level.getRecipeManager().getRecipes());
                    entry = fixture("integrated_working_station"); applyDelta(true, false);
                }).waitUntil("native JEI and derived large category contain recipe", context -> jeiContainsFixture(false) && jeiContainsFixture(true))
                .check("machine cache sees added recipe", context -> runtimeContainsFixture())
                .step("update source output", context -> {
                    var root = root(); root.add("itemOutput", NeoEcoRecipeFactory.encode(ItemStack.CODEC, new ItemStack(Items.AMETHYST_SHARD, 5)));
                    data().setRecipeJson(root.toString()); applyDelta(true, true);
                }).waitUntil("derived JEI output is updated", context -> jeiFixtureOutput(true).is(Items.AMETHYST_SHARD) && jeiFixtureOutput(true).getCount() == 5)
                .check("machine cache sees changed output", context -> LargeWorkstationRecipes.getAll(Minecraft.getInstance().level).stream()
                        .anyMatch(value -> value.id().equals(ViScriptRecipe.id("neoecoae_delta_test")) && value.display().itemOutput().is(Items.AMETHYST_SHARD)))
                .step("disable source recipe", context -> applyDelta(false, true))
                .waitUntil("native and derived JEI remove recipe", context -> !jeiContainsFixture(false) && !jeiContainsFixture(true))
                .check("machine cache removes recipe", context -> !runtimeContainsFixture())
                .teardown("restore recipe manager", context -> {
                    if (originalRecipes != null) Minecraft.getInstance().level.getRecipeManager().replaceRecipes(originalRecipes);
                });
    }

    private RecipeEntry fixture(String type) {
        var value = new RecipeEntry().setType(NeoEcoRecipeEditorTypes.id(type)); value.applyDefaultData(); return value;
    }
    private ModularUI openEditor() {
        project = new RecipeProject(); project.getRecipeFile().getEntries().add(entry);
        var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null); project.refreshAiViews(); return new ModularUI(UI.of(editor));
    }
    private NeoEcoRecipeData data() { return entry.getData(); }
    private com.google.gson.JsonObject root() { return JsonParser.parseString(data().getRecipeJson()).getAsJsonObject(); }
    private String canonical() { return NeoEcoRecipeFactory.encode(entry.compile(), Platform.getFrozenRegistry()); }
    private void applyDelta(boolean add, boolean remove) {
        var id = ViScriptRecipe.id("neoecoae_delta_test"); RecipeHolder<?> holder = new RecipeHolder<>(id, entry.compile());
        var recipes = new java.util.ArrayList<RecipeHolder<?>>(originalRecipes); if (add) recipes.add(holder);
        Minecraft.getInstance().level.getRecipeManager().replaceRecipes(recipes);
        RecipeDeltaJeiSynchronizer.applyDelta(++deltaRevision, Set.of(id), remove ? Map.of(id, holder) : Map.of(),
                add ? Map.of(id, holder) : Map.of(), remove ? Map.of(id, entry.getType()) : Map.of(), add ? Map.of(id, entry.getType()) : Map.of());
    }
    private boolean runtimeContainsFixture() {
        return LargeWorkstationRecipes.getGroup(Minecraft.getInstance().level, ViScriptRecipe.id("neoecoae_delta_test")) != null;
    }
    private boolean jeiContainsFixture(boolean large) { return !jeiFixtureOutput(large).isEmpty(); }
    private ItemStack jeiFixtureOutput(boolean large) {
        var runtime = LDLibJEIPlugin.jeiRuntime; if (runtime == null) return ItemStack.EMPTY;
        var type = runtime.getRecipeManager().getRecipeType(NeoEcoRecipeEditorTypes.id(large ? "large_integrated_working_station" : "integrated_working_station")).orElseThrow();
        return runtime.getRecipeManager().createRecipeLookup(type).get().map(value -> {
            if (value instanceof RecipeHolder<?> holder && holder.id().getPath().endsWith("neoecoae_delta_test")) return holder.value().getResultItem(Platform.getFrozenRegistry());
            if (value instanceof LargeWorkstationRecipe recipe && recipe.id().getPath().endsWith("neoecoae_delta_test")) return recipe.display().itemOutput();
            return ItemStack.EMPTY;
        }).filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
    }
    private void verifyNativeImports() {
        int tested = 0;
        for (var holder : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
            if (!NeoEcoRecipeImporter.INSTANCE.canImport(holder)) continue;
            var imported = NeoEcoRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry());
            var expected = NeoEcoRecipeFactory.encode(holder.value(), Platform.getFrozenRegistry());
            if (!expected.equals(NeoEcoRecipeFactory.encode(imported.entry().compile(), Platform.getFrozenRegistry()))) throw new AssertionError("Import changed " + holder.id());
            var copy = new RecipeEntry(); copy.deserializeNBT(Platform.getFrozenRegistry(), imported.entry().serializeNBT(Platform.getFrozenRegistry()));
            if (!expected.equals(NeoEcoRecipeFactory.encode(copy.compile(), Platform.getFrozenRegistry()))) throw new AssertionError("Persistence changed " + holder.id());
            var delta = new RecipeDeltaSnapshot(1, 2, false, false, List.of(), List.of(holder), Map.of(), Map.of());
            var decoded = RecipeDeltaSnapshot.deserialize(Platform.getFrozenRegistry(), delta.serialize(Platform.getFrozenRegistry()));
            if (!expected.equals(NeoEcoRecipeFactory.encode(decoded.upsertedRecipes().getFirst().value(), Platform.getFrozenRegistry()))) throw new AssertionError("Network changed " + holder.id());
            tested++;
        }
        if (tested == 0) throw new AssertionError("No native Neo ECO recipes loaded");
        ViScriptRecipe.LOGGER.info("Neo ECO native import/persistence/network round trips: {}", tested);
    }
}
