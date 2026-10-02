package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.compat.goety.canvas.RitualCanvas;
import com.viscript_recipe.compat.goety.data.GoetyRitualRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.gui.views.WorkBenchView;
import net.minecraft.world.item.Items;

@LDLRegisterClient(name = "goety_category_switch", group = ViScriptRecipe.MOD_ID, modID = "goety",
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class GoetyCategorySwitchScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) {
        options.tags("recipes", "goety").defaultTimeoutMs(30_000);
    }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.openModularUI("打开祭坛和工作台配方", context -> {
            var project = new RecipeProject();
            var ritual = new RecipeEntry().setType(GoetyRecipeEditorTypes.RITUAL);
            var shaped = new RecipeEntry().setType(RecipeEditorTypes.CRAFTING_SHAPED);
            project.getRecipeFile().getEntries().add(ritual);
            project.getRecipeFile().getEntries().add(shaped);
            var editor = new RecipeEditor();
            editor.layout(layout -> layout.widthPercent(100).heightPercent(100));
            editor.loadProject(project, null);
            context.put("project", project);
            context.put("ritual", ritual);
            context.put("shaped", shaped);
            var navigation = context.<NavigationView>getField(project, "navigationView");
            context.put("navigation", navigation);
            navigation.selectEntry(ritual);
            return new ModularUI(UI.of(editor), context.requirePlayer());
        }).awaitModularUI().ticks(3);

        scenario.step("保留旧祭坛输入框的提交回调", context -> {
            var project = context.<RecipeProject>get("project");
            var workbench = context.<WorkBenchView>getField(project, "workbenchView");
            var canvas = (RitualCanvas) workbench.getCanvas();
            var propertiesView = context.<PropertiesView>getField(project, "propertiesView");
            var properties = context.<UIElement>getField(propertiesView, "content");
            context.put("oldCanvas", canvas);
            context.put("oldSoulCost", properties.select("*")
                    .filter(element -> element instanceof TextField field && field.getValue().equals("0"))
                    .map(TextField.class::cast).findFirst().orElseThrow());
        });
        for (int i = 0; i < 3; i++) {
            scenario.step("切换到工作台 " + i, context ->
                    context.<NavigationView>get("navigation").setSelectedCategoryId(
                            RecipeEditorTypes.requireCategory(RecipeEditorTypes.CRAFTING_TABLE)))
                    .ticks(2)
                    .check("工作台保持有序合成数据", context -> context.<NavigationView>get("navigation")
                            .getSelectedEntry().getData() instanceof ShapedCraftingRecipeData)
                    .step("切换到祭坛 " + i, context ->
                            context.<NavigationView>get("navigation").setSelectedCategoryId(
                                    RecipeEditorTypes.requireCategory(GoetyRecipeEditorTypes.DARK_ALTAR)))
                    .ticks(2)
                    .check("祭坛保持仪式数据", context -> context.<NavigationView>get("navigation")
                            .getSelectedEntry().getData() instanceof GoetyRitualRecipeData);
        }
        scenario.screenshot("goety_ritual")
                .step("旧属性提交不得读取工作台数据", context -> {
                    context.<NavigationView>get("navigation").setSelectedCategoryId(
                            RecipeEditorTypes.requireCategory(RecipeEditorTypes.CRAFTING_TABLE));
                    context.<TextField>get("oldSoulCost").setText("123");
                    context.check("旧回调更新原仪式", context.<RecipeEntry>get("ritual")
                            .<GoetyRitualRecipeData>getData().getSoulCost() == 123);
                    context.check("工作台产物未被修改", context.<RecipeEntry>get("shaped")
                            .<ShapedCraftingRecipeData>getData().getResult().is(Items.CRAFTING_TABLE));
                    var oldCanvas = context.<RitualCanvas>get("oldCanvas");
                    oldCanvas.setVisualOutput(0, Items.DIAMOND.getDefaultInstance());
                    oldCanvas.save();
                    context.<RecipeProject>get("project").saveCurrentVisualState();
                    context.check("旧画布槽位只写入原仪式", context.<RecipeEntry>get("ritual")
                            .<GoetyRitualRecipeData>getData().getResult().is(Items.DIAMOND));
                    context.check("新画布槽位保持工作台产物", context.<RecipeEntry>get("shaped")
                            .<ShapedCraftingRecipeData>getData().getResult().is(Items.CRAFTING_TABLE));
                    context.<RecipeEntry>get("ritual").setType(RecipeEditorTypes.CRAFTING_SHAPED);
                    context.<TextField>get("oldSoulCost").setText("456");
                    context.check("原条目切换类型后旧画布仍绑定原数据", oldCanvas.getData().getSoulCost() == 456);
                    context.<RecipeEntry>get("ritual").setType(GoetyRecipeEditorTypes.RITUAL);
                }).screenshot("goety_switched_to_crafting").closeScreen();
    }
}
