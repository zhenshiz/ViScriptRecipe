package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialDissolutionRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class DissolutionCanvas extends FluidRecipeCanvas<IndustrialDissolutionRecipeData> {
    static final Label processingLabel = emptyLabel();

    public DissolutionCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getInput());
        setVisualFluidInput(0, data.getInputFluid());
        setVisualOutput(0, data.isHasItemOutput() ? data.getOutput() : ItemStack.EMPTY);
        setVisualFluidOutput(0, data.isHasFluidOutput() ? data.getOutputFluid() : FluidStack.EMPTY);
        updateProcessingLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getIngredients(IndustrialDissolutionRecipeData.MAX_INPUTS));
        var itemOutput = getVisualOutput(0);
        var fluidOutput = getVisualFluidOutput(0);
        data.setInputFluid(getVisualFluidInput(0))
                .setHasItemOutput(!itemOutput.isEmpty()).setOutput(itemOutput.getItem())
                .setHasFluidOutput(!fluidOutput.isEmpty()).setOutputFluid(fluidOutput);
    }

    @Override
    public UIElement createCanvas() {
        var cells = new UIElement[IndustrialDissolutionRecipeData.MAX_INPUTS];
        for (int index = 0; index < cells.length; index++) {
            var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
            configureJeiOverlaySlotVisual(slot);
            cells[index] = IndustrialForegoingCanvasFactory.slotCell(slot, JEI_SLOT_SIZE, JEI_SLOT_SIZE);
        }
        var fluidInput = createFluidInputSlot(0).layout(layout -> {
            layout.width(16);
            layout.height(16);
        });
        var itemOutput = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(itemOutput);
        var fluidOutput = createFluidOutputSlot(0, 16).layout(layout -> {
            layout.width(16);
            layout.height(52);
        });
        return IndustrialForegoingCanvasFactory.createDissolution(
                cells,
                IndustrialForegoingCanvasFactory.slotCell(fluidInput, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(itemOutput, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(fluidOutput, 18, 54),
                processingLabel);
    }

    private void updateProcessingLabel() {
        processingLabel.setText(Component.translatable(
                "viscript_recipe.editor.industrial_foregoing.processing_ticks", getData().getProcessingTime()));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.dissolution"),
                intField("viscript_recipe.config.industrial_foregoing.processing_time",
                        data.getProcessingTime(), 0, Integer.MAX_VALUE, data::setProcessingTime, this::updateProcessingLabel));
    }

    @Override
    public String selectedFluidConfigNameKey() {
        return selectedSlotIndex() < 2 ? "viscript_recipe.config.industrial_foregoing.dissolution.input_fluid" :
                "viscript_recipe.config.industrial_foregoing.dissolution.output_fluid";
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        if (selectedSlotIndex() == 2) {
            var data = getData();
            content.addChild(switchField("viscript_recipe.config.industrial_foregoing.dissolution.has_fluid_output",
                    data.isHasFluidOutput(), value -> {
                        data.setHasFluidOutput(value);
                        if (!value) setSelectedFluidOutput(FluidStack.EMPTY);
                        else setSelectedFluidOutput(new FluidStack(Fluids.WATER, 1000));
                        reloadProperties();
                    }));
            if (!data.isHasFluidOutput()) return;
        }
        super.buildFluidProperties(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        content.addChild(switchField("viscript_recipe.config.industrial_foregoing.dissolution.has_item_output",
                data.isHasItemOutput(), value -> {
                    data.setHasItemOutput(value);
                    if (!value) setSelectedOutput(ItemStack.EMPTY);
                    else setSelectedOutput(new ItemStack(Items.DIAMOND));
                    reloadProperties();
                }));
        if (!data.isHasItemOutput()) return;
        super.buildResultProperties(content);
    }
}
