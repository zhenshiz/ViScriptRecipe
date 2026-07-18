package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientData;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientKind;
import com.viscript_recipe.data.mekanism.MekanismChemicalStackData;
import dev.vfyjxf.taffy.style.TaffyPosition;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.GuiUtils;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

/** Renders a Mekanism chemical as a tinted gauge or compact bar for recipe previews. */
final class MekanismChemicalVisualDisplay implements MekanismCanvasFactory.ChemicalDisplay {
    private final ChemicalTexture fillTexture = new ChemicalTexture();
    private final UIElement fill;
    private final UIElement root;

    MekanismChemicalVisualDisplay(String gaugeFrame) {
        this(gaugeFrame, "standard.png", 16, 58);
    }

    MekanismChemicalVisualDisplay(String gaugeFrame, String gaugeOverlay, int overlayWidth, int overlayHeight) {
        this(
                overlayWidth,
                overlayHeight,
                SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                        "mekanism",
                        "gui/gauge/" + gaugeFrame
                )).setSprite(0, 0, 5, 5).setBorder(2),
                SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                        "mekanism",
                        "gui/gauge/" + gaugeOverlay
                )).setSprite(0, 0, overlayWidth, overlayHeight)
        );
    }

    MekanismChemicalVisualDisplay(int barWidth, int barHeight) {
        this(
                barWidth,
                barHeight,
                SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                        "mekanism",
                        "gui/bar/base.png"
                )).setSprite(0, 0, 5, 5).setBorder(2),
                IGuiTexture.EMPTY
        );
    }

    private MekanismChemicalVisualDisplay(
            int contentWidth,
            int contentHeight,
            IGuiTexture frameTexture,
            IGuiTexture overlayTexture
    ) {
        fill = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(contentWidth);
            layout.height(contentHeight);
        }).style(style -> style.backgroundTexture(fillTexture));
        var overlay = new UIElement().layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(1);
            layout.top(1);
            layout.width(contentWidth);
            layout.height(contentHeight);
        }).style(style -> style.backgroundTexture(overlayTexture));
        root = new UIElement().layout(layout -> {
            layout.width(contentWidth + 2);
            layout.height(contentHeight + 2);
        }).style(style -> style.backgroundTexture(frameTexture));
        root.addChildren(fill, overlay);
    }

    static ItemStack[] catalystStacks(MekanismChemicalIngredientData data) {
        if (data == null) {
            return new ItemStack[0];
        }
        var amount = Math.max(1, data.getAmount());
        var creator = IngredientCreatorAccess.chemicalStack();
        var ingredient = data.getKind() == MekanismChemicalIngredientKind.TAG
                ? data.getTag() == null ? null : creator.from(
                        TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, data.getTag()), amount
                )
                : data.getChemical() == null ? null : MekanismAPI.CHEMICAL_REGISTRY.getHolder(data.getChemical())
                        .map(holder -> creator.fromHolder(holder, amount))
                        .orElse(null);
        return ingredient == null
                ? new ItemStack[0]
                : RecipeViewerUtils.getStacksFor(ingredient, true).toArray(ItemStack[]::new);
    }

    static IGuiTexture energyTexture() {
        return new EnergyTexture();
    }

    static int colorRepresentation(MekanismChemicalIngredientData data) {
        if (data == null) {
            return 0xFFFFFFFF;
        }
        Chemical chemical;
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            var tag = data.getTag() == null ? null : TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, data.getTag());
            chemical = tag == null ? null : MekanismAPI.CHEMICAL_REGISTRY.getTag(tag)
                    .flatMap(holders -> holders.stream().findFirst())
                    .map(Holder::value)
                    .orElse(null);
        } else {
            chemical = chemical(data.getChemical());
        }
        return colorRepresentation(chemical);
    }

    static int colorRepresentation(MekanismChemicalStackData data) {
        return colorRepresentation(data == null ? null : chemical(data.getChemical()));
    }

    @Override
    public UIElement element() {
        return root;
    }

    @Override
    public void setInput(MekanismChemicalIngredientData data) {
        if (data == null) {
            set(null, Component.empty());
            return;
        }
        var amount = Math.max(1, data.getAmount());
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            var tag = data.getTag() == null ? null : TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, data.getTag());
            var chemical = tag == null ? null : MekanismAPI.CHEMICAL_REGISTRY.getTag(tag)
                    .flatMap(holders -> holders.stream().findFirst())
                    .map(Holder::value)
                    .orElse(null);
            set(chemical, Component.literal("#" + data.getTag() + " × " + amount));
            return;
        }
        set(chemical(data.getChemical()), Component.literal(String.valueOf(data.getChemical()) + " × " + amount));
    }

    @Override
    public void setOutput(MekanismChemicalStackData data) {
        if (data == null || data.isEmpty()) {
            set(null, Component.empty());
            return;
        }
        set(chemical(data.getChemical()), Component.literal(data.getChemical() + " × " + data.getAmount()));
    }

    private void set(Chemical chemical, Component fallback) {
        if (chemical == null || chemical == MekanismAPI.CHEMICAL_REGISTRY.get(MekanismAPI.EMPTY_CHEMICAL_KEY.location())) {
            fill.setDisplay(false);
            root.style(style -> style.tooltips(fallback));
            return;
        }
        fill.setDisplay(true);
        fillTexture.setChemical(chemical);
        root.style(style -> style.tooltips(chemical.getTextComponent().copy()
                .append(Component.literal(" × " + amountFromFallback(fallback)))));
    }

    private static Chemical chemical(ResourceLocation id) {
        return id == null ? null : MekanismAPI.CHEMICAL_REGISTRY.getOptional(id).orElse(null);
    }

    private static int colorRepresentation(Chemical chemical) {
        if (chemical == null || chemical == MekanismAPI.CHEMICAL_REGISTRY.get(
                MekanismAPI.EMPTY_CHEMICAL_KEY.location())) {
            return 0xFFFFFFFF;
        }
        int color = chemical.getColorRepresentation();
        return (color & 0xFF000000) == 0 ? 0xFF000000 | color : color;
    }

    private static String amountFromFallback(Component fallback) {
        var text = fallback.getString();
        var marker = text.lastIndexOf('×');
        return marker < 0 ? "" : text.substring(marker + 1).trim();
    }

    private static final class ChemicalTexture implements IGuiTexture {
        private Chemical chemical;

        private void setChemical(Chemical chemical) {
            this.chemical = chemical;
        }

        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            if (chemical == null || width <= 0 || height <= 0) {
                return;
            }
            graphics.flush();
            MekanismRenderer.color(graphics, 0xFF000000 | chemical.getTint());
            GuiUtils.drawTiledSprite(
                    graphics,
                    Math.round(x),
                    Math.round(y),
                    Math.round(height),
                    Math.round(width),
                    Math.round(height),
                    MekanismRenderer.getSprite(chemical.getIcon()),
                    16,
                    16,
                    100,
                    GuiUtils.TilingDirection.UP_RIGHT
            );
            MekanismRenderer.resetColor(graphics);
        }
    }

    private static final class EnergyTexture implements IGuiTexture {
        @Override
        public void draw(
                GuiGraphics graphics,
                float mouseX,
                float mouseY,
                float x,
                float y,
                float width,
                float height,
                float partialTicks
        ) {
            if (MekanismRenderer.energyIcon == null || width <= 0 || height <= 0) {
                return;
            }
            graphics.flush();
            GuiUtils.drawTiledSprite(
                    graphics,
                    Math.round(x),
                    Math.round(y),
                    Math.round(height),
                    Math.round(width),
                    Math.round(height),
                    MekanismRenderer.energyIcon,
                    16,
                    16,
                    100,
                    GuiUtils.TilingDirection.UP_RIGHT
            );
        }
    }
}
