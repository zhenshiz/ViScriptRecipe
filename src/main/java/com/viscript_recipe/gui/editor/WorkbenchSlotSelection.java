package com.viscript_recipe.gui.editor;

public record WorkbenchSlotSelection(Kind kind, int index) {
    public static final WorkbenchSlotSelection RECIPE = new WorkbenchSlotSelection(Kind.RECIPE, -1);
    public static final WorkbenchSlotSelection RESULT = new WorkbenchSlotSelection(Kind.RESULT, -1);

    public static WorkbenchSlotSelection ingredient(int index) {
        return new WorkbenchSlotSelection(Kind.INGREDIENT, index);
    }

    public enum Kind {
        RECIPE,
        INGREDIENT,
        RESULT
    }
}
