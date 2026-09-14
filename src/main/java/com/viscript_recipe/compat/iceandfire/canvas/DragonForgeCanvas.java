package com.viscript_recipe.compat.iceandfire.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.viscript_recipe.compat.iceandfire.data.DragonForgeRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DragonForgeCanvas extends RecipeCanvas<DragonForgeRecipeData> {
    static final boolean useJeiCanvas = DragonForgeCanvasFactory.hasJeiSkin();

    public DragonForgeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredientSlot(1, data.getBlood());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        data.setBlood(getVisualIngredient(1));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                sectionTitle("viscript_recipe.editor.properties.dragon_forge"),
                field("viscript_recipe.config.iceandfire.dragon_forge.dragon_type",
                        RecipeEditorUi.selector(List.of("fire", "ice", "lightning"), data.getDragonType(),
                                DragonForgeCanvas::dragonTypeDisplayName, value -> {
                                    data.setDragonType(value); reloadCanvas();
                                }
                        )),
                intField("viscript_recipe.config.iceandfire.dragon_forge.cook_time",
                        data.getCookTime(), 1, Integer.MAX_VALUE, data::setCookTime)
        );
    }

    static Component dragonTypeDisplayName(String dragonType) {
        return Component.translatable("viscript_recipe.editor.dragon_forge.dragon_type." + dragonType);
    }

    @Override
    public UIElement createCanvas() {
        var outputSlot = createOutputSlot(0, useJeiCanvas ? JEI_SLOT_SIZE : OUTPUT_SLOT_SIZE);
        if (useJeiCanvas) {
            var inputSlot = jeiInput("viscript_recipe.config.iceandfire.dragon_forge.input", 0);
            var bloodSlot = jeiInput("viscript_recipe.config.iceandfire.dragon_forge.blood", 1);
            configureJeiOverlaySlotVisual(outputSlot);
            return DragonForgeCanvasFactory.createJeiCanvas(
                    inputSlot, bloodSlot, outputSlot,
                    DragonForgeCanvasFactory.createDragonTypeLayer(getData().getDragonType(), RecipeCanvas::selectRecipe)
            );
        }
        return BasicRecipeCanvasFactory.createDragonForgeCanvas(
                createDragonBreathColumn(), createDragonForgeInputColumn(), outputSlot
        );
    }

    private IngredientDisplaySlot jeiInput(String labelKey, int index) {
        var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(slot);
        slot.style(style -> style.tooltips(Component.translatable(labelKey)));
        return slot;
    }

    private UIElement createDragonBreathColumn() {
        var breathLabel = RecipeEditorUi.label(dragonTypeDisplayName(getData().getDragonType()));
        breathLabel.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textAlignHorizontal(Horizontal.CENTER)
                .textWrap(TextWrap.HOVER_ROLL));
        breathLabel.layout(layout -> {
            layout.widthPercent(100);
            layout.height(24);
        });
        var panel = RecipeEditorUi.column().layout(layout -> {
            layout.width(76);
            layout.height(64);
            layout.paddingAll(5);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).style(style -> style
                .backgroundTexture(Sprites.BORDER_DARK)
                .tooltips(Component.translatable("viscript_recipe.editor.dragon_forge.breath_tip"))
        ).addChildren(
                RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.dragon_forge.breath"))
                        .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                        .layout(layout -> layout.widthPercent(100).height(16)),
                breathLabel
        );
        panel.addEventListener(UIEvents.MOUSE_DOWN, event -> selectRecipe());
        return panel;
    }

    private UIElement createDragonForgeInputColumn() {
        return RecipeEditorUi.column().layout(layout -> {
                    layout.width(108);
                    layout.height(72);
                    layout.paddingAll(6);
                    layout.gapAll(5);
                    layout.alignItems(AlignItems.CENTER);
                }).style(style -> style.backgroundTexture(Sprites.BORDER_DARK))
                .addChildren(
                        RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.dragon_forge.forge_inputs"))
                                .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                                .layout(layout -> layout.widthPercent(100).height(16)),
                        RecipeEditorUi.row().layout(layout -> {
                            layout.widthPercent(100);
                            layout.height(SLOT_SIZE);
                            layout.gapAll(6);
                            layout.alignItems(AlignItems.CENTER);
                            layout.justifyContent(AlignContent.CENTER);
                        }).addChildren(
                                createInput("viscript_recipe.config.iceandfire.dragon_forge.input", 0),
                                createInput("viscript_recipe.config.iceandfire.dragon_forge.blood", 1)
                        )
                );
    }

    private UIElement createInput(String labelKey, int index) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(42);
            layout.gapAll(2);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                RecipeEditorUi.label(Component.translatable(labelKey))
                        .textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(ColorPattern.LIGHT_GRAY.color))
                        .layout(layout -> layout.widthPercent(100).height(12)),
                createIngredientSlot(index, SLOT_SIZE)
        );
    }
}
