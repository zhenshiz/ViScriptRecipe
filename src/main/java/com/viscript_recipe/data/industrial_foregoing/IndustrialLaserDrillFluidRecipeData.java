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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialLaserDrillFluidRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.laser.fluid_output", subConfigurable = true)
    private IndustrialFluidIngredientData output = new IndustrialFluidIngredientData();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.laser.catalyst", subConfigurable = true)
    private RecipeIngredient catalyst = RecipeIngredient.item(Items.WHITE_STAINED_GLASS_PANE);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition", subConfigurable = true)
    private IndustrialEntityConditionData entityCondition = new IndustrialEntityConditionData();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.laser.rarity")
    @ConfigList(addDefaultMethod = "createDefaultRarity")
    private List<IndustrialLaserDrillRarityData> rarity = new ArrayList<>(List.of(new IndustrialLaserDrillRarityData()));

    /** Creates a default catch-all rarity rule. */
    public IndustrialLaserDrillRarityData createDefaultRarity() {
        return new IndustrialLaserDrillRarityData();
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileLaserFluid(this);
    }
}
