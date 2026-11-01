package com.viscript_recipe.compat.ae2lt.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.viscript_lib.gui.configurator.ViScriptItemStackAccessor;
import com.viscript_lib.util.item.ViScriptItemStack;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory;
import com.viscript_recipe.compat.ae2lt.data.Ae2LightningRecipeData;
import com.viscript_recipe.compat.jei.ae2lt.Ae2LightningJeiCanvas;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeEditorTypes.key;
import static com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeFactory.decode;
import static com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeFactory.encode;

/** 在原生 JEI 槽位上复用幻影槽、属性栏、拖拽与 VSL 物品堆编辑。 */
public final class Ae2LightningRecipeCanvas extends FluidRecipeCanvas<Ae2LightningRecipeData> {
    private JsonObject root;
    private final List<Input> inputs = new ArrayList<>();
    private final List<RecipeIngredient> originalInputs = new ArrayList<>();
    private final List<ItemStack> originalOutputs = new ArrayList<>();
    private final Map<Integer, Integer> alternatives = new HashMap<>();
    private final Label status = new Label();
    private Ae2LightningJeiCanvas preview;
    private boolean loaded;
    private boolean nativeLayout;

    private record Input(JsonObject parent, String ingredient, String count) {
        JsonElement json() { return parent.get(ingredient); }
        int amount() { return parent.has(count) ? parent.get(count).getAsInt() : 1; }
    }

    public Ae2LightningRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private String type() { return entry.getType().getPath(); }
    private boolean crystal() { return type().equals("crystal_catalyzer"); }
    private boolean strike() { return type().equals("lightning_strike"); }
    private boolean multipleResults() { return type().equals("overload_processing") || type().equals("firmament_conversion"); }
    private boolean nativePreview() { return preview != null && preview.isAvailable(); }
    private static Component text(String name, Object... args) { return Component.translatable(key(name), args); }

    @Override
    public UIElement createCanvas() {
        loaded = false;
        root = JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        inputs.clear(); originalInputs.clear(); originalOutputs.clear();
        if (ViScriptRecipe.isModLoaded("jei")) {
            preview = new Ae2LightningJeiCanvas();
            updatePreview();
        }
        nativeLayout = nativePreview();
        int width = nativePreview() ? preview.recipeWidth() : 176;
        int height = nativePreview() ? preview.recipeHeight() : 90;
        var body = RecipeEditorUi.column().setId("ae2lt_canvas").layout(l -> l.width(Math.max(200, width + 12))
                .gapAll(10).alignItems(AlignItems.CENTER).flexShrink(0));
        var panel = Ae2CanvasFactory.panel("ae2lt_jei_panel", width, height);
        if (nativePreview()) panel.addChild(Ae2CanvasFactory.at(preview, 0, 0, width, height));
        else panel.addChild(Ae2CanvasFactory.arrow(85, 32));
        if (strike()) {
            blockSlot(panel, "center_input", true);
            blockSlot(panel, "center_output", false);
        } else {
            if (crystal()) {
                if (root.has("catalyst")) inputs.add(new Input(root, "catalyst", "catalystCount"));
            } else for (var value : array("inputs")) inputs.add(new Input(value.getAsJsonObject(), "ingredient", "count"));
            for (int i = 0; i < inputs.size(); i++) {
                var slot = createIngredientSlot(i, JEI_SLOT_SIZE, new IngredientDisplaySlot() {
                    @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                        if (!nativeLayout || !nativePreview() || !preview.drawsIngredients()) super.drawItemStack(context, stack);
                    }
                });
                place(panel, slot, true, false, i);
            }
            for (int i = 0; i < outputCount(); i++) {
                var slot = createOutputSlot(i, JEI_SLOT_SIZE, new ItemSlot() {
                    @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                        if (!nativeLayout || !nativePreview() || !preview.drawsIngredients()) super.drawItemStack(context, stack);
                    }
                });
                place(panel, slot, false, false, i);
            }
            if (crystal() || root.has("inputFluid")) fluidSlot(panel, true);
            if (root.has("resultFluid")) fluidSlot(panel, false);
        }
        body.addChildren(panel, status.setId("ae2lt_validation").setDisplay(false)
                .layout(l -> l.widthPercent(100).maxWidth(300)));
        return Ae2CanvasFactory.centered(body).setId("ae2lt_canvas_container");
    }

    private void place(UIElement panel, UIElement slot, boolean input, boolean fluid, int index) {
        List<Rect2i> positions = nativePreview() ? preview.slots(input, fluid) : List.of();
        var bounds = index < positions.size() ? positions.get(index)
                : new Rect2i(input ? 8 + index % 3 * 20 : 124 + index % 2 * 20,
                        8 + index / (input ? 3 : 2) * 20, 16, 16);
        if (slot instanceof ItemSlot item && nativePreview()) configureJeiOverlaySlotVisual(item);
        if (slot instanceof FluidSlot tank && nativePreview()) configureJeiOverlayFluidSlotVisual(tank);
        slot.setId("ae2lt_" + (fluid ? "fluid_" : "") + (input ? "input_" : "output_") + index);
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.addChild(Ae2CanvasFactory.at(slot, bounds.getX() - 1, bounds.getY() - 1,
                bounds.getWidth() + 2, bounds.getHeight() + 2));
    }

    private void fluidSlot(UIElement panel, boolean input) {
        FluidSlot slot;
        if (input) {
            fluidInputSlots[0] = new FluidDisplaySlot() {
                @Override protected void drawFluid(GUIContext context, FluidStack fluid, float x, float y, float w, float h) {
                    if (!nativeLayout || !nativePreview() || !preview.drawsIngredients()) super.drawFluid(context, fluid, x, y, w, h);
                }
            };
            slot = configureFluidInputSlot(0);
        } else {
            fluidOutputSlots[0] = new FluidSlot() {
                @Override protected void drawFluid(GUIContext context, FluidStack fluid, float x, float y, float w, float h) {
                    if (!nativeLayout || !nativePreview() || !preview.drawsIngredients()) super.drawFluid(context, fluid, x, y, w, h);
                }
            };
            slot = configureFluidOutputSlot(0);
        }
        slot.xeiPhantom().setAllowClickFilled(false).setAllowClickDrained(false);
        place(panel, slot, input, true, 0);
    }

    private void blockSlot(UIElement panel, String field, boolean input) {
        var slot = new ItemSlot() {
            @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                if (!nativeLayout || !nativePreview() || !preview.drawsIngredients()) super.drawItemStack(context, stack);
            }
        };
        slot.xeiPhantom().setItem(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(root.get(field).getAsString())).asItem().getDefaultInstance(), false);
        slot.registerValueListener(stack -> {
            if (stack.getItem() instanceof BlockItem block) {
                root.addProperty(field, BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString()); persist();
            } else slot.setItem(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(root.get(field).getAsString()))
                    .asItem().getDefaultInstance(), false);
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, e -> selectRecipe());
        place(panel, slot, input, false, 0);
    }

    @Override
    public void load() {
        for (int i = 0; i < inputs.size(); i++) {
            var value = displayIngredient(i);
            originalInputs.add(value.copy());
            loadIngredientSlot(i, value);
        }
        if (!strike()) for (int i = 0; i < outputCount(); i++) {
            var value = outputStack(i);
            originalOutputs.add(value.copy()); setVisualOutput(i, value);
        }
        if (fluidInputSlots[0] != null) setVisualFluidInput(0, FluidIngredientData.fluid(fluid("inputFluid")));
        if (fluidOutputSlots[0] != null) setVisualFluidOutput(0, fluid("resultFluid"));
        loaded = true;
        updatePreview();
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (loaded) {
            save();
            if (!nativeLayout && nativePreview()) navigationView.loadCanvas();
        }
    }

    @Override
    public void save() {
        if (!loaded) return;
        for (int i = 0; i < inputs.size(); i++) {
            var value = getVisualIngredient(i);
            if (!value.serializeNBT(Platform.getFrozenRegistry()).equals(originalInputs.get(i).serializeNBT(Platform.getFrozenRegistry()))) {
                if (!value.isEmpty()) {
                    writeIngredient(i, encode(Ingredient.CODEC, value.compile()));
                    inputs.get(i).parent().addProperty(inputs.get(i).count(), value.getCount());
                } else inputs.get(i).parent().add(inputs.get(i).ingredient(), new JsonArray());
                originalInputs.set(i, value.copy());
            }
        }
        for (int i = 0; i < originalOutputs.size(); i++) {
            var value = getVisualOutput(i).getItem();
            if (!ItemStack.matches(value, originalOutputs.get(i))) {
                writeOutput(i, stackJson(value)); originalOutputs.set(i, value.copy());
            }
        }
        if (fluidInputSlots[0] != null) writeFluid("inputFluid", getVisualFluidInput(0).getFluid());
        if (fluidOutputSlots[0] != null) writeFluid("resultFluid", getVisualFluidOutput(0));
        if (!root.toString().equals(getData().getRecipeJson())) persist();
    }

    private JsonElement selectedIngredient(int index) {
        var value = inputs.get(index).json();
        if (!value.isJsonArray()) return value;
        var array = value.getAsJsonArray();
        return array.isEmpty() ? new JsonObject() : array.get(Math.min(alternatives.getOrDefault(index, 0), array.size() - 1));
    }

    private RecipeIngredient displayIngredient(int index) {
        var json = selectedIngredient(index);
        if (json.isJsonObject()) {
            var obj = json.getAsJsonObject();
            if (obj.has("tag")) return RecipeIngredient.tag(obj.get("tag").getAsString()).setCount(inputs.get(index).amount());
            if (obj.has("item")) return RecipeIngredient.item(BuiltInRegistries.ITEM.get(ResourceLocation.parse(obj.get("item").getAsString()))
                    .getDefaultInstance()).setCount(inputs.get(index).amount());
        }
        var stacks = decode(Ingredient.CODEC, inputs.get(index).json()).getItems();
        return stacks.length == 0 ? RecipeIngredient.empty() : RecipeIngredient.item(stacks[0].copy()).setCount(inputs.get(index).amount());
    }

    private void writeIngredient(int index, JsonElement json) {
        var input = inputs.get(index);
        if (input.json().isJsonArray() && !input.json().getAsJsonArray().isEmpty()) {
            var values = input.json().getAsJsonArray();
            values.set(Math.min(alternatives.getOrDefault(index, 0), values.size() - 1), json);
        } else input.parent().add(input.ingredient(), json);
    }

    private int outputCount() { return multipleResults() ? array("results").size() : 1; }
    private JsonElement output(int index) { return multipleResults() ? array("results").get(index) : root.get(crystal() ? "output" : "result"); }
    private void writeOutput(int index, JsonElement json) {
        if (multipleResults()) array("results").set(index, json); else root.add(crystal() ? "output" : "result", json);
    }
    private ItemStack outputStack(int index) {
        var json = output(index).getAsJsonObject().deepCopy();
        if (!json.has("id")) {
            if (!json.has("tag") && !json.has("harvestBlock")) return ItemStack.EMPTY;
            return decode(com.moakiee.ae2lt.machine.crystalcatalyzer.recipe.CrystalCatalyzerOutput.CODEC, json).resolve();
        }
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        json.addProperty("count", 1);
        return decode(ItemStack.CODEC, json).copyWithCount(count);
    }
    private static JsonElement stackJson(ItemStack stack) {
        if (stack.isEmpty()) return new JsonObject();
        var json = encode(ItemStack.CODEC, stack.copyWithCount(1)).getAsJsonObject();
        json.addProperty("count", stack.getCount()); return json;
    }
    private JsonArray array(String name) { return root.has(name) ? root.getAsJsonArray(name) : new JsonArray(); }
    private FluidStack fluid(String name) {
        return root.has(name) ? decode(FluidStack.OPTIONAL_CODEC, root.get(name))
                : crystal() && name.equals("inputFluid") ? new FluidStack(Fluids.WATER, 1000) : FluidStack.EMPTY;
    }
    private void writeFluid(String name, FluidStack value) {
        if (FluidStack.matches(fluid(name), value)) return;
        if (value.isEmpty() && !crystal()) root.remove(name);
        else root.add(name, encode(FluidStack.OPTIONAL_CODEC, value));
    }
    private void persist() { getData().setRecipeJson(root.toString()); updatePreview(); }
    private void updatePreview() {
        try {
            var recipe = getData().compile(entry.getType());
            if (preview != null) preview.setRecipe(recipe);
            status.setText(Component.empty());
            status.setDisplay(false);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (preview != null) preview.invalidateRecipe();
            status.setText(text("invalid"));
            status.setDisplay(true);
            status.style(style -> style.tooltips(Component.literal(exception.getMessage())));
        }
    }
    private void rebuild(Runnable change) { save(); change.run(); persist(); navigationView.loadCanvas(); reloadProperties(); }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var input = inputs.get(index);
        var values = input.json();
        if (values.isJsonArray() && !values.getAsJsonArray().isEmpty()) {
            int size = values.getAsJsonArray().size();
            content.addChild(intField(key("alternative"), alternatives.getOrDefault(index, 0) + 1, 1, size, value -> {
                save(); alternatives.put(index, value - 1);
                var selected = displayIngredient(index); originalInputs.set(index, selected.copy()); setVisualIngredient(index, selected); reloadProperties();
            }));
            if (size > 1) content.addChild(RecipeEditorUi.textButton(text("remove_alternative"), null, e -> rebuild(() -> {
                values.getAsJsonArray().remove(Math.min(alternatives.getOrDefault(index, 0), size - 1));
                alternatives.put(index, 0);
            })));
        }
        var selected = selectedIngredient(index);
        if (!selected.isJsonObject() || (!selected.getAsJsonObject().has("item") && !selected.getAsJsonObject().has("tag"))
                || selected.getAsJsonObject().has("type")) {
            content.addChildren(RecipeEditorUi.label(text("preserved_ingredient")), RecipeEditorUi.textButton(text("replace_ingredient"), null,
                    e -> rebuild(() -> writeIngredient(index, encode(Ingredient.CODEC, Ingredient.of(Items.IRON_INGOT))))));
            countField(content, input); return;
        }
        var ingredient = displayIngredient(index);
        content.addChild(selector(key("ingredient_kind"), List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG), ingredient.getKind(),
                IngredientValueKind::displayName, kind -> {
                    var value = kind == IngredientValueKind.TAG ? RecipeIngredient.tag("c:ingots/iron") : RecipeIngredient.item(Items.IRON_INGOT.getDefaultInstance());
                    changeIngredient(index, value.setCount(input.amount())); reloadProperties();
                }));
        if (ingredient.getKind() == IngredientValueKind.TAG) {
            content.addChild(PropertiesView.createItemTagConfigurator(ingredient.getTag(), tag ->
                    changeIngredient(index, RecipeIngredient.tag(tag.location()).setCount(input.amount()))));
            countField(content, input);
        } else content.addChild(itemConfigurator(key("item"), () -> new ViScriptItemStack(getVisualIngredient(index).toStack().copyWithCount(input.amount())),
                value -> { if (!value.toItemStack().isEmpty()) changeIngredient(index, RecipeIngredient.item(value.toItemStack()).setCount(value.toItemStack().getCount())); }, true));
        content.addChild(RecipeEditorUi.textButton(text("add_alternative"), null, e -> rebuild(() -> {
            var list = input.json().isJsonArray() ? input.json().getAsJsonArray() : new JsonArray();
            if (!input.json().isJsonArray()) list.add(input.json());
            list.add(encode(Ingredient.CODEC, Ingredient.of(Items.IRON_INGOT))); input.parent().add(input.ingredient(), list);
        })));
    }

    private void changeIngredient(int index, RecipeIngredient value) {
        writeIngredient(index, encode(Ingredient.CODEC, value.compile()));
        var input = inputs.get(index); input.parent().addProperty(input.count(), value.getCount());
        originalInputs.set(index, value.copy()); setVisualIngredient(index, value); persist();
    }

    private void countField(UIElement content, Input input) {
        content.addChild(intField(key("count"), input.amount(), 1, Integer.MAX_VALUE, value -> {
            input.parent().addProperty(input.count(), value);
            int index = inputs.indexOf(input); var ingredient = displayIngredient(index);
            originalInputs.set(index, ingredient.copy()); setVisualIngredient(index, ingredient); persist();
        }).setId("ae2lt_count_field"));
    }

    private ConfiguratorGroup itemConfigurator(String name, Supplier<ViScriptItemStack> supplier,
                                               Consumer<ViScriptItemStack> consumer, boolean input) {
        var group = (ConfiguratorGroup) new ViScriptItemStackAccessor().create(name, supplier, consumer, true, null, null);
        group.setCollapse(false); group.layout(l -> l.widthPercent(100));
        for (var child : group.getConfigurators()) if (child instanceof NumberConfigurator number) {
            number.setRange(1, input || type().equals("overload_processing") ? Integer.MAX_VALUE : 99);
            number.textField.setId("ae2lt_stack_count");
        }
        group.setId("ae2lt_item_stack"); return group;
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (crystal()) {
            var json = output(0).getAsJsonObject();
            String mode = json.has("tag") ? "tag" : json.has("harvestBlock") ? "harvestBlock" : "id";
            content.addChild(selector(key("output_kind"), List.of("id", "tag", "harvestBlock"), mode, value -> text("output_" + value),
                    value -> rebuild(() -> {
                        var replacement = new JsonObject(); replacement.addProperty("count", json.has("count") ? json.get("count").getAsInt() : 1);
                        replacement.addProperty(value, value.equals("tag") ? "c:gems/amethyst" : value.equals("harvestBlock") ? "minecraft:amethyst_cluster" : "minecraft:amethyst_shard");
                        root.add("output", replacement);
                    })));
            if (!mode.equals("id")) {
                if (mode.equals("tag")) content.addChild(PropertiesView.createItemTagConfigurator(ResourceLocation.parse(json.get(mode).getAsString()),
                        tag -> { json.addProperty(mode, tag.location().toString()); persist(); }));
                else content.addChild(PropertiesView.createBlockConfigurator(key("harvestBlock"),
                        () -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(json.get(mode).getAsString())),
                        block -> { json.addProperty(mode, BuiltInRegistries.BLOCK.getKey(block).toString()); persist(); }));
                integer(content, json, "count", 1, 1, Integer.MAX_VALUE); return;
            }
        }
        content.addChild(itemConfigurator("viscript_recipe.config.recipe.result", () -> new ViScriptItemStack(getSelectedOutput().getItem()),
                value -> { setSelectedOutput(value.toItemStack()); save(); }, false));
    }

    @Override
    public FluidIngredientData getVisualFluidInput(int index) {
        return fluidInputSlots[index] == null ? FluidIngredientData.empty()
                : FluidIngredientData.fluid(fluidInputSlots[index].getFluid().copy());
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        boolean input = selectedSlotIndex() < 2;
        content.addChild(PropertiesView.createFluidStackConfigurator(key(input ? "inputFluid" : "resultFluid"),
                () -> input ? getVisualFluidInput(0).getFluid() : getVisualFluidOutput(0), value -> {
                    if (input) setVisualFluidInput(0, FluidIngredientData.fluid(value)); else setVisualFluidOutput(0, value);
                    save();
                }));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        if (strike()) { structureProperties(content); return; }
        if (!crystal()) integer(content, root, "priority", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type().equals("firmament_conversion")) integer(content, root, "processTime", 100, 1, Integer.MAX_VALUE);
        else if (!type().equals("lightning_transform")) {
            if (crystal()) integer(content, root, "energyPerCycle", 1000, 1, Integer.MAX_VALUE);
            else content.addChild(new NumberConfigurator(key("totalEnergy"), () -> root.get("totalEnergy").getAsLong(), value -> {
                root.addProperty("totalEnergy", value.longValue()); persist();
            }, 1000L, true).setType(ConfigNumber.Type.LONG).setRange(5L, Long.MAX_VALUE));
            integer(content, root, "lightningCost", crystal() ? 1 : 4, 1, Integer.MAX_VALUE);
            content.addChild(selector(key("lightningTier"), List.of("high_voltage", "extreme_high_voltage"),
                    root.has("lightningTier") ? root.get("lightningTier").getAsString() : "high_voltage", value -> text(value),
                    value -> { root.addProperty("lightningTier", value); persist(); }));
        }
        if (crystal()) {
            content.addChild(selector(key("mode"), List.of("crystal", "dust"), root.has("mode") ? root.get("mode").getAsString() : "crystal",
                    value -> text(value), value -> { root.addProperty("mode", value); persist(); }));
            content.addChild(switchField(key("catalyst"), root.has("catalyst"), value -> rebuild(() -> {
                if (value) { root.add("catalyst", encode(Ingredient.CODEC, Ingredient.of(Items.AMETHYST_SHARD))); root.addProperty("catalystCount", 1); }
                else { root.remove("catalyst"); root.remove("catalystCount"); }
            })));
        } else {
            int max = switch (type()) { case "lightning_simulation", "firmament_conversion" -> 3; case "lightning_assembly", "overload_processing" -> 9; default -> MAX_INGREDIENT; };
            listCount(content, "inputs", type().equals("overload_processing") ? 0 : 1, max,
                    () -> JsonParser.parseString("{\"ingredient\":{\"item\":\"minecraft:iron_ingot\"},\"count\":1}"));
            if (multipleResults()) listCount(content, "results", type().equals("overload_processing") ? 0 : 1,
                    type().equals("overload_processing") ? 1 : 4, () -> stackJson(Items.GOLD_INGOT.getDefaultInstance()));
        }
        if (crystal() || type().equals("overload_processing")) {
            content.addChild(PropertiesView.createFluidStackConfigurator(key("inputFluid"), () -> fluid("inputFluid"),
                    value -> rebuild(() -> writeFluid("inputFluid", value))));
            if (!crystal()) content.addChild(PropertiesView.createFluidStackConfigurator(key("resultFluid"), () -> fluid("resultFluid"),
                    value -> rebuild(() -> writeFluid("resultFluid", value))));
        }
    }

    private void structureProperties(UIElement content) {
        content.addChild(switchField(key("requires_natural_lightning"), root.has("requires_natural_lightning") && root.get("requires_natural_lightning").getAsBoolean(),
                value -> { root.addProperty("requires_natural_lightning", value); persist(); }));
        for (var field : List.of("center_input", "center_output")) content.addChild(PropertiesView.createBlockConfigurator(key(field),
                () -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(root.get(field).getAsString())),
                block -> rebuild(() -> root.addProperty(field, BuiltInRegistries.BLOCK.getKey(block).toString()))));
        listCount(content, "requirements", 0, 256, () -> JsonParser.parseString("{\"offset\":[1,0,0],\"block\":\"minecraft:copper_block\",\"consume\":false}"));
        for (int i = 0; i < array("requirements").size(); i++) {
            var row = array("requirements").get(i).getAsJsonObject();
            var group = new ConfiguratorGroup("", true); group.setLabel(text("requirement", i + 1));
            var target = group.configuratorContainer;
            target.addChild(PropertiesView.createBlockConfigurator(key("block"),
                    () -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(row.get("block").getAsString())),
                    block -> { row.addProperty("block", BuiltInRegistries.BLOCK.getKey(block).toString()); persist(); }));
            for (int axis = 0; axis < 3; axis++) {
                int offset = axis;
                target.addChild(intField(key(List.of("x", "y", "z").get(axis)), row.getAsJsonArray("offset").get(axis).getAsInt(), Integer.MIN_VALUE, Integer.MAX_VALUE,
                        value -> { row.getAsJsonArray("offset").set(offset, new com.google.gson.JsonPrimitive(value)); persist(); }));
            }
            target.addChild(switchField(key("consume"), row.has("consume") && row.get("consume").getAsBoolean(),
                    value -> { row.addProperty("consume", value); persist(); }));
            content.addChild(group);
        }
    }

    private void integer(UIElement content, JsonObject parent, String name, int fallback, int min, int max) {
        content.addChild(intField(key(name), parent.has(name) ? parent.get(name).getAsInt() : fallback, min, max,
                value -> { parent.addProperty(name, value); persist(); }));
    }
    private void listCount(UIElement content, String name, int min, int max, Supplier<JsonElement> factory) {
        content.addChild(intField(key(name), array(name).size(), min, Math.max(max, array(name).size()), value -> rebuild(() -> {
            var list = array(name); while (list.size() < value) list.add(factory.get()); while (list.size() > value) list.remove(list.size() - 1); root.add(name, list);
        })).setId("ae2lt_" + name + "_size"));
    }
}
