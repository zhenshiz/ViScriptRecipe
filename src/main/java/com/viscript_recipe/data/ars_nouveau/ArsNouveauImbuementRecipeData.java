package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauImbuementRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.imbuement.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.LAPIS_LAZULI);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int source = 500;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.AMETHYST_SHARD);
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileImbuement(this);
    }
}
