package com.viscript_recipe.compat.immersive_engineering.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.immersive_engineering.IERecipeFactory;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;

/** Stores the item, fluid, and machine parameters shared by IE's JEI recipe categories. */
@Getter
@Setter
@Accessors(chain = true)
public class IERecipeData implements IVSRecipeData {
    @Persisted private ArrayList<RecipeIngredient> inputs = new ArrayList<>();
    /**
     * Stores output slots in editor order. For arc furnaces, indices 0–3 and 9–10 are main
     * products, 4 is slag, and 5–8 are chance products. The original indices remain stable
     * when loading projects written before support for the fifth and sixth main products.
     */
    @Persisted private ArrayList<RecipeOutputData> outputs = new ArrayList<>();
    @Persisted private ArrayList<FluidIngredientData> fluidInputs = new ArrayList<>();
    @Persisted private ArrayList<String> fluidInputCodecs = new ArrayList<>();
    @Persisted private FluidStack fluidOutput = FluidStack.EMPTY;
    @Persisted private int time = 200;
    @Persisted private int energy = 1600;
    @Persisted private int creosote = 500;
    @Persisted private float growthModifier = 1.5f;
    @Persisted private String blueprintCategory = "components";
    @Persisted private String clocheRender = "{\"type\":\"immersiveengineering:generic\",\"block\":\"minecraft:wheat\"}";
    @Persisted private boolean recycling = false;
    @Persisted private String arcSpecialType = "";
    @Persisted private ArrayList<Double> recyclingAmounts = new ArrayList<>();
    @Persisted private ResourceLocation currentEditorType;

    public IERecipeData() {
        inputs.add(RecipeIngredient.item(Items.IRON_INGOT));
        outputs.add(RecipeOutputData.of(Items.GOLD_INGOT.getDefaultInstance()));
    }

    @Override public String getDataName() { return "immersiveEngineering"; }

    @Override public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(currentEditorType)) return;
        currentEditorType = typeId;
        inputs = new ArrayList<>();
        inputs.add(RecipeIngredient.item(Items.IRON_INGOT));
        outputs = new ArrayList<>();
        outputs.add(RecipeOutputData.of(Items.GOLD_INGOT.getDefaultInstance()));
        fluidInputs = new ArrayList<>();
        fluidInputCodecs = new ArrayList<>();
        fluidOutput = FluidStack.EMPTY;
        recyclingAmounts = new ArrayList<>();
        recycling = false;
        arcSpecialType = "";
        time = 200;
        energy = 1600;
        creosote = 500;
        growthModifier = 1.5f;
        blueprintCategory = "components";
        clocheRender = "{\"type\":\"immersiveengineering:generic\",\"block\":\"minecraft:wheat\"}";
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return IERecipeFactory.compile(typeId, this);
    }
}
