package com.viscript_recipe.compat.farm_and_charm.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

/** Positions Farm & Charm slots in logical JEI pixels; item icons remain centered in their 18-pixel cells. */
public final class FarmCharmCanvasFactory {
    private FarmCharmCanvasFactory() {}

    public static UIElement panel(String id, int width, int height) {
        return new UIElement().setId(id).layout(layout -> {
            layout.width(width); layout.height(height); layout.flexShrink(0);
            layout.positionType(TaffyPosition.RELATIVE);
        }).addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) { RecipeCanvas.selectRecipe(); event.stopPropagation(); }
        });
    }

    public static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100); layout.flex(1); layout.minWidth(0); layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER); layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    public static UIElement at(UIElement element, int x, int y, int width, int height) {
        return element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE); layout.left(x); layout.top(y);
            layout.width(width); layout.height(height);
        });
    }

    public static UIElement slot(UIElement element, String id, int x, int y) {
        element.setId(id);
        element.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        return at(element, x, y, 18, 18);
    }

    public static UIElement standardSlot(ItemSlot element, String id, int x, int y) {
        RecipeCanvas.configureJeiOverlaySlotVisual(element);
        element.style(style -> style.backgroundTexture(SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                "jei", "textures/jei/atlas/gui/slot.png")).setSprite(0, 0, 18, 18)));
        return slot(element, id, x, y);
    }

    public static UIElement arrow(int x, int y) {
        var texture = SpriteTexture.of(ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/recipe_arrow.png"))
                .setSprite(0, 0, 22, 16);
        return at(new UIElement().style(style -> style.backgroundTexture(texture)), x, y, 22, 16);
    }

}
