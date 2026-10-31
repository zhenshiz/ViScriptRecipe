package com.viscript_recipe.gui.canvas.vanilla;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeData;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeEditorTypes;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class VanillaEventRecipeCanvas extends RecipeCanvas<VanillaEventRecipeData> {
    private final IngredientDisplaySlot[] bottles = new IngredientDisplaySlot[3];
    private final Label costLabel = emptyLabel();
    private boolean updatingBottles;

    public VanillaEventRecipeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    private boolean isBrewing() {return entry.isType(VanillaEventRecipeEditorTypes.BREWING);}

    @Override
    public void load() {
        loadIngredientSlot(0, getData().getInput());
        loadIngredientSlot(1, getData().getMaterial());
        setVisualOutput(0, getData().getResult());
        updateCostLabel();
    }

    @Override
    public void save() {
        getData().setInput(getVisualIngredient(0)).setMaterial(getVisualIngredient(1))
                .setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void setVisualOutput(int index, ItemStack item) {super.setVisualOutput(index, isBrewing() ? item.copyWithCount(1) : item);}

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        if (!isBrewing() || index != 0) {
            super.setVisualIngredient(index, ingredient);
            return;
        }
        if (updatingBottles) return;
        updatingBottles = true;
        try {
            // 三个瓶槽展示同一条输入条件，拖入、清空和属性编辑均同步。
            for (var bottle : bottles) if (bottle != null) bottle.setIngredient(ingredient.copy());
        } finally {
            updatingBottles = false;
        }
    }

    @Override
    public void screenTick() {
        // 标签轮播仅改变显示物品，不能把输入条件改成其中某个物品。
        updatingBottles = true;
        try {super.screenTick();} finally {updatingBottles = false;}
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (!isBrewing()) super.buildResultProperties(content);
        else content.addChildren(sectionTitle("viscript_recipe.editor.properties.result"),
                PropertiesView.removeCountConfig(PropertiesView.createItemStackConfigurator("viscript_recipe.config.recipe.result",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput)));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(RecipeEditorUi.label(Component.translatable(isBrewing()
                ? "viscript_recipe.editor.brewing.rules" : "viscript_recipe.editor.anvil.rules"))
                .textStyle(style -> style.textWrap(TextWrap.WRAP))
                .layout(layout -> layout.widthPercent(100)));
        if (isBrewing()) return;
        var levels = intField("viscript_recipe.editor.anvil.level_cost", getData().getLevelCost(), 1, Integer.MAX_VALUE, getData()::setLevelCost, this::updateCostLabel);
        var materials = intField("viscript_recipe.editor.anvil.material_cost", getData().getMaterialCost(), 1, Integer.MAX_VALUE, getData()::setMaterialCost);
        levels.setId("vanilla_anvil_level_cost");
        materials.setId("vanilla_anvil_material_cost");
        content.addChildren(levels, materials);
    }

    @Override
    public UIElement createCanvas() {
        setId("vanilla_event_canvas");
        var material = createIngredientSlot(1, 16);
        material.setId("vanilla_event_input_1");
        var output = createOutputSlot(0, 16);
        output.setId("vanilla_event_output");
        configureJeiOverlaySlotVisual(material, output);
        if (isBrewing()) {
            for (int i = 0; i < bottles.length; i++) {
                bottles[i] = createIngredientSlot(0, 16);
                bottles[i].setId(i == 0 ? "vanilla_event_input_0" : "vanilla_brewing_bottle_" + i);
                configureJeiOverlaySlotVisual(bottles[i]);
            }
            visualIngredientSlots[0] = bottles[0];
            return VanillaEventCanvasFactory.brewing(bottles[0], bottles[1], bottles[2], material, output);
        }
        var input = createIngredientSlot(0, 16);
        input.setId("vanilla_event_input_0");
        configureJeiOverlaySlotVisual(input);
        costLabel.setId("vanilla_anvil_cost_preview");
        return VanillaEventCanvasFactory.anvil(input, material, output, costLabel);
    }

    private void updateCostLabel() {
        if (isBrewing()) return;
        var player = Minecraft.getInstance().player;
        int cost = getData().getLevelCost();
        boolean enough = player == null || player.isCreative() || cost < 40 && cost <= player.experienceLevel;
        costLabel.setText(Component.translatable("container.repair.cost", cost));
        costLabel.textStyle(style -> style.textColor(enough ? 0xff80ff20 : 0xffff6060));
    }
}
