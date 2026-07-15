package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureAwakeningRecipeData;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureEnchanterRecipeData;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureInfusionRecipeData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Owns the six editable Mystical Agriculture Recipe Codec canvases in the recipe workbench.
 */
final class MysticalAgricultureWorkbenchSection {
    private final RecipeEditorController controller;
    private final boolean useJeiSkin;

    private final IngredientDisplaySlot infusionInput = editableIngredientSlot(0,
            "viscript_recipe.editor.mysticalagriculture.infusion.input");
    private final IngredientDisplaySlot[] infusionPedestals = new IngredientDisplaySlot[
            MysticalAgricultureInfusionRecipeData.MAX_PEDESTAL_INGREDIENTS];
    private final ItemSlot infusionResult = editableResultSlot();
    private final UIElement infusionCanvas;

    private final IngredientDisplaySlot awakeningInput = editableIngredientSlot(0,
            "viscript_recipe.editor.mysticalagriculture.awakening.input");
    private final IngredientDisplaySlot[] awakeningIngredients = new IngredientDisplaySlot[
            MysticalAgricultureAwakeningRecipeData.PEDESTAL_INGREDIENT_COUNT];
    private final ItemSlot[] awakeningEssences = new ItemSlot[MysticalAgricultureAwakeningRecipeData.ESSENCE_COUNT];
    private final ItemSlot awakeningResult = editableResultSlot();
    private final UIElement awakeningCanvas;

    private final IngredientDisplaySlot[] enchanterIngredients = new IngredientDisplaySlot[
            MysticalAgricultureEnchanterRecipeData.MAX_INGREDIENTS];
    private final UIElement enchanterBook = readOnlyIcon(new ItemStack(Items.BOOK),
            "viscript_recipe.editor.mysticalagriculture.enchanter.book");
    private final IngredientDisplaySlot enchanterResult = readOnlyCycleSlot(
            "viscript_recipe.editor.mysticalagriculture.enchanter.result");
    private final UIElement enchanterCanvas;

    private final IngredientDisplaySlot reprocessorInput = editableIngredientSlot(0,
            "viscript_recipe.editor.mysticalagriculture.reprocessor.input");
    private final ItemSlot reprocessorResult = editableResultSlot();
    private final UIElement reprocessorCanvas;

    private final IngredientDisplaySlot soulExtractionInput = editableIngredientSlot(0,
            "viscript_recipe.editor.mysticalagriculture.soul_extraction.input");
    private final ItemSlot soulExtractionResult = readOnlySlot(
            "viscript_recipe.editor.mysticalagriculture.soul_extraction.result");
    private final UIElement soulExtractionCanvas;

    private final IngredientDisplaySlot souliumSpawnerInput = editableIngredientSlot(0,
            "viscript_recipe.editor.mysticalagriculture.soulium_spawner.input");
    private final IngredientDisplaySlot souliumSpawnerResult = readOnlyCycleSlot(
            "viscript_recipe.editor.mysticalagriculture.soulium_spawner.result");
    private final UIElement souliumSpawnerCanvas;

    MysticalAgricultureWorkbenchSection(RecipeEditorController controller) {
        this.controller = controller;
        this.useJeiSkin = MysticalAgricultureCanvasFactory.hasJeiSkin();
        for (int index = 0; index < infusionPedestals.length; index++) {
            infusionPedestals[index] = editableIngredientSlot(index + 1,
                    "viscript_recipe.editor.mysticalagriculture.infusion.pedestal");
        }
        infusionCanvas = MysticalAgricultureCanvasFactory.createInfusionCanvas(
                infusionInput, infusionPedestals, infusionResult, useJeiSkin);

        for (int index = 0; index < awakeningIngredients.length; index++) {
            awakeningIngredients[index] = editableIngredientSlot(index + 1,
                    "viscript_recipe.editor.mysticalagriculture.awakening.ingredient");
            awakeningEssences[index] = editableEssenceSlot(index);
        }
        awakeningCanvas = MysticalAgricultureCanvasFactory.createAwakeningCanvas(
                awakeningInput, awakeningIngredients, awakeningEssences, awakeningResult, useJeiSkin);

        for (int index = 0; index < enchanterIngredients.length; index++) {
            enchanterIngredients[index] = editableIngredientSlot(index,
                    "viscript_recipe.editor.mysticalagriculture.enchanter.ingredient");
        }
        enchanterCanvas = MysticalAgricultureCanvasFactory.createEnchanterCanvas(
                enchanterIngredients, enchanterBook, enchanterResult, useJeiSkin);
        reprocessorCanvas = MysticalAgricultureCanvasFactory.createProcessCanvas(
                reprocessorInput, reprocessorResult, false, useJeiSkin);
        soulExtractionCanvas = MysticalAgricultureCanvasFactory.createProcessCanvas(
                soulExtractionInput, soulExtractionResult, false, useJeiSkin);
        souliumSpawnerCanvas = MysticalAgricultureCanvasFactory.createProcessCanvas(
                souliumSpawnerInput, souliumSpawnerResult, true, useJeiSkin);
    }

    List<UIElement> canvases() {
        return List.of(
                infusionCanvas,
                awakeningCanvas,
                enchanterCanvas,
                reprocessorCanvas,
                soulExtractionCanvas,
                souliumSpawnerCanvas
        );
    }

    boolean isSelectedLayout() {
        return controller.isSelectedMysticalAgricultureInfusionLayout()
                || controller.isSelectedMysticalAgricultureAwakeningLayout()
                || controller.isSelectedMysticalAgricultureEnchanterLayout()
                || controller.isSelectedMysticalAgricultureReprocessorLayout()
                || controller.isSelectedMysticalAgricultureSoulExtractionLayout()
                || controller.isSelectedMysticalAgricultureSouliumSpawnerLayout();
    }

    void refresh() {
        var entry = controller.getSelectedEntry();
        infusionCanvas.setDisplay(controller.isSelectedMysticalAgricultureInfusionLayout());
        awakeningCanvas.setDisplay(controller.isSelectedMysticalAgricultureAwakeningLayout());
        enchanterCanvas.setDisplay(controller.isSelectedMysticalAgricultureEnchanterLayout());
        reprocessorCanvas.setDisplay(controller.isSelectedMysticalAgricultureReprocessorLayout());
        soulExtractionCanvas.setDisplay(controller.isSelectedMysticalAgricultureSoulExtractionLayout());
        souliumSpawnerCanvas.setDisplay(controller.isSelectedMysticalAgricultureSouliumSpawnerLayout());
        if (entry == null) {
            return;
        }
        if (controller.isMysticalAgricultureInfusionEntry(entry)) {
            refreshInfusion();
        } else if (controller.isMysticalAgricultureAwakeningEntry(entry)) {
            refreshAwakening();
        } else if (controller.isMysticalAgricultureEnchanterEntry(entry)) {
            refreshEnchanter();
        } else if (controller.isMysticalAgricultureReprocessorEntry(entry)) {
            refreshIngredient(reprocessorInput, 0, 1);
            setSlot(reprocessorResult, entry.getMysticalAgricultureReprocessor().getResult());
        } else if (controller.isMysticalAgricultureSoulExtractionEntry(entry)) {
            refreshIngredient(soulExtractionInput, 0, 1);
            setSlot(soulExtractionResult, MysticalAgricultureRecipeUiSupport.soulJar(
                    entry.getMysticalAgricultureSoulExtraction()));
        } else if (controller.isMysticalAgricultureSouliumSpawnerEntry(entry)) {
            refreshIngredient(souliumSpawnerInput, 0,
                    Math.max(1, entry.getMysticalAgricultureSouliumSpawner().getInput().getCount()));
            souliumSpawnerResult.setTagDisplayStacks(MysticalAgricultureRecipeUiSupport.spawnEggs(
                    entry.getMysticalAgricultureSouliumSpawner().getEntities()));
        }
    }

    private void refreshInfusion() {
        refreshIngredient(infusionInput, 0, 1);
        for (int index = 0; index < infusionPedestals.length; index++) {
            refreshIngredient(infusionPedestals[index], index + 1, 1);
        }
        var entry = controller.getSelectedEntry();
        if (entry != null) {
            setSlot(infusionResult, entry.getMysticalAgricultureInfusion().getResult());
        }
    }

    private void refreshAwakening() {
        refreshIngredient(awakeningInput, 0, 1);
        var entry = controller.getSelectedEntry();
        if (entry == null) {
            return;
        }
        var data = entry.getMysticalAgricultureAwakening();
        for (int index = 0; index < awakeningIngredients.length; index++) {
            refreshIngredient(awakeningIngredients[index], index + 1, 1);
            setSlot(awakeningEssences[index], data.essence(index));
        }
        setSlot(awakeningResult, data.getResult());
    }

    private void refreshEnchanter() {
        var entry = controller.getSelectedEntry();
        if (entry == null) {
            return;
        }
        var data = entry.getMysticalAgricultureEnchanter();
        for (int index = 0; index < enchanterIngredients.length; index++) {
            refreshIngredient(enchanterIngredients[index], index,
                    Math.max(1, data.ingredient(index).getCount()));
        }
        enchanterResult.setTagDisplayStacks(MysticalAgricultureRecipeUiSupport.enchantedBooks(data.getEnchantment()));
    }

    private void refreshIngredient(IngredientDisplaySlot slot, int index, int count) {
        var alternatives = controller.getVisualIngredientTagStacks(index);
        if (alternatives.length > 0) {
            var counted = new ItemStack[alternatives.length];
            for (int alternativeIndex = 0; alternativeIndex < alternatives.length; alternativeIndex++) {
                counted[alternativeIndex] = withCount(alternatives[alternativeIndex], count);
            }
            slot.setTagDisplayStacks(counted);
            return;
        }
        slot.clearTagDisplayStacks();
        setSlot(slot, withCount(controller.getVisualIngredient(index), count));
    }

    private IngredientDisplaySlot editableIngredientSlot(int index, String tooltipKey) {
        var slot = createIngredientSlot();
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualIngredient(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(index);
            if (event.button == 1) {
                controller.clearVisualIngredient(index);
            }
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(tooltipKey, index + 1)));
        configureJeiSlot(slot);
        return slot;
    }

    private ItemSlot editableResultSlot() {
        var slot = createItemSlot();
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualResult(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectResultSlot();
            if (event.button == 1) {
                controller.clearVisualResult();
            }
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.result_slot")));
        configureJeiSlot(slot);
        return slot;
    }

    private ItemSlot editableEssenceSlot(int index) {
        var slot = createItemSlot();
        slot.registerValueListener(stack -> {
            var entry = controller.getSelectedEntry();
            if (!controller.isRefreshing() && entry != null && controller.isMysticalAgricultureAwakeningEntry(entry)) {
                entry.getMysticalAgricultureAwakening().setEssence(index, stack);
                controller.notifyChanged();
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectMysticalAgricultureEssenceSlot(index);
            if (event.button == 1) {
                controller.setSelectedMysticalAgricultureEssence(ItemStack.EMPTY);
            }
            event.stopPropagation();
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.mysticalagriculture.awakening.essence", index + 1)));
        configureJeiSlot(slot);
        return slot;
    }

    private ItemSlot readOnlySlot(String tooltipKey) {
        var slot = (ItemSlot) new ItemSlot()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                    layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                });
        slot.style(style -> style.tooltips(Component.translatable(tooltipKey)));
        configureJeiSlot(slot);
        return slot;
    }

    private IngredientDisplaySlot readOnlyCycleSlot(String tooltipKey) {
        var slot = (IngredientDisplaySlot) new IngredientDisplaySlot()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                    layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                });
        slot.style(style -> style.tooltips(Component.translatable(tooltipKey)));
        configureJeiSlot(slot);
        return slot;
    }

    private UIElement readOnlyIcon(ItemStack stack, String tooltipKey) {
        return new UIElement().layout(layout -> {
            layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
            layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
        }).style(style -> style
                .backgroundTexture(new ItemStackTexture(stack))
                .tooltips(Component.translatable(tooltipKey)));
    }

    private ItemSlot createItemSlot() {
        return (ItemSlot) new ItemSlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                    layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                });
    }

    private IngredientDisplaySlot createIngredientSlot() {
        return (IngredientDisplaySlot) new IngredientDisplaySlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                    layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                });
    }

    private void configureJeiSlot(ItemSlot slot) {
        if (useJeiSkin) {
            slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
            slot.slotStyle(style -> style.slotOverlay(IGuiTexture.EMPTY));
        }
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(Math.max(1, count));
    }

    private static void setSlot(ItemSlot slot, ItemStack stack) {
        slot.setItem(stack == null ? ItemStack.EMPTY : stack.copy(), false);
    }
}
