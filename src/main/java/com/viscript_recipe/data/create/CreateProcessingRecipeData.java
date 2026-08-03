package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CreateProcessingRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.COBBLESTONE)));
    @Persisted
    private List<FluidIngredientData> fluidIngredients = new ArrayList<>();
    @Persisted
    private List<RecipeOutputData> outputs = new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(Items.GRAVEL))));
    @Persisted
    private List<FluidStack> fluidOutputs = new ArrayList<>();
    @Persisted
    private int processingTime = 100;
    @Persisted
    private CreateHeatCondition heatRequirement = CreateHeatCondition.NONE;
    @Persisted
    private boolean keepHeldItem;

    @Override
    public ItemStack getResult() {return CreateRecipeEditorTypes.firstOutput(this);}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        CreateRecipeEditorTypes.setFirstOutput(this, result);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CreateRecipeFactory.compileProcessing(type, this);
    }
}
