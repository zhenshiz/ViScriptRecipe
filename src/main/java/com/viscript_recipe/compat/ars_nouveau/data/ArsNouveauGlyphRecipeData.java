package com.viscript_recipe.compat.ars_nouveau.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauGlyphRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> inputs = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.LANTERN),
            RecipeIngredient.item(Items.TORCH)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.PAPER);
    @Persisted
    private int exp = 27;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileGlyph(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("ars_nouveau:glyph_light", Items.PAPER)));
    }
}
