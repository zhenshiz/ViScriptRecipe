package com.viscript_recipe.compat.kaleidoscope_tavern.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.kaleidoscope_tavern.KaleidoscopeTavernRecipeFactory;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopePressingTubRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluids;

public class PressingTubCanvas extends RecipeCanvas<KaleidoscopePressingTubRecipeData> {
    static final Label pressCountLabel = emptyLabel();
    static {configureLabel(pressCountLabel);}

    public PressingTubCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setExtraItem(KaleidoscopeTavernCanvasFactory.fluidBucket(data.getFluid()));
        updatePressCountLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        data.setFluid(KaleidoscopeTavernCanvasFactory.fluidId(getExtraItem()));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(input);
        //压榨桶的产物是流体的桶，由流体种类决定，不能单独编辑
        var fluid = createExtraItemSlot(JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(fluid);
        return KaleidoscopeTavernCanvasFactory.createPressingTubCanvas(input, fluid, pressCountLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_tavern"),
                intField("viscript_recipe.config.kaleidoscope_tavern.fluid_amount",
                        data.getFluidAmount(), 1, Integer.MAX_VALUE, data::setFluidAmount, this::updatePressCountLabel)
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

    private void updatePressCountLabel() {
        pressCountLabel.setText(Component.translatable("jei.kaleidoscope_tavern.pressing_tub.need_press_count",
                KaleidoscopeTavernRecipeFactory.needPressCount(getData().getFluidAmount())));
    }

    static void configureLabel(Label label) {
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.GRAY.color).textWrap(TextWrap.HOVER_ROLL));
    }
}
