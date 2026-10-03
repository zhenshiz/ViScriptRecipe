package com.viscript_recipe.compat.draconic_evolution.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.draconic_evolution.DraconicEvolutionRecipeEditorTypes;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Coordinates match DE 1.21.1's FusionRecipeCategory, with readable spacing for large pages. */
final class FusionCraftingCanvasFactory {
    private static final int WIDTH = 164;
    private static final int HEIGHT = 111;
    private static final int SLOT = 18;
    private static final ResourceLocation BACKGROUND =
            DraconicEvolutionRecipeEditorTypes.id("textures/gui/jei_fusion_background.png");

    private FusionCraftingCanvasFactory() {}

    static boolean hasJeiSkin() {
        return ViScriptRecipe.isModLoaded("jei")
                && Minecraft.getInstance().getResourceManager().getResource(BACKGROUND).isPresent();
    }

    static UIElement create(UIElement catalyst, UIElement[] injectors, UIElement output,
                            Label tier, Label energy, boolean skin) {
        int columns = injectors.length > 16 ? 4 : 2;
        int rows = (injectors.length + columns - 1) / columns;
        // More than five rows overlap in the native JEI formula. Keep every editable slot accessible.
        int height = rows > 5 ? Math.max(HEIGHT, 24 + rows * 20 + 24) : HEIGHT;
        IGuiTexture background = skin ? SpriteTexture.of(BACKGROUND).setSprite(0, 0, WIDTH, HEIGHT) : Sprites.BORDER_DARK;
        var panel = new UIElement().layout(layout -> {
            layout.width(WIDTH);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(background));
        int centerX = WIDTH / 2 - 8;
        int centerY = height / 2;
        panel.addChildren(positioned(catalyst, centerX, centerY - 8 - 23),
                positioned(output, centerX, centerY - 8 + 23),
                label(tier, 1, 3, WIDTH - 2, 12),
                label(Component.translatable("viscript_recipe.config.draconicevolution.total_energy"), 1, height - 20, WIDTH - 2, 10),
                label(energy, 1, height - 10, WIDTH - 2, 10));
        for (int i = 0; i < injectors.length; i++) {
            int side = i % columns >= columns / 2 ? 1 : -1;
            int offset = columns == 2 ? 0 : i % 2 == 0 ? -1 : 1;
            int x = centerX + side * (60 + offset * 10);
            int row = i / columns;
            int y = rows == 1 ? centerY - 8 : rows > 5 ? 23 + row * 20
                    : centerY - 42 + (84 / (rows - 1)) * row - 8;
            panel.addChild(positioned(injectors[i], x, y));
        }
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static UIElement positioned(UIElement element, int left, int top) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(SLOT);
            layout.height(SLOT);
        }).style(style -> style.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE)).addChild(element);
    }

    private static UIElement label(Component text, int left, int top, int width, int height) {
        return label(RecipeEditorUi.label(text), left, top, width, height);
    }

    private static UIElement label(Label label, int left, int top, int width, int height) {
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textShadow(false));
        return label.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        });
    }
}
