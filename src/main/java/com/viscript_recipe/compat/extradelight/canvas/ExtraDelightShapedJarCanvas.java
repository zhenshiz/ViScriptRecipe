package com.viscript_recipe.compat.extradelight.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extradelight.data.ExtraDelightShapedJarRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeEditorTypes.key;
import static com.viscript_recipe.compat.extradelight.canvas.ExtraDelightRecipeCanvas.*;

/** 共用有序合成的网格转换方法，另行编辑罐中需要消耗的流体。 */
public final class ExtraDelightShapedJarCanvas extends FluidRecipeCanvas<ExtraDelightShapedJarRecipeData> {
    public ExtraDelightShapedJarCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() {
        ShapedGridHelper.loadGrid(this, getData().getPattern(), getData().getKey(), 3, 3, 3);
        setVisualOutput(0, getData().getResult());
        for (int i = 0; i < getData().getFluids().size(); i++)
            setVisualFluidInput(i, com.viscript_recipe.data.FluidIngredientData.fluid(getData().getFluids().get(i)));
    }

    @Override
    public void save() {
        var pattern = ShapedGridHelper.saveGrid(this, 3, 3, 3);
        getData().setPattern(new ArrayList<>(pattern.pattern())).setKey(new ArrayList<>(pattern.key())).setResult(getVisualOutput(0).getItem());
        var fluids = new ArrayList<FluidStack>();
        for (int i = 0; i < 9; i++) {
            var ingredient = getVisualFluidInput(i);
            if (!ingredient.isEmpty()) fluids.add(ingredient.getFluid().copyWithAmount(ingredient.getAmount()));
        }
        getData().setFluids(fluids);
    }

    @Override
    public UIElement createCanvas() {
        var panel = new UIElement().setId("extradelight_canvas_shaped_jar").layout(l -> { l.width(162); l.height(106); l.flexShrink(0); l.positionType(dev.vfyjxf.taffy.style.TaffyPosition.RELATIVE); });
        panel.addChild(at(skin("jei.png", 0, 53, 121, 72), 20, 0, 121, 72));
        for (int i = 0; i < 9; i++) {
            var slot = createIngredientSlot(i, 18); configureJeiOverlaySlotVisual(slot);
            panel.addChild(at(slot.setId("extradelight_input_" + i), 21 + i % 3 * 18, 1 + i / 3 * 18, 18, 18));
            var fluid = createFluidInputSlot(i);
            panel.addChild(at(fluid.setId("extradelight_fluid_input_" + i), i * 18, 88, 18, 18));
        }
        var output = createOutputSlot(0, 18); configureJeiOverlaySlotVisual(output);
        panel.addChildren(at(output.setId("extradelight_output"), 115, 20, 18, 18),
                at(RecipeEditorUi.label(Component.translatable(key("jar_fluids"))), 0, 72, 162, 14));
        return centered(panel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(textField(key("group"), getData().getGroup(), getData()::setGroup),
                selector(key("category"), List.of(CraftingBookCategory.values()), getData().getCategory(),
                        v -> Component.literal(v.getSerializedName()), getData()::setCategory));
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        var ingredient = getSelectedFluidInput();
        content.addChildren(PropertiesView.removeCountConfig(PropertiesView.createFluidStackConfigurator(key("fluid"),
                        ingredient::getFluid, stack -> setSelectedFluidInput(ingredient.setFluid(stack)))),
                intField(key("amount"), ingredient.getAmount(), 1, Integer.MAX_VALUE,
                        value -> setSelectedFluidInput(ingredient.setAmount(value))));
    }
}
