package com.viscript_recipe.compat.eidolon.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.eidolon.EidolonRecipeEditorTypes;
import com.viscript_recipe.compat.eidolon.EidolonRecipeFactory;
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
public class EidolonRitualRecipeData implements IVSRecipeData {
    @Persisted
    private EidolonIngredientData reagent = EidolonIngredientData.of(RecipeIngredient.item(Items.CHARCOAL));
    @Persisted
    private List<EidolonIngredientData> pedestals = new ArrayList<>(List.of(EidolonIngredientData.of(RecipeIngredient.item(Items.GOLD_INGOT))));
    @Persisted
    private List<EidolonIngredientData> foci = new ArrayList<>();
    @Persisted
    private List<EidolonIngredientData> invariants = new ArrayList<>();
    @Persisted
    private float healthRequirement;
    @Persisted
    private ResourceLocation ritual = EidolonRecipeEditorTypes.id("repelling");
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND);
    @Persisted
    private boolean keepReagentComponents;
    @Persisted
    private ResourceLocation entity = ResourceLocation.withDefaultNamespace("zombie");
    @Persisted
    private int summonCount = 1;
    @Persisted
    private ResourceLocation structureTag = EidolonRecipeEditorTypes.id("catacombs");
    @Persisted
    private List<String> commands = new ArrayList<>(List.of("say Ritual complete"));
    @Persisted
    private ResourceLocation symbol = EidolonRecipeEditorTypes.id("particle/sanguine_ritual");
    @Persisted
    private int color = 0xFFFF3355;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return EidolonRecipeFactory.ritual(this, typeId); }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        pedestals = new ArrayList<>(List.of(EidolonIngredientData.of(RecipeIngredient.item(Items.GOLD_INGOT))));
    }
}
