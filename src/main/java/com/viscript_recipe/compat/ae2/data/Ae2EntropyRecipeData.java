package com.viscript_recipe.compat.ae2.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ae2.Ae2RecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import java.util.ArrayList;
import java.util.List;

/** Stores editable AE2 Entropy recipe data independently of AE2's runtime classes. */
@Getter
@Setter
@Accessors(chain = true)
public class Ae2EntropyRecipeData implements IVSRecipeData {
    @Persisted
    private boolean heat = true;
    @Persisted
    private boolean inputBlockEnabled = true;
    @Persisted
    private ResourceLocation inputBlock = ResourceLocation.withDefaultNamespace("cobblestone");
    @Persisted
    private boolean inputFluidEnabled = false;
    @Persisted
    private ResourceLocation inputFluid = ResourceLocation.withDefaultNamespace("water");
    @Persisted
    private boolean outputBlockEnabled = true;
    @Persisted
    private ResourceLocation outputBlock = ResourceLocation.withDefaultNamespace("stone");
    @Persisted
    private boolean outputFluidEnabled = false;
    @Persisted
    private ResourceLocation outputFluid = ResourceLocation.withDefaultNamespace("empty");
    @Persisted
    private boolean keepBlockProperties = false;
    @Persisted
    private boolean keepFluidProperties = false;
    @Persisted
    private List<Ae2StatePropertyData> inputBlockProperties = new ArrayList<>();
    @Persisted
    private List<Ae2StatePropertyData> inputFluidProperties = new ArrayList<>();
    @Persisted
    private List<Ae2StatePropertyData> outputBlockProperties = new ArrayList<>();
    @Persisted
    private List<Ae2StatePropertyData> outputFluidProperties = new ArrayList<>();
    @Persisted
    private List<ItemStack> drops = new ArrayList<>();

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return Ae2RecipeFactory.compile(this);
    }
}
