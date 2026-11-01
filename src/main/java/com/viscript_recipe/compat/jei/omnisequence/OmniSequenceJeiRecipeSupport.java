package com.viscript_recipe.compat.jei.omnisequence;

import com.atir.molecularmanipulator.crafting.MatterRecipeBridge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.HashSet;

/** 判断来源配方变化是否影响原生整机导入的 JEI 展示。 */
public final class OmniSequenceJeiRecipeSupport {
    private OmniSequenceJeiRecipeSupport() {}

    public static boolean affectsImportedRecipes(Collection<RecipeHolder<?>> recipes, Collection<RecipeHolder<?>> changed) {
        var serializers = new HashSet<String>();
        for (var holder : changed) {
            var id = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer());
            if (id != null) serializers.add(id.toString());
        }
        return MatterRecipeBridge.machines(recipes).stream()
                .anyMatch(machine -> machine.recipeTypes().stream().anyMatch(serializers::contains));
    }
}
