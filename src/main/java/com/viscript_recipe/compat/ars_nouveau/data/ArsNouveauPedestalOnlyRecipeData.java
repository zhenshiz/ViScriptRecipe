package com.viscript_recipe.compat.ars_nouveau.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeEditorTypes;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.RecipeHelper;
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
public class ArsNouveauPedestalOnlyRecipeData implements IVSRecipeData, IPreview {
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.LAPIS_BLOCK),
            RecipeIngredient.item(Items.PAPER)
    ));
    @Persisted
    private int sourceCost = 3000;

    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compilePedestalOnly(type, this);
    }

    @Override
    public ItemStack centerPreview() {
        return RecipeHelper.registryItem("ars_nouveau:pedestal_only", Items.PAPER);
    }

    @Override
    public ItemStack outputPreview() {
        /*selectedEntry.isType(ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT)
                    ? new ItemStack(Items.ENCHANTED_BOOK)
                    : new ItemStack(itemFromRegistry("ars_nouveau:spell_parchment", Items.PAPER));*/
        return RecipeHelper.registryItem("ars_nouveau:spell_parchment", Items.PAPER);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setPedestalItems(new ArrayList<>(List.of(
                RecipeIngredient.item(RecipeHelper.itemFromRegistry("ars_nouveau:spell_parchment", Items.PAPER)),
                RecipeIngredient.item(RecipeHelper.itemFromRegistry("ars_nouveau:source_gem", Items.AMETHYST_SHARD))
        ))).setSourceCost(typeId.equals(ArsNouveauRecipeEditorTypes.PRESTIDIGITATION) ? 0 : 3000);
    }
}
