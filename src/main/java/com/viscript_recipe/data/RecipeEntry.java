package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.ars_nouveau.*;
import com.viscript_recipe.data.avaritia.*;
import com.viscript_recipe.data.cataclysm.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.create.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.create.CreateProcessingRecipeData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.data.confluence.ConfluenceRecipeData;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.farmersdelight.FarmerCookingPotRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.data.goety.*;
import com.viscript_recipe.data.iceandfire.DragonForgeRecipeData;
import com.viscript_recipe.data.industrial_foregoing.*;
import com.viscript_recipe.data.alloy_smelter.AlloySmelterRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.irons_spellbooks.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.*;
import com.viscript_recipe.data.mekanism.MekanismRecipeData;
import com.viscript_recipe.data.mysticalagriculture.*;
import com.viscript_recipe.data.spore.SporeGraftingRecipeData;
import com.viscript_recipe.data.spore.SporeSurgeryRecipeData;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.vanilla.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("unchecked")
public class RecipeEntry implements IPersistedSerializable, IConfigurable {
    private final HashMap<Class<? extends IVSRecipeData>, IVSRecipeData> recipeData = new HashMap<>();

    @Configurable(name = "viscript_recipe.config.entry.enabled")
    private boolean enabled = true;

    @Configurable(name = "viscript_recipe.config.entry.operation")
    @ConfigSelector(candidate = {"add", "replace", "remove"})
    private RecipeOperation operation = RecipeOperation.REPLACE;

    @Configurable(name = "viscript_recipe.config.entry.recipe_id")
    private ResourceLocation recipeId = ViScriptRecipe.id("example");

    @Configurable(name = "viscript_recipe.config.entry.type")
    private ResourceLocation type = RecipeEditorTypes.CRAFTING_SHAPED;

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        var tag = IPersistedSerializable.super.serializeNBT(provider);
        tag.put(getData().getDataName(), getData().serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        IPersistedSerializable.super.deserializeNBT(provider, tag);
        getData().deserializeNBT(provider, tag);
    }

    public <T extends IVSRecipeData> Class<T> getDataClass() {
        return (Class<T>) RecipeEditorTypes.require(getType()).dataClass();
    }

    public <T extends IVSRecipeData> T getData() {
        var clazz = getDataClass();
        if (!recipeData.containsKey(clazz)) recipeData.put(clazz, RecipeEditorTypes.require(getType()).dataSupplier().get());
        return (T) recipeData.get(clazz);
    }

    public RecipeEntry setData(IVSRecipeData data) {
        var dataClass = getDataClass();
        if (data != null && data.getClass().equals(dataClass)) recipeData.put(dataClass, data);
        return this;
    }

    public ShapedCraftingRecipeData getShaped() {return getData();}
    public ShapelessCraftingRecipeData getShapeless() {return getData();}
    public CookingRecipeData getCooking() {return getData();}
    public StonecuttingRecipeData getStonecutting() {return getData();}
    public SmithingTransformRecipeData getSmithingTransform() {return getData();}

    public IronAlchemistCauldronRecipeData getIronAlchemistCauldron() {return getData();}
    public IronArcaneAnvilRecipeData getIronArcaneAnvil() {return getData();}
    public IronNoAdditionSmithingRecipeData getIronNoAdditionSmithing() {return getData();}

    public DragonForgeRecipeData getIceAndFireDragonForge() {return getData();}

    public SporeSurgeryRecipeData getSporeSurgery() {return getData();}
    public SporeGraftingRecipeData getSporeGrafting() {return getData();}

    public FarmerCookingPotRecipeData getFarmerCookingPot() {return getData();}
    public FarmerCuttingRecipeData getFarmerCuttingBoard() {return getData();}

    public CreateProcessingRecipeData getCreateProcessing() {return getData();}
    public CreateMechanicalCraftingRecipeData getCreateMechanicalCrafting() {return getData();}
    public CreateSequencedAssemblyRecipeData getCreateSequencedAssembly() {return getData();}

    public ExtendedCraftingTableRecipeData getExtendedCraftingTable() {return getData();}
    public ExtendedCraftingEnderCrafterRecipeData getExtendedCraftingEnderCrafter() {return getData();}
    public ExtendedCraftingFluxCrafterRecipeData getExtendedCraftingFluxCrafter() {return getData();}
    public ExtendedCraftingCombinationRecipeData getExtendedCraftingCombination() {return getData();}
    public ExtendedCraftingCompressorRecipeData getExtendedCraftingCompressor() {return getData();}
    public ExtendedCraftingUltimateSingularityRecipeData getExtendedCraftingUltimateSingularity() {return getData();}

    public ArsNouveauApparatusRecipeData getArsNouveauApparatus() {return getData();}
    public ArsNouveauArmorUpgradeRecipeData getArsNouveauArmorUpgrade() {return getData();}
    public ArsNouveauEnchantmentRecipeData getArsNouveauEnchantment() {return getData();}
    public ArsNouveauImbuementRecipeData getArsNouveauImbuement() {return getData();}
    public ArsNouveauGlyphRecipeData getArsNouveauGlyph() {return getData();}
    public ArsNouveauCrushRecipeData getArsNouveauCrush() {return getData();}
    public ArsNouveauPedestalOnlyRecipeData getArsNouveauPedestalOnly() {return getData();}

    public KaleidoscopePotRecipeData getKaleidoscopePot() {return getData();}
    public KaleidoscopeStockpotRecipeData getKaleidoscopeStockpot() {return getData();}
    public KaleidoscopeMillstoneRecipeData getKaleidoscopeMillstone() {return getData();}
    public KaleidoscopeChoppingBoardRecipeData getKaleidoscopeChoppingBoard() {return getData();}
    public KaleidoscopeSteamerRecipeData getKaleidoscopeSteamer() {return getData();}
    public KaleidoscopeTeapotRecipeData getKaleidoscopeTeapot() {return getData();}

    public AvaritiaTableRecipeData getAvaritiaTable() {return getData();}
    public AvaritiaCompressorRecipeData getAvaritiaCompressor() {return getData();}
    public AvaritiaExtremeSmithingRecipeData getAvaritiaExtremeSmithing() {return getData();}
    public AvaritiaInfinityCatalystRecipeData getAvaritiaInfinityCatalyst() {return getData();}
    public AvaritiaEternalSingularityRecipeData getAvaritiaEternalSingularity() {return getData();}
    public AvaritiaFullMatterClusterRecipeData getAvaritiaFullMatterCluster() {return getData();}

    public CataclysmWeaponFusionRecipeData getCataclysmWeaponFusion() {return getData();}
    public CataclysmAmethystBlessRecipeData getCataclysmAmethystBless() {return getData();}

    public TouhouLittleMaidAltarRecipeData getTouhouLittleMaidAltar() {return getData();}

    public GoetyCursedInfuserRecipeData getGoetyCursedInfuser() {return getData();}
    public GoetyRitualRecipeData getGoetyRitual() {return getData();}
    public GoetyBrazierRecipeData getGoetyBrazier() {return getData();}
    public GoetyPulverizeRecipeData getGoetyPulverize() {return getData();}
    public GoetyBrewingRecipeData getGoetyBrewing() {return getData();}

    public MysticalAgricultureInfusionRecipeData getMysticalAgricultureInfusion() {return getData();}
    public MysticalAgricultureAwakeningRecipeData getMysticalAgricultureAwakening() {return getData();}
    public MysticalAgricultureEnchanterRecipeData getMysticalAgricultureEnchanter() {return getData();}
    public MysticalAgricultureReprocessorRecipeData getMysticalAgricultureReprocessor() {return getData();}
    public MysticalAgricultureSoulExtractionRecipeData getMysticalAgricultureSoulExtraction() {return getData();}
    public MysticalAgricultureSouliumSpawnerRecipeData getMysticalAgricultureSouliumSpawner() {return getData();}

    public IndustrialDissolutionRecipeData getIndustrialDissolution() {return getData();}
    public IndustrialCrusherRecipeData getIndustrialCrusher() {return getData();}
    public IndustrialFluidExtractorRecipeData getIndustrialFluidExtractor() {return getData();}
    public IndustrialLaserDrillOreRecipeData getIndustrialLaserDrillOre() {return getData();}
    public IndustrialLaserDrillFluidRecipeData getIndustrialLaserDrillFluid() {return getData();}
    public IndustrialStoneWorkRecipeData getIndustrialStoneWork() {return getData();}

    public AlloySmelterRecipeData getAlloySmelter() {return getData();}

    public MekanismRecipeData getMekanism() {return getData();}
    public ConfluenceRecipeData getConfluence() {return getData();}

    public Recipe<?> compile() {return getData().compile(getType());}

    public ResourceLocation getType() {
        return type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
    }

    public RecipeEntry setType(ResourceLocation type) {
        this.type = type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
        return this;
    }

    public boolean isType(ResourceLocation type) {
        return getType().equals(type);
    }
}
