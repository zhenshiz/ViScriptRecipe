package com.viscript_recipe.compat.forbidden_arcanus.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeLevel;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.compat.forbidden_arcanus.data.ForgeRitualRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.HEPHAESTUS_UPGRADING;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.id;
import static com.viscript_recipe.compat.forbidden_arcanus.data.ForgeRitualRecipeData.MAX_TIER;
import static com.viscript_recipe.compat.forbidden_arcanus.data.ForgeRitualRecipeData.PEDESTAL_COUNT;
import static com.viscript_recipe.gui.views.PropertiesView.createItemStackConfigurator;
import static com.viscript_recipe.gui.views.PropertiesView.removeCountConfig;

public final class ForgeRitualCanvas extends ForbiddenArcanusCanvas<ForgeRitualRecipeData> {
    private static final int[][] PEDESTAL_POSITIONS = {
            {96, 13}, {119, 20}, {126, 43}, {119, 66}, {96, 73}, {73, 66}, {66, 43}, {73, 20}
    };
    private final Label[] essences = new Label[EssenceType.values().length];
    private final ItemSlot[] enhancers = new ItemSlot[4];
    private ItemSlot requiredTier;
    private ItemSlot upgradedTier;

    public ForgeRitualCanvas(NavigationView navigation, RecipeEntry entry) {super(navigation, entry);}

    private boolean upgrading() {return entry.isType(HEPHAESTUS_UPGRADING);}

    @Override NativeIngredientData input(int index) {return index == 0 ? getData().getCenter() : getData().pedestal(index - 1);}

    @Override int inputCount() {return PEDESTAL_COUNT + 1;}

    @Override
    public UIElement createCanvas() {
        setId("hephaestus_ritual_canvas");
        var panel = panel(214, 125);
        for (int i = 0; i < PEDESTAL_COUNT; i++) {
            var position = PEDESTAL_POSITIONS[i];
            var pedestal = ingredientSlot(i + 1, JEI_SLOT_SIZE);
            tooltip(pedestal, Component.translatable(key("pedestal"), i + 1));
            panel.addChild(at(pedestal.setId("hephaestus_pedestal_" + i), position[0], position[1], 18, 18));
        }
        var center = ingredientSlot(0, SLOT_SIZE);
        tooltip(center, key("main_ingredient"));
        requiredTier = previewSlot(JEI_SLOT_SIZE);
        panel.addChildren(at(center.setId("hephaestus_center"), 93, 40, 24, 24),
                at(arrow(), 149, 43, 15, 18));
        if (upgrading()) {
            upgradedTier = previewSlot(OUTPUT_SLOT_SIZE);
            tooltip(upgradedTier, key("result_tier"));
            panel.addChildren(at(requiredTier.setId("hephaestus_required_tier"), 24, 43, 18, 18),
                    at(upgradedTier.setId("hephaestus_upgraded_tier"), 170, 37, 30, 30));
        } else {
            panel.addChildren(at(requiredTier.setId("hephaestus_required_tier"), 175, 76, 18, 18),
                    at(createOutputSlot(0, OUTPUT_SLOT_SIZE).setId("hephaestus_ritual_output"), 170, 37, 30, 30));
            for (int i = 0; i < enhancers.length; i++) {
                enhancers[i] = previewSlot(JEI_SLOT_SIZE);
                panel.addChild(at(enhancers[i], 22, 13 + i * 20, 18, 18));
            }
        }
        for (var essence : EssenceType.values()) {
            var index = essence.ordinal();
            var icon = new UIElement().layout(layout -> layout.width(12).height(12))
                    .style(style -> style.backgroundTexture(SpriteTexture.of(
                            id("textures/gui/sprites/icon/" + essence.getSerializedName() + ".png"))));
            var amount = RecipeEditorUi.label(Component.empty());
            essences[index] = amount;
            var field = RecipeEditorUi.row().layout(layout -> layout.gapAll(3).alignItems(AlignItems.CENTER))
                    .addChildren(icon, amount);
            tooltip(field, Component.translatable(essenceKey(essence)));
            panel.addChild(at(field, 12 + index * 48, 104, 46, 14));
        }
        return centered(panel);
    }

    @Override
    public void load() {
        super.load();
        if (!upgrading()) setVisualOutput(0, getData().getResult());
        refreshPreview();
    }

    @Override
    public void save() {
        super.save();
        if (!upgrading()) getData().setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                intField(key("required_tier"), data.getForgeTier(), 1, MAX_TIER, data::setForgeTier, this::refreshPreview),
                switchField(key("exact_tier"), data.isExactTier(), data::setExactTier, this::refreshPreview));
        if (upgrading()) {
            content.addChild(intField(key("result_tier"), data.getResultTier(), 1, MAX_TIER, data::setResultTier, this::refreshPreview));
        } else {
            content.addChild(switchField(key("transmute"), data.isTransmuteInput(), data::setTransmuteInput,
                    Component.translatable(key("transmute_hint"))));
        }
        content.addChildren(
                intField(key("duration"), data.getDuration(), 1, Integer.MAX_VALUE, data::setDuration),
                registryField(key("magic_circle"), FARegistries.MAGIC_CIRCLE, data::getMagicCircle, false,
                        circle -> Component.literal(circle.key().location().toString()), data::setMagicCircle),
                sectionTitle(key("essences")),
                essenceField(EssenceType.AUREAL, data.getAureal(), data::setAureal).setId("hephaestus_aureal"),
                essenceField(EssenceType.SOULS, data.getSouls(), data::setSouls),
                essenceField(EssenceType.BLOOD, data.getBlood(), data::setBlood),
                essenceField(EssenceType.EXPERIENCE, data.getExperience(), data::setExperience));
        buildEnhancers(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (!getData().isTransmuteInput()) {
            super.buildResultProperties(content);
            return;
        }
        content.addChildren(RecipeEditorUi.label(Component.translatable(key("transmute_hint"))),
                removeCountConfig(createItemStackConfigurator("viscript_recipe.config.recipe.result",
                        () -> getSelectedOutput().getItem(), stack -> setSelectedOutput(stack.getItem().getDefaultInstance()))));
    }

    private UIElement essenceField(EssenceType type, int current, Consumer<Integer> setter) {
        return intField(essenceKey(type), current, 0, Integer.MAX_VALUE, setter, this::refreshPreview);
    }

    private void buildEnhancers(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle(key("enhancers")),
                textField(key("enhancer_tag"), data.getEnhancerTag(), data::setEnhancerTag, this::refreshPreview,
                        Component.translatable(key("enhancer_tag_hint"))));
        for (int i = 0; i < data.getEnhancers().size(); i++) {
            int index = i;
            content.addChildren(registryField(key("enhancer"), FARegistries.ENHANCER_DEFINITION, () -> data.getEnhancers().get(index),
                            true, ForbiddenArcanusCanvas::enhancerName,
                            value -> {data.getEnhancers().set(index, value); refreshPreview();}),
                    RecipeEditorUi.textButton(Component.translatable("viscript_recipe.editor.operation.remove"), null, event -> {
                        data.getEnhancers().remove(index);
                        refreshPreview();
                        reloadProperties();
                    }));
        }
        content.addChild(RecipeEditorUi.textButton(Component.translatable(key("add_enhancer")), null, event -> {
            data.getEnhancers().add("");
            reloadProperties();
        }));
    }

    private void refreshPreview() {
        var data = getData();
        int[] amounts = {data.getAureal(), data.getSouls(), data.getBlood(), data.getExperience()};
        for (int i = 0; i < essences.length; i++) essences[i].setText(Component.literal(Integer.toString(amounts[i])));
        requiredTier.setItem(forgeStack(data.getForgeTier()), false);
        tooltip(requiredTier, Component.translatable(key(data.isExactTier() ? "tier_exact_hint" : "tier_minimum_hint"),
                data.getForgeTier()));
        if (upgrading()) {
            upgradedTier.setItem(forgeStack(data.getResultTier()), false);
            return;
        }
        var displayEnhancers = enhancerPreviews();
        for (int i = 0; i < enhancers.length; i++) {
            var enhancer = enhancers[i];
            enhancer.setItem(i < displayEnhancers.size() ? displayEnhancers.get(i) : ItemStack.EMPTY, false);
            tooltip(enhancer, key("enhancer"));
        }
    }

    private List<ItemStack> enhancerPreviews() {
        var data = getData();
        var provider = registryAccess();
        if (provider == null) return List.of();
        if (!data.getEnhancerTag().isBlank()) {
            var id = ResourceLocation.tryParse(data.getEnhancerTag());
            if (id == null) return List.of();
            return provider.lookupOrThrow(FARegistries.ENHANCER_DEFINITION).get(TagKey.create(FARegistries.ENHANCER_DEFINITION, id))
                    .stream().flatMap(set -> set.stream()).limit(enhancers.length).map(ForgeRitualCanvas::enhancerStack).toList();
        }
        return data.getEnhancers().stream().map(value -> readValue(EnhancerDefinition.REFERENCE_CODEC, value))
                .filter(java.util.Objects::nonNull).limit(enhancers.length).map(ForgeRitualCanvas::enhancerStack).toList();
    }

    private static ItemStack enhancerStack(Holder<EnhancerDefinition> holder) {return holder.value().displayItem().value().getDefaultInstance();}

    private static ItemStack forgeStack(int tier) {
        return HephaestusForgeLevel.values()[Math.clamp(tier, 1, MAX_TIER) - 1].getBlock().asItem().getDefaultInstance();
    }

    private static String essenceKey(EssenceType type) {return key("essence." + type.getSerializedName());}
}
