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

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeStockpotRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BEEF),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));
    @Persisted
    private ResourceLocation soupBase = ResourceLocation.withDefaultNamespace("water");
    @Persisted
    private ItemStack result = new ItemStack(Items.RABBIT_STEW);
    @Persisted
    private int time = 300;
    @Persisted
    private RecipeIngredient carrier = RecipeIngredient.item(Items.BOWL);
    @Persisted
    private ResourceLocation cookingTexture = KaleidoscopeCookeryRecipeEditorTypes.kaleidoscope("stockpot/default_cooking");
    @Persisted
    private ResourceLocation finishedTexture = KaleidoscopeCookeryRecipeEditorTypes.kaleidoscope("stockpot/default_finished");
    @Persisted
    private int cookingBubbleColor = 0xFFECC3;
    @Persisted
    private int finishedBubbleColor = 0xF4AA8B;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileStockpot(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:borscht", Items.RABBIT_STEW)));
    }
}
