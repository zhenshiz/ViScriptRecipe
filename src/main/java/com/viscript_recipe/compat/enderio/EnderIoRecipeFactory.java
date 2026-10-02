package com.viscript_recipe.compat.enderio;

import com.enderio.enderio.content.enchanter.EnchanterRecipe;
import com.enderio.enderio.content.fire_crafting.FireCraftingRecipe;
import com.enderio.enderio.content.machines.alloy.AlloySmeltingRecipe;
import com.enderio.enderio.content.machines.obelisks.weather.WeatherChangeRecipe;
import com.enderio.enderio.content.machines.sag_mill.SagMillingRecipe;
import com.enderio.enderio.content.machines.slicer.SlicingRecipe;
import com.enderio.enderio.content.machines.soul_binder.SoulBindingRecipe;
import com.enderio.enderio.content.machines.vat.FermentingRecipe;
import com.enderio.enderio.content.storage.fluid_tank.TankRecipe;
import com.enderio.enderio.foundation.soul.ShapedEntityStorageRecipe;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.datafixers.util.Either;
import com.viscript_recipe.compat.enderio.data.*;
import com.viscript_recipe.data.IngredientValueKind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

public final class EnderIoRecipeFactory {
    private EnderIoRecipeFactory() {}

    public static Recipe<?> compile(String type, EnderIoRecipeData data) {
        require(data.getEnergy() >= 0, "能量不能为负数");
        return switch (type) {
            case "alloy_smelting" -> {
                require(data.getInputs().size() >= 1 && data.getInputs().size() <= 3, "合金冶炼需要 1–3 个原料");
                require(Float.isFinite(data.getExperience()) && data.getExperience() >= 0, "经验必须为非负有限数");
                yield new AlloySmeltingRecipe(data.getInputs().stream().map(EnderIoRecipeFactory::sized).toList(),
                        result(data, 0), data.getEnergy(), data.getExperience(), data.isSmelting());
            }
            case "sag_milling" -> {
                require(!data.getOutputs().isEmpty() && data.getOutputs().size() <= 4, "SAG 磨粉需要 1–4 个产物");
                var outputs = new ArrayList<SagMillingRecipe.OutputItem>();
                for (var output : data.getOutputs()) {
                    chance(output.getChance());
                    require(output.getCount() > 0, "产物数量必须为正数");
                    Either<ItemStack, SagMillingRecipe.OutputItem.SizedTagOutput> value = output.isUseTag()
                            ? Either.right(new SagMillingRecipe.OutputItem.SizedTagOutput(TagKey.create(Registries.ITEM, output.getTag()), output.getCount()))
                            : Either.left(result(output.getItem()));
                    outputs.add(new SagMillingRecipe.OutputItem(value, output.getChance(), output.isOptional()));
                }
                yield new SagMillingRecipe(input(data, 0), outputs, data.getEnergy(), named(SagMillingRecipe.BonusType.values(), data.getBonus()));
            }
            case "slicing" -> {
                require(data.getInputs().size() == 6, "切片拼接必须保留六个槽位");
                var inputs = data.getInputs().stream().map(EnderIoIngredientData::compile).toList();
                require(inputs.stream().anyMatch(i -> !i.isEmpty()), "原料不能为空");
                yield new SlicingRecipe(result(data, 0), inputs, data.getEnergy());
            }
            case "soul_binding" -> {
                var entity = optionalId(data.getEntityType());
                var category = data.getMobCategory().isBlank() ? Optional.<MobCategory>empty()
                        : Optional.of(named(MobCategory.values(), data.getMobCategory()));
                var soul = data.getSoulData().isBlank() ? Optional.<String>empty() : Optional.of(data.getSoulData());
                require((entity.isPresent() ? 1 : 0) + (category.isPresent() ? 1 : 0) + (soul.isPresent() ? 1 : 0) <= 1, "灵魂条件只能选择一种");
                soul.ifPresent(name -> require(com.enderio.enderio.foundation.souldata.SoulDataReloadListener.fromString(name) != null, "未知灵魂数据集：" + name));
                entity.ifPresent(id -> require(BuiltInRegistries.ENTITY_TYPE.containsKey(id), "未知实体：" + id));
                require(data.getExperienceLevels() >= 0, "经验等级不能为负数");
                yield new SoulBindingRecipe(result(data, 0), input(data, 0), data.getEnergy(), data.getExperienceLevels(),
                        entity, category, soul, data.isCopyInputComponents());
            }
            case "tank" -> new TankRecipe(input(data, 0), result(data, 0), data.getFluidInput().compile(), named(TankRecipe.Mode.values(), data.getTankMode()));
            case "vat_fermenting" -> {
                require(data.getTicks() > 0, "发酵时间必须为正数");
                yield new FermentingRecipe(data.getFluidInput().compile(), reagent(data, 0), reagent(data, 1), fluid(data.getFluidOutput()), data.getTicks());
            }
            case "weather_change" -> new WeatherChangeRecipe(fluid(data.getFluidOutput()), named(WeatherChangeRecipe.WeatherMode.values(), data.getWeather()));
            case "enchanting" -> {
                require(data.getCostMultiplier() > 0, "附魔费用倍率必须为正数");
                var enchantment = Platform.getFrozenRegistry().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, data.getEnchantment()));
                yield new EnchanterRecipe(enchantment, data.getCostMultiplier(), sized(data.getInputs().getFirst()));
            }
            case "fire_crafting" -> {
                require(!data.getBases().isEmpty() || !data.getBaseTags().isEmpty(), "火焰合成需要基底方块或标签");
                require(!data.getDimensions().isEmpty(), "火焰合成需要允许的维度");
                require(!data.getOutputs().isEmpty() && data.getOutputs().size() <= 16, "火焰合成需要 1–16 个产物");
                var outputs = new ArrayList<FireCraftingRecipe.Result>();
                for (var output : data.getOutputs()) {
                    chance(output.getChance());
                    require(output.getMinCount() >= 0 && output.getMaxCount() >= output.getMinCount(), "掉落数量范围无效");
                    outputs.add(new FireCraftingRecipe.Result(result(output.getItem()), output.getMinCount(), output.getMaxCount(), output.getChance()));
                }
                yield new FireCraftingRecipe(outputs, data.getBases().stream().map(EnderIoRecipeFactory::block).toList(),
                        data.getBaseTags().stream().map(id -> TagKey.create(Registries.BLOCK, id)).toList(),
                        data.getDimensions().stream().map(id -> ResourceKey.create(Registries.DIMENSION, id)).toList(),
                        optionalId(data.getBlockAfterBurning()).map(EnderIoRecipeFactory::block));
            }
            case "shaped_entity_storage" -> {
                require(data.getWidth() >= 1 && data.getWidth() <= 3 && data.getHeight() >= 1 && data.getHeight() <= 3, "合成网格必须为 1–3 行、1–3 列");
                require(data.getInputs().size() == 9, "合成网格必须保留九个槽位");
                var keys = new LinkedHashMap<Character, Ingredient>();
                var rows = new ArrayList<String>();
                for (int y = 0; y < data.getHeight(); y++) {
                    var row = new StringBuilder();
                    for (int x = 0; x < data.getWidth(); x++) {
                        int index = y * 3 + x;
                        var ingredient = data.getInputs().get(index).compile();
                        char key = (char) ('A' + index);
                        if (ingredient.isEmpty()) row.append(' ');
                        else { keys.put(key, ingredient); row.append(key); }
                    }
                    rows.add(row.toString());
                }
                require(!keys.isEmpty(), "合成原料不能为空");
                yield new ShapedEntityStorageRecipe(new ShapedRecipe(data.getGroup(), data.getCategory(),
                        ShapedRecipePattern.of(keys, rows), result(data, 0), !Boolean.FALSE.equals(data.getShowNotification())));
            }
            default -> throw new IllegalArgumentException("未知 Ender IO 配方：" + type);
        };
    }

    private static Ingredient input(EnderIoRecipeData data, int index) {
        var value = data.getInputs().get(index).compile();
        require(!value.isEmpty(), "原料不能为空");
        return value;
    }

    private static SizedIngredient sized(EnderIoIngredientData data) {
        require(data.getCount() > 0 && !data.compile().isEmpty(), "原料及其数量不能为空");
        return new SizedIngredient(data.compile(), data.getCount());
    }

    private static TagKey<Item> reagent(EnderIoRecipeData data, int index) {
        var input = data.getInputs().get(index);
        require(input.getCustomJson().isBlank() && input.getAlternatives().size() == 1
                && input.getAlternatives().getFirst().getKind() == IngredientValueKind.TAG, "发酵槽试剂必须为单个物品标签");
        return TagKey.create(Registries.ITEM, input.getAlternatives().getFirst().getTag());
    }

    private static ItemStack result(EnderIoRecipeData data, int index) { return result(data.getOutputs().get(index).getItem()); }
    private static ItemStack result(ItemStack stack) { require(!stack.isEmpty(), "产物不能为空"); return stack.copy(); }
    private static FluidStack fluid(FluidStack stack) { require(!stack.isEmpty(), "流体不能为空"); return stack.copy(); }
    private static Block block(ResourceLocation id) { require(BuiltInRegistries.BLOCK.containsKey(id), "未知方块：" + id); return BuiltInRegistries.BLOCK.get(id); }
    private static Optional<ResourceLocation> optionalId(String text) { return text.isBlank() ? Optional.empty() : Optional.of(ResourceLocation.parse(text)); }
    private static void chance(float value) { require(Float.isFinite(value) && value >= 0 && value <= 1, "概率必须在 0–1 之间"); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalArgumentException(message); }
    private static <T extends Enum<T> & StringRepresentable> T named(T[] values, String name) {
        return Arrays.stream(values).filter(v -> v.getSerializedName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知选项：" + name));
    }
}
