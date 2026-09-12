package com.viscript_recipe.compat.ae2.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ae2.data.Ae2TransformRecipeData;
import com.viscript_recipe.compat.ae2.data.Ae2IngredientData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import static com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory.*;

/** Edits world transformation recipes with a fluid tag or explosion condition and paged ingredients. */
public class TransformCanvas extends Ae2ItemRecipeCanvas<Ae2TransformRecipeData> {
    private static final int PAGE_SIZE = 9;
    private int page;
    private int slotCount;
    public TransformCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        slotCount = Math.min(PAGE_SIZE, Math.max(3, getData().getInputs().size() - page * PAGE_SIZE + 1));
        int extra = ((slotCount + 2) / 3 - 1) * 18;
        var panel = panel("ae2_transform", 130 + extra, 62);
        for (int i = 0; i < slotCount; i++) {
            panel.addChild(standardSlot(createIngredientSlot(i, 18), "ae2_input_" + i, 5 + i / 3 * 18, 5 + i % 3 * 18));
        }
        var data = getData();
        ItemStack environment = Items.TNT.getDefaultInstance();
        if (!data.isExplosion()) {
            environment = BuiltInRegistries.FLUID.getTag(TagKey.create(Registries.FLUID, data.getFluidTag()))
                    .flatMap(tag -> tag.stream().filter(fluid -> fluid.value().getBucket() != Items.AIR).findFirst())
                    .map(fluid -> fluid.value().getBucket().getDefaultInstance()).orElse(ItemStack.EMPTY);
        }
        var hint = createItemIcon(environment, 16).setId("ae2_circumstance");
        tooltip(hint, data.isExplosion() ? Component.translatable("viscript_recipe.editor.ae2.explosion")
                : Component.literal("#" + data.getFluidTag()));
        panel.addChildren(arrow(27 + extra, 23), at(hint, 56 + extra, 24, 16, 16), arrow(80 + extra, 23),
                standardSlot(createOutputSlot(0, 18), "ae2_output", 105 + extra, 23),
                label(Component.translatable("viscript_recipe.editor.ae2." + (data.isExplosion() ? "explosion" : "submerge")),
                        32 + extra, 2, 96));
        return centered(panel);
    }

    @Override
    public void load() {
        var inputs = getData().getInputs();
        for (int i = 0; i < slotCount; i++) {
            int index = page * PAGE_SIZE + i;
            loadAe2Ingredient(i, index < inputs.size() ? inputs.get(index) : new Ae2IngredientData());
        }
        setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        var inputs = new ArrayList<>(getData().getInputs());
        while (inputs.size() < page * PAGE_SIZE + slotCount) inputs.add(new Ae2IngredientData());
        for (int i = 0; i < slotCount; i++) inputs.set(page * PAGE_SIZE + i, saveAe2Ingredient(i));
        while (!inputs.isEmpty() && inputs.getLast().isEmpty()) inputs.removeLast();
        getData().setInputs(inputs).setResult(getVisualOutput(0).getItem());
    }

    private void rebuild() {
        clearAllChildren(); initVisualState(); load(); reloadProperties();
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.type.ae2.transform"),
                switchField("viscript_recipe.editor.ae2.explosion", data.isExplosion(), value -> {
                    save(); data.setExplosion(value); rebuild();
                }).setId("ae2_explosion"));
        if (!data.isExplosion()) content.addChild(PropertiesView.createFluidTagConfigurator(
                FluidIngredientData.of().setTag(data.getFluidTag()), tag -> {
                    save(); data.setFluidTag(tag.location()); rebuild();
                }));
        content.addChild(intField("viscript_recipe.editor.ae2.input_page", page + 1, 1,
                Math.max(page + 2, data.getInputs().size() / PAGE_SIZE + 2), value -> {
                    save(); page = value - 1; rebuild();
                }).setId("ae2_input_page"));
    }
}
