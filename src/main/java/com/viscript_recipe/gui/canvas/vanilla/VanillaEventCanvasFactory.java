package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;

public final class VanillaEventCanvasFactory {
    private static final ResourceLocation ANVIL = ResourceLocation.withDefaultNamespace("textures/gui/container/anvil.png");
    private static final ResourceLocation BREWING = ResourceLocation.withDefaultNamespace("textures/gui/container/brewing_stand.png");

    private VanillaEventCanvasFactory() {}

    // 槽位坐标和画布尺寸沿用 JEI 1.21.1 的原版配方分类。
    public static UIElement anvil(UIElement left, UIElement right, UIElement output, Label cost) {
        cost.textStyle(style -> style.textAlignHorizontal(Horizontal.RIGHT).textAlignVertical(Vertical.CENTER).textShadow(true));
        return centered(panel("vanilla_anvil_layout", 125, 38).addChildren(
                standardSlot(left, 0, 0), standardSlot(right, 49, 0), standardSlot(output, 107, 0),
                texture(jeiSprite("recipe_plus_sign", 13, 13, sprite(ANVIL, 53, 48, 13, 13)), 27, 3, 13, 13),
                texture(jeiSprite("recipe_arrow", 22, 16, sprite(ANVIL, 102, 47, 22, 16)), 76, 1, 22, 16),
                positioned(cost, 2, 27, 121, 10)
        ));
    }

    public static UIElement brewing(UIElement leftBottle, UIElement middleBottle, UIElement rightBottle,
                                    UIElement material, UIElement output) {
        return centered(panel("vanilla_brewing_layout", 114, 61).addChildren(
                texture(jeiSprite("brewing_stand_background", 64, 60, sprite(BREWING, 55, 15, 64, 60)), 0, 1, 64, 60),
                texture(jeiSprite("brewing_stand_blaze_heat", 18, 4, minecraftBrewingSprite("fuel_length")), 5, 30, 18, 4),
                texture(jeiSprite("brewing_stand_bubbles", 11, 28, minecraftBrewingSprite("bubbles")), 9, 1, 11, 28),
                texture(jeiSprite("brewing_stand_arrow", 7, 27, minecraftBrewingSprite("brew_progress")), 43, 3, 7, 27),
                positioned(leftBottle, 1, 37, 16, 16), positioned(middleBottle, 24, 44, 16, 16),
                positioned(rightBottle, 47, 37, 16, 16), positioned(material, 24, 3, 16, 16),
                standardSlot(output, 80, 2)
        ));
    }

    private static UIElement standardSlot(UIElement item, int left, int top) {
        return texture(jeiSprite("slot", 18, 18, sprite(ANVIL, 26, 46, 18, 18)), left, top, 18, 18)
                .addChild(positioned(item, 1, 1, 16, 16));
    }

    private static UIElement panel(String id, int width, int height) {
        return new UIElement().setId(id).layout(layout -> layout.width(width).height(height).flexShrink(0)
                .positionType(TaffyPosition.RELATIVE));
    }

    private static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> layout.widthPercent(100).flex(1).minWidth(0).minHeight(0)
                .alignItems(AlignItems.CENTER).justifyContent(AlignContent.CENTER)).addChild(panel);
    }

    private static UIElement positioned(UIElement element, int left, int top, int width, int height) {
        return element.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE).left(left).top(top).width(width).height(height));
    }

    private static UIElement texture(SpriteTexture sprite, int left, int top, int width, int height) {
        return positioned(new UIElement(), left, top, width, height).style(style -> style.backgroundTexture(sprite));
    }

    private static SpriteTexture jeiSprite(String name, int width, int height, SpriteTexture fallback) {
        var location = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/" + name + ".png");
        return ViScriptRecipe.isPresentResource(location) ? sprite(location, 0, 0, width, height) : fallback;
    }

    private static SpriteTexture minecraftBrewingSprite(String name) {
        return SpriteTexture.of(ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/brewing_stand/" + name + ".png"));
    }

    private static SpriteTexture sprite(ResourceLocation location, int x, int y, int width, int height) {
        return SpriteTexture.of(location).setSprite(x, y, width, height);
    }
}
