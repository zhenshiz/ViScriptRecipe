package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.RecipeHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
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
public class IndustrialFluidExtractorRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.OAK_LOG);
    @Persisted
    private ResourceLocation resultBlock = ResourceLocation.withDefaultNamespace("stripped_oak_log");
    @Persisted
    private List<IndustrialBlockStatePropertyData> resultProperties = new ArrayList<>();
    @Persisted
    private float breakChance = 0.01F;
    @Persisted
    private FluidStack output = new FluidStack(Fluids.WATER, 4);
    @Persisted
    private boolean defaultRecipe;

    public ItemStack getResult() {
        return new ItemStack(BuiltInRegistries.BLOCK.get(resultBlock));
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileFluidExtractor(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setOutput(new FluidStack(RecipeHelper.fluidFromRegistry("industrialforegoing:latex", Fluids.WATER), 4));
    }
}
