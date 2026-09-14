package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialEntityIngredientKind;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialLaserDrillOreRecipeData;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialLaserDrillRarityData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import static com.viscript_recipe.compat.industrial_foregoing.canvas.LaserDrillFluidCanvas.*;

public class LaserDrillOreCanvas extends RecipeCanvas<IndustrialLaserDrillOreRecipeData> {
    public LaserDrillOreCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return slotIndex == 1;}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getCatalyst());
        loadIngredientSlot(1, data.getOutput());
        refreshLaserLabels();
    }

    @Override
    public void save() {
        getData().setCatalyst(getVisualIngredient(0)).setOutput(getVisualIngredient(1));
    }

    @Override
    public UIElement createCanvas() {
        var catalyst = createIngredientSlot(0, JEI_SLOT_SIZE);
        var output = createIngredientSlot(1, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(catalyst, output);
        return IndustrialForegoingCanvasFactory.createLaser(
                IndustrialForegoingCanvasFactory.slotCell(catalyst, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(output, 18, 18), rangeLabel, requirementsLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.laser_drill_ore"));
        buildRarities(content, data::getRarity, data::setRarity, this::refreshPropertiesAndLabels);
        buildEntityCondition(content, data.getEntityCondition(), this::refreshPropertiesAndLabels);
    }

    private void refreshPropertiesAndLabels() {
        refreshLaserLabels();
        reloadProperties();
    }

    private void refreshLaserLabels() {
        var data = getData();
        var rarities = data.getRarity();
        var rarity = rarities.isEmpty() ? new IndustrialLaserDrillRarityData() : rarities.getFirst();
        rangeLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.laser.summary",
                rarity.getDepthMin(), rarity.getDepthMax(), rarity.getWeight(), rarities.size()));
        var entity = data.getEntityCondition();
        var entityText = entity.isEnabled()
                ? Component.literal((entity.getKind() == IndustrialEntityIngredientKind.TAG ? "#" : "") + entity.getId())
                : Component.translatable("viscript_recipe.editor.industrial_foregoing.entity_condition.none");
        requirementsLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.laser.requirements",
                entityText, rarity.getBiomeWhitelist().size(), rarity.getBiomeBlacklist().size(),
                rarity.getDimensionWhitelist().size(), rarity.getDimensionBlacklist().size()));
    }
}
