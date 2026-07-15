package com.viscript_recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the serialized component-transfer flag on Mystical Agriculture awakening recipes.
 */
@Pseudo
@Mixin(targets = "com.blakebr0.mysticalagriculture.crafting.recipe.AwakeningRecipe")
public interface MysticalAgricultureAwakeningRecipeAccessor {
    /**
     * Gets whether the recipe transfers components from the altar input to its result.
     *
     * @return {@code true} when the recipe transfers input components
     */
    @Accessor("transferComponents")
    boolean viscriptRecipe$getTransferComponents();
}
