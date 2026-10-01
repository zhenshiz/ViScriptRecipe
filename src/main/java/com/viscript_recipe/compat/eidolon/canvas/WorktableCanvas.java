package com.viscript_recipe.compat.eidolon.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.eidolon.data.EidolonWorktableRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.views.NavigationView;

public final class WorktableCanvas extends EidolonItemCanvas<EidolonWorktableRecipeData> {
    public WorktableCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() { super.load(); setVisualOutput(0, getData().getResult()); }

    @Override
    public void save() { super.save(); getData().setResult(getVisualOutput(0).getItem()); }

    @Override
    public UIElement createCanvas() {
        var data = getData();
        var panel = EidolonCanvasFactory.page("codex_worktable_page.png", 138, 172);
        for (int y = 0; y < data.getHeight(); y++) for (int x = 0; x < data.getWidth(); x++) {
            int index = y * 3 + x;
            panel.addChild(EidolonCanvasFactory.slot(inputSlot(index, data.getCore().get(index), text("core", index + 1)),
                    43 + x * 17, 36 + y * 17));
        }
        var positions = new int[][]{{60, 14}, {99, 53}, {60, 92}, {21, 53}};
        for (int i = 0; i < 4; i++) panel.addChild(EidolonCanvasFactory.slot(
                inputSlot(i + 9, data.getReagents().get(i), text("worktable_reagent", i + 1)), positions[i][0], positions[i][1]));
        var output = createOutputSlot(0, JEI_SLOT_SIZE); output.setId("eidolon_output");
        panel.addChild(EidolonCanvasFactory.slot(output, 60, 132));
        return EidolonCanvasFactory.centered(panel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.type.eidolon.worktable"),
                intField("viscript_recipe.config.eidolon.width", data.getWidth(), 1, 3,
                        value -> { save(); data.setWidth(value); rebuild(); }),
                intField("viscript_recipe.config.eidolon.height", data.getHeight(), 1, 3,
                        value -> { save(); data.setHeight(value); rebuild(); }));
    }
}
