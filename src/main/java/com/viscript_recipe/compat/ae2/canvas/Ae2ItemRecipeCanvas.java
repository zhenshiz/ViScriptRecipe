package com.viscript_recipe.compat.ae2.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ae2.data.Ae2IngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import java.util.HashMap;
import java.util.Map;

/** Uses ordinary editor slots while preserving and editing every alternative in an AE2 input. */
public abstract class Ae2ItemRecipeCanvas<D extends IVSRecipeData> extends RecipeCanvas<D> {
    private final Map<Integer, Ae2IngredientData> ingredients = new HashMap<>();
    private final Map<Integer, Integer> selectedAlternatives = new HashMap<>();

    protected Ae2ItemRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    protected void loadAe2Ingredient(int index, Ae2IngredientData ingredient) {
        ingredients.put(index, ingredient);
        int selected = Math.min(selectedAlternatives.getOrDefault(index, 0), Math.max(0, ingredient.getAlternatives().size() - 1));
        selectedAlternatives.put(index, selected);
        loadIngredientSlot(index, ingredient.getAlternatives().isEmpty() ? RecipeIngredient.empty() : ingredient.getAlternatives().get(selected));
        visualIngredientSlots[index].style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.ae2.alternatives_hint",
                ingredient.getAlternatives().size())));
    }

    protected Ae2IngredientData saveAe2Ingredient(int index) {
        var data = ingredients.computeIfAbsent(index, unused -> new Ae2IngredientData());
        int selected = selectedAlternatives.getOrDefault(index, 0);
        var current = getVisualIngredient(index);
        if (!current.isEmpty() || !data.getAlternatives().isEmpty()) {
            while (data.getAlternatives().size() <= selected) data.getAlternatives().add(RecipeIngredient.empty());
            data.getAlternatives().set(selected, current);
        }
        return data;
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var ingredient = saveAe2Ingredient(index);
        int selected = selectedAlternatives.getOrDefault(index, 0);
        content.addChild(intField("viscript_recipe.editor.ae2.alternative", selected + 1, 1,
                Math.max(1, ingredient.getAlternatives().size() + 1), value -> {
                    saveAe2Ingredient(index);
                    while (ingredient.getAlternatives().size() < value) ingredient.getAlternatives().add(RecipeIngredient.empty());
                    selectedAlternatives.put(index, value - 1);
                    loadAe2Ingredient(index, ingredient);
                    reloadProperties();
                }, Component.translatable("viscript_recipe.editor.ae2.alternative_hint")).setId("ae2_alternative"));
        super.buildIngredientProperties(content);
    }
}
