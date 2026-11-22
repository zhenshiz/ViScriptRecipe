package com.viscript_recipe.compat.forbidden_arcanus.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.stal111.forbidden_arcanus.common.block.entity.forge.TierPredicate;
import com.stal111.forbidden_arcanus.common.block.entity.forge.circle.MagicCircleType;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssencesDefinition;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualInput;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualRequirements;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.CreateItemResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.RitualResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.TransmuteInputResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.UpgradeTierResult;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues;
import com.viscript_recipe.compat.forbidden_arcanus.ForgeRitualRecipe;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.HEPHAESTUS_UPGRADING;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues.requireIngredient;

@Getter
@Setter
@Accessors(chain = true)
public final class ForgeRitualRecipeData implements IVSRecipeData {
    public static final int PEDESTAL_COUNT = 8;
    public static final int MAX_TIER = 5;

    @Persisted private List<NativeIngredientData> pedestals = emptyPedestals();
    @Persisted private NativeIngredientData center = new NativeIngredientData().setValue(RecipeIngredient.item(Items.DIAMOND));
    @Persisted private ItemStack result = new ItemStack(Items.NETHER_STAR);
    @Persisted private boolean transmuteInput;
    @Persisted private int forgeTier = 1;
    @Persisted private boolean exactTier;
    @Persisted private int resultTier = 2;
    @Persisted private int duration = Ritual.DEFAULT_DURATION;
    @Persisted private int aureal;
    @Persisted private int souls;
    @Persisted private int blood;
    @Persisted private int experience;
    @Persisted private String magicCircle = "forbidden_arcanus:create_item";
    @Persisted private List<String> enhancers = new ArrayList<>();
    @Persisted private String enhancerTag = "";

    private static ArrayList<NativeIngredientData> emptyPedestals() {
        var slots = new ArrayList<NativeIngredientData>(PEDESTAL_COUNT);
        for (int i = 0; i < PEDESTAL_COUNT; i++) slots.add(new NativeIngredientData());
        return slots;
    }

    public NativeIngredientData pedestal(int index) {return pedestals.get(index);}

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        boolean upgrading = HEPHAESTUS_UPGRADING.equals(typeId);
        magicCircle = upgrading ? "forbidden_arcanus:upgrade_tier" : "forbidden_arcanus:create_item";
        exactTier = upgrading;
        pedestals = emptyPedestals();
        pedestals.getFirst().setValue(RecipeIngredient.item(Items.IRON_INGOT));
    }

    @Override
    public ForgeRitualRecipe compile(ResourceLocation typeId) {return compile(typeId, Platform.getFrozenRegistry());}

    @Override
    public ForgeRitualRecipe compile(ResourceLocation typeId, HolderLookup.Provider provider) {
        var inputs = pedestals.stream().map(slot -> slot.compile(provider)).filter(ingredient -> !ingredient.isEmpty())
                .map(ingredient -> new RitualInput(ingredient, 1)).toList();
        if (inputs.isEmpty() || inputs.size() > PEDESTAL_COUNT || forgeTier < 1 || forgeTier > MAX_TIER || duration < 1
                || aureal < 0 || souls < 0 || blood < 0 || experience < 0) {
            throw new IllegalArgumentException("Invalid forge ritual pedestal count, tier, essence cost or duration");
        }
        var requirements = new RitualRequirements(EssencesDefinition.of(aureal, souls, blood, experience),
                new TierPredicate(forgeTier, exactTier), compileEnhancers(provider));
        return new ForgeRitualRecipe(new Ritual(inputs, requireIngredient(center, provider), compileResult(typeId), requirements,
                ForbiddenArcanusRecipeValues.decode(MagicCircleType.CODEC, provider, magicCircle), duration));
    }

    private HolderSet<EnhancerDefinition> compileEnhancers(HolderLookup.Provider provider) {
        if (!enhancerTag.isBlank()) {
            return provider.lookupOrThrow(FARegistries.ENHANCER_DEFINITION)
                    .getOrThrow(TagKey.create(FARegistries.ENHANCER_DEFINITION, ResourceLocation.parse(enhancerTag)));
        }
        return HolderSet.direct(enhancers.stream().filter(value -> !value.isBlank()).distinct()
                .map(value -> ForbiddenArcanusRecipeValues.decode(EnhancerDefinition.REFERENCE_CODEC, provider, value)).toList());
    }

    private RitualResult compileResult(ResourceLocation typeId) {
        if (HEPHAESTUS_UPGRADING.equals(typeId)) {
            if (resultTier < 1 || resultTier > MAX_TIER) throw new IllegalArgumentException("Invalid forge result tier");
            return new UpgradeTierResult(resultTier);
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Missing forge ritual output");
        return transmuteInput ? new TransmuteInputResult(result.getItemHolder()) : new CreateItemResult(result.copy());
    }
}
