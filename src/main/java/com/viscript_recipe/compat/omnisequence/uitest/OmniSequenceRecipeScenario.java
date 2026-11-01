package com.viscript_recipe.compat.omnisequence.uitest;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.atir.molecularmanipulator.research.MatterResearchRecipe;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeEditorTypes;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeImporter;
import com.viscript_recipe.compat.jei.omnisequence.OmniSequenceJeiRecipeSupport;
import com.viscript_recipe.compat.omnisequence.data.OmniSequenceRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** 回归原生定义的无损保存、大数量与真实编辑器槽位交互。 */
@LDLRegisterClient(name = "omnisequence_recipes", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = OmniSequenceRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class OmniSequenceRecipeScenario implements UIScenario {
    private RecipeProject project;
    private RecipeEntry entry;
    private String before;

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "omnisequence"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI is ready", context -> com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin.jeiRuntime != null)
                .step("open native fabrication JEI", context -> {
                    var minecraft = net.minecraft.client.Minecraft.getInstance();
                    minecraft.options.guiScale().set(4);
                    minecraft.resizeDisplay();
                    var runtime = com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin.jeiRuntime;
                    var category = runtime.getRecipeManager().getRecipeCategory(com.atir.molecularmanipulator.integration.jei.MatterFabricationJeiCategory.TYPE);
                    var recipes = com.atir.molecularmanipulator.crafting.MatterRecipeIndex.get(net.minecraft.client.Minecraft.getInstance().level)
                            .fabrication().stream().limit(3).map(RecipeHolder::value).toList();
                    runtime.getRecipesGui().showRecipes(category, recipes, List.of());
                })
                .ticks(5)
                .check("native JEI recipes screen is open", context -> net.minecraft.client.Minecraft.getInstance().screen
                        == com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin.jeiRuntime.getRecipesGui())
                .screenshot("omnisequence_native_jei")
                .closeScreen()
                .step("restore editor scale", context -> {
                    var minecraft = net.minecraft.client.Minecraft.getInstance();
                    minecraft.options.guiScale().set(2);
                    minecraft.resizeDisplay();
                });
        for (var type : OmniSequenceRecipeEditorTypes.TYPES) {
            scenario.openModularUI("open default " + type, context -> {
                        entry = new RecipeEntry().setType(OmniSequenceRecipeEditorTypes.id(type));
                        entry.applyDefaultData();
                        return openEntry();
                    })
                    .awaitElement("#omnisequence_input_0")
                    .check("default count is one", context -> firstIngredientCount() == 1)
                    .ticks(5)
                    .check("recipe preview is horizontally centered", context -> centered(context))
                    .step("save untouched default " + type, context -> project.saveCurrentVisualState())
                    .check("default definition survives without edits", context -> before.equals(canonical()))
                    .click("#omnisequence_input_0")
                    .checkExists("#omnisequence_ingredient_count")
                    .check("opening count properties preserves default definition", context -> before.equals(canonical()))
                    .focus("#omnisequence_ingredient_count")
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                    .type("123213")
                    .key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                    .waitUntil("count is saved", context -> firstIngredientCount() == 123213)
                    .checkCount("#omnisequence_item_stack #omnisequence_ingredient_count", 1)
                    .checkNotExists("#omnisequence_count_0")
                    .checkHidden("#omnisequence_validation").hover("#omnisequence_canvas")
                    .screenshot("omnisequence_centered_" + type)
                    .screenshotElement("omnisequence_card_" + type, "#omnisequence_canvas")
                    .screenshotElement("omnisequence_stack_" + type, "#omnisequence_item_stack")
                    .step("save edited count", context -> project.saveCurrentVisualState())
                    .check("edited count survives save and compiles", context -> entry.compile() != null
                            && firstIngredientCount() == 123213)
                    .closeScreen();
        }
        scenario.openModularUI("open actual native recipe in editor", context -> {
                    var holder = com.atir.molecularmanipulator.crafting.MatterRecipeIndex.get(net.minecraft.client.Minecraft.getInstance().level)
                            .fabrication().getFirst();
                    entry = OmniSequenceRecipeImporter.INSTANCE.tryImport(holder, Platform.getFrozenRegistry()).entry();
                    return openEntry();
                })
                .awaitElement("#omnisequence_input_0")
                .ticks(5)
                .check("imported native recipe is centered", context -> centered(context))
                .step("save imported native recipe", context -> project.saveCurrentVisualState())
                .check("native recipe stays identical", context -> before.equals(canonical()))
                .checkHidden("#omnisequence_validation").hover("#omnisequence_canvas")
                .screenshot("omnisequence_native_recipe_editor")
                .screenshotElement("omnisequence_native_recipe_card", "#omnisequence_canvas")
                .closeScreen();
        scenario.step("native defaults, import and persistence", context -> verifyRoundTrips())
                .step("native indexes follow add, replace and remove", context -> verifyNativeIndex())
                .openModularUI("open fabrication editor", context -> open("matter_fabrication"))
                .awaitElement("#omnisequence_input_0")
                .checkExists("#omnisequence_output_0")
                .step("untouched ingredients, components and AE resources survive", context -> project.saveCurrentVisualState())
                .check("untouched fabrication is identical", context -> before.equals(canonical()))
                .click("#omnisequence_input_0")
                .checkExists("#omnisequence_ingredient_count")
                .check("custom alternatives survive selection", context -> before.equals(canonical()))
                .checkHidden("#omnisequence_validation").hover("#omnisequence_canvas")
                .screenshot("omnisequence_fabrication")
                .click("#omnisequence_output_0", 1)
                .step("save cleared output", context -> project.saveCurrentVisualState())
                .check("output removal changes the native definition", context -> JsonParser.parseString(data().getRecipeJson())
                        .getAsJsonObject().getAsJsonArray("results").get(0).getAsJsonObject().size() == 0)
                .closeScreen()
                .openModularUI("open research editor", context -> open("matter_research"))
                .awaitElement("#omnisequence_input_0")
                .step("save research with maximum prerequisites", context -> project.saveCurrentVisualState())
                .check("research preserves long costs and depth overrides", context -> before.equals(canonical())
                        && ((MatterResearchRecipe) entry.compile()).ingredients().getFirst().count() == 9007199254740993L)
                .click("#omnisequence_input_0")
                .checkExists("#omnisequence_ingredient_count")
                .focus("#omnisequence_ingredient_count")
                .key(org.lwjgl.glfw.GLFW.GLFW_KEY_END)
                .key(org.lwjgl.glfw.GLFW.GLFW_KEY_HOME, org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT)
                .type("9007199254740995")
                .key(org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER)
                .check("editing large research counts keeps every digit", context ->
                        ((MatterResearchRecipe) entry.compile()).ingredients().getFirst().count() == 9007199254740995L)
                .checkCount("#omnisequence_item_stack #omnisequence_ingredient_count", 1)
                .screenshot("omnisequence_research")
                .closeScreen()
                .openModularUI("open machine import editor", context -> open("matter_machine_import"))
                .awaitElement("#omnisequence_canvas")
                .step("save machine field mappings", context -> project.saveCurrentVisualState())
                .check("machine mappings and nested research survive", context -> before.equals(canonical()))
                .screenshot("omnisequence_machine_import")
                .closeScreen();
    }

    private boolean centered(com.lowdragmc.lowdraglib2.uitest.TestContext context) {
        var body = context.el("#omnisequence_canvas").element();
        var container = context.el("#omnisequence_canvas_container").element();
        return Math.abs(body.getPositionX() + body.getSizeWidth() / 2 - container.getPositionX() - container.getSizeWidth() / 2) < 1;
    }

    private void verifyRoundTrips() {
        for (var type : OmniSequenceRecipeEditorTypes.TYPES) {
            var fixture = fixture(type);
            var nativeRecipe = fixture.compile();
            var imported = OmniSequenceRecipeImporter.INSTANCE.tryImport(new RecipeHolder<>(ViScriptRecipe.id("omni_test"), nativeRecipe), Platform.getFrozenRegistry());
            if (imported == null || !imported.successful() || !OmniSequenceRecipeFactory.encode(nativeRecipe, Platform.getFrozenRegistry()).equals(
                    OmniSequenceRecipeFactory.encode(imported.entry().compile(), Platform.getFrozenRegistry())))
                throw new AssertionError("Native recipe import changed " + type);
            var copy = new RecipeEntry(); copy.deserializeNBT(Platform.getFrozenRegistry(), fixture.serializeNBT(Platform.getFrozenRegistry()));
            if (!OmniSequenceRecipeFactory.encode(nativeRecipe, Platform.getFrozenRegistry()).equals(
                    OmniSequenceRecipeFactory.encode(copy.compile(), Platform.getFrozenRegistry()))) throw new AssertionError("Persistence changed " + type);
        }
    }

    private void verifyNativeIndex() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        var manager = level.getRecipeManager();
        var original = List.copyOf(manager.getRecipes());
        var id = ViScriptRecipe.id("omni_cache_test");
        var data = fixture("matter_fabrication");
        var first = (com.atir.molecularmanipulator.crafting.MatterFabricationRecipe) data.compile();
        var changed = new java.util.ArrayList<RecipeHolder<?>>(original);
        try {
            changed.add(new RecipeHolder<>(id, first)); manager.replaceRecipes(changed);
            if (com.atir.molecularmanipulator.crafting.MatterRecipeIndex.get(level).fabrication(id).value() != first)
                throw new AssertionError("Added recipe missing from the native index");
            var replacement = JsonParser.parseString(((OmniSequenceRecipeData) data.getData()).getRecipeJson()).getAsJsonObject();
            replacement.addProperty("processing_time", 17);
            var second = (com.atir.molecularmanipulator.crafting.MatterFabricationRecipe) OmniSequenceRecipeFactory.compile(data.getType(), replacement.toString());
            changed.set(changed.size() - 1, new RecipeHolder<>(id, second)); manager.replaceRecipes(changed);
            if (com.atir.molecularmanipulator.crafting.MatterRecipeIndex.get(level).fabrication(id).value() != second)
                throw new AssertionError("Replaced recipe was cached by the native index");
            manager.replaceRecipes(original);
            if (com.atir.molecularmanipulator.crafting.MatterRecipeIndex.get(level).fabrication(id) != null)
                throw new AssertionError("Removed recipe remained in the native index");
            var source = new RecipeHolder<>(ViScriptRecipe.id("omni_import_source"),
                    OmniSequenceRecipeFactory.decode(net.minecraft.world.item.crafting.Recipe.CODEC, JsonParser.parseString("""
                            {"type":"minecraft:smelting","ingredient":{"item":"minecraft:iron_ore"},
                             "result":{"id":"minecraft:iron_ingot"},"experience":0,"cookingtime":200}
                            """)));
            if (!OmniSequenceJeiRecipeSupport.affectsImportedRecipes(List.of(new RecipeHolder<>(id, fixture("matter_machine_import").compile())), List.of(source)))
                throw new AssertionError("Machine import sources must refresh their JEI derivatives");
        } finally { manager.replaceRecipes(original); }
    }

    private ModularUI open(String type) {
        entry = fixture(type);
        return openEntry();
    }

    private ModularUI openEntry() {
        project = new RecipeProject(); project.getRecipeFile().getEntries().add(entry);
        before = canonical();
        var editor = new RecipeEditor();
        editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null);
        project.refreshAiViews();
        return new ModularUI(UI.of(editor));
    }

    private long firstIngredientCount() {
        var object = JsonParser.parseString(data().getRecipeJson()).getAsJsonObject();
        var research = entry.getType().getPath().equals("matter_machine_import") ? object.getAsJsonObject("research") : object;
        var row = research.getAsJsonArray("ingredients").get(0).getAsJsonObject();
        return row.has("count") ? row.get("count").getAsLong() : 1;
    }

    private static RecipeEntry fixture(String type) {
        var id = OmniSequenceRecipeEditorTypes.id(type);
        if (RecipeEditorTypes.get(id).isEmpty()) throw new AssertionError("Optional editor module was not registered");
        var object = JsonParser.parseString(OmniSequenceRecipeFactory.defaults(id)).getAsJsonObject();
        if (type.equals("matter_fabrication")) {
            object.add("ingredients", JsonParser.parseString("""
                    [{"ingredient":[{"item":"minecraft:iron_ingot"},{"item":"minecraft:copper_ingot"}],"count":4096}]
                    """));
            object.add("catalysts", JsonParser.parseString("[{\"ingredient\":{\"item\":\"minecraft:diamond\"},\"count\":2}]"));
            object.add("ae_inputs", OmniSequenceRecipeFactory.encode(GenericStack.CODEC.listOf(),
                    List.of(new GenericStack(AEItemKey.of(Items.REDSTONE), 9007199254740993L))));
            var result = new ItemStack(Items.GOLD_INGOT, 2); result.set(DataComponents.CUSTOM_NAME, Component.literal("Omni test"));
            object.add("results", OmniSequenceRecipeFactory.encode(ItemStack.CODEC.listOf(), List.of(result)));
        } else {
            var research = type.equals("matter_research") ? object : object.getAsJsonObject("research");
            research.getAsJsonArray("ingredients").get(0).getAsJsonObject().addProperty("count", "9007199254740993");
            research.add("prerequisite_levels", JsonParser.parseString("{\"molecularmanipulator:research/ae_foundation\":\"max\"}"));
            research.add("depths", JsonParser.parseString("""
                    [{"parallel":"64","material_multiplier":"2","ingredients":[{"ingredient":{"item":"minecraft:diamond"},"count":"9223372036854775807"}]}]
                    """));
            if (type.equals("matter_machine_import")) object.add("inputs", JsonParser.parseString("""
                    {"items":["/ingredients"],"ae_keys":[{"key_type":"ae2:item","amount_default":1,"fields":{"id":"minecraft:iron_ingot"}}]}
                    """));
        }
        var compiled = OmniSequenceRecipeFactory.compile(id, object.toString());
        return new RecipeEntry().setType(id).setData(new OmniSequenceRecipeData()
                .setRecipeJson(OmniSequenceRecipeFactory.encode(compiled, Platform.getFrozenRegistry())));
    }

    private OmniSequenceRecipeData data() { return entry.getData(); }
    private String canonical() { return OmniSequenceRecipeFactory.encode(entry.compile(), Platform.getFrozenRegistry()); }
}
