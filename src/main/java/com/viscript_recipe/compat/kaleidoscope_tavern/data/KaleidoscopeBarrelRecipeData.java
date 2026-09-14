package com.viscript_recipe.compat.kaleidoscope_tavern.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_tavern.KaleidoscopeTavernRecipeFactory;
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
public class KaleidoscopeBarrelRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ResourceLocation fluid = ResourceLocation.withDefaultNamespace("water");
    @Persisted
    private RecipeIngredient carrier = RecipeIngredient.item(Items.GLASS_BOTTLE);
    @Persisted
    private ItemStack result = new ItemStack(Items.POTION);
    @Persisted
    private int unitTime = 2400;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeTavernRecipeFactory.compileBarrel(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredients(new ArrayList<>(List.of(
                RecipeIngredient.item(itemFromRegistry("kaleidoscope_tavern:grape", Items.SWEET_BERRIES))
        ))).setFluid(ResourceLocation.fromNamespaceAndPath("kaleidoscope_tavern", "grape_juice"))
                .setResult(new ItemStack(itemFromRegistry("kaleidoscope_tavern:wine", Items.POTION)));
    }
}
