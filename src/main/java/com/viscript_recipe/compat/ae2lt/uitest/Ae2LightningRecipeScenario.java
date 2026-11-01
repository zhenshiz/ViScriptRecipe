package com.viscript_recipe.compat.ae2lt.uitest;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeEditorTypes;
import com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeFactory;
import com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeImporter;
import com.viscript_recipe.compat.ae2lt.data.Ae2LightningRecipeData;
import com.viscript_recipe.compat.jei.ae2lt.Ae2LightningJeiCanvas;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import java.util.List;
import java.util.Map;

/** 验证七种配方的无损导入、原生 JEI 画布及数量编辑。 */
@LDLRegisterClient(name = "ae2lt_recipes", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = Ae2LightningRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class Ae2LightningRecipeScenario implements UIScenario {
    private RecipeProject project;
    private RecipeEntry entry;
    private String before;

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "ae2lt"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI is ready", context -> LDLibJEIPlugin.jeiRuntime != null)
                .step("round-trip all native definitions", context -> verifyNativeImports())
                .server("resolve and synchronize native harvest output", context -> {
                    var manager = context.server().getRecipeManager();
                    var original = List.copyOf(manager.getRecipes());
                    var id = Ae2LightningRecipeEditorTypes.id("crystal_catalyzer");
                    var root = JsonParser.parseString(Ae2LightningRecipeFactory.defaults(id)).getAsJsonObject();
                    root.add("output", JsonParser.parseString("{\"harvestBlock\":\"minecraft:amethyst_cluster\",\"count\":7}"));
                    var recipe = Ae2LightningRecipeFactory.compile(id, root.toString());
                    var holder = new RecipeHolder<>(ViScriptRecipe.id("ae2lt_harvest_test"), recipe);
                    var changed = new java.util.ArrayList<RecipeHolder<?>>(original); changed.add(holder);
                    try {
                        manager.replaceRecipes(changed); Ae2LightningRecipeFactory.resolveHarvestOutputs(manager);
                        if (recipe.getResultItem(context.level().registryAccess()).isEmpty()) throw new AssertionError("Harvest result was not resolved");
                        var delta = new RecipeDeltaSnapshot(1, 2, false, false, List.of(), List.of(holder), Map.of(), Map.of());
                        var decoded = RecipeDeltaSnapshot.deserialize(context.level().registryAccess(), delta.serialize(context.level().registryAccess()));
                        var result = decoded.upsertedRecipes().getFirst().value().getResultItem(context.level().registryAccess());
                        if (result.getCount() != 7 || !result.is(net.minecraft.world.item.Items.AMETHYST_SHARD))
                            throw new AssertionError("Native harvest output was lost during delta synchronization");
                    } finally { manager.replaceRecipes(original); }
                });
        for (var type : Ae2LightningRecipeEditorTypes.TYPES) {
            scenario.openModularUI("open " + type, context -> {
                entry = new RecipeEntry().setType(Ae2LightningRecipeEditorTypes.id(type)); entry.applyDefaultData();
                return openEntry();
            }).awaitElement("#ae2lt_canvas").ticks(3)
                    .check("original JEI category is present", context -> {
                        var preview = context.el("#ae2lt_jei_panel").element().getChildren().getFirst();
                        return preview instanceof Ae2LightningJeiCanvas canvas && canvas.isAvailable();
                    })
                    .check("preview is centered", context -> {
                        var body = context.el("#ae2lt_canvas").element();
                        var parent = context.el("#ae2lt_canvas_container").element();
                        return Math.abs(body.getPositionX() + body.getSizeWidth() / 2 - parent.getPositionX() - parent.getSizeWidth() / 2) < 1;
                    })
                    .step("save without changes", context -> project.saveCurrentVisualState())
                    .check("untouched definition remains identical", context -> before.equals(canonical()));
            if (!type.equals("lightning_strike")) {
                scenario.click("#ae2lt_input_0").awaitElement("#ae2lt_stack_count")
                        .focus("#ae2lt_stack_count")
                        .key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                        .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                        .type("4096").key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                        .waitUntil("large input count is saved", context -> firstCount() == 4096)
                        .checkCount("#ae2lt_item_stack #ae2lt_stack_count", 1)
                        .step("compile edited recipe", context -> entry.compile());
            }
            scenario.checkHidden("#ae2lt_validation").hover("#ae2lt_canvas").screenshot("ae2lt_" + type)
                    .screenshotElement("ae2lt_card_" + type, "#ae2lt_canvas").closeScreen();
        }
        scenario.openModularUI("edit candidate ingredient", context -> {
            entry = new RecipeEntry().setType(Ae2LightningRecipeEditorTypes.id("lightning_assembly")); entry.applyDefaultData();
            var root = JsonParser.parseString(data().getRecipeJson()).getAsJsonObject();
            root.getAsJsonArray("inputs").get(0).getAsJsonObject().add("ingredient", JsonParser.parseString("[{\"tag\":\"c:ingots/iron\"},{\"item\":\"minecraft:copper_ingot\"}]"));
            data().setRecipeJson(root.toString()); return openEntry();
        }).awaitElement("#ae2lt_input_0").click("#ae2lt_input_0")
                .step("save candidate ingredient", context -> project.saveCurrentVisualState())
                .check("candidate list and tag survive", context -> before.equals(canonical()))
                .screenshot("ae2lt_alternatives").closeScreen()
                .openModularUI("open incomplete output fixture", context -> {
                    entry = new RecipeEntry().setType(Ae2LightningRecipeEditorTypes.id("lightning_assembly")); entry.applyDefaultData();
                    return openEntry();
                }).awaitElement("#ae2lt_output_0").rightClick("#ae2lt_output_0").ticks(2)
                .check("cleared output remains an editable draft", context -> JsonParser.parseString(data().getRecipeJson()).getAsJsonObject().getAsJsonObject("result").isEmpty())
                .screenshot("ae2lt_incomplete_output").closeScreen()
                .openModularUI("reopen incomplete recipe", context -> openEditor())
                .awaitElement("#ae2lt_output_0")
                .step("replace cleared output through the phantom slot", context ->
                        ((com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot) context.el("#ae2lt_output_0").element())
                                .setValue(net.minecraft.world.item.Items.DIAMOND.getDefaultInstance(), true))
                .ticks(3)
                .check("restored draft compiles", context -> entry.compile().getResultItem(Platform.getFrozenRegistry()).is(net.minecraft.world.item.Items.DIAMOND))
                .check("restored draft uses native JEI again", context -> context.el("#ae2lt_jei_panel").element().getChildren().getFirst() instanceof Ae2LightningJeiCanvas canvas && canvas.drawsIngredients())
                .screenshot("ae2lt_restored_output").closeScreen()
                .openModularUI("open fluid input fixture", context -> {
                    entry = new RecipeEntry().setType(Ae2LightningRecipeEditorTypes.id("crystal_catalyzer")); entry.applyDefaultData();
                    return openEntry();
                }).awaitElement("#ae2lt_fluid_input_0")
                .step("accept a fluid through the JEI phantom slot", context ->
                        ((com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot) context.el("#ae2lt_fluid_input_0").element())
                                .setValue(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 2000)))
                .ticks(2)
                .check("dropped fluid and amount are saved", context -> {
                    var fluid = JsonParser.parseString(data().getRecipeJson()).getAsJsonObject().getAsJsonObject("inputFluid");
                    return fluid.get("id").getAsString().equals("minecraft:lava") && fluid.get("amount").getAsInt() == 2000;
                }).click("#ae2lt_fluid_input_0").screenshot("ae2lt_fluid_edit").closeScreen();
    }

    private ModularUI openEntry() {
        before = canonical();
        return openEditor();
    }
    private ModularUI openEditor() {
        project = new RecipeProject(); project.getRecipeFile().getEntries().add(entry);
        var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null); project.refreshAiViews(); return new ModularUI(UI.of(editor));
    }
    private Ae2LightningRecipeData data() { return entry.getData(); }
    private String canonical() { return Ae2LightningRecipeFactory.encode(entry.compile(), Platform.getFrozenRegistry()); }
    private int firstCount() {
        var root = JsonParser.parseString(data().getRecipeJson()).getAsJsonObject();
        return entry.getType().getPath().equals("crystal_catalyzer") ? root.get("catalystCount").getAsInt()
                : root.getAsJsonArray("inputs").get(0).getAsJsonObject().get("count").getAsInt();
    }
    private void verifyNativeImports() {
        int tested = 0;
        for (var holder : net.minecraft.client.Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
            if (!Ae2LightningRecipeImporter.INSTANCE.canImport(holder)) continue;
            var imported = Ae2LightningRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry());
            var expected = Ae2LightningRecipeFactory.encode(holder.value(), Platform.getFrozenRegistry());
            if (!expected.equals(Ae2LightningRecipeFactory.encode(imported.entry().compile(), Platform.getFrozenRegistry())))
                throw new AssertionError("Import changed " + holder.id());
            var copy = new RecipeEntry(); copy.deserializeNBT(Platform.getFrozenRegistry(), imported.entry().serializeNBT(Platform.getFrozenRegistry()));
            if (!expected.equals(Ae2LightningRecipeFactory.encode(copy.compile(), Platform.getFrozenRegistry())))
                throw new AssertionError("Persistence changed " + holder.id());
            tested++;
        }
        if (tested == 0) throw new AssertionError("No native AE2LT recipes loaded");
        ViScriptRecipe.LOGGER.info("AE2LT native import round trips: {}", tested);
    }
}
