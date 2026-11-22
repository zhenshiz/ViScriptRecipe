package com.viscript_recipe.compat.forbidden_arcanus.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoFireType;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueType;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.compat.forbidden_arcanus.data.ClibanoRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

import java.text.NumberFormat;
import java.util.List;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.id;

public final class ClibanoCanvas extends ForbiddenArcanusCanvas<ClibanoRecipeData> {
    private UIElement flame;
    private ItemSlot enhancer;
    private ItemSlot fireSource;
    private ItemStack[] fireSources;
    private Label residue;
    private Label timing;
    private Label experience;

    public ClibanoCanvas(NavigationView navigation, RecipeEntry entry) {super(navigation, entry);}

    @Override NativeIngredientData input(int index) {return index == 0 ? getData().getInput() : getData().getSecondInput();}

    @Override int inputCount() {return 2;}

    @Override
    public UIElement createCanvas() {
        setId("clibano_canvas");
        var panel = panel(200, 116);
        var first = ingredientSlot(0, SLOT_SIZE);
        var second = ingredientSlot(1, SLOT_SIZE);
        tooltip(first, key("first_input"));
        tooltip(second, key("second_input"));
        flame = new UIElement();
        enhancer = previewSlot(JEI_SLOT_SIZE);
        fireSource = previewSlot(JEI_SLOT_SIZE);
        fireSource.bindDataSource(SupplierDataSource.of(() -> fireSources == null || fireSources.length == 0 ? ItemStack.EMPTY
                : fireSources[(int) (System.currentTimeMillis() / 1000 % fireSources.length)].copy()));
        residue = RecipeEditorUi.label(Component.empty());
        timing = RecipeEditorUi.label(Component.empty());
        experience = RecipeEditorUi.label(Component.empty());
        return centered(panel.addChildren(
                at(enhancer.setId("clibano_enhancer"), 16, 36, 18, 18),
                at(first.setId("clibano_input_0"), 50, 36, 24, 24),
                at(second.setId("clibano_input_1"), 78, 36, 24, 24),
                at(flame.setId("clibano_fire"), 68, 64, 16, 20),
                at(fireSource.setId("clibano_fire_source"), 16, 66, 18, 18),
                at(arrow(), 115, 50, 19, 14),
                at(createOutputSlot(0, OUTPUT_SLOT_SIZE).setId("clibano_output"), 145, 38, 30, 30),
                at(experience, 116, 12, 74, 12),
                at(residue, 113, 73, 77, 12),
                at(timing, 20, 94, 159, 12)));
    }

    @Override
    public void load() {
        super.load();
        setVisualOutput(0, getData().getResult());
        refreshPreview();
    }

    @Override
    public void save() {
        super.save();
        getData().setResult(getVisualOutput(0).getItem());
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(
                selector(key("fire_type"), List.of(ClibanoFireType.values()), data.getFire(),
                        fire -> Component.translatable(key(fire.getSerializedName())), value -> data.setFire(value), this::refreshPreview),
                intField(key("cooking_time"), data.getCookingTime(), 1, Integer.MAX_VALUE,
                        value -> data.setCookingTime(value), this::refreshPreview),
                floatField(key("experience"), data.getExperience(), 0, Float.MAX_VALUE,
                        value -> data.setExperience(value), this::refreshPreview),
                registryField(key("enhancer"), FARegistries.ENHANCER_DEFINITION, data::getEnhancer, true, ForbiddenArcanusCanvas::enhancerName,
                        value -> {data.setEnhancer(value); refreshPreview();}),
                registryField(key("residue"), FARegistries.RESIDUE_TYPE, data::getResidue, true, holder -> residueName(holder.value()),
                        value -> {data.setResidue(value); refreshPreview();}),
                field(key("residue_chance"), RecipeEditorUi.doubleField(data.getResidueChance() * 100, 0, 100,
                        value -> {data.setResidueChance(value / 100); refreshPreview();})),
                textField(key("recipe_group"), data.getGroup(), data::setGroup),
                selector(key("recipe_book_category"), List.of(CookingBookCategory.values()), data.getCategory(),
                        value -> Component.translatable(key("category." + value.getSerializedName())), data::setCategory));
    }

    private void refreshPreview() {
        var data = getData();
        var fire = data.getFire();
        flame.style(style -> style.backgroundTexture(SpriteTexture.of(id("textures/gui/jei/clibano_combustion.png"))
                .setSprite(151 + 19 * fire.ordinal(), 1, 12, 15)));
        tooltip(flame, key(fire.getSerializedName()));
        var fireTag = fire.getTagKey();
        fireSources = fireTag == null ? null : Ingredient.of(fireTag).getItems();
        fireSource.setDisplay(fireTag != null);
        tooltip(fireSource, key("fire_type"));
        var enhancerHolder = readValue(EnhancerDefinition.REFERENCE_CODEC, data.getEnhancer());
        enhancer.setItem(enhancerHolder == null ? ItemStack.EMPTY : enhancerHolder.value().displayItem().value().getDefaultInstance(), false);
        tooltip(enhancer, key("enhancer"));
        var residueHolder = readValue(ResidueType.CODEC, data.getResidue());
        if (residueHolder == null) {
            residue.setText(Component.empty());
            tooltip(residue, new Component[0]);
        } else {
            var chance = NumberFormat.getPercentInstance().format(data.getResidueChance());
            residue.setText(Component.translatable(key("residue_summary"), chance));
            tooltip(residue, residueName(residueHolder.value()));
        }
        timing.setText(Component.translatable(key("time_summary"), data.getCookingTime()));
        experience.setText(Component.translatable(key("xp_summary"), data.getExperience()));
    }

    private static Component residueName(ResidueType residue) {
        var name = residue.name();
        if (name.getContents() instanceof TranslatableContents translation && name.getString().equals(translation.getKey())) {
            return residue.combineInfo().result().getHoverName();
        }
        return name;
    }
}
