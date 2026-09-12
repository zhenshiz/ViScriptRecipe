package com.viscript_recipe.compat.ae2.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ae2.Ae2RecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import com.viscript_recipe.compat.ae2.Ae2RecipeEditorTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** Stores editable AE2 Inscriber recipe data independently of AE2's runtime classes. */
@Getter
@Setter
@Accessors(chain = true)
public class Ae2InscriberRecipeData implements IVSRecipeData {
    @Persisted
    private Ae2IngredientData middle = Ae2IngredientData.of(RecipeIngredient.item(BuiltInRegistries.ITEM.get(Ae2RecipeEditorTypes.id("silicon"))));
    @Persisted
    private Ae2IngredientData top = Ae2IngredientData.of(RecipeIngredient.item(BuiltInRegistries.ITEM.get(Ae2RecipeEditorTypes.id("silicon_press"))));
    @Persisted
    private Ae2IngredientData bottom = Ae2IngredientData.of(RecipeIngredient.empty());
    @Persisted
    private ItemStack result = BuiltInRegistries.ITEM.get(Ae2RecipeEditorTypes.id("printed_silicon")).getDefaultInstance();
    @Persisted
    private boolean consumePresses = false;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return Ae2RecipeFactory.compile(this);
    }
}
