package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureSouliumSpawnerRecipeData;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureWeightedEntityData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import static com.viscript_recipe.compat.mysticalagriculture.canvas.AwakeningCanvas.useJeiCanvas;

public class SouliumSpawnerCanvas extends RecipeCanvas<MysticalAgricultureSouliumSpawnerRecipeData> {
    static final IngredientDisplaySlot preview =
            EnchanterCanvas.readOnlySlot("viscript_recipe.editor.mysticalagriculture.soulium_spawner.result");

    public SouliumSpawnerCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    @Override
    public void load() {
        loadIngredientSlot(0, getData().getInput());
        setResultPreview();
    }

    @Override
    public void save() {getData().setInput(getVisualIngredient(0));}

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, 18);
        tooltip(input, "viscript_recipe.editor.mysticalagriculture.soulium_spawner.input");
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(input);
            configureJeiOverlaySlotVisual(preview);
        }
        return MysticalAgricultureCanvasFactory.createProcessCanvas(input, preview, true, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(
                sectionTitle("viscript_recipe.editor.properties.mysticalagriculture.soulium_spawner"),
                sectionTitle("viscript_recipe.config.mysticalagriculture.soulium_spawner.entities")
        );
        var data = getData();
        var entities = data.getEntities();
        var totalWeight = entities.stream().mapToInt(MysticalAgricultureWeightedEntityData::getWeight).sum();
        for (int index = 0; index < entities.size(); index++) {
            var entityIndex = index;
            var weightedEntity = entities.get(entityIndex);
            var chance = totalWeight <= 0 ? 0D : weightedEntity.getWeight() * 100D / totalWeight;
            content.addChildren(
                    RecipeEditorUi.label(Component.translatable(
                            "viscript_recipe.editor.mysticalagriculture.soulium_spawner.entity", entityIndex + 1)),
                    RecipeSearchComponents.entityType(
                            "viscript_recipe.config.mysticalagriculture.soulium_spawner.entity",
                            weightedEntity::getEntity, weightedEntity::setEntity,
                            this::setResultPreview, EntityType.ZOMBIE),
                    intField("viscript_recipe.config.mysticalagriculture.soulium_spawner.weight",
                            weightedEntity.getWeight(), 1, Integer.MAX_VALUE, weightedEntity::setWeight, RecipeCanvas::reloadProperties),
                    RecipeEditorUi.label(Component.translatable(
                            "viscript_recipe.editor.mysticalagriculture.soulium_spawner.chance", chance))
            );
            if (entities.size() > 1) {
                content.addChild(RecipeEditorUi.textButton(
                        Component.translatable("viscript_recipe.editor.mysticalagriculture.soulium_spawner.remove_entity"),
                        Icons.DELETE, event -> {
                            entities.remove(entityIndex); setResultPreview(); reloadProperties();
                        }).layout(layout -> layout.widthPercent(100).height(18)));
            }
        }
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.mysticalagriculture.soulium_spawner.add_entity"),
                Icons.ADD, event -> {
                    entities.add(new MysticalAgricultureWeightedEntityData());
                    setResultPreview(); reloadProperties();
                }).layout(layout -> layout.widthPercent(100).height(18)));
    }

    private void setResultPreview() {
        preview.setTagDisplayStacks(MysticalAgricultureRecipeUiSupport.spawnEggs(getData().getEntities()));
    }
}
