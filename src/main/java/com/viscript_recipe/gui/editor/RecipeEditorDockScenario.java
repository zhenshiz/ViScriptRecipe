package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.settings.AppearanceSettings;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ElementBounds;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_lib.gui.editor.ViScriptEditorWindow;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.nbt.CompoundTag;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/** 验证正式编辑器从独立窗口返回后保留容器、JEI、草稿和还原尺寸。 */
@LDLRegisterClient(name = "recipe_editor_dock", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class RecipeEditorDockScenario implements UIScenario {
    private ViScriptEditorWindow window;
    private RecipeEditor editor;
    private RecipeProject project;
    private CompoundTag before;
    private final AtomicInteger removals = new AtomicInteger();

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("editor", "window", "xei"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.server("open the registered recipe editor menu", context ->
                        PlayerUIMenuType.openUI(context.player(), RecipeEditor.WINDOW_ID))
                .awaitScreen(ModularUIContainerScreen.class).awaitModularUI()
                .step("prepare an unsaved project in the production editor", context -> {
                    window = (ViScriptEditorWindow) context.requireUI().ui.rootElement;
                    editor = (RecipeEditor) window.getCurrentEditor();
                    editor.getEditorSettings().setSettingsFile(Path.of(System.getProperty("ldlib2.uitest.out"), "dock-settings.json").toFile());
                    editor.addEventListener(UIEvents.REMOVED, event -> removals.incrementAndGet());
                    project = new RecipeProject();
                    var entry = new RecipeEntry(); entry.applyDefaultData();
                    project.getRecipeFile().getEntries().add(entry);
                    editor.loadProject(project, null);
                    before = project.serializeNBT(Platform.getFrozenRegistry()).copy();
                    window.retoreWindow();
                }).ticks(3)
                .waitUntil("JEI is visible beside the restored editor", context -> jeiVisible())
                .check("editor starts restored", context -> !window.isMaximized())
                .step("click the production pop-out button", context -> {
                    var bounds = ElementBounds.of(editor.buttonContainer.selectId(EditorWindow.HOST_TOGGLE_ID).findFirst().orElseThrow());
                    context.input().mouseDown(bounds.centerX(), bounds.centerY(), 0);
                    context.input().mouseUp(bounds.centerX(), bounds.centerY(), 0);
                }).waitUntil("editor is in an independent window", context -> window.isInOsWindow())
                .check("game screen closes while the editor stays alive", context -> context.mc().screen == null && removals.get() == 0)
                .step("click the production dock button in the OS window", context -> context.input(window.getOsWindow())
                        .moveTo(editor.buttonContainer.selectId(EditorWindow.HOST_TOGGLE_ID).findFirst().orElseThrow())
                        .mouseDown(0).mouseUp(0))
                .awaitScreen(ModularUIContainerScreen.class).awaitModularUI().ticks(3)
                .check("same VSL window and editor are restored", context -> context.requireUI().ui.rootElement == window && window.getCurrentEditor() == editor)
                .check("unsaved project survives docking", context -> editor.getCurrentProject() == project
                        && before.equals(project.serializeNBT(Platform.getFrozenRegistry())))
                .check("restored size survives docking", context -> !window.isMaximized())
                .check("editor is never removed during rehosting", context -> removals.get() == 0)
                .waitUntil("JEI is visible again after docking", context -> jeiVisible())
                .screenshot("recipe-editor-docked-with-jei")
                .step("pop out and minimize the same editor", context -> {
                    context.require("pop out succeeds", window.popOutToOsWindow());
                    window.minimizeWindow();
                }).waitUntil("independent window closes", context -> !window.isInOsWindow())
                .server("restore the minimized menu", context -> PlayerUIMenuType.openUI(context.player(), RecipeEditor.WINDOW_ID))
                .awaitScreen(ModularUIContainerScreen.class).awaitModularUI().ticks(3)
                .check("ordinary minimize also retains the same editor and draft", context -> context.requireUI().ui.rootElement == window
                        && editor.getCurrentProject() == project && removals.get() == 0)
                .check("ordinary minimize restores the original size", context -> !window.isMaximized())
                .waitUntil("JEI is visible after minimize", context -> jeiVisible())
                .teardown("close the editor without changing its saved host preference", context -> {
                    if (editor != null) editor.getEditorSettings().getSettings(AppearanceSettings.ID).ifPresent(settings -> {
                        if (settings instanceof AppearanceSettings appearance) appearance.setPreferOsWindow(false);
                    });
                    if (window != null && window.isInOsWindow()) window.getOsWindow().close();
                    if (context.player() != null) context.player().closeContainer();
                    context.mc().setScreen(null);
                });
    }

    private static boolean jeiVisible() {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        return runtime != null && runtime.getIngredientListOverlay().isListDisplayed();
    }
}
