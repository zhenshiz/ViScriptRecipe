package com.viscript_recipe.compat.farm_and_charm;

import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.satisfy.farm_and_charm.core.recipe.*;

/** Validates machine capacities and creates native Farm & Charm recipes through public constructors. */
public final class FarmCharmRecipeFactory {
    private FarmCharmRecipeFactory() {}

    /**
     * Creates a native recipe, omitting empty editor cells while retaining repeated ingredients.
     *
     * <p>Cooking pots and roasters require a nonempty container stack in the native serialized format,
     * including cooking pot recipes whose container requirement is disabled.
     *
     * @param typeId the identifier of one of the six supported native recipe types
     * @param data the editor data to compile without mutation
     * @return the native recipe with copied output and container stacks
     * @throws IllegalArgumentException if the type is unknown, inputs exceed the machine capacity,
     *         required inputs or stacks are empty, or stove experience is not finite
     */
    public static Recipe<?> compile(ResourceLocation typeId, FarmCharmRecipeData data) {
        var kind = FarmCharmRecipeKind.byType(typeId).orElseThrow(() -> new IllegalArgumentException("Unknown Farm & Charm type: " + typeId));
        var inputs = NonNullList.<Ingredient>create();
        for (var input : data.getInputs()) if (!input.isEmpty()) inputs.add(input.compile());
        if (inputs.isEmpty() || inputs.size() > kind.inputCount()) {
            throw new IllegalArgumentException("Farm & Charm " + typeId + " requires 1–" + kind.inputCount() + " ingredients");
        }
        var result = required(data.getResult(), "output");
        return switch (kind) {
            case COOKING_POT -> new CookingPotRecipe(inputs, data.isContainerRequired(), required(data.getContainer(), "container"), result, data.isRequiresLearning());
            case ROASTER -> new RoasterRecipe(inputs, required(data.getContainer(), "container"), result, data.isRequiresLearning());
            case STOVE -> {
                if (!Float.isFinite(data.getExperience())) throw new IllegalArgumentException("Stove experience must be finite");
                yield new StoveRecipe(inputs, result, data.getExperience(), data.isRequiresLearning());
            }
            case CRAFTING_BOWL -> new CraftingBowlRecipe(inputs, result);
            case MINCER -> new MincerRecipe(data.getProcessingCategory(), inputs.getFirst(), result);
            case SILO -> new SiloRecipe(data.getProcessingCategory(), inputs.getFirst(), result);
        };
    }

    private static ItemStack required(ItemStack stack, String name) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Farm & Charm " + name + " cannot be empty");
        return stack.copy();
    }
}
