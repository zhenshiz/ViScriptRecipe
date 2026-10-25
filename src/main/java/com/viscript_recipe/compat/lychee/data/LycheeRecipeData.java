package com.viscript_recipe.compat.lychee.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.lychee.LycheeRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 持久化完整原生配方，保留无法通过物品槽表示的动作、条件和自定义组件。 */
@Getter
@Setter
@Accessors(chain = true)
public final class LycheeRecipeData implements IVSRecipeData {
    @Persisted private String recipeJson = "{}";

    @Override
    public void applyDefaultData(ResourceLocation typeId) { recipeJson = LycheeRecipeFactory.defaults(typeId); }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return LycheeRecipeFactory.compile(typeId, recipeJson); }
}
