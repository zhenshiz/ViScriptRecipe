package com.viscript_recipe.compat.powah.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.powah.PowahRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 保存原生配方定义，保留标签、复合材料和产物组件。 */
@Getter
@Setter
@Accessors(chain = true)
public final class PowahRecipeData implements IVSRecipeData {
    @Persisted private String recipeJson = """
            {"type":"powah:energizing","ingredients":[{"item":"minecraft:iron_ingot"},{"item":"minecraft:gold_ingot"}],
             "result":{"id":"powah:steel_energized","count":2},"energy":10000}
            """;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return PowahRecipeFactory.compile(typeId, recipeJson); }
}
