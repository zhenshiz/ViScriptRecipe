package com.viscript_recipe.compat.alloy_smelter.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds the Alloy Smelter recipe canvas from the coordinates used by its JEI categories.
 *
 * <p>The displayed coal is a read-only JEI hint. Alloy Smelter recipes store fuel consumption,
 * but they do not store a fuel ingredient.
 */
public final class AlloySmelterCanvasFactory {
    private static final ResourceLocation JEI_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "alloy_smelter",
            "textures/gui/jei_alloy_smelter.png"
    );
    private static final int WIDTH = 176;
    private static final int HEIGHT = 85;
    private static final int SLOT_SIZE = 18;

    private AlloySmelterCanvasFactory() {
    }

    public static UIElement create(
            UIElement[] inputs,
            UIElement output,
            UIElement fuelHint,
            Label time,
            Label fuel,
            Label tier,
            Runnable onPropertiesSelected
    ) {
        var panel = new UIElement().layout(layout -> {
            layout.width(WIDTH);
            layout.height(HEIGHT);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(sprite(0, 0, WIDTH, HEIGHT)));

        for (int index = 0; index < inputs.length; index++) {
            panel.addChild(positioned(inputs[index], 20 + 21 * index, 25, SLOT_SIZE, SLOT_SIZE));
        }
        panel.addChildren(
                positioned(output, 135, 25, SLOT_SIZE, SLOT_SIZE),
                positioned(fuelHint, 20, 45, SLOT_SIZE, SLOT_SIZE),
                textureElement(sprite(177, 0, 14, 14), 42, 46, 14, 14),
                textureElement(sprite(176, 14, 24, 17), 131, 47, 24, 17),
                configureLabel(tier, 8, 6, 72, Horizontal.LEFT),
                configureLabel(fuel, 0, 65, 56, Horizontal.CENTER),
                configureLabel(time, 115, 65, 56, Horizontal.CENTER)
        );
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) {
                onPropertiesSelected.run();
                event.stopPropagation();
            }
        });

        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static Label configureLabel(Label label, int left, int top, int width, Horizontal alignment) {
        label.textStyle(style -> style
                .fontSize(5)
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(alignment)
                .textAlignVertical(Vertical.TOP)
                .textWrap(TextWrap.NONE));
        return (Label) positioned(label, left, top, width, 9);
    }

    private static UIElement positioned(UIElement element, int left, int top, int width, int height) {
        element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        });
        return element;
    }

    private static UIElement textureElement(IGuiTexture texture, int left, int top, int width, int height) {
        return positioned(new UIElement(), left, top, width, height)
                .style(style -> style.backgroundTexture(texture));
    }

    private static SpriteTexture sprite(int left, int top, int width, int height) {
        return SpriteTexture.of(JEI_TEXTURE).setSprite(left, top, width, height);
    }
}
