package com.viscript_recipe.compat.cataclysm.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.cataclysm.CataclysmRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class CataclysmWeaponFusionRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private RecipeIngredient addition = RecipeIngredient.item(Items.AMETHYST_SHARD);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CataclysmRecipeFactory.compileWeaponFusion(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setBase(RecipeIngredient.item(itemFromRegistry("cataclysm:infernal_forge", Items.IRON_SWORD)))
                .setAddition(RecipeIngredient.item(itemFromRegistry("cataclysm:void_core", Items.AMETHYST_SHARD)))
                .setResult(new ItemStack(itemFromRegistry("cataclysm:void_forge", Items.DIAMOND_SWORD)));
    }
}
