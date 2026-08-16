package com.viscript_recipe.data;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.ars_nouveau.data.*;
import com.viscript_recipe.compat.avaritia.data.AvaritiaCompressorRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaExtremeSmithingRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaSpecialShapelessRecipeData;
import com.viscript_recipe.compat.avaritia.data.AvaritiaTableRecipeData;
import com.viscript_recipe.compat.cataclysm.data.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.compat.cataclysm.data.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.compat.create.data.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.compat.create.data.CreateProcessingRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.compat.create.data.CreateSequencedAssemblyStepData;
import com.viscript_recipe.compat.extendedcrafting.data.*;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.compat.goety.data.*;
import com.viscript_recipe.compat.iceandfire.data.DragonForgeRecipeData;
import com.viscript_recipe.compat.industrial_foregoing.data.*;
import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.compat.irons_spellbooks.data.IronArcaneAnvilRecipeData;
import com.viscript_recipe.compat.irons_spellbooks.data.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.*;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalIngredientData;
import com.viscript_recipe.compat.mekanism.data.MekanismChemicalStackData;
import com.viscript_recipe.compat.mekanism.data.MekanismRecipeData;
import com.viscript_recipe.compat.mysticalagriculture.data.*;
import com.viscript_recipe.compat.spore.data.SporeGraftingRecipeData;
import com.viscript_recipe.compat.spore.data.SporeSurgeryRecipeData;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.vanilla.*;

public final class RecipeDataAccessors {

    @ViScriptRegisterAccessors
    public static void register(RegisterAccessorEvent event) {
        registerBase(event);
        registerVanilla(event);
        registerIronSpellbooks(event);
        registerIceAndFire(event);
        registerSpore(event);
        registerFarmersDelight(event);
        registerCreate(event);
        registerExtendedCrafting(event);
        registerArsNouveau(event);
        registerKaleidoscopeCookery(event);
        registerAvaritia(event);
        registerCataclysm(event);
        registerTouhouLittleMaid(event);
        registerGoety(event);
        registerMysticalAgriculture(event);
        registerIndustrialForegoing(event);
        registerMekanism(event);
    }

    private static void registerBase(RegisterAccessorEvent event) {
        event.register(RecipeFile.class, RecipeFile::new);
        event.register(RecipeEntry.class, RecipeEntry::new);
        event.register(RecipeIngredient.class, RecipeIngredient::of);
        event.register(FluidIngredientData.class, FluidIngredientData::of);
        event.register(RecipeOutputData.class, RecipeOutputData::of);
    }

    private static void registerVanilla(RegisterAccessorEvent event) {
        event.register(CraftingRemainderRule.class, CraftingRemainderRule::new);
        event.register(ShapedKeyEntry.class, ShapedKeyEntry::new);
        event.register(ShapedCraftingRecipeData.class, ShapedCraftingRecipeData::new);
        event.register(ShapelessCraftingRecipeData.class, ShapelessCraftingRecipeData::new);
        event.register(CookingRecipeData.class, CookingRecipeData::new);
        event.register(StonecuttingRecipeData.class, StonecuttingRecipeData::new);
        event.register(SmithingTransformRecipeData.class, SmithingTransformRecipeData::new);
    }

    private static void registerIronSpellbooks(RegisterAccessorEvent event) {
        event.register(IronArcaneAnvilRecipeData.class, IronArcaneAnvilRecipeData::new);
        event.register(IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new);
        event.register(IronNoAdditionSmithingRecipeData.class, IronNoAdditionSmithingRecipeData::new);
    }

    private static void registerIceAndFire(RegisterAccessorEvent event) {
        event.register(DragonForgeRecipeData.class, DragonForgeRecipeData::new);
    }

    private static void registerSpore(RegisterAccessorEvent event) {
        event.register(SporeSurgeryRecipeData.class, SporeSurgeryRecipeData::new);
        event.register(SporeGraftingRecipeData.class, SporeGraftingRecipeData::new);
    }

    private static void registerFarmersDelight(RegisterAccessorEvent event) {
        event.register(FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new);
        event.register(FarmerCuttingRecipeData.class, FarmerCuttingRecipeData::new);
    }

    private static void registerCreate(RegisterAccessorEvent event) {
        event.register(CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new);
        event.register(CreateProcessingRecipeData.class, CreateProcessingRecipeData::new);
        event.register(CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new);
        event.register(CreateSequencedAssemblyStepData.class, CreateSequencedAssemblyStepData::new);
    }

    private static void registerExtendedCrafting(RegisterAccessorEvent event) {
        event.register(ExtendedCraftingCombinationRecipeData.class, ExtendedCraftingCombinationRecipeData::new);
        event.register(ExtendedCraftingCompressorRecipeData.class, ExtendedCraftingCompressorRecipeData::new);
        event.register(ExtendedCraftingEnderCrafterRecipeData.class, ExtendedCraftingEnderCrafterRecipeData::new);
        event.register(ExtendedCraftingFluxCrafterRecipeData.class, ExtendedCraftingFluxCrafterRecipeData::new);
        event.register(ExtendedCraftingTableRecipeData.class, ExtendedCraftingTableRecipeData::new);
        event.register(ExtendedCraftingUltimateSingularityRecipeData.class, ExtendedCraftingUltimateSingularityRecipeData::new);
    }

    private static void registerArsNouveau(RegisterAccessorEvent event) {
        event.register(ArsNouveauApparatusRecipeData.class, ArsNouveauApparatusRecipeData::new);
        event.register(ArsNouveauArmorUpgradeRecipeData.class, ArsNouveauArmorUpgradeRecipeData::new);
        event.register(ArsNouveauCrushOutputData.class, ArsNouveauCrushOutputData::new);
        event.register(ArsNouveauCrushRecipeData.class, ArsNouveauCrushRecipeData::new);
        event.register(ArsNouveauEnchantmentRecipeData.class, ArsNouveauEnchantmentRecipeData::new);
        event.register(ArsNouveauGlyphRecipeData.class, ArsNouveauGlyphRecipeData::new);
        event.register(ArsNouveauImbuementRecipeData.class, ArsNouveauImbuementRecipeData::new);
        event.register(ArsNouveauPedestalOnlyRecipeData.class, ArsNouveauPedestalOnlyRecipeData::new);
    }

    private static void registerKaleidoscopeCookery(RegisterAccessorEvent event) {
        event.register(KaleidoscopePotRecipeData.class, KaleidoscopePotRecipeData::new);
        event.register(KaleidoscopeStockpotRecipeData.class, KaleidoscopeStockpotRecipeData::new);
        event.register(KaleidoscopeMillstoneRecipeData.class, KaleidoscopeMillstoneRecipeData::new);
        event.register(KaleidoscopeChoppingBoardRecipeData.class, KaleidoscopeChoppingBoardRecipeData::new);
        event.register(KaleidoscopeSteamerRecipeData.class, KaleidoscopeSteamerRecipeData::new);
        event.register(KaleidoscopeTeapotRecipeData.class, KaleidoscopeTeapotRecipeData::new);
    }

    private static void registerAvaritia(RegisterAccessorEvent event) {
        event.register(AvaritiaSpecialShapelessRecipeData.class, AvaritiaSpecialShapelessRecipeData::new);
        event.register(AvaritiaCompressorRecipeData.class, AvaritiaCompressorRecipeData::new);
        event.register(AvaritiaExtremeSmithingRecipeData.class, AvaritiaExtremeSmithingRecipeData::new);
        event.register(AvaritiaTableRecipeData.class, AvaritiaTableRecipeData::new);
    }

    private static void registerCataclysm(RegisterAccessorEvent event) {
        event.register(CataclysmWeaponFusionRecipeData.class, CataclysmWeaponFusionRecipeData::new);
        event.register(CataclysmAmethystBlessRecipeData.class, CataclysmAmethystBlessRecipeData::new);
    }

    private static void registerTouhouLittleMaid(RegisterAccessorEvent event) {
        event.register(TouhouLittleMaidAltarRecipeData.class, TouhouLittleMaidAltarRecipeData::new);
    }

    private static void registerGoety(RegisterAccessorEvent event) {
        event.register(GoetyCursedInfuserRecipeData.class, GoetyCursedInfuserRecipeData::new);
        event.register(GoetyRitualRecipeData.class, GoetyRitualRecipeData::new);
        event.register(GoetyBrazierRecipeData.class, GoetyBrazierRecipeData::new);
        event.register(GoetyPulverizeRecipeData.class, GoetyPulverizeRecipeData::new);
        event.register(GoetyBrewingRecipeData.class, GoetyBrewingRecipeData::new);
    }

    private static void registerMysticalAgriculture(RegisterAccessorEvent event) {
        event.register(MysticalAgricultureInfusionRecipeData.class, MysticalAgricultureInfusionRecipeData::new);
        event.register(MysticalAgricultureAwakeningRecipeData.class, MysticalAgricultureAwakeningRecipeData::new);
        event.register(MysticalAgricultureEnchanterRecipeData.class, MysticalAgricultureEnchanterRecipeData::new);
        event.register(MysticalAgricultureReprocessorRecipeData.class, MysticalAgricultureReprocessorRecipeData::new);
        event.register(MysticalAgricultureSoulExtractionRecipeData.class, MysticalAgricultureSoulExtractionRecipeData::new);
        event.register(MysticalAgricultureWeightedEntityData.class, MysticalAgricultureWeightedEntityData::new);
        event.register(MysticalAgricultureSouliumSpawnerRecipeData.class, MysticalAgricultureSouliumSpawnerRecipeData::new);
    }

    private static void registerIndustrialForegoing(RegisterAccessorEvent event) {
        event.register(IndustrialEntityConditionData.class, IndustrialEntityConditionData::new);
        event.register(IndustrialBlockStatePropertyData.class, IndustrialBlockStatePropertyData::new);
        event.register(IndustrialLaserDrillRarityData.class, IndustrialLaserDrillRarityData::new);
        event.register(IndustrialCrusherRecipeData.class, IndustrialCrusherRecipeData::new);
        event.register(IndustrialDissolutionRecipeData.class, IndustrialDissolutionRecipeData::new);
        event.register(IndustrialFluidExtractorRecipeData.class, IndustrialFluidExtractorRecipeData::new);
        event.register(IndustrialLaserDrillOreRecipeData.class, IndustrialLaserDrillOreRecipeData::new);
        event.register(IndustrialLaserDrillFluidRecipeData.class, IndustrialLaserDrillFluidRecipeData::new);
        event.register(IndustrialStoneWorkRecipeData.class, IndustrialStoneWorkRecipeData::new);
    }

    private static void registerMekanism(RegisterAccessorEvent event) {
        event.register(MekanismChemicalIngredientData.class, MekanismChemicalIngredientData::new);
        event.register(MekanismChemicalStackData.class, MekanismChemicalStackData::new);
        event.register(MekanismRecipeData.class, MekanismRecipeData::new);
    }
}
