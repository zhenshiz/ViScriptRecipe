package com.viscript_recipe.compat.neoecoae.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
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
import com.viscript_recipe.compat.jei.neoecoae.NeoEcoJeiCanvas;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory;
import com.viscript_recipe.compat.neoecoae.data.NeoEcoRecipeData;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.FluidDisplaySlot;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.viscript_recipe.compat.neoecoae.NeoEcoRecipeEditorTypes.*;
import static com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory.decode;
import static com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory.encode;

/** 在原生 JEI 布局上复用 VSR 幻影槽和 VSL 物品堆配置器。 */
public final class NeoEcoRecipeCanvas extends FluidRecipeCanvas<NeoEcoRecipeData> {
    private JsonObject root;
    private final List<JsonObject> items = new ArrayList<>();
    private final List<RecipeIngredient> originalInputs = new ArrayList<>();
    private ItemStack originalOutput = ItemStack.EMPTY;
    private FluidIngredientData originalFluidInput = FluidIngredientData.empty();
    private FluidStack originalFluidOutput = FluidStack.EMPTY;
    private final Label status = new Label();
    private NeoEcoJeiCanvas preview;
    private UIElement panel;
    private boolean loaded;
    private boolean validRecipe;

    public NeoEcoRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private boolean cooling() { return entry.getType().getPath().equals("cooling"); }
    private boolean large() { return entry.getType().getPath().equals("large_integrated_working_station"); }
    private String fluidInputKey() { return cooling() ? "input" : "inputFluid"; }
    private String fluidOutputKey() { return cooling() ? "output" : "fluidOutput"; }
    private boolean nativeDrawing() { return preview != null && preview.isAvailable(); }

    @Override
    public UIElement createCanvas() {
        loaded = false; preview = null;
        root = JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        items.clear(); originalInputs.clear();
        var array = root.has("inputItems") ? root.getAsJsonArray("inputItems") : new JsonArray();
        for (int i = 0; i < (cooling() ? 0 : large() ? 12 : 9); i++)
            items.add(i < array.size() ? array.get(i).getAsJsonObject().deepCopy() : new JsonObject());
        int width = cooling() ? 100 : 168;
        int height = cooling() ? 60 : large() ? 96 : 78;
        panel = Ae2CanvasFactory.panel("neoecoae_jei_panel", width, height);
        if (!cooling()) panel.style(s -> s.backgroundTexture(SpriteTexture.of(id("textures/guis/"
                + (large() ? "large_integration_working_station" : "integration_working_station") + ".png"))
                .setSprite(0, 0, 168, large() ? 96 : 75)));
        if (cooling()) {
            var background = SpriteTexture.of(ResourceLocation.parse("jei:textures/jei/atlas/gui/slot.png")).setSprite(0, 0, 18, 18);
            panel.addChildren(Ae2CanvasFactory.at(new UIElement().style(s -> s.backgroundTexture(background)), 9, 10, 18, 18),
                    Ae2CanvasFactory.at(new UIElement().style(s -> s.backgroundTexture(background)), 71, 10, 18, 18));
        }
        if (ViScriptRecipe.isModLoaded("jei")) {
            preview = new NeoEcoJeiCanvas();
            panel.addChild(Ae2CanvasFactory.at(preview, 0, 0, width, height));
        }
        for (int i = 0; i < items.size(); i++) {
            var slot = createIngredientSlot(i, JEI_SLOT_SIZE, new IngredientDisplaySlot() {
                @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                    if (!nativeDrawing()) super.drawItemStack(context, stack);
                }
            });
            configureJeiOverlaySlotVisual(slot);
            panel.addChild(Ae2CanvasFactory.slot(slot, "neoecoae_input_" + i,
                    (large() ? 26 : 38) + i % (large() ? 4 : 3) * 18 - 1, 11 + i / (large() ? 4 : 3) * 18));
        }
        if (!cooling()) {
            var output = createOutputSlot(0, JEI_SLOT_SIZE, new ItemSlot() {
                @Override protected void drawItemStack(GUIContext context, ItemStack stack) {
                    if (!nativeDrawing()) super.drawItemStack(context, stack);
                }
            });
            configureJeiOverlaySlotVisual(output);
            panel.addChild(Ae2CanvasFactory.slot(output, "neoecoae_output", 113, 30));
        }
        fluidInputSlots[0] = new FluidDisplaySlot() {
            @Override protected void drawFluid(GUIContext context, FluidStack fluid, float x, float y, float width, float height) {
                if (!nativeDrawing()) super.drawFluid(context, fluid, x, y, width, height);
            }
        };
        fluidOutputSlots[0] = new FluidSlot() {
            @Override protected void drawFluid(GUIContext context, FluidStack fluid, float x, float y, float width, float height) {
                if (!nativeDrawing()) super.drawFluid(context, fluid, x, y, width, height);
            }
        };
        fluidInputSlots[0].registerValueListener(fluid -> setVisualFluidInput(0, FluidIngredientData.fluid(fluid)));
        placeFluid(configureFluidInputSlot(0), true);
        placeFluid(configureFluidOutputSlot(0), false);
        var body = RecipeEditorUi.column().setId("neoecoae_canvas").layout(l -> l.width(210).gapAll(8).alignItems(AlignItems.CENTER).flexShrink(0));
        body.addChildren(panel, status.setId("neoecoae_validation").setDisplay(false));
        return Ae2CanvasFactory.centered(body).setId("neoecoae_canvas_container");
    }

    private void placeFluid(FluidSlot slot, boolean input) {
        slot.xeiPhantom().setAllowClickFilled(false).setAllowClickDrained(false).setCapacity(cooling() ? 1 : 16000);
        configureJeiOverlayFluidSlotVisual(slot);
        slot.layout(l -> l.paddingAll(0));
        slot.setId("neoecoae_fluid_" + (input ? "input" : "output"));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.addChild(Ae2CanvasFactory.at(slot, cooling() ? input ? 10 : 72 : input ? 5 : 147,
                cooling() ? 11 : 9, 16, cooling() ? 16 : 58));
    }

    @Override
    public void load() {
        for (int i = 0; i < items.size(); i++) {
            var input = displayIngredient(i); originalInputs.add(input.copy()); loadIngredientSlot(i, input);
        }
        if (!cooling()) {
            var outputJson = root.has("itemOutput") ? root.getAsJsonObject("itemOutput").deepCopy() : new JsonObject();
            int amount = outputJson.has("count") ? outputJson.get("count").getAsInt() : 1;
            if (!outputJson.isEmpty()) outputJson.addProperty("count", 1);
            originalOutput = decode(ItemStack.OPTIONAL_CODEC, outputJson).copyWithCount(amount);
            setVisualOutput(0, originalOutput);
        }
        var fluid = fluidInputJson();
        if (fluid != null && fluid.has("tag") && !fluid.has("type"))
            originalFluidInput = FluidIngredientData.tag(ResourceLocation.parse(fluid.get("tag").getAsString())).setAmount(fluid.get("amount").getAsInt());
        else {
            var stacks = fluid == null ? new FluidStack[0] : decode(SizedFluidIngredient.FLAT_CODEC, fluid).getFluids();
            originalFluidInput = stacks.length == 0 ? FluidIngredientData.empty() : FluidIngredientData.fluid(stacks[0]);
        }
        originalFluidOutput = root.has(fluidOutputKey()) ? decode(FluidStack.OPTIONAL_CODEC, root.get(fluidOutputKey())) : FluidStack.EMPTY;
        setVisualFluidInput(0, originalFluidInput.copy()); setVisualFluidOutput(0, originalFluidOutput);
        loaded = true; validate();
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (loaded) { save(); if (validRecipe && preview != null && !preview.isAvailable()) validate(); }
    }

    @Override
    public void save() {
        if (!loaded) return;
        for (int i = 0; i < items.size(); i++) {
            var value = getVisualIngredient(i);
            if (!same(value, originalInputs.get(i))) {
                items.set(i, value.isEmpty() ? new JsonObject() : encode(SizedIngredient.FLAT_CODEC, new SizedIngredient(value.getKind() == IngredientValueKind.ITEM && !value.getItem().getComponentsPatch().isEmpty()
                        ? net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false, value.toStack()) : value.compile(), value.getCount())).getAsJsonObject());
                originalInputs.set(i, value.copy());
            }
        }
        if (!cooling()) {
            var array = new JsonArray(); items.stream().filter(value -> !value.isEmpty()).forEach(array::add); root.add("inputItems", array);
            var output = getVisualOutput(0).getItem();
            if (!ItemStack.matches(originalOutput, output)) {
                // 保留无效数量草稿，原生 Codec 在发布时校验。
                var json = output.isEmpty() ? new JsonObject() : encode(ItemStack.CODEC, output.copyWithCount(1)).getAsJsonObject();
                if (!output.isEmpty()) json.addProperty("count", output.getCount());
                root.add("itemOutput", json); originalOutput = output.copy();
            }
        }
        var input = getVisualFluidInput(0);
        if (!same(input, originalFluidInput)) {
            if (input.isEmpty()) root.remove(fluidInputKey());
            else {
                var ingredient = input.getKind() == FluidIngredientKind.TAG
                        ? FluidIngredient.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, input.getTag()))
                        : input.getFluid().getComponentsPatch().isEmpty() ? FluidIngredient.of(input.getFluid().getFluid())
                        : net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient.of(false, input.getFluid());
                root.add(fluidInputKey(), encode(SizedFluidIngredient.FLAT_CODEC, new SizedFluidIngredient(ingredient, input.getAmount())));
            }
            originalFluidInput = input.copy();
        }
        var output = getVisualFluidOutput(0);
        if (!FluidStack.matches(output, originalFluidOutput)) {
            root.add(fluidOutputKey(), encode(FluidStack.OPTIONAL_CODEC, output)); originalFluidOutput = output.copy();
        }
        if (!root.toString().equals(getData().getRecipeJson())) persist();
    }

    private static boolean same(com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable a, com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable b) {
        return a.serializeNBT(Platform.getFrozenRegistry()).equals(b.serializeNBT(Platform.getFrozenRegistry()));
    }
    private JsonObject fluidInputJson() { return root.has(fluidInputKey()) ? root.getAsJsonObject(fluidInputKey()) : null; }
    private RecipeIngredient displayIngredient(int index) {
        var json = items.get(index);
        if (json.isEmpty()) return RecipeIngredient.empty();
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        if (!json.has("type") && json.has("tag")) return RecipeIngredient.tag(json.get("tag").getAsString()).setCount(count);
        if (!json.has("type") && json.has("item")) return RecipeIngredient.item(BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()))).setCount(count);
        var stacks = decode(SizedIngredient.FLAT_CODEC, json).getItems();
        return stacks.length == 0 ? RecipeIngredient.empty() : RecipeIngredient.item(stacks[0]);
    }
    private void persist() { getData().setRecipeJson(root.toString()); validate(); }
    private void validate() {
        try {
            var recipe = getData().compile(entry.getType()); validRecipe = true;
            if (preview != null) {
                var inputs = items.stream().map(json -> json.isEmpty() ? new SizedIngredient(Ingredient.EMPTY, 1) : decode(SizedIngredient.FLAT_CODEC, json)).toList();
                preview.setRecipe(new RecipeHolder<>(ViScriptRecipe.id("neoecoae_preview"), recipe), inputs);
                if (large() && preview.isAvailable()) {
                    panel.layout(l -> l.height(preview.recipeHeight())); preview.layout(l -> l.height(preview.recipeHeight()));
                }
            }
            status.setDisplay(false);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            validRecipe = false;
            if (preview != null) preview.clear();
            status.setText(Component.translatable(key("invalid"))); status.setDisplay(true);
            status.style(s -> s.tooltips(Component.literal(exception.getMessage())));
        }
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex(); var json = items.get(index);
        if (json.has("type")) {
            content.addChild(RecipeEditorUi.textButton(Component.translatable(key("replace_ingredient")), null, e -> {
                items.set(index, new JsonObject()); originalInputs.set(index, RecipeIngredient.empty());
                setVisualIngredient(index, RecipeIngredient.item(Items.IRON_INGOT)); save(); reloadProperties();
            }));
            content.addChild(intField(key("count"), json.has("count") ? json.get("count").getAsInt() : 1, 1, Integer.MAX_VALUE, value -> {
                json.addProperty("count", value); var input = displayIngredient(index);
                originalInputs.set(index, input.copy()); setVisualIngredient(index, input); save();
            })); return;
        }
        var input = getVisualIngredient(index);
        content.addChild(selector("viscript_recipe.config.ingredient.value.kind", List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG), input.getKind(),
                IngredientValueKind::displayName, kind -> {
                    setVisualIngredient(index, (kind == IngredientValueKind.TAG ? RecipeIngredient.tag("c:ingots/iron") : RecipeIngredient.item(Items.IRON_INGOT)).setCount(Math.max(1, input.getCount())));
                    save(); reloadProperties();
                }));
        if (input.getKind() == IngredientValueKind.TAG) {
            content.addChild(PropertiesView.createItemTagConfigurator(input.getTag(), tag -> setVisualIngredient(index, RecipeIngredient.tag(tag.location()).setCount(getVisualIngredient(index).getCount()))));
            content.addChild(intField(key("count"), input.getCount(), 1, Integer.MAX_VALUE, value -> setVisualIngredient(index, getVisualIngredient(index).setCount(value))));
        } else content.addChild(itemConfigurator(() -> new ViScriptItemStack(getVisualIngredient(index).toStack()),
                value -> setVisualIngredient(index, RecipeIngredient.item(value.toItemStack())), true));
    }

    private ConfiguratorGroup itemConfigurator(Supplier<ViScriptItemStack> supplier, Consumer<ViScriptItemStack> consumer, boolean input) {
        var group = (ConfiguratorGroup) new ViScriptItemStackAccessor().create(input ? key("item") : "viscript_recipe.config.recipe.result", supplier, consumer, true, null, null);
        group.setCollapse(false); group.layout(l -> l.widthPercent(100)); group.setId("neoecoae_item_stack");
        for (var child : group.getConfigurators()) if (child instanceof NumberConfigurator number) {
            number.setRange(1, input ? Integer.MAX_VALUE : 99); number.textField.setId("neoecoae_stack_count");
        }
        return group;
    }
    @Override public void buildResultProperties(UIElement content) {
        content.addChild(itemConfigurator(() -> new ViScriptItemStack(getSelectedOutput().getItem()), value -> setSelectedOutput(value.toItemStack()), false));
    }
    @Override public void buildFluidProperties(UIElement content) {
        var json = fluidInputJson();
        if (selectedSlotIndex() == 0 && json != null && json.has("type")) {
            content.addChild(RecipeEditorUi.textButton(Component.translatable(key("replace_ingredient")), null, e -> {
                int amount = Math.max(1, originalFluidInput.getAmount()); originalFluidInput = FluidIngredientData.empty();
                setVisualFluidInput(0, FluidIngredientData.fluid(new FluidStack(Fluids.WATER, amount))); save(); reloadProperties();
            }));
            content.addChild(intField("viscript_recipe.config.create.fluid_ingredient.amount", json.get("amount").getAsInt(), 1, Integer.MAX_VALUE,
                    value -> {
                        json.addProperty("amount", value);
                        var stacks = decode(SizedFluidIngredient.FLAT_CODEC, json).getFluids();
                        originalFluidInput = stacks.length == 0 ? FluidIngredientData.empty() : FluidIngredientData.fluid(stacks[0]);
                        setVisualFluidInput(0, originalFluidInput.copy()); persist();
                    }));
        } else super.buildFluidProperties(content);
    }
    @Override public void buildRecipeProperties(UIElement content) {
        if (cooling()) {
            parameter(content, "coolant", 100, 0);
            parameter(content, "max_overclock", 0, 0);
        } else {
            parameter(content, "energy", 1000, 0);
            parameter(content, "processingTicks", 100, 1);
        }
    }
    private void parameter(UIElement content, String name, int fallback, int minimum) {
        var input = RecipeEditorUi.intField(root.has(name) ? root.get(name).getAsInt() : fallback, minimum, Integer.MAX_VALUE,
                value -> { root.addProperty(name, value); persist(); });
        content.addChild(field(key(name), input.setId("neoecoae_" + name)));
    }
}
