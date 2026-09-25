package com.viscript_recipe.compat.kaleidoscope_cookery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class KaleidoscopeBambooTrayRecipeData implements IVSRecipeData {
    public static final String SUBTYPE_WETTING = "wetting";
    public static final String SUBTYPE_DRYING = "drying";
    public static final int DEFAULT_DURATION = 1200;

    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.CLAY);
    @Persisted
    private ItemStack result = new ItemStack(Items.CLAY);
    @Persisted
    private String subtype = SUBTYPE_WETTING;
    @Persisted
    private int duration = DEFAULT_DURATION;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileBambooTray(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredient(RecipeIngredient.item(Items.CLAY))
                .setResult(new ItemStack(Items.MUD))
                .setSubtype(SUBTYPE_WETTING)
                .setDuration(DEFAULT_DURATION);
    }
}
