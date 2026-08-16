package com.viscript_recipe.compat.industrial_foregoing.data;

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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

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

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setCatalyst(RecipeIngredient.item(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("industrialforegoing:red_laser_lens", Items.RED_STAINED_GLASS_PANE)))
                .setOutput(FluidIngredientData.fluid(new FluidStack(Fluids.LAVA, 100)));
    }
}
