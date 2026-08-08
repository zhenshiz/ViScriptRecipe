package com.viscript_recipe.data.alloy_smelter;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.alloy_smelter.AlloySmelterRecipeFactory;
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

/** Editor-owned representation of Alloy Smelter's single native smelting recipe type. */
@Getter
@Setter
@Accessors(chain = true)
public class AlloySmelterRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 5;

    @Configurable(name = "viscript_recipe.config.alloy_smelter.materials")
    @ConfigList(addDefaultMethod = "createDefaultMaterial")
    private List<AlloySmelterMaterialData> materials = new ArrayList<>(List.of(
            new AlloySmelterMaterialData().setIngredient(RecipeIngredient.item(Items.RAW_IRON))
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.IRON_INGOT);

    @Configurable(name = "viscript_recipe.config.alloy_smelter.smelting_time")
    private int smeltingTime = 200;

    @Configurable(name = "viscript_recipe.config.alloy_smelter.fuel_per_tick")
    private int fuelPerTick = 1;

    @Configurable(name = "viscript_recipe.config.alloy_smelter.required_tier")
    private int requiredTier = 1;

    public AlloySmelterMaterialData createDefaultMaterial() {
        return new AlloySmelterMaterialData();
    }

    public AlloySmelterMaterialData material(int index) {
        if (materials == null) {
            materials = new ArrayList<>();
        }
        var normalized = Math.clamp(index, 0, MAX_INPUTS - 1);
        while (materials.size() <= normalized) {
            materials.add(new AlloySmelterMaterialData());
        }
        var material = materials.get(normalized);
        if (material == null) {
            material = new AlloySmelterMaterialData();
            materials.set(normalized, material);
        }
        return material;
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return AlloySmelterRecipeFactory.compile(this);
    }
}
