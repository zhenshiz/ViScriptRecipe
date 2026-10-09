package com.viscript_recipe.compat.eidolon.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.eidolon.EidolonRecipeEditorTypes;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class EidolonCanvasFactory {
    private EidolonCanvasFactory() {}

    static UIElement page(String texture, int width, int height) {
        var panel = new UIElement().layout(layout -> {
            layout.width(width); layout.height(height); layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK));
        var background = EidolonRecipeEditorTypes.id("textures/gui/" + texture);
        if (ViScriptRecipe.isModLoaded("jei") && Minecraft.getInstance().getResourceManager().getResource(background).isPresent()) {
            panel.addChild(at(new UIElement().style(style -> style.backgroundTexture(
                    SpriteTexture.of(background).setSprite(0, 0, 128, 160))), 5, 4, width - 10, height - 12));
        }
        return panel;
    }

    static UIElement slot(UIElement child, int left, int top) {
        return at(new UIElement().style(style -> style.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE)).addChild(child),
                left, top, 18, 18);
    }

    static UIElement at(UIElement element, int left, int top, int width, int height) {
        return element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE); layout.left(left); layout.top(top);
            layout.width(width); layout.height(height);
        });
    }

    static UIElement label(Component component, int left, int top, int width) {
        var label = RecipeEditorUi.label(component);
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textShadow(false)
                .fontSize(8).textWrap(TextWrap.HOVER_ROLL)
                .textColor(ViScriptRecipe.isModLoaded("jei") ? 0xFF3D3125 : 0xFFFFFFFF));
        label.style(style -> style.tooltips(component));
        return at(label, left, top, width, 12);
    }

    static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100); layout.flex(1);
            layout.alignItems(AlignItems.CENTER); layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }
}
