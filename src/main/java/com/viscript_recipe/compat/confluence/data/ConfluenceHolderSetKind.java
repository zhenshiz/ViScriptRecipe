package com.viscript_recipe.compat.confluence.data;

import com.viscript_recipe.data.ITranslated;

public enum ConfluenceHolderSetKind implements ITranslated {
    NONE,
    IDS,
    TAG;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.confluence.holder_kind.";}
}
