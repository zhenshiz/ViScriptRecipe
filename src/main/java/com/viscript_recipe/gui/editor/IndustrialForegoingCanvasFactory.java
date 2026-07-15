package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

/** Creates JEI-proportioned Industrial Foregoing recipe canvases using editor-owned slots. */
final class IndustrialForegoingCanvasFactory {
    private IndustrialForegoingCanvasFactory() {
    }

    static UIElement createDissolution(UIElement[] inputs, UIElement inputFluid, UIElement itemOutput,
                                       UIElement fluidOutput, Label processingLabel) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
        });
        grid.addChildren(dissolutionRow(inputs[0], inputs[1], inputs[2]),
                dissolutionRow(inputs[3], inputFluid, inputs[4]),
                dissolutionRow(inputs[5], inputs[6], inputs[7]));
        return canvas(220, 126).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.gapAll(12);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(energyBar(), grid, inputFluid, arrow(), itemOutput, fluidOutput),
                processingLabel
        );
    }

    static UIElement createFluidExtractor(UIElement input, UIElement blockOutput, UIElement fluidOutput,
                                          Label productionLabel) {
        return canvas(210, 108).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.gapAll(18);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(input, arrow(), blockOutput, fluidOutput),
                productionLabel
        );
    }

    static UIElement createCrusher(UIElement input, UIElement action, UIElement output) {
        return canvas(210, 86).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.gapAll(12);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(input, arrow(), action, arrow(), output)
        );
    }

    static UIElement createLaser(UIElement catalyst, UIElement output, Label rangeLabel, Label requirementsLabel) {
        return canvas(190, 132).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.gapAll(42);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(catalyst, arrow(), output),
                centeredSummary(rangeLabel),
                centeredSummary(requirementsLabel)
        );
    }

    static UIElement createStoneWork(UIElement output, Label needsLabel, Label consumesLabel) {
        return canvas(210, 112).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.gapAll(18);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(output, RecipeEditorUi.column().layout(layout -> layout.gapAll(5))
                        .addChildren(needsLabel, consumesLabel))
        );
    }

    static UIElement slotCell(UIElement slot, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.paddingAll(1);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK)).addChild(slot);
    }

    private static UIElement canvas(int width, int height) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.paddingAll(10);
            layout.gapAll(9);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
    }

    private static UIElement arrow() {
        return RecipeEditorUi.label(Component.literal("➜")).layout(layout -> layout.width(18).height(18));
    }

    private static Label centeredSummary(Label label) {
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
        label.layout(layout -> {
            layout.widthPercent(100);
            layout.height(12);
        });
        return label;
    }

    private static UIElement dissolutionRow(UIElement left, UIElement center, UIElement right) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(left, center, right);
    }

    private static UIElement energyBar() {
        return new UIElement().layout(layout -> layout.width(8).height(58))
                .style(style -> style.backgroundTexture(Sprites.BORDER_DARK)
                        .tooltips(Component.translatable("viscript_recipe.editor.industrial_foregoing.energy_info")));
    }
}
