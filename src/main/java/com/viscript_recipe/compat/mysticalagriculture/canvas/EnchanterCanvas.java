package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureEnchanterRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.mysticalagriculture.canvas.AwakeningCanvas.useJeiCanvas;

public class EnchanterCanvas extends RecipeCanvas<MysticalAgricultureEnchanterRecipeData> {
    static final IngredientDisplaySlot preview = readOnlySlot("viscript_recipe.editor.mysticalagriculture.enchanter.result");

    public EnchanterCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    @Override
    public void load() {
        loadIngredients(getData().getIngredients());
        setResultPreview();
    }

    @Override
    public void save() {
        getData().setIngredients(getIngredients(2, true));
    }

    @Override
    public UIElement createCanvas() {
        var ingredients = new UIElement[2];
        for (int index = 0; index < 2; index++) {
            var slot = createIngredientSlot(index, 18);
            tooltip(slot, Component.translatable("viscript_recipe.editor.mysticalagriculture.enchanter.ingredient", index + 1));
            if (useJeiCanvas) configureJeiOverlaySlotVisual(slot);
            ingredients[index] = slot;
        }
        var book = createItemIcon(new ItemStack(Items.BOOK), 18);
        tooltip(book, "viscript_recipe.editor.mysticalagriculture.enchanter.book");
        if (useJeiCanvas) configureJeiOverlaySlotVisual(preview);
        return MysticalAgricultureCanvasFactory.createEnchanterCanvas(ingredients, book, preview, useJeiCanvas);
    }

    private void setResultPreview() {
        var books = MysticalAgricultureRecipeUiSupport.enchantedBooks(getData().getEnchantment());
        preview.setTagDisplayStacks(books);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.mysticalagriculture.enchanter"),
                RecipeSearchComponents.enchantment(
                        "viscript_recipe.config.mysticalagriculture.enchanter.enchantment",
                        data::getEnchantment, data::setEnchantment, this::setResultPreview)
        );
    }

    static IngredientDisplaySlot readOnlySlot(String tip) {
        var slot = (IngredientDisplaySlot) new IngredientDisplaySlot()
                .slotStyle(style -> style.showItemTooltips(true))
                .layout(layout -> {
                    layout.width(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                    layout.height(MysticalAgricultureCanvasFactory.SLOT_SIZE);
                });
        slot.style(style -> style.tooltips(Component.translatable(tip)));
        return slot;
    }
}
