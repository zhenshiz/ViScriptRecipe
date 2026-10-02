package com.viscript_recipe.compat.farm_and_charm;

import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.satisfy.farm_and_charm.core.recipe.*;

public final class FarmCharmRecipeFactory {
    private FarmCharmRecipeFactory() {}

    /**
     * 忽略空槽位并保留重复原料，不修改传入数据。
     *
     * <p>烹饪锅和烤炉的原生序列化格式要求容器物品堆非空，即使烹饪锅已关闭容器需求。
     *
     * @param typeId 支持的原生配方类型标识
     * @param data 待编译的编辑器数据
     * @return 使用产物和容器物品堆副本创建的原生配方
     * @throws IllegalArgumentException 类型未知、原料超出机器容量、必需原料或物品堆为空，或炉灶经验值非有限数
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
