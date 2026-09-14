package com.viscript_recipe.compat.kaleidoscope_tavern.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

public final class KaleidoscopeTavernCanvasFactory {
    private static final int JEI_SLOT_SIZE = 18;

    private KaleidoscopeTavernCanvasFactory() {
    }

    public static UIElement createBarrelCanvas(UIElement fluidSlot, UIElement[] ingredientSlots, UIElement carrierSlot, UIElement resultSlot) {
        var panel = createPanel("barrel", 180, 150);
        panel.addChild(createCell(fluidSlot, 10, 9));
        for (int i = 0; i < ingredientSlots.length; i++) {
            panel.addChild(createCell(ingredientSlots[i], 30 + i * JEI_SLOT_SIZE, 9));
        }
        panel.addChild(createCell(carrierSlot, 84, 117));
        panel.addChild(createCell(resultSlot, 152, 86));
        return centerPanel(panel);
    }

    public static UIElement createPressingTubCanvas(UIElement inputSlot, UIElement fluidSlot, Label pressCountLabel) {
        var panel = createPanel("pressing_tub", 155, 50);
        panel.addChild(createCell(inputSlot, 32, 13));
        panel.addChild(createCell(fluidSlot, 128, 18));
        panel.addChild(createLabelCell(pressCountLabel, 0, 39, 155, 10));
        return centerPanel(panel);
    }

    public static UIElement createShakerCanvas(UIElement[] ingredientSlots, UIElement resultSlot) {
        var panel = createPanel("shaker", 150, 80);
        for (int i = 0; i < ingredientSlots.length; i++) {
            panel.addChild(createCell(ingredientSlots[i], 52, 14 + i * JEI_SLOT_SIZE));
        }
        panel.addChild(createCell(resultSlot, 112, 36));
        return centerPanel(panel);
    }

    private static UIElement createPanel(String texturePath, int width, int height) {
        return new UIElement().layout(layout -> {
            layout.width(width);
            layout.height(height);
            layout.positionType(TaffyPosition.RELATIVE);
        }).style(style -> style.backgroundTexture(jeiTexture(texturePath, width, height)));
    }

    private static IGuiTexture jeiTexture(String path, int width, int height) {
        return SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                "kaleidoscope_tavern",
                "textures/gui/jei/" + path + ".png"
        )).setSprite(0, 0, width, height);
    }

    private static UIElement centerPanel(UIElement panel) {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    private static UIElement createCell(UIElement slot, int left, int top) {
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(JEI_SLOT_SIZE);
            layout.height(JEI_SLOT_SIZE);
        }).addChild(slot);
    }

    private static UIElement createLabelCell(Label label, int left, int top, int width, int height) {
        label.layout(layout -> {
            layout.width(width);
            layout.height(height);
        });
        return new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(left);
            layout.top(top);
            layout.width(width);
            layout.height(height);
        }).addChild(label);
    }

    public static ItemStack fluidBucket(ResourceLocation id) {
        var fluid = BuiltInRegistries.FLUID.get(id);
        return new ItemStack(fluid.getBucket());
    }

    public static ResourceLocation fluidId(ItemStack stack) {
        if (stack.isEmpty()) return ResourceLocation.withDefaultNamespace("water");
        var item = stack.getItem();
        for (var fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.getBucket() == item) return BuiltInRegistries.FLUID.getKey(fluid);
        }
        return ResourceLocation.withDefaultNamespace("water");
    }
}
