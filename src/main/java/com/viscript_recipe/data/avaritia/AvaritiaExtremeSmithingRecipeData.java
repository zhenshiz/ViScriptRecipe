package com.viscript_recipe.data.avaritia;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeFactory;
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
public class AvaritiaExtremeSmithingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient template = RecipeIngredient.item(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.DIAMOND_SWORD);
    @Persisted
    private List<RecipeIngredient> additions = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT),
            RecipeIngredient.item(Items.NETHERITE_INGOT)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHERITE_SWORD);

    public RecipeIngredient createDefaultAddition() {
        return RecipeIngredient.item(Items.NETHERITE_INGOT);
    }

    public RecipeIngredient addition(int index) {
        if (additions == null || index < 0 || index >= additions.size()) {
            return RecipeIngredient.empty();
        }
        var addition = additions.get(index);
        return addition == null ? RecipeIngredient.empty() : addition;
    }

    public AvaritiaExtremeSmithingRecipeData setAddition(int index, RecipeIngredient ingredient) {
        ensureAdditionSize();
        additions.set(Math.clamp(index, 0, 2), ingredient == null ? RecipeIngredient.empty() : ingredient);
        return this;
    }

    public List<RecipeIngredient> normalizedAdditions() {
        ensureAdditionSize();
        return additions.stream().limit(3).toList();
    }

    private void ensureAdditionSize() {
        if (additions == null) {
            additions = new ArrayList<>();
        }
        while (additions.size() < 3) {
            additions.add(createDefaultAddition());
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return AvaritiaRecipeFactory.compileExtremeSmithing(this);
    }
}
