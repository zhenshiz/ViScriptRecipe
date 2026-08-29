package com.viscript_recipe.compat.kaleidoscope_cookery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeMillstoneRecipeData implements IVSRecipeData {
    public static final int MAX_RESULTS = 4;

    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.WHEAT);

    @Deprecated
    @Persisted
    private ItemStack result = new ItemStack(Items.BONE_MEAL);
    @Persisted
    private List<RecipeOutputData> results = new ArrayList<>();

    public List<RecipeOutputData> getResolvedResults() {
        if (results != null && !results.isEmpty()) {
            return results.stream()
                    .filter(Objects::nonNull)
                    .limit(MAX_RESULTS)
                    .map(RecipeOutputData::copy)
                    .toList();
        }
        if (result == null || result.isEmpty()) {
            return List.of();
        }
        return List.of(RecipeOutputData.of(result));
    }

    public KaleidoscopeMillstoneRecipeData setResults(List<RecipeOutputData> results) {
        this.results = results == null ? new ArrayList<>() : results.stream()
                .filter(Objects::nonNull)
                .limit(MAX_RESULTS)
                .map(RecipeOutputData::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        this.result = ItemStack.EMPTY;
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileMillstone(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResults(List.of(RecipeOutputData.of(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("kaleidoscope_cookery:flour", Items.BONE_MEAL)))));
    }
}
