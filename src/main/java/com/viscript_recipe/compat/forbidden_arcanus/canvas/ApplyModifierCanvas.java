package com.viscript_recipe.compat.forbidden_arcanus.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.compat.forbidden_arcanus.data.ApplyModifierRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public final class ApplyModifierCanvas extends ForbiddenArcanusCanvas<ApplyModifierRecipeData> {
    private ItemSlot base;
    private ItemSlot output;
    private boolean loading;

    public ApplyModifierCanvas(NavigationView navigation, RecipeEntry entry) {super(navigation, entry);}

    @Override NativeIngredientData input(int index) {return index == 0 ? getData().getTemplate() : getData().getAddition();}

    @Override int inputCount() {return 2;}

    @Override
    public UIElement createCanvas() {
        setId("forbidden_arcanus_modifier_canvas");
        base = previewSlot(SLOT_SIZE).xeiPhantom();
        base.registerValueListener(stack -> refreshPreview(false));
        tooltip(base, key("base_preview_hint"));
        output = previewSlot(OUTPUT_SLOT_SIZE);
        tooltip(output, key("modifier_output_hint"));
        return BasicRecipeCanvasFactory.createSmithingCanvas(
                inputColumn("viscript_recipe.editor.smithing.template", ingredientSlot(0, SLOT_SIZE).setId("modifier_template")),
                inputColumn(key("base_preview"), base.setId("modifier_base")),
                inputColumn("viscript_recipe.editor.smithing.addition", ingredientSlot(1, SLOT_SIZE).setId("modifier_addition")),
                output.setId("modifier_result"));
    }

    @Override
    public void load() {
        loading = true;
        super.load();
        loading = false;
        refreshPreview(true);
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        super.setVisualIngredient(index, ingredient);
        if (!loading && base != null) refreshPreview(false);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        var modifierField = registryField(key("modifier"), FARegistries.ITEM_MODIFIER, () -> data.getModifier().toString(), false,
                        modifier -> modifier.value().displaySettings().name(), value -> {
                            var id = ResourceLocation.tryParse(value);
                            if (id != null) {
                                data.setModifier(id);
                                refreshPreview(true);
                            }
                        }).setId("modifier_definition");
        tooltip(modifierField, key("modifier_output_hint"));
        content.addChild(modifierField);
    }

    private void refreshPreview(boolean chooseBase) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        save();
        try {
            var recipe = getData().compile(entry.getType(), level.registryAccess());
            if (chooseBase && !recipe.isBaseIngredient(base.getValue())) {
                var pickaxe = Items.DIAMOND_PICKAXE.getDefaultInstance();
                var sample = recipe.isBaseIngredient(pickaxe) ? pickaxe : BuiltInRegistries.ITEM.stream().map(Item::getDefaultInstance)
                        .filter(stack -> !stack.isEmpty() && recipe.isBaseIngredient(stack)).findFirst().orElse(ItemStack.EMPTY);
                base.setItem(sample, false);
            }
            var template = recipe.template().getItems();
            var addition = recipe.addition().getItems();
            var input = new SmithingRecipeInput(template.length > 0 ? template[0] : ItemStack.EMPTY,
                    base.getValue(), addition.length > 0 ? addition[0] : ItemStack.EMPTY);
            output.setItem(recipe.matches(input, level) ? recipe.assemble(input, level.registryAccess()) : ItemStack.EMPTY, false);
        } catch (RuntimeException exception) {
            output.setItem(ItemStack.EMPTY, false);
        }
    }

    private static UIElement inputColumn(String label, UIElement slot) {
        return RecipeEditorUi.column().layout(layout -> layout.width(62).gapAll(4).alignItems(AlignItems.CENTER))
                .addChildren(RecipeEditorUi.label(Component.translatable(label)), slot);
    }
}
