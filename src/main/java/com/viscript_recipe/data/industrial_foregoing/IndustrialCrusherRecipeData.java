package com.viscript_recipe.data.industrial_foregoing;

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

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialCrusherRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.crusher.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.crusher.output", subConfigurable = true)
    private RecipeIngredient output = RecipeIngredient.item(Items.GRAVEL);

    @Override
    public ItemStack getResult() {return IndustrialForegoingRecipeEditorTypes.firstStack(getOutput());}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        setOutput(RecipeIngredient.item(result));
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileCrusher(this);
    }
}
