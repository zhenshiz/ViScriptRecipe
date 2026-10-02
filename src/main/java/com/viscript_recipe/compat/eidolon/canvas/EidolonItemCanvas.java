package com.viscript_recipe.compat.eidolon.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.eidolon.data.EidolonIngredientData;
import com.viscript_recipe.compat.eidolon.data.EidolonIngredientValueData;
import com.viscript_recipe.data.IngredientValueKind;
import net.minecraft.world.item.ItemStack;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.viscript_recipe.gui.views.PropertiesView.createItemStackConfigurator;
import static com.viscript_recipe.gui.views.PropertiesView.removeCountConfig;

/** 修改选中候选项时，保留其他物品、标签和数据组件匹配条件。 */
public abstract class EidolonItemCanvas<D extends IVSRecipeData> extends RecipeCanvas<D> {
    protected final Map<Integer, EidolonIngredientData> inputs = new LinkedHashMap<>();
    private final Map<Integer, Integer> alternatives = new HashMap<>();

    protected EidolonItemCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    protected IngredientDisplaySlot inputSlot(int index, EidolonIngredientData data, Component tooltip) {
        inputs.put(index, data);
        var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
        slot.setId("eidolon_input_" + index);
        tooltip(slot, tooltip);
        return slot;
    }

    @Override
    public void load() { inputs.forEach(this::loadInput); }

    @Override
    public void save() { inputs.forEach(this::saveInput); }

    private void loadInput(int index, EidolonIngredientData data) {
        int selected = Math.min(alternatives.getOrDefault(index, 0), Math.max(0, data.getAlternatives().size() - 1));
        alternatives.put(index, selected);
        loadIngredientSlot(index, data.getAlternatives().isEmpty() ? RecipeIngredient.empty() : data.getAlternatives().get(selected).getValue());
    }

    private void saveInput(int index, EidolonIngredientData data) {
        int selected = alternatives.getOrDefault(index, 0);
        var ingredient = getVisualIngredient(index);
        if (data.getAlternatives().isEmpty() && ingredient.isEmpty()) return;
        while (data.getAlternatives().size() <= selected) data.getAlternatives().add(new EidolonIngredientValueData());
        var alternative = data.getAlternatives().get(selected);
        var before = alternative.getValue();
        if (alternative.isMatchComponents() && ingredient.getKind() == IngredientValueKind.ITEM
                && !ItemStack.isSameItemSameComponents(before.getItem(), ingredient.getItem())) {
            alternative.updatePredicate(ingredient.getItem());
        }
        alternative.setValue(ingredient.copy().setCount(1));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var data = inputs.get(index);
        if (data == null) return;
        saveInput(index, data);
        int selected = alternatives.getOrDefault(index, 0);
        while (data.getAlternatives().size() <= selected) data.getAlternatives().add(new EidolonIngredientValueData());
        content.addChild(intField("viscript_recipe.config.eidolon.alternative", selected + 1, 1,
                data.getAlternatives().size() + 1, value -> {
                    saveInput(index, data);
                    while (data.getAlternatives().size() < value) data.getAlternatives().add(new EidolonIngredientValueData());
                    alternatives.put(index, value - 1);
                    loadInput(index, data);
                    reloadProperties();
                }, Component.translatable("viscript_recipe.config.eidolon.alternative_hint")));
        var alternative = data.getAlternatives().get(selected);
        content.addChild(switchField("viscript_recipe.config.eidolon.match_components", alternative.isMatchComponents(), enabled -> {
            saveInput(index, data);
            alternative.setMatchComponents(enabled);
            if (enabled) alternative.updatePredicate(alternative.getValue().getItem());
            else if (alternative.getValue().getKind() == IngredientValueKind.ITEM) {
                alternative.setValue(RecipeIngredient.item(new ItemStack(alternative.getValue().getItem().getItem())));
            }
            loadInput(index, data); reloadProperties();
        }, Component.translatable("viscript_recipe.config.eidolon.match_components_hint")));
        if (alternative.isMatchComponents()) content.addChild(switchField(
                "viscript_recipe.config.eidolon.strict_components", alternative.isStrict(), alternative::setStrict));
        super.buildIngredientProperties(content);
        if (alternative.isMatchComponents() && alternative.getValue().getKind() == IngredientValueKind.TAG) {
            content.addChild(removeCountConfig(createItemStackConfigurator(
                    "viscript_recipe.config.eidolon.component_example", alternative::componentPreview, alternative::updatePredicate)));
        }
    }

    protected void rebuild() {
        inputs.clear(); alternatives.clear(); clearAllChildren();
        initVisualState(); load();
        if (navigationView != null) selectRecipe();
    }

    protected static void resize(List<EidolonIngredientData> list, int size) {
        while (list.size() < size) list.add(new EidolonIngredientData());
        while (list.size() > size) list.removeLast();
    }

    protected static Component text(String path, Object... args) {
        return Component.translatable("viscript_recipe.editor.eidolon." + path, args);
    }
}
