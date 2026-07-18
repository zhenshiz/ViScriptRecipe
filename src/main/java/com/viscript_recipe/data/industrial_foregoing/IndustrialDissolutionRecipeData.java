package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialDissolutionRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 8;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.dissolution.input")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> input = new ArrayList<>(List.of(RecipeIngredient.item(Items.IRON_INGOT)));

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.dissolution.input_fluid", subConfigurable = true)
    private IndustrialFluidIngredientData inputFluid = new IndustrialFluidIngredientData();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.processing_time")
    private int processingTime = 300;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.dissolution.has_item_output")
    private boolean hasItemOutput = true;

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack output = new ItemStack(Items.DIAMOND);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.dissolution.has_fluid_output")
    private boolean hasFluidOutput;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.dissolution.output_fluid")
    private FluidStack outputFluid = new FluidStack(Fluids.WATER, 1000);

    /** Creates an empty ingredient for the persisted eight-slot input list. */
    public RecipeIngredient createDefaultIngredient() {
        return new RecipeIngredient();
    }

    @Override
    public ItemStack getResult() {return hasItemOutput ? getOutput() : ItemStack.EMPTY;}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        setHasItemOutput(!result.isEmpty()).setOutput(result);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileDissolution(this);
    }
}
