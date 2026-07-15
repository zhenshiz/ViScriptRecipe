package com.viscript_recipe.compat.industrial_foregoing;

import com.buuz135.industrial.recipe.DissolutionChamberRecipe;
import com.buuz135.industrial.recipe.FluidExtractorRecipe;
import com.buuz135.industrial.recipe.LaserDrillFluidRecipe;
import com.buuz135.industrial.recipe.LaserDrillOreRecipe;
import com.buuz135.industrial.recipe.LaserDrillRarity;
import com.buuz135.industrial.recipe.CrusherRecipe;
import com.buuz135.industrial.recipe.StoneWorkGenerateRecipe;
import com.buuz135.industrial.recipe.data.EntityData;
import com.buuz135.industrial.recipe.data.EntityIngredient;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.industrial_foregoing.IndustrialBlockStatePropertyData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialCrusherRecipeData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialDissolutionRecipeData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialEntityConditionData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialEntityIngredientKind;
import com.viscript_recipe.data.industrial_foregoing.IndustrialFluidExtractorRecipeData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialFluidIngredientData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialFluidIngredientKind;
import com.viscript_recipe.data.industrial_foregoing.IndustrialLaserDrillFluidRecipeData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialLaserDrillOreRecipeData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialLaserDrillRarityData;
import com.viscript_recipe.data.industrial_foregoing.IndustrialStoneWorkRecipeData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Converts editor-owned data into Industrial Foregoing's native codec-backed recipe classes. */
public final class IndustrialForegoingRecipeFactory {
    private IndustrialForegoingRecipeFactory() {
    }

    /** Compiles a material stonework crushing recipe. */
    public static CrusherRecipe compileCrusher(IndustrialCrusherRecipeData data) {
        return new CrusherRecipe(
                requireIngredient(data.getInput(), "Crusher input"),
                requireIngredient(data.getOutput(), "Crusher output")
        );
    }

    /** Compiles a dissolution chamber recipe. */
    public static DissolutionChamberRecipe compileDissolution(IndustrialDissolutionRecipeData data) {
        var inputs = new ArrayList<Ingredient>();
        for (var input : safeList(data.getInput())) {
            if (input != null && !input.getValues().isEmpty()) {
                inputs.add(requireIngredient(input, "Dissolution chamber input"));
            }
        }
        if (inputs.size() > IndustrialDissolutionRecipeData.MAX_INPUTS) {
            throw new IllegalArgumentException("Dissolution chamber recipes support at most eight item inputs");
        }
        var output = data.isHasItemOutput() ? Optional.of(requireOutput(data.getOutput(), "Dissolution chamber item output")) : Optional.<ItemStack>empty();
        var outputFluid = data.isHasFluidOutput() ? Optional.of(requireFluid(data.getOutputFluid(), "Dissolution chamber fluid output")) : Optional.<FluidStack>empty();
        if (output.isEmpty() && outputFluid.isEmpty()) {
            throw new IllegalArgumentException("Dissolution chamber recipe must have an item or fluid output");
        }
        return new DissolutionChamberRecipe(inputs, compileFluidIngredient(data.getInputFluid()),
                Math.max(0, data.getProcessingTime()), output, outputFluid);
    }

    /** Compiles a fluid extractor recipe, validating every stored block-state property. */
    public static FluidExtractorRecipe compileFluidExtractor(IndustrialFluidExtractorRecipeData data) {
        return new FluidExtractorRecipe(
                requireIngredient(data.getInput(), "Fluid extractor input"),
                compileBlockState(data.getResultBlock(), data.getResultProperties()),
                data.getBreakChance(),
                requireFluid(data.getOutput(), "Fluid extractor output"),
                data.isDefaultRecipe()
        );
    }

    /** Compiles an ore laser drill recipe. */
    public static LaserDrillOreRecipe compileLaserOre(IndustrialLaserDrillOreRecipeData data) {
        return new LaserDrillOreRecipe(
                new SizedIngredient(requireIngredient(data.getOutput(), "Laser drill ore output"), Math.max(1, data.getOutputCount())),
                requireIngredient(data.getCatalyst(), "Laser drill catalyst"),
                compileEntityCondition(data.getEntityCondition()),
                compileRarities(data.getRarity())
        );
    }

    /** Compiles a fluid laser drill recipe. */
    public static LaserDrillFluidRecipe compileLaserFluid(IndustrialLaserDrillFluidRecipeData data) {
        return new LaserDrillFluidRecipe(
                compileFluidIngredient(data.getOutput()),
                requireIngredient(data.getCatalyst(), "Laser drill catalyst"),
                compileEntityCondition(data.getEntityCondition()),
                compileRarities(data.getRarity())
        );
    }

    /** Compiles a material stonework generation recipe. */
    public static StoneWorkGenerateRecipe compileStoneWork(IndustrialStoneWorkRecipeData data) {
        return new StoneWorkGenerateRecipe(
                requireOutput(data.getOutput(), "Stonework output"),
                Math.max(0, data.getWaterNeed()),
                Math.max(0, data.getLavaNeed()),
                Math.max(0, data.getWaterConsume()),
                Math.max(0, data.getLavaConsume())
        );
    }

    private static SizedFluidIngredient compileFluidIngredient(IndustrialFluidIngredientData data) {
        if (data == null) {
            throw new IllegalArgumentException("Fluid ingredient cannot be empty");
        }
        var amount = Math.max(1, data.getAmount());
        if (data.getKind() == IndustrialFluidIngredientKind.TAG) {
            if (data.getTag() == null) {
                throw new IllegalArgumentException("Fluid ingredient tag cannot be empty");
            }
            return SizedFluidIngredient.of(TagKey.create(Registries.FLUID, data.getTag()), amount);
        }
        var stack = requireFluid(data.getFluid(), "Fluid ingredient");
        return SizedFluidIngredient.of(stack.copyWithAmount(amount));
    }

    private static Optional<EntityData> compileEntityCondition(IndustrialEntityConditionData data) {
        if (data == null || !data.isEnabled()) {
            return Optional.empty();
        }
        if (data.getId() == null) {
            throw new IllegalArgumentException("Laser drill entity condition identifier cannot be empty");
        }
        EntityIngredient ingredient;
        if (data.getKind() == IndustrialEntityIngredientKind.TAG) {
            ingredient = EntityIngredient.of(TagKey.create(Registries.ENTITY_TYPE, data.getId()));
        } else {
            var entity = BuiltInRegistries.ENTITY_TYPE.getOptional(data.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown entity type: " + data.getId()));
            ingredient = EntityIngredient.of(entity);
        }
        try {
            CompoundTag nbt = TagParser.parseTag(data.getNbt() == null || data.getNbt().isBlank() ? "{}" : data.getNbt());
            var builder = EntityData.builder();
            for (var key : nbt.getAllKeys()) {
                var tag = nbt.get(key);
                if (tag != null) {
                    builder.putTag(key, ignored -> tag.copy());
                }
            }
            return Optional.of(builder.build(Component.literal(data.getDisplay() == null ? "" : data.getDisplay()), ingredient));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid entity condition SNBT: " + data.getNbt(), exception);
        }
    }

    private static List<LaserDrillRarity> compileRarities(List<IndustrialLaserDrillRarityData> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Laser drill recipe must contain at least one rarity rule");
        }
        return values.stream().map(value -> {
            if (value == null) {
                throw new IllegalArgumentException("Laser drill rarity rule cannot be empty");
            }
            if (value.getDepthMin() > value.getDepthMax()) {
                throw new IllegalArgumentException("Laser drill minimum depth cannot exceed maximum depth");
            }
            return new LaserDrillRarity(
                    new LaserDrillRarity.BiomeRarity(biomeTags(value.getBiomeWhitelist()), biomeTags(value.getBiomeBlacklist())),
                    new LaserDrillRarity.DimensionRarity(dimensionTypes(value.getDimensionWhitelist()), dimensionTypes(value.getDimensionBlacklist())),
                    value.getDepthMin(), value.getDepthMax(), Math.max(1, value.getWeight())
            );
        }).toList();
    }

    private static List<TagKey<net.minecraft.world.level.biome.Biome>> biomeTags(List<ResourceLocation> ids) {
        return safeList(ids).stream().filter(java.util.Objects::nonNull)
                .map(id -> TagKey.create(Registries.BIOME, id)).toList();
    }

    private static List<ResourceKey<net.minecraft.world.level.dimension.DimensionType>> dimensionTypes(List<ResourceLocation> ids) {
        return safeList(ids).stream().filter(java.util.Objects::nonNull)
                .map(id -> ResourceKey.create(Registries.DIMENSION_TYPE, id)).toList();
    }

    private static BlockState compileBlockState(ResourceLocation blockId, List<IndustrialBlockStatePropertyData> properties) {
        var block = blockId == null ? null : BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
        if (block == null) {
            throw new IllegalArgumentException("Unknown fluid extractor result block: " + blockId);
        }
        var state = block.defaultBlockState();
        for (var entry : safeList(properties)) {
            if (entry == null || entry.getName() == null || entry.getName().isBlank()) {
                continue;
            }
            var property = block.getStateDefinition().getProperty(entry.getName());
            if (property == null) {
                throw new IllegalArgumentException("Unknown block-state property " + entry.getName() + " for " + blockId);
            }
            state = setProperty(state, property, entry.getValue(), blockId);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> property,
                                                                     String value, ResourceLocation blockId) {
        var parsed = property.getValue(value == null ? "" : value)
                .orElseThrow(() -> new IllegalArgumentException("Invalid value " + value + " for " + blockId + " property " + property.getName()));
        return state.setValue(property, parsed);
    }

    private static Ingredient requireIngredient(RecipeIngredient data, String label) {
        var ingredient = data == null ? Ingredient.EMPTY : data.compile();
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
        return ingredient;
    }

    private static ItemStack requireOutput(ItemStack stack, String label) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
        return stack.copy();
    }

    private static FluidStack requireFluid(FluidStack stack, String label) {
        if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
        return stack.copy();
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
