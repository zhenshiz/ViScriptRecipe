package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Menu;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.TestContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

/** 验证菜单筛选、勾选提示、跨工作站显示及启用后的即时移除。 */
@LDLRegisterClient(name = "recipe_disabled_filter", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class RecipeDisabledFilterScenario implements UIScenario {
    private RecipeProject project;
    private CompoundTag before;

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("recipe", "filter"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.openModularUI("open recipe editor with mixed enabled states", context -> openEditor())
                .awaitElement("#recipe_menu")
                .check("filter defaults to off", context -> !project.isShowDisabledRecipesOnly())
                .checkCount(".recipe_entry_row", 2)
                .click("#recipe_menu")
                .step("locate unchecked filter option", RecipeDisabledFilterScenario::locateOption)
                .checkText("#recipe_disabled_filter_text", filterName(false))
                .hover("#recipe_disabled_filter_option")
                .step("toggle disabled recipe filter", context -> clickTransient(context, "#recipe_disabled_filter_option"))
                .check("filter is enabled", context -> project.isShowDisabledRecipesOnly())
                .checkCount(".recipe_entry_row", 2)
                .check("disabled recipes from both workstations are visible", context ->
                        context.query(".recipe_entry_row button").withTextContaining("filter_fixture:disabled_crafting").count() == 1
                                && context.query(".recipe_entry_row button").withTextContaining("filter_fixture:disabled_smelting").count() == 1)
                .check("filtering preserves saved recipe data", context -> before.equals(project.serializeRuntimeFile(Platform.getFrozenRegistry())))
                .click("#recipe_menu")
                .step("locate checked filter option", RecipeDisabledFilterScenario::locateOption)
                .checkText("#recipe_disabled_filter_text", filterName(true))
                .screenshot("disabled_recipe_filter_checked")
                .hover("#recipe_disabled_filter_option")
                .step("toggle disabled recipe filter", context -> clickTransient(context, "#recipe_disabled_filter_option"))
                .check("second click disables the filter", context -> !project.isShowDisabledRecipesOnly())
                .checkCount(".recipe_entry_row", 2)
                .check("enabled recipe is visible again", context ->
                        context.query(".recipe_entry_row button").withTextContaining("filter_fixture:enabled_crafting").count() == 1)
                .click("#recipe_menu")
                .step("locate filter option after turning it off", RecipeDisabledFilterScenario::locateOption)
                .checkText("#recipe_disabled_filter_text", filterName(false))
                .hover("#recipe_disabled_filter_option")
                .step("toggle disabled recipe filter", context -> clickTransient(context, "#recipe_disabled_filter_option"))
                .hover("#recipe_entry_enabled switch")
                .step("enable selected recipe", context -> clickTransient(context, "#recipe_entry_enabled switch"))
                .checkCount(".recipe_entry_row", 1)
                .check("newly enabled crafting recipe is hidden immediately", context ->
                        context.query(".recipe_entry_row button").withTextContaining("filter_fixture:disabled_crafting").count() == 0)
                .hover("#recipe_entry_enabled switch")
                .step("enable selected recipe", context -> clickTransient(context, "#recipe_entry_enabled switch"))
                .checkCount(".recipe_entry_row", 0)
                .check("all recipes are enabled", context -> project.getRecipeFile().getEntries().stream().allMatch(RecipeEntry::isEnabled))
                .screenshot("disabled_recipe_filter_empty")
                .check("another file starts without a filter", context -> !new RecipeProject().isShowDisabledRecipesOnly())
                .closeScreen();
    }

    private ModularUI openEditor() {
        project = new RecipeProject();
        project.getRecipeFile().getEntries().add(entry("enabled_crafting", RecipeEditorTypes.CRAFTING_SHAPELESS, true));
        project.getRecipeFile().getEntries().add(entry("disabled_crafting", RecipeEditorTypes.CRAFTING_SHAPELESS, false));
        project.getRecipeFile().getEntries().add(entry("disabled_smelting", RecipeEditorTypes.SMELTING, false));
        var editor = new RecipeEditor();
        editor.layout(l -> l.widthPercent(100).heightPercent(100));
        editor.loadProject(project, null);
        project.selectEntry(project.getRecipeFile().getEntries().getFirst());
        before = project.serializeRuntimeFile(Platform.getFrozenRegistry()).copy();
        return new ModularUI(UI.of(editor));
    }

    private static RecipeEntry entry(String path, ResourceLocation type, boolean enabled) {
        var entry = new RecipeEntry().setType(type).setRecipeId(ResourceLocation.fromNamespaceAndPath("filter_fixture", path)).setEnabled(enabled);
        entry.applyDefaultData();
        return entry;
    }

    private static String filterName(boolean checked) {
        return Component.translatable("viscript_recipe.editor.menu.disabled_recipes_only").getString() + (checked ? " √" : "");
    }

    private static void clickTransient(TestContext context, String selector) {
        // 菜单在按下时关闭，启用开关也可能随筛选结果移除，释放时沿用按下前的坐标。
        var bounds = context.el(selector).bounds();
        context.input().mouseDown(bounds.centerX(), bounds.centerY(), 0);
        context.input().mouseUp(bounds.centerX(), bounds.centerY(), 0);
    }

    @SuppressWarnings("unchecked")
    private static void locateOption(TestContext context) {
        var menu = (Menu<Tuple<IGuiTexture, Component>, Runnable>) context.el("menu").element();
        menu.getNodeUIs().entrySet().stream()
                .filter(entry -> TreeBuilder.Menu.getName(entry.getKey().getKey()).getString().equals(filterName(false))
                        || TreeBuilder.Menu.getName(entry.getKey().getKey()).getString().equals(filterName(true)))
                .findFirst().orElseThrow().getValue().setId("recipe_disabled_filter_option");
        context.el("#recipe_disabled_filter_option label").element().setId("recipe_disabled_filter_text");
    }
}
