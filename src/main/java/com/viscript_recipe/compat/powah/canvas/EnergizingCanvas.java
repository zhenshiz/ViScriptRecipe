package com.viscript_recipe.compat.powah.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.viscript_lib.gui.configurator.ViScriptItemStackAccessor;
import com.viscript_lib.util.item.ViScriptItemStack;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory;
import com.viscript_recipe.compat.jei.powah.PowahJeiCanvas;
import com.viscript_recipe.compat.powah.data.PowahRecipeData;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import owmii.powah.block.energizing.EnergizingRecipe;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.powah.PowahRecipeEditorTypes.*;
import static com.viscript_recipe.compat.powah.PowahRecipeFactory.*;

/** 在原生 JEI 画布上复用幻影槽及 VSL 物品堆配置器。 */
public final class EnergizingCanvas extends RecipeCanvas<PowahRecipeData> {
    private JsonObject root;
    private final List<JsonElement> inputs = new ArrayList<>();
    private final List<RecipeIngredient> originalInputs = new ArrayList<>();
    private ItemStack originalOutput = ItemStack.EMPTY;
    private final Label fallbackEnergy = new Label();
    private PowahJeiCanvas preview;
    private boolean loaded;

    public EnergizingCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        loaded = false; preview = null;
        root = JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        inputs.clear(); originalInputs.clear();
        var array = root.getAsJsonArray("ingredients");
        for (int i = 0; i < 6; i++) inputs.add(i < array.size() ? array.get(i).deepCopy() : JsonNull.INSTANCE);
        var panel = Ae2CanvasFactory.panel("powah_canvas", 160, 39);
        panel.addChild(Ae2CanvasFactory.at(new UIElement().style(s -> s.backgroundTexture(
                SpriteTexture.of(id("textures/gui/jei/energizing.png")).setSprite(0, 0, 160, 38))), 0, 1, 160, 38));
        if (ViScriptRecipe.isModLoaded("jei")) {
            preview = new PowahJeiCanvas();
            panel.addChild(Ae2CanvasFactory.at(preview, 0, 0, 160, 39));
        }
        for (int i = 0; i < 6; i++) {
            int index = i;
            var slot = createIngredientSlot(i, JEI_SLOT_SIZE, new IngredientDisplaySlot() {
                @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                    if (!nativeDrawing()) super.drawItemStack(context, stack);
                }
            });
            slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1) { inputs.set(index, JsonNull.INSTANCE); save(); reloadProperties(); }
            });
            configureJeiOverlaySlotVisual(slot);
            panel.addChild(Ae2CanvasFactory.slot(slot, "powah_input_" + i, 3 + i * 20, 4));
        }
        var output = createOutputSlot(0, JEI_SLOT_SIZE, new ItemSlot() {
            @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                if (!nativeDrawing()) super.drawItemStack(context, stack);
            }
        });
        configureJeiOverlaySlotVisual(output);
        panel.addChild(Ae2CanvasFactory.slot(output, "powah_output", 136, 4));
        panel.addChild(Ae2CanvasFactory.at(fallbackEnergy, 2, 29, 158, 10));
        return Ae2CanvasFactory.centered(panel).setId("powah_canvas_container");
    }

    private boolean nativeDrawing() { return preview != null && preview.isAvailable(); }

    @Override
    public void load() {
        for (int i = 0; i < 6; i++) {
            setVisualIngredient(i, displayIngredient(i));
            // 空标签也需要保留原定义，不能把没有展示物品误判成玩家清空了原料。
            originalInputs.add(getVisualIngredient(i).copy());
        }
        var output = root.getAsJsonObject("result").deepCopy();
        int count = output.has("count") ? output.get("count").getAsInt() : 1;
        if (!output.isEmpty()) output.addProperty("count", 1);
        originalOutput = decode(ItemStack.OPTIONAL_CODEC, output).copyWithCount(count);
        setVisualOutput(0, originalOutput);
        loaded = true; updatePreview();
    }

    private RecipeIngredient displayIngredient(int index) {
        var json = inputs.get(index);
        if (json.isJsonNull()) return RecipeIngredient.empty();
        if (json instanceof JsonObject object && !object.has("type")) {
            if (object.has("tag")) return RecipeIngredient.tag(object.get("tag").getAsString());
            if (object.has("item")) return RecipeIngredient.item(BuiltInRegistries.ITEM.get(ResourceLocation.parse(object.get("item").getAsString())));
        }
        var stacks = decode(Ingredient.CODEC, json).getItems();
        return stacks.length == 0 ? RecipeIngredient.empty() : RecipeIngredient.item(stacks[0].copyWithCount(1));
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (loaded) { save(); if (preview != null && !preview.isAvailable()) updatePreview(); }
    }

    @Override
    public void save() {
        if (!loaded) return;
        for (int i = 0; i < 6; i++) {
            var value = getVisualIngredient(i).copy().setCount(1);
            if (!value.serializeNBT(Platform.getFrozenRegistry()).equals(originalInputs.get(i).serializeNBT(Platform.getFrozenRegistry()))) {
                inputs.set(i, value.isEmpty() ? JsonNull.INSTANCE : encode(Ingredient.CODEC, value.compile()));
                originalInputs.set(i, value.copy());
            }
        }
        var ingredients = new JsonArray(); inputs.stream().filter(json -> !json.isJsonNull()).forEach(ingredients::add);
        root.add("ingredients", ingredients);
        var output = getVisualOutput(0).getItem();
        if (!ItemStack.matches(output, originalOutput)) {
            var json = output.isEmpty() ? new JsonObject() : encode(ItemStack.CODEC, output.copyWithCount(1)).getAsJsonObject();
            if (!output.isEmpty()) json.addProperty("count", output.getCount());
            root.add("result", json); originalOutput = output.copy();
        }
        if (!root.toString().equals(getData().getRecipeJson())) persist();
    }

    private void persist() { getData().setRecipeJson(root.toString()); updatePreview(); }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient value) {
        super.setVisualIngredient(index, value.copy().setCount(1));
    }

    private void changeIngredient(int index, RecipeIngredient value) {
        setVisualIngredient(index, value);
        inputs.set(index, value.isEmpty() ? JsonNull.INSTANCE : encode(Ingredient.CODEC, value.compile()));
        originalInputs.set(index, getVisualIngredient(index).copy());
        save();
    }

    private void updatePreview() {
        // 空槽只参与编辑布局；发布时材料列表仍由原生 Codec 校验。
        if (preview != null) preview.setRecipe(new EnergizingRecipe(getVisualOutput(0).getItem(), energy(),
                inputs.stream().map(json -> json.isJsonNull() ? Ingredient.EMPTY : decode(Ingredient.CODEC, json)).toList()));
        fallbackEnergy.setText(Component.translatable("info.lollipop.fe", String.format(java.util.Locale.ROOT, "%,d", energy())));
        fallbackEnergy.setDisplay(!nativeDrawing());
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex(); var json = inputs.get(index);
        if (json.isJsonArray() || json instanceof JsonObject object && object.has("type")) {
            content.addChild(RecipeEditorUi.textButton(Component.translatable(key("replace_ingredient")), null, e -> {
                changeIngredient(index, RecipeIngredient.item(Items.IRON_INGOT)); reloadProperties();
            }).setId("powah_replace_ingredient"));
            return;
        }
        var input = displayIngredient(index);
        content.addChild(selector("viscript_recipe.config.ingredient.value.kind", List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG),
                input.getKind(), IngredientValueKind::displayName, kind -> {
                    changeIngredient(index, kind == IngredientValueKind.TAG ? RecipeIngredient.tag("c:ingots/iron") : RecipeIngredient.item(Items.IRON_INGOT));
                    reloadProperties();
                }));
        if (input.getKind() == IngredientValueKind.TAG) content.addChild(PropertiesView.createItemTagConfigurator(input.getTag(),
                tag -> changeIngredient(index, RecipeIngredient.tag(tag.location()))));
        else content.addChild(PropertiesView.removeCountConfig(itemConfigurator(true)));
    }

    private ConfiguratorGroup itemConfigurator(boolean input) {
        var group = (ConfiguratorGroup) new ViScriptItemStackAccessor().create(input ? "viscript_recipe.editor.ingredient.item_slot" : "viscript_recipe.config.recipe.result",
                () -> new ViScriptItemStack(input ? getSelectedIngredient().toStack() : getSelectedOutput().getItem()),
                value -> { if (input) changeIngredient(selectedSlotIndex(), RecipeIngredient.item(value.toItemStack().copyWithCount(1))); else setSelectedOutput(value.toItemStack()); }, true, null, null);
        group.setCollapse(false); group.layout(l -> l.widthPercent(100)); group.setId("powah_item_stack");
        for (var child : group.getConfigurators()) if (child instanceof NumberConfigurator number) {
            number.textField.setId("powah_stack_count");
        }
        return group;
    }

    @Override public void buildResultProperties(UIElement content) { content.addChild(itemConfigurator(false)); }
    private long energy() { return root.get("energy").getAsLong(); }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(field(key("energy"), RecipeEditorUi.longField(energy(), 1, Long.MAX_VALUE,
                value -> { root.addProperty("energy", value); persist(); }).setId("powah_energy")));
    }
}
