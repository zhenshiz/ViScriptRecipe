package com.viscript_recipe.data.confluence;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.confluence.ConfluenceRecipeFactory;
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
public class ConfluenceRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 16;
    public static final int MAX_TRANSMUTATION_RESULTS = 16;

    @Configurable(name = "viscript_recipe.config.confluence.ingredients")
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<ConfluenceIngredientData> ingredients = new ArrayList<>(List.of(
            new ConfluenceIngredientData().setIngredient(RecipeIngredient.item(Items.STONE))
    ));

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.confluence.transmutation.targets")
    @ConfigList(addDefaultMethod = "createDefaultTarget")
    private List<ItemStack> targets = new ArrayList<>(List.of(new ItemStack(Items.COBBLESTONE)));

    @Configurable(name = "viscript_recipe.config.confluence.crafting_mode")
    @ConfigSelector(candidate = {"shaped", "shapeless"})
    private ConfluenceCraftingMode craftingMode = ConfluenceCraftingMode.SHAPED;

    @Configurable(name = "viscript_recipe.config.confluence.width")
    private int width = 1;

    @Configurable(name = "viscript_recipe.config.confluence.height")
    private int height = 1;

    @Configurable(name = "viscript_recipe.config.confluence.environment", subConfigurable = true)
    private ConfluenceEnvironmentData environment = new ConfluenceEnvironmentData();

    @Configurable(name = "viscript_recipe.config.confluence.container", subConfigurable = true)
    private RecipeIngredient container = RecipeIngredient.item(Items.BOWL);

    @Configurable(name = "viscript_recipe.config.confluence.heat_source", subConfigurable = true)
    private ConfluenceHeatSourceData heatSource = new ConfluenceHeatSourceData();

    @Configurable(name = "viscript_recipe.config.cooking.experience")
    private float experience;

    @Configurable(name = "viscript_recipe.config.cooking.cooking_time")
    private int cookingTime = 100;

    @Configurable(name = "viscript_recipe.config.confluence.requires_fuel")
    private boolean requiresFuel;

    @Configurable(name = "viscript_recipe.config.confluence.transmutation.shrink")
    private int shrink = 1;

    @Configurable(name = "viscript_recipe.config.confluence.transmutation.game_phase")
    @ConfigSelector(candidate = {"before_skeletron", "after_skeletron", "wall_of_flesh", "mechanical_bosses", "plantera", "golem", "moon_lord"})
    private ConfluenceGamePhase gamePhase = ConfluenceGamePhase.BEFORE_SKELETRON;

    public ConfluenceIngredientData createDefaultIngredient() {
        return new ConfluenceIngredientData();
    }

    public ItemStack createDefaultTarget() {
        return new ItemStack(Items.COBBLESTONE);
    }

    public ConfluenceIngredientData ingredient(int index) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        while (ingredients.size() <= Math.max(0, index)) {
            ingredients.add(new ConfluenceIngredientData()
                    .setIngredient(new RecipeIngredient()));
        }
        var value = ingredients.get(Math.max(0, index));
        if (value == null) {
            value = new ConfluenceIngredientData().setIngredient(new RecipeIngredient());
            ingredients.set(Math.max(0, index), value);
        }
        return value;
    }

    public ItemStack target(int index) {
        if (targets == null || index < 0 || index >= targets.size() || targets.get(index) == null) {
            return ItemStack.EMPTY;
        }
        return targets.get(index);
    }

    public ConfluenceRecipeData setTarget(int index, ItemStack stack) {
        if (targets == null) {
            targets = new ArrayList<>();
        }
        while (targets.size() <= Math.max(0, index)) {
            targets.add(ItemStack.EMPTY);
        }
        targets.set(Math.max(0, index), stack == null ? ItemStack.EMPTY : stack.copy());
        while (!targets.isEmpty() && targets.getLast().isEmpty()) {
            targets.removeLast();
        }
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ConfluenceRecipeFactory.compile(type, this);
    }
}
