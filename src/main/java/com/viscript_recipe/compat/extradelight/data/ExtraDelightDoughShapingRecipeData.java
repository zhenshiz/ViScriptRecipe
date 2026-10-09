package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.workstations.doughshaping.recipes.DoughShapingRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.item;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.requiredItem;

@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightDoughShapingRecipeData extends ExtraDelightRecipeData {
    @Persisted
    private String group = "";
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted
    private ItemStack result = new ItemStack(Items.BREAD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new DoughShapingRecipe(group, compileIngredient(0), requiredItem(result));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(item("sugar_cookie_dough"))));
        result = item("raw_sugar_cookie_diamond");
    }
}
