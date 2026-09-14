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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauApparatusRecipeData implements IVSRecipeData, IPreview {
    @Persisted
    private RecipeIngredient reagent = RecipeIngredient.item(Items.DIAMOND);
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.GOLD_INGOT),
            RecipeIngredient.item(Items.AMETHYST_SHARD)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
    @Persisted
    private int sourceCost;
    @Persisted
    private boolean keepNbtOfReagent;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileApparatus(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setReagent(RecipeIngredient.item(itemFromRegistry("ars_nouveau:source_gem", Items.DIAMOND)))
                .setPedestalItems(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("ars_nouveau:archwood_planks", Items.OAK_PLANKS)),
                        RecipeIngredient.item(itemFromRegistry("ars_nouveau:source_gem", Items.AMETHYST_SHARD))
                )))
                .setResult(new ItemStack(itemFromRegistry("ars_nouveau:jar_of_light", Items.LANTERN)));
    }
}
