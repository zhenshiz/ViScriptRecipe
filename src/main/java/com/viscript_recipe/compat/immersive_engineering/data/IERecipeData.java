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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;

@Getter
@Setter
@Accessors(chain = true)
public class IERecipeData implements IVSRecipeData {
    @Persisted private ArrayList<RecipeIngredient> inputs = new ArrayList<>();
    /**
     * 电弧炉输出索引：0–3 和 9–10 为主要产物，4 为炉渣，5–8 为概率产物。
     * 第五、第六个主要产物追加在末尾，以兼容旧项目的槽位索引。
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

    public IERecipeData() {
        inputs.add(RecipeIngredient.item(Items.IRON_INGOT));
        outputs.add(RecipeOutputData.of(Items.GOLD_INGOT.getDefaultInstance()));
    }

    @Override public String getDataName() { return "immersiveEngineering"; }

    @Override public void applyDefaultData(ResourceLocation typeId) {
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
        switch (typeId.getPath()) {
            case "alloy", "metal_press" -> inputs.add(RecipeIngredient.item(Items.IRON_INGOT));
            case "cloche" -> {
                inputs.set(0, RecipeIngredient.item(Items.WHEAT_SEEDS));
                inputs.add(RecipeIngredient.item(Items.DIRT));
                outputs.set(0, RecipeOutputData.of(Items.WHEAT.getDefaultInstance()));
                fluidInputs.add(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)));
            }
            case "refinery", "mixer", "bottling_machine" -> {
                fluidInputs.add(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)));
                if (!typeId.getPath().equals("bottling_machine")) fluidOutput = new FluidStack(Fluids.WATER, 1000);
            }
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return IERecipeFactory.compile(typeId, this);
    }
}
