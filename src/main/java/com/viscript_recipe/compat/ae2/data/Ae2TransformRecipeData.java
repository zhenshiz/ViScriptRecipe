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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import java.util.ArrayList;
import java.util.List;

/** Stores editable AE2 Transform recipe data independently of AE2's runtime classes. */
@Getter
@Setter
@Accessors(chain = true)
public class Ae2TransformRecipeData implements IVSRecipeData {
    @Persisted
    private List<Ae2IngredientData> inputs = new ArrayList<>(List.of(Ae2IngredientData.of(RecipeIngredient.item(BuiltInRegistries.ITEM.get(Ae2RecipeEditorTypes.id("charged_certus_quartz_crystal")))),
            Ae2IngredientData.of(RecipeIngredient.item(Items.REDSTONE)), Ae2IngredientData.of(RecipeIngredient.item(Items.QUARTZ))));
    @Persisted
    private ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(Ae2RecipeEditorTypes.id("fluix_crystal")), 2);
    @Persisted
    private boolean explosion = false;
    @Persisted
    private ResourceLocation fluidTag = ResourceLocation.withDefaultNamespace("water");

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return Ae2RecipeFactory.compile(this);
    }
}
