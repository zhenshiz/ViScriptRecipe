package com.viscript_recipe.compat.confluence.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.confluence.data.*;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.viscript_recipe.compat.confluence.ConfluenceRecipeEditorTypes.*;

public class ConfluenceCanvas extends RecipeCanvas<ConfluenceRecipeData> {
    private static final int CONTAINER_SLOT = 16;
    private static final int HEAT_SLOT = 17;
    static final Label phaseLabel = emptyLabel();

    public ConfluenceCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return !ITEM_TRANSMUTATION.equals(entry.getType());}

    @Override
    public void load() {
        var data = getData();
        var type = entry.getType();
        if (ITEM_TRANSMUTATION.equals(type)) {
            loadIngredientSlot(0, data.ingredient(0));
            var targets = data.getTargets();
            for (int i = 0; i < targets.size(); i++) setVisualOutput(i, targets.get(i));
            updatePhase();
            return;
        }
        loadIngredients(data.getIngredients());
        if (COOKING_POT.equals(type)) {
            loadIngredientSlot(CONTAINER_SLOT, data.getContainer());
            loadIngredientSlot(HEAT_SLOT, data.getHeatSource().getBlocks().asVisualIngredient());
        }
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        var type = entry.getType();
        if (ITEM_TRANSMUTATION.equals(type)) {
            data.setIngredient(0, getVisualIngredient(0));
            var targets = new ArrayList<ItemStack>();
            for (int i = 0; i < 16; i++) {
                var item = getVisualOutput(i).getItem();
                if (!item.isEmpty()) targets.add(item);
            }
            data.setTargets(targets);
            return;
        }
        data.setIngredients(getIngredients(maxInputs(type), true));
        if (COOKING_POT.equals(type)) data.setContainer(getVisualIngredient(CONTAINER_SLOT));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var type = entry.getType();
        if (ITEM_TRANSMUTATION.equals(type)) return createTransmutation();
        if (ALCHEMY_TABLE.equals(type)) return createAmount(7, true);
        if (FLETCHING_TABLE.equals(type)) return createFletching();
        if (COOKING_POT.equals(type)) return createCooking();
        if (isEitherType(type)) return createEither();
        if (HELLFORGE.equals(type) || HARDMODE_FORGE.equals(type)) return createForge();
        return createAmount(maxInputs(type), false);
    }

    private UIElement createTransmutation() {
        var input = createIngredientSlot(0, JEI_SLOT_SIZE);
        var targetSlots = new ItemSlot[16];
        for (int i = 0; i < 16; i++) {
            var slot = createOutputSlot(i, JEI_SLOT_SIZE);
            tooltip(slot, "viscript_recipe.config.confluence.target");
            targetSlots[i] = slot;
        }
        return ConfluenceCanvasFactory.transmutation(input, targetSlots, phaseLabel);
    }
    private UIElement createAmount(int count, boolean alchemy) {
        var inputs = new IngredientDisplaySlot[count];
        for (int i = 0; i < count; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        if (alchemy) {
            configureJeiOverlaySlotVisual(inputs);
            configureJeiOverlaySlotVisual(output);
        }
        return alchemy ? ConfluenceCanvasFactory.alchemy(inputs, output) : ConfluenceCanvasFactory.amount(inputs, output);
    }
    private UIElement createFletching() {
        var inputs = new IngredientDisplaySlot[3];
        for (int i = 0; i < inputs.length; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(inputs);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return ConfluenceCanvasFactory.fletching(inputs, output);
    }
    private UIElement createForge() {
        var inputs = new IngredientDisplaySlot[5];
        for (int i = 0; i < inputs.length; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        return ConfluenceCanvasFactory.forge(inputs, output);
    }
    private UIElement createEither() {
        var inputs = new IngredientDisplaySlot[16];
        for (int i = 0; i < inputs.length; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        return ConfluenceCanvasFactory.either(inputs, output);
    }
    private UIElement createCooking() {
        var inputs = new IngredientDisplaySlot[4];
        for (int i = 0; i < inputs.length; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(inputs);
        var container = createIngredientSlot(CONTAINER_SLOT, JEI_SLOT_SIZE);
        var heat = createIngredientSlot(HEAT_SLOT, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(container, heat, output);
        return ConfluenceCanvasFactory.cooking(inputs, container, heat, output);
    }

    private void updatePhase() {
        var phase = getData().getGamePhase();
        phaseLabel.setText(Component.translatable("viscript_recipe.editor.confluence.phase", phase.displayName()));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        if (COOKING_POT.equals(entry.getType())) {
            if (selectedSlotIndex() == CONTAINER_SLOT) {
                content.addChildren(sectionTitle("viscript_recipe.config.confluence.container"),
                        PropertiesView.createItemStackConfigurator("viscript_recipe.config.confluence.container", () -> getSelectedIngredient().toStack(), stack -> setSelectedIngredient(RecipeIngredient.item(stack))));
                return;
            } else if (selectedSlotIndex() == HEAT_SLOT) {
                var source = getData().getHeatSource();
                content.addChild(sectionTitle("viscript_recipe.config.confluence.heat_source"));
                buildHolderKind(content, "viscript_recipe.config.confluence.heat_source.block_kind", source.getBlocks(), kind -> source.setBlocks(source.getBlocks().setKind(kind)),
                        () -> RecipeSearchComponents.block("viscript_recipe.config.confluence.heat_source.block",
                                () -> source.getBlocks().firstId(), id -> {
                                    source.setBlocks(ConfluenceHolderSetData.ids(id));
                                    setSelectedIngredient(source.getBlocks().asVisualIngredient());
                                }, Runnables.doNothing(), Blocks.CAMPFIRE),
                        () -> RecipeSearchComponents.blockTag("viscript_recipe.config.confluence.heat_source.block_tag",
                                () -> source.getBlocks().getTag(), id -> {
                                    source.setBlocks(ConfluenceHolderSetData.tag(id));
                                    setSelectedIngredient(source.getBlocks().asVisualIngredient());
                                }, Runnables.doNothing()));
                content.addChildren(
                        switchField("viscript_recipe.config.confluence.heat_source.has_state",
                                source.isHasState(), source::setHasState),
                        textField("viscript_recipe.config.confluence.heat_source.state", source.getState().stateText(),
                                value -> source.setState(ConfluenceStatePredicateData.parseState(value))),
                        switchField("viscript_recipe.config.confluence.heat_source.has_nbt",
                                source.isHasNbt(), source::setHasNbt),
                        textField("viscript_recipe.config.confluence.heat_source.nbt", source.getNbt(), source::setNbt)
                );
                return;
            }
        }
        super.buildIngredientProperties(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (ITEM_TRANSMUTATION.equals(entry.getType())) {
            content.addChildren(sectionTitle("viscript_recipe.editor.properties.confluence.target"),
                    PropertiesView.createItemStackConfigurator("viscript_recipe.config.confluence.target", () -> getSelectedOutput().getItem(), this::setSelectedOutput));
            return;
        }
        super.buildResultProperties(content);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        var type = entry.getType();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.confluence"));

        if (isEitherType(type)) {
            content.addChild(selector("viscript_recipe.config.confluence.crafting_mode",
                    List.of(ConfluenceCraftingMode.values()), data.getCraftingMode(), ConfluenceCraftingMode::displayName,
                    data::setCraftingMode, RecipeCanvas::reloadCanvas));
            if (data.getCraftingMode() != ConfluenceCraftingMode.SHAPELESS) content.addChildren(
                    intField("viscript_recipe.config.confluence.width",
                            data.getWidth(), 1, 4, data::setWidth, RecipeCanvas::reloadCanvas,
                            Component.translatable("viscript_recipe.config.confluence.width.tooltip")),
                    intField("viscript_recipe.config.confluence.height",
                            data.getHeight(), 1, 4, data::setHeight, RecipeCanvas::reloadCanvas,
                                Component.translatable("viscript_recipe.config.confluence.height.tooltip"))
            );
        }
        if (SOLIDIFIER.equals(type)) content.addChildren(
                intField("viscript_recipe.config.confluence.width", data.getWidth(), 1, 4, data::setWidth,
                        RecipeCanvas::reloadCanvas, Component.translatable("viscript_recipe.config.confluence.width.tooltip")),
                intField("viscript_recipe.config.confluence.height", data.getHeight(), 1, 4, data::setHeight,
                        RecipeCanvas::reloadCanvas, Component.translatable("viscript_recipe.config.confluence.height.tooltip"))
        );
        if (HELLFORGE.equals(type) || HARDMODE_FORGE.equals(type)) content.addChildren(
                floatField("viscript_recipe.config.cooking.experience", data.getExperience(), 0, Float.MAX_VALUE, data::setExperience),
                intField("viscript_recipe.config.cooking.cooking_time", data.getCookingTime(), 0, Integer.MAX_VALUE, data::setCookingTime),
                switchField("viscript_recipe.config.confluence.requires_fuel", data.isRequiresFuel(), data::setRequiresFuel)
        );
        if (COOKING_POT.equals(type)) content.addChild(
                intField("viscript_recipe.config.cooking.cooking_time", data.getCookingTime(), 0, Integer.MAX_VALUE, data::setCookingTime)
        );
        if (ITEM_TRANSMUTATION.equals(type)) content.addChildren(
                intField("viscript_recipe.config.confluence.transmutation.shrink", data.getShrink(), 1, Integer.MAX_VALUE, data::setShrink),
                selector("viscript_recipe.config.confluence.transmutation.game_phase",
                        List.of(ConfluenceGamePhase.values()), data.getGamePhase(),
                        ConfluenceGamePhase::displayName, data::setGamePhase, this::updatePhase));

        if (isEnvironmentType(type)) {
            var env = data.getEnvironment();
            content.addChild(sectionTitle("viscript_recipe.config.confluence.environment"));
            content.addChild(RecipeSearchComponents.biomeTag("viscript_recipe.config.confluence.environment.biome_tag",
                    () -> env.getBiomes().getTag(), id -> env.setBiomes(ConfluenceHolderSetData.tag(id)), Runnables.doNothing()));
            content.addChildren(
                    intField("viscript_recipe.config.confluence.environment.inflate",
                            env.getInflate(), 1, Integer.MAX_VALUE, env::setInflate),
                    textField("viscript_recipe.config.confluence.environment.state_predicates",
                            env.statePredicatesText(), value -> env.setStatePredicates(ConfluenceStatePredicateData.parseStatePredicates(value)),
                            Component.translatable("viscript_recipe.config.confluence.environment.state_predicates.tooltip")),
                    switchField("viscript_recipe.config.confluence.environment.graveyard", env.isGraveyard(), env::setGraveyard)
            );
            buildHolderKind(content, "viscript_recipe.config.confluence.environment.block_kind", env.getBlocks(), kind -> env.setBlocks(env.getBlocks().setKind(kind)),
                    () -> RecipeSearchComponents.block("viscript_recipe.config.confluence.environment.block", () -> env.getBlocks().firstId(), id -> env.setBlocks(ConfluenceHolderSetData.ids(id)), Runnables.doNothing(), Blocks.CAMPFIRE),
                    () -> RecipeSearchComponents.blockTag("viscript_recipe.config.confluence.environment.block_tag", () -> env.getBlocks().firstId(), id -> env.setBlocks(ConfluenceHolderSetData.tag(id)), Runnables.doNothing()));
            buildHolderKind(content, "viscript_recipe.config.confluence.environment.fluid_kind", env.getFluids(), kind -> env.setFluids(env.getFluids().setKind(kind)),
                    () -> RecipeSearchComponents.fluid("viscript_recipe.config.confluence.environment.fluid", () -> env.getFluids().firstId(), id -> env.setFluids(ConfluenceHolderSetData.ids(id)), Runnables.doNothing(), Fluids.WATER),
                    () -> RecipeSearchComponents.fluidTag("viscript_recipe.config.confluence.environment.fluid_tag", () -> env.getFluids().firstId(), id -> env.setFluids(ConfluenceHolderSetData.tag(id)), Runnables.doNothing()));
        }
    }

    static void buildHolderKind(UIElement content, String key, ConfluenceHolderSetData data, Consumer<ConfluenceHolderSetKind> setter, Supplier<UIElement> idsView, Supplier<UIElement> tagView) {
        var kind = data.getKind();
        content.addChild(selector(key, List.of(ConfluenceHolderSetKind.values()), kind, ConfluenceHolderSetKind::displayName,
                setter, RecipeCanvas::reloadProperties)
        );
        if (kind == ConfluenceHolderSetKind.IDS) content.addChild(idsView.get());
        if (kind == ConfluenceHolderSetKind.TAG) content.addChild(tagView.get());
    }
}
