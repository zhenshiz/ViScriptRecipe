package com.viscript_recipe.compat.justdirethings.data;

import com.direwolf20.justdirethings.common.items.datacomponents.JustDireDataComponents;
import com.direwolf20.justdirethings.common.items.interfaces.Ability;
import com.direwolf20.justdirethings.common.items.interfaces.ToggleableTool;
import com.direwolf20.justdirethings.datagen.recipes.AbilityRecipe;
import com.direwolf20.justdirethings.datagen.recipes.PaxelRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import static com.viscript_recipe.compat.justdirethings.JustDireRecipeEditorTypes.id;

/** 使用原生锻造配方，保留能力安装、附魔合并及物品组件继承行为。 */
@Getter
@Setter
@Accessors(chain = true)
public final class JustDireSmithingData implements IVSRecipeData {
    @Persisted private JustDireIngredientData template = new JustDireIngredientData();
    @Persisted private JustDireIngredientData base = new JustDireIngredientData();
    @Persisted private JustDireIngredientData addition = new JustDireIngredientData();
    @Persisted private ItemStack result = ItemStack.EMPTY;

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        boolean paxel = typeId.equals(id("paxel"));
        base = new JustDireIngredientData().setValue(RecipeIngredient.item(
                BuiltInRegistries.ITEM.get(id(paxel ? "celestigem_axe" : "celestigem_pickaxe"))));
        addition = new JustDireIngredientData().setValue(RecipeIngredient.item(
                BuiltInRegistries.ITEM.get(id(paxel ? "celestigem_shovel" : "upgrade_hammer"))));
        if (paxel) {
            template.setValue(RecipeIngredient.item(BuiltInRegistries.ITEM.get(id("celestigem_pickaxe"))));
            result = BuiltInRegistries.ITEM.get(id("celestigem_paxel")).getDefaultInstance();
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var t = template.compile(); var b = base.compile(); var a = addition.compile();
        if (b.isEmpty() || a.isEmpty()) throw new IllegalArgumentException("锻造配方缺少基础物品或附加材料");
        if (typeId.equals(id("ability"))) {
            // 原生 JEI 预览会直接访问第一个候选和能力组件，提前校验以免无效配方使 JEI 崩溃。
            if (b.getItems().length == 0 || a.getItems().length == 0) throw new IllegalArgumentException("能力升级材料没有可用候选");
            for (var stack : b.getItems()) if (!(stack.getItem() instanceof ToggleableTool))
                throw new IllegalArgumentException("基础物品必须支持 Just Dire Things 能力升级");
            for (var stack : a.getItems()) {
                var ability = Ability.getAbilityFromUpgradeItem(stack.getItem());
                if (ability == null || !JustDireDataComponents.ABILITY_UPGRADE_INSTALLS.containsKey(ability))
                    throw new IllegalArgumentException("附加材料必须是有效的能力升级物品");
            }
            return new AbilityRecipe(t, b, a);
        }
        if (!typeId.equals(id("paxel"))) throw new IllegalArgumentException("未知的锻造类型：" + typeId);
        if (t.isEmpty() || result.isEmpty()) throw new IllegalArgumentException("多功能工具配方缺少镐或产物");
        if (result.getCount() < 1 || result.getCount() > result.getMaxStackSize()) throw new IllegalArgumentException("产物数量超过物品堆叠上限");
        return new PaxelRecipe(t, b, a, result.copy());
    }
}
