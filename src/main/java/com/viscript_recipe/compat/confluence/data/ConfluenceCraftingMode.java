package com.viscript_recipe.compat.confluence.data;

import com.viscript_recipe.data.ITranslated;

public enum ConfluenceCraftingMode implements ITranslated {
    SHAPED,
    SHAPELESS;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.confluence.mode.";}
}
