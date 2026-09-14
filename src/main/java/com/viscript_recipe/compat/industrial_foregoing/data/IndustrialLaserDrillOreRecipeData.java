package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
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
public class IndustrialLaserDrillOreRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient output = RecipeIngredient.item(Items.DIAMOND_ORE);
    @Persisted
    private RecipeIngredient catalyst = RecipeIngredient.item(Items.WHITE_STAINED_GLASS_PANE);
    @Persisted
    private IndustrialEntityConditionData entityCondition = new IndustrialEntityConditionData();
    @Persisted
    private List<IndustrialLaserDrillRarityData> rarity = new ArrayList<>(List.of(new IndustrialLaserDrillRarityData()));

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileLaserOre(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setCatalyst(RecipeIngredient.item(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("industrialforegoing:white_laser_lens", Items.WHITE_STAINED_GLASS_PANE)));
    }
}
