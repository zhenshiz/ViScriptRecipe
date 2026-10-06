package com.viscript_recipe.compat.justdirethings.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.VanillaSmithingCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** 与 JEI 一致的紧凑槽位画布，共用 VSR 的居中和属性选择行为。 */
final class JustDireCanvasLayout {
    private JustDireCanvasLayout() {}

    static UIElement at(UIElement element, int x, int y, int w, int h) {
        return element.layout(l -> { l.positionType(TaffyPosition.ABSOLUTE); l.left(x); l.top(y); l.width(w); l.height(h); });
    }

    static UIElement panel(String type, int w, int h) {
        return new UIElement().setId("justdire_canvas_" + type).layout(l -> {
            l.width(w); l.height(h); l.flexShrink(0); l.positionType(TaffyPosition.RELATIVE);
        }).addEventListener(UIEvents.MOUSE_DOWN, e -> { if (e.button == 0) RecipeCanvas.selectRecipe(); });
    }

    static void skin(ItemSlot slot) {
        if (VanillaSmithingCanvasFactory.hasJeiSkin()) {
            RecipeCanvas.configureJeiOverlaySlotVisual(slot);
            slot.style(s -> s.backgroundTexture(SpriteTexture.of(ResourceLocation.parse("jei:textures/jei/atlas/gui/slot.png")).setSprite(0, 0, 18, 18)));
        }
    }

    static UIElement arrow(int x, int y) {
        var arrow = VanillaSmithingCanvasFactory.hasJeiSkin()
                ? new UIElement().style(s -> s.backgroundTexture(SpriteTexture.of(ResourceLocation.parse("jei:textures/jei/atlas/gui/recipe_arrow.png")).setSprite(0, 0, 22, 16)))
                : RecipeEditorUi.label(Component.literal("→"));
        return at(arrow, x, y, 22, 16);
    }

    static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(l -> {
            l.widthPercent(100); l.flex(1); l.minWidth(0); l.minHeight(0);
            l.alignItems(AlignItems.CENTER); l.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }
}
