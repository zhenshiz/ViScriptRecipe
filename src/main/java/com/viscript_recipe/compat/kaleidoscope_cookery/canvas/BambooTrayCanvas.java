package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeBambooTrayRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BambooTrayCanvas extends RecipeCanvas<KaleidoscopeBambooTrayRecipeData> {
    public BambooTrayCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return KaleidoscopeCanvasFactory.createBambooTrayCanvas(
                createIngredientSlot(0, JEI_SLOT_SIZE), createOutputSlot(0, JEI_SLOT_SIZE));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"),
                selector("viscript_recipe.config.kaleidoscope_cookery.subtype",
                        List.of(KaleidoscopeBambooTrayRecipeData.SUBTYPE_WETTING, KaleidoscopeBambooTrayRecipeData.SUBTYPE_DRYING),
                        data.getSubtype(),
                        subtype -> Component.translatable("viscript_recipe.config.kaleidoscope_cookery.subtype." + subtype),
                        data::setSubtype),
                intField("viscript_recipe.config.kaleidoscope_cookery.time",
                        data.getDuration(), 1, Integer.MAX_VALUE, data::setDuration)
        );
    }
}
