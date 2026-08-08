package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.ui.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.industrial_foregoing.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Builds parameter controls that follow Industrial Foregoing's native recipe codecs. */
final class IndustrialForegoingPropertiesSections {
    private IndustrialForegoingPropertiesSections() {
    }

    /** Builds the properties for the currently clicked Industrial Foregoing fluid slot. */
    static void buildSelectedFluid(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedIndustrialFluidIngredient()) {
            return;
        }
        buildFluidIngredient(content, controller, controller.getSelectedIndustrialFluidIngredient(),
                controller.selectedFluidConfigNameKey());
    }

    /** Adds the dissolution chamber's item-output enable switch and returns whether that output is enabled. */
    static boolean buildDissolutionItemOutputEnabled(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedIndustrialDissolutionItemOutput()) {
            return true;
        }
        var data = controller.getSelectedEntry().getIndustrialDissolution();
        content.addChild(switchField("viscript_recipe.config.industrial_foregoing.dissolution.has_item_output",
                data.isHasItemOutput(), data::setHasItemOutput, controller));
        return data.isHasItemOutput();
    }

    /** Adds the dissolution chamber's fluid-output enable switch and returns whether that output is enabled. */
    static boolean buildDissolutionFluidOutputEnabled(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedIndustrialDissolutionFluidOutput()) {
            return true;
        }
        var data = controller.getSelectedEntry().getIndustrialDissolution();
        content.addChild(switchField("viscript_recipe.config.industrial_foregoing.dissolution.has_fluid_output",
                data.isHasFluidOutput(), data::setHasFluidOutput, controller));
        return data.isHasFluidOutput();
    }

    /** Builds the properties for the currently clicked non-slot Industrial Foregoing canvas component. */
    static void buildSelectedComponent(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedIndustrialComponent() || controller.getSelectedEntry() == null) {
            return;
        }
        var entry = controller.getSelectedEntry();
        switch (controller.getSlotSelection().index()) {
            case RecipeEditorController.INDUSTRIAL_DISSOLUTION_SETTINGS_COMPONENT ->
                    buildDissolutionSettings(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_FLUID_EXTRACTOR_BLOCK_COMPONENT ->
                    buildFluidExtractorBlock(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_FLUID_EXTRACTOR_OPERATION_COMPONENT ->
                    buildFluidExtractorOperation(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_LASER_ORE_RARITY_COMPONENT ->
                    buildLaserOreRarities(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_LASER_ORE_ENTITY_COMPONENT ->
                    buildLaserOreEntityCondition(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_LASER_FLUID_RARITY_COMPONENT ->
                    buildLaserFluidRarities(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_LASER_FLUID_ENTITY_COMPONENT ->
                    buildLaserFluidEntityCondition(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_STONEWORK_NEEDS_COMPONENT ->
                    buildStoneWorkNeeds(content, controller, entry);
            case RecipeEditorController.INDUSTRIAL_STONEWORK_CONSUMES_COMPONENT ->
                    buildStoneWorkConsumes(content, controller, entry);
            default -> {
            }
        }
    }

    static String structureSignature(RecipeEntry entry) {
        if (entry == null) {
            return "";
        }
        if (entry.isType(IndustrialForegoingRecipeEditorTypes.DISSOLUTION_CHAMBER)) {
            var data = entry.getIndustrialDissolution();
            return "d:" + data.isHasItemOutput() + ':' + data.isHasFluidOutput() + ':' + fluidKind(data.getInputFluid());
        }
        if (entry.isType(IndustrialForegoingRecipeEditorTypes.FLUID_EXTRACTOR)) {
            var data = entry.getIndustrialFluidExtractor();
            return "e:" + data.getResultBlock();
        }
        if (entry.isType(IndustrialForegoingRecipeEditorTypes.CRUSHER)) {
            return "c";
        }
        if (entry.isType(IndustrialForegoingRecipeEditorTypes.LASER_DRILL_ORE)) {
            var data = entry.getIndustrialLaserDrillOre();
            return "o:" + entitySignature(data.getEntityCondition());
        }
        if (entry.isType(IndustrialForegoingRecipeEditorTypes.LASER_DRILL_FLUID)) {
            var data = entry.getIndustrialLaserDrillFluid();
            return "f:" + fluidKind(data.getOutput()) + ':' + entitySignature(data.getEntityCondition());
        }
        return entry.isType(IndustrialForegoingRecipeEditorTypes.STONEWORK_GENERATE) ? "s" : "";
    }

    private static void buildDissolutionSettings(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialDissolution();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.dissolution"),
                intField("viscript_recipe.config.industrial_foregoing.processing_time", data.getProcessingTime(), 0,
                        Integer.MAX_VALUE, data::setProcessingTime, controller)
        );
    }

    private static void buildFluidExtractorBlock(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialFluidExtractor();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.fluid_extractor.block"),
                RecipeSearchComponents.block(
                        "viscript_recipe.config.industrial_foregoing.fluid_extractor.result_block",
                        data::getResultBlock,
                        value -> {
                            data.setResultBlock(value);
                            data.setResultProperties(new ArrayList<>());
                        },
                        controller::notifyChanged,
                        Blocks.STRIPPED_OAK_LOG)
        );
        buildBlockStateProperties(content, controller, data.getResultBlock(), data.getResultProperties());
    }

    private static void buildFluidExtractorOperation(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialFluidExtractor();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.fluid_extractor.operation"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.industrial_foregoing.fluid_extractor.break_chance",
                        RecipeEditorUi.floatField(data.getBreakChance(), 0, 1, value -> {
                            data.setBreakChance(value);
                            controller.notifyChanged();
                        })),
                switchField("viscript_recipe.config.industrial_foregoing.fluid_extractor.default_recipe",
                        data.isDefaultRecipe(), data::setDefaultRecipe, controller)
        );
    }

    private static void buildLaserOreRarities(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialLaserDrillOre();
        content.addChild(RecipeEditorUi.sectionTitle(
                "viscript_recipe.editor.properties.industrial_foregoing.laser_drill_ore"));
        buildRarities(content, controller, data::getRarity, data::setRarity);
    }

    private static void buildLaserOreEntityCondition(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        buildEntityCondition(content, controller, entry.getIndustrialLaserDrillOre().getEntityCondition());
    }

    private static void buildLaserFluidRarities(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialLaserDrillFluid();
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.laser_drill_fluid"));
        buildRarities(content, controller, data::getRarity, data::setRarity);
    }

    private static void buildLaserFluidEntityCondition(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        buildEntityCondition(content, controller, entry.getIndustrialLaserDrillFluid().getEntityCondition());
    }

    private static void buildStoneWorkNeeds(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialStoneWork();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.stonework.needs"),
                intField("viscript_recipe.config.industrial_foregoing.stonework.water_need", data.getWaterNeed(), 0,
                        Integer.MAX_VALUE, data::setWaterNeed, controller),
                intField("viscript_recipe.config.industrial_foregoing.stonework.lava_need", data.getLavaNeed(), 0,
                        Integer.MAX_VALUE, data::setLavaNeed, controller)
        );
    }

    private static void buildStoneWorkConsumes(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getIndustrialStoneWork();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.stonework.consumes"),
                intField("viscript_recipe.config.industrial_foregoing.stonework.water_consume", data.getWaterConsume(), 0,
                        Integer.MAX_VALUE, data::setWaterConsume, controller),
                intField("viscript_recipe.config.industrial_foregoing.stonework.lava_consume", data.getLavaConsume(), 0,
                        Integer.MAX_VALUE, data::setLavaConsume, controller)
        );
    }

    private static void buildFluidIngredient(UIElement content, RecipeEditorController controller,
                                             FluidIngredientData data, String titleKey) {
        var kind = fluidKind(data);
        content.addChildren(
                RecipeEditorUi.sectionTitle(titleKey),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.industrial_foregoing.fluid_ingredient.kind",
                        RecipeEditorUi.selector(List.of(FluidIngredientKind.values()), kind,
                                FluidIngredientKind::displayName, value -> {
                                    data.setKind(value);
                                    controller.notifyChanged();
                                }))
        );
        if (kind == FluidIngredientKind.TAG) {
            content.addChild(RecipeSearchComponents.fluidTag(
                    "viscript_recipe.config.industrial_foregoing.fluid_ingredient.tag",
                    data::getTag, data::setTag, controller::notifyChanged));
        } else {
            content.addChild(RecipeSearchComponents.fluid(
                    "viscript_recipe.config.industrial_foregoing.fluid_ingredient.fluid",
                    () -> fluidId(data.getFluid()),
                    id -> data.setFluid(new FluidStack(BuiltInRegistries.FLUID.get(id), Math.max(1, data.getAmount()))),
                    controller::notifyChanged,
                    Fluids.WATER));
        }
        content.addChild(intField("viscript_recipe.config.industrial_foregoing.fluid_ingredient.amount",
                Math.max(1, data.getAmount()), 1, Integer.MAX_VALUE, value -> {
                    data.setAmount(value);
                    if (data.getFluid() != null && !data.getFluid().isEmpty()) {
                        data.setFluid(data.getFluid().copyWithAmount(value));
                    }
                }, controller));
    }

    private static void buildEntityCondition(UIElement content, RecipeEditorController controller,
                                             IndustrialEntityConditionData data) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.industrial_foregoing.entity_condition"),
                switchField("viscript_recipe.config.industrial_foregoing.entity_condition.enabled",
                        data.isEnabled(), data::setEnabled, controller)
        );
        if (!data.isEnabled()) {
            return;
        }
        var kind = data.getKind() == null ? IndustrialEntityIngredientKind.ENTITY : data.getKind();
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.industrial_foregoing.entity_condition.kind",
                RecipeEditorUi.selector(List.of(IndustrialEntityIngredientKind.values()), kind,
                        value -> Component.translatable("viscript_recipe.editor.industrial_foregoing.entity_kind."
                                + value.name().toLowerCase(java.util.Locale.ROOT)),
                        value -> {
                            data.setKind(value);
                            controller.notifyChanged();
                        })));
        if (kind == IndustrialEntityIngredientKind.TAG) {
            content.addChild(RecipeSearchComponents.entityTag(
                    "viscript_recipe.config.industrial_foregoing.entity_condition.id",
                    data::getId, data::setId, controller::notifyChanged));
        } else {
            content.addChild(RecipeSearchComponents.entityType(
                    "viscript_recipe.config.industrial_foregoing.entity_condition.id",
                    data::getId, data::setId, controller::notifyChanged, EntityType.WITHER));
        }
        content.addChildren(
                RecipeEditorUi.fieldGroup("viscript_recipe.config.industrial_foregoing.entity_condition.nbt",
                        RecipeEditorUi.textField(data.getNbt(), value -> {
                            data.setNbt(value);
                            controller.notifyChanged();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.industrial_foregoing.entity_condition.display",
                        RecipeEditorUi.textField(data.getDisplay(), value -> {
                            data.setDisplay(value);
                            controller.notifyChanged();
                        }))
        );
    }

    private static void buildBlockStateProperties(UIElement content, RecipeEditorController controller,
                                                  ResourceLocation blockId,
                                                  List<IndustrialBlockStatePropertyData> stored) {
        var block = blockId == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(blockId);
        if (block == Blocks.AIR || block.getStateDefinition().getProperties().isEmpty()) {
            return;
        }
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.config.industrial_foregoing.block_state.properties"));
        for (var property : block.getStateDefinition().getProperties()) {
            var current = stored.stream().filter(row -> row != null && property.getName().equals(row.getName()))
                    .findFirst().map(IndustrialBlockStatePropertyData::getValue)
                    .filter(value -> property.getValue(value).isPresent())
                    .orElseGet(() -> defaultPropertyValueName(block.defaultBlockState(), property));
            var candidates = property.getPossibleValues().stream()
                    .map(value -> propertyValueName(property, value)).toList();
            content.addChild(RecipeEditorUi.fieldGroup(
                    "viscript_recipe.config.industrial_foregoing.block_state.property",
                    RecipeEditorUi.selector(candidates, current, Component::literal, value -> {
                        setBlockProperty(stored, property.getName(), value);
                        controller.notifyChanged();
                    }),
                    Component.literal(property.getName())));
        }
    }

    private static void setBlockProperty(List<IndustrialBlockStatePropertyData> stored, String name, String value) {
        for (var row : stored) {
            if (row != null && name.equals(row.getName())) {
                row.setValue(value);
                return;
            }
        }
        stored.add(new IndustrialBlockStatePropertyData().setName(name).setValue(value));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValueName(net.minecraft.world.level.block.state.properties.Property property,
                                            Comparable value) {
        return property.getName(value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String defaultPropertyValueName(net.minecraft.world.level.block.state.BlockState state,
                                                   net.minecraft.world.level.block.state.properties.Property property) {
        return property.getName(state.getValue(property));
    }

    private static void buildRarities(UIElement content, RecipeEditorController controller,
                                      Supplier<List<IndustrialLaserDrillRarityData>> source,
                                      Consumer<List<IndustrialLaserDrillRarityData>> setter) {
        ensureRarities(source, setter);
        var rules = new ArrayConfiguratorGroup<IndustrialLaserDrillRarityData>(
                "viscript_recipe.config.industrial_foregoing.laser.rarity",
                false,
                () -> new ArrayList<>(ensureRarities(source, setter)),
                (getter, rowSetter) -> createRarityRuleConfigurator(getter, rowSetter, source, setter, controller),
                true
        );
        rules.setAddDefault(IndustrialLaserDrillRarityData::new);
        rules.setCanRemove(rarity -> ensureRarities(source, setter).size() > 1);
        rules.setCanReorder(true);
        rules.setOnUpdate(updated -> {
            setter.accept(new ArrayList<>(updated));
            updateRarityRuleLabels(rules);
            controller.notifyChanged();
        });
        content.addChild(rules.layout(layout -> layout.widthPercent(100)));
    }

    private static Configurator createRarityRuleConfigurator(
            Supplier<IndustrialLaserDrillRarityData> getter,
            Consumer<IndustrialLaserDrillRarityData> setter,
            Supplier<List<IndustrialLaserDrillRarityData>> source,
            Consumer<List<IndustrialLaserDrillRarityData>> sourceSetter,
            RecipeEditorController controller
    ) {
        var rarity = getter.get();
        if (rarity == null) {
            rarity = new IndustrialLaserDrillRarityData();
            setter.accept(rarity);
        }
        var ruleIndex = Math.max(0, ensureRarities(source, sourceSetter).indexOf(rarity)) + 1;
        var group = new ConfiguratorGroup("");
        group.setLabel(Component.translatable("viscript_recipe.editor.industrial_foregoing.rarity.title", ruleIndex));
        group.setCollapse(true);
        group.configuratorContainer.addChildren(
                intField("viscript_recipe.config.industrial_foregoing.rarity.depth_min", rarity.getDepthMin(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, rarity::setDepthMin, controller),
                intField("viscript_recipe.config.industrial_foregoing.rarity.depth_max", rarity.getDepthMax(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, rarity::setDepthMax, controller),
                intField("viscript_recipe.config.industrial_foregoing.rarity.weight", rarity.getWeight(),
                        1, Integer.MAX_VALUE, rarity::setWeight, controller)
        );
        buildResourceList(group.configuratorContainer, controller, rarity::getBiomeWhitelist, rarity::setBiomeWhitelist,
                "viscript_recipe.config.industrial_foregoing.rarity.biome_whitelist", true);
        buildResourceList(group.configuratorContainer, controller, rarity::getBiomeBlacklist, rarity::setBiomeBlacklist,
                "viscript_recipe.config.industrial_foregoing.rarity.biome_blacklist", true);
        buildResourceList(group.configuratorContainer, controller, rarity::getDimensionWhitelist, rarity::setDimensionWhitelist,
                "viscript_recipe.config.industrial_foregoing.rarity.dimension_whitelist", false);
        buildResourceList(group.configuratorContainer, controller, rarity::getDimensionBlacklist, rarity::setDimensionBlacklist,
                "viscript_recipe.config.industrial_foregoing.rarity.dimension_blacklist", false);
        return group;
    }

    private static void buildResourceList(UIElement content, RecipeEditorController controller,
                                          Supplier<List<ResourceLocation>> source,
                                          Consumer<List<ResourceLocation>> setter,
                                          String key, boolean biomeTag) {
        var filters = new ArrayConfiguratorGroup<ResourceLocation>(
                key,
                true,
                () -> new ArrayList<>(ensureResourceValues(source, setter)),
                (getter, rowSetter) -> createResourceFilterConfigurator(getter, rowSetter, biomeTag),
                true
        );
        filters.setAddDefault(() -> defaultResourceFilter(biomeTag));
        filters.setCanReorder(true);
        filters.setOnUpdate(updated -> {
            setter.accept(new ArrayList<>(updated));
            controller.notifyChanged();
        });
        content.addChild(filters.layout(layout -> layout.widthPercent(100)));
    }

    private static Configurator createResourceFilterConfigurator(Supplier<ResourceLocation> getter,
                                                                  Consumer<ResourceLocation> setter,
                                                                  boolean biomeTag) {
        var row = new Configurator("");
        var input = biomeTag
                ? RecipeSearchComponents.biomeTag("", getter, setter, () -> { })
                : RecipeSearchComponents.dimensionType("", getter, setter, () -> { });
        input.layout(layout -> layout.widthPercent(100));
        return row.addInlineChild(input);
    }

    private static List<IndustrialLaserDrillRarityData> ensureRarities(
            Supplier<List<IndustrialLaserDrillRarityData>> source,
            Consumer<List<IndustrialLaserDrillRarityData>> setter
    ) {
        var rarities = source.get();
        if (rarities == null || rarities.isEmpty()) {
            var defaultRarities = new ArrayList<IndustrialLaserDrillRarityData>();
            defaultRarities.add(new IndustrialLaserDrillRarityData());
            setter.accept(defaultRarities);
            return defaultRarities;
        }
        return rarities;
    }

    private static List<ResourceLocation> ensureResourceValues(Supplier<List<ResourceLocation>> source,
                                                                Consumer<List<ResourceLocation>> setter) {
        var values = source.get();
        if (values == null) {
            values = new ArrayList<>();
            setter.accept(values);
        }
        return values;
    }

    private static ResourceLocation defaultResourceFilter(boolean biomeTag) {
        var candidates = biomeTag
                ? RecipeRegistryClientData.biomeTags().keySet().stream().toList()
                : RecipeRegistryClientData.dimensionTypes();
        return candidates.isEmpty()
                ? ResourceLocation.withDefaultNamespace(biomeTag ? "is_overworld" : "overworld")
                : candidates.getFirst();
    }

    private static UIElement intField(String key, int value, int min, int max, Consumer<Integer> setter,
                                      RecipeEditorController controller) {
        return RecipeEditorUi.fieldGroup(key, RecipeEditorUi.intField(value, min, max, updated -> {
            setter.accept(updated);
            controller.notifyChanged();
        }));
    }

    private static UIElement switchField(String key, boolean value, Consumer<Boolean> setter,
                                         RecipeEditorController controller) {
        return RecipeEditorUi.fieldGroup(key, new Switch().setOn(value, false).setOnSwitchChanged(updated -> {
            setter.accept(updated);
            controller.notifyChanged();
        }));
    }

    private static FluidIngredientKind fluidKind(FluidIngredientData data) {
        return data == null || data.getKind() == null ? FluidIngredientKind.FLUID : data.getKind();
    }

    private static ResourceLocation fluidId(FluidStack stack) {
        var fluid = stack == null || stack.isEmpty() ? Fluids.WATER : stack.getFluid();
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    private static String entitySignature(IndustrialEntityConditionData data) {
        return data == null ? "none" : data.isEnabled() + ":" + data.getKind();
    }

    private static void updateRarityRuleLabels(ArrayConfiguratorGroup<IndustrialLaserDrillRarityData> rules) {
        for (int index = 0; index < rules.getConfigurators().size(); index++) {
            var configurator = rules.getConfigurators().get(index);
            if (configurator instanceof ArrayConfiguratorGroup<?>.ItemConfigurator item
                    && item.inner instanceof ConfiguratorGroup group) {
                group.setLabel(Component.translatable("viscript_recipe.editor.industrial_foregoing.rarity.title", index + 1));
            }
        }
    }
}
