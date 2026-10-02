package com.viscript_recipe.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureSoulExtractionRecipeData;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureWeightedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;

/** 预览由配方字段派生，仅供查看。 */
public final class MysticalAgricultureRecipeUiSupport {
    private MysticalAgricultureRecipeUiSupport() {
    }

    public static ItemStack soulJar(MysticalAgricultureSoulExtractionRecipeData data) {
        if (data == null || data.getSoulType() == null || !Double.isFinite(data.getSouls()) || data.getSouls() <= 0) {
            return ItemStack.EMPTY;
        }
        var type = MobSoulTypeRegistry.getInstance().getMobSoulTypeById(data.getSoulType());
        return type == null ? ItemStack.EMPTY : MobSoulUtils.getSoulJar(type, data.getSouls(), ModItems.SOUL_JAR.get());
    }

    public static ItemStack firstEnchantedBook(ResourceLocation enchantmentId) {
        var books = enchantedBooks(enchantmentId);
        return books.length == 0 ? ItemStack.EMPTY : books[0];
    }

    public static ItemStack[] enchantedBooks(ResourceLocation enchantmentId) {
        var enchantment = enchantment(enchantmentId);
        if (enchantment == null) {
            return new ItemStack[0];
        }
        var result = new ItemStack[enchantment.value().getMaxLevel()];
        for (int level = 1; level <= result.length; level++) {
            result[level - 1] = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
        }
        return result;
    }

    public static ItemStack firstSpawnEgg(List<MysticalAgricultureWeightedEntityData> entities) {
        var eggs = spawnEggs(entities);
        return eggs.length == 0 ? ItemStack.EMPTY : eggs[0];
    }

    public static ItemStack[] spawnEggs(List<MysticalAgricultureWeightedEntityData> entities) {
        var result = new ArrayList<ItemStack>();
        if (entities == null) {
            return new ItemStack[0];
        }
        for (var entityData : entities) {
            if (entityData == null || entityData.getEntity() == null) {
                continue;
            }
            var entity = BuiltInRegistries.ENTITY_TYPE.getOptional(entityData.getEntity()).orElse(null);
            var egg = entity == null ? null : SpawnEggItem.byId(entity);
            if (egg != null) {
                result.add(new ItemStack(egg));
            }
        }
        return result.toArray(ItemStack[]::new);
    }

    private static Holder<Enchantment> enchantment(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        var key = ResourceKey.create(Registries.ENCHANTMENT, id);
        var level = Minecraft.getInstance().level;
        var registryAccess = level == null ? Platform.getFrozenRegistry() : level.registryAccess();
        // 资源重载期间，附魔动态注册表可能尚未挂接到客户端世界。
        // 查找失败时返回空预览，避免编辑器崩溃。
        return registryAccess.lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(key))
                .orElse(null);
    }
}
