package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.confluence.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Owns the editable JEI-shaped canvases shared by Confluence's fifteen RecipeManager types. */
final class ConfluenceWorkbenchSection {
    private final RecipeEditorController controller;
    private final IngredientDisplaySlot[] amountInputs = new IngredientDisplaySlot[5];
    private final ItemSlot amountOutput = resultSlot();
    /* These slots belong to a different canvas and must not be shared with amountInputs. */
    private final IngredientDisplaySlot[] forgeInputs = new IngredientDisplaySlot[5];
    private final ItemSlot forgeOutput = resultSlot();
    private final IngredientDisplaySlot[] eitherInputs = new IngredientDisplaySlot[16];
    private final ItemSlot eitherOutput = resultSlot();
    private final IngredientDisplaySlot[] alchemyInputs = new IngredientDisplaySlot[7];
    private final ItemSlot alchemyOutput = resultSlot();
    private final IngredientDisplaySlot[] fletchingInputs = new IngredientDisplaySlot[3];
    private final ItemSlot fletchingOutput = resultSlot();
    private final IngredientDisplaySlot[] cookingInputs = new IngredientDisplaySlot[4];
    private final IngredientDisplaySlot cookingContainer = ingredientSlot(-1);
    private final IngredientDisplaySlot cookingHeat = ingredientSlot(-2);
    private final ItemSlot cookingOutput = resultSlot();
    private final IngredientDisplaySlot transmutationInput = ingredientSlot(0);
    private final ItemSlot[] transmutationTargets = new ItemSlot[ConfluenceRecipeData.MAX_TRANSMUTATION_RESULTS];
    private final Label transmutationPhase = RecipeEditorUi.label(Component.empty());
    private final UIElement transmutationCanvas;
    private final UIElement amountCanvas;
    private final UIElement forgeCanvas;
    private final UIElement eitherCanvas;
    private final UIElement alchemyCanvas;
    private final UIElement fletchingCanvas;
    private final UIElement cookingCanvas;

    ConfluenceWorkbenchSection(RecipeEditorController controller) {
        this.controller = controller;
        for (int i = 0; i < amountInputs.length; i++) amountInputs[i] = ingredientSlot(i);
        for (int i = 0; i < forgeInputs.length; i++) forgeInputs[i] = ingredientSlot(i);
        for (int i = 0; i < eitherInputs.length; i++) eitherInputs[i] = ingredientSlot(i);
        for (int i = 0; i < alchemyInputs.length; i++) alchemyInputs[i] = ingredientSlot(i);
        for (int i = 0; i < fletchingInputs.length; i++) fletchingInputs[i] = ingredientSlot(i);
        for (int i = 0; i < cookingInputs.length; i++) cookingInputs[i] = ingredientSlot(i);
        for (int i = 0; i < transmutationTargets.length; i++) transmutationTargets[i] = targetSlot(i);
        transmutationCanvas = ConfluenceCanvasFactory.transmutation(transmutationInput, transmutationTargets, transmutationPhase);
        amountCanvas = ConfluenceCanvasFactory.amount(amountInputs, amountOutput);
        forgeCanvas = ConfluenceCanvasFactory.forge(forgeInputs, forgeOutput);
        eitherCanvas = ConfluenceCanvasFactory.either(eitherInputs, eitherOutput);
        alchemyCanvas = ConfluenceCanvasFactory.alchemy(alchemyInputs, alchemyOutput);
        fletchingCanvas = ConfluenceCanvasFactory.fletching(fletchingInputs, fletchingOutput);
        cookingCanvas = ConfluenceCanvasFactory.cooking(cookingInputs, cookingContainer, cookingHeat, cookingOutput);
    }

    List<UIElement> canvases() {
        return List.of(transmutationCanvas, amountCanvas, forgeCanvas, eitherCanvas, alchemyCanvas, fletchingCanvas, cookingCanvas);
    }

    boolean isSelectedLayout() {
        return controller.isSelectedConfluenceLayout();
    }

    void refresh() {
        var entry = controller.getSelectedEntry();
        var type = entry == null ? null : entry.getType();
        var trans = ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION.equals(type);
        var amount = ConfluenceRecipeEditorTypes.SKY_MILL.equals(type) || ConfluenceRecipeEditorTypes.ALTAR.equals(type)
                || ConfluenceRecipeEditorTypes.DYE_VAT.equals(type) || ConfluenceRecipeEditorTypes.CRYSTAL_BALL.equals(type);
        var forge = ConfluenceRecipeEditorTypes.HELLFORGE.equals(type) || ConfluenceRecipeEditorTypes.HARDMODE_FORGE.equals(type);
        var either = ConfluenceRecipeEditorTypes.HEAVY_WORK_BENCH.equals(type) || ConfluenceRecipeEditorTypes.SAWMILL.equals(type)
                || ConfluenceRecipeEditorTypes.SOLIDIFIER.equals(type) || ConfluenceRecipeEditorTypes.HARDMODE_ANVIL.equals(type)
                || ConfluenceRecipeEditorTypes.LOOM.equals(type);
        var alchemy = ConfluenceRecipeEditorTypes.ALCHEMY_TABLE.equals(type);
        var fletching = ConfluenceRecipeEditorTypes.FLETCHING_TABLE.equals(type);
        var cooking = ConfluenceRecipeEditorTypes.COOKING_POT.equals(type);
        transmutationCanvas.setDisplay(trans);
        amountCanvas.setDisplay(amount);
        forgeCanvas.setDisplay(forge);
        eitherCanvas.setDisplay(either);
        alchemyCanvas.setDisplay(alchemy);
        fletchingCanvas.setDisplay(fletching);
        cookingCanvas.setDisplay(cooking);
        if (entry == null || !ConfluenceRecipeEditorTypes.isType(type)) return;
        var data = entry.getConfluence();
        if (trans) {
            refreshIngredient(transmutationInput, data, 0);
            for (int i = 0; i < transmutationTargets.length; i++) setItem(transmutationTargets[i], data.target(i));
            var phase = data.getGamePhase() == null ? ConfluenceGamePhase.BEFORE_SKELETRON : data.getGamePhase();
            transmutationPhase.setText(Component.translatable(
                    "viscript_recipe.editor.confluence.phase",
                    Component.translatable("viscript_recipe.editor.confluence.phase." + phase.getSerializedName())));
        } else if (alchemy) {
            for (int i = 0; i < alchemyInputs.length; i++) refreshIngredient(alchemyInputs[i], data, i);
            setItem(alchemyOutput, data.getResult());
        } else if (fletching) {
            for (int i = 0; i < fletchingInputs.length; i++) refreshIngredient(fletchingInputs[i], data, i);
            setItem(fletchingOutput, data.getResult());
        } else if (cooking) {
            for (int i = 0; i < cookingInputs.length; i++) refreshIngredient(cookingInputs[i], data, i);
            setIngredient(cookingContainer, data.getContainer());
            var heat = data.getHeatSource() == null || data.getHeatSource().getBlocks() == null ? ItemStack.EMPTY : firstBlock(data.getHeatSource().getBlocks());
            cookingHeat.setItem(heat, false);
            setItem(cookingOutput, data.getResult());
        } else if (forge) {
            for (int i = 0; i < forgeInputs.length; i++) refreshIngredient(forgeInputs[i], data, i);
            setItem(forgeOutput, data.getResult());
        } else if (either) {
            for (int i = 0; i < eitherInputs.length; i++) refreshIngredient(eitherInputs[i], data, i);
            setItem(eitherOutput, data.getResult());
        } else if (amount) {
            for (int i = 0; i < amountInputs.length; i++) refreshIngredient(amountInputs[i], data, i);
            setItem(amountOutput, data.getResult());
        }
    }

    private IngredientDisplaySlot ingredientSlot(int index) {
        var slot = (IngredientDisplaySlot) new IngredientDisplaySlot().xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> layout.width(18).height(18));
        slot.registerValueListener(stack -> {
            var entry = controller.getSelectedEntry();
            if (!controller.isRefreshing() && entry != null && ConfluenceRecipeEditorTypes.isType(entry.getType()) && index == -1
                    && ConfluenceRecipeEditorTypes.COOKING_POT.equals(entry.getType())) {
                entry.getConfluence().setContainer(stack == null || stack.isEmpty() ? RecipeIngredient.empty() : RecipeIngredient.item(stack.copyWithCount(1)));
                controller.notifyChanged();
            } else if (!controller.isRefreshing() && entry != null && ConfluenceRecipeEditorTypes.isType(entry.getType()) && index >= 0) {
                var data = entry.getConfluence();
                data.ingredient(index).setIngredient(stack == null || stack.isEmpty() ? RecipeIngredient.empty() : RecipeIngredient.item(stack.copyWithCount(1))).setCount(stack == null || stack.isEmpty() ? 1 : Math.max(1, stack.getCount()));
                controller.notifyChanged();
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (index >= 0) {
                controller.selectIngredientSlot(index);
                if (event.button == 1) controller.clearVisualIngredient(index);
            } else if (index == -1) {
                controller.selectContainerSlot();
            }
            event.stopPropagation();
        });
        return slot;
    }

    private ItemSlot resultSlot() {
        var slot = (ItemSlot) new ItemSlot().xeiPhantom().slotStyle(style -> style.showItemTooltips(true)).layout(layout -> layout.width(18).height(18));
        slot.registerValueListener(stack -> {
            var entry = controller.getSelectedEntry();
            if (!controller.isRefreshing() && entry != null && ConfluenceRecipeEditorTypes.isType(entry.getType())) {
                entry.getConfluence().setResult(stack == null ? ItemStack.EMPTY : stack.copy());
                controller.notifyChanged();
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectResultSlot();
            if (event.button == 1) controller.clearVisualResult();
            event.stopPropagation();
        });
        return slot;
    }

    private ItemSlot targetSlot(int index) {
        var slot = (ItemSlot) new ItemSlot().xeiPhantom().slotStyle(style -> style.showItemTooltips(true)).layout(layout -> layout.width(18).height(18));
        slot.registerValueListener(stack -> {
            var entry = controller.getSelectedEntry();
            if (!controller.isRefreshing() && entry != null && ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION.equals(entry.getType())) {
                entry.getConfluence().setTarget(index, stack);
                controller.notifyChanged();
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectConfluenceTargetSlot(index);
            if (event.button == 1) controller.setSelectedConfluenceTarget(ItemStack.EMPTY);
            event.stopPropagation();
        });
        return slot;
    }

    private void refreshIngredient(IngredientDisplaySlot slot, ConfluenceRecipeData data, int index) {
        var value = data.ingredient(index);
        var ingredient = value.getIngredient();
        if (ingredient == null || ingredient.isEmpty()) {
            slot.clearTagDisplayStacks();
            slot.setItem(ItemStack.EMPTY, false);
            return;
        }
        var stacks = ingredient.compile().getItems();
        if (stacks.length == 0) slot.setItem(ItemStack.EMPTY, false);
        else {
            var counted = new ItemStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) counted[i] = stacks[i].copyWithCount(Math.max(1, value.getCount()));
            slot.setTagDisplayStacks(counted);
        }
    }

    private void setIngredient(IngredientDisplaySlot slot, RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) slot.setItem(ItemStack.EMPTY, false);
        else {
            var stacks = ingredient.compile().getItems();
            slot.setItem(stacks.length == 0 ? ItemStack.EMPTY : stacks[0].copy(), false);
        }
    }

    private static ItemStack firstBlock(ConfluenceHolderSetData data) {
        if (data.getKind() != ConfluenceHolderSetKind.IDS || data.getValues() == null || data.getValues().isEmpty()) return ItemStack.EMPTY;
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(data.getValues().getFirst()).orElse(null);
        return block == null || block.asItem() == Items.AIR ? ItemStack.EMPTY : block.asItem().getDefaultInstance();
    }

    private static void setItem(ItemSlot slot, ItemStack stack) { slot.setItem(stack == null ? ItemStack.EMPTY : stack.copy(), false); }
}
