package com.viscript_recipe.gui.editor;

import com.viscript_recipe.data.*;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauCrushOutputData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauRecipeEditorTypes;
import com.viscript_recipe.data.avaritia.AvaritiaCompressorRecipeData;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.data.avaritia.AvaritiaTableRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmRecipeEditorTypes;
import com.viscript_recipe.data.create.*;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.farmersdelight.FarmerCuttingRecipeData;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyRitualCraftType;
import com.viscript_recipe.data.iceandfire.IceAndFireRecipeEditorTypes;
import com.viscript_recipe.data.industrial_foregoing.IndustrialForegoingRecipeEditorTypes;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeCookeryRecipeEditorTypes;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientData;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientKind;
import com.viscript_recipe.data.mekanism.MekanismChemicalStackData;
import com.viscript_recipe.data.mekanism.MekanismRecipeKind;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureCountedIngredientData;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureRecipeEditorTypes;
import com.viscript_recipe.data.mysticalagriculture.MysticalAgricultureWeightedEntityData;
import com.viscript_recipe.data.spore.SporeRecipeEditorTypes;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.recipe.RecipeHelper.fluidFromRegistry;
import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

final class RecipeDefaultDataInitializer {
    private RecipeDefaultDataInitializer() {
    }

    static void apply(RecipeEntry entry, ResourceLocation type) {
        var mekanismKind = MekanismRecipeKind.byType(type).orElse(null);
        if (mekanismKind != null) {
            applyMekanism(entry, mekanismKind);
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.CRUSHER)) {
            entry.getIndustrialCrusher()
                    .setInput(RecipeIngredient.item(Items.COBBLESTONE))
                    .setOutput(RecipeIngredient.item(Items.GRAVEL));
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.DISSOLUTION_CHAMBER)) {
            entry.getIndustrialDissolution()
                    .setInput(new ArrayList<>(List.of(RecipeIngredient.item(Items.IRON_INGOT))))
                    .setInputFluid(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)))
                    .setProcessingTime(300)
                    .setHasItemOutput(true)
                    .setOutput(new ItemStack(Items.DIAMOND));
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.FLUID_EXTRACTOR)) {
            entry.getIndustrialFluidExtractor()
                    .setInput(RecipeIngredient.item(Items.OAK_LOG))
                    .setResultBlock(ResourceLocation.withDefaultNamespace("stripped_oak_log"))
                    .setBreakChance(0.01F)
                    .setOutput(new FluidStack(fluidFromRegistry("industrialforegoing:latex", Fluids.WATER), 4));
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.LASER_DRILL_ORE)) {
            entry.getIndustrialLaserDrillOre()
                    .setCatalyst(RecipeIngredient.item(itemFromRegistry("industrialforegoing:white_laser_lens", Items.WHITE_STAINED_GLASS_PANE)))
                    .setOutput(RecipeIngredient.item(Items.DIAMOND_ORE))
                    .setOutputCount(1);
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.LASER_DRILL_FLUID)) {
            entry.getIndustrialLaserDrillFluid()
                    .setCatalyst(RecipeIngredient.item(itemFromRegistry("industrialforegoing:red_laser_lens", Items.RED_STAINED_GLASS_PANE)))
                    .setOutput(FluidIngredientData.fluid(new FluidStack(Fluids.LAVA, 100)));
        } else if (type.equals(IndustrialForegoingRecipeEditorTypes.STONEWORK_GENERATE)) {
            entry.getIndustrialStoneWork().setOutput(new ItemStack(Items.COBBLESTONE))
                    .setWaterNeed(1000).setLavaNeed(1000).setWaterConsume(0).setLavaConsume(0);
        } else if (type.equals(RecipeEditorTypes.BLASTING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.SMOKING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.CAMPFIRE_COOKING)) {
            entry.getCooking().setCookingTime(600);
        } else if (type.equals(RecipeEditorTypes.SMELTING)) {
            entry.getCooking().setCookingTime(200);
        } else if (type.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL)) {
            entry.getIronAlchemistCauldron()
                    .setInput(RecipeIngredient.item(Items.WATER_BUCKET))
                    .setResult(new ItemStack(Items.BUCKET))
                    .setFluid(new FluidStack(Fluids.WATER, 1000))
                    .setMustFitAll(false)
                    .setSound(ResourceLocation.withDefaultNamespace("item.bucket.empty"));
        } else if (type.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY)) {
            entry.getIronAlchemistCauldron()
                    .setInput(RecipeIngredient.item(Items.GLASS_BOTTLE))
                    .setResult(new ItemStack(Items.POTION))
                    .setFluid(new FluidStack(Fluids.WATER, 250))
                    .setSound(ResourceLocation.withDefaultNamespace("item.bottle.fill"));
        } else if (type.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW)) {
            var data = entry.getIronAlchemistCauldron()
                    .setInput(RecipeIngredient.item(Items.GOLD_INGOT))
                    .setBaseFluid(new FluidStack(Fluids.WATER, 1000))
                    .setByproduct(ItemStack.EMPTY);
            data.setFirstResultFluid(new FluidStack(Fluids.WATER, 1000));
        } else if (type.equals(IronSpellbooksRecipeEditorTypes.ARCANE_ANVIL_TRANSFORM)) {
            entry.getIronArcaneAnvil()
                    .setInput(RecipeIngredient.item(Items.IRON_SWORD))
                    .setMaterial(RecipeIngredient.item(Items.AMETHYST_SHARD))
                    .setResult(new ItemStack(Items.DIAMOND_SWORD));
        } else if (type.equals(IronSpellbooksRecipeEditorTypes.SMITHING_TRANSFORM_NO_ADDITION)) {
            entry.getIronNoAdditionSmithing()
                    .setTemplate(RecipeIngredient.item(Items.GOLD_INGOT))
                    .setBase(RecipeIngredient.item(Items.IRON_SWORD))
                    .setResult(new ItemStack(Items.GOLDEN_SWORD));
        } else if (type.equals(IceAndFireRecipeEditorTypes.DRAGONFORGE)) {
            entry.getIceAndFireDragonForge()
                    .setDragonType("fire")
                    .setCookTime(1000)
                    .setInput(RecipeIngredient.item(Items.IRON_INGOT))
                    .setBlood(RecipeIngredient.item(itemFromRegistry("iceandfire:fire_dragon_blood", Items.GLASS_BOTTLE)))
                    .setResult(new ItemStack(itemFromRegistry("iceandfire:dragonsteel_fire_ingot", Items.IRON_INGOT)));
        } else if (type.equals(SporeRecipeEditorTypes.SURGERY)) {
            entry.getSporeSurgery()
                    .setIngredient(2, RecipeIngredient.item(itemFromRegistry("spore:mutated_fiber", Items.STRING)))
                    .setResult(new ItemStack(itemFromRegistry("spore:knife", Items.IRON_SWORD)));
        } else if (type.equals(SporeRecipeEditorTypes.GRAFTING)) {
            entry.getSporeGrafting()
                    .setIngredient(0, RecipeIngredient.item(itemFromRegistry("spore:inf_helmet", Items.IRON_HELMET)))
                    .setIngredient(1, RecipeIngredient.item(itemFromRegistry("spore:brain_remnants", Items.ROTTEN_FLESH)))
                    .setIngredient(2, RecipeIngredient.item(itemFromRegistry("spore:respirator", Items.LEATHER_HELMET)))
                    .setResult(new ItemStack(itemFromRegistry("spore:inf_up_helmet", Items.NETHERITE_HELMET)));
        } else if (type.equals(CataclysmRecipeEditorTypes.WEAPON_FUSION)) {
            entry.getCataclysmWeaponFusion()
                    .setBase(RecipeIngredient.item(itemFromRegistry("cataclysm:infernal_forge", Items.IRON_SWORD)))
                    .setAddition(RecipeIngredient.item(itemFromRegistry("cataclysm:void_core", Items.AMETHYST_SHARD)))
                    .setResult(new ItemStack(itemFromRegistry("cataclysm:void_forge", Items.DIAMOND_SWORD)));
        } else if (type.equals(CataclysmRecipeEditorTypes.AMETHYST_BLESS)) {
            entry.getCataclysmAmethystBless()
                    .setIngredient(RecipeIngredient.item(itemFromRegistry("cataclysm:amethyst_crab_meat", Items.COOKED_COD)))
                    .setResult(new ItemStack(itemFromRegistry("cataclysm:blessed_amethyst_crab_meat", Items.GOLDEN_CARROT)))
                    .setTime(120);
        } else if (type.equals(TouhouLittleMaidRecipeEditorTypes.ALTAR_RECIPE)) {
            entry.getTouhouLittleMaidAltar()
                    .setIngredient(0, RecipeIngredient.item(Items.HAY_BLOCK))
                    .setIngredient(1, RecipeIngredient.item(Items.HAY_BLOCK))
                    .setIngredient(2, RecipeIngredient.item(Items.HAY_BLOCK))
                    .setIngredient(3, tagIngredient("c:rods/wooden"))
                    .setIngredient(4, tagIngredient("c:rods/wooden"))
                    .setIngredient(5, RecipeIngredient.item(Items.ENDER_EYE))
                    .setPower(0.2F)
                    .setResult(new ItemStack(itemFromRegistry("touhou_little_maid:broom", Items.STICK)));
        } else if (type.equals(GoetyRecipeEditorTypes.CURSED_INFUSER_RECIPE)) {
            entry.getGoetyCursedInfuser()
                    .setIngredient(RecipeIngredient.item(Items.EMERALD_BLOCK))
                    .setResult(new ItemStack(itemFromRegistry("goety:awakened_emerald_block", Items.DIAMOND_BLOCK)))
                    .setCookingTime(540)
                    .setGrim(false);
        } else if (type.equals(GoetyRecipeEditorTypes.RITUAL)) {
            entry.getGoetyRitual()
                    .setActivationItem(RecipeIngredient.item(itemFromRegistry("goety:dark_gem", Items.BOOK)))
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.IRON_INGOT),
                            RecipeIngredient.item(Items.COAL),
                            RecipeIngredient.item(itemFromRegistry("goety:cursed_ingot", Items.GOLD_INGOT))
                    )))
                    .setResult(new ItemStack(itemFromRegistry("goety:dark_ingot", Items.NETHERITE_INGOT)))
                    .setCraftType(GoetyRitualCraftType.FORGE)
                    .setRitualType(GoetyRecipeEditorTypes.goety("craft"))
                    .setSoulCost(5)
                    .setDuration(30);
        } else if (type.equals(GoetyRecipeEditorTypes.BRAZIER)) {
            entry.getGoetyBrazier()
                    .setIngredient(0, RecipeIngredient.item(Items.COAL))
                    .setIngredient(1, RecipeIngredient.item(Items.SCULK))
                    .setIngredient(2, RecipeIngredient.item(itemFromRegistry("goety:cursed_ingot", Items.IRON_INGOT)))
                    .setResult(new ItemStack(itemFromRegistry("goety:dark_ingot", Items.NETHERITE_INGOT)))
                    .setSoulCost(500);
        } else if (type.equals(GoetyRecipeEditorTypes.PULVERIZE)) {
            entry.getGoetyPulverize()
                    .setIngredient(RecipeIngredient.item(Items.STONE))
                    .setItemResult(new ItemStack(Items.COBBLESTONE));
        } else if (type.equals(GoetyRecipeEditorTypes.BREWING)) {
            entry.getGoetyBrewing()
                    .setIngredient(RecipeIngredient.item(Items.SPIDER_EYE))
                    .setEffect(ResourceLocation.withDefaultNamespace("poison"))
                    .setSoulCost(25)
                    .setCapacityExtra(1)
                    .setDuration(600);
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.INFUSION)) {
            entry.getMysticalAgricultureInfusion()
                    .setInput(RecipeIngredient.item(Items.DIAMOND))
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(itemFromRegistry("mysticalagriculture:inferium_essence", Items.REDSTONE)),
                            RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_shard", Items.AMETHYST_SHARD))
                    )))
                    .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:prosperity_gemstone", Items.EMERALD)))
                    .setTransferComponents(false);
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.AWAKENING)) {
            var awakening = entry.getMysticalAgricultureAwakening()
                    .setInput(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:supremium_essence", Items.NETHER_STAR)))
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_gemstone", Items.EMERALD)),
                            RecipeIngredient.item(Items.NETHER_STAR),
                            RecipeIngredient.item(Items.DRAGON_BREATH),
                            RecipeIngredient.item(Items.END_CRYSTAL)
                    )))
                    .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:awakened_supremium_essence", Items.NETHER_STAR)))
                    .setTransferComponents(false);
            awakening.setEssence(0, new ItemStack(itemFromRegistry("mysticalagriculture:air_essence", Items.FEATHER)));
            awakening.setEssence(1, new ItemStack(itemFromRegistry("mysticalagriculture:earth_essence", Items.DIRT)));
            awakening.setEssence(2, new ItemStack(itemFromRegistry("mysticalagriculture:water_essence", Items.WATER_BUCKET)));
            awakening.setEssence(3, new ItemStack(itemFromRegistry("mysticalagriculture:fire_essence", Items.BLAZE_POWDER)));
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.ENCHANTER)) {
            entry.getMysticalAgricultureEnchanter()
                    .setIngredients(new ArrayList<>(List.of(
                            new MysticalAgricultureCountedIngredientData()
                                    .setIngredient(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:experience_droplet", Items.EXPERIENCE_BOTTLE)))
                                    .setCount(8),
                            new MysticalAgricultureCountedIngredientData()
                                    .setIngredient(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_shard", Items.LAPIS_LAZULI)))
                                    .setCount(2)
                    )))
                    .setEnchantment(ResourceLocation.withDefaultNamespace("sharpness"));
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.REPROCESSOR)) {
            entry.getMysticalAgricultureReprocessor()
                    .setInput(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:inferium_seeds", Items.WHEAT_SEEDS)))
                    .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:inferium_essence", Items.WHEAT)));
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.SOUL_EXTRACTION)) {
            entry.getMysticalAgricultureSoulExtraction()
                    .setInput(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:corrupted_essence", Items.ROTTEN_FLESH)))
                    .setSoulType(ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "zombie"))
                    .setSouls(1.0D);
        } else if (type.equals(MysticalAgricultureRecipeEditorTypes.SOULIUM_SPAWNER)) {
            entry.getMysticalAgricultureSouliumSpawner()
                    .setInput(new MysticalAgricultureCountedIngredientData()
                            .setIngredient(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:soulium_ingot", Items.ROTTEN_FLESH)))
                            .setCount(4))
                    .setEntities(new ArrayList<>(List.of(new MysticalAgricultureWeightedEntityData()
                            .setEntity(ResourceLocation.withDefaultNamespace("zombie"))
                            .setWeight(1))));
        } else if (type.equals(FarmersDelightRecipeEditorTypes.COOKING)) {
            entry.getFarmerCookingPot()
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.BEEF),
                            RecipeIngredient.item(Items.CARROT),
                            RecipeIngredient.item(Items.POTATO)
                    )))
                    .setResult(new ItemStack(itemFromRegistry("farmersdelight:beef_stew", Items.RABBIT_STEW)))
                    .setContainer(new ItemStack(Items.BOWL))
                    .setExperience(1.0F)
                    .setCookingTime(200);
        } else if (type.equals(FarmersDelightRecipeEditorTypes.CUTTING)) {
            entry.getFarmerCuttingBoard()
                    .setInput(RecipeIngredient.item(Items.BEEF))
                    .setTool(FarmerCuttingRecipeData.defaultKnifeTool())
                    .setResults(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(itemFromRegistry("farmersdelight:minced_beef", Items.BEEF))))))
                    .setCustomSound(false)
                    .setSound(ResourceLocation.withDefaultNamespace("item.axe.strip"));
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.POT)) {
            entry.getKaleidoscopePot()
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.EGG),
                            RecipeIngredient.item(Items.EGG),
                            RecipeIngredient.item(itemFromRegistry("kaleidoscope_cookery:tomato", Items.CARROT))
                    )))
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:scramble_egg_with_tomatoes", Items.BAKED_POTATO)))
                    .setCarrier(RecipeIngredient.item(Items.BOWL))
                    .setTime(200)
                    .setStirFryCount(3);
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.STOCKPOT)) {
            entry.getKaleidoscopeStockpot()
                    .setIngredients(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.BEEF),
                            RecipeIngredient.item(Items.CARROT),
                            RecipeIngredient.item(Items.POTATO)
                    )))
                    .setSoupBase(ResourceLocation.withDefaultNamespace("water"))
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:tomato_beef_brisket_soup", Items.RABBIT_STEW)))
                    .setTime(300)
                    .setCarrier(RecipeIngredient.item(Items.BOWL));
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.MILLSTONE)) {
            entry.getKaleidoscopeMillstone()
                    .setIngredient(RecipeIngredient.item(Items.WHEAT))
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:flour", Items.BONE_MEAL)));
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.CHOPPING_BOARD)) {
            entry.getKaleidoscopeChoppingBoard()
                    .setIngredient(RecipeIngredient.item(Items.MUTTON))
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:raw_lamb_chops", Items.MUTTON)))
                    .setCutCount(4)
                    .setModelId(KaleidoscopeCookeryRecipeEditorTypes.kaleidoscope("raw_lamb_chops"));
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.STEAMER)) {
            entry.getKaleidoscopeSteamer()
                    .setIngredient(RecipeIngredient.item(Items.WHEAT))
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:mantou", Items.BREAD)))
                    .setCookTick(1200);
        } else if (type.equals(KaleidoscopeCookeryRecipeEditorTypes.TEAPOT)) {
            entry.getKaleidoscopeTeapot()
                    .setTeaFluid(ResourceLocation.withDefaultNamespace("water"))
                    .setIngredient(RecipeIngredient.item(Items.WHEAT_SEEDS))
                    .setIngredientCount(12)
                    .setTime(240)
                    .setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:barley_tea", Items.POTION)));
        } else if (type.equals(ArsNouveauRecipeEditorTypes.APPARATUS)) {
            entry.getArsNouveauApparatus()
                    .setReagent(RecipeIngredient.item(itemFromRegistry("ars_nouveau:source_gem", Items.DIAMOND)))
                    .setPedestalItems(new ArrayList<>(List.of(
                            RecipeIngredient.item(itemFromRegistry("ars_nouveau:archwood_planks", Items.OAK_PLANKS)),
                            RecipeIngredient.item(itemFromRegistry("ars_nouveau:source_gem", Items.AMETHYST_SHARD))
                    )))
                    .setResult(new ItemStack(itemFromRegistry("ars_nouveau:jar_of_light", Items.LANTERN)))
                    .setSourceCost(0)
                    .setKeepNbtOfReagent(false);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.ARMOR_UPGRADE)) {
            entry.getArsNouveauArmorUpgrade()
                    .setPedestalItems(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.BLAZE_ROD),
                            RecipeIngredient.item(Items.BLAZE_ROD)
                    )))
                    .setSourceCost(2500)
                    .setTier(1);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.ENCHANTMENT)) {
            entry.getArsNouveauEnchantment()
                    .setPedestalItems(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.LAPIS_BLOCK),
                            RecipeIngredient.item(Items.AMETHYST_SHARD)
                    )))
                    .setEnchantment(ResourceLocation.withDefaultNamespace("sharpness"))
                    .setLevel(1)
                    .setSourceCost(1000);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.IMBUEMENT)) {
            entry.getArsNouveauImbuement()
                    .setInput(RecipeIngredient.item(Items.LAPIS_LAZULI))
                    .setPedestalItems(new ArrayList<>())
                    .setResult(new ItemStack(itemFromRegistry("ars_nouveau:source_gem", Items.AMETHYST_SHARD)))
                    .setSource(500);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.GLYPH)) {
            entry.getArsNouveauGlyph()
                    .setInputs(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.LANTERN),
                            RecipeIngredient.item(Items.TORCH)
                    )))
                    .setResult(new ItemStack(itemFromRegistry("ars_nouveau:glyph_light", Items.PAPER)))
                    .setExp(27);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.CRUSH)) {
            entry.getArsNouveauCrush()
                    .setInput(RecipeIngredient.item(Items.COBBLESTONE))
                    .setOutputs(new ArrayList<>(List.of(new ArsNouveauCrushOutputData()
                            .setItem(new ItemStack(Items.GRAVEL))
                            .setChance(1.0F)
                            .setMaxRange(1))))
                    .setSkipBlockPlace(false);
        } else if (type.equals(ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT)
                || type.equals(ArsNouveauRecipeEditorTypes.SPELL_WRITE)
                || type.equals(ArsNouveauRecipeEditorTypes.PRESTIDIGITATION)) {
            entry.getArsNouveauPedestalOnly()
                    .setPedestalItems(new ArrayList<>(List.of(
                            RecipeIngredient.item(itemFromRegistry("ars_nouveau:spell_parchment", Items.PAPER)),
                            RecipeIngredient.item(itemFromRegistry("ars_nouveau:source_gem", Items.AMETHYST_SHARD))
                    )))
                    .setSourceCost(type.equals(ArsNouveauRecipeEditorTypes.PRESTIDIGITATION) ? 0 : 3000);
        } else if (type.equals(RecipeEditorTypes.CREATE_MECHANICAL_CRAFTING)) {
            applyCreateMechanicalCrafting(entry.getCreateMechanicalCrafting());
        } else if (type.equals(RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)) {
            applyCreateSequencedAssembly(entry.getCreateSequencedAssembly());
        } else if (ExtendedCraftingRecipeEditorTypes.isTableType(type)) {
            applyExtendedCraftingTable(entry.getExtendedCraftingTable(), type);
        } else if (type.equals(ExtendedCraftingRecipeEditorTypes.ULTIMATE_SINGULARITY)) {
            entry.getExtendedCraftingUltimateSingularity()
                    .setResult(new ItemStack(itemFromRegistry("extendedcrafting:ultimate_singularity", Items.NETHER_STAR)));
        } else if (type.equals(ExtendedCraftingRecipeEditorTypes.COMBINATION)) {
            applyExtendedCraftingCombination(entry.getExtendedCraftingCombination());
        } else if (type.equals(ExtendedCraftingRecipeEditorTypes.COMPRESSOR_RECIPE)) {
            applyExtendedCraftingCompressor(entry.getExtendedCraftingCompressor());
        } else if (type.equals(ExtendedCraftingRecipeEditorTypes.SHAPED_ENDER_CRAFTER)
                || type.equals(ExtendedCraftingRecipeEditorTypes.SHAPELESS_ENDER_CRAFTER)) {
            applyExtendedCraftingEnderCrafter(entry.getExtendedCraftingEnderCrafter(), type);
        } else if (type.equals(ExtendedCraftingRecipeEditorTypes.SHAPED_FLUX_CRAFTER)
                || type.equals(ExtendedCraftingRecipeEditorTypes.SHAPELESS_FLUX_CRAFTER)) {
            applyExtendedCraftingFluxCrafter(entry.getExtendedCraftingFluxCrafter(), type);
        } else if (AvaritiaRecipeEditorTypes.isTableType(type)) {
            applyAvaritiaTable(entry.getAvaritiaTable(), type);
        } else if (type.equals(AvaritiaRecipeEditorTypes.COMPRESSOR)) {
            applyAvaritiaCompressor(entry.getAvaritiaCompressor());
        } else if (type.equals(AvaritiaRecipeEditorTypes.EXTREME_SMITHING)) {
            entry.getAvaritiaExtremeSmithing()
                    .setTemplate(RecipeIngredient.item(itemFromRegistry("avaritia:infinity_catalyst", Items.NETHER_STAR)))
                    .setBase(RecipeIngredient.item(Items.NETHERITE_CHESTPLATE))
                    .setAdditions(new ArrayList<>(List.of(
                            RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)),
                            RecipeIngredient.item(itemFromRegistry("avaritia:enhancement_core", Items.NETHER_STAR)),
                            RecipeIngredient.item(itemFromRegistry("avaritia:infinity_catalyst", Items.NETHER_STAR))
                    )))
                    .setResult(new ItemStack(itemFromRegistry("avaritia:infinity_chestplate", Items.NETHERITE_CHESTPLATE)));
        } else if (type.equals(AvaritiaRecipeEditorTypes.INFINITY_CATALYST)) {
            entry.getAvaritiaInfinityCatalyst()
                    .setGroup("default")
                    .setIngredients(new ArrayList<>(List.of(RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)))))
                    .setCount(1);
        } else if (type.equals(AvaritiaRecipeEditorTypes.ETERNAL_SINGULARITY)) {
            entry.getAvaritiaEternalSingularity()
                    .setIngredients(new ArrayList<>(List.of(RecipeIngredient.item(itemFromRegistry("avaritia:singularity", Items.NETHER_STAR)))))
                    .setCount(1);
        } else if (type.equals(AvaritiaRecipeEditorTypes.FULL_MATTER_CLUSTER)) {
            entry.getAvaritiaFullMatterCluster()
                    .setGroup("default")
                    .setIngredients(new ArrayList<>(List.of(RecipeIngredient.item(Items.CHEST))))
                    .setCount(1);
        } else {
            CreateProcessingKind.byType(type).ifPresent(kind -> applyCreateProcessing(entry.getCreateProcessing(), kind));
        }
    }

    private static void applyMekanism(RecipeEntry entry, MekanismRecipeKind kind) {
        var data = entry.getMekanism()
                .setItemInput(RecipeIngredient.item(Items.COBBLESTONE))
                .setExtraItemInput(RecipeIngredient.item(Items.COBBLESTONE))
                .setItemInputAmount(1)
                .setExtraItemInputAmount(1)
                .setFluidInput(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)))
                .setChemicalInput(mekanismChemicalIngredient("oxygen"))
                .setExtraChemicalInput(mekanismChemicalIngredient("hydrogen"))
                .setItemOutput(new ItemStack(Items.IRON_INGOT))
                .setSecondaryItemOutput(ItemStack.EMPTY)
                .setSecondaryChance(0)
                .setFluidOutput(new FluidStack(Fluids.WATER, 1000))
                .setChemicalOutput(mekanismChemicalOutput("hydrogen"))
                .setSecondaryChemicalOutput(mekanismChemicalOutput("oxygen"))
                .setPerTickUsage(false)
                .setDuration(100)
                .setEnergyRequired(0)
                .setEnergyMultiplier(1)
                .setEnergyOutput(1_000);
        if (kind == MekanismRecipeKind.SAWING) {
            data.setItemOutput(new ItemStack(Items.STICK));
        }
    }

    private static MekanismChemicalIngredientData mekanismChemicalIngredient(String path) {
        return new MekanismChemicalIngredientData()
                .setKind(MekanismChemicalIngredientKind.CHEMICAL)
                .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", path))
                .setAmount(1);
    }

    private static MekanismChemicalStackData mekanismChemicalOutput(String path) {
        return new MekanismChemicalStackData()
                .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", path))
                .setAmount(1);
    }

    private static void applyCreateMechanicalCrafting(CreateMechanicalCraftingRecipeData data) {
        data.setWidth(3)
                .setHeight(3)
                .setAcceptMirrored(true)
                .setPattern(new ArrayList<>(List.of(
                        "AAA",
                        "A A",
                        "AAA"
                )))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(itemFromRegistry("create:brass_ingot", Items.IRON_INGOT)))
                )))
                .setResult(new ItemStack(itemFromRegistry("create:mechanical_crafter", Items.CRAFTING_TABLE)));
    }

    private static void applyCreateSequencedAssembly(CreateSequencedAssemblyRecipeData data) {
        data.setIngredient(RecipeIngredient.item(itemFromRegistry("create:golden_sheet", Items.GOLD_INGOT)))
                .setTransitionalItem(new ItemStack(itemFromRegistry("create:incomplete_precision_mechanism", Items.CLOCK)))
                .setLoops(5)
                .setSequence(new ArrayList<>(List.of(
                        createSequencedDeployingStep("create:cogwheel", Items.IRON_NUGGET),
                        createSequencedDeployingStep("create:large_cogwheel", Items.IRON_NUGGET),
                        createSequencedDeployingStep("minecraft:iron_nugget", Items.IRON_NUGGET)
                )))
                .setOutputs(new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(itemFromRegistry("create:precision_mechanism", Items.CLOCK))))));
    }

    private static CreateSequencedAssemblyStepData createSequencedDeployingStep(String itemId, Item fallback) {
        return new CreateSequencedAssemblyStepData()
                .setKind(CreateSequencedAssemblyStepKind.DEPLOYING)
                .setIngredient(RecipeIngredient.item(itemFromRegistry(itemId, fallback)));
    }

    private static void applyCreateProcessing(CreateProcessingRecipeData data, CreateProcessingKind kind) {
        var defaultIngredients = new ArrayList<RecipeIngredient>();
        var defaultInputCount = switch (kind) {
            case AUTO_PACKING -> 9;
            case AUTOMATIC_SHAPELESS -> 2;
            default -> 1;
        };
        for (int i = 0; i < defaultInputCount; i++) {
            defaultIngredients.add(RecipeIngredient.item(kind.defaultInput()));
        }
        data.setIngredients(defaultIngredients);
        data.setFluidIngredients(new ArrayList<>());
        data.setOutputs(kind.maxItemOutputs() > 0
                ? new ArrayList<>(List.of(RecipeOutputData.of(new ItemStack(kind.defaultOutput()))))
                : new ArrayList<>());
        data.setFluidOutputs(new ArrayList<>());
        data.setProcessingTime(kind.durationAllowed() ? 100 : 0);
        data.setHeatRequirement(kind == CreateProcessingKind.AUTOMATIC_BREWING ? CreateHeatCondition.HEATED : CreateHeatCondition.NONE);
        data.setKeepHeldItem(false);
        if (kind.maxFluidInputs() > 0) {
            data.getFluidIngredients().add(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)));
        }
        if (kind.maxFluidOutputs() > 0 && kind.maxItemOutputs() == 1 && kind == CreateProcessingKind.EMPTYING) {
            data.getFluidOutputs().add(new FluidStack(Fluids.WATER, 250));
        } else if (kind.maxFluidOutputs() > 0 && kind == CreateProcessingKind.AUTOMATIC_BREWING) {
            data.getFluidOutputs().add(new FluidStack(Fluids.WATER, 1000));
        }
    }

    private static void applyExtendedCraftingTable(ExtendedCraftingTableRecipeData data, ResourceLocation type) {
        var tier = ExtendedCraftingRecipeEditorTypes.tableTierForType(type);
        var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(tier);
        data.setTier(tier)
                .setWidth(gridSize)
                .setHeight(gridSize)
                .setPattern(singleSlotPattern(gridSize))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(itemFromRegistry("extendedcrafting:black_iron_ingot", Items.IRON_INGOT)))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("extendedcrafting:black_iron_ingot", Items.IRON_INGOT))
                )))
                .setResult(new ItemStack(itemFromRegistry(defaultTableResultItem(tier), Items.CRAFTING_TABLE)));
    }

    private static ArrayList<String> singleSlotPattern(int size) {
        var pattern = new ArrayList<String>();
        for (int row = 0; row < Math.clamp(size, 1, 9); row++) {
            pattern.add(row == 0 ? "A" + " ".repeat(Math.max(0, size - 1)) : " ".repeat(size));
        }
        return pattern;
    }

    private static String defaultTableResultItem(int tier) {
        return switch (Math.clamp(tier, 1, 4)) {
            case 1 -> "extendedcrafting:basic_component";
            case 2 -> "extendedcrafting:advanced_component";
            case 3 -> "extendedcrafting:elite_component";
            default -> "extendedcrafting:ultimate_component";
        };
    }

    private static void applyExtendedCraftingCombination(ExtendedCraftingCombinationRecipeData data) {
        data.setInput(RecipeIngredient.item(Items.DIAMOND))
                .setPedestalItems(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("extendedcrafting:black_iron_ingot", Items.IRON_INGOT)),
                        RecipeIngredient.item(Items.GOLD_INGOT),
                        RecipeIngredient.item(Items.REDSTONE)
                )))
                .setResult(new ItemStack(Items.NETHER_STAR))
                .setPowerCost(100000)
                .setPowerRate(500);
    }

    private static void applyExtendedCraftingCompressor(ExtendedCraftingCompressorRecipeData data) {
        data.setInputs(new ArrayList<>(List.of(new ExtendedCraftingCountedIngredientData()
                        .setIngredient(RecipeIngredient.item(Items.COBBLESTONE))
                        .setCount(64))))
                .setCatalyst(RecipeIngredient.item(itemFromRegistry("extendedcrafting:ultimate_catalyst", Items.NETHER_STAR)))
                .setResult(new ItemStack(Items.DIAMOND))
                .setPowerCost(100000)
                .setPowerRate(500);
    }

    private static void applyExtendedCraftingEnderCrafter(ExtendedCraftingEnderCrafterRecipeData data, ResourceLocation type) {
        data.setPattern(new ArrayList<>(List.of(
                        " A ",
                        "ABA",
                        " A "
                )))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(Items.ENDER_EYE)),
                        ShapedKeyEntry.of("B", RecipeIngredient.item(Items.NETHER_STAR))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.ENDER_EYE),
                        RecipeIngredient.item(Items.NETHER_STAR)
                )))
                .setResult(new ItemStack(itemFromRegistry("extendedcrafting:ender_star", Items.ENDER_EYE)))
                .setCraftingTime(type.equals(ExtendedCraftingRecipeEditorTypes.SHAPELESS_ENDER_CRAFTER) ? 100 : 200);
    }

    private static void applyExtendedCraftingFluxCrafter(ExtendedCraftingFluxCrafterRecipeData data, ResourceLocation type) {
        data.setPattern(new ArrayList<>(List.of(
                        " A ",
                        "ABA",
                        " A "
                )))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(Items.REDSTONE)),
                        ShapedKeyEntry.of("B", RecipeIngredient.item(Items.NETHER_STAR))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.REDSTONE),
                        RecipeIngredient.item(Items.NETHER_STAR)
                )))
                .setResult(new ItemStack(itemFromRegistry("extendedcrafting:flux_star", Items.REDSTONE)))
                .setPowerRequired(type.equals(ExtendedCraftingRecipeEditorTypes.SHAPELESS_FLUX_CRAFTER) ? 200000 : 400000)
                .setPowerRate(500);
    }

    private static void applyAvaritiaTable(AvaritiaTableRecipeData data, ResourceLocation type) {
        var tier = AvaritiaRecipeEditorTypes.tableTierForType(type);
        var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier);
        data.setTier(tier)
                .setWidth(gridSize)
                .setHeight(gridSize)
                .setCompatible(false)
                .setPattern(singleSlotPattern(gridSize))
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of("A", RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT)))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("avaritia:neutron_ingot", Items.IRON_INGOT))
                )))
                .setResult(new ItemStack(itemFromRegistry(defaultAvaritiaTableResultItem(tier), Items.NETHER_STAR)));
    }

    private static String defaultAvaritiaTableResultItem(int tier) {
        return switch (Math.clamp(tier, 1, 4)) {
            case 1 -> "avaritia:neutron_pile";
            case 2 -> "avaritia:neutron_nugget";
            case 3 -> "avaritia:neutron_ingot";
            default -> "avaritia:infinity_catalyst";
        };
    }

    private static void applyAvaritiaCompressor(AvaritiaCompressorRecipeData data) {
        data.setIngredient(RecipeIngredient.item(Items.COBBLESTONE))
                .setResult(new ItemStack(itemFromRegistry("avaritia:singularity", Items.BEDROCK)))
                .setInputCount(1000)
                .setTimeCost(240);
    }

    private static RecipeIngredient tagIngredient(String id) {
        var location = ResourceLocation.tryParse(id);
        return RecipeIngredient.tag(location == null ? ResourceLocation.withDefaultNamespace("planks") : location);
    }
}
