package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.accessors.BlockAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.FluidStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.create.CreateFluidIngredientData;
import com.viscript_recipe.data.create.CreateFluidIngredientKind;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.data.confluence.ConfluenceHolderSetData;
import com.viscript_recipe.data.confluence.ConfluenceHolderSetKind;
import com.viscript_recipe.data.confluence.ConfluenceRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyBrewingEntityKind;
import com.viscript_recipe.data.goety.GoetyPulverizeResultKind;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientKind;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
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
                case RESULT -> buildResultProperties(entry);
                case CONTAINER -> buildContainerProperties();
                case CUTTING_RESULT -> buildCuttingResultProperties();
                case CREATE_RESULT -> buildCreateResultProperties();
                case CREATE_TRANSITIONAL -> buildCreateSequencedTransitionalProperties();
                case CREATE_SEQUENCED_STEP -> buildCreateSequencedStepProperties(entry);
                case ARS_NOUVEAU_OUTPUT -> buildArsNouveauOutputProperties();
                case INDUSTRIAL_COMPONENT -> buildIndustrialComponentProperties();
                case MEKANISM_CHEMICAL -> buildMekanismChemicalProperties();
                case MEKANISM_FLUID -> buildMekanismFluidProperties();
                case MEKANISM_ITEM -> buildMekanismItemProperties();
                case MYSTICAL_ESSENCE -> buildMysticalEssenceProperties();
                case KALEIDOSCOPE_FLUID -> buildKaleidoscopeFluidProperties();
                case KALEIDOSCOPE_SOUP_BASE -> buildKaleidoscopeSoupBaseProperties();
                case CONFLUENCE_TARGET -> buildConfluenceTargetProperties();
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
                selectedCreateFluidIngredientSignature(),
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
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasSacrifice(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasSummon(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasConversion(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasStructure(),
                entry != null && controller.isGoetyRitualEntry(entry) && entry.getGoetyRitual().isHasEnchantment(),
                entry != null && controller.isGoetyBrewingEntry(entry)
                        ? entry.getGoetyBrewing().getEntityKind()
                        : null,
                entry != null && controller.isGoetyPulverizeEntry(entry)
                        ? entry.getGoetyPulverize().getResultKind()
                        : null,
                controller.isSelectedExtendedCraftingCompressorInput(),
                controller.isSelectedCreateFluidInput(),
                controller.isSelectedCreateFluidOutput(),
                controller.selectedCreateDurationAllowed(),
                controller.selectedCreateHeatAllowed(),
                controller.selectedCreateKeepHeldItemAllowed(),
                controller.selectedCreateOutputChanceAllowed(),
                controller.selectedCreateCountedInputSignature(),
                createSequencedStructureSignature(entry),
                IndustrialForegoingPropertiesSections.structureSignature(entry),
                MekanismPropertiesSections.structureSignature(entry),
                selectedMekanismChemicalIngredientKind(),
                selectedMekanismChemicalOutputSignature(),
                mysticalAgricultureSpawnerEntityCount(entry),
                confluenceStructureSignature(entry)
        );
    }

    private String confluenceStructureSignature(RecipeEntry entry) {
        if (entry == null || !controller.isConfluenceEntry(entry)) {
            return "";
        }
        var data = entry.getConfluence();
        var builder = new StringBuilder(entry.getType().toString());
        if (ConfluenceRecipeEditorTypes.isEitherType(entry.getType())) {
            builder.append("|mode=").append(data.getCraftingMode());
        }
        if (ConfluenceRecipeEditorTypes.isEnvironmentType(entry.getType())) {
            var environment = data.getEnvironment();
            builder.append("|blocks=").append(holderKind(environment == null ? null : environment.getBlocks()));
            builder.append("|fluids=").append(holderKind(environment == null ? null : environment.getFluids()));
        }
        if (ConfluenceRecipeEditorTypes.COOKING_POT.equals(entry.getType())) {
            var heat = data.getHeatSource();
            builder.append("|heat=").append(holderKind(heat == null ? null : heat.getBlocks()));
        }
        return builder.toString();
    }

    private static ConfluenceHolderSetKind holderKind(ConfluenceHolderSetData data) {
        return data == null || data.getKind() == null ? ConfluenceHolderSetKind.NONE : data.getKind();
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

    private int mysticalAgricultureSpawnerEntityCount(RecipeEntry entry) {
        if (entry == null || !controller.isMysticalAgricultureSouliumSpawnerEntry(entry)
                || entry.getMysticalAgricultureSouliumSpawner().getEntities() == null) {
            return 0;
        }
        return entry.getMysticalAgricultureSouliumSpawner().getEntities().size();
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

    private String selectedCreateFluidIngredientSignature() {
        if (!controller.isSelectedCreateFluidInput()) {
            return "";
        }
        var ingredient = controller.getSelectedCreateFluidIngredient();
        var kind = ingredient.getKind() == null ? CreateFluidIngredientKind.FLUID : ingredient.getKind();
        if (kind == CreateFluidIngredientKind.TAG) {
            return "tag:" + ingredient.getTag() + ":" + Math.max(1, ingredient.getAmount());
        }
        var stack = ingredient.getFluid();
        if (stack == null || stack.isEmpty()) {
            return "fluid:empty";
        }
        var fluidId = stack.getFluidHolder().unwrapKey().map(Object::toString).orElse(stack.getFluid().toString());
        return "fluid:" + fluidId + ":" + stack.getAmount();
    }

    private MekanismChemicalIngredientKind selectedMekanismChemicalIngredientKind() {
        if (!controller.isSelectedMekanismChemicalIngredient()) {
            return null;
        }
        var ingredient = controller.getSelectedMekanismChemicalIngredient();
        return ingredient.getKind() == null ? MekanismChemicalIngredientKind.CHEMICAL : ingredient.getKind();
    }

    private String selectedMekanismChemicalOutputSignature() {
        if (!controller.isSelectedMekanismChemicalSlot() || controller.isSelectedMekanismChemicalIngredient()) {
            return "";
        }
        var output = controller.getSelectedMekanismChemicalOutput();
        return output.getChemical() + ":" + output.getAmount();
    }

    private record PropertiesStructureKey(
            int entryIdentity,
            ResourceLocation selectedCategory,
            ResourceLocation type,
            WorkbenchSlotSelection selection,
            IngredientValueKind ingredientKind,
            CraftingRemainderMode remainderMode,
            CreateFluidIngredientKind createFluidIngredientKind,
            String createFluidIngredientSignature,
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
            boolean goetyRitualSacrifice,
            boolean goetyRitualSummon,
            boolean goetyRitualConversion,
            boolean goetyRitualStructure,
            boolean goetyRitualEnchantment,
            GoetyBrewingEntityKind goetyBrewingEntityKind,
            GoetyPulverizeResultKind goetyPulverizeResultKind,
            boolean selectedExtendedCraftingCompressorInput,
            boolean selectedCreateFluidInput,
            boolean selectedCreateFluidOutput,
            boolean createDurationAllowed,
            boolean createHeatAllowed,
            boolean createKeepHeldItemAllowed,
            boolean createOutputChanceAllowed,
            int createCountedInputSignature,
            String createSequencedSignature,
            String industrialForegoingSignature,
            String mekanismSignature,
            MekanismChemicalIngredientKind mekanismChemicalIngredientKind,
            String mekanismChemicalOutputSignature,
            int mysticalAgricultureSpawnerEntityCount,
            String confluenceStructureSignature
    ) {
    }

    private void buildRecipeProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.recipe"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.type",
                        RecipeEditorUi.selector(
                                controller.availableTypesForSelectedCategory(),
                                controller.getSelectedRecipeType(),
                                RecipeEditorType::displayName,
                                controller::setSelectedRecipeType
                        )),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.enabled",
                        new Switch()
                                .setOn(entry.isEnabled(), false)
                                .setOnSwitchChanged(value -> {
                                    entry.setEnabled(value);
                                    controller.notifyChanged();
                                })),
                RecipeSearchComponents.recipeId("viscript_recipe.config.entry.recipe_id",
                        entry::getRecipeId,
                        entry::setRecipeId,
                        controller::notifyChanged),
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
            RecipePropertiesSections.buildCooking(content, controller, entry);
        }
        if (controller.isConfluenceEntry(entry)) {
            ConfluencePropertiesSections.build(content, controller, entry);
        }
        if (controller.isAlloySmelterEntry(entry)) {
            var data = entry.getAlloySmelter();
            content.addChildren(
                    RecipeEditorUi.sectionTitle("viscript_recipe.editor.category.alloy_smelter.smelting"),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.smelting_time",
                            RecipeEditorUi.intField(Math.max(0, data.getSmeltingTime()), 0, Integer.MAX_VALUE,
                                    value -> { data.setSmeltingTime(value); controller.notifyChanged(); })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.fuel_per_tick",
                            RecipeEditorUi.intField(Math.max(0, data.getFuelPerTick()), 0, Integer.MAX_VALUE,
                                    value -> { data.setFuelPerTick(value); controller.notifyChanged(); })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.required_tier",
                            RecipeEditorUi.intField(Math.clamp(data.getRequiredTier(), 1, 3), 1, 3,
                                    value -> { data.setRequiredTier(value); controller.notifyChanged(); })));
        }
        if (controller.isFarmersCookingPotEntry(entry)) {
            RecipePropertiesSections.buildFarmersCooking(content, controller, entry);
        }
        if (controller.isFarmersCuttingBoardEntry(entry)) {
            RecipePropertiesSections.buildFarmersCutting(content, controller, entry);
        }
        if (controller.isIronAlchemistCauldronEntry(entry)) {
            RecipePropertiesSections.buildAlchemistCauldron(content, controller, entry);
        }
        if (controller.isIceAndFireDragonForgeEntry(entry)) {
            RecipePropertiesSections.buildDragonForge(content, controller, entry);
        }
        if (controller.isCataclysmAmethystBlessEntry(entry)) {
            RecipePropertiesSections.buildCataclysmAmethystBless(content, controller, entry);
        }
        if (controller.isTouhouLittleMaidAltarEntry(entry)) {
            RecipePropertiesSections.buildTouhouLittleMaidAltar(content, controller, entry);
        }
        if (controller.isGoetyCursedInfuserEntry(entry)) {
            RecipePropertiesSections.buildGoetyCursedInfuser(content, controller, entry);
        }
        if (controller.isGoetyRitualEntry(entry)) {
            RecipePropertiesSections.buildGoetyRitual(content, controller, entry);
        }
        if (controller.isGoetyBrazierEntry(entry)) {
            RecipePropertiesSections.buildGoetyBrazier(content, controller, entry);
        }
        if (controller.isGoetyBrewingEntry(entry)) {
            RecipePropertiesSections.buildGoetyBrewing(content, controller, entry);
        }
        if (controller.isMysticalAgricultureInfusionEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureInfusion(content, controller, entry);
        }
        if (controller.isMysticalAgricultureAwakeningEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureAwakening(content, controller, entry);
        }
        if (controller.isMysticalAgricultureEnchanterEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureEnchanter(content, controller, entry);
        }
        if (controller.isMysticalAgricultureReprocessorEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureReprocessor(content, controller, entry);
        }
        if (controller.isMysticalAgricultureSoulExtractionEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureSoulExtraction(content, controller, entry);
        }
        if (controller.isMysticalAgricultureSouliumSpawnerEntry(entry)) {
            RecipePropertiesSections.buildMysticalAgricultureSouliumSpawner(content, controller, entry);
        }
        if (controller.isCreateMechanicalCraftingEntry(entry)) {
            RecipePropertiesSections.buildCreateMechanicalCrafting(content, controller, entry);
        }
        if (controller.isCreateSequencedAssemblyEntry(entry)) {
            RecipePropertiesSections.buildCreateSequencedAssembly(content, controller, entry);
        }
        if (controller.isCreateProcessingEntry(entry)) {
            RecipePropertiesSections.buildCreateProcessing(content, controller, entry);
        }
        if (controller.isExtendedCraftingTableEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingTable(content, controller, entry);
        }
        if (controller.isExtendedCraftingEnderCrafterEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingEnderCrafter(content, controller, entry);
        }
        if (controller.isExtendedCraftingFluxCrafterEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingFluxCrafter(content, controller, entry);
        }
        if (controller.isExtendedCraftingCombinationEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingCombination(content, controller, entry);
        }
        if (controller.isExtendedCraftingCompressorEntry(entry)) {
            RecipePropertiesSections.buildExtendedCraftingCompressor(content, controller, entry);
        }
        if (controller.isAvaritiaTableEntry(entry)) {
            RecipePropertiesSections.buildAvaritiaTable(content, controller, entry);
        }
        if (controller.isAvaritiaSpecialShapelessEntry(entry)) {
            RecipePropertiesSections.buildAvaritiaSpecialShapeless(content, controller, entry);
        }
        if (controller.isAvaritiaCompressorEntry(entry)) {
            RecipePropertiesSections.buildAvaritiaCompressor(content, controller, entry);
        }
        if (controller.isArsNouveauApparatusLayoutEntry(entry) || controller.isArsNouveauImbuementLayoutEntry(entry) || controller.isArsNouveauGlyphLayoutEntry(entry)) {
            RecipePropertiesSections.buildArsNouveauApparatus(content, controller, entry, () -> rebuilding);
        }
        if (controller.isArsNouveauCrushEntry(entry)) {
            RecipePropertiesSections.buildArsNouveauCrush(content, controller, entry);
        }
        if (controller.isKaleidoscopePotEntry(entry)
                || controller.isKaleidoscopeStockpotEntry(entry)
                || controller.isKaleidoscopeMillstoneEntry(entry)
                || controller.isKaleidoscopeChoppingBoardEntry(entry)
                || controller.isKaleidoscopeSteamerEntry(entry)
                || controller.isKaleidoscopeTeapotEntry(entry)) {
            RecipePropertiesSections.buildKaleidoscope(content, controller, entry, () -> rebuilding);
        }
        MekanismPropertiesSections.build(content, controller, entry);
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
            if (controller.isSelectedMekanismItemInput()) {
                content.addChild(RecipeEditorUi.fieldGroup(controller.selectedMekanismItemInputAmountKey(),
                        RecipeEditorUi.intField(controller.getSelectedMekanismItemInputAmount(), 1, Integer.MAX_VALUE,
                                controller::setSelectedMekanismItemInputAmount)));
            }
        } else if (selectedKind == IngredientValueKind.ITEM_ABILITY) {
            content.addChild(createItemAbilityConfigurator(ingredient, value));
        }
        if (controller.isSelectedExtendedCraftingCompressorInput()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.extendedcrafting.counted_ingredient.count",
                    RecipeEditorUi.intField(controller.getSelectedExtendedCraftingCompressorInputCount(), 1, Integer.MAX_VALUE,
                            controller::setSelectedExtendedCraftingCompressorInputCount)));
        }
        if (controller.isConfluenceEntry(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.ingredient.count",
                    RecipeEditorUi.intField(controller.getSelectedConfluenceIngredientCount(), 1, Integer.MAX_VALUE,
                            controller::setSelectedConfluenceIngredientCount)));
        }
        if (controller.isSelectedAlloySmelterMaterial()) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.material.count",
                    RecipeEditorUi.intField(controller.getSelectedAlloySmelterMaterialCount(), 1, Integer.MAX_VALUE,
                            controller::setSelectedAlloySmelterMaterialCount)));
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            buildRemainderProperties();
        }
        RecipePropertiesSections.buildSelectedIngredientSlot(content, controller, entry);
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

    private void buildResultProperties(RecipeEntry entry) {
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.result"));
        if (!IndustrialForegoingPropertiesSections.buildDissolutionItemOutputEnabled(content, controller)) {
            return;
        }
        if (!MekanismPropertiesSections.buildSelectedReactionItemOutputEnabled(content, controller)) {
            return;
        }
        if (controller.isGoetyPulverizeEntry(entry)
                && !RecipePropertiesSections.buildSelectedGoetyPulverizeResult(content, controller, entry)) {
            return;
        }
        if (!RecipePropertiesSections.buildSelectedAvaritiaSpecialResult(content, controller, entry)) {
            return;
        }
        content.addChild(createItemStackConfigurator(
                "viscript_recipe.config.recipe.result",
                controller::getSelectedResult,
                stack -> controller.setSelectedResult(normalizeResultStack(stack))
        ));
    }

    private void buildConfluenceTargetProperties() {
        ConfluencePropertiesSections.buildTarget(content, controller);
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

    private void buildCreateSequencedStepProperties(RecipeEntry entry) {
        RecipePropertiesSections.buildSelectedCreateSequencedStep(content, controller, entry, () -> buildRecipeProperties(entry));
        var selection = controller.getSlotSelection();
        if (controller.isCreateSequencedAssemblyEntry(entry)
                && selection.kind() == WorkbenchSlotSelection.Kind.CREATE_SEQUENCED_STEP
                && selection.index() >= 0
                && selection.index() < controller.selectedCreateSequencedStepCount()
                && controller.getCreateSequencedStepKind(entry, selection.index()) == CreateSequencedAssemblyStepKind.FILLING) {
            buildCreateFluidIngredientProperties();
        }
    }

    private void buildContainerProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle(controller.isConfluenceEntry(controller.getSelectedEntry())
                        ? "viscript_recipe.config.confluence.container"
                        : "viscript_recipe.editor.properties.farmersdelight.container"),
                createItemStackConfigurator(
                        controller.isConfluenceEntry(controller.getSelectedEntry())
                                ? "viscript_recipe.config.confluence.container"
                                : "viscript_recipe.config.farmersdelight.cooking.container",
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
        if (controller.isSelectedIndustrialFluidIngredient()) {
            IndustrialForegoingPropertiesSections.buildSelectedFluid(content, controller);
            return;
        }
        if (controller.isSelectedCreateFluidInput()) {
            buildCreateFluidIngredientProperties();
            return;
        }
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.fluid"));
        if (!IndustrialForegoingPropertiesSections.buildDissolutionFluidOutputEnabled(content, controller)) {
            return;
        }
        content.addChild(createFluidStackConfigurator(
                controller.selectedFluidConfigNameKey(),
                controller::getSelectedFluid,
                stack -> controller.setSelectedFluid(stack == null ? FluidStack.EMPTY : stack.copy())
        ));
    }

    private void buildIndustrialComponentProperties() {
        IndustrialForegoingPropertiesSections.buildSelectedComponent(content, controller);
    }

    private void buildMekanismChemicalProperties() {
        MekanismPropertiesSections.buildSelectedChemical(content, controller);
    }

    private void buildMekanismFluidProperties() {
        MekanismPropertiesSections.buildSelectedFluid(content, controller);
    }

    private void buildMekanismItemProperties() {
        MekanismPropertiesSections.buildSelectedItem(content, controller);
    }

    private void buildMysticalEssenceProperties() {
        if (!controller.isSelectedMysticalAgricultureEssenceSlot()) {
            return;
        }
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.mysticalagriculture.awakening.essences"),
                createItemStackConfigurator(
                        "viscript_recipe.config.mysticalagriculture.awakening.essences",
                        controller::getSelectedMysticalAgricultureEssence,
                        controller::setSelectedMysticalAgricultureEssence
                )
        );
    }

    private void buildKaleidoscopeFluidProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.kaleidoscope_cookery.tea_fluid"),
                RecipeSearchComponents.fluid(
                        "viscript_recipe.config.kaleidoscope_cookery.tea_fluid",
                        controller::getSelectedKaleidoscopeTeapotFluid,
                        controller::setSelectedKaleidoscopeTeapotFluid,
                        () -> { },
                        Fluids.WATER
                )
        );
    }

    private void buildKaleidoscopeSoupBaseProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.kaleidoscope_cookery.soup_base"),
                RecipeSearchComponents.soupBase(
                        "viscript_recipe.config.kaleidoscope_cookery.soup_base",
                        controller::getSelectedKaleidoscopeStockpotSoupBase,
                        controller::setSelectedKaleidoscopeStockpotSoupBase,
                        () -> { }
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
        return RecipeSearchComponents.itemTag(
                "viscript_recipe.config.ingredient.value.tag",
                value::getTag,
                tagId -> {
                    if (!rebuilding) {
                        ingredient.getValues().clear();
                        value.setKind(IngredientValueKind.TAG);
                        value.setTag(tagId);
                        ingredient.getValues().add(value);
                        controller.setSelectedIngredient(ingredient);
                    }
                },
                () -> { }
        );
    }

    private UIElement createFluidTagConfigurator(CreateFluidIngredientData ingredient) {
        return RecipeSearchComponents.fluidTag(
                "viscript_recipe.config.create.fluid_ingredient.tag",
                ingredient::getTag,
                tagId -> {
                    if (!rebuilding) {
                        controller.setSelectedCreateFluidIngredientTag(tagId);
                    }
                },
                () -> { }
        );
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

    private ItemStack normalizeResultStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Math.clamp(copy.getCount(), 0, 99));
        return copy;
    }

    private void setIngredientKind(RecipeIngredient ingredient, RecipeIngredientValue currentValue, IngredientValueKind kind) {
        var updated = new RecipeIngredientValue().setKind(kind);
        ingredient.getValues().clear();
        if (kind == IngredientValueKind.ITEM) {
            var stack = controller.normalizeSelectedIngredientItemStack(currentValue.getItem());
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
        var normalizedStack = controller.normalizeSelectedIngredientItemStack(stack);
        if (!normalizedStack.isEmpty()) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(normalizedStack));
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
