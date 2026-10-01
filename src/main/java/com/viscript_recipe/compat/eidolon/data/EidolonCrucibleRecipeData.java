package com.viscript_recipe.compat.eidolon.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.eidolon.EidolonRecipeFactory;
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
public class EidolonCrucibleRecipeData implements IVSRecipeData {
    @Persisted
    private List<EidolonCrucibleStepData> steps = new ArrayList<>(List.of(new EidolonCrucibleStepData()
            .setItems(new ArrayList<>(List.of(EidolonIngredientData.of(RecipeIngredient.item(Items.GOLD_NUGGET)))))));
    @Persisted
    private ItemStack result = new ItemStack(Items.GOLD_INGOT);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return EidolonRecipeFactory.crucible(this); }
}
