package com.viscript_recipe.compat.farm_and_charm.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.farm_and_charm.FarmCharmRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import java.util.ArrayList;
import java.util.List;

/** Persists Farm & Charm inputs and native parameters without loading the optional mod's classes. */
@Getter
@Setter
@Accessors(chain = true)
public class FarmCharmRecipeData implements IVSRecipeData {
    @Persisted
    private List<FarmCharmIngredientData> inputs = new ArrayList<>(List.of(
            FarmCharmIngredientData.of(RecipeIngredient.item(Items.WHEAT))));
    @Persisted
    private ItemStack result = Items.BREAD.getDefaultInstance();
    @Persisted
    private ItemStack container = Items.BOWL.getDefaultInstance();
    @Persisted
    private boolean containerRequired = true;
    @Persisted
    private boolean requiresLearning;
    @Persisted
    private float experience;
    @Persisted
    private String processingCategory = "MEAT";

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return FarmCharmRecipeFactory.compile(typeId, this); }
}
