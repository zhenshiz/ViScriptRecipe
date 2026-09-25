package com.viscript_recipe.compat.immersive_engineering.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.text.DecimalFormat;
import java.util.function.Supplier;

/** Renders IE's original GUI sprites at their native JEI pixel sizes. */
final class IECanvasVisuals {
    private IECanvasVisuals() {}

    static UIElement panel(String id, int width, int height) {
        return new UIElement().setId(id).layout(layout -> {
            layout.width(width); layout.height(height); layout.flexShrink(0);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) { RecipeCanvas.selectRecipe(); event.stopPropagation(); }
        });
    }

    static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100); layout.flex(1); layout.minWidth(0); layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER); layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    static UIElement at(UIElement element, int x, int y, int width, int height) {
        return element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE); layout.left(x); layout.top(y);
            layout.width(width); layout.height(height);
        });
    }

    static SpriteTexture sprite(String file, int u, int v, int width, int height) {
        return SpriteTexture.of(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "textures/gui/" + file + ".png"))
                .setSprite(u, v, width, height);
    }

    static SpriteTexture vanillaFurnace(int u, int v, int width, int height) {
        return SpriteTexture.of(ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png"))
                .setSprite(u, v, width, height);
    }

    static SpriteTexture slotTexture() {
        return SpriteTexture.of(ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/slot.png"))
                .setSprite(0, 0, 18, 18);
    }

    static void background(UIElement panel, String file, int u, int v, int width, int height) {
        panel.style(style -> style.backgroundTexture(new ColorRectTexture(0xFFC6C6C6)));
        image(panel, sprite(file, u, v, width, height), 0, 0, width, height);
    }

    static void imageBehindSlot(UIElement panel, int x, int y) {
        image(panel, slotTexture(), x - 1, y - 1, 18, 18);
    }

    static void image(UIElement panel, IGuiTexture texture, int x, int y, int width, int height) {
        // Decorations must not intercept editor clicks or JEI ingredient drops.
        panel.addChild(at(new UIElement().setAllowHitTest(false)
                .style(style -> style.backgroundTexture(texture)), x, y, width, height));
    }

    static void tankFrame(UIElement panel, int x, int y, int width, int height) {
        IGuiTexture texture = (graphics, mouseX, mouseY, left, top, w, h, tick) -> {
            int minX = Math.round(left), minY = Math.round(top);
            graphics.fill(minX - 1, minY - 1, minX + width, minY + height, 0xFF373737);
            graphics.fill(minX, minY, minX + width + 1, minY + height + 1, 0xFFFFFFFF);
            graphics.fill(minX, minY, minX + width, minY + height, 0xFF8B8B8B);
        };
        image(panel, texture, x, y, width, height);
    }

    static void text(UIElement panel, Supplier<Component> value, int x, int y, boolean rightAligned) {
        IGuiTexture texture = (graphics, mouseX, mouseY, left, top, width, height, partialTick) -> {
            var font = Minecraft.getInstance().font;
            var text = value.get();
            graphics.drawString(font, text, Math.round(left) - (rightAligned ? font.width(text) : 0), Math.round(top), 0xFF777777, false);
        };
        image(panel, texture, x, y, 1, 9);
    }

    static Component seconds(int ticks) {
        return Component.translatable("desc.immersiveengineering.info.seconds", number(ticks / 20d));
    }

    static String number(double value) { return new DecimalFormat("0.##").format(value); }
}
