package com.viscript_recipe.compat.farm_and_charm;

import net.minecraft.resources.ResourceLocation;
import java.util.Arrays;
import java.util.Optional;

/** Maps native Farm & Charm recipe types to their JEI categories and machine capacities. */
public enum FarmCharmRecipeKind {
    COOKING_POT("pot_cooking", "cooking_pot", "cooking_pot", 6),
    STOVE("stove", "stove", "stove", 3),
    CRAFTING_BOWL("crafting_bowl", "doughing", "crafting_bowl", 4),
    ROASTER("roaster", "roaster", "roaster", 6),
    SILO("drying", "drying", "silo_wood", 1),
    MINCER("mincer", "mincer", "mincer", 1);

    public static final String MOD_ID = "farm_and_charm";
    private final ResourceLocation typeId;
    private final ResourceLocation jeiTypeId;
    private final ResourceLocation workstationId;
    private final int inputCount;

    FarmCharmRecipeKind(String type, String jeiType, String workstation, int inputCount) {
        this.typeId = id(type);
        this.jeiTypeId = id(jeiType);
        this.workstationId = id(workstation);
        this.inputCount = inputCount;
    }

    public ResourceLocation typeId() { return typeId; }
    public ResourceLocation jeiTypeId() { return jeiTypeId; }
    public ResourceLocation workstationId() { return workstationId; }
    public int inputCount() { return inputCount; }
    public boolean hasContainer() { return this == COOKING_POT || this == ROASTER; }
    public boolean hasLearning() { return hasContainer() || this == STOVE; }
    public boolean hasProcessingCategory() { return this == MINCER || this == SILO; }
    public String translationKey() { return "viscript_recipe.editor.type.farm_and_charm." + typeId.getPath(); }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static Optional<FarmCharmRecipeKind> byType(ResourceLocation id) {
        return Arrays.stream(values()).filter(kind -> kind.typeId.equals(id)).findFirst();
    }
}
