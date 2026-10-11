package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.VanillaEventRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class VanillaEventRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_INGOT);
    @Persisted
    private RecipeIngredient material = RecipeIngredient.item(Items.GOLD_INGOT);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND);
    @Persisted
    private int levelCost = 1;
    @Persisted
    private int materialCost = 1;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {return new VanillaEventRecipe(typeId, this);}

    public VanillaEventRecipeData copy() {
        return new VanillaEventRecipeData().setInput(input.copy()).setMaterial(material.copy())
                .setResult(result.copy()).setLevelCost(levelCost).setMaterialCost(materialCost);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(VanillaEventRecipeEditorTypes.BREWING)) {
            setInput(RecipeIngredient.item(PotionContents.createItemStack(Items.POTION, Potions.WATER)))
                    .setMaterial(RecipeIngredient.item(Items.NETHER_WART))
                    .setResult(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));
        }
    }
}
