package com.viscript_recipe.compat.ae2cs.canvas;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_lib.gui.configurator.ViScriptItemStackAccessor;
import com.viscript_lib.util.item.ViScriptItemStack;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory;
import com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeFactory;
import com.viscript_recipe.compat.ae2cs.data.Ae2CrystalScienceRecipeData;
import com.viscript_recipe.compat.jei.ae2cs.Ae2CrystalScienceJeiCanvas;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeEditorTypes.*;
import static com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeFactory.decode;
import static com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeFactory.encode;

/** 在原生 JEI 机器面板上复用材料槽、拖放、标签选择和 VSL 物品堆配置器。 */
public final class Ae2CrystalScienceRecipeCanvas extends RecipeCanvas<Ae2CrystalScienceRecipeData> {
    private JsonObject root;
    private List<String> inputs;
    private final List<RecipeIngredient> originalInputs = new ArrayList<>();
    private ItemStack originalOutput = ItemStack.EMPTY;
    private final Label status = new Label();
    private final Label energy = new Label();
    private Ae2CrystalScienceJeiCanvas preview;
    private boolean loaded;

    public Ae2CrystalScienceRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private boolean pulverizer() { return entry.getType().getPath().equals("crystal_pulverizer"); }
    private static Component text(String name, Object... args) { return Component.translatable(key(name), args); }

    @Override
    public UIElement createCanvas() {
        loaded = false;
        root = JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        // 原生 JEI 将非空材料向前排列；沿用相同顺序，同时保留原字段以无损保存。
        inputs = new ArrayList<>(pulverizer() ? List.of("input") : List.of("input_a", "input_b", "input_c"));
        inputs.sort(java.util.Comparator.comparing(name -> !root.has(name)));
        originalInputs.clear();
        if (ViScriptRecipe.isModLoaded("jei")) {
            preview = new Ae2CrystalScienceJeiCanvas();
            preview.setRecipe(new RecipeHolder<>(ViScriptRecipe.id("ae2cs_preview"),
                    Ae2CrystalScienceRecipeFactory.compile(entry.getType(), Ae2CrystalScienceRecipeFactory.defaults(entry.getType()))));
        }
        int width = preview == null ? 135 : preview.recipeWidth();
        int height = preview == null ? 58 : preview.recipeHeight();
        var panel = Ae2CanvasFactory.panel("ae2cs_jei_panel", width, height);
        if (preview != null) panel.addChild(Ae2CanvasFactory.at(preview, 0, 0, width, height));
        if (preview == null || !preview.isAvailable()) panel.style(s -> s.backgroundTexture(SpriteTexture.of(id("textures/gui/recipe/" + entry.getType().getPath() + ".png"))
                .setSprite(0, 0, 135, 58)));
        for (int i = 0; i < inputs.size(); i++) place(panel, createIngredientSlot(i, JEI_SLOT_SIZE), true, i);
        place(panel, createOutputSlot(0, JEI_SLOT_SIZE), false, 0);
        var body = RecipeEditorUi.column().setId("ae2cs_canvas").layout(l -> l.width(210).gapAll(8).alignItems(AlignItems.CENTER).flexShrink(0));
        energy.textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER));
        status.textStyle(s -> s.textAlignHorizontal(Horizontal.CENTER).textWrap(TextWrap.WRAP));
        body.addChildren(panel, energy.setId("ae2cs_energy").layout(l -> l.widthPercent(100)),
                status.setId("ae2cs_validation").setDisplay(false).layout(l -> l.widthPercent(100).height(18)));
        return Ae2CanvasFactory.centered(body).setId("ae2cs_canvas_container");
    }

    private void place(UIElement panel, ItemSlot slot, boolean input, int index) {
        var positions = preview == null ? List.<Rect2i>of() : preview.slots(input);
        var bounds = index < positions.size() ? positions.get(index)
                : new Rect2i(input ? pulverizer() ? 23 : 30 : 86, input && !pulverizer() ? 3 + index * 18 : 22, 16, 16);
        configureJeiOverlaySlotVisual(slot);
        panel.addChild(Ae2CanvasFactory.slot(slot, "ae2cs_" + (input ? "input_" : "output_") + index,
                bounds.getX() - 1, bounds.getY() - 1));
    }

    @Override
    public void load() {
        for (int i = 0; i < inputs.size(); i++) {
            var value = displayIngredient(i);
            originalInputs.add(value.copy()); loadIngredientSlot(i, value);
        }
        var output = root.getAsJsonObject("result").deepCopy();
        int amount = output.has("count") ? output.get("count").getAsInt() : 1;
        if (!output.isEmpty()) output.addProperty("count", 1);
        originalOutput = output.isEmpty() ? ItemStack.EMPTY : decode(ItemStack.CODEC, output).copyWithCount(amount);
        setVisualOutput(0, originalOutput);
        loaded = true; validate();
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (!loaded) return;
        save();
        if (preview != null && !preview.isAvailable()) validate();
    }

    @Override
    public void save() {
        if (!loaded) return;
        for (int i = 0; i < inputs.size(); i++) {
            var value = getVisualIngredient(i);
            if (!value.serializeNBT(Platform.getFrozenRegistry()).equals(originalInputs.get(i).serializeNBT(Platform.getFrozenRegistry()))) {
                writeIngredient(i, value); originalInputs.set(i, value.copy());
            }
        }
        var output = getVisualOutput(0).getItem();
        if (!ItemStack.matches(output, originalOutput)) {
            // 空产物保留为草稿；数量上限由原生 ItemStack Codec 校验。
            var json = output.isEmpty() ? new JsonObject() : encode(ItemStack.CODEC, output.copyWithCount(1)).getAsJsonObject();
            if (!output.isEmpty()) json.addProperty("count", output.getCount());
            root.add("result", json); originalOutput = output.copy();
        }
        if (!root.toString().equals(getData().getRecipeJson())) persist();
    }

    private RecipeIngredient displayIngredient(int index) {
        var name = inputs.get(index);
        if (!root.has(name)) return RecipeIngredient.empty();
        var json = root.getAsJsonObject(name);
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        if (!json.has("type") && json.has("tag")) return RecipeIngredient.tag(json.get("tag").getAsString()).setCount(count);
        if (!json.has("type") && json.has("item")) return RecipeIngredient.item(BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()))).setCount(count);
        var stacks = decode(SizedIngredient.FLAT_CODEC, json).getItems();
        return stacks.length == 0 ? RecipeIngredient.empty() : RecipeIngredient.item(stacks[0]);
    }

    private void writeIngredient(int index, RecipeIngredient value) {
        if (value.isEmpty()) root.remove(inputs.get(index));
        else root.add(inputs.get(index), encode(SizedIngredient.FLAT_CODEC, new SizedIngredient(value.compile(), value.getCount())));
    }

    private void changeIngredient(int index, RecipeIngredient value) {
        writeIngredient(index, value); originalInputs.set(index, value.copy()); setVisualIngredient(index, value); persist();
    }

    private void persist() { getData().setRecipeJson(root.toString()); validate(); }
    private void validate() {
        energy.setText(text("energy", energyCost()));
        try {
            var compiled = getData().compile(entry.getType());
            if (preview != null) preview.setRecipe(new RecipeHolder<>(ViScriptRecipe.id("ae2cs_preview"), compiled));
            status.setText(Component.empty()); status.setDisplay(false);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            status.setText(text("invalid")); status.setDisplay(true); status.style(s -> s.tooltips(Component.literal(exception.getMessage())));
        }
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var json = root.getAsJsonObject(inputs.get(index));
        if (json != null && json.has("type")) {
            content.addChildren(RecipeEditorUi.label(text("preserved_ingredient")),
                    RecipeEditorUi.textButton(text("replace_ingredient"), null, e -> {
                        changeIngredient(index, RecipeIngredient.item(Items.IRON_INGOT).setCount(count(index))); reloadProperties();
                    }));
            countField(content, index); return;
        }
        var ingredient = getVisualIngredient(index);
        content.addChild(selector("viscript_recipe.config.ingredient.value.kind", List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG), ingredient.getKind(),
                IngredientValueKind::displayName, kind -> {
                    changeIngredient(index, (kind == IngredientValueKind.TAG ? RecipeIngredient.tag("c:ingots/iron")
                            : RecipeIngredient.item(Items.IRON_INGOT)).setCount(Math.max(1, count(index)))); reloadProperties();
                }));
        if (ingredient.getKind() == IngredientValueKind.TAG) {
            content.addChild(PropertiesView.createItemTagConfigurator(ingredient.getTag(), tag ->
                    changeIngredient(index, RecipeIngredient.tag(tag.location()).setCount(count(index)))));
            countField(content, index);
        } else content.addChild(itemConfigurator(() -> new ViScriptItemStack(getVisualIngredient(index).toStack()),
                value -> changeIngredient(index, RecipeIngredient.item(value.toItemStack())), true));
    }

    private int count(int index) {
        var json = root.getAsJsonObject(inputs.get(index));
        return json != null && json.has("count") ? json.get("count").getAsInt() : 1;
    }

    private void countField(UIElement content, int index) {
        content.addChild(intField(key("count"), count(index), 1, Integer.MAX_VALUE, value -> {
            root.getAsJsonObject(inputs.get(index)).addProperty("count", value);
            var ingredient = displayIngredient(index); originalInputs.set(index, ingredient.copy()); setVisualIngredient(index, ingredient); persist();
        }));
    }

    private ConfiguratorGroup itemConfigurator(Supplier<ViScriptItemStack> supplier, Consumer<ViScriptItemStack> consumer, boolean input) {
        var group = (ConfiguratorGroup) new ViScriptItemStackAccessor().create(input ? key("item") : "viscript_recipe.config.recipe.result", supplier, consumer, true, null, null);
        group.setCollapse(false); group.layout(l -> l.widthPercent(100));
        for (var child : group.getConfigurators()) if (child instanceof NumberConfigurator number) {
            number.setRange(1, input ? Integer.MAX_VALUE : 99);
            number.textField.setId("ae2cs_stack_count");
        }
        group.setId("ae2cs_item_stack"); return group;
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChild(itemConfigurator(() -> new ViScriptItemStack(getSelectedOutput().getItem()),
                value -> setSelectedOutput(value.toItemStack()), false));
    }

    private int energyCost() {
        return root.has("energy_cost") ? root.get("energy_cost").getAsInt()
                : entry.getType().getPath().equals("circuit_etcher") ? 3200 : 200;
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(intField(key("energy_cost"), energyCost(),
                1, Integer.MAX_VALUE, value -> { root.addProperty("energy_cost", value); persist(); }).setId("ae2cs_energy_cost"));
    }
}
