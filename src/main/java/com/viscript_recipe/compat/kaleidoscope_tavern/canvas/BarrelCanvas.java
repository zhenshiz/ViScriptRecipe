package com.viscript_recipe.compat.kaleidoscope_tavern.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeBarrelRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;

public class BarrelCanvas extends RecipeCanvas<KaleidoscopeBarrelRecipeData> {
    /**0~3 为原料槽位，4 为容器槽位*/
    public static final int CARRIER_INDEX = 4;

    public BarrelCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        loadIngredientSlot(CARRIER_INDEX, data.getCarrier());
        setVisualOutput(0, data.getResult());
        setExtraItem(KaleidoscopeTavernCanvasFactory.fluidBucket(data.getFluid()));
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(new ArrayList<>(getIngredients(CARRIER_INDEX)));
        data.setCarrier(getVisualIngredient(CARRIER_INDEX));
        data.setResult(getVisualOutput(0).getItem());
        data.setFluid(KaleidoscopeTavernCanvasFactory.fluidId(getExtraItem()));
    }

    @Override
    public UIElement createCanvas() {
        var slots = new IngredientDisplaySlot[4];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(slots);
        var carrier = createIngredientSlot(CARRIER_INDEX, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(carrier, result);
        var fluid = createExtraItemSlot(JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(fluid);
        return KaleidoscopeTavernCanvasFactory.createBarrelCanvas(fluid, slots, carrier, result);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_tavern"),
                intField("viscript_recipe.config.kaleidoscope_tavern.unit_time",
                        data.getUnitTime(), 1, Integer.MAX_VALUE, data::setUnitTime)
        );
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.config.kaleidoscope_tavern.fluid"),
                RecipeSearchComponents.fluid("viscript_recipe.config.kaleidoscope_tavern.fluid",
                        () -> KaleidoscopeTavernCanvasFactory.fluidId(getExtraItem()),
                        id -> setExtraItem(KaleidoscopeTavernCanvasFactory.fluidBucket(id)),
                        Runnables.doNothing(), Fluids.WATER
                )
        );
    }
}
