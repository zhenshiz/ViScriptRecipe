package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class CraftingWorkbenchView extends View {
    private static final int SLOT_SIZE = 24;
    private static final int OUTPUT_SLOT_SIZE = 30;

    private final RecipeEditorController controller;
    private final IngredientDisplaySlot[] craftingIngredientSlots = new IngredientDisplaySlot[9];
    private final ItemSlot craftingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot cookingIngredientSlot = createIngredientSlot(SLOT_SIZE);
    private final ItemSlot cookingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final IngredientDisplaySlot[] smithingIngredientSlots = new IngredientDisplaySlot[3];
    private final ItemSlot smithingOutputSlot = createEditorSlot(OUTPUT_SLOT_SIZE);
    private final Label titleLabel = RecipeEditorUi.sectionTitle("viscript_recipe.editor.workbench");
    private final Label statusLabel = RecipeEditorUi.label(Component.empty());
    private UIElement craftingCanvas;
    private UIElement cookingCanvas;
    private UIElement smithingCanvas;

    public CraftingWorkbenchView(RecipeEditorController controller) {
        super("viscript_recipe.view.workbench", Icons.GRID);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(8);
            layout.gapAll(8);
        }).style(style -> style.backgroundTexture(Sprites.RECT_SOLID));

        var top = RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
            layout.alignItems(AlignItems.CENTER);
        });
        titleLabel.layout(layout -> layout.flex(1));
        statusLabel.textStyle(style -> style
                .textAlignHorizontal(Horizontal.RIGHT)
                .textWrap(TextWrap.HOVER_ROLL)
                .textColor(ColorPattern.LIGHT_GRAY.color));
        statusLabel.layout(layout -> {
            layout.width(260);
            layout.height(18);
        });
        top.addChildren(titleLabel, statusLabel);

        root.addChildren(top, createCanvasStack());
        return root;
    }

    private UIElement createCanvasStack() {
        craftingCanvas = createCraftingCanvas();
        cookingCanvas = createCookingCanvas();
        smithingCanvas = createSmithingCanvas();
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        }).addChildren(craftingCanvas, cookingCanvas, smithingCanvas);
    }

    private UIElement createCraftingCanvas() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(24);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                createGrid(),
                new UIElement().layout(layout -> {
                    layout.width(28);
                    layout.height(16);
                }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                        configureResultSlot(craftingOutputSlot)
                )
        );
    }

    private UIElement createSmithingCanvas() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                createSmithingInput("viscript_recipe.editor.smithing.template", 0),
                createSmithingInput("viscript_recipe.editor.smithing.base", 1),
                createSmithingInput("viscript_recipe.editor.smithing.addition", 2),
                new UIElement().layout(layout -> {
                    layout.width(28);
                    layout.height(16);
                }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                        configureResultSlot(smithingOutputSlot)
                )
        );
    }

    private UIElement createSmithingInput(String labelKey, int index) {
        var slot = createIngredientSlot(SLOT_SIZE);
        configureIngredientSlot(slot, index);
        smithingIngredientSlots[index] = slot;
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey)),
                slot
        );
    }

    private UIElement createGrid() {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT_SIZE * 3 + 12);
            layout.height(SLOT_SIZE * 3 + 12);
            layout.paddingAll(4);
            layout.gapAll(2);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));

        for (int row = 0; row < 3; row++) {
            var rowElement = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT_SIZE);
                layout.gapAll(2);
            });
            for (int col = 0; col < 3; col++) {
                var index = row * 3 + col;
                var slot = createIngredientSlot(SLOT_SIZE);
                configureIngredientSlot(slot, index);
                craftingIngredientSlots[index] = slot;
                rowElement.addChild(slot);
            }
            grid.addChild(rowElement);
        }
        return grid;
    }

    private UIElement createCookingCanvas() {
        configureIngredientSlot(cookingIngredientSlot, 0);
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(22);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.cooking.ingredient")),
                        cookingIngredientSlot
                ),
                new UIElement().layout(layout -> {
                    layout.width(28);
                    layout.height(16);
                }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")),
                        configureResultSlot(cookingOutputSlot)
                )
        );
    }

    private IngredientDisplaySlot configureIngredientSlot(IngredientDisplaySlot slot, int index) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualIngredient(index, stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectIngredientSlot(index);
            if (event.button == 1) {
                controller.clearVisualIngredient(index);
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ingredient_slot",
                index + 1
        )));
        return slot;
    }

    private ItemSlot configureResultSlot(ItemSlot slot) {
        slot.registerValueListener(stack -> {
            if (!controller.isRefreshing()) {
                controller.setVisualResult(stack);
            }
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            controller.selectResultSlot();
            if (event.button == 1) {
                controller.clearVisualResult();
                event.stopPropagation();
            }
        });
        slot.style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.result_slot")));
        return slot;
    }

    private void refresh() {
        titleLabel.setText(controller.selectedCategoryDisplayName());
        var singleInput = controller.isSelectedSingleInputLayout();
        var smithing = controller.isSelectedSmithingLayout();
        if (craftingCanvas != null) {
            craftingCanvas.setDisplay(!singleInput && !smithing);
        }
        if (cookingCanvas != null) {
            cookingCanvas.setDisplay(singleInput);
        }
        if (smithingCanvas != null) {
            smithingCanvas.setDisplay(smithing);
        }
        if (smithing) {
            for (int i = 0; i < smithingIngredientSlots.length; i++) {
                refreshIngredientSlot(smithingIngredientSlots[i], i);
            }
            setSlot(smithingOutputSlot, controller.getVisualResult());
        } else if (singleInput) {
            refreshIngredientSlot(cookingIngredientSlot, 0);
            setSlot(cookingOutputSlot, controller.getVisualResult());
        } else {
            for (int i = 0; i < craftingIngredientSlots.length; i++) {
                refreshIngredientSlot(craftingIngredientSlots[i], i);
            }
            setSlot(craftingOutputSlot, controller.getVisualResult());
        }
        updateStatus();
    }

    private void refreshIngredientSlot(IngredientDisplaySlot slot, int index) {
        if (slot == null) {
            return;
        }
        var tagDisplayStacks = controller.getVisualIngredientTagStacks(index);
        if (tagDisplayStacks.length > 0) {
            slot.setTagDisplayStacks(tagDisplayStacks);
        } else {
            slot.clearTagDisplayStacks();
            setSlot(slot, controller.getVisualIngredient(index));
        }
        updateIngredientSlotTooltip(slot, index);
    }

    private void updateIngredientSlotTooltip(ItemSlot slot, int index) {
        var tag = controller.getVisualIngredientTag(index);
        if (tag == null) {
            slot.style(style -> style.tooltips(Component.translatable(
                    "viscript_recipe.editor.ingredient_slot",
                    index + 1
            )));
            return;
        }
        slot.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.ingredient_slot_tag",
                index + 1,
                "#" + tag
        )));
    }

    private void updateStatus() {
        var selectedEntry = controller.getSelectedEntry();
        if (selectedEntry == null) {
            statusLabel.setText(Component.empty());
            return;
        }
        var warningKey = controller.isSelectedContainsUnsupportedIngredients()
                ? "viscript_recipe.editor.status.unsupported_ingredient"
                : "viscript_recipe.editor.status.ready";
        statusLabel.setText(Component.translatable(
                "viscript_recipe.editor.status",
                controller.recipeFile().getEntries().size(),
                controller.recipeFile().getEntries().stream().filter(entry -> entry.isEnabled()).count(),
                Component.translatable(warningKey)
        ));
    }

    private void setSlot(ItemSlot slot, ItemStack stack) {
        slot.setItem(stack == null ? ItemStack.EMPTY : stack.copy(), false);
    }

    private static ItemSlot createEditorSlot(int size) {
        return (ItemSlot) new ItemSlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                });
    }

    private static IngredientDisplaySlot createIngredientSlot(int size) {
        return (IngredientDisplaySlot) new IngredientDisplaySlot()
                .xeiPhantom()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(size);
                    layout.height(size);
                });
    }

    private static class IngredientDisplaySlot extends ItemSlot {
        private ItemStack[] tagDisplayStacks = new ItemStack[0];
        private int tagDisplayIndex;
        private int tagDisplayTicks;

        void setTagDisplayStacks(ItemStack[] stacks) {
            if (stacks == null || stacks.length == 0) {
                clearTagDisplayStacks();
                return;
            }
            var copies = new ItemStack[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                copies[i] = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copyWithCount(1);
            }
            if (sameStacks(tagDisplayStacks, copies)) {
                return;
            }
            tagDisplayStacks = copies;
            tagDisplayIndex = 0;
            tagDisplayTicks = 0;
            setItem(tagDisplayStacks[0].copy(), false);
        }

        void clearTagDisplayStacks() {
            if (tagDisplayStacks.length == 0) {
                return;
            }
            tagDisplayStacks = new ItemStack[0];
            tagDisplayIndex = 0;
            tagDisplayTicks = 0;
        }

        @Override
        public void screenTick() {
            super.screenTick();
            if (tagDisplayStacks.length <= 1) {
                return;
            }
            tagDisplayTicks++;
            if (tagDisplayTicks < 20) {
                return;
            }
            tagDisplayTicks = 0;
            tagDisplayIndex = (tagDisplayIndex + 1) % tagDisplayStacks.length;
            setItem(tagDisplayStacks[tagDisplayIndex].copy(), false);
        }

        private boolean sameStacks(ItemStack[] left, ItemStack[] right) {
            return left.length == right.length && Arrays.equals(stackKeys(left), stackKeys(right));
        }

        private String[] stackKeys(ItemStack[] stacks) {
            var keys = new String[stacks.length];
            for (int i = 0; i < stacks.length; i++) {
                var stack = stacks[i];
                keys[i] = stack == null || stack.isEmpty() ? "" : stack.getItemHolder().unwrapKey().map(Object::toString).orElse(stack.getItem().toString());
            }
            return keys;
        }
    }
}
