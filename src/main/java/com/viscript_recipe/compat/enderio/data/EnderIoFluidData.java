package com.viscript_recipe.compat.enderio.data;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.JsonOps;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.fluids.crafting.*;

@Getter
@Setter
@Accessors(chain = true)
public class EnderIoFluidData implements ISkipDefaultedSerialize {
    @Persisted private FluidIngredientData value = FluidIngredientData.of();
    @Persisted private String customJson = "";

    public static EnderIoFluidData from(SizedFluidIngredient input, HolderLookup.Provider provider) {
        var data = new EnderIoFluidData();
        if (input.ingredient() instanceof TagFluidIngredient tag) data.value = FluidIngredientData.tag(tag.tag().location()).setAmount(input.amount());
        else if (input.ingredient() instanceof SingleFluidIngredient) data.value = FluidIngredientData.fluid(input.getFluids()[0]);
        else {
            data.customJson = SizedFluidIngredient.FLAT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), input).getOrThrow().toString();
            data.value = input.getFluids().length == 0 ? FluidIngredientData.empty() : FluidIngredientData.fluid(input.getFluids()[0]);
        }
        return data;
    }

    public SizedFluidIngredient compile() {
        if (!customJson.isBlank()) return SizedFluidIngredient.FLAT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()),
                JsonParser.parseString(customJson)).getOrThrow();
        if (value.isEmpty()) throw new IllegalArgumentException("流体原料不能为空");
        return value.getKind() == FluidIngredientKind.TAG
                ? SizedFluidIngredient.of(TagKey.create(Registries.FLUID, value.getTag()), value.getAmount())
                : SizedFluidIngredient.of(value.getFluid().copyWithAmount(value.getAmount()));
    }
}
