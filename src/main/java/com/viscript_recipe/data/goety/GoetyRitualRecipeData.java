package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
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
public class GoetyRitualRecipeData implements IVSRecipeData {
    public static final int MAX_PEDESTAL_INGREDIENTS = 12;

    @Persisted
    private RecipeIngredient activationItem = RecipeIngredient.item(Items.BOOK);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
    @Persisted
    private GoetyRitualCraftType craftType = GoetyRitualCraftType.MAGIC;
    @Persisted
    private ResourceLocation ritualType = ResourceLocation.fromNamespaceAndPath("goety", "craft");
    @Persisted
    private int soulCost;
    @Persisted
    private int duration = 30;
    @Persisted
    private int summonLife = -1;
    @Persisted
    private boolean hasSacrifice;
    @Persisted
    private ResourceLocation entityToSacrifice = ResourceLocation.fromNamespaceAndPath("minecraft", "zombies");
    @Persisted
    private String entityToSacrificeDisplayName = "entity.minecraft.zombie";
    @Persisted
    private boolean hasSummon;
    @Persisted
    private ResourceLocation entityToSummon = ResourceLocation.withDefaultNamespace("zombie");
    @Persisted
    private boolean hasConversion;
    @Persisted
    private ResourceLocation entityToConvert = ResourceLocation.fromNamespaceAndPath("minecraft", "zombies");
    @Persisted
    private String entityToConvertDisplayName = "entity.minecraft.zombie";
    @Persisted
    private ResourceLocation entityToConvertInto = ResourceLocation.withDefaultNamespace("zombie_villager");
    @Persisted
    private boolean hasStructure;
    @Persisted
    private ResourceLocation structureToLocate = ResourceLocation.fromNamespaceAndPath("minecraft", "village");
    @Persisted
    private String structureDisplayName = "filled_map.village";
    @Persisted
    private boolean hasEnchantment;
    @Persisted
    private ResourceLocation enchantment = ResourceLocation.withDefaultNamespace("sharpness");
    @Persisted
    private int xpLevelCost;
    @Persisted
    private String research = "";

    /**
     * Returns a pedestal ingredient or an empty value when the index is outside the official twelve-slot layout.
     *
     * @param  index zero-based pedestal index
     * @return the stored ingredient or an empty ingredient
     */
    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= Math.min(MAX_PEDESTAL_INGREDIENTS, ingredients.size())) {
            return RecipeIngredient.empty();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? RecipeIngredient.empty() : ingredient;
    }

    /**
     * Replaces a pedestal ingredient while enforcing the official JEI twelve-slot limit.
     *
     * @param  index zero-based pedestal index
     * @param  ingredient replacement ingredient
     * @return this data object
     */
    public GoetyRitualRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        if (index < 0 || index >= MAX_PEDESTAL_INGREDIENTS) {
            return this;
        }
        ingredients = normalizedIngredients();
        while (ingredients.size() <= index) {
            ingredients.add(RecipeIngredient.empty());
        }
        ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        trimTrailingEmptyIngredients();
        return this;
    }

    /**
     * Returns a defensive pedestal list truncated to the official JEI limit.
     *
     * @return normalized pedestal ingredients
     */
    public List<RecipeIngredient> normalizedIngredients() {
        var normalized = new ArrayList<RecipeIngredient>();
        if (ingredients != null) {
            for (int i = 0; i < Math.min(MAX_PEDESTAL_INGREDIENTS, ingredients.size()); i++) {
                var ingredient = ingredients.get(i);
                normalized.add(ingredient == null ? RecipeIngredient.empty() : ingredient);
            }
        }
        return normalized;
    }

    private void trimTrailingEmptyIngredients() {
        while (!ingredients.isEmpty() && ingredients.getLast().isEmpty()) {
            ingredients.removeLast();
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileRitual(this);
    }
}
