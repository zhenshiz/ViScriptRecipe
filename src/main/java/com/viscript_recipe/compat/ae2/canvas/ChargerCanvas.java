package com.viscript_recipe.compat.ae2.canvas;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.definitions.AEBlocks;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ae2.data.Ae2ChargerRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import static com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory.*;

/** Mirrors AE2's charger JEI layout, with the crank energy requirement shown as a hint. */
public class ChargerCanvas extends Ae2ItemRecipeCanvas<Ae2ChargerRecipeData> {
    public ChargerCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
                / CrankBlockEntity.POWER_PER_CRANK_TURN;
        return centered(panel("ae2_charger", 130, 50).addChildren(
                standardSlot(createIngredientSlot(0, 18), "ae2_input", 30, 7), arrow(52, 8),
                standardSlot(createOutputSlot(0, 18), "ae2_output", 80, 7),
                at(createItemIcon(AEBlocks.CRANK.stack(), 16), 3, 30, 16, 16),
                label(Component.translatable("viscript_recipe.editor.ae2.charger_energy", turns,
                        ChargerBlockEntity.POWER_MAXIMUM_AMOUNT), 20, 34, 110)));
    }

    @Override
    public void load() { loadAe2Ingredient(0, getData().getInput()); setVisualOutput(0, getData().getResult()); }
    @Override
    public void save() { getData().setInput(saveAe2Ingredient(0)).setResult(getVisualOutput(0).getItem()); }
    @Override
    public void buildRecipeProperties(UIElement content) { content.addChild(sectionTitle("viscript_recipe.editor.type.ae2.charger")); }
}
