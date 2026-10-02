package com.viscript_recipe.compat.enderio.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.enderio.EnderIoRecipeEditorTypes;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;

final class EnderIoCanvasLayout {
    private EnderIoCanvasLayout() {}

    static UIElement panel(int width, int height) {
        return new UIElement().layout(l -> { l.width(width); l.height(height); l.flexShrink(0); l.positionType(TaffyPosition.RELATIVE); })
                .addEventListener(UIEvents.MOUSE_DOWN, e -> { if (e.button == 0) RecipeCanvas.selectRecipe(); });
    }

    static UIElement background(String texture, int u, int v, int width, int height) {
        return panel(width, height).style(s -> s.backgroundTexture(SpriteTexture.of(EnderIoRecipeEditorTypes.id("textures/gui/" + texture + ".png"))
                .setSprite(u, v, width, height)));
    }

    static UIElement at(UIElement element, int x, int y, int width, int height) {
        return element.layout(l -> { l.positionType(TaffyPosition.ABSOLUTE); l.left(x); l.top(y); l.width(width); l.height(height); });
    }

    static UIElement slot(ItemSlot slot, String id, int x, int y) {
        RecipeCanvas.configureJeiOverlaySlotVisual(slot);
        slot.setId(id).addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation());
        return at(slot, x, y, 18, 18);
    }

    static com.lowdragmc.lowdraglib2.gui.ui.elements.Label label(Component value, int x, int y, int width) {
        return (com.lowdragmc.lowdraglib2.gui.ui.elements.Label) at(RecipeEditorUi.label(value).textStyle(s -> s.fontSize(9)).style(s -> s.tooltips(value)), x, y, width, 14);
    }

    static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(l -> { l.widthPercent(100); l.flex(1); l.alignItems(AlignItems.CENTER); l.justifyContent(AlignContent.CENTER); }).addChild(panel);
    }
}
