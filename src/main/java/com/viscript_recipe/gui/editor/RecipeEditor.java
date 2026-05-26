package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorLayout;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.viscript_recipe.ViScriptRecipe;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class RecipeEditor extends Editor {
    public static final ResourceLocation WINDOW_ID = ViScriptRecipe.id("recipe_editor");

    public RecipeEditor() {
        hideBottomWindow();
    }

    public static ModularUI createUI() {
        return new ModularUI(UI.of(EditorWindow.open(WINDOW_ID, RecipeEditor::new)))
                .shouldCloseOnEsc(false)
                .shouldCloseOnKeyInventory(false);
    }

    @Override
    protected @Nonnull Editor createNewEditorInstance() {
        return new RecipeEditor();
    }

    @Override
    protected void onPrepareHistoryView() {
    }

    @Override
    protected void onPrepareInspectorView() {
    }

    @Override
    protected void onPrepareResourceView() {
    }

    @Override
    public void applyLayout(EditorLayout layout) {
        super.applyLayout(layout);
        hideBottomWindow();
    }

    @Override
    protected void initMenus() {
        super.initMenus();
        fileMenu.addProjectProvider(RecipeProjectType.TYPE);
    }

    private void hideBottomWindow() {
        var parent = bottomWindow.getParentWindow();
        if (parent != null) {
            bottomWindow.setDisplay(TaffyDisplay.NONE);
            if (parent.getSplitView() != null) {
                parent.getSplitView().setBorderSize(0);
            }
            parent.splitStyle(style -> style
                    .minPercentage(0)
                    .maxPercentage(100)
                    .percentage(100)
                    .minPercentage(100));
        }
    }
}
