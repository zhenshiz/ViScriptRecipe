package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeStockpotRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;

public class StockpotCanvas extends RecipeCanvas<KaleidoscopeStockpotRecipeData> {
    public StockpotCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        loadIngredientSlot(9, data.getCarrier());
        setExtraItem(KaleidoscopeSoupBaseUiSupport.displayStack(data.getSoupBase()));
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(9));
        data.setCarrier(getVisualIngredient(9));
        var soupBase = KaleidoscopeSoupBaseUiSupport.idForStack(getExtraItem());
        data.setSoupBase(soupBase == null ? KaleidoscopeSoupBaseUiSupport.DEFAULT_SOUP_BASE : soupBase);
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new IngredientDisplaySlot[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(slots);
        var soupBase = createExtraItemSlot(JEI_SLOT_SIZE,
                Component.translatable("viscript_recipe.config.kaleidoscope_cookery.soup_base"));
        var carrier = createIngredientSlot(9, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(soupBase, carrier, result);
        return KaleidoscopeCanvasFactory.createStockpotCanvas(slots, soupBase, carrier, result);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"),
                intField("viscript_recipe.config.kaleidoscope_cookery.time",
                        data.getTime(), 1, Integer.MAX_VALUE, data::setTime),
                resourceField("viscript_recipe.config.kaleidoscope_cookery.cooking_texture",
                        data.getCookingTexture(), data::setCookingTexture),
                resourceField("viscript_recipe.config.kaleidoscope_cookery.finished_texture",
                        data.getFinishedTexture(), data::setFinishedTexture),
                PropertiesView.createRgbColorConfigurator("viscript_recipe.config.kaleidoscope_cookery.cooking_bubble_color",
                        data::getCookingBubbleColor, data::setCookingBubbleColor),
                PropertiesView.createRgbColorConfigurator("viscript_recipe.config.kaleidoscope_cookery.finished_bubble_color",
                        data::getFinishedBubbleColor, data::setFinishedBubbleColor)
        );
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        var data = getData();
        content.addChild(RecipeSearchComponents.soupBase("viscript_recipe.config.kaleidoscope_cookery.soup_base",
                data::getSoupBase, id -> setExtraItem(KaleidoscopeSoupBaseUiSupport.displayStack(id)),
                Runnables.doNothing())
        );
    }
}
