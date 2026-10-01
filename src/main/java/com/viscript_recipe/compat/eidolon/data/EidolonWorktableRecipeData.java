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
public class EidolonWorktableRecipeData implements IVSRecipeData {
    @Persisted
    private int width = 3;
    @Persisted
    private int height = 3;
    @Persisted
    private List<EidolonIngredientData> core = emptySlots(9);
    @Persisted
    private List<EidolonIngredientData> reagents = emptySlots(4);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND);

    public EidolonWorktableRecipeData() {
        core.set(4, EidolonIngredientData.of(RecipeIngredient.item(Items.DIAMOND)));
        reagents.set(0, EidolonIngredientData.of(RecipeIngredient.item(Items.GOLD_INGOT)));
    }

    public static List<EidolonIngredientData> emptySlots(int count) {
        var slots = new ArrayList<EidolonIngredientData>();
        for (int i = 0; i < count; i++) slots.add(new EidolonIngredientData());
        return slots;
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return EidolonRecipeFactory.worktable(this); }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        core.set(4, EidolonIngredientData.of(RecipeIngredient.item(Items.DIAMOND)));
        reagents.set(0, EidolonIngredientData.of(RecipeIngredient.item(Items.GOLD_INGOT)));
    }
}
