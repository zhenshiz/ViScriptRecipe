package com.viscript_recipe.compat.enderio.canvas;

import com.enderio.enderio.api.soul.Soul;
import com.enderio.enderio.content.enchanter.EnchanterRecipe;
import com.enderio.enderio.content.tools.vials.SoulVialItem;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.enderio.EnderIoRecipeEditorTypes;
import com.viscript_recipe.compat.enderio.data.EnderIoIngredientData;
import com.viscript_recipe.compat.enderio.data.EnderIoOutputData;
import com.viscript_recipe.compat.enderio.data.EnderIoRecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.viscript_recipe.compat.enderio.canvas.EnderIoCanvasLayout.*;

public final class EnderIoRecipeCanvas extends FluidRecipeCanvas<EnderIoRecipeData> {
    private final Map<Integer, Integer> alternatives = new HashMap<>();
    private int previewLevel = 1;

    public EnderIoRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private String type() { return entry.getType().getPath(); }
    private static String key(String path) { return "viscript_recipe.editor.enderio." + path; }
    private static Component text(String path, Object... args) { return Component.translatable(key(path), args); }

    @Override
    public UIElement createCanvas() {
        var data = getData();
        UIElement body;
        switch (type()) {
            case "alloy_smelting" -> {
                body = background("viewer/alloy_smelter", 0, 0, 107, 73);
                int[][] positions = {{0, 10}, {25, 0}, {49, 10}};
                for (int i = 0; i < data.getInputs().size(); i++) body.addChild(input(i, positions[i][0], positions[i][1]));
                body.addChild(output(0, 25, 51));
            }
            case "sag_milling" -> {
                body = background("viewer/sag_mill", 0, 0, 123, 65);
                body.addChild(input(0, 31, 0));
                for (int i = 0; i < data.getOutputs().size(); i++) body.addChild(output(i, i * 21, 47));
            }
            case "slicing" -> {
                body = background("viewer/slice_and_splice", 0, 0, 108, 60);
                for (int i = 0; i < 6; i++) body.addChild(input(i, i % 3 * 18, 24 + i / 3 * 18));
                body.addChildren(output(0, 90, 33), fixed(new ItemStack(Items.IRON_AXE), 10, 0), fixed(new ItemStack(Items.SHEARS), 28, 0));
            }
            case "soul_binding" -> {
                body = background("screen/soul_binder", 35, 30, 118, 44);
                ItemStack vial = BuiltInRegistries.ITEM.get(EnderIoRecipeEditorTypes.id("soul_vial")).getDefaultInstance();
                var entity = ResourceLocation.tryParse(data.getEntityType());
                var filled = entity != null && BuiltInRegistries.ENTITY_TYPE.containsKey(entity) ? SoulVialItem.forSoul(Soul.of(entity)) : vial;
                body.addChildren(input(0, 23, 3), output(0, 98, 3), fixed(filled, 2, 3), fixed(vial, 76, 3));
            }
            case "tank" -> {
                body = background("screen/tank", 41, 18, 94, 53);
                int x = data.getTankMode().equals("empty") ? 2 : 74;
                body.addChildren(input(0, x, 2), output(0, x, 33), fluidInput(38, 2, 18, 49));
            }
            case "vat_fermenting" -> {
                body = background("screen/vat", 28, 10, 120, 53);
                body.addChildren(input(0, 27, 1), input(1, 76, 1), fluidInput(2, 2, 15, 47), fluidOutput(104, 2, 15, 47));
            }
            case "weather_change" -> {
                body = background("screen/weather_obelisk", 18, 4, 120, 76);
                body.addChildren(fluidOutput(4, 7, 15, 61), fixed(new ItemStack(Items.FIREWORK_ROCKET), 61, 6));
            }
            case "enchanting" -> {
                body = background("screen/enchanter", 15, 24, 146, 40);
                ItemStack lapis = new ItemStack(Items.LAPIS_LAZULI);
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                try {
                    var recipe = (EnchanterRecipe) data.compile(entry.getType());
                    book = recipe.getBookForLevel(previewLevel);
                    lapis.setCount(recipe.getLapisForLevel(previewLevel));
                }
                catch (RuntimeException ignored) { /* 注册表尚未就绪或正在输入无效附魔时保留占位预览。 */ }
                body.addChildren(fixed(new ItemStack(Items.WRITABLE_BOOK), 0, 10), input(0, 49, 10), fixed(lapis, 69, 10), fixed(book, 128, 10));
            }
            case "shaped_entity_storage" -> {
                body = panel(132, 66);
                for (int y = 0; y < data.getHeight(); y++) for (int x = 0; x < data.getWidth(); x++)
                    body.addChild(input(y * 3 + x, x * 20, y * 20));
                body.addChildren(label(Component.literal("→"), 72, 23, 20), output(0, 104, 20));
            }
            default -> {
                body = panel(172, 95);
                int base = 0;
                for (var id : data.getBases().stream().limit(6).toList()) body.addChild(fixed(BuiltInRegistries.BLOCK.get(id).asItem().getDefaultInstance(), base++ * 22, 0));
                body.addChild(label(text("fire_base_hint"), 0, 23, 172));
                for (int i = 0; i < data.getOutputs().size(); i++) body.addChild(output(i, i % 8 * 21, 48 + i / 8 * 22));
            }
        }
        var page = panel(228, 160).setId("enderio_canvas_" + type());
        page.addChildren(label(Component.translatable(EnderIoRecipeEditorTypes.key(type())), 8, 4, 212),
                at(centered(body), 0, 26, 228, 104),
                label(summary(), 8, 135, 212).bindDataSource(com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource.of(this::summary)).setId("enderio_summary"));
        return centered(page);
    }

    private Component summary() {
        return switch (type()) {
            case "alloy_smelting", "sag_milling", "slicing" -> text("energy_summary", getData().getEnergy());
            case "soul_binding" -> text("soul_summary", getData().getEnergy(), getData().getExperienceLevels());
            case "vat_fermenting" -> text("ticks_summary", getData().getTicks());
            case "tank" -> text(getData().getTankMode());
            case "weather_change" -> text(getData().getWeather());
            case "enchanting" -> Component.literal(getData().getEnchantment().toString());
            case "fire_crafting" -> text("dimension_summary", getData().getDimensions().size());
            default -> text("soul_crafting_hint");
        };
    }

    private UIElement input(int index, int x, int y) {
        var data = getData().getInputs().get(index);
        if (!data.getCustomJson().isBlank()) {
            var items = data.compile().getItems();
            return fixed(items.length == 0 ? ItemStack.EMPTY : items[0], x, y).setId("enderio_input_" + index)
                    .style(s -> s.tooltips(text("preserved_ingredient")))
                    .addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(SlotSelection.ingredient(index)); e.stopPropagation(); });
        }
        var slot = createIngredientSlot(index, 18);
        var placed = slot(slot, "enderio_input_" + index, x, y);
        if (type().equals("shaped_entity_storage")) slot.style(s -> s.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE));
        return placed;
    }

    private UIElement output(int index, int x, int y) {
        var output = getData().getOutputs().get(index);
        if (output.isUseTag()) {
            var stack = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, output.getTag()))
                    .flatMap(set -> set.stream().findFirst()).map(holder -> new ItemStack(holder, output.getCount())).orElse(ItemStack.EMPTY);
            return fixed(stack, x, y).setId("enderio_output_" + index).style(s -> s.tooltips(Component.literal("#" + output.getTag())))
                    .addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(SlotSelection.result(index)); e.stopPropagation(); });
        }
        var slot = createOutputSlot(index, 18);
        var placed = slot(slot, "enderio_output_" + index, x, y);
        if (type().equals("fire_crafting") || type().equals("shaped_entity_storage")) slot.style(s -> s.backgroundTexture(ItemSlot.ITEM_SLOT_TEXTURE));
        return placed;
    }

    private UIElement fixed(ItemStack stack, int x, int y) {
        var slot = new ItemSlot(); slot.setItem(stack, false);
        return slot(slot, "enderio_reference", x, y);
    }

    private UIElement fluidInput(int x, int y, int width, int height) {
        FluidSlot slot;
        if (getData().getFluidInput().getCustomJson().isBlank()) {
            slot = createFluidInputSlot(0);
            slot.registerValueListener(stack -> setVisualFluidInput(0, FluidIngredientData.fluid(stack)));
        }
        else {
            slot = new FluidSlot();
            var fluids = getData().getFluidInput().compile().getFluids();
            if (fluids.length > 0) slot.setFluid(fluids[0], false);
            slot.addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(SlotSelection.fluid(0)); e.stopPropagation(); });
        }
        configureJeiOverlayFluidSlotVisual(slot);
        return at(slot.setId("enderio_fluid_input").addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation()), x, y, width, height);
    }

    private UIElement fluidOutput(int x, int y, int width, int height) {
        var slot = createFluidOutputSlot(0, 18); configureJeiOverlayFluidSlotVisual(slot);
        return at(slot.setId("enderio_fluid_output").addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation()), x, y, width, height);
    }

    @Override
    public void load() {
        for (int i = 0; i < getData().getInputs().size(); i++) loadInput(i);
        for (int i = 0; i < getData().getOutputs().size(); i++) if (!getData().getOutputs().get(i).isUseTag())
            setVisualOutput(i, getData().getOutputs().get(i).getItem());
        setVisualFluidInput(0, getData().getFluidInput().getValue());
        setVisualFluidOutput(0, getData().getFluidOutput());
    }

    private void loadInput(int index) {
        var data = getData().getInputs().get(index);
        if (!data.getCustomJson().isBlank() || visualIngredientSlots[index] == null) return;
        int selected = Math.clamp(data.getAlternatives().size() - 1, 0, alternatives.getOrDefault(index, 0));
        alternatives.put(index, selected);
        loadIngredientSlot(index, data.getAlternatives().isEmpty() ? RecipeIngredient.empty() : data.getAlternatives().get(selected).copy().setCount(data.getCount() * (type().equals("enchanting") ? previewLevel : 1)));
    }

    @Override
    public void save() {
        for (int i = 0; i < getData().getInputs().size(); i++) saveInput(i);
        for (int i = 0; i < getData().getOutputs().size(); i++) if (visualOutputSlots[i] != null && !getData().getOutputs().get(i).isUseTag())
            getData().getOutputs().get(i).setItem(getVisualOutput(i).getItem());
        if (fluidInputSlots[0] != null && getData().getFluidInput().getCustomJson().isBlank()) getData().getFluidInput().setValue(getVisualFluidInput(0));
        if (fluidOutputSlots[0] != null) getData().setFluidOutput(getVisualFluidOutput(0));
    }

    private void saveInput(int index) {
        var data = getData().getInputs().get(index);
        if (!data.getCustomJson().isBlank() || visualIngredientSlots[index] == null) return;
        int selected = alternatives.getOrDefault(index, 0);
        while (data.getAlternatives().size() <= selected) data.getAlternatives().add(RecipeIngredient.empty());
        data.getAlternatives().set(selected, getVisualIngredient(index).setCount(1));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var data = getData().getInputs().get(index);
        if (!data.getCustomJson().isBlank()) {
            content.addChildren(sectionTitle(key("preserved_ingredient")), RecipeEditorUi.textButton(text("replace_ingredient"), null,
                    e -> { data.setCustomJson(""); rebuild(); }));
            addCountField(content, index, data);
            return;
        }
        saveInput(index);
        if (type().equals("vat_fermenting")) {
            var value = data.getAlternatives().getFirst();
            content.addChild(PropertiesView.createItemTagConfigurator(value.getTag(), tag -> setSelectedIngredient(RecipeIngredient.tag(tag.location()))));
            return;
        }
        content.addChild(intField(key("alternative"), alternatives.getOrDefault(index, 0) + 1, 1, data.getAlternatives().size() + 1, value -> {
            saveInput(index);
            while (data.getAlternatives().size() < value) data.getAlternatives().add(RecipeIngredient.empty());
            alternatives.put(index, value - 1); loadInput(index); reloadProperties();
        }, text("alternative_hint")));
        super.buildIngredientProperties(content);
        addCountField(content, index, data);
    }

    private void addCountField(UIElement content, int index, EnderIoIngredientData data) {
        if (type().equals("alloy_smelting") || type().equals("enchanting"))
            content.addChild(intField(key(type().equals("enchanting") ? "count_per_level" : "count"), data.getCount(), 1, 64,
                    value -> { data.setCount(value); loadInput(index); }));
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        if (selectedSlotIndex() == 0 && !getData().getFluidInput().getCustomJson().isBlank()) {
            content.addChildren(sectionTitle(key("preserved_fluid")), RecipeEditorUi.textButton(text("replace_ingredient"), null,
                    e -> { getData().getFluidInput().setCustomJson(""); rebuild(); }).setId("enderio_replace_fluid"));
        } else super.buildFluidProperties(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        int index = selectedSlotIndex();
        var output = getData().getOutputs().get(index);
        if (type().equals("sag_milling")) {
            content.addChild(switchField(key("tag_output"), output.isUseTag(), value -> { save(); output.setUseTag(value); rebuild(); }));
            if (output.isUseTag()) content.addChildren(PropertiesView.createItemTagConfigurator(output.getTag(), tag -> { output.setTag(tag.location()); rebuild(); }),
                    intField(key("count"), output.getCount(), 1, 64, value -> { output.setCount(value); rebuild(); }));
            content.addChild(switchField(key("optional"), output.isOptional(), output::setOptional));
        }
        if (!output.isUseTag()) super.buildResultProperties(content);
        if (type().equals("fire_crafting") || type().equals("sag_milling")) content.addChild(floatField(key("chance"), output.getChance(), 0, 1, output::setChance));
        if (type().equals("fire_crafting")) content.addChildren(intField(key("min_count"), output.getMinCount(), 0, Integer.MAX_VALUE, output::setMinCount),
                intField(key("max_count"), output.getMaxCount(), 0, Integer.MAX_VALUE, output::setMaxCount));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var d = getData();
        content.addChild(sectionTitle(EnderIoRecipeEditorTypes.key(type())));
        switch (type()) {
            case "alloy_smelting" -> content.addChildren(energy(), floatField(key("experience"), d.getExperience(), 0, Float.MAX_VALUE, d::setExperience),
                    intField(key("inputs"), d.getInputs().size(), 1, d.isSmelting() ? 1 : 3, value -> {
                        save(); while (d.getInputs().size() < value) d.getInputs().add(new EnderIoIngredientData());
                        while (d.getInputs().size() > value) d.getInputs().removeLast(); rebuild();
                    }), switchField(key("smelting"), d.isSmelting(), value -> { d.setSmelting(value); reloadProperties(); }));
            case "sag_milling" -> content.addChildren(energy(), outputCount(4), choice("bonus", List.of("none", "multiply_output", "chance_only"), d.getBonus(), d::setBonus));
            case "slicing" -> content.addChild(energy());
            case "soul_binding" -> {
                String mode = !d.getEntityType().isBlank() ? "entity" : !d.getMobCategory().isBlank() ? "category" : !d.getSoulData().isBlank() ? "soul_data" : "any";
                content.addChildren(energy(), intField(key("experience_levels"), d.getExperienceLevels(), 0, Integer.MAX_VALUE, d::setExperienceLevels),
                        choice("soul_mode", List.of("any", "entity", "category", "soul_data"), mode, value -> {
                            d.setEntityType(value.equals("entity") ? "minecraft:zombie" : "").setMobCategory(value.equals("category") ? "monster" : "")
                                    .setSoulData(value.equals("soul_data") ? "spawner" : ""); rebuild();
                        }), switchField(key("copy_components"), d.isCopyInputComponents(), d::setCopyInputComponents));
                switch (mode) {
                    case "entity" -> content.addChild(resourceField(key("entity"), ResourceLocation.tryParse(d.getEntityType()), id -> { d.setEntityType(id.toString()); rebuild(); }));
                    case "category" -> content.addChild(choice("category", Arrays.stream(MobCategory.values()).map(MobCategory::getSerializedName).toList(), d.getMobCategory(), d::setMobCategory));
                    case "soul_data" -> content.addChild(textField(key("soul_data"), d.getSoulData(), d::setSoulData));
                }
            }
            case "tank" -> content.addChild(choice("tank_mode", List.of("fill", "empty"), d.getTankMode(), value -> { save(); d.setTankMode(value); rebuild(); }));
            case "vat_fermenting" -> content.addChild(intField(key("ticks"), d.getTicks(), 1, Integer.MAX_VALUE, d::setTicks));
            case "weather_change" -> content.addChild(choice("weather", List.of("clear", "rain", "lightning"), d.getWeather(), value -> { d.setWeather(value); rebuild(); }));
            case "enchanting" -> content.addChildren(resourceField(key("enchantment"), d.getEnchantment(), value -> { save(); d.setEnchantment(value); previewLevel = 1; rebuild(); }),
                    intField(key("cost_multiplier"), d.getCostMultiplier(), 1, Integer.MAX_VALUE, d::setCostMultiplier),
                    intField(key("preview_level"), previewLevel, 1, maxLevel(), value -> { save(); previewLevel = value; rebuild(); }));
            case "fire_crafting" -> {
                content.addChild(outputCount(16));
                resourceList(content, "bases", d.getBases(), ResourceLocation.parse("minecraft:bedrock"));
                resourceList(content, "base_tags", d.getBaseTags(), ResourceLocation.parse("minecraft:base_stone_overworld"));
                resourceList(content, "dimensions", d.getDimensions(), ResourceLocation.parse("minecraft:overworld"));
                content.addChild(textField(key("block_after_burning"), d.getBlockAfterBurning(), d::setBlockAfterBurning));
            }
            case "shaped_entity_storage" -> content.addChildren(intField(key("width"), d.getWidth(), 1, 3, value -> { save(); d.setWidth(value); rebuild(); }),
                    intField(key("height"), d.getHeight(), 1, 3, value -> { save(); d.setHeight(value); rebuild(); }),
                    textField(key("group"), d.getGroup(), d::setGroup), selector(key("crafting_category"), List.of(CraftingBookCategory.values()), d.getCategory(),
                            value -> text("crafting_" + value.getSerializedName()), d::setCategory));
        }
    }

    private int maxLevel() {
        try { return ((EnchanterRecipe) getData().compile(entry.getType())).enchantment().value().getMaxLevel(); }
        catch (RuntimeException exception) { return 1; }
    }

    private UIElement energy() { return field(key("energy"), RecipeEditorUi.intField(getData().getEnergy(), 0, Integer.MAX_VALUE, getData()::setEnergy).setId("enderio_energy")); }
    private UIElement choice(String field, List<String> values, String value, java.util.function.Consumer<String> setter) {
        return selector(key(field), values, value, EnderIoRecipeCanvas::text, setter);
    }
    private UIElement outputCount(int max) {
        return intField(key("outputs"), getData().getOutputs().size(), 1, max, value -> {
            save(); while (getData().getOutputs().size() < value) getData().getOutputs().add(new EnderIoOutputData());
            while (getData().getOutputs().size() > value) getData().getOutputs().removeLast(); rebuild();
        });
    }
    private void resourceList(UIElement content, String name, List<ResourceLocation> values, ResourceLocation initial) {
        content.addChild(sectionTitle(key(name)));
        for (int i = 0; i < values.size(); i++) {
            int index = i;
            content.addChildren(resourceField(key("id"), values.get(i), value -> { save(); values.set(index, value); rebuild(); }),
                    RecipeEditorUi.textButton(text("remove", i + 1), null, e -> { save(); values.remove(index); rebuild(); }));
        }
        content.addChild(RecipeEditorUi.textButton(text("add"), null, e -> { save(); values.add(initial); rebuild(); }));
    }
    private void rebuild() {
        save(); clearAllChildren(); initVisualState(); load(); if (navigationView != null) selectRecipe();
    }
}
