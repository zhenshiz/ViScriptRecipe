package com.viscript_recipe.compat.ae2cs.uitest;

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
import com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeEditorTypes;
import com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeFactory;
import com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeImporter;
import com.viscript_recipe.compat.ae2cs.data.Ae2CrystalScienceRecipeData;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.compat.jei.ae2cs.Ae2CrystalScienceJeiCanvas;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 验证原生定义往返、JEI 画布、材料数量、未完成草稿与分类同步。 */
@LDLRegisterClient(name = "ae2cs_recipes", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = Ae2CrystalScienceRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class Ae2CrystalScienceRecipeScenario implements UIScenario {
    private RecipeProject project;
    private RecipeEntry entry;
    private String before;
    private List<RecipeHolder<?>> originalRecipes;
    private Object deltaRuntime;
    private long deltaRevision = 100;

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "ae2cs"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI is ready", context -> LDLibJEIPlugin.jeiRuntime != null)
                .step("round-trip every native definition and delta packet", context -> verifyNativeImports());
        for (var type : Ae2CrystalScienceRecipeEditorTypes.TYPES) {
            scenario.openModularUI("open " + type, context -> {
                entry = fixture(type); before = canonical(); return openEditor();
            }).awaitElement("#ae2cs_canvas").ticks(2)
                    .check("original JEI category is present", context ->
                            context.el("#ae2cs_jei_panel").element().getChildren().getFirst() instanceof Ae2CrystalScienceJeiCanvas canvas && canvas.isAvailable())
                    .check("canvas is centered", context -> {
                        var body = context.el("#ae2cs_canvas").element(); var parent = context.el("#ae2cs_canvas_container").element();
                        return Math.abs(body.getPositionX() + body.getSizeWidth() / 2 - parent.getPositionX() - parent.getSizeWidth() / 2) < 1;
                    })
                    .step("save unchanged", context -> project.saveCurrentVisualState())
                    .check("native definition stays identical", context -> before.equals(canonical()))
                    .click("#ae2cs_input_0").awaitElement("#ae2cs_stack_count")
                    .focus("#ae2cs_stack_count")
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                    .type("4096").key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                    .waitUntil("large count is saved", context -> root().getAsJsonObject(type.equals("crystal_pulverizer") ? "input" : "input_a").get("count").getAsInt() == 4096)
                    .checkCount("#ae2cs_item_stack #ae2cs_stack_count", 1)
                    .step("compile edited recipe", context -> entry.compile())
                    .checkHidden("#ae2cs_validation").hover("#ae2cs_canvas").screenshot("ae2cs_" + type)
                    .screenshotElement("ae2cs_card_" + type, "#ae2cs_canvas")
                    .closeScreen();
        }
        scenario.openModularUI("import native etching recipe with tags", context -> {
                    var holder = Minecraft.getInstance().level.getRecipeManager().byKey(
                            Ae2CrystalScienceRecipeEditorTypes.id("circuit_etcher/calculation_processor")).orElseThrow();
                    entry = Ae2CrystalScienceRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry()).entry();
                    before = canonical(); return openEditor();
                }).awaitElement("#ae2cs_canvas").ticks(25)
                .step("save cycled tag display", context -> project.saveCurrentVisualState())
                .check("native tag inputs stay unchanged while display cycles", context -> before.equals(canonical()))
                .checkHidden("#ae2cs_validation").hover("#ae2cs_canvas").screenshot("ae2cs_native_etching")
                .screenshotElement("ae2cs_native_etching_card", "#ae2cs_canvas").closeScreen();
        scenario.openModularUI("sparse and compound ingredients", context -> {
                    entry = fixture("circuit_etcher"); var root = root();
                    root.remove("input_a");
                    root.add("input_b", JsonParser.parseString("{\"type\":\"neoforge:compound\",\"ingredients\":[{\"tag\":\"c:ingots/iron\"},{\"item\":\"minecraft:copper_ingot\"}],\"count\":3}"));
                    data().setRecipeJson(root.toString()); before = canonical(); return openEditor();
                }).awaitElement("#ae2cs_input_0").click("#ae2cs_input_0").ticks(2)
                .check("compound ingredient and empty fields survive", context -> before.equals(canonical()))
                .screenshot("ae2cs_compound_ingredient")
                .step("fill an empty phantom slot", context -> ((ItemSlot) context.el("#ae2cs_input_1").element()).setValue(new ItemStack(Items.DIAMOND, 2), true))
                .ticks(2)
                .check("empty slot accepts material and count", context -> root().getAsJsonObject("input_a").get("count").getAsInt() == 2)
                .check("compound input stays intact after another slot changes", context -> root().getAsJsonObject("input_b").get("type").getAsString().equals("neoforge:compound"))
                .step("edit output components through phantom slot", context -> {
                    var stack = new ItemStack(Items.DIAMOND, 3); stack.set(DataComponents.CUSTOM_NAME, Component.literal("Crystal test"));
                    ((ItemSlot) context.el("#ae2cs_output_0").element()).setValue(stack, true);
                }).ticks(2)
                .check("result components and count survive", context -> {
                    var stack = entry.compile().getResultItem(Platform.getFrozenRegistry());
                    return stack.getCount() == 3 && stack.getHoverName().getString().equals("Crystal test");
                }).rightClick("#ae2cs_output_0").ticks(2)
                .check("cleared output is an editable draft", context -> root().getAsJsonObject("result").isEmpty())
                .closeScreen().openModularUI("reopen incomplete draft", context -> openEditor())
                .awaitElement("#ae2cs_output_0")
                .step("restore output", context -> ((ItemSlot) context.el("#ae2cs_output_0").element()).setValue(Items.EMERALD.getDefaultInstance(), true))
                .ticks(2)
                .check("restored draft compiles", context -> entry.compile().getResultItem(Platform.getFrozenRegistry()).is(Items.EMERALD))
                .screenshot("ae2cs_restored_output").closeScreen()
                .step("add recipe using normal JEI delta mapping", context -> {
                    originalRecipes = List.copyOf(Minecraft.getInstance().level.getRecipeManager().getRecipes());
                    entry = fixture("crystal_aggregator"); deltaRuntime = LDLibJEIPlugin.jeiRuntime; applyJeiDelta(true, false);
                }).waitUntil("JEI receives added crystal recipe", context -> jeiContainsFixture())
                .check("new recipe is visible in native JEI category", context -> jeiContainsFixture())
                .check("JEI uses the expected route for installed integrations", context -> ViScriptRecipe.isModLoaded("molecularmanipulator")
                        ? deltaRuntime != LDLibJEIPlugin.jeiRuntime : deltaRuntime == LDLibJEIPlugin.jeiRuntime)
                .step("update existing JEI recipe", context -> {
                    var root = root(); root.add("result", Ae2CrystalScienceRecipeFactory.encode(ItemStack.CODEC, new ItemStack(Items.EMERALD, 5)));
                    data().setRecipeJson(root.toString()); applyJeiDelta(true, true);
                }).waitUntil("JEI receives edited result", context -> jeiFixtureResult().is(Items.EMERALD) && jeiFixtureResult().getCount() == 5)
                .check("edited output appears in existing JEI category", context -> jeiFixtureResult().getCount() == 5)
                .step("disable recipe using normal JEI delta mapping", context -> applyJeiDelta(false, true))
                .waitUntil("JEI removes disabled crystal recipe", context -> !jeiContainsFixture())
                .check("disabled recipe disappears", context -> !jeiContainsFixture())
                .teardown("restore recipe manager", context -> {
                    if (originalRecipes != null) Minecraft.getInstance().level.getRecipeManager().replaceRecipes(originalRecipes);
                });
    }

    private RecipeEntry fixture(String type) {
        var value = new RecipeEntry().setType(Ae2CrystalScienceRecipeEditorTypes.id(type)); value.applyDefaultData(); return value;
    }
    private ModularUI openEditor() {
        project = new RecipeProject(); project.getRecipeFile().getEntries().add(entry);
        var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null); project.refreshAiViews(); return new ModularUI(UI.of(editor));
    }
    private Ae2CrystalScienceRecipeData data() { return entry.getData(); }
    private com.google.gson.JsonObject root() { return JsonParser.parseString(data().getRecipeJson()).getAsJsonObject(); }
    private String canonical() { return Ae2CrystalScienceRecipeFactory.encode(entry.compile(), Platform.getFrozenRegistry()); }

    private void applyJeiDelta(boolean add, boolean remove) {
        var id = ViScriptRecipe.id("ae2cs_delta_test");
        RecipeHolder<?> holder = new RecipeHolder<>(id, entry.compile());
        var recipes = new java.util.ArrayList<RecipeHolder<?>>(originalRecipes);
        if (add) recipes.add(holder);
        Minecraft.getInstance().level.getRecipeManager().replaceRecipes(recipes);
        RecipeDeltaJeiSynchronizer.applyDelta(++deltaRevision, Set.of(id), remove ? Map.of(id, holder) : Map.of(),
                add ? Map.of(id, holder) : Map.of(), remove ? Map.of(id, entry.getType()) : Map.of(), add ? Map.of(id, entry.getType()) : Map.of());
    }

    private boolean jeiContainsFixture() { return !jeiFixtureResult().isEmpty(); }

    private ItemStack jeiFixtureResult() {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) return ItemStack.EMPTY;
        var type = runtime.getRecipeManager().getRecipeType(Ae2CrystalScienceRecipeEditorTypes.id("crystal_aggregator")).orElseThrow();
        return runtime.getRecipeManager().createRecipeLookup(type).get().filter(value -> value instanceof RecipeHolder<?> holder
                && holder.id().getPath().endsWith("ae2cs_delta_test"))
                .map(value -> ((RecipeHolder<?>) value).value().getResultItem(Platform.getFrozenRegistry())).findFirst().orElse(ItemStack.EMPTY);
    }

    private void verifyNativeImports() {
        int tested = 0;
        for (var holder : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
            if (!Ae2CrystalScienceRecipeImporter.INSTANCE.canImport(holder)) continue;
            var imported = Ae2CrystalScienceRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry());
            var expected = Ae2CrystalScienceRecipeFactory.encode(holder.value(), Platform.getFrozenRegistry());
            if (!expected.equals(Ae2CrystalScienceRecipeFactory.encode(imported.entry().compile(), Platform.getFrozenRegistry())))
                throw new AssertionError("Import changed " + holder.id());
            var copy = new RecipeEntry(); copy.deserializeNBT(Platform.getFrozenRegistry(), imported.entry().serializeNBT(Platform.getFrozenRegistry()));
            if (!expected.equals(Ae2CrystalScienceRecipeFactory.encode(copy.compile(), Platform.getFrozenRegistry())))
                throw new AssertionError("Persistence changed " + holder.id());
            var delta = new RecipeDeltaSnapshot(1, 2, false, false, List.of(), List.of(holder), Map.of(), Map.of());
            var decoded = RecipeDeltaSnapshot.deserialize(Platform.getFrozenRegistry(), delta.serialize(Platform.getFrozenRegistry()));
            if (!expected.equals(Ae2CrystalScienceRecipeFactory.encode(decoded.upsertedRecipes().getFirst().value(), Platform.getFrozenRegistry())))
                throw new AssertionError("Network round trip changed " + holder.id());
            tested++;
        }
        if (tested == 0) throw new AssertionError("No native AE2 Crystal Science recipes loaded");
        ViScriptRecipe.LOGGER.info("AE2CS native import/persistence/network round trips: {}", tested);
    }
}
