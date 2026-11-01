package com.viscript_recipe.compat.omnisequence.canvas;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.atir.molecularmanipulator.client.AEStackIcon;
import com.atir.molecularmanipulator.client.DisplayNumbers;
import com.atir.molecularmanipulator.crafting.MatterFabricationRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.viscript_lib.gui.configurator.ViScriptItemStackAccessor;
import com.viscript_lib.util.item.ViScriptItemStack;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory;
import com.viscript_recipe.compat.jei.omnisequence.OmniSequenceJeiCanvas;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory;
import com.viscript_recipe.compat.omnisequence.data.OmniSequenceRecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeEditorTypes.key;
import static com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory.decode;
import static com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory.encode;

/** 保留原生定义，仅把实际修改的材料、产物与表单字段写回。 */
public final class OmniSequenceRecipeCanvas extends FluidRecipeCanvas<OmniSequenceRecipeData> {
    private JsonObject root;
    private final List<JsonObject> inputs = new ArrayList<>();
    private final List<RecipeIngredient> originalInputs = new ArrayList<>();
    private final List<ItemStack> originalOutputs = new ArrayList<>();
    private final Label status = new Label();
    private Consumer<MatterFabricationRecipe> updatePreview = recipe -> {};

    public OmniSequenceRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private boolean fabrication() { return entry.getType().getPath().equals("matter_fabrication"); }
    private boolean machineImport() { return entry.getType().getPath().equals("matter_machine_import"); }
    private JsonObject research() { return machineImport() ? root.getAsJsonObject("research") : root; }
    private static Component text(String path, Object... args) { return Component.translatable(key(path), args); }

    @Override
    public UIElement createCanvas() {
        root = com.google.gson.JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        inputs.clear(); originalInputs.clear(); originalOutputs.clear();
        var body = RecipeEditorUi.column().setId("omnisequence_canvas").layout(l ->
                l.width(fabrication() ? 184 : 340).gapAll(8).alignItems(AlignItems.CENTER).flexShrink(0));
        if (fabrication()) body.addChild(fabricationPanel());
        else {
            body.addChild(RecipeEditorUi.label(text(entry.getType().getPath())).textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER)).layout(l -> l.widthPercent(100)));
            if (machineImport()) body.addChild(RecipeEditorUi.label(text("machine_summary", root.get("machine").getAsString())).textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER)).layout(l -> l.widthPercent(100)));
            var slots = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(300).flexWrap(FlexWrap.WRAP)
                    .justifyContent(AlignContent.CENTER));
            for (var value : array(research(), "ingredients")) {
                if (inputs.size() >= MAX_INGREDIENT) break;
                slots.addChild(inputSlot(value.getAsJsonObject(), SLOT_SIZE));
            }
            body.addChildren(RecipeEditorUi.sectionTitle(key("ingredients")).textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER)).layout(l -> l.widthPercent(100)), slots);
        }
        status.textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER));
        body.addChild(status.setId("omnisequence_validation").setDisplay(false).layout(l -> l.widthPercent(100).height(18)));
        body.addEventListener(UIEvents.MOUSE_DOWN, e -> { if (e.button == 0) selectRecipe(); });
        return Ae2CanvasFactory.centered(body).setId("omnisequence_canvas_container");
    }

    private UIElement fabricationPanel() {
        var panel = Ae2CanvasFactory.panel("omnisequence_fabrication_panel", 184, 100);
        if (ViScriptRecipe.isModLoaded("jei")) {
            var preview = new OmniSequenceJeiCanvas();
            updatePreview = recipe -> preview.setRecipe(recipe, entry.getRecipeId());
            panel.addChild(Ae2CanvasFactory.at(preview, 0, 0, 184, 100));
        } else {
            panel.addChildren(Ae2CanvasFactory.label(text("matter_fabrication"), 8, 5, 168),
                    Ae2CanvasFactory.at(new UIElement().style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90))),
                            98, 44, 22, 14));
        }
        int ingredientSize = array(root, "ingredients").size();
        int aeSize = array(root, "ae_inputs").size();
        int count = ingredientSize + aeSize + array(root, "catalysts").size();
        int position = 0;
        for (var value : array(root, "ingredients")) {
            panel.addChild(atInput(inputSlot(value.getAsJsonObject(), JEI_SLOT_SIZE), count, position++));
        }
        for (int i = 0; i < aeSize; i++) panel.addChild(atInput(aeSlot("ae_inputs", i), count, position++));
        for (var value : array(root, "catalysts")) {
            var slot = inputSlot(value.getAsJsonObject(), JEI_SLOT_SIZE);
            slot.style(style -> style.tooltips(text("catalysts")));
            panel.addChild(atInput(slot, count, position++));
        }
        var fluidInput = createFluidInputSlot(0).setId("omnisequence_fluid_input");
        configureJeiOverlayFluidSlotVisual(fluidInputSlots[0]);
        if (!fluid("fluid_input").isEmpty()) panel.addChild(atSlot(fluidInput, count == 0 ? 34 : count > 9 ? 79 : 71, 41));
        int outputs = array(root, "results").size() + array(root, "ae_outputs").size() + (fluid("fluid_result").isEmpty() ? 0 : 1);
        position = 0;
        for (int i = 0; i < array(root, "results").size(); i++) {
            var slot = createOutputSlot(i, JEI_SLOT_SIZE).setId("omnisequence_output_" + i);
            configureJeiOverlaySlotVisual(visualOutputSlots[i]);
            panel.addChild(atOutput(slot, outputs, position++));
        }
        var fluidOutput = createFluidOutputSlot(0).setId("omnisequence_fluid_output");
        configureJeiOverlayFluidSlotVisual(fluidOutputSlots[0]);
        if (!fluid("fluid_result").isEmpty()) panel.addChild(atOutput(fluidOutput, outputs, position++));
        for (int i = 0; i < array(root, "ae_outputs").size(); i++) panel.addChild(atOutput(aeSlot("ae_outputs", i), outputs, position++));
        return panel;
    }

    private UIElement atInput(UIElement slot, int count, int index) {
        int columns = count > 9 ? 4 : count == 4 ? 2 : Math.min(3, count);
        int rowItems = Math.min(columns, count - index / columns * columns);
        int rows = (count + columns - 1) / columns;
        return atSlot(slot, 7 + ((count > 9 ? 4 : 3) - rowItems) * 9 + index % columns * 18,
                23 + (3 - rows) * 9 + index / columns * 18);
    }

    private UIElement atOutput(UIElement slot, int count, int index) {
        int x = Math.min(2, count - index / 2 * 2) == 1 ? 146 : 136 + index % 2 * 20;
        int y = count <= 2 ? 42 : count <= 4 ? 33 + index / 2 * 20 : 24 + index / 2 * 18;
        return atSlot(slot, x - 1, y - 1);
    }

    private UIElement atSlot(UIElement slot, int x, int y) {
        slot.style(style -> style.backgroundTexture(SpriteTexture.of(ResourceLocation.parse("ae2:textures/guis/terminal.png"))
                .setSprite(7, 17, 18, 18)));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        return Ae2CanvasFactory.at(slot, x, y, 18, 18).layout(l -> l.paddingAll(1));
    }

    private UIElement aeSlot(String name, int index) {
        var slot = new UIElement() {
            @Override
            public void drawBackgroundAdditional(GUIContext context) {
                super.drawBackgroundAdditional(context);
                AEStackIcon.draw(context.graphics,
                        decode(GenericStack.CODEC, array(root, name).get(index)), (int) getContentX(), (int) getContentY());
            }
        }.setId("omnisequence_" + name + "_" + index);
        var stack = decode(GenericStack.CODEC, array(root, name).get(index));
        slot.style(style -> style.tooltips(stack.what().getDisplayName(), Component.literal(Long.toString(stack.amount()))));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> selectRecipe());
        return slot;
    }

    private ItemSlot inputSlot(JsonObject row, int size) {
        int index = inputs.size();
        inputs.add(row);
        var ingredient = decode(Ingredient.CODEC, row.get("ingredient"));
        var simple = simpleIngredient(ingredient);
        originalInputs.add(simple);
        var slot = new IngredientDisplaySlot() {
            @Override
            public List<Component> getFullTooltipTexts(boolean withItemTooltips) {
                var tips = new ArrayList<>(super.getFullTooltipTexts(withItemTooltips));
                tips.add(Component.translatable("gui.molecularmanipulator.jei_required_count", DisplayNumbers.exact(ingredientCount(row))));
                return tips;
            }

            @Override
            protected void drawItemStack(GUIContext context, ItemStack stack) {
                context.pose.pushPose();
                context.pose.translate(0, 0, 232);
                AEStackIcon.draw(context.graphics,
                        new GenericStack(AEItemKey.of(stack), ingredientCount(row)), 0, 0);
                context.graphics.flush();
                context.pose.popPose();
            }
        };
        slot.layout(l -> l.width(size).height(size));
        if (simple != null) {
            visualIngredientSlots[index] = slot;
            slot.xeiPhantom();
            configureIngredientSlot(index);
        } else {
            slot.setTagDisplayStacks(ingredient.getItems());
            slot.addEventListener(UIEvents.MOUSE_DOWN, e -> selectSlot(SlotSelection.ingredient(index)));
        }
        if (fabrication()) configureJeiOverlaySlotVisual(slot);
        slot.slotStyle(style -> style.showItemTooltips(true));
        slot.setId("omnisequence_input_" + index).addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation());
        return slot;
    }

    private static RecipeIngredient simpleIngredient(Ingredient ingredient) {
        if (ingredient.isCustom() || ingredient.getValues().length != 1) return null;
        return switch (ingredient.getValues()[0]) {
            case Ingredient.ItemValue value -> RecipeIngredient.item(value.item().copyWithCount(1));
            case Ingredient.TagValue value -> RecipeIngredient.tag(value.tag().location());
            default -> null;
        };
    }

    @Override
    public void load() {
        for (int i = 0; i < inputs.size(); i++) if (originalInputs.get(i) != null) loadIngredientSlot(i, originalInputs.get(i));
        if (fabrication()) {
            for (int i = 0; i < array(root, "results").size(); i++) {
                var stack = decode(ItemStack.CODEC, array(root, "results").get(i));
                originalOutputs.add(stack.copy()); setVisualOutput(i, stack);
            }
            setVisualFluidInput(0, FluidIngredientData.fluid(fluid("fluid_input")));
            setVisualFluidOutput(0, fluid("fluid_result"));
        }
        validate();
    }

    @Override
    public void save() {
        for (int i = 0; i < inputs.size(); i++) {
            var original = originalInputs.get(i);
            if (original == null) continue;
            var value = getVisualIngredient(i).setCount(1);
            var provider = com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry();
            if (!value.serializeNBT(provider).equals(original.serializeNBT(provider))) {
                inputs.get(i).add("ingredient", encode(Ingredient.CODEC, value.compile()));
                originalInputs.set(i, value.copy());
            }
        }
        if (fabrication()) {
            for (int i = 0; i < originalOutputs.size(); i++) {
                var stack = getVisualOutput(i).getItem();
                if (!ItemStack.matches(stack, originalOutputs.get(i))) {
                    array(root, "results").set(i, encode(ItemStack.OPTIONAL_CODEC, stack));
                    originalOutputs.set(i, stack.copy());
                }
            }
            writeFluid("fluid_input", getVisualFluidInput(0).getFluid());
            writeFluid("fluid_result", getVisualFluidOutput(0));
        }
        persist();
    }

    private FluidStack fluid(String name) { return root.has(name) ? decode(FluidStack.OPTIONAL_CODEC, root.get(name)) : FluidStack.EMPTY; }
    private void writeFluid(String name, FluidStack value) {
        if (FluidStack.matches(fluid(name), value)) return;
        if (value.isEmpty()) root.remove(name); else root.add(name, encode(FluidStack.CODEC, value));
    }
    private void persist() { getData().setRecipeJson(root.toString()); validate(); }
    private void validate() {
        try {
            var compiled = getData().compile(entry.getType());
            if (compiled instanceof MatterFabricationRecipe recipe) updatePreview.accept(recipe);
            status.setText(Component.empty());
            status.setDisplay(false);
        }
        catch (IllegalArgumentException | IllegalStateException exception) {
            status.setText(text("invalid", exception.getMessage()));
            status.setDisplay(true);
        }
    }
    private void rebuild(Runnable change) {
        save(); change.run(); persist(); navigationView.loadCanvas(); reloadProperties();
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        costProperties(content, inputs.get(index), index);
    }

    private void costProperties(UIElement content, JsonObject row, int slotIndex) {
        var simple = slotIndex >= 0 ? originalInputs.get(slotIndex) : simpleIngredient(decode(Ingredient.CODEC, row.get("ingredient")));
        if (simple == null) {
            var group = new ConfiguratorGroup(key("ingredients"), false);
            group.configuratorContainer.addChildren(RecipeEditorUi.label(text("preserved_ingredient")),
                    RecipeEditorUi.textButton(text("replace_ingredient"), null, e -> rebuild(() ->
                            row.add("ingredient", encode(Ingredient.CODEC, Ingredient.of(Items.IRON_INGOT))))));
            group.addConfigurators(costCount(row));
            content.addChild(group);
            return;
        }
        content.addChild(selector(key("ingredient_kind"), List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG), simple.getKind(),
                IngredientValueKind::displayName, kind -> rebuild(() -> row.add("ingredient", encode(Ingredient.CODEC,
                        kind == IngredientValueKind.TAG ? RecipeIngredient.tag("c:ingots/iron").compile() : Ingredient.of(Items.IRON_INGOT))))));
        if (simple.getKind() == IngredientValueKind.TAG) {
            content.addChildren(PropertiesView.createItemTagConfigurator(simple.getTag(), tag -> {
                var value = RecipeIngredient.tag(tag.location());
                row.add("ingredient", encode(Ingredient.CODEC, value.compile()));
                if (slotIndex >= 0) { originalInputs.set(slotIndex, value.copy()); setVisualIngredient(slotIndex, value); }
                persist();
            }), costCount(row));
            return;
        }
        var group = itemConfigurator(key("item"),
                () -> new ViScriptItemStack((slotIndex >= 0 ? getVisualIngredient(slotIndex) : simple)
                        .toStack().copyWithCount(fabrication() ? (int) ingredientCount(row) : 1)),
                value -> {
                    var stack = value.toItemStack();
                    if (stack.isEmpty()) return;
                    var changed = RecipeIngredient.item(stack.copyWithCount(1));
                    if (slotIndex < 0) simple.setItem(stack);
                    row.add("ingredient", encode(Ingredient.CODEC, changed.compile()));
                    if (fabrication()) row.addProperty("count", stack.getCount());
                    if (slotIndex >= 0) { originalInputs.set(slotIndex, changed.copy()); setVisualIngredient(slotIndex, changed); }
                    persist();
                });
        if (!fabrication()) {
            PropertiesView.removeCountConfig(group);
            group.addConfigurators(costCount(row));
        } else {
            for (var child : group.getConfigurators()) if (child instanceof NumberConfigurator number) {
                number.setRange(1, Integer.MAX_VALUE);
                number.textField.setId("omnisequence_ingredient_count");
            }
        }
        content.addChild(group.setId("omnisequence_item_stack"));
    }

    private NumberConfigurator costCount(JsonObject row) {
        var count = new NumberConfigurator(key("count"), () -> ingredientCount(row), value -> {
            row.addProperty("count", value.longValue()); persist();
        }, 1L, true).setType(fabrication() ? ConfigNumber.Type.INTEGER : ConfigNumber.Type.LONG)
                .setRange(1, fabrication() ? Integer.MAX_VALUE : Long.MAX_VALUE);
        count.textField.setId("omnisequence_ingredient_count");
        return count;
    }

    private ConfiguratorGroup itemConfigurator(String name, Supplier<ViScriptItemStack> supplier,
                                               Consumer<ViScriptItemStack> consumer) {
        var group = (ConfiguratorGroup) new ViScriptItemStackAccessor().create(name, supplier, consumer, true, null, null);
        group.setCollapse(false);
        group.layout(l -> l.widthPercent(100));
        return group;
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChild(itemConfigurator("viscript_recipe.config.recipe.result",
                () -> new ViScriptItemStack(getSelectedOutput().getItem()), value -> setSelectedOutput(value.toItemStack())));
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        // 构筑井只接受精确 FluidStack，不提供无法由原生类型表示的流体标签。
        boolean input = selectedSlotIndex() == 0;
        content.addChild(PropertiesView.createFluidStackConfigurator(key(input ? "fluid_input" : "fluid_result"),
                () -> input ? getVisualFluidInput(0).getFluid() : getVisualFluidOutput(0), value -> {
                    if (input) setVisualFluidInput(0, FluidIngredientData.fluid(value)); else setVisualFluidOutput(0, value);
                }));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        if (fabrication()) {
            integer(content, root, "processing_time", 400, 1);
            power(content, root, 64);
            for (var name : List.of("fluid_input", "fluid_result")) content.addChild(
                    PropertiesView.createFluidStackConfigurator(key(name), () -> fluid(name),
                            value -> rebuild(() -> writeFluid(name, value))));
            content.addChild(switchField(key("requires_research"), bool(root, "requires_research"), value -> { root.addProperty("requires_research", value); persist(); }));
            countList(content, root, "ingredients", 12 - array(root, "catalysts").size() - array(root, "ae_inputs").size(), OmniSequenceRecipeFactory::defaultCost);
            countList(content, root, "catalysts", 12 - array(root, "ingredients").size() - array(root, "ae_inputs").size(), OmniSequenceRecipeFactory::defaultCost);
            countList(content, root, "results", 6, () -> encode(ItemStack.CODEC, new ItemStack(Items.GOLD_INGOT)));
            aeResources(content, "ae_inputs", 12 - array(root, "ingredients").size() - array(root, "catalysts").size());
            aeResources(content, "ae_outputs", 6);
        } else {
            if (machineImport()) {
                resource(content, root, "machine");
                stringList(content, root, "serializers", true, "minecraft:smelting");
                integer(content, root, "processing_time", 400, 1); power(content, root, 256);
                mapping(content, "inputs"); mapping(content, "outputs");
            }
            researchProperties(content, research());
        }
    }

    private void researchProperties(UIElement content, JsonObject research) {
        content.addChild(textField(key("title"), string(research, "title", ""), value -> { research.addProperty("title", value); persist(); }));
        integer(content, research, "duration", 1200, 1); power(content, research, 256);
        integer(content, research, "stage", 1, 0); integer(content, research, "sort_order", 0, Integer.MIN_VALUE);
        countList(content, research, "ingredients", MAX_INGREDIENT, OmniSequenceRecipeFactory::defaultCost);
        stringList(content, research, "prerequisites", true, "molecularmanipulator:research/ae_foundation");
        prerequisiteLevels(content, research);
        stringList(content, research, "unlocks", true, "molecularmanipulator:example");
        stringList(content, research, "required_mods", false, "ae2");
        if (!research.has("depths")) research.add("depths", encode(
                com.atir.molecularmanipulator.research.ResearchDepth.CODEC.listOf(),
                com.atir.molecularmanipulator.research.ResearchDepth.DEFAULTS));
        var depths = array(research, "depths");
        countList(content, research, "depths", 256, () -> {
            var row = new JsonObject(); row.addProperty("parallel", 1); return row;
        });
        for (int i = 0; i < depths.size(); i++) {
            var row = depths.get(i).getAsJsonObject();
            var group = new ConfiguratorGroup("", true); group.setLabel(text("depth", i + 1));
            for (var name : List.of("material_multiplier", "parallel", "speed_multiplier")) {
                longNumber(group.configuratorContainer, row, name, 1, 1);
            }
            integer(group.configuratorContainer, row, "processing_ticks", 0, 0);
            group.configuratorContainer.addChild(switchField(key("custom_costs"), row.has("ingredients"), value -> rebuild(() -> {
                if (value) row.add("ingredients", new JsonArray()); else row.remove("ingredients");
            })));
            if (row.has("ingredients")) inlineCosts(group.configuratorContainer, row);
            content.addChild(group);
        }
    }

    private void inlineCosts(UIElement content, JsonObject owner) {
        var costs = array(owner, "ingredients");
        countList(content, owner, "ingredients", MAX_INGREDIENT, OmniSequenceRecipeFactory::defaultCost);
        for (var cost : costs) costProperties(content, cost.getAsJsonObject(), -1);
    }

    private void prerequisiteLevels(UIElement content, JsonObject research) {
        var levels = object(research, "prerequisite_levels");
        content.addChild(sectionTitle(key("prerequisite_levels")));
        for (var id : List.copyOf(levels.keySet())) {
            content.addChild(RecipeEditorUi.label(Component.literal(id)));
            int level = levels.get(id).getAsString().equals("max") ? 0 : levels.get(id).getAsInt();
            content.addChild(field(key("level"), RecipeEditorUi.intField(level, 0, Integer.MAX_VALUE, value -> {
                if (value == 0) levels.addProperty(id, "max"); else levels.addProperty(id, value);
                persist();
            })));
            content.addChild(RecipeEditorUi.textButton(text("remove"), null, e -> rebuild(() -> levels.remove(id))));
        }
        var newId = new ResourceLocation[]{ResourceLocation.parse("molecularmanipulator:research/ae_foundation")};
        content.addChildren(resourceField(key("prerequisite"), newId[0], value -> newId[0] = value),
                RecipeEditorUi.textButton(text("add_prerequisite_level"), null, e -> rebuild(() -> levels.addProperty(newId[0].toString(), 1))));
        content.addChild(RecipeEditorUi.label(text("level_hint")));
    }

    private void aeResources(UIElement content, String name, int maximum) {
        var resources = array(root, name);
        countList(content, root, name, maximum, () -> encode(GenericStack.CODEC, new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)));
        for (int i = 0; i < resources.size(); i++) {
            int index = i;
            var stack = decode(GenericStack.CODEC, resources.get(i));
            var group = new ConfiguratorGroup("", true); group.setLabel(text("resource", i + 1, stack.what().getDisplayName()));
            var fields = group.configuratorContainer;
            Consumer<GenericStack> update = value -> { resources.set(index, encode(GenericStack.CODEC, value)); persist(); };
            fields.addChild(selector(key("resource_kind"), List.of("item", "fluid", "preserved"),
                    stack.what() instanceof AEItemKey ? "item" : stack.what() instanceof AEFluidKey ? "fluid" : "preserved",
                    OmniSequenceRecipeCanvas::text, kind -> {
                        if (!kind.equals("preserved")) rebuild(() -> resources.set(index, encode(GenericStack.CODEC, new GenericStack(
                                kind.equals("item") ? AEItemKey.of(Items.IRON_INGOT) : AEFluidKey.of(Fluids.WATER),
                                decode(GenericStack.CODEC, resources.get(index)).amount()))));
                    }));
            ConfiguratorGroup itemGroup = null;
            if (stack.what() instanceof AEItemKey item) {
                itemGroup = itemConfigurator(key("item"),
                        () -> new ViScriptItemStack(decode(GenericStack.CODEC, resources.get(index)).what() instanceof AEItemKey current ? current.toStack(1) : item.toStack(1)),
                        value -> { var resource = AEItemKey.of(value.toItemStack()); if (resource != null) update.accept(new GenericStack(resource, decode(GenericStack.CODEC, resources.get(index)).amount())); });
                PropertiesView.removeCountConfig(itemGroup);
                fields.addChild(itemGroup);
            } else if (stack.what() instanceof AEFluidKey fluid) fields.addChild(PropertiesView.removeCountConfig(PropertiesView.createFluidStackConfigurator(key("fluid"),
                    () -> decode(GenericStack.CODEC, resources.get(index)).what() instanceof AEFluidKey current ? current.toStack(1) : fluid.toStack(1),
                    value -> { var resource = AEFluidKey.of(value); if (resource != null) update.accept(new GenericStack(resource, decode(GenericStack.CODEC, resources.get(index)).amount())); })));
            else fields.addChild(RecipeEditorUi.label(text("preserved_resource")));
            var amount = new NumberConfigurator(key("count"), () -> decode(GenericStack.CODEC, resources.get(index)).amount(), value -> {
                var current = decode(GenericStack.CODEC, resources.get(index)); update.accept(new GenericStack(current.what(), value.longValue()));
            }, 1L, true).setType(ConfigNumber.Type.LONG).setRange(1L, Long.MAX_VALUE);
            if (itemGroup != null) itemGroup.addConfigurators(amount); else fields.addChild(amount);
            content.addChild(group);
        }
    }

    private void mapping(UIElement content, String name) {
        content.addChild(switchField(key(name + "_mapping"), root.has(name), enabled -> rebuild(() -> {
            if (!enabled) root.remove(name);
            else { var fields = new JsonObject(); var items = new JsonArray(); items.add(name.equals("inputs") ? "/ingredients" : "/result"); fields.add("items", items); root.add(name, fields); }
        })));
        if (!root.has(name)) return;
        var fields = root.getAsJsonObject(name);
        for (var kind : List.of("items", "fluids", "resources")) stringList(content, fields, kind, false, "/" + kind);
        if (!array(fields, "ae_keys").isEmpty()) content.addChild(RecipeEditorUi.label(text("preserved_mappings", array(fields, "ae_keys").size())));
        content.addChild(RecipeEditorUi.label(text("mapping_hint")));
    }

    private void stringList(UIElement content, JsonObject owner, String name, boolean resource, String defaultValue) {
        var values = array(owner, name);
        content.addChild(sectionTitle(key(name)));
        for (int i = 0; i < values.size(); i++) {
            int index = i;
            var row = RecipeEditorUi.row().layout(l -> l.gapAll(2));
            UIElement input = resource ? RecipeEditorUi.resourceLocationField(ResourceLocation.parse(values.get(i).getAsString()), value -> {
                values.set(index, new com.google.gson.JsonPrimitive(value.toString())); persist();
            }) : RecipeEditorUi.textField(values.get(i).getAsString(), value -> { values.set(index, new com.google.gson.JsonPrimitive(value)); persist(); });
            row.addChildren(input.layout(l -> l.flex(1).minWidth(0)),
                    RecipeEditorUi.textButton(text("remove"), null, e -> rebuild(() -> values.remove(index))));
            content.addChild(row);
        }
        content.addChild(RecipeEditorUi.textButton(text("add"), null, e -> rebuild(() -> values.add(defaultValue))).setId("omnisequence_add_" + name));
    }

    private void countList(UIElement content, JsonObject owner, String name, int max, java.util.function.Supplier<JsonElement> factory) {
        var values = array(owner, name);
        content.addChild(intField(key(name + "_count"), values.size(), 0, Math.max(max, values.size()), value -> rebuild(() -> {
            while (values.size() < value) values.add(factory.get());
            while (values.size() > value) values.remove(values.size() - 1);
        })).setId("omnisequence_" + name + "_count"));
    }
    private void integer(UIElement content, JsonObject owner, String name, int fallback, int min) {
        content.addChild(intField(key(name), owner.has(name) ? owner.get(name).getAsInt() : fallback, min, Integer.MAX_VALUE, value -> {
            owner.addProperty(name, value); persist();
        }).setId("omnisequence_" + name));
    }
    private void longNumber(UIElement content, JsonObject owner, String name, long fallback, long min) {
        content.addChild(field(key(name), RecipeEditorUi.longField(owner.has(name) ? owner.get(name).getAsLong() : fallback, min, Long.MAX_VALUE, value -> {
            owner.addProperty(name, value); persist();
        })));
    }
    private void power(UIElement content, JsonObject owner, double fallback) {
        content.addChild(field(key("ae_per_tick"), RecipeEditorUi.doubleField(owner.has("ae_per_tick") ? owner.get("ae_per_tick").getAsDouble() : fallback,
                0, Double.MAX_VALUE, value -> { if (Double.isFinite(value)) { owner.addProperty("ae_per_tick", value); persist(); } })));
    }
    private void resource(UIElement content, JsonObject owner, String name) {
        content.addChild(resourceField(key(name), ResourceLocation.parse(owner.get(name).getAsString()), value -> {
            owner.addProperty(name, value.toString()); persist();
        }));
    }
    private static long ingredientCount(JsonObject row) {
        // 构筑井省略默认数量 1，研究配方则使用字符串保留长整型数量的精度。
        return row.has("count") ? row.get("count").getAsLong() : 1L;
    }
    private static String string(JsonObject owner, String name, String fallback) { return owner.has(name) ? owner.get(name).getAsString() : fallback; }
    private static boolean bool(JsonObject owner, String name) { return owner.has(name) && owner.get(name).getAsBoolean(); }
    private static JsonArray array(JsonObject owner, String name) {
        if (!owner.has(name)) owner.add(name, new JsonArray());
        return owner.getAsJsonArray(name);
    }
    private static JsonObject object(JsonObject owner, String name) {
        if (!owner.has(name)) owner.add(name, new JsonObject());
        return owner.getAsJsonObject(name);
    }
}
