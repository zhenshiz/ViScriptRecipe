package com.viscript_recipe.compat.irons_spellbooks.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.RecipeHelper;
import dev.vfyjxf.taffy.style.AlignItems;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class IronAlchemistCauldronCanvas extends FluidRecipeCanvas<IronAlchemistCauldronRecipeData> {
    static final boolean useJeiCanvas = AlchemistCauldronCanvasFactory.hasJeiSkin();
    static final ResourceLocation IRON_SCROLL_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "scroll");

    public IronAlchemistCauldronCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        setVisualOutput(0, isBrewEntry() ? ItemStack.EMPTY : data.getResult());
        if (isBrewEntry()) {
            setVisualFluidOutput(0, data.getBaseFluid());
            setVisualFluidOutput(1, data.firstResultFluid());
        } else setVisualFluidOutput(0, data.getFluid());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        if (isBrewEntry()) {
            data.setByproduct(ItemStack.EMPTY);
            data.setBaseFluid(getVisualFluidOutput(0));
            data.setFirstResultFluid(getVisualFluidOutput(1));
        } else {
            data.setResult(getVisualOutput(0).getItem());
            data.setFluid(getVisualFluidOutput(0));
        }
    }

    boolean isBrewEntry() {return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW);}
    boolean isFillEntry() {return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL);}

    @Override
    public void buildRecipeProperties(UIElement content) {
        if (isBrewEntry()) return;
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.alchemist_cauldron"));
        var data = getData();
        if (isFillEntry()) content.addChild(switchField(
                "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.must_fit_all", data.isMustFitAll(), data::setMustFitAll));
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.irons_spellbooks.alchemist_cauldron.sound",
                RecipeEditorUi.resourceLocationField(data.getSound(), data::setSound)));
    }

    @Override
    public UIElement createCanvas() {
        boolean isBrew = isBrewEntry();
        boolean isFill = isFillEntry();
        var ingredientSlot = createIngredientSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : SLOT_SIZE);
        var middleFluidSlot = createFluidOutputSlot(0, useJeiCanvas ? 16 : 30);
        var resultFluidSlot = createFluidOutputSlot(1, useJeiCanvas ? 16 : 30);
        var outputSlot = createOutputSlot(0, useJeiCanvas ? 16 : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(ingredientSlot);
            var chanceLabel = RecipeEditorUi.label(Component.empty());
            if (isBrew) configureInputSlot(ingredientSlot, chanceLabel);
            configureJeiOverlayFluidSlotVisual(middleFluidSlot);
            configureJeiOverlayFluidSlotVisual(resultFluidSlot);
            configureJeiOverlaySlotVisual(outputSlot);
            return AlchemistCauldronCanvasFactory.createCanvas(
                    ingredientSlot, middleFluidSlot, resultFluidSlot.setDisplay(isBrew),
                    outputSlot.setDisplay(!isBrew),
                    createItemIcon(new ItemStack(RecipeHelper.itemFromRegistry(
                            "irons_spellbooks:alchemist_cauldron", Items.CAULDRON
                    )), 23), chanceLabel
            );
        }
        var outputLabel = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.alchemist_cauldron.result_item"));
        var middleFluidLabel = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.alchemist_cauldron."
                + (isFill ? "result_fluid" : isBrew ? "base_fluid" : "input_fluid")));
        var outputFluidLabel = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.alchemist_cauldron.result_fluid"));
        var resultFluidColumn = createFluidColumn(outputFluidLabel, resultFluidSlot);
        var outputItemColumn = RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(outputLabel, outputSlot);

        var inputPlusLabel = createOperatorPlusLabel().setDisplay(!isFill);
        var inputArrow = createArrowElement().setDisplay(isFill);
        var outputArrow = createArrowElement().setDisplay(!isFill);
        var outputPlusLabel = createOperatorPlusLabel().setDisplay(isFill);
        return BasicRecipeCanvasFactory.createAlchemistCanvas(
                RecipeEditorUi.column().layout(layout -> {
                    layout.width(72);
                    layout.gapAll(4);
                    layout.alignItems(AlignItems.CENTER);
                }).addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.alchemist_cauldron.input")),
                        ingredientSlot
                ),
                inputPlusLabel,
                inputArrow,
                createFluidColumn(middleFluidLabel, middleFluidSlot),
                outputArrow,
                resultFluidColumn.setDisplay(isBrew),
                outputPlusLabel,
                outputItemColumn.setDisplay(!isBrew)
        );
    }

    void configureInputSlot(IngredientDisplaySlot slot, Label chanceLabel) {
        slot.registerValueListener(stack -> {
            boolean showChance = IRON_SCROLL_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            chanceLabel.setDisplay(showChance);
            if (showChance) {
                var chance = (int) Math.clamp(ServerConfigs.SCROLL_RECYCLE_CHANCE.get(), 0, 1) * 100;
                var color = chance >= 100 ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor();
                chanceLabel.setText(Component.literal(chance + "%"))
                        .textStyle(style -> style.textColor(color == null ? 0xFFFFFF : color));
            }
        });
    }

    static Label createOperatorPlusLabel() {
        Label label = RecipeEditorUi.label(Component.literal("+"));
        label.textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color));
        label.layout(layout -> layout.width(22).height(24));
        return label;
    }

    static UIElement createArrowElement() {
        return new UIElement().layout(layout -> {
            layout.width(28);
            layout.height(16);
        }).style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }

    static UIElement createFluidColumn(Label label, FluidSlot slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(label, slot);
    }
}
