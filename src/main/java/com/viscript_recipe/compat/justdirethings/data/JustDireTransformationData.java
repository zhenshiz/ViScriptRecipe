package com.viscript_recipe.compat.justdirethings.data;

import com.direwolf20.justdirethings.datagen.recipes.FluidDropRecipe;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipe;
import com.direwolf20.justdirethings.datagen.recipes.GooSpreadRecipeTag;
import com.direwolf20.justdirethings.setup.Registration;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.BlockTagIngredient;

import static com.viscript_recipe.compat.justdirethings.JustDireRecipeEditorTypes.id;

/** 保存完整方块状态；流体槽只是预览，不将源方块改写为桶物品。 */
@Getter
@Setter
@Accessors(chain = true)
public final class JustDireTransformationData implements IVSRecipeData {
    // 原生配方内部的 id 与 RecipeHolder.id 可以不同，导入后原样保留。
    @Persisted private ResourceLocation nativeId = id("edited_transformation");
    @Persisted private BlockState input = Blocks.IRON_BLOCK.defaultBlockState();
    @Persisted private ResourceLocation inputTag = ResourceLocation.parse("c:storage_blocks/charcoal");
    @Persisted private BlockState output = Blocks.GOLD_BLOCK.defaultBlockState();
    @Persisted private Item catalyst = Items.COAL;
    @Persisted private int tier = 1;
    @Persisted private int duration = 1200;

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        output = Registration.RawFerricoreOre.get().defaultBlockState();
        if (typeId.equals(id("goospread_tag"))) {
            output = Registration.RawCoal_T1.get().defaultBlockState();
            duration = 2400;
        } else if (typeId.equals(id("fluiddrop"))) {
            input = Blocks.WATER.defaultBlockState();
            output = Registration.POLYMORPHIC_FLUID_BLOCK.get().defaultBlockState();
            catalyst = Registration.PolymorphicCatalyst.get();
        }
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        if (output == null || output.isAir()) throw new IllegalArgumentException("转化产物必须是非空气方块");
        if (typeId.equals(id("fluiddrop"))) {
            if (!(input.getBlock() instanceof LiquidBlock) || !input.getFluidState().isSource())
                throw new IllegalArgumentException("流体转化输入必须是流体源方块");
            if (catalyst == null || catalyst == Items.AIR) throw new IllegalArgumentException("流体转化需要催化物品");
            return new FluidDropRecipe(nativeId, input, output, catalyst);
        }
        if (tier < 1 || tier > 4 || duration < 1) throw new IllegalArgumentException("黏液等级必须为 1–4，转化时间必须大于零");
        if (typeId.equals(id("goospread_tag"))) {
            if (inputTag == null) throw new IllegalArgumentException("缺少输入方块标签");
            return new GooSpreadRecipeTag(nativeId, new BlockTagIngredient(TagKey.create(Registries.BLOCK, inputTag)), output, tier, duration);
        }
        if (!typeId.equals(id("goospread"))) throw new IllegalArgumentException("未知的转化类型：" + typeId);
        if (input == null || input.isAir()) throw new IllegalArgumentException("转化输入必须是非空气方块");
        return new GooSpreadRecipe(nativeId, input, output, tier, duration);
    }
}
