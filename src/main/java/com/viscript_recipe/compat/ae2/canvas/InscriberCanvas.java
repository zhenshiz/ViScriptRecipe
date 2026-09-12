package com.viscript_recipe.compat.ae2.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ae2.Ae2RecipeEditorTypes;
import com.viscript_recipe.compat.ae2.data.Ae2InscriberRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import static com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory.*;

/** Edits the middle ingredient, optional upper/lower presses, and the press consumption mode. */
public class InscriberCanvas extends Ae2ItemRecipeCanvas<Ae2InscriberRecipeData> {
    public InscriberCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        var top = createIngredientSlot(0, 18);
        var middle = createIngredientSlot(1, 18);
        var bottom = createIngredientSlot(2, 18);
        var output = createOutputSlot(0, 18);
        configureJeiOverlaySlotVisual(top, middle, bottom, output);
        tooltip(top, "viscript_recipe.editor.ae2.top_press");
        tooltip(middle, "viscript_recipe.editor.ae2.middle");
        tooltip(bottom, "viscript_recipe.editor.ae2.bottom_press");
        var texture = SpriteTexture.of(Ae2RecipeEditorTypes.id("textures/guis/inscriber.png")).setSprite(36, 20, 105, 54);
        return centered(panel("ae2_inscriber", 105, 54).style(style -> style.backgroundTexture(texture)).addChildren(
                slot(top, "ae2_top", 2, 2), slot(middle, "ae2_middle", 26, 18),
                slot(bottom, "ae2_bottom", 2, 34), slot(output, "ae2_output", 76, 19)));
    }

    @Override
    public void load() {
        var data = getData();
        loadAe2Ingredient(0, data.getTop()); loadAe2Ingredient(1, data.getMiddle()); loadAe2Ingredient(2, data.getBottom());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        getData().setTop(saveAe2Ingredient(0)).setMiddle(saveAe2Ingredient(1)).setBottom(saveAe2Ingredient(2))
                .setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.type.ae2.inscriber"),
                switchField("viscript_recipe.editor.ae2.consume_presses", getData().isConsumePresses(), getData()::setConsumePresses,
                        Component.translatable("viscript_recipe.editor.ae2.consume_presses_hint")));
    }
}
