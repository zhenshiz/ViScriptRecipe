package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialBlockStatePropertyData;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialFluidExtractorRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public class FluidExtractorCanvas extends FluidRecipeCanvas<IndustrialFluidExtractorRecipeData> {
    static final Label productionLabel = emptyLabel();

    public FluidExtractorCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        setVisualFluidOutput(0, data.getOutput());
        setVisualOutput(0, data.getResult());
        updateProductionLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        data.setOutput(getVisualFluidOutput(0));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, JEI_SLOT_SIZE);
        var blockOutput = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(input, blockOutput);
        var fluidOutput = createFluidOutputSlot(0, 16).layout(layout -> {
            layout.width(16);
            layout.height(48);
        });
        return IndustrialForegoingCanvasFactory.createFluidExtractor(
                IndustrialForegoingCanvasFactory.slotCell(input, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(blockOutput, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(fluidOutput, 18, 50), productionLabel);
    }

    private void updateProductionLabel() {
        var data = getData();
        productionLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.production_per_work",
                data.getOutput().getAmount(), data.getBreakChance()));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.fluid_extractor"),
                floatField("viscript_recipe.config.industrial_foregoing.fluid_extractor.break_chance",
                        data.getBreakChance(), 0, 1, data::setBreakChance, this::updateProductionLabel),
                switchField("viscript_recipe.config.industrial_foregoing.fluid_extractor.default_recipe",
                        data.isDefaultRecipe(), data::setDefaultRecipe));
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.fluid_extractor"),
                PropertiesView.createBlockConfigurator("viscript_recipe.config.industrial_foregoing.fluid_extractor.result_block", () -> fromItemStack(getSelectedOutput().getItem()), block -> {
                    data.setResultBlock(BuiltInRegistries.BLOCK.getKey(block));
                    data.setResultProperties(new java.util.ArrayList<>());
                    setSelectedOutput(new ItemStack(block));
                    reloadProperties();
                }));
        buildBlockStateProperties(content, data.getResultBlock(), data.getResultProperties());
    }

    static Block fromItemStack(ItemStack stack) {
        return stack.getItem() instanceof BlockItem block ? block.getBlock() : Blocks.AIR;
    }

    private static void buildBlockStateProperties(UIElement content, ResourceLocation blockId,
                                                  List<IndustrialBlockStatePropertyData> stored) {
        var block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == Blocks.AIR || block.getStateDefinition().getProperties().isEmpty()) {
            return;
        }
        content.addChild(sectionTitle("viscript_recipe.config.industrial_foregoing.block_state.properties"));
        for (var property : block.getStateDefinition().getProperties()) {
            var current = stored.stream().filter(row -> property.getName().equals(row.getName()))
                    .findFirst().map(IndustrialBlockStatePropertyData::getValue)
                    .filter(value -> property.getValue(value).isPresent())
                    .orElseGet(() -> defaultPropertyValueName(block.defaultBlockState(), property));
            var candidates = property.getPossibleValues().stream()
                    .map(value -> propertyValueName(property, value)).toList();
            content.addChild(selector("viscript_recipe.config.industrial_foregoing.block_state.property",
                    candidates, current, Component::literal,
                            value -> setBlockProperty(stored, property.getName(), value),
                    Component.literal(property.getName())));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValueName(Property property, Comparable value) {
        return property.getName(value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String defaultPropertyValueName(BlockState state, Property property) {
        return property.getName(state.getValue(property));
    }

    private static void setBlockProperty(List<IndustrialBlockStatePropertyData> stored, String name, String value) {
        for (var row : stored) {
            if (name.equals(row.getName())) { row.setValue(value); return; }
        }
        stored.add(new IndustrialBlockStatePropertyData().setName(name).setValue(value));
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.config.industrial_foregoing.fluid_extractor.output"),
                PropertiesView.createFluidStackConfigurator("viscript_recipe.config.industrial_foregoing.fluid_extractor.output", this::getSelectedFluidOutput, output -> {
                    setSelectedFluidOutput(output);
                    updateProductionLabel();
                })
        );
    }
}
