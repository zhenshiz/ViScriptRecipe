package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.farm_and_charm.*;
import com.viscript_recipe.compat.farm_and_charm.data.*;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.WorkBenchView;
import io.netty.buffer.Unpooled;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.farm_and_charm.core.recipe.*;
import java.util.*;

/** Verifies all native recipes, six JEI/editor layouts, real slot interactions, and live JEI replacements. */
@LDLRegisterClient(name = "viscript_recipe_farm_charm", group = ViScriptRecipe.MOD_ID, modID = FarmCharmRecipeKind.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class FarmCharmRecipeScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("farm_charm", "recipes").scenarioTimeoutMs(180_000); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI runtime ready", context -> runtime() != null)
                .step("round-trip every native recipe through NBT, JSON and network codecs", context -> {
                    var counts = new EnumMap<FarmCharmRecipeKind, Integer>(FarmCharmRecipeKind.class);
                    for (var holder : context.level().getRecipeManager().getRecipes()) {
                        if (!FarmCharmRecipeImporter.INSTANCE.canImport(holder)) continue;
                        var entry = roundTrip(context, holder);
                        var kind = FarmCharmRecipeKind.byType(entry.getType()).orElseThrow();
                        counts.merge(kind, 1, Integer::sum);
                    }
                    for (var kind : FarmCharmRecipeKind.values()) context.check("native recipes found: " + kind, counts.getOrDefault(kind, 0) > 0);
                    ViScriptRecipe.LOGGER.info("Farm & Charm tested native recipes: {}", counts);
                    var data = new FarmCharmRecipeData().setContainerRequired(false).setRequiresLearning(true)
                            .setContainer(new ItemStack(Items.GLASS_BOTTLE, 2)).setExperience(1.75f).setProcessingCategory("CUSTOM");
                    var alternative = new FarmCharmIngredientData().setAlternatives(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.QUARTZ), RecipeIngredient.item(Items.REDSTONE))));
                    data.setInputs(new ArrayList<>(List.of(alternative)));
                    for (var kind : FarmCharmRecipeKind.values()) {
                        roundTrip(context, new RecipeHolder<>(ViScriptRecipe.id("test_" + kind.typeId().getPath()), data.compile(kind.typeId())));
                    }
                    var oversized = new FarmCharmRecipeData();
                    for (int i = 0; i < 6; i++) oversized.getInputs().add(FarmCharmIngredientData.of(RecipeIngredient.item(Items.WHEAT)));
                    try {
                        oversized.compile(FarmCharmRecipeKind.COOKING_POT.typeId());
                        context.check("reject overflowing machine inputs", false);
                    } catch (IllegalArgumentException expected) { context.check("reject overflowing machine inputs", true); }
                    var tagged = new FarmCharmRecipeData().setInputs(new ArrayList<>(List.of(
                            FarmCharmIngredientData.of(RecipeIngredient.tag(net.minecraft.tags.ItemTags.LOGS.location())))));
                    var taggedHolder = new RecipeHolder<>(ViScriptRecipe.id("test_tag"), tagged.compile(FarmCharmRecipeKind.MINCER.typeId()));
                    var taggedCopy = importEntry(context, taggedHolder).copy();
                    context.check("tag identity persists", taggedCopy.<FarmCharmRecipeData>getData().getInputs().getFirst().getAlternatives().getFirst()
                            .getTag().equals(net.minecraft.tags.ItemTags.LOGS.location()));
                });
        for (var kind : FarmCharmRecipeKind.values()) {
            scenario.step("show native JEI " + kind, context -> {
                var type = runtime().getRecipeManager().getRecipeType(kind.jeiTypeId()).orElseThrow();
                runtime().getRecipesGui().showTypes(List.of(type));
            }).ticks(4).screenshot("jei_" + kind.typeId().getPath()).closeScreen();
        }
        scenario.openModularUI("open recipe editor", context -> RecipeEditor.createUI()).awaitModularUI();
        for (var kind : FarmCharmRecipeKind.values()) {
            var selector = "#farm_charm_" + kind.typeId().getPath();
            scenario.step("load editor " + kind, context -> {
                var holder = nativeRecipe(context, kind);
                load(context, importEntry(context, holder));
                context.put("original", holder);
            }).awaitElement(selector).ticks(4)
                    .screenshotElement("editor_" + kind.typeId().getPath(), selector)
                    .screenshot("editor_full_" + kind.typeId().getPath())
                    .step("save native editor " + kind, context -> {
                        canvas(context).save();
                        var saved = context.<RecipeEntry>get("entry").copy();
                        var original = context.<RecipeHolder<?>>get("original");
                        var ops = RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess());
                        context.check("editor preserves native data: " + kind,
                                Recipe.CODEC.encodeStart(ops, original.value()).getOrThrow().equals(Recipe.CODEC.encodeStart(ops, saved.compile()).getOrThrow()));
                        context.check("every input cell exists: " + kind, context.count("#farm_charm_input_" + (kind.inputCount() - 1)) == 1);
                    });
        }
        scenario.step("load bowl with empty input cells", context -> load(context, new RecipeEntry().setType(FarmCharmRecipeKind.CRAFTING_BOWL.typeId())))
                .awaitElement("#farm_charm_crafting_bowl").ticks(4)
                .screenshotElement("editor_bowl_empty_cells", "#farm_charm_crafting_bowl")
                .step("last bowl cell accepts a second ingredient", context -> {
                    context.el("#farm_charm_input_3").as(ItemSlot.class).setItem(Items.SUGAR.getDefaultInstance(), true);
                    canvas(context).save();
                    context.check("fourth bowl cell is editable", context.<RecipeEntry>get("entry").copy().compile().getIngredients().size() == 2);
                });
        scenario.step("load pot interaction fixture", context -> load(context, new RecipeEntry().setType(FarmCharmRecipeKind.COOKING_POT.typeId())))
                .awaitElement("#farm_charm_pot_cooking").ticks(5).click("#farm_charm_pot_cooking")
                .click("#farm_charm_require_container switch").click("#farm_charm_requires_learning switch")
                .step("edit output and optional container", context -> {
                    context.el("#farm_charm_output").as(ItemSlot.class).setItem(new ItemStack(Items.DIAMOND, 3), true);
                    context.el("#farm_charm_container").as(ItemSlot.class).setItem(new ItemStack(Items.GLASS_BOTTLE, 2), true);
                    canvas(context).save();
                    var recipe = (CookingPotRecipe) context.<RecipeEntry>get("entry").copy().compile();
                    context.check("container flag toggles", !recipe.isContainerRequired());
                    context.check("learning flag toggles", recipe.requiresLearning());
                    context.check("container retained while disabled", recipe.getContainerItem().is(Items.GLASS_BOTTLE) && recipe.getContainerItem().getCount() == 2);
                    context.check("output item and count retained", recipe.getResultItem(context.level().registryAccess()).is(Items.DIAMOND)
                            && recipe.getResultItem(context.level().registryAccess()).getCount() == 3);
                    context.el("#farm_charm_input_5").as(ItemSlot.class).setItem(Items.CARROT.getDefaultInstance(), true);
                    canvas(context).save();
                    context.check("last empty input cell accepts items", context.<RecipeEntry>get("entry").compile().getIngredients().size() == 2);
                }).rightClick("#farm_charm_input_5")
                .step("right-click clears the last input without shifting others", context -> {
                    canvas(context).save();
                    var recipe = context.<RecipeEntry>get("entry").copy().compile();
                    context.check("only wheat remains", recipe.getIngredients().size() == 1 && recipe.getIngredients().getFirst().test(Items.WHEAT.getDefaultInstance()));
                })
                .step("load stove", context -> load(context, new RecipeEntry().setType(FarmCharmRecipeKind.STOVE.typeId())))
                .awaitElement("#farm_charm_stove").ticks(5).click("#farm_charm_stove")
                .step("edit experience", context -> {
                    context.el("#farm_charm_experience text-field").as(TextField.class).setText("2.75", true);
                    canvas(context).save();
                    context.check("experience persists", ((StoveRecipe) context.<RecipeEntry>get("entry").copy().compile()).getExperience() == 2.75f);
                })
                .step("load mincer alternatives", context -> {
                    var input = new FarmCharmIngredientData().setAlternatives(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.QUARTZ), RecipeIngredient.item(Items.REDSTONE))));
                    load(context, new RecipeEntry().setType(FarmCharmRecipeKind.MINCER.typeId())
                            .setData(new FarmCharmRecipeData().setInputs(new ArrayList<>(List.of(input)))));
                }).awaitElement("#farm_charm_mincer").ticks(5).click("#farm_charm_mincer")
                .step("edit custom processing category", context -> context.el("#farm_charm_processing_category text-field").as(TextField.class).setText("CUSTOM", true))
                .click("#farm_charm_input_0").awaitElement("#farm_charm_alternative")
                .step("select second alternative", context -> context.el("#farm_charm_alternative text-field").as(TextField.class).setText("2", true))
                .ticks(2).step("edit selected alternative", context -> {
                    context.check("second alternative shown", context.el("#farm_charm_input_0").as(ItemSlot.class).getValue().is(Items.REDSTONE));
                    context.el("#farm_charm_input_0").as(ItemSlot.class).setItem(Items.DIAMOND.getDefaultInstance(), true);
                    canvas(context).save();
                    var recipe = (MincerRecipe) context.<RecipeEntry>get("entry").copy().compile();
                    context.check("custom category survives", recipe.getRecipeType().equals("CUSTOM"));
                    context.check("both alternatives accepted", recipe.getInput().test(Items.QUARTZ.getDefaultInstance()) && recipe.getInput().test(Items.DIAMOND.getDefaultInstance()));
                    context.check("old alternative replaced", !recipe.getInput().test(Items.REDSTONE.getDefaultInstance()));
                }).closeScreen();
        for (var kind : FarmCharmRecipeKind.values()) {
            scenario.step("JEI replace, remove and restore " + kind, context -> verifyJeiDelta(context, kind));
        }
        scenario.step("JEI moves one recipe ID between raw categories", context -> {
            var id = ViScriptRecipe.id("test_category_change");
            var pot = new RecipeHolder<>(id, new FarmCharmRecipeData().compile(FarmCharmRecipeKind.COOKING_POT.typeId()));
            var bowl = new RecipeHolder<>(id, new FarmCharmRecipeData().compile(FarmCharmRecipeKind.CRAFTING_BOWL.typeId()));
            delta(4, null, pot);
            delta(5, pot, bowl);
            context.check("old category no longer displays recipe", !visible(FarmCharmRecipeKind.COOKING_POT).contains(pot.value()));
            context.check("new category displays moved recipe", visible(FarmCharmRecipeKind.CRAFTING_BOWL).contains(bowl.value()));
            delta(6, bowl, null);
            context.check("moved recipe can be removed", !visible(FarmCharmRecipeKind.CRAFTING_BOWL).contains(bowl.value()));
        });
    }

    private static RecipeHolder<?> nativeRecipe(TestContext context, FarmCharmRecipeKind kind) {
        return context.level().getRecipeManager().getRecipes().stream()
                .filter(holder -> FarmCharmRecipeImporter.INSTANCE.canImport(holder)
                        && kind.typeId().equals(BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType())))
                .max(Comparator.comparingInt(holder -> holder.value().getIngredients().size())).orElseThrow();
    }

    private static RecipeEntry importEntry(TestContext context, RecipeHolder<?> holder) {
        try { return FarmCharmRecipeImporter.INSTANCE.tryImport(holder, context.level().registryAccess()).entry(); }
        catch (Exception exception) { throw new IllegalStateException("Farm & Charm import failed: " + holder.id(), exception); }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static RecipeEntry roundTrip(TestContext context, RecipeHolder<?> holder) {
        var copy = importEntry(context, holder).copy();
        var ops = RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess());
        var original = Recipe.CODEC.encodeStart(ops, holder.value()).getOrThrow();
        var compiled = copy.compile();
        var json = Recipe.CODEC.encodeStart(ops, compiled).getOrThrow();
        context.check("NBT/JSON round-trip " + holder.id(), original.equals(json), original, json);
        Recipe.CODEC.parse(ops, json).getOrThrow();
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.level().registryAccess());
        try {
            StreamCodec codec = compiled.getSerializer().streamCodec();
            codec.encode(buffer, compiled);
            var decoded = (Recipe<?>) codec.decode(buffer);
            var networkJson = Recipe.CODEC.encodeStart(ops, decoded).getOrThrow();
            context.check("network round-trip " + holder.id(), json.equals(networkJson), json, networkJson);
        } finally { buffer.release(); }
        return copy;
    }

    private static void verifyJeiDelta(TestContext context, FarmCharmRecipeKind kind) {
        var old = nativeRecipe(context, kind);
        var entry = importEntry(context, old);
        entry.<FarmCharmRecipeData>getData().setResult(new ItemStack(Items.DIAMOND, 7));
        RecipeHolder<?> next = new RecipeHolder<>(old.id(), entry.compile());
        var before = visible(kind);
        context.check("native raw JEI object found: " + kind, before.contains(old.value()));
        delta(1, old, next);
        var changed = visible(kind);
        context.check("JEI replaces raw object: " + kind, !changed.contains(old.value()) && changed.contains(next.value()) && changed.size() == before.size());
        delta(2, next, null);
        context.check("JEI removes raw object: " + kind, !visible(kind).contains(next.value()) && visible(kind).size() == before.size() - 1);
        var restored = new RecipeHolder<>(old.id(), importEntry(context, old).compile());
        delta(3, null, restored);
        context.check("JEI adds raw object: " + kind, visible(kind).contains(restored.value()) && visible(kind).size() == before.size());
    }

    private static void delta(long revision, RecipeHolder<?> old, RecipeHolder<?> next) {
        var id = old == null ? next.id() : old.id();
        RecipeDeltaJeiSynchronizer.applyDelta(revision, Set.of(id), old == null ? Map.of() : Map.of(id, old),
                next == null ? Map.of() : Map.of(id, next), Map.of(), Map.of(), false);
    }

    private static List<?> visible(FarmCharmRecipeKind kind) {
        var manager = runtime().getRecipeManager();
        return manager.createRecipeLookup(manager.getRecipeType(kind.jeiTypeId()).orElseThrow()).get().toList();
    }

    private static void load(TestContext context, RecipeEntry entry) {
        var editor = context.requireUI().getElementsByType(RecipeEditor.class).getFirst();
        if (editor.getCurrentProject() instanceof RecipeProject project) {
            project.getRecipeFile().getEntries().clear();
            project.getRecipeFile().getEntries().add(entry);
        } else {
            var project = new RecipeProject();
            project.getRecipeFile().getEntries().add(entry);
            editor.loadProject(project, null);
        }
        context.requireUI().getElementsByType(NavigationView.class).getFirst().selectEntry(entry);
        context.put("entry", entry);
    }

    private static RecipeCanvas<?> canvas(TestContext context) {
        return context.requireUI().getElementsByType(WorkBenchView.class).getFirst().getCanvas();
    }

    private static IJeiRuntime runtime() {
        try {
            var field = RecipeDeltaJeiSynchronizer.class.getDeclaredField("runtime");
            field.setAccessible(true);
            return (IJeiRuntime) field.get(null);
        } catch (ReflectiveOperationException exception) { throw new IllegalStateException(exception); }
    }
}
