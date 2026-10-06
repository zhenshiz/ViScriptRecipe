package com.viscript_recipe.compat.tacz.canvas;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.tacz.TaczRecipeEditorTypes;
import com.viscript_recipe.compat.tacz.data.TaczIngredientData;
import com.viscript_recipe.compat.tacz.data.TaczRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/** 使用 TACZ JEI 的产物与材料布局，接入 VSR 槽位和属性栏编辑。 */
public final class TaczWorkbenchCanvas extends RecipeCanvas<TaczRecipeData> {
    private int slotCount;
    private final Map<Integer, ItemSlot> preservedSlots = new HashMap<>();

    /**
     * 创建工作台画布。
     *
     * @param navigation 配方导航视图
     * @param entry 当前配方条目
     */
    public TaczWorkbenchCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        preservedSlots.clear();
        slotCount = Math.clamp(Math.max(getData().getInputSlots(), getData().getMaterials().size()), 1, TaczRecipeData.MAX_INPUTS);
        int rows = (slotCount + 5) / 6;
        int bodyHeight = Math.max(40, rows * 20);
        var panel = new UIElement().setId("tacz_canvas").layout(l -> {
            l.width(184); l.height(bodyHeight + 28); l.flexShrink(0); l.positionType(TaffyPosition.RELATIVE);
        }).addEventListener(UIEvents.MOUSE_DOWN, e -> { if (e.button == 0) selectRecipe(); });
        // JEI 为 160×40：产物槽背景位于 (2,11)，材料每行六个，间隔 20。
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureSlot(output);
        panel.addChild(at(output.setId("tacz_output"), 14, (bodyHeight - 18) / 2, 18, 18));
        for (int i = 0; i < slotCount; i++) {
            UIElement slot;
            var material = material(i);
            if (!material.getCustomJson().isBlank()) {
                var stacks = material.compile().getItems();
                var preview = new ItemSlot();
                preview.setItem(stacks.length == 0 ? ItemStack.EMPTY : stacks[0].copyWithCount(material.getCount()), false);
                configureSlot(preview);
                preservedSlots.put(i, preview);
                final int index = i;
                slot = preview.style(s -> s.tooltips(text("preserved_ingredient")))
                        .addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(SlotSelection.ingredient(index)); e.stopPropagation(); });
            } else {
                var editable = createIngredientSlot(i, JEI_SLOT_SIZE);
                configureSlot(editable);
                slot = editable;
            }
            int y = rows == 1 ? 11 : i / 6 * 20 + 1;
            panel.addChild(at(slot.setId("tacz_input_" + i), 46 + i % 6 * 20, y, 18, 18));
        }
        panel.addChild(at(RecipeEditorUi.label(groupSummary()).bindDataSource(SupplierDataSource.of(this::groupSummary))
                .setId("tacz_group_summary"), 0, bodyHeight + 8, 184, 14));
        return RecipeEditorUi.row().layout(l -> {
            l.widthPercent(100); l.flex(1); l.minWidth(0); l.minHeight(0);
            l.alignItems(AlignItems.CENTER); l.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    @Override
    public void load() {
        for (int i = 0; i < slotCount; i++) {
            var material = material(i);
            if (material.getCustomJson().isBlank()) loadIngredientSlot(i, material.getValue().copy().setCount(material.getCount()));
        }
        setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        var materials = new ArrayList<TaczIngredientData>();
        for (int i = 0; i < slotCount; i++) {
            var material = material(i);
            if (material.getCustomJson().isBlank()) {
                var value = getVisualIngredient(i);
                material.setCount(value.getCount()).setValue(value.setCount(1));
            }
            materials.add(material);
        }
        // 保留中间空槽的索引，末尾空槽只由 inputSlots 记录。
        while (!materials.isEmpty() && materials.getLast().isEmpty()) materials.removeLast();
        getData().setMaterials(materials).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var material = material(index);
        if (material.getCustomJson().isBlank()) {
            super.buildIngredientProperties(content);
            // 数量修改只更新槽位，保留输入焦点，允许连续输入多位数字。
            content.addChild(intField(key("material_count"), getVisualIngredient(index).getCount(), 1, Integer.MAX_VALUE,
                    value -> setVisualIngredient(index, getVisualIngredient(index).setCount(value))).setId("tacz_material_count"));
            return;
        }
        content.addChildren(sectionTitle(key("preserved_ingredient")),
                RecipeEditorUi.label(text("preserved_ingredient_hint")),
                intField(key("material_count"), material.getCount(), 1, Integer.MAX_VALUE, value -> {
                    material.setCount(value);
                    var preview = preservedSlots.get(index);
                    if (preview != null) preview.setItem(preview.getValue().copyWithCount(value), false);
                }).setId("tacz_material_count"),
                RecipeEditorUi.textButton(text("replace_ingredient"), null, e -> {
                    save();
                    material.setCustomJson("").setValue(RecipeIngredient.empty());
                    navigationView.reloadCanvas();
                    reloadProperties();
                }).setId("tacz_replace_ingredient"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var tabs = new LinkedHashMap<ResourceLocation, Component>();
        for (var tab : TabConfig.DEFAULT_TABS) tabs.put(tab.id(), tab.getName());
        for (var block : TimelessAPI.getAllCommonBlockIndex()) {
            for (var tab : block.getValue().getData().getTabs()) tabs.put(tab.id(), tab.getName());
        }
        tabs.putIfAbsent(getData().getGroup(), Component.literal(getData().getGroup().toString()));
        var search = new TabSearchBox(getData().getGroup(), tabs, getData()::setGroup);
        search.setId("tacz_group");
        search.setOnValueChanged(value -> { if (value != null) getData().setGroup(value); });
        search.searchStyle(s -> { s.maxItemCount(8); s.scrollerViewHeight(120); s.closeAfterSelect(true); });
        search.layout(l -> { l.widthPercent(100); l.height(18); });
        content.addChildren(sectionTitle(TaczRecipeEditorTypes.NAME_KEY),
                RecipeEditorUi.fieldGroup(key("group"), search, text("group_hint")),
                intField(key("input_slots"), slotCount, 1, TaczRecipeData.MAX_INPUTS, value -> {
                    navigationView.saveCanvas();
                    getData().setInputSlots(Math.max(value, getData().getMaterials().size()));
                    navigationView.loadCanvas();
                }).setId("tacz_input_slots"));
    }

    private TaczIngredientData material(int index) {
        return index < getData().getMaterials().size() ? getData().getMaterials().get(index) : new TaczIngredientData();
    }

    private Component groupSummary() { return text("group_summary", getData().getGroup().toString()); }
    private static String key(String path) { return "viscript_recipe.editor.tacz." + path; }
    private static Component text(String path, Object... args) { return Component.translatable(key(path), args); }
    private static void configureSlot(ItemSlot slot) {
        configureJeiOverlaySlotVisual(slot);
        var texture = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/slot.png");
        slot.style(s -> s.backgroundTexture(ViScriptRecipe.isModLoaded("jei") && ViScriptRecipe.isPresentResource(texture)
                ? SpriteTexture.of(texture).setSprite(0, 0, 18, 18) : ItemSlot.ITEM_SLOT_TEXTURE));
    }
    private static UIElement at(UIElement element, int x, int y, int width, int height) {
        return element.layout(l -> { l.positionType(TaffyPosition.ABSOLUTE); l.left(x); l.top(y); l.width(width); l.height(height); });
    }

    private static final class TabSearchBox extends RegistrySearchBox<ResourceLocation> {
        private final Consumer<ResourceLocation> onTyped;

        private TabSearchBox(ResourceLocation current, LinkedHashMap<ResourceLocation, Component> tabs, Consumer<ResourceLocation> onTyped) {
            super(current, () -> null, Function.identity(), ResourceLocation::toString,
                    (word, results) -> tabs.keySet().stream().sorted(Comparator.comparing(ResourceLocation::toString))
                            .filter(id -> (id + " " + tabs.get(id).getString()).toLowerCase(Locale.ROOT).contains(word.toLowerCase(Locale.ROOT)))
                            .forEach(results::acceptResult),
                    UIElementProvider.text(id -> Component.literal(id + " — ").append(tabs.get(id))));
            this.onTyped = onTyped;
            textField.setResourceLocationOnly();
        }

        @Override
        protected void onSearchWordChanged(String word) {
            if (onTyped != null && word != null && !word.isBlank()) {
                var id = ResourceLocation.tryParse(word);
                if (id != null && !id.getPath().isEmpty()) onTyped.accept(id);
            }
            super.onSearchWordChanged(word);
        }
    }
}
