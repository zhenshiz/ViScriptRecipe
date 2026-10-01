package com.viscript_recipe.compat.eidolon.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.eidolon.data.EidolonDyeRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class DyeCanvas extends EidolonItemCanvas<EidolonDyeRecipeData> {
    public DyeCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() { super.load(); setVisualOutput(0, getData().getResult()); }

    @Override
    public void save() { super.save(); getData().setResult(getVisualOutput(0).getItem()); }

    @Override
    public UIElement createCanvas() {
        resize(getData().getInputs(), 9);
        var panel = EidolonCanvasFactory.page("jei_page_bg.png", 180, 132);
        panel.addChild(EidolonCanvasFactory.label(text("dye_hint"), 6, 8, 168));
        for (int i = 0; i < 9; i++) panel.addChild(EidolonCanvasFactory.slot(
                inputSlot(i, getData().getInputs().get(i), text("dye_input", i + 1)), 20 + i % 3 * 20, 42 + i / 3 * 20));
        var output = createOutputSlot(0, JEI_SLOT_SIZE); output.setId("eidolon_output");
        panel.addChild(EidolonCanvasFactory.slot(output, 136, 62));
        return EidolonCanvasFactory.centered(panel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.type.eidolon.dye"),
                textField("viscript_recipe.config.eidolon.group", data.getGroup(), data::setGroup),
                selector("viscript_recipe.config.eidolon.book_category", List.of(CraftingBookCategory.values()),
                        data.getCategory(), value -> Component.literal(value.getSerializedName()), data::setCategory));
    }
}
