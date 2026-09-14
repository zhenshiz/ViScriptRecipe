package com.viscript_recipe.compat.kaleidoscope_cookery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
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
public class KaleidoscopeTeapotRecipeData implements IVSRecipeData {
    @Persisted
    private ResourceLocation teaFluid = ResourceLocation.withDefaultNamespace("water");
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.WHEAT_SEEDS).setCount(12);
    @Persisted
    private int time = 240;
    @Persisted
    private ItemStack result = new ItemStack(Items.POTION);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileTeapot(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("kaleidoscope_cookery:barley_tea", Items.POTION)));
    }
}
