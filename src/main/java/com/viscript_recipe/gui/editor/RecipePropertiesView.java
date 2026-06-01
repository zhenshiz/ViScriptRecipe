package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.BlockAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.FluidStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.ColorConfigurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.RegistrySearchComponent;
import com.lowdragmc.lowdraglib2.configurator.ui.TagKeySearchComponent;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.data.create.CreateFluidIngredientData;
import com.viscript_recipe.data.create.CreateFluidIngredientKind;
import com.viscript_recipe.data.create.CreateHeatCondition;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.irons_spellbooks.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecipePropertiesView extends View {
    private final RecipeEditorController controller;
    private final UIElement content = RecipeEditorUi.column();
    private boolean rebuilding;
    private PropertiesStructureKey lastStructureKey;

    public RecipePropertiesView(RecipeEditorController controller) {
        super("viscript_recipe.view.recipe_properties", Icons.SETTINGS);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(content.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(6);
        }));
        root.addChild(scroller);
        return root;
    }

    private void refresh() {
        if (rebuilding) {
            return;
        }
        var structureKey = createStructureKey();
        if (structureKey.equals(lastStructureKey)) {
            return;
        }
        rebuilding = true;
        try {
            lastStructureKey = structureKey;
            content.clearAllChildren();
            var entry = controller.getSelectedEntry();
            if (entry == null) {
                content.addChild(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.no_entry")));
                return;
            }
            switch (controller.getSlotSelection().kind()) {
                case RECIPE -> buildRecipeProperties(entry);
                case INGREDIENT -> buildIngredientProperties(entry);
                case FLUID -> buildFluidProperties();
                case RESULT -> buildResultProperties();
                case CONTAINER -> buildContainerProperties();
                case CUTTING_RESULT -> buildCuttingResultProperties();
                case CREATE_RESULT -> buildCreateResultProperties();
                case CREATE_TRANSITIONAL -> buildCreateSequencedTransitionalProperties();
                case CREATE_SEQUENCED_STEP -> buildSelectedCreateSequencedStepProperties(entry);
                case ARS_NOUVEAU_OUTPUT -> buildArsNouveauOutputProperties();
            }
        } finally {
            rebuilding = false;
        }
    }

    private PropertiesStructureKey createStructureKey() {
        var entry = controller.getSelectedEntry();
        var selection = controller.getSlotSelection();
        return new PropertiesStructureKey(
                entry == null ? 0 : System.identityHashCode(entry),
                controller.getSelectedCategory(),
                entry == null ? null : entry.getType(),
                selection,
                selectedIngredientKind(entry, selection),
                selectedRemainderMode(entry, selection),
                selectedCreateFluidIngredientKind(),
                entry != null && controller.supportsNotification(entry),
                entry != null && controller.isCookingEntry(entry),
                entry != null && controller.isFarmersCookingPotEntry(entry),
                entry != null && controller.isFarmersCuttingBoardEntry(entry),
                entry != null && controller.isFarmersCuttingBoardEntry(entry) && controller.getFarmersCuttingCustomSound(entry),
                entry != null && controller.isIronAlchemistCauldronEntry(entry),
                entry != null && controller.isIronAlchemistCauldronFillEntry(entry),
                entry != null && controller.isIronAlchemistCauldronEmptyEntry(entry),
                entry != null && controller.isIronAlchemistCauldronBrewEntry(entry),
                entry != null && controller.isIceAndFireDragonForgeEntry(entry),
                entry != null && controller.isCreateMechanicalCraftingEntry(entry),
                entry != null && controller.isCreateSequencedAssemblyEntry(entry),
                entry != null && controller.isCreateProcessingEntry(entry),
                entry != null && controller.isExtendedCraftingTableEntry(entry),
                entry != null && controller.isExtendedCraftingShapedTableEntry(entry),
                entry != null && controller.isExtendedCraftingEnderCrafterEntry(entry),
                entry != null && controller.isExtendedCraftingFluxCrafterEntry(entry),
                entry != null && controller.isExtendedCraftingCombinationEntry(entry),
                entry != null && controller.isExtendedCraftingCompressorEntry(entry),
                entry != null && controller.isAvaritiaTableEntry(entry),
                entry != null && controller.isAvaritiaShapedTableEntry(entry),
                entry != null && controller.isAvaritiaCompressorEntry(entry),
                controller.isSelectedExtendedCraftingCompressorInput(),
                controller.isSelectedCreateFluidInput(),
                controller.isSelectedCreateFluidOutput(),
                controller.selectedCreateDurationAllowed(),
                controller.selectedCreateHeatAllowed(),
                controller.selectedCreateKeepHeldItemAllowed(),
                controller.selectedCreateOutputChanceAllowed(),
                createSequencedStructureSignature(entry)
        );
    }

    private String createSequencedStructureSignature(RecipeEntry entry) {
        if (entry == null || !controller.isCreateSequencedAssemblyEntry(entry)) {
            return "";
        }
        var builder = new StringBuilder().append(controller.selectedCreateSequencedStepCount()).append(':');
        for (int i = 0; i < controller.selectedCreateSequencedStepCount(); i++) {
            builder.append(controller.getCreateSequencedStepKind(entry, i).getSerializedName()).append(';');
        }
        return builder.toString();
    }

    private IngredientValueKind selectedIngredientKind(RecipeEntry entry, WorkbenchSlotSelection selection) {
        if (entry == null || selection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return null;
        }
        return editableValue(controller.getSelectedIngredient()).getKind();
    }

    private CraftingRemainderMode selectedRemainderMode(RecipeEntry entry, WorkbenchSlotSelection selection) {
        if (entry == null || !entry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || selection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return null;
        }
        return remainderMode(controller.getSelectedRemainder());
    }

    private CreateFluidIngredientKind selectedCreateFluidIngredientKind() {
        if (!controller.isSelectedCreateFluidInput()) {
            return null;
        }
        var ingredient = controller.getSelectedCreateFluidIngredient();
        return ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
    }

    private record PropertiesStructureKey(
            int entryIdentity,
            ResourceLocation selectedCategory,
            ResourceLocation type,
            WorkbenchSlotSelection selection,
            IngredientValueKind ingredientKind,
            CraftingRemainderMode remainderMode,
            CreateFluidIngredientKind createFluidIngredientKind,
            boolean supportsNotification,
            boolean cooking,
            boolean farmersCookingPot,
            boolean farmersCuttingBoard,
            boolean farmersCuttingCustomSound,
            boolean ironAlchemistCauldron,
            boolean ironAlchemistCauldronFill,
            boolean ironAlchemistCauldronEmpty,
            boolean ironAlchemistCauldronBrew,
            boolean iceAndFireDragonForge,
            boolean createMechanicalCrafting,
            boolean createSequencedAssembly,
            boolean createProcessing,
            boolean extendedCraftingTable,
            boolean extendedCraftingShapedTable,
            boolean extendedCraftingEnderCrafter,
            boolean extendedCraftingFluxCrafter,
            boolean extendedCraftingCombination,
            boolean extendedCraftingCompressor,
            boolean avaritiaTable,
            boolean avaritiaShapedTable,
            boolean avaritiaCompressor,
            boolean selectedExtendedCraftingCompressorInput,
            boolean selectedCreateFluidInput,
            boolean selectedCreateFluidOutput,
            boolean createDurationAllowed,
            boolean createHeatAllowed,
            boolean createKeepHeldItemAllowed,
            boolean createOutputChanceAllowed,
            String createSequencedSignature
    ) {
    }

    private void buildRecipeProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.recipe"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.type",
                        RecipeEditorUi.selector(
                                controller.availableTypesForSelectedCategory(),
                                controller.getSelectedRecipeType(),
                                type -> type.displayName(),
                                controller::setSelectedRecipeType
                        )),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.enabled",
                        new Switch()
                                .setOn(entry.isEnabled(), false)
                                .setOnSwitchChanged(value -> {
                                    entry.setEnabled(value);
                                    controller.notifyChanged();
                                })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.recipe_id",
                        RecipeEditorUi.resourceLocationField(entry.getRecipeId(), value -> {
                            entry.setRecipeId(value);
                            controller.notifyChanged();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.operation",
                        RecipeEditorUi.selector(
                                List.of(RecipeOperation.ADD, RecipeOperation.REPLACE, RecipeOperation.REMOVE),
                                entry.getOperation(),
                                operation -> Component.translatable("viscript_recipe.editor.operation." + operation.getSerializedName()),
                                operation -> {
                                    entry.setOperation(operation);
                                    controller.notifyChanged();
                                }
                        ))
        );
        if (controller.supportsNotification(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.recipe.show_notification",
                    new Switch()
                            .setOn(controller.showNotification(entry), false)
                            .setOnSwitchChanged(value -> controller.setShowNotification(entry, value))));
        }
        if (controller.isCookingEntry(entry)) {
            buildCookingProperties(entry);
        }
        if (controller.isFarmersCookingPotEntry(entry)) {
            buildFarmersCookingProperties(entry);
        }
        if (controller.isFarmersCuttingBoardEntry(entry)) {
            buildFarmersCuttingProperties(entry);
        }
        if (controller.isIronAlchemistCauldronEntry(entry)) {
            buildAlchemistCauldronProperties(entry);
        }
        if (controller.isIceAndFireDragonForgeEntry(entry)) {
            buildDragonForgeProperties(entry);
        }
        if (controller.isCreateMechanicalCraftingEntry(entry)) {
            buildCreateMechanicalCraftingProperties(entry);
        }
        if (controller.isCreateSequencedAssemblyEntry(entry)) {
            buildCreateSequencedAssemblyProperties(entry);
        }
        if (controller.isCreateProcessingEntry(entry)) {
            buildCreateProcessingProperties(entry);
        }
        if (controller.isExtendedCraftingTableEntry(entry)) {
            buildExtendedCraftingTableProperties(entry);
        }
        if (controller.isExtendedCraftingEnderCrafterEntry(entry)) {
            buildExtendedCraftingEnderCrafterProperties(entry);
        }
        if (controller.isExtendedCraftingFluxCrafterEntry(entry)) {
            buildExtendedCraftingFluxCrafterProperties(entry);
        }
        if (controller.isExtendedCraftingCombinationEntry(entry)) {
            buildExtendedCraftingCombinationProperties(entry);
        }
        if (controller.isExtendedCraftingCompressorEntry(entry)) {
            buildExtendedCraftingCompressorProperties(entry);
        }
        if (controller.isAvaritiaTableEntry(entry)) {
            buildAvaritiaTableProperties(entry);
        }
        if (controller.isAvaritiaCompressorEntry(entry)) {
            buildAvaritiaCompressorProperties(entry);
        }
        if (controller.isArsNouveauApparatusLayoutEntry(entry) || controller.isArsNouveauImbuementLayoutEntry(entry) || controller.isArsNouveauGlyphLayoutEntry(entry)) {
            buildArsNouveauApparatusProperties(entry);
        }
        if (controller.isArsNouveauCrushEntry(entry)) {
            buildArsNouveauCrushProperties(entry);
        }
        if (controller.isKaleidoscopePotEntry(entry)
                || controller.isKaleidoscopeStockpotEntry(entry)
                || controller.isKaleidoscopeMillstoneEntry(entry)
                || controller.isKaleidoscopeChoppingBoardEntry(entry)
                || controller.isKaleidoscopeSteamerEntry(entry)
                || controller.isKaleidoscopeTeapotEntry(entry)) {
            buildKaleidoscopeProperties(entry);
        }
    }

    private void buildCookingProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.cooking"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience",
                        RecipeEditorUi.floatField(controller.getCookingExperience(entry), 0, Integer.MAX_VALUE, value -> controller.setCookingExperience(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time",
                        RecipeEditorUi.intField(controller.getCookingTime(entry), 1, 72000, value -> controller.setCookingTime(entry, value)))
        );
    }

    private void buildIngredientProperties(RecipeEntry entry) {
        var ingredient = copyIngredient(controller.getSelectedIngredient());
        var value = editableValue(ingredient);
        var availableKinds = controller.availableIngredientKindsForSelectedSlot();
        var selectedKind = availableKinds.contains(value.getKind()) ? value.getKind() : availableKinds.getFirst();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ingredient.value.kind",
                        RecipeEditorUi.selector(
                                availableKinds,
                                selectedKind,
                                kind -> Component.translatable("viscript_recipe.editor.ingredient.kind." + kind.getSerializedName()),
                                kind -> setIngredientKind(ingredient, value, kind)
                        ))
        );
        if (controller.isCreateAutoPackingEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.auto_packing.grid_size",
                    RecipeEditorUi.selector(
                            controller.createAutoPackingGridSizes(),
                            controller.getCreateAutoPackingGridSize(entry),
                            controller::createAutoPackingGridSizeDisplayName,
                            gridSize -> controller.setCreateAutoPackingGridSize(entry, gridSize)
                    )));
        }

        if (selectedKind == IngredientValueKind.ITEM && controller.isSelectedCreateItemApplicationBlockInput()) {
            content.addChild(createBlockConfigurator(
                    "viscript_recipe.editor.create.item_application.base_block",
                    () -> ingredientBlock(ingredient),
                    block -> setIngredientBlock(ingredient, block)
            ));
        } else if (selectedKind == IngredientValueKind.ITEM) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.editor.ingredient.item_slot",
                    () -> ingredientItemStack(ingredient),
                    stack -> setIngredientItem(ingredient, stack)
            ));
        } else if (selectedKind == IngredientValueKind.TAG) {
            content.addChild(createItemTagConfigurator(ingredient, value));
        } else if (selectedKind == IngredientValueKind.ITEM_ABILITY) {
            content.addChild(createItemAbilityConfigurator(ingredient, value));
        }
        if (controller.isSelectedExtendedCraftingCompressorInput()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.counted_ingredient.count",
                    RecipeEditorUi.intField(controller.getSelectedExtendedCraftingCompressorInputCount(), 1, Integer.MAX_VALUE,
                            controller::setSelectedExtendedCraftingCompressorInputCount)));
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            buildRemainderProperties();
        }
    }

    private void buildFarmersCookingProperties(RecipeEntry entry) {
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

    private void buildFarmersCuttingProperties(RecipeEntry entry) {
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

    private void buildAlchemistCauldronProperties(RecipeEntry entry) {
        if (controller.isIronAlchemistCauldronBrewEntry(entry)) {
            return;
        }
        var data = entry.getIronAlchemistCauldron();
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.alchemist_cauldron"));
        buildAlchemistFillEmptyProperties(entry, data);
    }

    private void buildAlchemistFillEmptyProperties(RecipeEntry entry, IronAlchemistCauldronRecipeData data) {
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

    private void buildDragonForgeProperties(RecipeEntry entry) {
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

    private void buildCreateProcessingProperties(RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.processing"));
        if (controller.isCreateAutoPackingEntry(entry)) {
            buildCreateAutoPackingProperties(entry);
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

    private void buildCreateMechanicalCraftingProperties(RecipeEntry entry) {
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

    private void buildCreateSequencedAssemblyProperties(RecipeEntry entry) {
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

    private void buildSelectedCreateSequencedStepProperties(RecipeEntry entry) {
        var index = controller.getSlotSelection().index();
        if (!controller.isCreateSequencedAssemblyEntry(entry) || index < 0 || index >= controller.selectedCreateSequencedStepCount()) {
            buildRecipeProperties(entry);
            return;
        }
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.step"));
        buildCreateSequencedStepProperties(entry, index);
    }

    private void buildCreateSequencedStepProperties(RecipeEntry entry, int index) {
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

    private UIElement createStepTitle(int index) {
        var label = RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.create.sequenced_assembly.step", index + 1));
        label.textStyle(style -> style
                .textColor(ColorPattern.WHITE.color)
                .textWrap(TextWrap.HOVER_ROLL));
        label.layout(layout -> layout.height(16));
        return label;
    }

    private void buildCreateAutoPackingProperties(RecipeEntry entry) {
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.create.auto_packing.grid_size",
                RecipeEditorUi.selector(
                        controller.createAutoPackingGridSizes(),
                        controller.getCreateAutoPackingGridSize(entry),
                        controller::createAutoPackingGridSizeDisplayName,
                        value -> controller.setCreateAutoPackingGridSize(entry, value)
                )));
    }

    private void buildExtendedCraftingTableProperties(RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.table"));
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.table.size",
                RecipeEditorUi.selector(
                        controller.extendedCraftingTableTiers(),
                        controller.getExtendedCraftingTableTier(entry),
                        controller::extendedCraftingTableTierDisplayName,
                        value -> controller.setExtendedCraftingTableTier(entry, value)
                )));
    }

    private void buildExtendedCraftingEnderCrafterProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.extendedcrafting.ender_crafter"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.ender_crafter.crafting_time",
                        RecipeEditorUi.intField(controller.getExtendedCraftingEnderCraftingTime(entry), 0, Integer.MAX_VALUE,
                                value -> controller.setExtendedCraftingEnderCraftingTime(entry, value)))
        );
    }

    private void buildExtendedCraftingFluxCrafterProperties(RecipeEntry entry) {
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

    private void buildExtendedCraftingCombinationProperties(RecipeEntry entry) {
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

    private void buildExtendedCraftingCompressorProperties(RecipeEntry entry) {
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

    private void buildAvaritiaTableProperties(RecipeEntry entry) {
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

    private void buildAvaritiaCompressorProperties(RecipeEntry entry) {
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

    private void buildArsNouveauApparatusProperties(RecipeEntry entry) {
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
                    createArsNouveauEnchantmentConfigurator(entry),
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

    private void buildArsNouveauCrushProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau.crush"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.skip_block_place",
                        new Switch()
                                .setOn(controller.getArsNouveauCrushSkipBlockPlace(entry), false)
                                .setOnSwitchChanged(value -> controller.setArsNouveauCrushSkipBlockPlace(entry, value)))
        );
    }

    private void buildKaleidoscopeProperties(RecipeEntry entry) {
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
                            }),
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
                            }),
                    createRgbColorConfigurator("viscript_recipe.config.kaleidoscope_cookery.finished_bubble_color",
                            data::getFinishedBubbleColor, value -> {
                                data.setFinishedBubbleColor(value);
                                controller.notifyChanged();
                            })
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
                            }),
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

    private UIElement createRgbColorConfigurator(String nameKey, Supplier<Integer> supplier, Consumer<Integer> consumer) {
        var configurator = new ColorConfigurator(
                nameKey,
                () -> opaqueRgb(supplier.get()),
                value -> {
                    if (!rebuilding) {
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

    private UIElement createFluidRegistryConfigurator(String nameKey, Supplier<ResourceLocation> supplier, Consumer<ResourceLocation> consumer) {
        var configurator = new RegistrySearchComponent.Fluid(
                nameKey,
                () -> fluidFromRegistry(supplier.get()),
                fluid -> {
                    if (!rebuilding) {
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

    private UIElement createArsNouveauEnchantmentConfigurator(RecipeEntry entry) {
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
                    if (!rebuilding) {
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

    private void buildRemainderProperties() {
        var remainder = controller.getSelectedRemainder();
        var mode = remainderMode(remainder);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.remainder"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.remainder.mode",
                        RecipeEditorUi.selector(
                                List.of(CraftingRemainderMode.DEFAULT, CraftingRemainderMode.CONSUME, CraftingRemainderMode.REPLACE),
                                mode,
                                value -> Component.translatable("viscript_recipe.editor.remainder.mode." + value.getSerializedName()),
                                value -> {
                                    var updated = remainder.copy();
                                    updated.setMode(value);
                                    if (value != CraftingRemainderMode.REPLACE) {
                                        updated.setItem(ItemStack.EMPTY);
                                    }
                                    controller.setSelectedRemainder(updated);
                                }
                        ),
                        Component.translatable("viscript_recipe.editor.remainder.tip.default"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.consume"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.replace"))
        );

        if (mode == CraftingRemainderMode.REPLACE) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.config.remainder.item",
                    () -> remainder.getItem() == null ? ItemStack.EMPTY : remainder.getItem().copy(),
                    stack -> {
                        var updated = remainder.copy();
                        updated.setMode(CraftingRemainderMode.REPLACE);
                        updated.setItem(stack == null ? ItemStack.EMPTY : stack.copy());
                        controller.setSelectedRemainder(updated);
                    }
            ));
        }
    }

    private void buildResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.recipe.result",
                        controller::getSelectedResult,
                        stack -> controller.setSelectedResult(normalizeResultStack(stack))
                )
        );
    }

    private void buildCreateSequencedTransitionalProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.sequenced_assembly.transitional_item"),
                createItemStackConfigurator(
                        "viscript_recipe.config.create.sequenced_assembly.transitional_item",
                        controller::getVisualCreateSequencedTransitional,
                        stack -> controller.setVisualCreateSequencedTransitional(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1))
                )
        );
    }

    private void buildContainerProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.container"),
                createItemStackConfigurator(
                        "viscript_recipe.config.farmersdelight.cooking.container",
                        controller::getSelectedContainer,
                        stack -> controller.setSelectedContainer(stack == null ? ItemStack.EMPTY : stack.copy())
                )
        );
    }

    private void buildCuttingResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.farmersdelight.cutting_result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.farmersdelight.cutting.result_item",
                        controller::getSelectedCuttingResult,
                        stack -> controller.setSelectedCuttingResult(normalizeResultStack(stack))
                ),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.farmersdelight.cutting.chance",
                        RecipeEditorUi.floatField(controller.getSelectedCuttingChance(), 0, 1,
                                controller::setSelectedCuttingChance))
        );
    }

    private void buildCreateResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.output"),
                createItemStackConfigurator(
                        "viscript_recipe.config.create.output.item",
                        controller::getSelectedCreateOutput,
                        stack -> controller.setSelectedCreateOutput(normalizeResultStack(stack))
                )
        );
        if (controller.selectedCreateOutputChanceAllowed()) {
            var labelKey = controller.getSelectedEntry() != null && controller.isCreateSequencedAssemblyEntry(controller.getSelectedEntry())
                    ? "viscript_recipe.config.create.output.weight"
                    : "viscript_recipe.config.create.output.chance";
            var max = controller.getSelectedEntry() != null && controller.isCreateSequencedAssemblyEntry(controller.getSelectedEntry())
                    ? Integer.MAX_VALUE
                    : 1;
            content.addChild(RecipeEditorUi.fieldGroup(labelKey,
                    RecipeEditorUi.floatField(controller.getSelectedCreateOutputChance(), 0, max,
                            controller::setSelectedCreateOutputChance)));
        }
    }

    private void buildArsNouveauOutputProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau.output"),
                createItemStackConfigurator(
                        "viscript_recipe.config.ars_nouveau.crush.output_item",
                        controller::getSelectedArsNouveauOutput,
                        stack -> controller.setSelectedArsNouveauOutput(normalizeResultStack(stack))
                ),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.chance",
                        RecipeEditorUi.floatField(controller.getSelectedArsNouveauOutputChance(), 0, 1,
                                controller::setSelectedArsNouveauOutputChance)),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.crush.max_range",
                        RecipeEditorUi.intField(controller.getSelectedArsNouveauOutputMaxRange(), 1, Integer.MAX_VALUE,
                                controller::setSelectedArsNouveauOutputMaxRange))
        );
    }

    private void buildFluidProperties() {
        if (controller.isSelectedCreateFluidInput()) {
            buildCreateFluidIngredientProperties();
            return;
        }
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.fluid"),
                createFluidStackConfigurator(
                        controller.selectedFluidConfigNameKey(),
                        controller::getSelectedFluid,
                        stack -> controller.setSelectedFluid(stack == null ? FluidStack.EMPTY : stack.copy())
                )
        );
    }

    private void buildCreateFluidIngredientProperties() {
        var ingredient = controller.getSelectedCreateFluidIngredient();
        var kind = ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.create.fluid_ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.kind",
                        RecipeEditorUi.selector(
                                List.of(CreateFluidIngredientKind.FLUID, CreateFluidIngredientKind.TAG),
                                kind,
                                value -> Component.translatable("viscript_recipe.editor.create.fluid_ingredient.kind." + value.getSerializedName()),
                                controller::setSelectedCreateFluidIngredientKind
                        ))
        );

        if (kind == CreateFluidIngredientKind.TAG) {
            content.addChildren(
                    createFluidTagConfigurator(ingredient),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.create.fluid_ingredient.amount",
                            RecipeEditorUi.intField(Math.max(1, ingredient.getAmount()), 1, Integer.MAX_VALUE,
                                    controller::setSelectedCreateFluidIngredientAmount))
            );
        } else {
            content.addChild(createFluidStackConfigurator(
                    "viscript_recipe.config.create.fluid_ingredient.fluid",
                    controller::getSelectedFluid,
                    stack -> controller.setSelectedFluid(stack == null ? FluidStack.EMPTY : stack.copy())
            ));
        }
    }

    private UIElement createItemStackConfigurator(String nameKey, Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
        var configurator = new ItemStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? ItemStack.EMPTY : stack.copy();
                },
                stack -> {
                    if (!rebuilding) {
                        consumer.accept(stack == null ? ItemStack.EMPTY : stack.copy());
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private UIElement createBlockConfigurator(String nameKey, Supplier<Block> supplier, Consumer<Block> consumer) {
        var configurator = new BlockAccessor().create(
                nameKey,
                () -> {
                    var block = supplier.get();
                    return block == null ? Blocks.AIR : block;
                },
                block -> {
                    if (!rebuilding) {
                        consumer.accept(block == null ? Blocks.AIR : block);
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        return configurator;
    }

    private UIElement createFluidStackConfigurator(String nameKey, Supplier<FluidStack> supplier, Consumer<FluidStack> consumer) {
        var configurator = new FluidStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? FluidStack.EMPTY : stack.copy();
                },
                stack -> {
                    if (!rebuilding) {
                        consumer.accept(stack == null ? FluidStack.EMPTY : stack.copy());
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private UIElement createItemTagConfigurator(RecipeIngredient ingredient, RecipeIngredientValue value) {
        var configurator = new TagKeySearchComponent.Item(
                "viscript_recipe.config.ingredient.value.tag",
                () -> itemTag(value.getTag()),
                tag -> {
                    if (!rebuilding) {
                        ingredient.getValues().clear();
                        value.setKind(IngredientValueKind.TAG);
                        value.setTag(tag.location());
                        ingredient.getValues().add(value);
                        controller.setSelectedIngredient(ingredient);
                    }
                },
                itemTag(defaultTag()),
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private UIElement createFluidTagConfigurator(CreateFluidIngredientData ingredient) {
        var configurator = new TagKeySearchComponent.Fluid(
                "viscript_recipe.config.create.fluid_ingredient.tag",
                () -> fluidTag(ingredient.getTag()),
                tag -> {
                    if (!rebuilding) {
                        controller.setSelectedCreateFluidIngredientTag(tag.location());
                    }
                },
                fluidTag(defaultFluidTag()),
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private UIElement createItemAbilityConfigurator(RecipeIngredient ingredient, RecipeIngredientValue value) {
        return RecipeEditorUi.fieldGroup(
                "viscript_recipe.config.ingredient.value.item_ability",
                RecipeEditorUi.selector(
                        controller.itemAbilityChoices(),
                        itemAbilityValue(value),
                        controller::itemAbilityDisplayName,
                        itemAbility -> {
                            if (!rebuilding) {
                                ingredient.getValues().clear();
                                value.setKind(IngredientValueKind.ITEM_ABILITY);
                                value.setItemAbility(itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility);
                                ingredient.getValues().add(value);
                                controller.setSelectedIngredient(ingredient);
                            }
                        }
                )
        );
    }

    private TagKey<Item> itemTag(ResourceLocation tag) {
        return TagKey.create(Registries.ITEM, tag == null ? defaultTag() : tag);
    }

    private TagKey<Fluid> fluidTag(ResourceLocation tag) {
        return TagKey.create(Registries.FLUID, tag == null ? defaultFluidTag() : tag);
    }

    private ResourceLocation defaultTag() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "planks");
    }

    private ResourceLocation defaultFluidTag() {
        return ResourceLocation.fromNamespaceAndPath("c", "water");
    }

    private int opaqueRgb(Integer color) {
        return 0xFF000000 | toRgb(color);
    }

    private int toRgb(Integer color) {
        return color == null ? 0xFFFFFF : color & 0xFFFFFF;
    }

    private Fluid fluidFromRegistry(ResourceLocation id) {
        if (id == null) {
            return Fluids.WATER;
        }
        var fluid = BuiltInRegistries.FLUID.get(id);
        return fluid == null || fluid == Fluids.EMPTY ? Fluids.WATER : fluid;
    }

    private ResourceLocation fluidRegistryName(Fluid fluid) {
        var id = BuiltInRegistries.FLUID.getKey(fluid == null || fluid == Fluids.EMPTY ? Fluids.WATER : fluid);
        return id == null ? ResourceLocation.withDefaultNamespace("water") : id;
    }

    private ItemStack normalizeResultStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Math.max(0, Math.min(99, copy.getCount())));
        return copy;
    }

    private void setIngredientKind(RecipeIngredient ingredient, RecipeIngredientValue currentValue, IngredientValueKind kind) {
        var updated = new RecipeIngredientValue().setKind(kind);
        ingredient.getValues().clear();
        if (kind == IngredientValueKind.ITEM) {
            var stack = currentValue.getItem() == null ? ItemStack.EMPTY : currentValue.getItem().copyWithCount(1);
            if (!stack.isEmpty()) {
                updated.setItem(stack);
                ingredient.getValues().add(updated);
            }
        } else if (kind == IngredientValueKind.TAG) {
            updated.setTag(currentValue.getTag() == null
                    ? ResourceLocation.fromNamespaceAndPath("minecraft", "planks")
                    : currentValue.getTag());
            ingredient.getValues().add(updated);
        } else if (kind == IngredientValueKind.ITEM_ABILITY) {
            updated.setItemAbility(itemAbilityValue(currentValue));
            ingredient.getValues().add(updated);
        }
        controller.setSelectedIngredient(ingredient);
    }

    private ItemStack ingredientItemStack(RecipeIngredient ingredient) {
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null) {
                return value.getItem().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private Block ingredientBlock(RecipeIngredient ingredient) {
        var stack = ingredientItemStack(ingredient);
        return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : Blocks.AIR;
    }

    private RecipeIngredientValue editableValue(RecipeIngredient ingredient) {
        if (ingredient.getValues().isEmpty()) {
            return new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(ItemStack.EMPTY);
        }
        return ingredient.getValues().getFirst();
    }

    private void setIngredientItem(RecipeIngredient ingredient, ItemStack stack) {
        ingredient.getValues().clear();
        if (stack != null && !stack.isEmpty()) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(stack.copyWithCount(1)));
        }
        controller.setSelectedIngredient(ingredient);
    }

    private void setIngredientBlock(RecipeIngredient ingredient, Block block) {
        ingredient.getValues().clear();
        if (block != null && block != Blocks.AIR && block.asItem() != Items.AIR) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(new ItemStack(block)));
        }
        controller.setSelectedIngredient(ingredient);
    }

    private CraftingRemainderMode remainderMode(CraftingRemainderRule remainder) {
        return remainder.getMode() == null ? CraftingRemainderMode.DEFAULT : remainder.getMode();
    }

    private RecipeIngredient copyIngredient(RecipeIngredient original) {
        var copy = new RecipeIngredient();
        for (var value : original.getValues()) {
            var valueCopy = new RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag())
                    .setItemAbility(value.getItemAbility());
            if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }

    private String itemAbilityValue(RecipeIngredientValue value) {
        var itemAbility = value == null ? null : value.getItemAbility();
        return itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility;
    }
}
