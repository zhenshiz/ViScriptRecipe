package com.viscript_recipe.compat.confluence.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Compact JEI-shaped canvases for the Confluence workstation categories. */
public final class ConfluenceCanvasFactory {
    private static final int SLOT = 18;
    private static final ResourceLocation FLETCHING_BACKGROUND = confluenceTexture("fletching_table");
    private static final ResourceLocation ALCHEMY_BACKGROUND = confluenceTexture("alchemy_table");
    private static final ResourceLocation COOKING_BACKGROUND = confluenceTexture("cooking_pot");

    private ConfluenceCanvasFactory() {
    }

    public static UIElement transmutation(UIElement input, UIElement[] targets, UIElement phase) {
        var outputs = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT * 4 + 6);
            layout.height(SLOT * 4 + 6);
            layout.gapAll(2);
        });
        for (int row = 0; row < 4; row++) {
            var line = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT);
                layout.gapAll(2);
            });
            for (int col = 0; col < 4; col++) line.addChild(targets[row * 4 + col]);
            outputs.addChild(line);
        }
        return centered(RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                column(Component.translatable("viscript_recipe.editor.confluence.input"), input),
                arrow(),
                RecipeEditorUi.column().layout(layout -> {
                    layout.gapAll(3);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.confluence.targets")), outputs, phase)
        ));
    }

    public static UIElement amount(UIElement[] inputs, UIElement output) {
        var row = RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(18);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        for (int i = 0; i < Math.min(5, inputs.length); i++) row.addChild(slotCell(inputs[i]));
        row.addChild(arrow());
        row.addChild(column(Component.translatable("viscript_recipe.editor.result"), output));
        return centered(row);
    }

    public static UIElement forge(UIElement[] inputs, UIElement output) {
        return amount(inputs, output);
    }

    public static UIElement either(UIElement[] inputs, UIElement output) {
        var grid = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT * 4 + 6);
            layout.height(SLOT * 4 + 6);
            layout.gapAll(2);
        });
        for (int row = 0; row < 4; row++) {
            var line = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT);
                layout.gapAll(2);
            });
            for (int col = 0; col < 4; col++) line.addChild(inputs[row * 4 + col]);
            grid.addChild(line);
        }
        return centered(RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(22);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(grid, arrow(), column(Component.translatable("viscript_recipe.editor.result"), output)));
    }

    public static UIElement alchemy(UIElement[] inputs, UIElement output) {
        if (hasResource(ALCHEMY_BACKGROUND)) {
            var panel = panel(ALCHEMY_BACKGROUND, 112, 64);
            panel.addChildren(
                    positioned(inputs[0], 48, 1),
                    positioned(inputs[1], 7, 1), positioned(inputs[2], 89, 1),
                    positioned(inputs[3], 7, 21), positioned(inputs[4], 89, 21),
                    positioned(inputs[5], 7, 41), positioned(inputs[6], 89, 41),
                    positioned(output, 48, 46));
            return centered(panel);
        }
        var materials = RecipeEditorUi.column().layout(layout -> {
            layout.width(SLOT * 2 + 2);
            layout.gapAll(2);
        });
        for (int row = 0; row < 3; row++) {
            var line = RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.height(SLOT);
                layout.gapAll(2);
            }).addChildren(inputs[1 + row * 2], inputs[2 + row * 2]);
            materials.addChild(line);
        }
        return centered(RecipeEditorUi.column().layout(layout -> {
            layout.gapAll(5);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(inputs[0], arrowDown(), materials, arrowDown(), output));
    }

    public static UIElement fletching(UIElement[] inputs, UIElement output) {
        if (hasResource(FLETCHING_BACKGROUND)) {
            var panel = panel(FLETCHING_BACKGROUND, 128, 64);
            panel.addChildren(
                    positioned(inputs[0], 7, 42),
                    positioned(inputs[1], 25, 24),
                    positioned(inputs[2], 43, 6),
                    positioned(output, 101, 24));
            return centered(panel);
        }
        return centered(RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(20);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(
                RecipeEditorUi.column().layout(layout -> layout.gapAll(1)).addChildren(inputs[2], inputs[1], inputs[0]),
                arrow(),
                output
        ));
    }

    public static UIElement cooking(UIElement[] inputs, UIElement container, UIElement heat, UIElement output) {
        if (hasResource(COOKING_BACKGROUND)) {
            var panel = panel(COOKING_BACKGROUND, 142, 49);
            panel.addChildren(
                    positioned(inputs[0], 13, 7), positioned(inputs[1], 31, 7),
                    positioned(inputs[2], 13, 25), positioned(inputs[3], 31, 25),
                    positioned(container, 79, 1), positioned(heat, 79, 32),
                    positioned(output, 121, 16));
            return centered(panel);
        }
        var inputGrid = RecipeEditorUi.column().layout(layout -> layout.gapAll(2));
        inputGrid.addChild(RecipeEditorUi.row().layout(layout -> layout.gapAll(2)).addChildren(inputs[0], inputs[1]));
        inputGrid.addChild(RecipeEditorUi.row().layout(layout -> layout.gapAll(2)).addChildren(inputs[2], inputs[3]));
        var auxiliaries = RecipeEditorUi.column().layout(layout -> {
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.confluence.container")), container,
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.confluence.heat_source")), heat);
        var result = RecipeEditorUi.column().layout(layout -> {
            layout.gapAll(3);
            layout.alignItems(AlignItems.CENTER);
        })
                .addChildren(arrow(), output);
        return centered(RecipeEditorUi.row().layout(layout -> {
            layout.gapAll(14);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(inputGrid, auxiliaries, result));
    }

    private static UIElement centered(UIElement element) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(element);
    }

    private static UIElement column(Component label, UIElement child) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.gapAll(3);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(RecipeEditorUi.label(label), child);
    }

    private static UIElement slotCell(UIElement slot) {
        return new UIElement().layout(layout -> {
            layout.width(SLOT);
            layout.height(SLOT);
        }).style(style -> style.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE)).addChild(slot);
    }

    private static UIElement panel(ResourceLocation texture, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(SpriteTexture.of(texture).setSprite(0, 0, width, height)));
    }

    private static UIElement positioned(UIElement child, int left, int top) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(SLOT);
            layout.height(SLOT);
        }).addChild(child);
    }

    private static ResourceLocation confluenceTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath("confluence", "textures/gui/" + name + ".png");
    }

    private static boolean hasResource(ResourceLocation texture) {
        return ViScriptRecipe.isModLoaded("confluence")
                && ViScriptRecipe.isPresentResource(texture);
    }

    private static UIElement arrow() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(18);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static UIElement arrowDown() {
        return new UIElement().layout(layout -> {
            layout.width(18);
            layout.height(18);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR));
    }
}
