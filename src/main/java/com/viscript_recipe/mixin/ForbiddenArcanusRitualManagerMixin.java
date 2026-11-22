package com.viscript_recipe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssencesDefinition;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssencesStorage;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.ActiveRitualData;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualManager;
import com.viscript_recipe.compat.forbidden_arcanus.ForgeRitualRecipes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Pseudo
@Mixin(targets = "com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.RitualManager", remap = false)
public abstract class ForbiddenArcanusRitualManagerMixin {
    @Unique private static final String VISCRIPT_RECIPE_RITUAL_ID = "viscript_recipe_ritual_id";
    @Shadow private ServerLevel level;
    @Shadow @Nullable private ActiveRitualData activeRitualData;
    @Shadow public abstract void updateValidRitual(EssencesDefinition definition, HolderLookup.Provider lookupProvider);

    @WrapOperation(method = "updateValidRitual", at = @At(value = "INVOKE",
            target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
    private List<? extends Holder<Ritual>> viscriptRecipe$availableRituals(
            Stream<Holder.Reference<Ritual>> originals, Operation<List<Holder.Reference<Ritual>>> original) {
        return level == null ? original.call(originals) : ForgeRitualRecipes.activeRituals(level.getRecipeManager(), originals);
    }

    @Inject(method = "startRitual", at = @At("HEAD"))
    private void viscriptRecipe$recheckRitual(ServerPlayer player, EssencesStorage storage, CallbackInfoReturnable<Boolean> cir) {
        if (level != null) updateValidRitual(storage.immutable(), level.registryAccess());
    }

    // 编辑后的仪式保留完整数据用于续跑，同时保留原 ID 供模组的仪式进度判定读取。
    @Inject(method = "setActiveRitual", at = @At("RETURN"))
    private void viscriptRecipe$rememberRitualId(@Nullable Holder<Ritual> ritual, UUID player, CallbackInfo ci) {
        if (activeRitualData != null && ritual != null && ritual.unwrapKey().isEmpty()) {
            ((ForgeRitualRecipes.ActiveId) activeRitualData)
                    .viscriptRecipe$setRitualId(ForgeRitualRecipes.activeId(level.getRecipeManager(), ritual));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void viscriptRecipe$saveRitualId(CompoundTag tag, HolderLookup.Provider lookup, CallbackInfoReturnable<CompoundTag> cir) {
        if (activeRitualData != null && cir.getReturnValue().contains(RitualManager.TAG_ACTIVE_RITUAL)) {
            var id = ((ForgeRitualRecipes.ActiveId) activeRitualData).viscriptRecipe$getRitualId();
            if (id != null) cir.getReturnValue().putString(VISCRIPT_RECIPE_RITUAL_ID, id.toString());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void viscriptRecipe$loadRitualId(CompoundTag tag, HolderLookup.Provider lookup, CallbackInfo ci) {
        if (activeRitualData != null && tag.contains(VISCRIPT_RECIPE_RITUAL_ID)) {
            ((ForgeRitualRecipes.ActiveId) activeRitualData)
                    .viscriptRecipe$setRitualId(ResourceLocation.tryParse(tag.getString(VISCRIPT_RECIPE_RITUAL_ID)));
        }
    }
}
