package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.configurator.ui.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialEntityConditionData;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialEntityIngredientKind;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialLaserDrillFluidRecipeData;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialLaserDrillRarityData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeRegistryClientData;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LaserDrillFluidCanvas extends FluidRecipeCanvas<IndustrialLaserDrillFluidRecipeData> {
    static final Label rangeLabel = RecipeEditorUi.label(Component.empty());
    static final Label requirementsLabel = RecipeEditorUi.label(Component.empty());

    public LaserDrillFluidCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getCatalyst());
        setVisualFluidInput(0, data.getOutput());
        refreshLaserLabels();
    }

    @Override
    public void save() {
        getData().setCatalyst(getVisualIngredient(0)).setOutput(getVisualFluidInput(0));
    }

    @Override
    public UIElement createCanvas() {
        var catalyst = createIngredientSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(catalyst);
        var output = createFluidInputSlot(0);
        output.layout(layout -> {
            layout.width(18);
            layout.height(18);
        });
        return IndustrialForegoingCanvasFactory.createLaser(
                IndustrialForegoingCanvasFactory.slotCell(catalyst, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(output, 18, 18), rangeLabel, requirementsLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.laser_drill_fluid"));
        buildRarities(content, data::getRarity, data::setRarity, this::refreshPropertiesAndLabels);
        buildEntityCondition(content, data.getEntityCondition(), this::refreshPropertiesAndLabels);
    }

    @Override
    public String selectedFluidConfigNameKey() {return "viscript_recipe.config.industrial_foregoing.laser.fluid_output";}

    private void refreshPropertiesAndLabels() {
        refreshLaserLabels();
        reloadProperties();
    }

    private void refreshLaserLabels() {
        var data = getData();
        var rarities = data.getRarity();
        var rarity = rarities.isEmpty() ? new IndustrialLaserDrillRarityData() : rarities.getFirst();
        rangeLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.laser.fluid_summary",
                rarity.getDepthMin(), rarity.getDepthMax(), rarity.getWeight(), rarities.size()));
        var entity = data.getEntityCondition();
        var entityText = entity.isEnabled()
                ? Component.literal((entity.getKind() == IndustrialEntityIngredientKind.TAG ? "#" : "") + entity.getId())
                : Component.translatable("viscript_recipe.editor.industrial_foregoing.entity_condition.none");
        requirementsLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.laser.requirements",
                entityText, rarity.getBiomeWhitelist().size(), rarity.getBiomeBlacklist().size(),
                rarity.getDimensionWhitelist().size(), rarity.getDimensionBlacklist().size()));
    }

    static void buildRarities(UIElement content, Supplier<List<IndustrialLaserDrillRarityData>> source,
                                      Consumer<List<IndustrialLaserDrillRarityData>> setter, Runnable onUpdate) {
        var rules = new ArrayConfiguratorGroup<>(
                "viscript_recipe.config.industrial_foregoing.laser.rarity",
                false, source, (getter, rowSetter) -> createRarityRuleConfigurator(getter, source), true
        );
        rules.setAddDefault(IndustrialLaserDrillRarityData::new);
        rules.setCanRemove(rarity -> source.get().size() > 1);
        rules.setCanReorder(true);
        rules.setOnUpdate(updated -> {
            setter.accept(new ArrayList<>(updated));
            onUpdate.run();
        });
        content.addChild(rules.layout(layout -> layout.widthPercent(100)));
    } // todo 编辑之后不会立即刷新，先摆了

    private static Configurator createRarityRuleConfigurator(
            Supplier<IndustrialLaserDrillRarityData> getter, Supplier<List<IndustrialLaserDrillRarityData>> source
    ) {
        var rarity = getter.get();
        var ruleIndex = source.get().indexOf(rarity) + 1;
        var group = new ConfiguratorGroup();
        group.setLabel(Component.translatable("viscript_recipe.editor.industrial_foregoing.rarity.title", ruleIndex));
        group.setCollapse(true);
        group.configuratorContainer.addChildren(
                intField("viscript_recipe.config.industrial_foregoing.rarity.depth_min", rarity.getDepthMin(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, rarity::setDepthMin),
                intField("viscript_recipe.config.industrial_foregoing.rarity.depth_max", rarity.getDepthMax(),
                        Integer.MIN_VALUE, Integer.MAX_VALUE, rarity::setDepthMax),
                intField("viscript_recipe.config.industrial_foregoing.rarity.weight", rarity.getWeight(),
                        1, Integer.MAX_VALUE, rarity::setWeight)
        );
        buildResourceList(group.configuratorContainer, rarity::getBiomeWhitelist, rarity::setBiomeWhitelist,
                "viscript_recipe.config.industrial_foregoing.rarity.biome_whitelist", true);
        buildResourceList(group.configuratorContainer, rarity::getBiomeBlacklist, rarity::setBiomeBlacklist,
                "viscript_recipe.config.industrial_foregoing.rarity.biome_blacklist", true);
        buildResourceList(group.configuratorContainer, rarity::getDimensionWhitelist, rarity::setDimensionWhitelist,
                "viscript_recipe.config.industrial_foregoing.rarity.dimension_whitelist", false);
        buildResourceList(group.configuratorContainer, rarity::getDimensionBlacklist, rarity::setDimensionBlacklist,
                "viscript_recipe.config.industrial_foregoing.rarity.dimension_blacklist", false);
        return group;
    }

    private static void buildResourceList(UIElement content, Supplier<List<ResourceLocation>> source,
                                          Consumer<List<ResourceLocation>> setter, String key, boolean biomeTag) {
        var filters = new ArrayConfiguratorGroup<>(key, true, source,
                (getter, rowSetter) -> createResourceFilterConfigurator(getter, rowSetter, biomeTag), true
        );
        filters.setAddDefault(() -> defaultResourceFilter(biomeTag));
        filters.setCanReorder(true);
        filters.setOnUpdate(updated -> setter.accept(new ArrayList<>(updated)));
        content.addChild(filters.layout(layout -> layout.widthPercent(100)));
    }

    private static Configurator createResourceFilterConfigurator(Supplier<ResourceLocation> getter,
                                                                 Consumer<ResourceLocation> setter,
                                                                 boolean biomeTag) {
        var row = new Configurator();
        var input = biomeTag
                ? RecipeSearchComponents.biomeTag("", getter, setter, Runnables.doNothing())
                : RecipeSearchComponents.dimensionType("", getter, setter, Runnables.doNothing());
        input.layout(layout -> layout.widthPercent(100));
        return row.addInlineChild(input);
    }

    private static ResourceLocation defaultResourceFilter(boolean biomeTag) {
        var candidates = biomeTag
                ? RecipeRegistryClientData.biomeTags().keySet().stream().toList()
                : RecipeRegistryClientData.dimensionTypes();
        return candidates.isEmpty()
                ? ResourceLocation.withDefaultNamespace(biomeTag ? "is_overworld" : "overworld")
                : candidates.getFirst();
    }

    static void buildEntityCondition(UIElement content, IndustrialEntityConditionData data, Runnable onUpdate) {
        content.addChildren(sectionTitle("viscript_recipe.config.industrial_foregoing.entity_condition"),
                switchField("viscript_recipe.config.industrial_foregoing.entity_condition.enabled",
                        data.isEnabled(), bl -> {
                             data.setEnabled(bl); onUpdate.run();
                        })
        );
        if (!data.isEnabled()) return;
        var kind = data.getKind();
        content.addChild(field("viscript_recipe.config.industrial_foregoing.entity_condition.kind",
                RecipeEditorUi.selector(List.of(IndustrialEntityIngredientKind.values()), kind,
                        IndustrialEntityIngredientKind::displayName, value -> {
                            data.setKind(value); onUpdate.run();
                        })));
        if (kind == IndustrialEntityIngredientKind.TAG) {
            content.addChild(RecipeSearchComponents.entityTag(
                    "viscript_recipe.config.industrial_foregoing.entity_condition.id",
                    data::getId, data::setId, onUpdate));
        } else content.addChild(RecipeSearchComponents.entityType(
                "viscript_recipe.config.industrial_foregoing.entity_condition.id",
                data::getId, data::setId, onUpdate, EntityType.WITHER));
        content.addChildren(
                textField("viscript_recipe.config.industrial_foregoing.entity_condition.nbt",
                        data.getNbt(), data::setNbt),
                textField("viscript_recipe.config.industrial_foregoing.entity_condition.display",
                        data.getDisplay(), data::setDisplay)
        );
    }
}
