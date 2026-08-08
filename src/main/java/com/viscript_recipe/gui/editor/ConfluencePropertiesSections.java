package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.confluence.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/** Global, type-aware Confluence fields; individual item slots remain edited from the canvas. */
final class ConfluencePropertiesSections {
    private ConfluencePropertiesSections() {}

    static void build(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var data = entry.getConfluence();
        var type = entry.getType();
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.confluence"));
        if (ConfluenceRecipeEditorTypes.isEitherType(type)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.crafting_mode", RecipeEditorUi.selector(
                    List.of(ConfluenceCraftingMode.SHAPED, ConfluenceCraftingMode.SHAPELESS),
                    data.getCraftingMode() == null ? ConfluenceCraftingMode.SHAPED : data.getCraftingMode(),
                    value -> Component.translatable("viscript_recipe.editor.confluence.mode." + value.getSerializedName()),
                    value -> { data.setCraftingMode(value); controller.notifyChanged(); })));
            if (data.getCraftingMode() != ConfluenceCraftingMode.SHAPELESS) {
                content.addChildren(
                        RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.width", RecipeEditorUi.intField(Math.clamp(data.getWidth(), 1, 4), 1, 4, value -> { data.setWidth(value); controller.notifyChanged(); }),
                                Component.translatable("viscript_recipe.config.confluence.width.tooltip")),
                        RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.height", RecipeEditorUi.intField(Math.clamp(data.getHeight(), 1, 4), 1, 4, value -> { data.setHeight(value); controller.notifyChanged(); }),
                                Component.translatable("viscript_recipe.config.confluence.height.tooltip")));
            }
        }
        if (ConfluenceRecipeEditorTypes.SOLIDIFIER.equals(type)) {
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.width", RecipeEditorUi.intField(Math.clamp(data.getWidth(), 1, 4), 1, 4, value -> { data.setWidth(value); controller.notifyChanged(); }),
                            Component.translatable("viscript_recipe.config.confluence.width.tooltip")),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.height", RecipeEditorUi.intField(Math.clamp(data.getHeight(), 1, 4), 1, 4, value -> { data.setHeight(value); controller.notifyChanged(); }),
                            Component.translatable("viscript_recipe.config.confluence.height.tooltip")));
        }
        if (ConfluenceRecipeEditorTypes.HELLFORGE.equals(type) || ConfluenceRecipeEditorTypes.HARDMODE_FORGE.equals(type)) {
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience", RecipeEditorUi.floatField(data.getExperience(), 0, Float.MAX_VALUE, value -> { data.setExperience(value); controller.notifyChanged(); })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time", RecipeEditorUi.intField(Math.max(0, data.getCookingTime()), 0, Integer.MAX_VALUE, value -> { data.setCookingTime(value); controller.notifyChanged(); })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.requires_fuel", new Switch().setOn(data.isRequiresFuel(), false).setOnSwitchChanged(value -> { data.setRequiresFuel(value); controller.notifyChanged(); })));
        }
        if (ConfluenceRecipeEditorTypes.COOKING_POT.equals(type)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time", RecipeEditorUi.intField(Math.max(0, data.getCookingTime()), 0, Integer.MAX_VALUE, value -> { data.setCookingTime(value); controller.notifyChanged(); })));
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.container", RecipeEditorUi.textButton(
                    Component.translatable("viscript_recipe.editor.confluence.click_container"), com.lowdragmc.lowdraglib2.gui.texture.Icons.SETTINGS, event -> controller.selectContainerSlot()
            ).layout(layout -> layout.widthPercent(100).height(18))));
            buildHeatSource(content, controller, data);
        }
        if (ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION.equals(type)) {
            content.addChildren(
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.transmutation.shrink", RecipeEditorUi.intField(Math.max(1, data.getShrink()), 1, Integer.MAX_VALUE, value -> { data.setShrink(value); controller.notifyChanged(); })),
                    RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.transmutation.game_phase", RecipeEditorUi.selector(
                            List.of(ConfluenceGamePhase.values()), data.getGamePhase() == null ? ConfluenceGamePhase.BEFORE_SKELETRON : data.getGamePhase(),
                            value -> Component.translatable("viscript_recipe.editor.confluence.phase." + value.getSerializedName()), value -> { data.setGamePhase(value); controller.notifyChanged(); })));
        }
        if (ConfluenceRecipeEditorTypes.isEnvironmentType(type)) buildEnvironment(content, controller, data);
    }

    static void buildTarget(UIElement content, RecipeEditorController controller) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.confluence.target"),
                new com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor().create(
                        "viscript_recipe.config.confluence.target", controller::getSelectedConfluenceTarget,
                        stack -> controller.setSelectedConfluenceTarget(stack == null ? ItemStack.EMPTY : stack.copy()), true, null, content
                ).layout(layout -> layout.widthPercent(100))
        );
    }

    private static void buildEnvironment(UIElement content, RecipeEditorController controller, ConfluenceRecipeData data) {
        var environment = data.getEnvironment();
        if (environment == null) { environment = new ConfluenceEnvironmentData(); data.setEnvironment(environment); }
        final var env = environment;
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.config.confluence.environment"));
        content.addChild(RecipeSearchComponents.biomeTag("viscript_recipe.config.confluence.environment.biome_tag",
                () -> firstTag(env.getBiomes()), id -> env.setBiomes(tag(id)), controller::notifyChanged));
        content.addChildren(
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.environment.inflate", RecipeEditorUi.intField(Math.max(1, env.getInflate()), 1, Integer.MAX_VALUE, value -> { env.setInflate(value); controller.notifyChanged(); })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.environment.state_predicates",
                        RecipeEditorUi.textField(statePredicatesText(env), value -> { env.setStatePredicates(parseStatePredicates(value)); controller.notifyChanged(); }),
                        Component.translatable("viscript_recipe.config.confluence.environment.state_predicates.tooltip")),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.environment.graveyard", new Switch().setOn(env.isGraveyard(), false).setOnSwitchChanged(value -> { env.setGraveyard(value); controller.notifyChanged(); })));
        buildHolderKind(content, controller, "viscript_recipe.config.confluence.environment.block_kind", env.getBlocks(), kind -> env.setBlocks(new ConfluenceHolderSetData().setKind(kind)),
                () -> RecipeSearchComponents.block("viscript_recipe.config.confluence.environment.block", () -> firstId(env.getBlocks()), id -> { env.setBlocks(ids(id)); controller.notifyChanged(); }, controller::notifyChanged, Blocks.CAMPFIRE),
                () -> RecipeSearchComponents.blockTag("viscript_recipe.config.confluence.environment.block_tag", () -> firstTag(env.getBlocks()), id -> { env.setBlocks(tag(id)); controller.notifyChanged(); }, controller::notifyChanged));
        buildHolderKind(content, controller, "viscript_recipe.config.confluence.environment.fluid_kind", env.getFluids(), kind -> env.setFluids(new ConfluenceHolderSetData().setKind(kind)),
                () -> RecipeSearchComponents.fluid("viscript_recipe.config.confluence.environment.fluid", () -> firstId(env.getFluids()), id -> { env.setFluids(ids(id)); controller.notifyChanged(); }, controller::notifyChanged, Fluids.WATER),
                () -> RecipeSearchComponents.fluidTag("viscript_recipe.config.confluence.environment.fluid_tag", () -> firstTag(env.getFluids()), id -> { env.setFluids(tag(id)); controller.notifyChanged(); }, controller::notifyChanged));
    }

    private static void buildHeatSource(UIElement content, RecipeEditorController controller, ConfluenceRecipeData data) {
        var heat = data.getHeatSource(); if (heat == null) { heat = new ConfluenceHeatSourceData(); data.setHeatSource(heat); }
        final var source = heat;
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.config.confluence.heat_source"));
        buildHolderKind(content, controller, "viscript_recipe.config.confluence.heat_source.block_kind", source.getBlocks(), kind -> source.setBlocks(new ConfluenceHolderSetData().setKind(kind)),
                () -> RecipeSearchComponents.block("viscript_recipe.config.confluence.heat_source.block", () -> firstId(source.getBlocks()), id -> { source.setBlocks(ids(id)); controller.notifyChanged(); }, controller::notifyChanged, Blocks.CAMPFIRE),
                () -> RecipeSearchComponents.blockTag("viscript_recipe.config.confluence.heat_source.block_tag", () -> firstTag(source.getBlocks()), id -> { source.setBlocks(tag(id)); controller.notifyChanged(); }, controller::notifyChanged));
        content.addChildren(
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.heat_source.has_state", new Switch().setOn(source.isHasState(), false).setOnSwitchChanged(value -> { source.setHasState(value); controller.notifyChanged(); })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.heat_source.state", RecipeEditorUi.textField(stateText(source.getState()), value -> { source.setState(parseState(value)); controller.notifyChanged(); })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.heat_source.has_nbt", new Switch().setOn(source.isHasNbt(), false).setOnSwitchChanged(value -> { source.setHasNbt(value); controller.notifyChanged(); })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.confluence.heat_source.nbt", RecipeEditorUi.textField(source.getNbt(), value -> { source.setNbt(value); controller.notifyChanged(); })));
    }

    private static ConfluenceHolderSetData tag(ResourceLocation id) { return new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.TAG).setTag(id); }
    private static ConfluenceHolderSetData ids(ResourceLocation id) { return new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.IDS).setValues(new ArrayList<>(List.of(id))); }
    private static ResourceLocation firstTag(ConfluenceHolderSetData d) { return d == null || d.getTag() == null ? ResourceLocation.withDefaultNamespace("is_overworld") : d.getTag(); }
    private static ResourceLocation firstId(ConfluenceHolderSetData d) { return d == null || d.getValues() == null || d.getValues().isEmpty() ? null : d.getValues().getFirst(); }
    private static String stateText(ConfluenceStatePredicateData d) { return d == null || d.getProperties() == null || d.getProperties().isEmpty() ? "lit=true" : d.getProperties().getFirst().getName() + "=" + d.getProperties().getFirst().getValue(); }
    private static ConfluenceStatePredicateData parseState(String value) { var p = new ConfluenceStatePropertyData(); var split = value == null ? new String[]{"lit", "true"} : value.split("=", 2); p.setName(split[0].trim()); p.setValue(split.length > 1 ? split[1].trim() : "true"); return new ConfluenceStatePredicateData().setProperties(new ArrayList<>(List.of(p))); }

    private static String statePredicatesText(ConfluenceEnvironmentData data) {
        if (data == null || data.getStatePredicates() == null || data.getStatePredicates().isEmpty()) return "";
        return data.getStatePredicates().stream()
                .filter(java.util.Objects::nonNull)
                .map(predicate -> predicate.getProperties() == null ? "" : predicate.getProperties().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(property -> property.isRanged()
                                ? property.getName() + "=" + property.getMin() + ".." + property.getMax()
                                : property.getName() + "=" + property.getValue())
                        .collect(java.util.stream.Collectors.joining(",")))
                .filter(text -> !text.isBlank())
                .collect(java.util.stream.Collectors.joining(";"));
    }

    private static List<ConfluenceStatePredicateData> parseStatePredicates(String value) {
        var predicates = new ArrayList<ConfluenceStatePredicateData>();
        if (value == null || value.isBlank()) return predicates;
        for (var rawPredicate : value.split(";")) {
            var properties = new ArrayList<ConfluenceStatePropertyData>();
            for (var rawProperty : rawPredicate.split(",")) {
                var text = rawProperty.trim();
                if (text.isBlank()) continue;
                var split = text.split("=", 2);
                var property = new ConfluenceStatePropertyData().setName(split[0].trim());
                var propertyValue = split.length > 1 ? split[1].trim() : "true";
                var range = propertyValue.split("\\.\\.", 2);
                if (range.length == 2) {
                    property.setRanged(true).setMin(range[0].trim()).setMax(range[1].trim());
                } else {
                    property.setValue(propertyValue);
                }
                if (!property.getName().isBlank()) properties.add(property);
            }
            if (!properties.isEmpty()) predicates.add(new ConfluenceStatePredicateData().setProperties(properties));
        }
        return predicates;
    }

    private static void buildHolderKind(UIElement content, RecipeEditorController controller, String key,
                                        ConfluenceHolderSetData data,
                                        java.util.function.Consumer<ConfluenceHolderSetKind> setter,
                                        java.util.function.Supplier<UIElement> idsView,
                                        java.util.function.Supplier<UIElement> tagView) {
        var kind = data == null || data.getKind() == null ? ConfluenceHolderSetKind.NONE : data.getKind();
        content.addChild(RecipeEditorUi.fieldGroup(key, RecipeEditorUi.selector(
                List.of(ConfluenceHolderSetKind.NONE, ConfluenceHolderSetKind.IDS, ConfluenceHolderSetKind.TAG), kind,
                value -> Component.translatable("viscript_recipe.editor.confluence.holder_kind." + value.getSerializedName()), value -> { setter.accept(value); controller.notifyChanged(); })));
        if (kind == ConfluenceHolderSetKind.IDS) content.addChild(idsView.get());
        if (kind == ConfluenceHolderSetKind.TAG) content.addChild(tagView.get());
    }
}
