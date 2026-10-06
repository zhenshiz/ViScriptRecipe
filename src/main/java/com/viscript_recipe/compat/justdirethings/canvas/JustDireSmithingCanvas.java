package com.viscript_recipe.compat.justdirethings.canvas;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.justdirethings.data.*;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import static com.viscript_recipe.compat.justdirethings.JustDireRecipeEditorTypes.key;
import static com.viscript_recipe.compat.justdirethings.canvas.JustDireCanvasLayout.*;

/** 复用 VSR 材料和产物属性；能力升级产物按原生逻辑预览。 */
public final class JustDireSmithingCanvas extends RecipeCanvas<JustDireSmithingData> {
    private ItemSlot abilityOutput;
    /**
     * 创建绑定到指定配方条目的画布。
     *
     * @param navigation 编辑器导航视图
     * @param entry 当前配方条目
     */
    public JustDireSmithingCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private boolean ability() { return entry.getType().getPath().equals("ability"); }
    private JustDireIngredientData material(int index) {
        return switch (index) { case 0 -> getData().getTemplate(); case 1 -> getData().getBase(); default -> getData().getAddition(); };
    }

    @Override
    public UIElement createCanvas() {
        var panel = panel(entry.getType().getPath(), 156, 52);
        for (int i = 0; i < 3; i++) {
            final int index = i;
            ItemSlot slot;
            if (material(i).getCustomJson().isBlank()) slot = createIngredientSlot(i, JEI_SLOT_SIZE);
            else {
                slot = new ItemSlot();
                slot.bindDataSource(SupplierDataSource.of(() -> {
                    var stacks = material(index).compile().getItems();
                    return stacks.length == 0 ? ItemStack.EMPTY : stacks[(int) ((System.currentTimeMillis() / 1000) % stacks.length)].copy();
                }));
                slot.addEventListener(UIEvents.MOUSE_DOWN, e -> selectSlot(SlotSelection.ingredient(index)));
            }
            skin(slot);
            slot.addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation());
            panel.addChild(at(slot.setId("justdire_smithing_input_" + i), 24 + i * 18, 8, 18, 18));
        }
        ItemSlot output;
        if (ability()) {
            abilityOutput = new ItemSlot(); output = abilityOutput;
            output.addEventListener(UIEvents.MOUSE_DOWN, e -> selectSlot(SlotSelection.result(0)));
        } else output = createOutputSlot(0, JEI_SLOT_SIZE);
        skin(output); output.addEventListener(UIEvents.MOUSE_DOWN, e -> e.stopPropagation());
        panel.addChildren(arrow(84, 8), at(output.setId("justdire_smithing_output"), 114, 8, 18, 18),
                at(RecipeEditorUi.label(Component.translatable(key(ability() ? "ability_hint" : "paxel_hint"))).setId("justdire_summary"), 0, 36, 156, 14));
        return centered(panel);
    }

    @Override
    public void load() {
        for (int i = 0; i < 3; i++) if (material(i).getCustomJson().isBlank()) loadIngredientSlot(i, material(i).getValue());
        if (ability()) refreshPreview(); else setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        for (int i = 0; i < 3; i++) if (material(i).getCustomJson().isBlank()) material(i).setValue(getVisualIngredient(i));
        if (!ability()) getData().setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        super.setVisualIngredient(index, ingredient);
        if (abilityOutput != null) { material(index).setValue(ingredient.copy()); refreshPreview(); }
    }

    private void refreshPreview() {
        if (abilityOutput == null) return;
        try {
            var recipe = (SmithingRecipe) getData().compile(entry.getType());
            abilityOutput.setItem(recipe.assemble(new SmithingRecipeInput(first(0), first(1), first(2)), Platform.getFrozenRegistry()), false);
        } catch (IllegalArgumentException exception) {
            abilityOutput.setItem(ItemStack.EMPTY, false);
        }
    }

    private ItemStack first(int index) {
        var items = material(index).compile().getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0].copy();
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        var material = material(selectedSlotIndex());
        if (material.getCustomJson().isBlank()) { super.buildIngredientProperties(content); return; }
        content.addChildren(RecipeEditorUi.label(Component.translatable(key("preserved_ingredient"))),
                RecipeEditorUi.textButton(Component.translatable(key("replace_ingredient")), null, e -> {
                    save(); material.setCustomJson("").setValue(RecipeIngredient.empty());
                    navigationView.loadCanvas(); reloadProperties();
                }).setId("justdire_replace_ingredient"));
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (ability()) content.addChild(RecipeEditorUi.label(Component.translatable(key("ability_output_hint"))));
        else super.buildResultProperties(content);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(RecipeEditorUi.label(Component.translatable(key(ability() ? "ability_output_hint" : "paxel_output_hint"))));
    }
}
