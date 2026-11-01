package com.viscript_recipe.compat.neoecoae.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 保存完整原生配方定义，未编辑的字段保持原样。 */
@Getter
@Setter
@Accessors(chain = true)
public final class NeoEcoRecipeData implements IVSRecipeData {
    @Persisted private String recipeJson = "{}";

    @Override
    public void applyDefaultData(ResourceLocation typeId) { recipeJson = NeoEcoRecipeFactory.defaults(typeId); }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return NeoEcoRecipeFactory.compile(typeId, recipeJson); }
}
