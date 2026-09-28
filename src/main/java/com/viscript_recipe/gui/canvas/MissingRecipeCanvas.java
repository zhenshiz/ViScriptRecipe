package com.viscript_recipe.gui.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.data.MissingRecipeTypeHolder;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.views.NavigationView;

public class MissingRecipeCanvas extends RecipeCanvas<MissingRecipeTypeHolder> {
    public MissingRecipeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override public void load() {}
    @Override public void save() {}

    @Override
    public UIElement createCanvas() {
        return emptyLabel().setText("viscript_recipe.editor.category.unknown.canvas");
    }
}
