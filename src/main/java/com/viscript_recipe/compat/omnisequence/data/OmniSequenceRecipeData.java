package com.viscript_recipe.compat.omnisequence.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 保存完整原生定义；画布只替换用户实际修改的字段。 */
@Getter
@Setter
@Accessors(chain = true)
public final class OmniSequenceRecipeData implements IVSRecipeData {
    @Persisted private String recipeJson = "{}";

    @Override
    public void applyDefaultData(ResourceLocation typeId) { recipeJson = OmniSequenceRecipeFactory.defaults(typeId); }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return OmniSequenceRecipeFactory.compile(typeId, recipeJson); }
}
