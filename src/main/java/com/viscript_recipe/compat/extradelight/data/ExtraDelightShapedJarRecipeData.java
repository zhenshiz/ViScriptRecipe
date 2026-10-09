package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.recipe.ShapedWithJarRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

/** 保留罐中流体消耗行为的有序合成配方。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightShapedJarRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private CraftingBookCategory category = CraftingBookCategory.MISC;
    @Persisted private Boolean showNotification = true;
    @Persisted private List<String> pattern = new ArrayList<>(List.of("AB"));
    @Persisted private List<ShapedKeyEntry> key = new ArrayList<>();
    @Persisted private List<FluidStack> fluids = new ArrayList<>();
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var values = new LinkedHashMap<Character, Ingredient>();
        for (var entry : key) values.put(entry.compileSymbol(), entry.compileIngredient());
        return new ShapedWithJarRecipe(group, category, ShapedRecipePattern.of(values, pattern),
                fluids.stream().map(FluidStack::copy).toList(), requiredItem(result), showNotification);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        key = new ArrayList<>(List.of(ShapedKeyEntry.of('A', RecipeIngredient.item(Items.WHEAT)),
                ShapedKeyEntry.of('B', RecipeIngredient.item(item("jar")))));
        fluids = new ArrayList<>(List.of(fluidStack("oil_fluid", 250)));
    }
}
