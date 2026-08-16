package com.viscript_recipe.compat.kaleidoscope_cookery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeEditorTypes;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeChoppingBoardRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.BEEF);
    @Persisted
    private ItemStack result = new ItemStack(Items.BEEF);
    @Persisted
    private int cutCount = 4;
    @Persisted
    private ResourceLocation modelId = KaleidoscopeCookeryRecipeEditorTypes.kaleidoscope("empty");

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileChoppingBoard(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredient(RecipeIngredient.item(Items.MUTTON))
                .setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                        ("kaleidoscope_cookery:raw_lamb_chops", Items.MUTTON)))
                .setModelId(KaleidoscopeCookeryRecipeEditorTypes.kaleidoscope("raw_lamb_chops"));
    }
}
