package com.viscript_recipe.compat.confluence.data;

import com.viscript_recipe.data.ITranslated;

public enum ConfluenceGamePhase implements ITranslated {
    BEFORE_SKELETRON,
    AFTER_SKELETRON,
    WALL_OF_FLESH,
    MECHANICAL_BOSSES,
    PLANTERA,
    GOLEM,
    MOON_LORD;

    @Override
    public String translatePrefix() {return "viscript_recipe.editor.confluence.phase.";}
}
