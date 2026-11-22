package com.viscript_recipe.compat.forbidden_arcanus.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.stal111.forbidden_arcanus.common.item.crafting.ApplyModifierRecipe;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.id;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues.requireIngredient;
import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public final class ApplyModifierRecipeData implements IVSRecipeData {
    @Persisted private NativeIngredientData template = new NativeIngredientData();
    @Persisted private NativeIngredientData addition = new NativeIngredientData();
    @Persisted private ResourceLocation modifier = id("eternal");

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        template = new NativeIngredientData().setValue(RecipeIngredient.item(
                itemFromRegistry("forbidden_arcanus:apply_modifier_smithing_template", Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
        addition = new NativeIngredientData().setValue(RecipeIngredient.item(
                itemFromRegistry("forbidden_arcanus:eternal_stella", Items.NETHER_STAR)));
    }

    @Override
    public ApplyModifierRecipe compile(ResourceLocation typeId) {return compile(typeId, Platform.getFrozenRegistry());}

    @Override
    public ApplyModifierRecipe compile(ResourceLocation typeId, HolderLookup.Provider provider) {
        return new ApplyModifierRecipe(requireIngredient(template, provider), requireIngredient(addition, provider),
                provider.lookupOrThrow(FARegistries.ITEM_MODIFIER).getOrThrow(ResourceKey.create(FARegistries.ITEM_MODIFIER, modifier)));
    }
}
