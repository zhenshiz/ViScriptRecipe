package com.viscript_recipe.compat.enderio.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.enderio.EnderIoRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EnderIoRecipeData implements IVSRecipeData {
    @Persisted private List<EnderIoIngredientData> inputs = new ArrayList<>();
    @Persisted private List<EnderIoOutputData> outputs = new ArrayList<>();
    @Persisted private EnderIoFluidData fluidInput = new EnderIoFluidData();
    @Persisted private FluidStack fluidOutput = new FluidStack(Fluids.WATER, 1000);
    @Persisted private int energy = 2000;
    @Persisted private float experience;
    @Persisted private boolean smelting;
    @Persisted private int experienceLevels = 1;
    @Persisted private String bonus = "multiply_output";
    @Persisted private String tankMode = "fill";
    @Persisted private int ticks = 200;
    @Persisted private String weather = "clear";
    @Persisted private ResourceLocation enchantment = ResourceLocation.parse("minecraft:sharpness");
    @Persisted private int costMultiplier = 1;
    @Persisted private String entityType = "minecraft:zombie";
    @Persisted private String mobCategory = "";
    @Persisted private String soulData = "";
    @Persisted private boolean copyInputComponents;
    @Persisted private List<ResourceLocation> bases = new ArrayList<>(List.of(ResourceLocation.parse("minecraft:bedrock")));
    @Persisted private List<ResourceLocation> baseTags = new ArrayList<>();
    @Persisted private List<ResourceLocation> dimensions = new ArrayList<>(List.of(ResourceLocation.parse("minecraft:overworld")));
    @Persisted private String blockAfterBurning = "";
    @Persisted private int width = 3;
    @Persisted private int height = 3;
    @Persisted private String group = "";
    @Persisted private CraftingBookCategory category = CraftingBookCategory.MISC;
    @Persisted private Boolean showNotification;

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        var type = typeId.getPath();
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        fluidInput = new EnderIoFluidData();
        fluidOutput = new FluidStack(Fluids.WATER, 1000);
        energy = 2000;
        experience = 0;
        smelting = false;
        experienceLevels = 1;
        bonus = "multiply_output";
        tankMode = "fill";
        ticks = 200;
        weather = "clear";
        enchantment = ResourceLocation.parse("minecraft:sharpness");
        costMultiplier = 1;
        entityType = "minecraft:zombie";
        mobCategory = "";
        soulData = "";
        copyInputComponents = false;
        bases = new ArrayList<>(List.of(ResourceLocation.parse("minecraft:bedrock")));
        baseTags = new ArrayList<>();
        dimensions = new ArrayList<>(List.of(ResourceLocation.parse("minecraft:overworld")));
        blockAfterBurning = "";
        width = 3;
        height = 3;
        group = "";
        category = CraftingBookCategory.MISC;
        showNotification = type.equals("shaped_entity_storage") ? true : null;
        int count = switch (type) { case "alloy_smelting" -> 3; case "slicing" -> 6; case "shaped_entity_storage" -> 9; case "vat_fermenting" -> 2; case "fire_crafting", "weather_change" -> 0; default -> 1; };
        for (int i = 0; i < count; i++) inputs.add(EnderIoIngredientData.of(RecipeIngredient.item(Items.IRON_INGOT)));
        if (!type.equals("weather_change") && !type.equals("vat_fermenting") && !type.equals("enchanting")) outputs.add(new EnderIoOutputData());
        if (type.equals("vat_fermenting")) for (int i = 0; i < 2; i++) inputs.set(i, EnderIoIngredientData.of(RecipeIngredient.tag(ResourceLocation.parse(i == 0 ? "c:crops" : "c:seeds"))));
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) { return EnderIoRecipeFactory.compile(type.getPath(), this); }
}
