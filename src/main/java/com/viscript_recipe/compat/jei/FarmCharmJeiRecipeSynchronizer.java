package com.viscript_recipe.compat.jei;

import com.viscript_recipe.compat.farm_and_charm.FarmCharmRecipeKind;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.*;

/** Tracks native recipe identities because Farm & Charm's JEI categories contain raw recipes without holder IDs. */
final class FarmCharmJeiRecipeSynchronizer {
    private static final Map<ResourceLocation, Map<ResourceLocation, RecipeHolder<?>>> displayedByType = new HashMap<>();

    private FarmCharmJeiRecipeSynchronizer() {}

    static void reset() {
        displayedByType.clear();
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        for (var holder : level.getRecipeManager().getRecipes()) {
            var type = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
            if (FarmCharmRecipeKind.byType(type).isPresent()) {
                displayedByType.computeIfAbsent(type, unused -> new HashMap<>()).put(holder.id(), holder);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static int reconcile(IRecipeManager manager, RecipeType jeiType, ResourceLocation nativeType,
                         Set<ResourceLocation> affectedIds, Collection<RecipeHolder<?>> desiredRecipes) {
        var desired = new HashMap<ResourceLocation, RecipeHolder<?>>();
        var displayed = displayedByType.computeIfAbsent(nativeType, unused -> new HashMap<>());
        desiredRecipes.forEach(holder -> desired.put(holder.id(), holder));
        var stale = new ArrayList<Recipe<?>>();
        var additions = new ArrayList<Recipe<?>>();
        for (var id : affectedIds) {
            var old = displayed.get(id);
            var next = desired.get(id);
            if (old != null && next != null && old.value() == next.value()) continue;
            if (old != null) {
                stale.add(old.value());
                displayed.remove(id);
            }
            if (next != null) additions.add(next.value());
        }
        if (!stale.isEmpty()) manager.hideRecipes(jeiType, stale);
        if (!additions.isEmpty()) manager.addRecipes(jeiType, additions);
        displayed.putAll(desired);
        return stale.size();
    }
}
