package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds Mystical Agriculture recipe canvases from the coordinates used by its JEI categories.
 */
final class MysticalAgricultureCanvasFactory {
    static final int SLOT_SIZE = 18;
    private static final ResourceLocation INFUSION = texture("infusion.png");
    private static final ResourceLocation ENCHANTER = texture("enchanter.png");
    private static final ResourceLocation REPROCESSOR = texture("reprocessor.png");
    private static final ResourceLocation SOULIUM_SPAWNER = texture("soulium_spawner.png");
    private static final int[][] INFUSION_PEDESTALS = {
            {7, 7}, {33, 1}, {59, 7}, {65, 33},
            {59, 59}, {33, 64}, {7, 59}, {1, 33}
    };

    private MysticalAgricultureCanvasFactory() {
    }

    static boolean hasJeiSkin() {
        if (!ViScriptRecipe.isModLoaded("mysticalagriculture") || !ViScriptRecipe.isModLoaded("jei")) {
            return false;
        }
        var resources = Minecraft.getInstance().getResourceManager();
        return resources.getResource(INFUSION).isPresent()
                && resources.getResource(ENCHANTER).isPresent()
                && resources.getResource(REPROCESSOR).isPresent()
                && resources.getResource(SOULIUM_SPAWNER).isPresent();
    }

    static UIElement createInfusionCanvas(UIElement input, UIElement[] pedestals, UIElement output, boolean useJei) {
        if (!useJei) {
            return fallbackAltar(input, pedestals, output);
        }
        var panel = panel(144, 81, sprite(INFUSION, 0, 0, 144, 81));
        panel.addChildren(
                positioned(input, 33, 33, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 123, 33, SLOT_SIZE, SLOT_SIZE)
        );
        addPedestals(panel, pedestals);
        return centered(panel);
    }

    static UIElement createAwakeningCanvas(
            UIElement input,
            UIElement[] ingredients,
            UIElement[] essences,
            UIElement output,
            boolean useJei
    ) {
        if (!useJei) {
            var inputs = RecipeEditorUi.row().layout(layout -> {
                layout.width(136);
                layout.gapAll(2);
                layout.flexWrap(FlexWrap.WRAP);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            });
            for (int index = 0; index < ingredients.length; index++) {
                inputs.addChildren(essences[index], ingredients[index]);
            }
            return RecipeEditorUi.row().layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
                layout.gapAll(12);
                layout.alignItems(AlignItems.CENTER);
                layout.justifyContent(AlignContent.CENTER);
            }).addChildren(input, inputs, rightArrow(), output);
        }
        var panel = panel(144, 81, sprite(INFUSION, 0, 0, 144, 81));
        panel.addChildren(
                positioned(input, 33, 33, SLOT_SIZE, SLOT_SIZE),
                positioned(essences[0], 7, 7, SLOT_SIZE, SLOT_SIZE),
                positioned(ingredients[0], 33, 1, SLOT_SIZE, SLOT_SIZE),
                positioned(essences[1], 59, 7, SLOT_SIZE, SLOT_SIZE),
                positioned(ingredients[1], 65, 33, SLOT_SIZE, SLOT_SIZE),
                positioned(essences[2], 59, 59, SLOT_SIZE, SLOT_SIZE),
                positioned(ingredients[2], 33, 64, SLOT_SIZE, SLOT_SIZE),
                positioned(essences[3], 7, 59, SLOT_SIZE, SLOT_SIZE),
                positioned(ingredients[3], 1, 33, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 123, 33, SLOT_SIZE, SLOT_SIZE)
        );
        return centered(panel);
    }

    static UIElement createEnchanterCanvas(UIElement[] ingredients, UIElement book, UIElement output, boolean useJei) {
        if (!useJei) {
            return centered(RecipeEditorUi.row().layout(layout -> {
                layout.gapAll(8);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(ingredients[0], ingredients[1], book, rightArrow(), output));
        }
        var panel = panel(144, 26, sprite(ENCHANTER, 0, 0, 144, 26));
        panel.addChildren(
                positioned(ingredients[0], 1, 5, SLOT_SIZE, SLOT_SIZE),
                positioned(ingredients[1], 23, 5, SLOT_SIZE, SLOT_SIZE),
                positioned(book, 63, 5, SLOT_SIZE, SLOT_SIZE),
                positioned(output, 123, 5, SLOT_SIZE, SLOT_SIZE)
        );
        return centered(panel);
    }

    static UIElement createProcessCanvas(UIElement input, UIElement output, boolean souliumSpawner, boolean useJei) {
        var texture = souliumSpawner ? SOULIUM_SPAWNER : REPROCESSOR;
        if (!useJei) {
            return centered(RecipeEditorUi.row().layout(layout -> {
                layout.gapAll(12);
                layout.alignItems(AlignItems.CENTER);
            }).addChildren(input, rightArrow(), output));
        }
        var panel = panel(82, 26, sprite(texture, 0, 0, 82, 26));
        panel.addChildren(
                positioned(input, 1, 5, SLOT_SIZE, SLOT_SIZE),
                textureElement(sprite(texture, 85, 0, 24, 17), 24, 4, 24, 17),
                positioned(output, 61, 5, SLOT_SIZE, SLOT_SIZE)
        );
        return centered(panel);
    }

    private static UIElement fallbackAltar(UIElement input, UIElement[] pedestals, UIElement output) {
        var pedestalGrid = RecipeEditorUi.row().layout(layout -> {
            layout.width(128);
            layout.gapAll(2);
            layout.flexWrap(FlexWrap.WRAP);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        });
        pedestalGrid.addChildren(pedestals);
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(12);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(input, pedestalGrid, rightArrow(), output);
    }

    private static void addPedestals(UIElement panel, UIElement[] pedestals) {
        for (int index = 0; index < Math.min(pedestals.length, INFUSION_PEDESTALS.length); index++) {
            var position = INFUSION_PEDESTALS[index];
            panel.addChild(positioned(pedestals[index], position[0], position[1], SLOT_SIZE, SLOT_SIZE));
        }
    }

    private static UIElement rightArrow() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static UIElement centered(UIElement content) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minWidth(0);
            layout.minHeight(0);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(content);
    }

    private static UIElement panel(int width, int height, IGuiTexture background) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(background));
    }

    private static UIElement positioned(UIElement element, int left, int top, int width, int height) {
        return element.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        });
    }

    private static UIElement textureElement(IGuiTexture texture, int left, int top, int width, int height) {
        return positioned(new UIElement(), left, top, width, height)
                .style(style -> style.backgroundTexture(texture));
    }

    private static SpriteTexture sprite(ResourceLocation texture, int left, int top, int width, int height) {
        return SpriteTexture.of(texture).setSprite(left, top, width, height);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "textures/jei/" + name);
    }
}
