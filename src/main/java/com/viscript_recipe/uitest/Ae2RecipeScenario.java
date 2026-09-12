package com.viscript_recipe.uitest;

import appeng.recipes.entropy.EntropyRecipeBuilder;
import appeng.recipes.entropy.PropertyValueMatcher;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import appeng.recipes.handlers.ChargerRecipe;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ae2.*;
import com.viscript_recipe.compat.ae2.data.*;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.WorkBenchView;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import java.util.ArrayList;
import java.util.List;

/** Exercises AE2 imports, NBT persistence, native codecs, JEI categories, and real editor interactions. */
@LDLRegisterClient(name = "viscript_recipe_ae2", group = ViScriptRecipe.MOD_ID, modID = "ae2",
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class Ae2RecipeScenario implements UIScenario {
    private static final List<ResourceLocation> TYPES = List.of(Ae2RecipeEditorTypes.INSCRIBER,
            Ae2RecipeEditorTypes.CHARGER, Ae2RecipeEditorTypes.TRANSFORM, Ae2RecipeEditorTypes.ENTROPY);

    @Override
    public void configure(ScenarioOptions options) { options.guiScale(2).tags("ae2", "recipes").scenarioTimeoutMs(180_000); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI runtime ready", context -> runtime() != null)
                .step("import every native AE2 recipe and round-trip through NBT and codecs", context -> {
                    var counts = new java.util.HashMap<ResourceLocation, Integer>();
                    for (var holder : context.level().getRecipeManager().getRecipes()) {
                        if (!Ae2RecipeImporter.INSTANCE.canImport(holder)) continue;
                        var entry = roundTrip(context, holder);
                        counts.merge(entry.getType(), 1, Integer::sum);
                    }
                    for (var type : TYPES) context.check("native recipes found: " + type, counts.getOrDefault(type, 0) > 0);
                    context.put("counts", counts.toString());
                    var entropy = EntropyRecipeBuilder.cool().setInputBlock(Blocks.OAK_LEAVES)
                            .addBlockStateMatcher("distance", new PropertyValueMatcher.Range("1", "7"))
                            .addBlockStateMatcher("persistent", new PropertyValueMatcher.MultiValue(List.of("false", "true")))
                            .addBlockStateMatcher("waterlogged", new PropertyValueMatcher.SingleValue("true"))
                            .setInputFluid(Fluids.WATER).setOutputBlock(Blocks.OAK_LEAVES).setOutputBlockKeep(true)
                            .addBlockStateAppliers("distance", "3").setOutputFluid(Fluids.FLOWING_WATER)
                            .setOutputFluidKeep(true).addFluidStateAppliers("level", "6")
                            .setDrops(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 3)).build();
                    roundTrip(context, new RecipeHolder<>(ViScriptRecipe.id("test_entropy"), entropy));
                    var air = EntropyRecipeBuilder.heat().setInputBlock(Blocks.ICE).setOutputBlock(Blocks.AIR).build();
                    roundTrip(context, new RecipeHolder<>(ViScriptRecipe.id("test_air"), air));
                    var keep = EntropyRecipeBuilder.heat().setInputFluid(Fluids.WATER).setDrops(Items.CLAY_BALL.getDefaultInstance()).build();
                    roundTrip(context, new RecipeHolder<>(ViScriptRecipe.id("test_keep"), keep));
                });
        for (var type : TYPES) {
            scenario.step("show JEI " + type, context -> {
                var recipeType = runtime().getRecipeManager().getRecipeType(type).orElseThrow();
                runtime().getRecipesGui().showTypes(List.of(recipeType));
            }).ticks(4).screenshot("jei_" + type.getPath()).closeScreen();
        }
        scenario.openModularUI("open recipe editor", context -> RecipeEditor.createUI()).awaitModularUI();
        for (var type : TYPES) {
            scenario.step("load editor " + type, context -> load(context, new RecipeEntry().setType(type)))
                    .awaitElement("#ae2_" + type.getPath()).ticks(3)
                    .screenshotElement("editor_" + type.getPath(), "#ae2_" + type.getPath())
                    .screenshot("editor_full_" + type.getPath())
                    .step("save editor " + type, context -> {
                        var entry = context.<RecipeEntry>get("entry");
                        canvas(context).save();
                        roundTrip(context, new RecipeHolder<>(entry.getRecipeId(), entry.copy().compile()));
                    });
        }
        scenario.step("load inscriber interaction fixture", context -> load(context, new RecipeEntry().setType(Ae2RecipeEditorTypes.INSCRIBER)))
                .awaitElement("#ae2_top").ticks(5).click("#ae2_top").rightClick("#ae2_top")
                .step("optional top clears without shifting middle", context -> {
                    canvas(context).save();
                    var recipe = (InscriberRecipe) context.<RecipeEntry>get("entry").compile();
                    context.check("top is empty", recipe.getTopOptional().isEmpty());
                    context.check("middle remains nonempty", !recipe.getMiddleInput().isEmpty());
                    context.el("#ae2_output").as(ItemSlot.class).setItem(new ItemStack(Items.DIAMOND, 3), true);
                    canvas(context).save();
                    var edited = (InscriberRecipe) context.<RecipeEntry>get("entry").copy().compile();
                    context.check("phantom output and quantity persist", edited.getResultItem().is(Items.DIAMOND) && edited.getResultItem().getCount() == 3);
                })
                .step("load charger alternatives", context -> {
                    var input = new Ae2IngredientData().setAlternatives(new ArrayList<>(List.of(
                            RecipeIngredient.item(Items.QUARTZ), RecipeIngredient.item(Items.REDSTONE))));
                    load(context, new RecipeEntry().setType(Ae2RecipeEditorTypes.CHARGER)
                            .setData(new Ae2ChargerRecipeData().setInput(input)));
                }).awaitElement("#ae2_input").ticks(5).click("#ae2_input")
                .awaitElement("#ae2_alternative")
                .step("select the second ingredient alternative", context -> {
                    context.el("#ae2_alternative text-field").as(TextField.class).setText("2", true);
                }).ticks(2)
                .step("editing one alternative preserves the other", context -> {
                    context.check("second alternative shown", context.el("#ae2_input").as(ItemSlot.class).getValue().is(Items.REDSTONE));
                    context.el("#ae2_input").as(ItemSlot.class).setItem(Items.DIAMOND.getDefaultInstance(), true);
                    canvas(context).save();
                    var recipe = (ChargerRecipe) context.<RecipeEntry>get("entry").copy().compile();
                    context.check("first alternative still accepted", recipe.getIngredient().test(Items.QUARTZ.getDefaultInstance()));
                    context.check("edited alternative accepted", recipe.getIngredient().test(Items.DIAMOND.getDefaultInstance()));
                    context.check("replaced alternative rejected", !recipe.getIngredient().test(Items.REDSTONE.getDefaultInstance()));
                })
                .step("load multi-page transform", context -> {
                    var data = new Ae2TransformRecipeData();
                    var inputs = new ArrayList<Ae2IngredientData>();
                    for (int i = 0; i < 12; i++) inputs.add(Ae2IngredientData.of(RecipeIngredient.item(i % 2 == 0 ? Items.QUARTZ : Items.REDSTONE)));
                    data.setInputs(inputs);
                    load(context, new RecipeEntry().setType(Ae2RecipeEditorTypes.TRANSFORM).setData(data));
                }).awaitElement("#ae2_transform").ticks(5).click("#ae2_transform").click("#ae2_explosion switch")
                .step("condition change preserves off-page ingredients", context -> {
                    canvas(context).save();
                    var recipe = (TransformRecipe) context.<RecipeEntry>get("entry").copy().compile();
                    context.check("all twelve inputs survive", recipe.getIngredients().size() == 12);
                    context.check("explosion enabled", recipe.circumstance.isExplosion());
                }).screenshotElement("editor_transform_explosion", "#ae2_transform")
                .step("load entropy with many drops", context -> {
                    var data = new Ae2EntropyRecipeData();
                    var drops = new ArrayList<ItemStack>();
                    for (int i = 0; i < 20; i++) drops.add(new ItemStack(Items.DIAMOND, i + 1));
                    data.setDrops(drops);
                    load(context, new RecipeEntry().setType(Ae2RecipeEditorTypes.ENTROPY).setData(data));
                }).awaitElement("#ae2_entropy").ticks(5).rightClick("#ae2_drop_0")
                .step("clearing a drop preserves later pages", context -> {
                    canvas(context).save();
                    var data = context.<RecipeEntry>get("entry").<Ae2EntropyRecipeData>getData();
                    context.check("off-page drops unchanged", data.getDrops().size() == 20 && data.getDrops().getLast().getCount() == 20);
                    var recipe = Ae2RecipeFactory.compile(data);
                    context.check("empty editor cell omitted from native drops", recipe.getDrops().size() == 19);
                }).closeScreen();
    }

    private static RecipeEntry roundTrip(TestContext context, RecipeHolder<?> holder) {
        try {
            var result = Ae2RecipeImporter.INSTANCE.tryImport(holder, context.level().registryAccess());
            context.check("imports " + holder.id(), result != null && result.successful());
            var ops = RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess());
            var original = Recipe.CODEC.encodeStart(ops, holder.value()).getOrThrow();
            var copy = result.entry().copy();
            var compiled = Recipe.CODEC.encodeStart(ops, copy.compile()).getOrThrow();
            context.check("NBT/codec round-trip " + holder.id(), original.equals(compiled), original, compiled);
            Recipe.CODEC.parse(ops, compiled).getOrThrow();
            return copy;
        } catch (Exception exception) { throw new IllegalStateException("AE2 round-trip failed for " + holder.id(), exception); }
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
