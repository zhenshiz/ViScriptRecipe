package com.viscript_recipe.compat.irons_spellbooks.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IronAlchemistCauldronRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.WATER_BUCKET);
    @Persisted
    private ItemStack result = new ItemStack(Items.BUCKET);
    @Persisted
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private FluidStack baseFluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private List<FluidStack> resultFluids = new ArrayList<>(List.of(new FluidStack(Fluids.WATER, 1000)));
    @Persisted
    private ItemStack byproduct = ItemStack.EMPTY;
    @Persisted
    private boolean mustFitAll = true;
    @Persisted
    private ResourceLocation sound = ResourceLocation.withDefaultNamespace("item.bucket.empty");

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW) ?
                IronSpellbooksRecipeFactory.compileBrew(this) :
                typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL) ?
                IronSpellbooksRecipeFactory.compileFill(this) : IronSpellbooksRecipeFactory.compileEmpty(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL)) {
            setMustFitAll(false);
        } else if (typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY)) {
            setInput(RecipeIngredient.item(Items.GLASS_BOTTLE))
                    .setResult(new ItemStack(Items.POTION))
                    .setFluid(new FluidStack(Fluids.WATER, 250))
                    .setSound(ResourceLocation.withDefaultNamespace("item.bottle.fill"));
        } else if (typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW)) {
            setInput(RecipeIngredient.item(Items.GOLD_INGOT));
        }
    }
}
