package com.viscript_recipe.compat.irons_spellbooks.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.recipe.RecipeHelper;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class IronAlchemistCauldronCanvas extends FluidRecipeCanvas<IronAlchemistCauldronRecipeData> {
    private final List<FluidSlot> fluids = new ArrayList<>();

    public IronAlchemistCauldronCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void initVisualState() {
        fluids.clear();
        super.initVisualState();
    }

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        setVisualOutput(0, isBrewEntry() ? data.getByproduct() : data.getResult());
        setVisualFluidOutput(0, isBrewEntry() ? data.getBaseFluid() : data.getFluid());
        if (isBrewEntry()) {
            for (int i = 0; i < data.getResultFluids().size(); i++) setVisualFluidOutput(i + 1, data.getResultFluids().get(i));
        }
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        if (isBrewEntry()) {
            data.setByproduct(getVisualOutput(0).getItem()).setBaseFluid(getVisualFluidOutput(0));
            var outputs = new ArrayList<FluidStack>();
            for (int i = 1; i < fluids.size(); i++) outputs.add(getVisualFluidOutput(i));
            data.setResultFluids(outputs);
        } else data.setResult(getVisualOutput(0).getItem()).setFluid(getVisualFluidOutput(0));
    }

    private boolean isBrewEntry() {return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW);}
    private boolean isFillEntry() {return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL);}

    @Override
    public String selectedFluidConfigNameKey() {
        return "viscript_recipe.editor.alchemist_cauldron." + (isBrewEntry()
                ? selectedSlotIndex() == 2 ? "base_fluid" : "result_fluid"
                : isFillEntry() ? "result_fluid" : "input_fluid");
    }

    @Override
    public void setVisualOutput(int index, ItemStack item) {
        super.setVisualOutput(index, isBrewEntry() ? item.copyWithCount(1) : item);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (!isBrewEntry()) {
            super.buildResultProperties(content);
            return;
        }
        // 上游副产物使用 SINGLE_ITEM_CODEC，数量固定为一件。
        content.addChildren(sectionTitle("viscript_recipe.editor.alchemist_cauldron.byproduct"),
                PropertiesView.removeCountConfig(PropertiesView.createItemStackConfigurator(
                        "viscript_recipe.editor.alchemist_cauldron.byproduct",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput)));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(sectionTitle("viscript_recipe.editor.properties.alchemist_cauldron"));
        var data = getData();
        if (isBrewEntry()) {
            var add = RecipeEditorUi.textButton(Component.translatable("viscript_recipe.editor.alchemist_cauldron.add_fluid"),
                    Icons.ADD, event -> resizeOutputs(true));
            add.setId("irons_cauldron_add_fluid");
            var remove = RecipeEditorUi.textButton(Component.translatable("viscript_recipe.editor.alchemist_cauldron.remove_fluid"),
                    Icons.REMOVE, event -> resizeOutputs(false));
            remove.setId("irons_cauldron_remove_fluid");
            content.addChildren(add, remove);
            return;
        }
        if (isFillEntry()) content.addChild(switchField(
                "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.must_fit_all", data.isMustFitAll(), data::setMustFitAll));
        content.addChild(RecipeSearchComponents.sound(
                "viscript_recipe.config.irons_spellbooks.alchemist_cauldron.sound",
                data::getSound, data::setSound, () -> {}, isFillEntry() ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_FILL)
                .setId("irons_cauldron_sound"));
    }

    private void resizeOutputs(boolean add) {
        save();
        var outputs = getData().getResultFluids();
        if (add) outputs.add(FluidStack.EMPTY);
        else if (!outputs.isEmpty()) outputs.remove(outputs.size() - 1);
        navigationView.loadCanvas();
        reloadProperties();
    }

    @Override
    public UIElement createCanvas() {
        setId("irons_cauldron_canvas");
        boolean brew = isBrewEntry();
        boolean fill = isFillEntry();
        int count = brew ? getData().getResultFluids().size() : 0;
        boolean jei = AlchemistCauldronCanvasFactory.hasJeiSkin() && count <= 1;
        var ingredient = createIngredientSlot(0, jei ? JEI_SLOT_SIZE : SLOT_SIZE);
        ingredient.setId("irons_cauldron_input");
        var middle = createFluidOutputSlot(0, jei ? 16 : 30);
        var output = createOutputSlot(0, jei ? 16 : OUTPUT_SLOT_SIZE);
        output.setId("irons_cauldron_item_output");
        var fluidResults = RecipeEditorUi.row().layout(layout -> {
            layout.maxWidth(160);
            layout.gapAll(4);
            layout.flexWrap(FlexWrap.WRAP);
        });
        for (int i = 0; i < count; i++) fluidResults.addChild(createFluidOutputSlot(i + 1, jei ? 16 : 30));
        if (jei) {
            configureJeiOverlaySlotVisual(ingredient, output);
            for (var slot : fluids) configureJeiOverlayFluidSlotVisual(slot);
            return AlchemistCauldronCanvasFactory.createCanvas(ingredient, middle,
                    fluidResults.setDisplay(brew && count > 0), output,
                    createItemIcon(new ItemStack(RecipeHelper.itemFromRegistry("irons_spellbooks:alchemist_cauldron", Items.CAULDRON)), 23), brew);
        }
        return BasicRecipeCanvasFactory.createAlchemistCanvas(
                column("viscript_recipe.editor.alchemist_cauldron.input", ingredient),
                plus().setDisplay(!fill), arrow().setDisplay(fill),
                column("viscript_recipe.editor.alchemist_cauldron." + (fill ? "result_fluid" : brew ? "base_fluid" : "input_fluid"), middle),
                arrow().setDisplay(!fill),
                column("viscript_recipe.editor.alchemist_cauldron.result_fluid", fluidResults).setDisplay(brew && count > 0),
                plus().setDisplay(fill || brew && count > 0),
                column("viscript_recipe.editor.alchemist_cauldron." + (brew ? "byproduct" : "result_item"), output));
    }

    @Override
    public FluidSlot createFluidOutputSlot(int index, int size) {
        var slot = createFluidSlot(size);
        fluids.add(slot);
        slot.setId("irons_cauldron_fluid_" + index);
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 1) {
                setVisualFluidOutput(index, FluidStack.EMPTY);
                event.stopPropagation();
            }
        });
        return slot;
    }

    @Override
    public FluidStack getVisualFluidOutput(int index) {return fluids.get(index).getFluid().copy();}

    @Override
    public void setVisualFluidOutput(int index, FluidStack stack) {fluids.get(index).setFluid(stack.copy(), false);}

    private static UIElement column(String key, UIElement slot) {
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(RecipeEditorUi.label(Component.translatable(key)), slot);
    }

    private static Label plus() {
        return (Label) RecipeEditorUi.label(Component.literal("+"))
                .textStyle(style -> style.fontSize(22).textColor(ColorPattern.GRAY.color))
                .layout(layout -> layout.width(22).height(24));
    }

    private static UIElement arrow() {
        return new UIElement().layout(layout -> layout.width(28).height(16))
                .style(style -> style.backgroundTexture(Icons.ARROW_LEFT_RIGHT));
    }
}
