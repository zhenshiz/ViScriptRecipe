package com.viscript_recipe.compat.bakery.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.bakery.BakeryRecipeEditorTypes;
import com.viscript_recipe.compat.bakery.data.BakeryBakingStationRecipeData;
import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.viscript_recipe.compat.farm_and_charm.canvas.FarmCharmCanvasFactory.*;

/** 沿用 Bakery JEI 背景和坐标，通过 VSR 槽位及属性栏编辑材料与产物。 */
public final class BakeryBakingStationCanvas extends RecipeCanvas<BakeryBakingStationRecipeData> {
    private final Map<Integer, FarmCharmIngredientData> inputs = new HashMap<>();
    private final Map<Integer, Integer> alternatives = new HashMap<>();
    private int slotCount;

    /**
     * 创建指定烘焙配方的画布。
     * @param navigation 配方导航视图
     * @param entry 当前配方条目
     */
    public BakeryBakingStationCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        slotCount = Math.clamp(Math.max(getData().getInputSlots(), getData().getInputs().size()), 2, 3);
        var body = panel("bakery_baking_station", 176, 85).style(style -> style.backgroundTexture(
                SpriteTexture.of(ResourceLocation.fromNamespaceAndPath("bakery", "textures/gui/baking_station.png"))
                        .setSprite(0, 0, 176, 85)));
        for (int i = 0; i < slotCount; i++) {
            var input = createIngredientSlot(i, JEI_SLOT_SIZE);
            if (i < 2) {
                configureJeiOverlaySlotVisual(input);
                body.addChild(slot(input, "bakery_input_" + i, 49, 24 + i * 18));
            } else {
                // 原生 Codec 接受三个材料；JEI 只绘制两个，额外槽位保持可编辑。
                body.addChild(standardSlot(input, "bakery_input_" + i, 29, 34));
            }
        }
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        body.addChild(slot(output, "bakery_output", 109, 34));
        return centered(body);
    }

    @Override
    public void load() {
        for (int i = 0; i < slotCount; i++) {
            var input = i < getData().getInputs().size() ? getData().getInputs().get(i) : new FarmCharmIngredientData();
            inputs.put(i, input);
            loadInput(i, input);
        }
        setVisualOutput(0, getData().getResult());
    }

    private void loadInput(int index, FarmCharmIngredientData input) {
        int selected = Math.min(alternatives.getOrDefault(index, 0), Math.max(0, input.getAlternatives().size() - 1));
        alternatives.put(index, selected);
        loadIngredientSlot(index, input.getAlternatives().isEmpty() ? RecipeIngredient.empty() : input.getAlternatives().get(selected));
    }

    private FarmCharmIngredientData saveInput(int index) {
        var input = inputs.get(index);
        int selected = alternatives.getOrDefault(index, 0);
        var value = getVisualIngredient(index);
        if (!value.isEmpty() || !input.getAlternatives().isEmpty()) {
            while (input.getAlternatives().size() <= selected) input.getAlternatives().add(RecipeIngredient.empty());
            input.getAlternatives().set(selected, value);
        }
        return input;
    }

    @Override
    public void save() {
        var values = new ArrayList<FarmCharmIngredientData>();
        for (int i = 0; i < slotCount; i++) values.add(saveInput(i));
        if (getData().getInputs().size() > slotCount)
            values.addAll(getData().getInputs().subList(slotCount, getData().getInputs().size()));
        getData().setInputs(values).setInputSlots(slotCount).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var input = saveInput(index);
        content.addChild(intField("viscript_recipe.editor.farm_and_charm.alternative",
                alternatives.getOrDefault(index, 0) + 1, 1, Math.max(1, input.getAlternatives().size() + 1), value -> {
                    saveInput(index);
                    while (input.getAlternatives().size() < value) input.getAlternatives().add(RecipeIngredient.empty());
                    alternatives.put(index, value - 1);
                    loadInput(index, input);
                    reloadProperties();
                }, Component.translatable("viscript_recipe.editor.farm_and_charm.alternative_hint"))
                .setId("bakery_alternative"));
        super.buildIngredientProperties(content);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(sectionTitle(BakeryRecipeEditorTypes.NAME_KEY),
                switchField("viscript_recipe.editor.bakery.third_input", slotCount == 3, value -> {
                    save();
                    // 缩减槽位只隐藏空槽，不删除已经设置的第三个材料。
                    if (!value && !inputs.get(2).isEmpty()) { reloadProperties(); return; }
                    getData().setInputSlots(value ? 3 : 2);
                    if (!value) getData().getInputs().removeLast();
                    navigationView.loadCanvas();
                    reloadProperties();
                }, Component.translatable("viscript_recipe.editor.bakery.third_input_hint")).setId("bakery_third_input"));
    }
}
