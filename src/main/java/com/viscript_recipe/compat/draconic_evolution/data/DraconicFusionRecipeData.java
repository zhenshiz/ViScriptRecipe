package com.viscript_recipe.compat.draconic_evolution.data;

import com.brandon3055.brandonscore.api.TechLevel;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.draconic_evolution.DraconicEvolutionRecipeFactory;
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
public class DraconicFusionRecipeData implements IVSRecipeData {
    @Persisted
    private DraconicIngredientData catalyst = DraconicIngredientData.of(RecipeIngredient.item(Items.DIAMOND));
    @Persisted
    private List<DraconicFusionIngredientData> injectors = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);
    @Persisted
    private long totalEnergy = 1_000_000;
    @Persisted
    private TechLevel techLevel = TechLevel.DRACONIUM;

    @Override
    public Recipe<?> compile(ResourceLocation recipeId, ResourceLocation typeId) {
        return DraconicEvolutionRecipeFactory.compile(recipeId, this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        catalyst = DraconicIngredientData.of(RecipeIngredient.item(Items.DIAMOND));
        injectors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            injectors.add(new DraconicFusionIngredientData().setIngredient(DraconicIngredientData.of(
                    RecipeIngredient.item(itemFromRegistry("draconicevolution:draconium_ingot", Items.IRON_INGOT)))));
        }
        result = new ItemStack(itemFromRegistry("draconicevolution:draconium_core", Items.NETHER_STAR));
    }
}
