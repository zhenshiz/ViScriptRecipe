package com.viscript_recipe.recipe;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.Config;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.compat.irons_spellbooks.IronAlchemistCauldronFluidSupport;
import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public final class RecipeOverrideManager {
    private static final Object LOCK = new Object();
    @Nullable
    private static LinkedHashMap<ResourceLocation, RecipeHolder<?>> baseRecipes;
    private static ApplyResult lastResult = ApplyResult.empty();

    private RecipeOverrideManager() {
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider) {
        synchronized (LOCK) {
            baseRecipes = snapshot(recipeManager.getRecipes());
            return applyOverrides(recipeManager, provider, baseRecipes);
        }
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider) {
        synchronized (LOCK) {
            if (baseRecipes == null) {
                baseRecipes = snapshot(recipeManager.getRecipes());
            }
            return applyOverrides(recipeManager, provider, baseRecipes);
        }
    }

    public static ApplyResult getLastResult() {
        synchronized (LOCK) {
            return lastResult;
        }
    }

    private static ApplyResult applyOverrides(RecipeManager recipeManager, HolderLookup.Provider provider, LinkedHashMap<ResourceLocation, RecipeHolder<?>> base) {
        var loadedFiles = RecipeFileLoader.loadAll(provider);
        var showcaseOnly = Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        var recipes = showcaseOnly ? new LinkedHashMap<ResourceLocation, RecipeHolder<?>>() : new LinkedHashMap<>(base);
        var arcaneAnvilRecipes = new LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe>();
        var alchemistCauldronFluids = new ArrayList<FluidStack>();

        int entries = 0;
        int enabled = 0;
        int applied = 0;
        int skipped = 0;
        int failed = 0;
        for (var loaded : loadedFiles) {
            var file = loaded.file();
            if (file == null) {
                continue;
            }
            for (var entry : file.getEntries()) {
                entries++;
                if (!entry.isEnabled()) {
                    skipped++;
                    continue;
                }
                enabled++;
                var entryResult = applyEntry(loaded.relativePath(), entry, recipes, arcaneAnvilRecipes, showcaseOnly);
                if (entryResult == ApplyEntryResult.APPLIED) {
                    collectAlchemistCauldronRecipeFluids(entry, alchemistCauldronFluids);
                }
                switch (entryResult) {
                    case APPLIED -> applied++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
            }
        }

        IronArcaneAnvilOverrideManager.replaceAll(arcaneAnvilRecipes.values());
        IronAlchemistCauldronFluidSupport.replaceAll(alchemistCauldronFluids);
        recipeManager.replaceRecipes(recipes.values());
        lastResult = new ApplyResult(
                loadedFiles.size(),
                entries,
                enabled,
                applied,
                skipped,
                failed,
                base.size(),
                recipes.size()
        );
        ViScriptRecipe.LOGGER.info(
                "Reloaded ViScriptRecipe overrides: {} files, {} entries, {} enabled, {} applied, {} skipped, {} failed",
                lastResult.fileCount(),
                lastResult.entryCount(),
                lastResult.enabledEntryCount(),
                lastResult.appliedEntryCount(),
                lastResult.skippedEntryCount(),
                lastResult.failedEntryCount()
        );
        if (!arcaneAnvilRecipes.isEmpty()) {
            ViScriptRecipe.LOGGER.info("Loaded {} Iron's Spells Arcane Anvil override recipes", arcaneAnvilRecipes.size());
        }
        if (IronAlchemistCauldronFluidSupport.allowedFluidCount() > 0) {
            ViScriptRecipe.LOGGER.info(
                    "Allowed {} Iron's Spells Alchemist Cauldron fluids referenced by ViScriptRecipe recipes",
                    IronAlchemistCauldronFluidSupport.allowedFluidCount()
            );
        }
        if (showcaseOnly) {
            ViScriptRecipe.LOGGER.info(
                    "ViScriptRecipe showcase recipe mode is enabled: cleared {} base recipes before applying .recipe files",
                    base.size()
            );
        }
        return lastResult;
    }

    private static LinkedHashMap<ResourceLocation, RecipeHolder<?>> snapshot(Collection<RecipeHolder<?>> recipes) {
        var snapshot = new LinkedHashMap<ResourceLocation, RecipeHolder<?>>();
        for (var holder : recipes) {
            snapshot.put(holder.id(), holder);
        }
        return snapshot;
    }

    private static ApplyEntryResult applyEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes, boolean showcaseOnly) {
        if (entry.getRecipeId() == null) {
            ViScriptRecipe.LOGGER.warn("Skipping recipe entry with empty id in {}", source);
            return ApplyEntryResult.FAILED;
        }
        var id = entry.getRecipeId();
        try {
            if (IronArcaneAnvilOverrideManager.isArcaneAnvilEntry(entry)) {
                return applyArcaneAnvilEntry(source, entry, arcaneAnvilRecipes);
            }
            return switch (entry.getOperation()) {
                case REMOVE -> removeEntry(source, id, entry, recipes, showcaseOnly);
                case ADD, REPLACE -> upsertEntry(source, entry, recipes, showcaseOnly);
            };
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to apply recipe override {} from {}", id, source, e);
            return ApplyEntryResult.FAILED;
        }
    }

    private static ApplyEntryResult applyArcaneAnvilEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes) {
        if (!ViScriptRecipe.isModLoaded(IronSpellbooksRecipeEditorTypes.MOD_ID)) {
            ViScriptRecipe.LOGGER.warn("Skipping Iron's Spells Arcane Anvil override {} because irons_spellbooks is not loaded", entry.getRecipeId());
            return ApplyEntryResult.SKIPPED;
        }
        var id = entry.getRecipeId();
        return switch (entry.getOperation()) {
            case REMOVE -> arcaneAnvilRecipes.remove(id) == null ? ApplyEntryResult.SKIPPED : ApplyEntryResult.APPLIED;
            case ADD, REPLACE -> {
                var exists = arcaneAnvilRecipes.containsKey(id);
                if (entry.getOperation() == RecipeOperation.ADD && exists) {
                    ViScriptRecipe.LOGGER.warn("Arcane Anvil override {} adds existing recipe {}; replacing it", source, id);
                }
                arcaneAnvilRecipes.put(id, IronArcaneAnvilOverrideManager.compile(id, entry.getIronArcaneAnvil()));
                yield ApplyEntryResult.APPLIED;
            }
        };
    }

    private static void collectAlchemistCauldronRecipeFluids(RecipeEntry entry, Collection<FluidStack> fluids) {
        if (entry.getOperation() == RecipeOperation.REMOVE || !isAlchemistCauldronRecipe(entry)) {
            return;
        }
        var data = entry.getIronAlchemistCauldron();
        if (entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW)) {
            addFluid(fluids, data.getBaseFluid());
            if (data.getResultFluids() != null) {
                data.getResultFluids().forEach(stack -> addFluid(fluids, stack));
            }
            return;
        }
        addFluid(fluids, data.getFluid());
    }

    private static boolean isAlchemistCauldronRecipe(RecipeEntry entry) {
        return entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL)
                || entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY)
                || entry.isType(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW);
    }

    private static void addFluid(Collection<FluidStack> fluids, FluidStack stack) {
        if (stack != null && !stack.isEmpty()) {
            fluids.add(stack.copy());
        }
    }

    private static ApplyEntryResult removeEntry(String source, ResourceLocation id, RecipeEntry entry, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, boolean showcaseOnly) {
        var removed = false;
        for (var recipeId : removableRecipeIds(id, entry)) {
            removed |= recipes.remove(recipeId) != null;
        }
        if (!removed) {
            if (!showcaseOnly) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} tried to remove missing recipe {}", source, id);
            }
            return ApplyEntryResult.SKIPPED;
        }
        return ApplyEntryResult.APPLIED;
    }

    private static ApplyEntryResult upsertEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, boolean showcaseOnly) {
        var id = entry.getRecipeId();
        var holders = compileRecipeHolders(id, entry);
        if (holders.isEmpty()) {
            ViScriptRecipe.LOGGER.warn("Recipe override {} compiled no recipes for {}", source, id);
            return ApplyEntryResult.FAILED;
        }
        for (var holder : holders) {
            var exists = recipes.containsKey(holder.id());
            if (entry.getOperation() == RecipeOperation.ADD && exists) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} adds existing recipe {}; replacing it", source, holder.id());
            } else if (entry.getOperation() == RecipeOperation.REPLACE && !exists && !showcaseOnly) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} replaces missing recipe {}; adding it", source, holder.id());
            }
            recipes.put(holder.id(), holder);
        }
        return ApplyEntryResult.APPLIED;
    }

    private static List<RecipeHolder<?>> compileRecipeHolders(ResourceLocation id, RecipeEntry entry) {
        var compiled = compileEntryRecipes(entry);
        var holders = new ArrayList<RecipeHolder<?>>();
        for (int i = 0; i < compiled.size(); i++) {
            var recipeId = derivedRecipeId(id, i);
            holders.add(new RecipeHolder<>(recipeId, compiled.get(i)));
        }
        return holders;
    }

    private static List<net.minecraft.world.item.crafting.Recipe<?>> compileEntryRecipes(RecipeEntry entry) {
        var createKind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (createKind == CreateProcessingKind.BLOCK_CUTTING) {
            return CreateRecipeFactory.compileProcessingRecipes(entry.getType(), entry.getCreateProcessing());
        }
        return List.of(entry.compile());
    }

    private static List<ResourceLocation> removableRecipeIds(ResourceLocation id, RecipeEntry entry) {
        var createKind = CreateProcessingKind.byType(entry.getType()).orElse(null);
        if (createKind != CreateProcessingKind.BLOCK_CUTTING) {
            return List.of(id);
        }
        var ids = new ArrayList<ResourceLocation>();
        for (int i = 0; i < createKind.maxItemOutputs(); i++) {
            ids.add(derivedRecipeId(id, i));
        }
        return ids;
    }

    private static ResourceLocation derivedRecipeId(ResourceLocation baseId, int index) {
        if (index <= 0) {
            return baseId;
        }
        return ResourceLocation.fromNamespaceAndPath(baseId.getNamespace(), baseId.getPath() + "_output_" + (index + 1));
    }

    private enum ApplyEntryResult {
        APPLIED,
        SKIPPED,
        FAILED
    }

    public record ApplyResult(
            int fileCount,
            int entryCount,
            int enabledEntryCount,
            int appliedEntryCount,
            int skippedEntryCount,
            int failedEntryCount,
            int baseRecipeCount,
            int resultRecipeCount
    ) {
        public static ApplyResult empty() {
            return new ApplyResult(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
