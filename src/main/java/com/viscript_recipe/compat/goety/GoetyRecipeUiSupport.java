package com.viscript_recipe.compat.goety;

import com.Polarice3.Goety.api.ritual.RitualType;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.items.research.ResearchScroll;
import com.Polarice3.Goety.common.research.ResearchList;
import com.Polarice3.Goety.utils.BrewUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/** 隔离 Goety 原生类的加载，仅在该模组存在时生成只读 JEI 预览。 */
public final class GoetyRecipeUiSupport {
    private GoetyRecipeUiSupport() {
    }

    /** 返回原生 JEI 仪式图标；类型未知或缺少图标时返回空物品堆。 */
    public static ItemStack ritualTypeIcon(String craftType) {
        var type = RitualType.getRitualType(craftType);
        return type == null || type.getJeiIcon() == null ? ItemStack.EMPTY : type.getJeiIcon().copy();
    }

    /** 返回研究对应的卷轴；未注册对应卷轴时返回空物品堆。 */
    public static ItemStack researchScroll(String researchName) {
        var research = ResearchList.getResearch(researchName == null ? "" : researchName);
        if (research == null) {
            return ItemStack.EMPTY;
        }
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof ResearchScroll scroll && scroll.research == research) {
                return new ItemStack(scroll);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 按 Goety 的 JEI 规则生成带效果的酿造预览。
     *
     * @param effectId 生物效果的注册标识
     * @param duration 效果持续时间，单位为游戏刻
     * @return 酿造物品预览
     */
    public static ItemStack brewPreview(ResourceLocation effectId, int duration) {
        var stack = new ItemStack(ModItems.BREW.get());
        if (effectId == null) {
            return stack;
        }
        var effect = BuiltInRegistries.MOB_EFFECT.getOptional(effectId).orElse(null);
        if (effect == null) {
            return stack;
        }
        var effects = List.of(new MobEffectInstance(
                BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect),
                Math.max(1, duration)
        ));
        BrewUtils.setCustomEffects(stack, effects, List.of());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(
                "CustomPotionColor",
                BrewUtils.getColor(effects, List.of())
        ));
        return stack;
    }
}
