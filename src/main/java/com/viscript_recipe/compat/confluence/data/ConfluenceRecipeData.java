package com.viscript_recipe.compat.confluence.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.confluence.ConfluenceRecipeEditorTypes;
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

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.STONE)));
    @Persisted
    private ItemStack result = new ItemStack(Items.COBBLESTONE);
    @Persisted
    private List<ItemStack> targets = new ArrayList<>(List.of(new ItemStack(Items.COBBLESTONE)));
    @Persisted
    private ConfluenceCraftingMode craftingMode = ConfluenceCraftingMode.SHAPED;
    @Persisted
    private int width = 1;
    @Persisted
    private int height = 1;
    @Persisted
    private ConfluenceEnvironmentData environment = new ConfluenceEnvironmentData();
    @Persisted
    private RecipeIngredient container = RecipeIngredient.item(Items.BOWL);
    @Persisted
    private ConfluenceHeatSourceData heatSource = new ConfluenceHeatSourceData();
    @Persisted
    private float experience;
    @Persisted
    private int cookingTime = 100;
    @Persisted
    private boolean requiresFuel;
    @Persisted
    private int shrink = 1;
    @Persisted
    private ConfluenceGamePhase gamePhase = ConfluenceGamePhase.BEFORE_SKELETRON;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ConfluenceRecipeFactory.compile(type, this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setTargets(new ArrayList<>());
        if (ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION.equals(typeId)) {
            setTargets(new ArrayList<>(List.of(new ItemStack(Items.COBBLESTONE))));
        } else if (ConfluenceRecipeEditorTypes.FLETCHING_TABLE.equals(typeId)) {
            setIngredients(new ArrayList<>(List.of(
                    RecipeIngredient.item(Items.FEATHER),
                    RecipeIngredient.item(Items.STICK),
                    RecipeIngredient.item(Items.FLINT)
            ))).setResult(new ItemStack(Items.ARROW, 4));
        } else if (ConfluenceRecipeEditorTypes.ALCHEMY_TABLE.equals(typeId)) {
            setIngredients(new ArrayList<>(List.of(
                    RecipeIngredient.item(Items.POTION),
                    RecipeIngredient.item(Items.NETHER_WART)
            ))).setResult(new ItemStack(Items.POTION));
        } else if (ConfluenceRecipeEditorTypes.COOKING_POT.equals(typeId)) {
            setIngredients(new ArrayList<>(List.of(
                    RecipeIngredient.item(Items.BEEF),
                    RecipeIngredient.item(Items.CARROT)
            ))).setResult(new ItemStack(Items.RABBIT_STEW)).setCookingTime(200);
            getHeatSource().setBlocks(new ConfluenceHolderSetData()
                    .setKind(ConfluenceHolderSetKind.IDS)
                    .setValues(new ArrayList<>(List.of(ResourceLocation.withDefaultNamespace("campfire")))));
        } else if (ConfluenceRecipeEditorTypes.SOLIDIFIER.equals(typeId) || ConfluenceRecipeEditorTypes.isEitherType(typeId)) {
            setWidth(1).setHeight(1).setCraftingMode(ConfluenceCraftingMode.SHAPED);
        }
    }
}
