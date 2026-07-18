package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.vanilla.ViscriptShapedRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ShapedCraftingRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.recipe.show_notification")
    private Boolean showNotification = true;

    @Configurable(name = "viscript_recipe.config.shaped.pattern")
    @ConfigList(addDefaultMethod = "createDefaultPatternRow")
    private List<String> pattern = new ArrayList<>(List.of("A"));

    @Configurable(name = "viscript_recipe.config.shaped.key")
    @ConfigList(addDefaultMethod = "createDefaultKey")
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.OAK_PLANKS))
    ));

    @Configurable(name = "viscript_recipe.config.shaped.remainders")
    @ConfigList(addDefaultMethod = "createDefaultRemainder")
    private List<CraftingRemainderRule> remainders = new ArrayList<>(List.of(CraftingRemainderRule.defaultRule()));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    public String createDefaultPatternRow() {
        return "A";
    }

    public ShapedKeyEntry createDefaultKey() {
        return new ShapedKeyEntry();
    }

    public CraftingRemainderRule createDefaultRemainder() {
        return CraftingRemainderRule.defaultRule();
    }

    @Override
    public String getDataName() {return "shaped";}

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Shaped recipe pattern cannot be empty");
        }
        var compiledKey = new LinkedHashMap<Character, net.minecraft.world.item.crafting.Ingredient>();
        for (var entry : key) {
            compiledKey.put(entry.compileSymbol(), entry.compileIngredient());
        }
        var compiledPattern = ShapedRecipePattern.of(compiledKey, pattern);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        var compiledRemainders = remainders == null ? List.<CraftingRemainderRule>of() : remainders;
        if (compiledRemainders.stream().anyMatch(rule -> !rule.isDefault())) {
            return new ViscriptShapedRecipe("", CraftingBookCategory.MISC, compiledPattern, result.copy(), showNotification, compiledRemainders);
        }
        return new ShapedRecipe("", CraftingBookCategory.MISC, compiledPattern, result.copy(), showNotification);
    }
}
