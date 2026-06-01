package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.ui.ColorConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.RegistrySearchComponent;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.create.CreateHeatCondition;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.irons_spellbooks.IronAlchemistCauldronRecipeData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class RecipePropertiesSections {
    private RecipePropertiesSections() {
    }

    static void buildCooking(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.cooking"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience",
                        RecipeEditorUi.floatField(controller.getCookingExperience(entry), 0, Integer.MAX_VALUE, value -> controller.setCookingExperience(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time",
                        RecipeEditorUi.intField(controller.getCookingTime(entry), 1, 72000, value -> controller.setCookingTime(entry, value)))
        );
    }

    static void buildFarmersCooking(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cooking_pot"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience",
                        RecipeEditorUi.floatField(controller.getFarmersCookingExperience(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setFarmersCookingExperience(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time",
                        RecipeEditorUi.intField(controller.getFarmersCookingTime(entry), 1, 72000,
                                value -> controller.setFarmersCookingTime(entry, value)))
        );
    }

    static void buildFarmersCutting(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cutting_board"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.custom_sound",
                        new Switch()
                                .setOn(controller.getFarmersCuttingCustomSound(entry), false)
                                .setOnSwitchChanged(value -> controller.setFarmersCuttingCustomSound(entry, value)))
        );
        if (controller.getFarmersCuttingCustomSound(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.sound",
                    RecipeEditorUi.resourceLocationField(controller.getFarmersCuttingSound(entry),
                            value -> controller.setFarmersCuttingSound(entry, value))));
        }
    }

    static void buildAlchemistCauldron(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        if (controller.isIronAlchemistCauldronBrewEntry(entry)) {
            return;
        }
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.alchemist_cauldron"));
        buildAlchemistFillEmpty(content, controller, entry, entry.getIronAlchemistCauldron());
    }

    private static void buildAlchemistFillEmpty(UIElement content, RecipeEditorController controller, RecipeEntry entry, IronAlchemistCauldronRecipeData data) {
        if (controller.isIronAlchemistCauldronFillEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.irons_spellbooks.alchemist_cauldron.must_fit_all",
                    new Switch()
                            .setOn(data.isMustFitAll(), false)
                            .setOnSwitchChanged(value -> {
                                data.setMustFitAll(value);
                                controller.notifyChanged();
                            })));
        }
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.irons_spellbooks.alchemist_cauldron.sound",
                RecipeEditorUi.resourceLocationField(data.getSound(), value -> {
                    data.setSound(value);
                    controller.notifyChanged();
                })));
    }

    static void buildDragonForge(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.dragon_forge"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.iceandfire.dragon_forge.dragon_type",
                        RecipeEditorUi.selector(
                                controller.dragonForgeDragonTypes(),
                                controller.getDragonForgeDragonType(entry),
                                controller::dragonForgeDragonTypeDisplayName,
                                value -> controller.setDragonForgeDragonType(entry, value)
                        )),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.iceandfire.dragon_forge.cook_time",
                        RecipeEditorUi.intField(controller.getDragonForgeCookTime(entry), 1, Integer.MAX_VALUE,
                                value -> controller.setDragonForgeCookTime(entry, value)))
        );
    }

    static void buildCreateProcessing(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.processing"));
        if (controller.isCreateAutoPackingEntry(entry)) {
            buildCreateAutoPacking(content, controller, entry);
        }
        if (controller.selectedCreateDurationAllowed()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.processing_time",
                    RecipeEditorUi.intField(controller.getCreateProcessingTime(entry), 0, Integer.MAX_VALUE,
                            value -> controller.setCreateProcessingTime(entry, value))));
        }
        if (controller.selectedCreateHeatAllowed()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.heat_requirement",
                    RecipeEditorUi.selector(
                            List.of(CreateHeatCondition.NONE, CreateHeatCondition.HEATED, CreateHeatCondition.SUPERHEATED),
                            controller.getCreateHeatRequirement(entry),
                            controller::createHeatDisplayName,
                            value -> controller.setCreateHeatRequirement(entry, value)
                    )));
        }
        if (controller.selectedCreateKeepHeldItemAllowed()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.keep_held_item",
                    new Switch()
                            .setOn(controller.getCreateKeepHeldItem(entry), false)
                            .setOnSwitchChanged(value -> controller.setCreateKeepHeldItem(entry, value))));
        }
    }

    static void buildCreateMechanicalCrafting(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.mechanical_crafting"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.width",
                        RecipeEditorUi.intField(controller.getCreateMechanicalCraftingWidth(entry), 1, 9,
                                value -> controller.setCreateMechanicalCraftingWidth(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.height",
                        RecipeEditorUi.intField(controller.getCreateMechanicalCraftingHeight(entry), 1, 9,
                                value -> controller.setCreateMechanicalCraftingHeight(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.mechanical_crafting.accept_mirrored",
                        new Switch()
                                .setOn(controller.getCreateMechanicalCraftingAcceptMirrored(entry), false)
                                .setOnSwitchChanged(value -> controller.setCreateMechanicalCraftingAcceptMirrored(entry, value)))
        );
    }

    static void buildCreateSequencedAssembly(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.sequenced_assembly.loops",
                        RecipeEditorUi.intField(controller.getCreateSequencedLoops(entry), 1, Integer.MAX_VALUE,
                                value -> controller.setCreateSequencedLoops(entry, value)))
        );
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.add_step"),
                Icons.ADD,
                event -> controller.addCreateSequencedStep(entry)
        ).layout(layout -> layout.widthPercent(100).height(18)));
    }

    static void buildSelectedCreateSequencedStep(UIElement content, RecipeEditorController controller, RecipeEntry entry, Runnable fallback) {
        var index = controller.getSlotSelection().index();
        if (!controller.isCreateSequencedAssemblyEntry(entry) || index < 0 || index >= controller.selectedCreateSequencedStepCount()) {
            fallback.run();
            return;
        }
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.step"));
        buildCreateSequencedStep(content, controller, entry, index);
    }

    private static void buildCreateSequencedStep(UIElement content, RecipeEditorController controller, RecipeEntry entry, int index) {
        content.addChildren(
                createStepTitle(index),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.sequenced_assembly.step.kind",
                        RecipeEditorUi.selector(
                                controller.createSequencedStepKinds(),
                                controller.getCreateSequencedStepKind(entry, index),
                                controller::createSequencedStepKindDisplayName,
                                kind -> controller.setCreateSequencedStepKind(entry, index, kind)
                        ))
        );
        var kind = controller.getCreateSequencedStepKind(entry, index);
        if (kind == CreateSequencedAssemblyStepKind.DEPLOYING) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.keep_held_item",
                    new Switch()
                            .setOn(controller.getCreateSequencedStepKeepHeldItem(entry, index), false)
                            .setOnSwitchChanged(value -> controller.setCreateSequencedStepKeepHeldItem(entry, index, value))));
        }
        if (kind == CreateSequencedAssemblyStepKind.CUTTING) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.processing_time",
                    RecipeEditorUi.intField(controller.getCreateSequencedStepProcessingTime(entry, index), 0, Integer.MAX_VALUE,
                            value -> controller.setCreateSequencedStepProcessingTime(entry, index, value))));
        }
        content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.create.sequenced_assembly.remove_step"),
                Icons.DELETE,
                event -> controller.removeCreateSequencedStep(entry, index)
        ).layout(layout -> layout.widthPercent(100).height(18)));
    }

    private static UIElement createStepTitle(int index) {
        var label = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.sequenced_assembly.step", index + 1));
        label.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.height(16));
        return label;
    }

    static void buildCreateAutoPacking(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.auto_packing.grid_size",
                RecipeEditorUi.selector(
                        controller.createAutoPackingGridSizes(),
                        controller.getCreateAutoPackingGridSize(entry),
                        controller::createAutoPackingGridSizeDisplayName,
                        value -> controller.setCreateAutoPackingGridSize(entry, value)
                )));
    }

    static void buildExtendedCraftingTable(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.table"));
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.table.size",
                RecipeEditorUi.selector(
                        controller.extendedCraftingTableTiers(),
                        controller.getExtendedCraftingTableTier(entry),
                        controller::extendedCraftingTableTierDisplayName,
                        value -> controller.setExtendedCraftingTableTier(entry, value)
                )));
    }

    static void buildExtendedCraftingEnderCrafter(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.ender_crafter"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.ender_crafter.crafting_time",
                        RecipeEditorUi.intField(controller.getExtendedCraftingEnderCraftingTime(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingEnderCraftingTime(entry, value)))
        );
    }

    static void buildExtendedCraftingFluxCrafter(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.flux_crafter"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.flux_crafter.power_required",
                        RecipeEditorUi.intField(controller.getExtendedCraftingFluxPowerRequired(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingFluxPowerRequired(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.power_rate",
                        RecipeEditorUi.intField(controller.getExtendedCraftingFluxPowerRate(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingFluxPowerRate(entry, value)))
        );
    }

    static void buildExtendedCraftingCombination(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.combination"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.power_cost",
                        RecipeEditorUi.intField(controller.getExtendedCraftingCombinationPowerCost(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingCombinationPowerCost(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.power_rate",
                        RecipeEditorUi.intField(controller.getExtendedCraftingCombinationPowerRate(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingCombinationPowerRate(entry, value)))
        );
    }

    static void buildExtendedCraftingCompressor(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.compressor"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.power_cost",
                        RecipeEditorUi.intField(controller.getExtendedCraftingCompressorPowerCost(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingCompressorPowerCost(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.power_rate",
                        RecipeEditorUi.intField(controller.getExtendedCraftingCompressorPowerRate(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingCompressorPowerRate(entry, value)))
        );
    }

    static void buildAvaritiaTable(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.avaritia.table"));
        if (controller.isAvaritiaNormalTableEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.avaritia.table.size",
                    RecipeEditorUi.selector(
                            controller.avaritiaTableTiers(),
                            controller.getAvaritiaTableTier(entry),
                            controller::avaritiaTableTierDisplayName,
                            value -> controller.setAvaritiaTableTier(entry, value)
                    )));
            if (controller.isAvaritiaShapedTableEntry(entry)) {
                content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.avaritia.table.compatible",
                        new Switch()
                                .setOn(controller.getAvaritiaTableCompatible(entry), false)
                                .setOnSwitchChanged(value -> controller.setAvaritiaTableCompatible(entry, value))));
            }
        }
    }

    static void buildAvaritiaCompressor(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.avaritia.compressor"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.avaritia.compressor.input_count",
                        RecipeEditorUi.intField(controller.getAvaritiaCompressorInputCount(entry), 1, Integer.MAX_VALUE,
                                value -> controller.setAvaritiaCompressorInputCount(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.avaritia.compressor.time_cost",
                        RecipeEditorUi.intField(controller.getAvaritiaCompressorTimeCost(entry), 1, Integer.MAX_VALUE,
                                value -> controller.setAvaritiaCompressorTimeCost(entry, value)))
        );
    }

    static void buildArsNouveauApparatus(UIElement content, RecipeEditorController controller, RecipeEntry entry, BooleanSupplier rebuilding) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau"));
        if (controller.isArsNouveauApparatusEntry(entry)
                || controller.isArsNouveauArmorUpgradeEntry(entry)
                || controller.isArsNouveauEnchantmentEntry(entry)
                || controller.isArsNouveauImbuementEntry(entry)
                || controller.isArsNouveauPedestalOnlyEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.source_cost",
                    RecipeEditorUi.intField(controller.getArsNouveauSourceCost(entry), 0, Integer.MAX_VALUE,
                            value -> controller.setArsNouveauSourceCost(entry, value))));
        }
        if (controller.isArsNouveauApparatusEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.apparatus.keep_nbt_of_reagent",
                    new Switch()
                            .setOn(controller.getArsNouveauKeepNbtOfReagent(entry), false)
                            .setOnSwitchChanged(value -> controller.setArsNouveauKeepNbtOfReagent(entry, value))));
        }
        if (controller.isArsNouveauArmorUpgradeEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.armor_upgrade.tier",
                    RecipeEditorUi.intField(controller.getArsNouveauArmorUpgradeTier(entry), 2, Integer.MAX_VALUE,
                            value -> controller.setArsNouveauArmorUpgradeTier(entry, value))));
        }
        if (controller.isArsNouveauEnchantmentEntry(entry)) {
            content.addChildren(
                    createArsNouveauEnchantmentConfigurator(controller, entry, rebuilding),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.enchantment.level",
                            RecipeEditorUi.intField(controller.getArsNouveauEnchantmentLevel(entry), 1, Integer.MAX_VALUE,
                                    value -> controller.setArsNouveauEnchantmentLevel(entry, value)))
            );
        }
        if (controller.isArsNouveauGlyphEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.glyph.exp",
                    RecipeEditorUi.intField(controller.getArsNouveauGlyphExperience(entry), 0, Integer.MAX_VALUE,
                            value -> controller.setArsNouveauGlyphExperience(entry, value))));
        }
    }

    static void buildArsNouveauCrush(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau.crush"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.skip_block_place",
                        new Switch()
                                .setOn(controller.getArsNouveauCrushSkipBlockPlace(entry), false)
                                .setOnSwitchChanged(value -> controller.setArsNouveauCrushSkipBlockPlace(entry, value)))
        );
    }

    static void buildKaleidoscope(UIElement content, RecipeEditorController controller, RecipeEntry entry, BooleanSupplier rebuilding) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"));
        if (controller.isKaleidoscopePotEntry(entry)) {
            var data = entry.getKaleidoscopePot();
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.time",
                            RecipeEditorUi.intField(data.getTime(), 1, Integer.MAX_VALUE, value -> {
                                data.setTime(Math.max(1, value));
                                controller.notifyChanged();
                            })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.stir_fry_count",
                            RecipeEditorUi.intField(data.getStirFryCount(), 0, Integer.MAX_VALUE, value -> {
                                data.setStirFryCount(Math.max(0, value));
                                controller.notifyChanged();
                            }))
            );
        } else if (controller.isKaleidoscopeStockpotEntry(entry)) {
            var data = entry.getKaleidoscopeStockpot();
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.time",
                            RecipeEditorUi.intField(data.getTime(), 1, Integer.MAX_VALUE, value -> {
                                data.setTime(Math.max(1, value));
                                controller.notifyChanged();
                            })),
                    createFluidRegistryConfigurator("viscript_recipe.config.kaleidoscope_cookery.soup_base",
                            data::getSoupBase, value -> {
                                data.setSoupBase(value);
                                controller.notifyChanged();
                            }, rebuilding),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.cooking_texture",
                            RecipeEditorUi.resourceLocationField(data.getCookingTexture(), value -> {
                                data.setCookingTexture(value);
                                controller.notifyChanged();
                            })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.finished_texture",
                            RecipeEditorUi.resourceLocationField(data.getFinishedTexture(), value -> {
                                data.setFinishedTexture(value);
                                controller.notifyChanged();
                            })),
                    createRgbColorConfigurator("viscript_recipe.config.kaleidoscope_cookery.cooking_bubble_color",
                            data::getCookingBubbleColor, value -> {
                                data.setCookingBubbleColor(value);
                                controller.notifyChanged();
                            }, rebuilding),
                    createRgbColorConfigurator("viscript_recipe.config.kaleidoscope_cookery.finished_bubble_color",
                            data::getFinishedBubbleColor, value -> {
                                data.setFinishedBubbleColor(value);
                                controller.notifyChanged();
                            }, rebuilding)
            );
        } else if (controller.isKaleidoscopeChoppingBoardEntry(entry)) {
            var data = entry.getKaleidoscopeChoppingBoard();
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.cut_count",
                            RecipeEditorUi.intField(data.getCutCount(), 1, Integer.MAX_VALUE, value -> {
                                data.setCutCount(Math.max(1, value));
                                controller.notifyChanged();
                            })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.model_id",
                            RecipeEditorUi.resourceLocationField(data.getModelId(), value -> {
                                data.setModelId(value);
                                controller.notifyChanged();
                            }))
            );
        } else if (controller.isKaleidoscopeSteamerEntry(entry)) {
            var data = entry.getKaleidoscopeSteamer();
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.cook_tick",
                    RecipeEditorUi.intField(data.getCookTick(), 1, Integer.MAX_VALUE, value -> {
                        data.setCookTick(Math.max(1, value));
                        controller.notifyChanged();
                    })));
        } else if (controller.isKaleidoscopeTeapotEntry(entry)) {
            var data = entry.getKaleidoscopeTeapot();
            content.addChildren(
                    createFluidRegistryConfigurator("viscript_recipe.config.kaleidoscope_cookery.tea_fluid",
                            data::getTeaFluid, value -> {
                                data.setTeaFluid(value);
                                controller.notifyChanged();
                            }, rebuilding),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.ingredient_count",
                            RecipeEditorUi.intField(data.getIngredientCount(), 1, Integer.MAX_VALUE, value -> {
                                data.setIngredientCount(Math.max(1, value));
                                controller.notifyChanged();
                            })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.kaleidoscope_cookery.time",
                            RecipeEditorUi.intField(data.getTime(), 1, Integer.MAX_VALUE, value -> {
                                data.setTime(Math.max(1, value));
                                controller.notifyChanged();
                            }))
            );
        }
    }

    private static UIElement createRgbColorConfigurator(String nameKey, Supplier<Integer> supplier, Consumer<Integer> consumer, BooleanSupplier rebuilding) {
        var configurator = new ColorConfigurator(
                nameKey,
                () -> opaqueRgb(supplier.get()),
                value -> {
                    if (!rebuilding.getAsBoolean()) {
                        consumer.accept(toRgb(value));
                    }
                },
                0xFFFFFFFF,
                true
        );
        configurator.colorSelector.alphaSlider.setDisplay(false);
        configurator.layout(layout -> layout.widthPercent(100));
        return configurator;
    }

    private static UIElement createFluidRegistryConfigurator(String nameKey, Supplier<ResourceLocation> supplier, Consumer<ResourceLocation> consumer, BooleanSupplier rebuilding) {
        var configurator = new RegistrySearchComponent.Fluid(
                nameKey,
                () -> fluidFromRegistry(supplier.get()),
                fluid -> {
                    if (!rebuilding.getAsBoolean()) {
                        consumer.accept(fluidRegistryName(fluid));
                    }
                },
                Fluids.WATER,
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private static UIElement createArsNouveauEnchantmentConfigurator(RecipeEditorController controller, RecipeEntry entry, BooleanSupplier rebuilding) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) {
            return RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.enchantment.enchantment",
                    RecipeEditorUi.resourceLocationField(controller.getArsNouveauEnchantmentId(entry),
                            value -> controller.setArsNouveauEnchantmentId(entry, value)));
        }
        var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var defaultEnchantment = registry.getOptional(ResourceLocation.withDefaultNamespace("sharpness"))
                .orElseGet(() -> registry.getAny().map(holder -> holder.value()).orElseThrow());
        var configurator = new RegistrySearchComponent<>(
                "viscript_recipe.config.ars_nouveau.enchantment.enchantment",
                () -> registry.getOptional(controller.getArsNouveauEnchantmentId(entry)).orElse(defaultEnchantment),
                enchantment -> {
                    if (!rebuilding.getAsBoolean()) {
                        controller.setArsNouveauEnchantmentId(entry, registry.getKey(enchantment));
                    }
                },
                defaultEnchantment,
                true,
                registry,
                UIElementProvider.iconText(
                        enchantment -> new ItemStackTexture(new ItemStack(Items.ENCHANTED_BOOK)),
                        Enchantment::description
                )
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.setTranslator(enchantment -> enchantment.description().getString());
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private static int opaqueRgb(Integer color) {
        return 0xFF000000 | toRgb(color);
    }

    private static int toRgb(Integer color) {
        return color == null ? 0xFFFFFF : color & 0xFFFFFF;
    }

    private static Fluid fluidFromRegistry(ResourceLocation id) {
        if (id == null) {
            return Fluids.WATER;
        }
        var fluid = BuiltInRegistries.FLUID.get(id);
        return fluid == null || fluid == Fluids.EMPTY ? Fluids.WATER : fluid;
    }

    private static ResourceLocation fluidRegistryName(Fluid fluid) {
        var id = BuiltInRegistries.FLUID.getKey(fluid == null || fluid == Fluids.EMPTY ? Fluids.WATER : fluid);
        return id == null ? ResourceLocation.withDefaultNamespace("water") : id;
    }
}
