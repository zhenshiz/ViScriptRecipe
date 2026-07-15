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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/** Editable representation of {@code FluidExtractorRecipe.CODEC}. */
@Getter
@Setter
@Accessors(chain = true)
public class IndustrialFluidExtractorRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_extractor.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.OAK_LOG);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_extractor.result_block")
    private ResourceLocation resultBlock = ResourceLocation.withDefaultNamespace("stripped_oak_log");

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.block_state.properties")
    @ConfigList(addDefaultMethod = "createDefaultProperty")
    private List<IndustrialBlockStatePropertyData> resultProperties = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_extractor.break_chance")
    private float breakChance = 0.01F;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_extractor.output")
    private FluidStack output = new FluidStack(Fluids.WATER, 4);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_extractor.default_recipe")
    private boolean defaultRecipe;

    /** Creates an empty block-state property row. */
    public IndustrialBlockStatePropertyData createDefaultProperty() {
        return new IndustrialBlockStatePropertyData();
    }

    /** Compiles the editor data into Industrial Foregoing's native recipe. */
    public Recipe<?> compile() {
        return IndustrialForegoingRecipeFactory.compileFluidExtractor(this);
    }
}
