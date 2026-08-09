package com.viscript_recipe.compat.confluence;

import com.mojang.datafixers.util.Either;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.confluence.*;
import com.viscript_recipe.recipe.importer.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import org.confluence.mod.common.recipe.*;

import java.lang.reflect.Field;
import java.util.*;

/** Imports Confluence RecipeManager recipes, including Magic Lib's counted ingredients. */
public final class ConfluenceRecipeImporter implements RecipeImportHandler {
    public static final ConfluenceRecipeImporter INSTANCE = new ConfluenceRecipeImporter();
    private ConfluenceRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) return false;
        var r = holder.value();
        return r instanceof ItemTransmutationRecipe || r instanceof SkyMillRecipe || r instanceof AltarRecipe
                || r instanceof HellforgeRecipe || r instanceof HeavyWorkBenchRecipe || r instanceof AlchemyTableRecipe
                || r instanceof FletchingTableRecipe || r instanceof CookingPotRecipe || r instanceof SawmillRecipe
                || r instanceof SolidifierRecipe || r instanceof HardmodeAnvilRecipe || r instanceof HardmodeForgeRecipe
                || r instanceof LoomRecipe || r instanceof DyeVatRecipe || r instanceof CrystalBallRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var r = holder.value();
        if (r instanceof ItemTransmutationRecipe x) {
            var d = base().setIngredients(new ArrayList<>(List.of(importIngredient(x.source()))))
                    .setTargets(new ArrayList<>(x.target().stream().map(RecipeImporter::copyStack).toList()))
                    .setShrink(Math.max(1, x.shrink())).setGamePhase(ConfluenceGamePhase.valueOf(x.gamePhase().name()));
            return success(holder, ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION, d);
        }
        if (r instanceof AlchemyTableRecipe x) {
            var in = new ArrayList<ConfluenceIngredientData>(); in.add(importIngredient(x.getBase())); in.addAll(importList(x.getIngredients()));
            return success(holder, ConfluenceRecipeEditorTypes.ALCHEMY_TABLE, result(base().setIngredients(in), r, provider));
        }
        if (r instanceof FletchingTableRecipe x) {
            var in = new ArrayList<>(List.of(importIngredient(x.getTail()), importIngredient(x.getBody()), importIngredient(x.getHead())));
            return success(holder, ConfluenceRecipeEditorTypes.FLETCHING_TABLE, result(base().setIngredients(in), r, provider));
        }
        if (r instanceof CookingPotRecipe x) {
            var d = result(base().setIngredients(importList(x.getIngredients())), r, provider).setContainer(RecipeImporter.importIngredient(x.getContainer()))
                    .setHeatSource(importHeat(x.getHeatSource())).setCookingTime(Math.max(0, x.getCookingTime()));
            return success(holder, ConfluenceRecipeEditorTypes.COOKING_POT, d);
        }
        if (r instanceof HellforgeRecipe || r instanceof HardmodeForgeRecipe) {
            var d = result(base().setIngredients(importList(r.getIngredients())), r, provider)
                    .setExperience(number(invoke(r, "getExperience"), 0).floatValue())
                    .setCookingTime(number(invoke(r, "getCookingTime"), 100).intValue())
                    .setRequiresFuel(Boolean.TRUE.equals(invoke(r, "isRequiresFuel")));
            return success(holder, r instanceof HellforgeRecipe ? ConfluenceRecipeEditorTypes.HELLFORGE : ConfluenceRecipeEditorTypes.HARDMODE_FORGE, d);
        }
        if (either(r)) {
            var d = importEither(r).setResult(RecipeImporter.copyResult(r, provider));
            if (environment(r)) d.setEnvironment(importEnvironment(invoke(r, "getEnvironment")));
            return success(holder, eitherType(r), d);
        }
        if (r instanceof SkyMillRecipe || r instanceof CrystalBallRecipe || r instanceof AltarRecipe || r instanceof DyeVatRecipe) {
            var d = result(base().setIngredients(importList(r.getIngredients())), r, provider);
            if (environment(r)) d.setEnvironment(importEnvironment(invoke(r, "getEnvironment")));
            return success(holder, amountType(r), d);
        }
        return null;
    }

    private static boolean either(Recipe<?> r) { return r instanceof HeavyWorkBenchRecipe || r instanceof SawmillRecipe || r instanceof SolidifierRecipe || r instanceof HardmodeAnvilRecipe || r instanceof LoomRecipe; }
    private static boolean environment(Recipe<?> r) { return r instanceof SkyMillRecipe || r instanceof CrystalBallRecipe || r instanceof HeavyWorkBenchRecipe; }

    private static ConfluenceRecipeData importEither(Recipe<?> r) throws RecipeImportException {
        var d = base(); var e = (Either<?, ?>) field(r, "either"); var left = e.left().orElse(null);
        if (left instanceof ShapedRecipePattern p) {
            d.setCraftingMode(ConfluenceCraftingMode.SHAPED).setWidth(p.width()).setHeight(p.height()).setIngredients(importShaped(p));
        } else {
            d.setCraftingMode(ConfluenceCraftingMode.SHAPELESS).setWidth(4).setHeight(4).setIngredients(importList((List<Ingredient>) e.right().orElseThrow()));
        }
        return d;
    }

    private static ArrayList<ConfluenceIngredientData> importShaped(ShapedRecipePattern p) throws RecipeImportException {
        var out = empty(16); var list = p.ingredients();
        for (int y = 0; y < p.height(); y++) for (int x = 0; x < p.width(); x++) {
            int i = y * p.width() + x; if (i < list.size() && !list.get(i).isEmpty()) out.set(y * 4 + x, importIngredient(list.get(i)));
        }
        return out;
    }

    private static ArrayList<ConfluenceIngredientData> importList(List<Ingredient> list) throws RecipeImportException {
        var out = new ArrayList<ConfluenceIngredientData>(); if (list != null) for (var i : list) if (i != null && !i.isEmpty()) out.add(importIngredient(i)); return out;
    }

    private static ConfluenceIngredientData importIngredient(Ingredient i) throws RecipeImportException {
        if (i == null || i.isEmpty()) return new ConfluenceIngredientData().setIngredient(RecipeIngredient.empty());
        var c = i.getCustomIngredient();
        if (c != null && c.getClass().getName().equals("org.confluence.lib.common.recipe.AmountIngredient")) {
            return new ConfluenceIngredientData().setIngredient(RecipeImporter.importIngredient((Ingredient) invoke(c, "ingredient")))
                    .setCount(Math.max(1, number(invoke(c, "amount"), 1).intValue()));
        }
        return new ConfluenceIngredientData().setIngredient(RecipeImporter.importIngredient(i));
    }

    private static ConfluenceEnvironmentData importEnvironment(Object m) {
        var d = new ConfluenceEnvironmentData(); if (m == null) return d;
        d.setGraveyard(Boolean.TRUE.equals(invoke(m, "graveyard")));
        optional(invoke(m, "biome")).ifPresent(v -> d.setBiomes(holderSet((HolderSet<?>) v)));
        optional(invoke(m, "block")).ifPresent(s -> {
            d.setInflate(Math.max(1, number(invoke(s, "inflate"), 1).intValue()));
            optional(invoke(s, "blocks")).ifPresent(v -> d.setBlocks(holderSet((HolderSet<?>) v)));
            optional(invoke(s, "fluids")).ifPresent(v -> d.setFluids(holderSet((HolderSet<?>) v)));
            if (invoke(s, "statePredicates") instanceof List<?> l) d.setStatePredicates(new ArrayList<>(l.stream().filter(StatePropertiesPredicate.class::isInstance).map(StatePropertiesPredicate.class::cast).map(ConfluenceRecipeImporter::state).toList()));
        });
        return d;
    }

    private static ConfluenceHeatSourceData importHeat(CookingPotRecipe.HeatSourcePredicate p) {
        var d = new ConfluenceHeatSourceData();
        p.blocks().ifPresent(v -> { v.ifLeft(t -> d.setBlocks(new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.TAG).setTag(t.location()))); v.ifRight(s -> d.setBlocks(holderSet(s))); });
        p.properties().ifPresent(v -> d.setHasState(true).setState(state(v)));
        p.nbt().ifPresent(v -> {
            var encoded = NbtPredicate.CODEC.encodeStart(JsonOps.INSTANCE, v).result().orElse(new JsonObject());
            d.setHasNbt(true).setNbt(encoded.toString());
        });
        return d;
    }

    private static ConfluenceStatePredicateData state(StatePropertiesPredicate p) {
        var out = new ArrayList<ConfluenceStatePropertyData>();
        var encoded = StatePropertiesPredicate.CODEC.encodeStart(JsonOps.INSTANCE, p).result().orElse(new JsonObject());
        if (encoded instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                var d = new ConfluenceStatePropertyData().setName(entry.getKey());
                JsonElement value = entry.getValue();
                if (value.isJsonObject()) {
                    var range = value.getAsJsonObject();
                    d.setRanged(true)
                            .setMin(range.has("min") ? range.get("min").getAsString() : "")
                            .setMax(range.has("max") ? range.get("max").getAsString() : "");
                } else if (value.isJsonPrimitive()) {
                    d.setValue(value.getAsString());
                }
                out.add(d);
            }
        }
        return new ConfluenceStatePredicateData().setProperties(out);
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfluenceHolderSetData holderSet(HolderSet<T> set) {
        return set.unwrap().map(t -> new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.TAG).setTag(t.location()), hs -> new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.IDS).setValues(new ArrayList<>(((List<Holder<T>>) hs).stream().map(Holder::unwrapKey).flatMap(Optional::stream).map(k -> k.location()).toList())));
    }

    private static ResourceLocation eitherType(Recipe<?> r) throws RecipeImportException {
        if (r instanceof HeavyWorkBenchRecipe) return ConfluenceRecipeEditorTypes.HEAVY_WORK_BENCH; if (r instanceof SawmillRecipe) return ConfluenceRecipeEditorTypes.SAWMILL; if (r instanceof SolidifierRecipe) return ConfluenceRecipeEditorTypes.SOLIDIFIER; if (r instanceof HardmodeAnvilRecipe) return ConfluenceRecipeEditorTypes.HARDMODE_ANVIL; if (r instanceof LoomRecipe) return ConfluenceRecipeEditorTypes.LOOM; throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type");
    }
    private static ResourceLocation amountType(Recipe<?> r) throws RecipeImportException {
        if (r instanceof SkyMillRecipe) return ConfluenceRecipeEditorTypes.SKY_MILL; if (r instanceof AltarRecipe) return ConfluenceRecipeEditorTypes.ALTAR; if (r instanceof DyeVatRecipe) return ConfluenceRecipeEditorTypes.DYE_VAT; if (r instanceof CrystalBallRecipe) return ConfluenceRecipeEditorTypes.CRYSTAL_BALL; throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type");
    }
    private static ConfluenceRecipeData result(ConfluenceRecipeData d, Recipe<?> r, HolderLookup.Provider p) { return d.setResult(RecipeImporter.copyResult(r, p)); }
    private static RecipeImportResult success(RecipeHolder<?> h, ResourceLocation t, ConfluenceRecipeData d) { return RecipeImporter.success(RecipeImporter.baseEntry(h.id(), t).setData(d)); }
    private static ConfluenceRecipeData base() { return new ConfluenceRecipeData().setIngredients(new ArrayList<>()).setTargets(new ArrayList<>()); }
    private static ArrayList<ConfluenceIngredientData> empty(int n) { var l = new ArrayList<ConfluenceIngredientData>(); for (int i = 0; i < n; i++) l.add(new ConfluenceIngredientData().setIngredient(RecipeIngredient.empty())); return l; }
    private static Object field(Object o, String n) { try { Field f = o.getClass().getField(n); return f.get(o); } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); } }
    private static Object invoke(Object o, String n) { try { return o == null ? null : o.getClass().getMethod(n).invoke(o); } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); } }
    private static Optional<?> optional(Object o) { return o instanceof Optional<?> x ? x : Optional.empty(); }
    private static Number number(Object o, Number d) { return o instanceof Number x ? x : d; }
}
