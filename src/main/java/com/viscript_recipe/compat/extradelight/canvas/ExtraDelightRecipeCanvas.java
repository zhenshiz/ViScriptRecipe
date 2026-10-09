package com.viscript_recipe.compat.extradelight.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.extradelight.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeEditorTypes.key;

/** 复用 VSR 槽位、属性选择及 JEI 拖放，工作站布局与原模组 JEI 对齐。 */
public final class ExtraDelightRecipeCanvas extends FluidRecipeCanvas<ExtraDelightRecipeData> {
    private boolean editing;

    public ExtraDelightRecipeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() {
        editing = false;
        loadIngredients(getData().getIngredients());
        switch (getData()) {
            case ExtraDelightBottleFluidRecipeData d -> { setVisualFluidInput(0, d.getFluid()); }
            case ExtraDelightChillerRecipeData d -> { setVisualOutput(0, d.getResult()); setVisualFluidInput(0, FluidIngredientData.fluid(d.getFluid())); setExtraItem(d.getContainer()); }
            case ExtraDelightDoughShapingRecipeData d -> { setVisualOutput(0, d.getResult()); }
            case ExtraDelightDryingRackRecipeData d -> { setVisualOutput(0, d.getResult()); }
            case ExtraDelightDynamicJamRecipeData d -> { setVisualOutput(0, d.getResult()); setExtraItem(d.getContainer()); }
            case ExtraDelightDynamicToastRecipeData d -> { setVisualOutput(0, d.getResult()); }
            case ExtraDelightEvaporatorRecipeData d -> { setVisualFluidInput(0, d.getFluid()); setVisualOutput(0, d.getResult()); }
            case ExtraDelightFeastRecipeData d -> { setExtraItem(d.getFeast()); setVisualOutput(0, d.getResult()); }
            case ExtraDelightJuicerRecipeData d -> { setVisualOutput(0, d.getResult()); setVisualFluidOutput(0, d.getFluidOutput()); }
            case ExtraDelightMeltingPotRecipeData d -> { setVisualFluidOutput(0, d.getFluidOutput()); }
            case ExtraDelightMixingBowlRecipeData d -> { setVisualOutput(0, d.getResult()); for (int i = 0; i < d.getFluids().size(); i++) setVisualFluidInput(i, d.getFluids().get(i)); setExtraItem(d.getContainer()); setVisualIngredient(9, d.getUtensil()); }
            case ExtraDelightMortarRecipeData d -> { setVisualOutput(0, d.getResult()); setVisualFluidOutput(0, d.getFluidOutput()); }
            case ExtraDelightOvenRecipeData d -> { setVisualOutput(0, d.getResult()); setExtraItem(d.getContainer()); }
            case ExtraDelightToolOnBlockRecipeData d -> { setVisualOutput(0, d.getResult()); }
            case ExtraDelightVatRecipeData d -> { setVisualOutput(0, d.getResult()); setVisualFluidInput(0, d.getFluid()); setExtraItem(d.getContainer()); for (int i = 0; i < d.getStages().size(); i++) setVisualIngredient(6 + i, d.getStages().get(i).getIngredient()); }
            default -> throw new IllegalStateException("Unsupported Extra Delight canvas");
        }
        editing = true;
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        super.setVisualIngredient(index, ingredient);
        if (!editing) return;
        if (getData() instanceof ExtraDelightMixingBowlRecipeData d && index == 9) d.setUtensilCondition("");
        else if (getData() instanceof ExtraDelightVatRecipeData d && index >= 6 && index - 6 < d.getStages().size())
            d.getStages().get(index - 6).setIngredientCondition("");
        else if (index < getData().getIngredientConditions().size()) getData().getIngredientConditions().set(index, "");
    }

    @Override
    public void setVisualFluidInput(int index, FluidIngredientData value) {
        var previous = getVisualFluidInput(index);
        super.setVisualFluidInput(index, value);
        if (editing && index < getData().getFluidConditions().size()
                && (previous.getKind() != value.getKind() || (value.getKind() == FluidIngredientKind.TAG
                ? !java.util.Objects.equals(previous.getTag(), value.getTag())
                : !FluidStack.isSameFluidSameComponents(previous.getFluid(), value.getFluid()))))
            getData().getFluidConditions().set(index, "");
    }

    @Override
    public void save() {
        switch (getData()) {
            case ExtraDelightBottleFluidRecipeData d -> { saveIngredients(d, 1); d.setFluid(getVisualFluidInput(0)); }
            case ExtraDelightChillerRecipeData d -> { saveIngredients(d, 4); d.setResult(getVisualOutput(0).getItem()); d.setFluid(getVisualFluidInput(0).getFluid().copyWithAmount(getVisualFluidInput(0).getAmount())); d.setContainer(getExtraItem()); }
            case ExtraDelightDoughShapingRecipeData d -> { saveIngredients(d, 1); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightDryingRackRecipeData d -> { saveIngredients(d, 1); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightDynamicJamRecipeData d -> { saveIngredients(d, 6); d.setResult(getVisualOutput(0).getItem()); d.setContainer(getExtraItem()); }
            case ExtraDelightDynamicToastRecipeData d -> { saveIngredients(d, 9); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightEvaporatorRecipeData d -> { d.setFluid(getVisualFluidInput(0)); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightFeastRecipeData d -> { saveIngredients(d, 1); d.setFeast(getExtraItem()); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightJuicerRecipeData d -> { saveIngredients(d, 1); d.setResult(getVisualOutput(0).getItem()); d.setFluidOutput(getVisualFluidOutput(0)); }
            case ExtraDelightMeltingPotRecipeData d -> { saveIngredients(d, 1); d.setFluidOutput(getVisualFluidOutput(0)); }
            case ExtraDelightMixingBowlRecipeData d -> { saveIngredients(d, 9); d.setResult(getVisualOutput(0).getItem()); var fluids = new ArrayList<FluidIngredientData>(); var conditions = new ArrayList<String>(); for (int i = 0; i < fluidInputSlots.length; i++) if (fluidInputSlots[i] != null && !getVisualFluidInput(i).isEmpty()) { fluids.add(getVisualFluidInput(i)); conditions.add(i < d.getFluidConditions().size() ? d.getFluidConditions().get(i) : ""); } d.setFluids(fluids); d.setFluidConditions(conditions); d.setContainer(getExtraItem()); var utensil = getVisualIngredient(9); if (!same(d.getUtensil(), utensil)) d.setUtensilCondition(""); d.setUtensil(utensil); }
            case ExtraDelightMortarRecipeData d -> { saveIngredients(d, 1); d.setResult(getVisualOutput(0).getItem()); d.setFluidOutput(getVisualFluidOutput(0)); }
            case ExtraDelightOvenRecipeData d -> { saveIngredients(d, 9); d.setResult(getVisualOutput(0).getItem()); d.setContainer(getExtraItem()); }
            case ExtraDelightToolOnBlockRecipeData d -> { saveIngredients(d, 2); d.setResult(getVisualOutput(0).getItem()); }
            case ExtraDelightVatRecipeData d -> { saveIngredients(d, 6); d.setResult(getVisualOutput(0).getItem()); d.setFluid(getVisualFluidInput(0)); d.setContainer(getExtraItem()); for (int i = 0; i < d.getStages().size(); i++) {     var stage = d.getStages().get(i); var ingredient = getVisualIngredient(6 + i);     if (!same(stage.getIngredient(), ingredient)) stage.setIngredientCondition("");     stage.setIngredient(ingredient); } }
            default -> throw new IllegalStateException("Unsupported Extra Delight canvas");
        }
    }

    private static boolean same(RecipeIngredient a, RecipeIngredient b) {
        return RecipeImporter.ingredientKey(a).equals(RecipeImporter.ingredientKey(b));
    }

    private void saveIngredients(ExtraDelightRecipeData data, int count) {
        var values = new ArrayList<RecipeIngredient>(); var conditions = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            var ingredient = getVisualIngredient(i);
            String raw = i < data.getIngredientConditions().size() && same(data.ingredient(i), ingredient)
                    ? data.getIngredientConditions().get(i) : "";
            if (!ingredient.isEmpty() || !raw.isBlank()) { values.add(ingredient); conditions.add(raw); }
        }
        data.getIngredients().clear(); data.getIngredients().addAll(values); data.setIngredientConditions(conditions);
    }

    @Override
    public UIElement createCanvas() {
        String type = entry.getType().getPath(); UIElement panel;
        switch (type) {
            case "mortar" -> { panel = panel(type, "jei.png", 0, 0, 84, 52); input(panel, 0, 9, 18); output(panel, 61, 26); fluidOutput(panel, 61, 8); }
            case "oven" -> { panel = panel(type, "jei.png", 0, 53, 121, 72); grid(panel, 9, 3, 1, 1); extra(panel, 63, 47); output(panel, 95, 20); }
            case "drying_rack" -> { panel = panel(type, "jei.png", 0, 125, 85, 47); input(panel, 0, 4, 22); output(panel, 64, 22); }
            case "dough_shaping" -> { panel = panel(type, "jei.png", 0, 238, 78, 18); input(panel, 0, 1, 1); output(panel, 61, 1); }
            case "tool_on_block" -> { panel = panel(type, "jei.png", 0, 220, 64, 18); input(panel, 1, 1, 1); input(panel, 0, 24, 1); output(panel, 47, 1); }
            case "feast" -> { panel = panel(type, "jei.png", 0, 220, 64, 18); input(panel, 0, 1, 1); extra(panel, 24, 1); output(panel, 47, 1); }
            case "melting_pot" -> { panel = panel(type, "jei.png", 203, 107, 53, 47); input(panel, 0, 7, 7); fluidOutput(panel, 30, 7); }
            case "chiller" -> { panel = panel(type, "jei.png", 132, 0, 124, 73); grid(panel, 4, 2, 45, 20); fluidInput(panel, 0, 24, 1, 16, 71); extra(panel, 54, 56); output(panel, 107, 30); }
            case "mixing_bowl" -> {
                var data = (ExtraDelightMixingBowlRecipeData) getData();
                panel = panel(type, "jei.png", 109, 182, 147, 74); grid(panel, 9, 3, 47, 11);
                input(panel, 9, 101, 12); extra(panel, 105, 52); output(panel, 130, 29);
                for (int i = 0; i < Math.max(6, data.getFluids().size()); i++) fluidInput(panel, i, 24, 61 - i * 12, 16, 12);
            }
            case "juicer" -> { panel = panel(type, "jei.png", 0, 181, 64, 36); input(panel, 0, 1, 10); fluidOutput(panel, 24, 1); output(panel, 47, 1); }
            case "bottle_fluid" -> { panel = panel(type, "jei.png", 84, 125, 61, 54); input(panel, 0, 2, 37); fluidInput(panel, 0, 24, 1, 18, 18); panel.addChild(at(createItemIcon(new ItemStack(net.minecraft.world.item.Items.GLASS_BOTTLE), 18), 45, 37, 18, 18)); }
            case "evaporator" -> { panel = panel(type, "jei3.png", 0, 183, 84, 73); fluidInput(panel, 0, 1, 1, 16, 71); output(panel, 44, 30); }
            case "vat" -> {
                var data = (ExtraDelightVatRecipeData) getData(); int n = data.getStages().size();
                panel = new UIElement().setId("extradelight_canvas_vat").layout(l -> { l.width(101); l.height(80 + n * 31); l.flexShrink(0); l.positionType(TaffyPosition.RELATIVE); });
                panel.addChild(at(skin("jei3.png", 0, 0, 101, 47), 0, 0, 101, 47)); grid(panel, 6, 3, 48, 1); fluidInput(panel, 0, 25, 1, 16, 34);
                for (int i = 0; i < n; i++) { panel.addChild(at(skin("jei3.png", 0, 47, 101, 31), 0, 47 + i * 31, 101, 31)); input(panel, 6 + i, 58, 59 + i * 31); panel.addChild(at(RecipeEditorUi.label(Component.translatable(key("stage"), i + 1)), 0, 49 + i * 31, 56, 27)); }
                panel.addChild(at(skin("jei3.png", 0, 78, 101, 33), 0, 47 + n * 31, 101, 33)); extra(panel, 84, 62 + n * 31); output(panel, 58, 62 + n * 31);
            }
            case "dynamic_jam", "dynamic_toast" -> {
                boolean jam = type.equals("dynamic_jam"); panel = panel(type, "jei.png", 0, 53, 121, 72); grid(panel, jam ? 6 : 9, 3, 1, 1); output(panel, 95, 20); if (jam) extra(panel, 63, 47);
            }
            default -> throw new IllegalStateException("Unsupported Extra Delight type: " + type);
        }
        panel.addEventListener(UIEvents.MOUSE_DOWN, e -> { if (e.button == 0) selectRecipe(); });
        return centered(panel);
    }

    static UIElement at(UIElement element, int x, int y, int w, int h) {
        return element.layout(l -> { l.positionType(TaffyPosition.ABSOLUTE); l.left(x); l.top(y); l.width(w); l.height(h); });
    }
    static UIElement skin(String texture, int u, int v, int w, int h) {
        return new UIElement().style(s -> s.backgroundTexture(SpriteTexture.of(ResourceLocation.parse("extradelight:textures/gui/" + texture)).setSprite(u, v, w, h)));
    }
    static UIElement panel(String type, String texture, int u, int v, int w, int h) {
        return skin(texture, u, v, w, h).setId("extradelight_canvas_" + type).layout(l -> { l.width(w); l.height(h); l.flexShrink(0); l.positionType(TaffyPosition.RELATIVE); });
    }
    static UIElement centered(UIElement panel) {
        return RecipeEditorUi.row().layout(l -> { l.widthPercent(100); l.flex(1); l.minWidth(0); l.minHeight(0); l.alignItems(AlignItems.CENTER); l.justifyContent(AlignContent.CENTER); }).addChild(panel);
    }
    private void input(UIElement panel, int index, int x, int y) {
        var slot = createIngredientSlot(index, JEI_SLOT_SIZE); configureJeiOverlaySlotVisual(slot);
        panel.addChild(at(slot.setId("extradelight_input_" + index), x, y, 18, 18));
    }
    private void grid(UIElement panel, int count, int width, int x, int y) {
        for (int i = 0; i < count; i++) input(panel, i, x + i % width * 18, y + i / width * 18);
    }
    private void output(UIElement panel, int x, int y) {
        var slot = createOutputSlot(0, JEI_SLOT_SIZE); configureJeiOverlaySlotVisual(slot);
        panel.addChild(at(slot.setId("extradelight_output"), x, y, 18, 18));
    }
    private void extra(UIElement panel, int x, int y) {
        var slot = createExtraItemSlot(18, Component.translatable(key("container"))); configureJeiOverlaySlotVisual(slot);
        panel.addChild(at(slot.setId("extradelight_container"), x, y, 18, 18));
    }
    private void fluidInput(UIElement panel, int index, int x, int y, int w, int h) {
        var slot = createFluidInputSlot(index); configureJeiOverlayFluidSlotVisual(slot);
        panel.addChild(at(slot.setId("extradelight_fluid_input_" + index), x, y, w, h));
    }
    private void fluidOutput(UIElement panel, int x, int y) {
        var slot = createFluidOutputSlot(0, 18); configureJeiOverlayFluidSlotVisual(slot);
        panel.addChild(at(slot.setId("extradelight_fluid_output"), x, y, 18, 18));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        switch (getData()) {
            case ExtraDelightBottleFluidRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
            }
            case ExtraDelightChillerRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
                content.addChild(floatField(key("experience"), d.getExperience(), 0, Float.MAX_VALUE, d::setExperience).setId("extradelight_experience"));
                content.addChild(switchField(key("consumeContainer"), d.isConsumeContainer(), d::setConsumeContainer).setId("extradelight_consumeContainer"));
            }
            case ExtraDelightDoughShapingRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
            }
            case ExtraDelightDryingRackRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
                content.addChild(floatField(key("experience"), d.getExperience(), 0, Float.MAX_VALUE, d::setExperience).setId("extradelight_experience"));
            }
            case ExtraDelightDynamicJamRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(textField(key("graphic"), d.getGraphic(), d::setGraphic).setId("extradelight_graphic"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
                content.addChild(floatField(key("experience"), d.getExperience(), 0, Float.MAX_VALUE, d::setExperience).setId("extradelight_experience"));
                content.addChild(textField(key("recipeBookTab"), d.getRecipeBookTab(), d::setRecipeBookTab).setId("extradelight_recipeBookTab"));
            }
            case ExtraDelightDynamicToastRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(textField(key("graphic"), d.getGraphic(), d::setGraphic).setId("extradelight_graphic"));
                content.addChild(selector(key("category"), List.of(CraftingBookCategory.values()), d.getCategory(), v -> Component.literal(v.getSerializedName()), d::setCategory));
            }
            case ExtraDelightEvaporatorRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
                content.addChild(resourceField(key("lootTable"), d.getLootTable(), d::setLootTable).setId("extradelight_lootTable"));
                content.addChild(resourceField(key("displayBlock"), d.getDisplayBlock(), d::setDisplayBlock).setId("extradelight_displayBlock"));
            }
            case ExtraDelightFeastRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
            }
            case ExtraDelightJuicerRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("chance"), d.getChance(), 0, 100, d::setChance).setId("extradelight_chance"));
            }
            case ExtraDelightMeltingPotRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
            }
            case ExtraDelightMixingBowlRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("stirs"), d.getStirs(), 1, Integer.MAX_VALUE, d::setStirs).setId("extradelight_stirs"));
            }
            case ExtraDelightMortarRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("grinds"), d.getGrinds(), 1, Integer.MAX_VALUE, d::setGrinds).setId("extradelight_grinds"));
            }
            case ExtraDelightOvenRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("time"), d.getTime(), 1, Integer.MAX_VALUE, d::setTime).setId("extradelight_time"));
                content.addChild(floatField(key("experience"), d.getExperience(), 0, Float.MAX_VALUE, d::setExperience).setId("extradelight_experience"));
                content.addChild(switchField(key("consumeContainer"), d.isConsumeContainer(), d::setConsumeContainer).setId("extradelight_consumeContainer"));
            }
            case ExtraDelightToolOnBlockRecipeData d -> {

            }
            case ExtraDelightVatRecipeData d -> {
                content.addChild(textField(key("group"), d.getGroup(), d::setGroup).setId("extradelight_group"));
                content.addChild(intField(key("stage_count"), d.getStages().size(), 1, MAX_INGREDIENT - 6, count -> {
                    save(); while (d.getStages().size() < count) d.getStages().add(new ExtraDelightVatStageData());
                    while (d.getStages().size() > count) d.getStages().removeLast();
                    navigationView.loadCanvas(); reloadProperties();
                }).setId("extradelight_stage_count"));
            }
            default -> { }
        }
    }

    @Override
    public void setExtraItem(ItemStack stack) {
        // 发酵缸会一次消耗多个容器，不能沿用通用额外槽位的单件归一化。
        if (extraItemSlots[0] != null) extraItemSlots[0].setItem(stack.copy(), false);
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        if (getData() instanceof ExtraDelightFeastRecipeData) {
            content.addChild(PropertiesView.createBlockConfigurator(key("feast_block"),
                    () -> getExtraItem().getItem() instanceof net.minecraft.world.item.BlockItem b ? b.getBlock() : net.minecraft.world.level.block.Blocks.AIR,
                    block -> setExtraItem(new ItemStack(block))));
        } else content.addChild(PropertiesView.createItemStackConfigurator(key("container"), this::getExtraItem, this::setExtraItem));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        if (getData() instanceof ExtraDelightToolOnBlockRecipeData && selectedSlotIndex() == 0) {
            content.addChild(PropertiesView.createBlockConfigurator(key("input_block"),
                    () -> getSelectedIngredient().toStack().getItem() instanceof net.minecraft.world.item.BlockItem b ? b.getBlock() : net.minecraft.world.level.block.Blocks.AIR,
                    block -> setSelectedIngredient(RecipeIngredient.item(new ItemStack(block)))));
        } else super.buildIngredientProperties(content);
        if (getData() instanceof ExtraDelightVatRecipeData d && selectedSlotIndex() >= 6) {
            var stage = d.getStages().get(selectedSlotIndex() - 6);
            content.addChildren(intField(key("time"), stage.getTime(), 1, Integer.MAX_VALUE, stage::setTime), switchField(key("lid"), stage.isLid(), stage::setLid));
        }
        String raw = selectedSlotIndex() < getData().getIngredientConditions().size() ? getData().getIngredientConditions().get(selectedSlotIndex()) : "";
        if (getData() instanceof ExtraDelightMixingBowlRecipeData d && selectedSlotIndex() == 9) raw = d.getUtensilCondition();
        if (getData() instanceof ExtraDelightVatRecipeData d && selectedSlotIndex() >= 6) raw = d.getStages().get(selectedSlotIndex() - 6).getIngredientCondition();
        if (!raw.isBlank()) content.addChild(RecipeEditorUi.label(Component.translatable(key("complex_ingredient"))));
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (getData() instanceof ExtraDelightToolOnBlockRecipeData) {
            content.addChild(PropertiesView.createBlockConfigurator(key("output_block"),
                    () -> getSelectedOutput().getItem().getItem() instanceof net.minecraft.world.item.BlockItem b ? b.getBlock() : net.minecraft.world.level.block.Blocks.AIR,
                    block -> setSelectedOutput(new ItemStack(block))));
        } else super.buildResultProperties(content);
        if (getData() instanceof ExtraDelightEvaporatorRecipeData)
            content.addChild(RecipeEditorUi.label(Component.translatable(key("loot_hint"))));
    }

    @Override
    public void buildFluidProperties(UIElement content) {
        int index = selectedSlotIndex();
        if (index >= 2 && index - 2 < fluidOutputSlots.length && fluidOutputSlots[index - 2] != null) { super.buildFluidProperties(content); return; }
        var ingredient = getVisualFluidInput(index);
        if (index < getData().getFluidConditions().size() && !getData().getFluidConditions().get(index).isBlank())
            content.addChild(RecipeEditorUi.label(Component.translatable(key("complex_fluid"))));
        boolean tags = !(getData() instanceof ExtraDelightChillerRecipeData);
        if (tags) content.addChild(selector(key("fluid_kind"), List.of(FluidIngredientKind.values()), ingredient.getKind(), FluidIngredientKind::displayName, this::setSelectedFluidIngredientKind));
        if (tags && ingredient.getKind() == FluidIngredientKind.TAG) content.addChild(PropertiesView.createFluidTagConfigurator(ingredient, tag -> setSelectedFluidInput(ingredient.setTag(tag.location()))));
        else content.addChild(PropertiesView.removeCountConfig(PropertiesView.createFluidStackConfigurator(key("fluid"), ingredient::getFluid, stack -> setSelectedFluidInput(ingredient.setFluid(stack)))));
        content.addChild(intField(key("amount"), ingredient.getAmount(), 1, Integer.MAX_VALUE, value -> setSelectedFluidInput(ingredient.setAmount(value))));
    }
}
