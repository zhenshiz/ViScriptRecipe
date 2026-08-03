package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauPedestalOnlyRecipeData implements IPersistedSerializable, IConfigurable {
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
}
