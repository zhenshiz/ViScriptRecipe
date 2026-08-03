package com.viscript_recipe.data.ars_nouveau;

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

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauCrushRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.COBBLESTONE);
    @Persisted
    private List<ArsNouveauCrushOutputData> outputs = new ArrayList<>(List.of(new ArsNouveauCrushOutputData()));
    @Persisted
    private boolean skipBlockPlace;

    @Override
    public ItemStack getResult() {return ArsNouveauRecipeEditorTypes.firstCrushOutput(this);}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        ArsNouveauRecipeEditorTypes.setFirstCrushOutput(this, result);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileCrush(this);
    }
}
