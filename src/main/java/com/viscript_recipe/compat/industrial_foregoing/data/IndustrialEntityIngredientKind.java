package com.viscript_recipe.compat.industrial_foregoing.data;

import com.viscript_recipe.data.ITranslated;

public enum IndustrialEntityIngredientKind implements ITranslated {
    ENTITY,
    TAG;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.industrial_foregoing.entity_kind.";}
}
