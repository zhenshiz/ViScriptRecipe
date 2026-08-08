package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialLaserDrillFluidRecipeData implements IVSRecipeData {
    @Persisted
    private FluidIngredientData output = FluidIngredientData.of();
    @Persisted
    private RecipeIngredient catalyst = RecipeIngredient.item(Items.WHITE_STAINED_GLASS_PANE);
    @Persisted
    private IndustrialEntityConditionData entityCondition = new IndustrialEntityConditionData();
    @Persisted
    private List<IndustrialLaserDrillRarityData> rarity = new ArrayList<>(List.of(new IndustrialLaserDrillRarityData()));

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileLaserFluid(this);
    }
}
