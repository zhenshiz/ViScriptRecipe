package com.viscript_recipe.gui.editor;

public record WorkbenchSlotSelection(Kind kind, int index) {
    public static final WorkbenchSlotSelection RECIPE = new WorkbenchSlotSelection(Kind.RECIPE, -1);
    public static final WorkbenchSlotSelection RESULT = new WorkbenchSlotSelection(Kind.RESULT, -1);
    public static final WorkbenchSlotSelection CONTAINER = new WorkbenchSlotSelection(Kind.CONTAINER, -1);

    public static WorkbenchSlotSelection ingredient(int index) {
        return new WorkbenchSlotSelection(Kind.INGREDIENT, index);
    }

    public static WorkbenchSlotSelection fluid(int index) {
        return new WorkbenchSlotSelection(Kind.FLUID, index);
    }

    public static WorkbenchSlotSelection cuttingResult(int index) {
        return new WorkbenchSlotSelection(Kind.CUTTING_RESULT, index);
    }

    public static WorkbenchSlotSelection createResult(int index) {
        return new WorkbenchSlotSelection(Kind.CREATE_RESULT, index);
    }

    public static WorkbenchSlotSelection createTransitional() {
        return new WorkbenchSlotSelection(Kind.CREATE_TRANSITIONAL, -1);
    }

    public static WorkbenchSlotSelection createSequencedStep(int index) {
        return new WorkbenchSlotSelection(Kind.CREATE_SEQUENCED_STEP, index);
    }

    public static WorkbenchSlotSelection arsNouveauOutput(int index) {
        return new WorkbenchSlotSelection(Kind.ARS_NOUVEAU_OUTPUT, index);
    }

    public static WorkbenchSlotSelection industrialComponent(int index) {
        return new WorkbenchSlotSelection(Kind.INDUSTRIAL_COMPONENT, index);
    }

    public static WorkbenchSlotSelection mekanismChemical(int index) {
        return new WorkbenchSlotSelection(Kind.MEKANISM_CHEMICAL, index);
    }

    public static WorkbenchSlotSelection mekanismFluid(int index) {
        return new WorkbenchSlotSelection(Kind.MEKANISM_FLUID, index);
    }

    public static WorkbenchSlotSelection mekanismItem(int index) {
        return new WorkbenchSlotSelection(Kind.MEKANISM_ITEM, index);
    }

    public static WorkbenchSlotSelection mysticalEssence(int index) {
        return new WorkbenchSlotSelection(Kind.MYSTICAL_ESSENCE, index);
    }

    public static WorkbenchSlotSelection kaleidoscopeFluid() {
        return new WorkbenchSlotSelection(Kind.KALEIDOSCOPE_FLUID, -1);
    }

    public static WorkbenchSlotSelection kaleidoscopeSoupBase() {
        return new WorkbenchSlotSelection(Kind.KALEIDOSCOPE_SOUP_BASE, -1);
    }

    public static WorkbenchSlotSelection confluenceTarget(int index) {
        return new WorkbenchSlotSelection(Kind.CONFLUENCE_TARGET, index);
    }

    public enum Kind {
        RECIPE,
        INGREDIENT,
        FLUID,
        RESULT,
        CONTAINER,
        CUTTING_RESULT,
        CREATE_RESULT,
        CREATE_TRANSITIONAL,
        CREATE_SEQUENCED_STEP,
        ARS_NOUVEAU_OUTPUT,
        INDUSTRIAL_COMPONENT,
        MEKANISM_CHEMICAL,
        MEKANISM_FLUID,
        MEKANISM_ITEM,
        MYSTICAL_ESSENCE,
        KALEIDOSCOPE_FLUID,
        KALEIDOSCOPE_SOUP_BASE,
        CONFLUENCE_TARGET
    }
}
