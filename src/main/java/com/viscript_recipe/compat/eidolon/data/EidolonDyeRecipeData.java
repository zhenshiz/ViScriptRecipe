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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class EidolonDyeRecipeData implements IVSRecipeData {
    @Persisted
    private List<EidolonIngredientData> inputs = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.LEATHER_CHESTPLATE);
    @Persisted
    private String group = "";
    @Persisted
    private CraftingBookCategory category = CraftingBookCategory.MISC;

    public EidolonDyeRecipeData() { applyDefaultData(null); }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return EidolonRecipeFactory.dye(this); }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        var item = itemFromRegistry("eidolon_repraised:warlock_cloak", Items.LEATHER_CHESTPLATE);
        inputs = new ArrayList<>(List.of(EidolonIngredientData.of(RecipeIngredient.tag("c:dyes")),
                EidolonIngredientData.of(RecipeIngredient.item(item))));
        result = new ItemStack(item);
    }
}
