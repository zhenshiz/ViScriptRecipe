package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

final class FarmersDelightCanvasFactory {
    private FarmersDelightCanvasFactory() {
    }

    static UIElement createCookingPotCanvas(UIElement ingredientGrid, UIElement heatSource, UIElement potPreview, UIElement servingRow) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(116);
                    layout.gapAll(9);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(ingredientGrid, heatSource),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(78);
                    layout.height(128);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(rotatedArrow(Icons.UP_ARROW_NO_BAR, 30, 60)),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(134);
                    layout.height(138);
                    layout.gapAll(8);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChildren(potPreview, servingRow)
        );
    }

    static UIElement createHeatSource(UIElement icon) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(86);
            layout.height(30);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.farmersdelight.cooking.heat_source")))
                .addChild(icon);
    }

    static UIElement createPotPreview(UIElement potIcon, UIElement previewSlot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(88);
            layout.height(72);
            layout.paddingAll(5);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.farmersdelight.cooking.pot_preview"))
        ).addChildren(
                RecipeEditorUi.row().layout(layout -> {
                    layout.widthPercent(100);
                    layout.height(12);
                    layout.alignItems(AlignItems.CENTER);
                    layout.justifyContent(AlignContent.CENTER);
                }).addChild(potIcon),
                previewSlot
        );
    }

    static UIElement createServingRow(UIElement containerSlot, UIElement outputSlot) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.width(130);
            layout.height(34);
            layout.gapAll(8);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                containerSlot,
                rotatedArrow(Icons.RIGHT_ARROW_NO_BAR, 24, 16),
                outputSlot
        );
    }

    static UIElement createCuttingBoardCanvas(UIElement inputColumn, UIElement toolColumn, UIElement resultGrid) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(16);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                inputColumn,
                operatorPlusLabel(),
                toolColumn,
                arrowElement(),
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(86);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.farmersdelight.cutting.results")),
                        resultGrid
                )
        );
    }

    static UIElement createCuttingInput(String labelKey, UIElement slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey)),
                slot
        );
    }

    private static Label operatorPlusLabel() {
        Label label = RecipeEditorUi.label(Component.literal("+"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(22).height(24));
        return label;
    }

    private static UIElement arrowElement() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    private static UIElement rotatedArrow(com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture texture, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
        }).style(style -> style.backgroundTexture(texture.copy().rotate(-90)));
    }
}
