package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Editable representation of {@code LaserDrillOreRecipe.CODEC}. */
@Getter
@Setter
@Accessors(chain = true)
public class IndustrialLaserDrillOreRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.laser.output", subConfigurable = true)
    private RecipeIngredient output = RecipeIngredient.item(Items.DIAMOND_ORE);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.laser.output_count")
    private int outputCount = 1;

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

    /** Compiles the editor data into Industrial Foregoing's native recipe. */
    public Recipe<?> compile() {
        return IndustrialForegoingRecipeFactory.compileLaserOre(this);
    }
}
