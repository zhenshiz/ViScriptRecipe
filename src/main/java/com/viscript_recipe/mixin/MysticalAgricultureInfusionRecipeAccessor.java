package com.viscript_recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe")
public interface MysticalAgricultureInfusionRecipeAccessor {
    /** 为 true 时，将祭坛输入的数据组件传递给产物。 */
    @Accessor("transferComponents")
    boolean viscriptRecipe$getTransferComponents();
}
