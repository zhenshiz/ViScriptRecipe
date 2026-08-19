package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeTeapotRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

public class TeapotCanvas extends RecipeCanvas<KaleidoscopeTeapotRecipeData> {
    static final Label timeLabel = emptyLabel();
    static {PotCanvas.configureLabel(timeLabel);}

    public TeapotCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
        setExtraItem(fluidBucket(data.getTeaFluid()));
        updateTimeLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        data.setResult(getVisualOutput(0).getItem());
        data.setTeaFluid(fluidId(getExtraItem()));
    }

    @Override
    public UIElement createCanvas() {
        var fluidBucket = createExtraItemSlot(JEI_SLOT_SIZE,
                Component.translatable("viscript_recipe.config.kaleidoscope_cookery.tea_fluid"));
        var input = createIngredientSlot(0, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(fluidBucket, input, result);
        return KaleidoscopeCanvasFactory.createTeapotCanvas(fluidBucket, input, result, timeLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"),
                intField("viscript_recipe.config.kaleidoscope_cookery.time", data.getTime(),
                        1, Integer.MAX_VALUE, data::setTime, this::updateTimeLabel)
        );
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.config.kaleidoscope_cookery.tea_fluid"),
                RecipeSearchComponents.fluid("viscript_recipe.config.kaleidoscope_cookery.tea_fluid",
                        () -> fluidId(getExtraItem()), id -> setExtraItem(fluidBucket(id)),
                        Runnables.doNothing(), Fluids.WATER
                )
        );
    }

    private void updateTimeLabel() {
        timeLabel.setText(Component.translatable("jei.kaleidoscope_cookery.teapot.time", getData().getTime() / 20));
    }

    static ItemStack fluidBucket(ResourceLocation id) {
        var fluid = BuiltInRegistries.FLUID.get(id);
        return new ItemStack(fluid.getBucket());
    }

    static ResourceLocation fluidId(ItemStack stack) {
        if (stack.isEmpty()) return ResourceLocation.withDefaultNamespace("water");
        var item = stack.getItem();
        for (var fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.getBucket() == item) return BuiltInRegistries.FLUID.getKey(fluid);
        }
        return ResourceLocation.withDefaultNamespace("water");
    }
}
