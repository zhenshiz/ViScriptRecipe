package com.viscript_recipe.uitest;

import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.TestContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.immersive_engineering.IERecipeEditorTypes;
import com.viscript_recipe.compat.immersive_engineering.IERecipeImporter;
import com.viscript_recipe.compat.immersive_engineering.canvas.IECanvas;
import com.viscript_recipe.compat.immersive_engineering.data.IERecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.TaffyPosition;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/** Compares real IE JEI layouts with editable canvases and checks data preservation in a client. */
@LDLRegisterClient(name = "ie_recipe_editors", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public final class IERecipeScenario implements UIScenario {
    private IECanvas canvas;
    private RecipeEntry entry;
    private NavigationView navigation;
    private IRecipeLayoutDrawable<?> nativeLayout;

    @Override public void configure(ScenarioOptions options) {
        options.guiScale(3).tags("immersive_engineering", "recipes", "ui");
    }

    @Override public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("IE recipes and JEI runtime loaded", context -> context.level() != null
                && LDLibJEIPlugin.jeiRuntime != null
                && context.level().getRecipeManager().getRecipes().stream().anyMatch(IERecipeImporter.INSTANCE::canImport))
                .step("all native recipes survive opening and saving the editor", context -> {
            var seen = new HashSet<String>();
            var errors = new ArrayList<String>();
            int tested = 0;
            for (var holder : context.level().getRecipeManager().getRecipes()) {
                if (!IERecipeImporter.INSTANCE.canImport(holder)) continue;
                try {
                    var imported = IERecipeImporter.INSTANCE.tryImport(holder, context.level().registryAccess());
                    var entry = imported.entry();
                    var before = encoded(entry, context);
                    var canvas = new IECanvas(null, entry);
                    canvas.initVisualState(); canvas.load(); canvas.save();
                    var after = encoded(entry, context);
                    if (!before.equals(after)) errors.add(holder.id() + ": editor save changed recipe\n" + before + "\n" + after);
                    entry.copy().compile();
                    seen.add(entry.getType().getPath());
                    tested++;
                } catch (Exception failure) { errors.add(holder.id() + ": " + failure); }
            }
            context.attach("import_errors", String.join("\n", errors));
            context.attach("tested_recipes", String.valueOf(tested));
            context.check("all IE recipes survive editor save", errors.isEmpty(), "no failures", errors.size());
            for (String path : IERecipeEditorTypes.TYPES) {
                context.check(path + " registered", RecipeEditorTypes.get(IERecipeEditorTypes.id(path)).isPresent());
                context.check(path + " opened, saved and compiled", seen.contains(path));
            }
        });
        for (var type : categories()) {
            var path = type.getUid().getPath();
            scenario.openModularUI("JEI comparison " + path, context -> comparison(context, type))
                    .awaitModularUI().awaitElement("#ie_editor")
                    .step("native slot geometry and no overlapping editor slots", this::checkGeometry)
                    .hover("#comparison_title")
                    .screenshotElement("compare_" + path, "#ie_comparison")
                    .click("#ie_input_0")
                    .step("input click selects input properties", context -> context.check("input selection", navigation.getSlotSelection().equals(SlotSelection.ingredient(0))))
                    .step("save preserves representative recipe", context -> {
                        var before = encoded(entry, context);
                        canvas.save();
                        context.check("same recipe after save", before.equals(encoded(entry, context)));
                    });
            if (path.equals("coke_oven")) {
                scenario.click("#ie_fluid_output").step("creosote output is editable and fixed to creosote", context -> {
                    context.check("output fluid selection", navigation.getSlotSelection().equals(SlotSelection.fluid(2)));
                    com.viscript_recipe.gui.canvas.FluidRecipeCanvas.fluidOutputSlots[0].setFluid(
                            new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 750), true);
                    canvas.save();
                    var recipe = (blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe) entry.compile();
                    context.check("creosote amount saved", recipe.creosoteOutput == 750);
                    context.check("foreign fluid normalized", canvas.getVisualFluidOutput(0).getFluid() == blusunrize.immersiveengineering.common.register.IEFluids.CREOSOTE.getStill());
                });
            }
            if (path.equals("mixer")) {
                scenario.click("#ie_fluid_input_0").step("JEI fluid drop updates the saved ingredient", context -> {
                    context.check("input fluid selection", navigation.getSlotSelection().equals(SlotSelection.fluid(0)));
                    com.viscript_recipe.gui.canvas.FluidRecipeCanvas.fluidInputSlots[0].setFluid(
                            new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 500), true);
                    canvas.save();
                    var recipe = (blusunrize.immersiveengineering.api.crafting.MixerRecipe) entry.compile();
                    context.check("dropped fluid compiled", recipe.fluidInput.getFluids()[0].getFluid() == net.minecraft.world.level.material.Fluids.LAVA);
                    context.check("dropped amount compiled", recipe.fluidInput.amount() == 500);
                });
            }
            if (path.equals("arc_furnace")) {
                scenario.step("populate all four chance slots", context -> {
                    for (int i = 5; i < 9; i++) canvas.setVisualOutput(i, net.minecraft.world.item.Items.DIAMOND.getDefaultInstance(), (i - 4) / 10f);
                }).waitUntil("optional outputs move into the JEI recipe", context ->
                        context.el("#ie_output_8").element().getParent() == context.el("#ie_arc_furnace").element())
                        .click("#ie_output_8").step("fourth probability output stays selectable", context -> {
                            context.check("fourth secondary selected", navigation.getSlotSelection().equals(SlotSelection.result(8)));
                            canvas.save();
                            var recipe = (blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe) entry.compile();
                            context.check("all four secondary outputs saved", recipe.secondaryOutputs.size() == 4);
                            context.check("fourth probability saved", Math.abs(recipe.secondaryOutputs.get(3).chance() - .4f) < .001f);
                        }).hover("#comparison_title").screenshotElement("four_probability_outputs", "#ie_comparison");
            }
            scenario.closeScreen();
        }
        scenario.openModularUI("fractional recycling", context -> {
            var recipe = new blusunrize.immersiveengineering.api.crafting.ArcRecyclingRecipe(
                    () -> context.level().registryAccess(),
                    List.of(com.mojang.datafixers.util.Pair.of(new blusunrize.immersiveengineering.api.crafting.TagOutput(net.minecraft.world.item.Items.IRON_INGOT.getDefaultInstance()), 1.5d)),
                    new blusunrize.immersiveengineering.api.crafting.IngredientWithSize(net.minecraft.world.item.crafting.Ingredient.of(net.minecraft.world.item.Items.IRON_PICKAXE), 1), 200, 512);
            return comparison(context, JEIRecipeTypes.ARC_FURNACE_RECYCLING,
                    new RecipeHolder<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("viscript_recipe", "test_fractional_recycling"), recipe));
        }).awaitModularUI().awaitElement("#ie_editor")
                .step("fractional yields use IE's own ingot and nugget expansion", this::checkGeometry)
                .hover("#comparison_title").screenshotElement("fractional_recycling", "#ie_comparison")
                .click("#ie_output_preview_1").step("nugget preview selects its source material", context -> {
                    context.check("preview selects material", navigation.getSlotSelection().equals(SlotSelection.result(0)));
                    var before = encoded(entry, context); canvas.save();
                    context.check("fractional amount survives save", before.equals(encoded(entry, context)));
                }).closeScreen();
    }

    private static List<RecipeType<?>> categories() {
        return List.of(JEIRecipeTypes.COKE_OVEN, JEIRecipeTypes.ALLOY, JEIRecipeTypes.BLAST_FURNACE,
                JEIRecipeTypes.BLAST_FUEL, JEIRecipeTypes.CLOCHE, JEIRecipeTypes.CLOCHE_FERTILIZER,
                JEIRecipeTypes.METAL_PRESS, JEIRecipeTypes.CRUSHER, JEIRecipeTypes.SAWMILL,
                JEIRecipeTypes.BLUEPRINT, JEIRecipeTypes.SQUEEZER, JEIRecipeTypes.FERMENTER,
                JEIRecipeTypes.REFINERY, JEIRecipeTypes.ARC_FURNACE, JEIRecipeTypes.MIXER,
                JEIRecipeTypes.BOTTLING_MACHINE, JEIRecipeTypes.ARC_FURNACE_RECYCLING,
                JEIRecipeTypes.MIXER_POTIONS, JEIRecipeTypes.BOTTLING_MACHINE_POTIONS,
                JEIRecipeTypes.BOTTLING_MACHINE_BUCKETS);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ModularUI comparison(TestContext context, RecipeType type) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        var manager = runtime.getRecipeManager();
        var holders = (List<RecipeHolder<?>>) manager.createRecipeLookup(type).includeHidden().get().toList();
        var holder = holders.stream().sorted(Comparator.comparing(h -> h.id().toString()))
                .max(Comparator.comparingInt(h -> complexity(h, context))).orElseThrow();
        return comparison(context, type, holder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ModularUI comparison(TestContext context, RecipeType type, RecipeHolder<?> holder) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        var manager = runtime.getRecipeManager();
        try { entry = IERecipeImporter.INSTANCE.tryImport(holder, context.level().registryAccess()).entry(); }
        catch (Exception e) { throw new IllegalStateException(e); }
        context.attach("fixture_" + type.getUid().getPath(), holder.id().toString());
        if (holder.value() instanceof blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe expected) {
            var actual = (blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe) entry.compile();
            context.check("arc main outputs retain their original roles", expected.getBaseOutputs().toString().equals(actual.getBaseOutputs().toString()));
            context.check("arc slag retained", net.minecraft.world.item.ItemStack.matches(expected.slag.get(), actual.slag.get()));
            context.check("arc secondaries retained", expected.secondaryOutputs.size() == actual.secondaryOutputs.size());
            context.check("arc JEI category retained", expected.specialRecipeType.equals(actual.specialRecipeType));
        }
        nativeLayout = (IRecipeLayoutDrawable<?>) manager.createRecipeLayoutDrawable(manager.getRecipeCategory(type), holder,
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElseThrow();
        navigation = new NavigationView(new RecipeProject());
        navigation.addListener(NavigationView.SLOT_SELECTION_CHANGED, () -> {});
        canvas = new IECanvas(navigation, entry);
        canvas.initVisualState(); canvas.load();
        var root = new UIElement().setId("ie_comparison").layout(l -> { l.width(430); l.height(220); });
        root.addChild(at(new Label().setValue(net.minecraft.network.chat.Component.literal("JEI / " + type.getUid().getPath())).setId("comparison_title"), 10, 5, 205, 14));
        root.addChild(at(new Label().setValue(net.minecraft.network.chat.Component.literal("ViScript Recipe")), 230, 5, 190, 14));
        IGuiTexture nativeTexture = (graphics, mouseX, mouseY, x, y, w, h, tick) -> {
            nativeLayout.setPosition(Math.round(x), Math.round(y));
            nativeLayout.drawRecipe(graphics, -100, -100);
        };
        root.addChild(at(new UIElement().style(s -> s.backgroundTexture(nativeTexture)), 20, 40,
                nativeLayout.getRect().getWidth(), nativeLayout.getRect().getHeight()));
        root.addChild(at(canvas, 230, 30, 190, 175));
        return new ModularUI(UI.of(root));
    }

    private static int complexity(RecipeHolder<?> holder, TestContext context) {
        try {
            IERecipeData data = IERecipeImporter.INSTANCE.tryImport(holder, context.level().registryAccess()).entry().getData();
            return (int) (data.getInputs().stream().filter(i -> !i.isEmpty()).count()
                    + data.getOutputs().stream().filter(o -> !o.isEmpty()).count() * 10);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @SuppressWarnings("removal")
    private void checkGeometry(TestContext context) {
        var panel = context.el("#ie_" + entry.getType().getPath()).element();
        var slots = context.query("*").list().stream().map(com.lowdragmc.lowdraglib2.uitest.ElementRef::element).filter(e -> e instanceof ItemSlot || e instanceof FluidSlot).toList();
        var errors = new ArrayList<String>();
        for (var view : nativeLayout.getRecipeSlotsView().getSlotViews()) {
            if (view.isEmpty() || view.getRole() == RecipeIngredientRole.RENDER_ONLY) continue;
            // The blueprint icon is a category selector, not a consumed or editable item ingredient.
            if (entry.getType().getPath().equals("blueprint") && view.getRole() == RecipeIngredientRole.CATALYST) continue;
            var rect = ((IRecipeSlotDrawable) view).getRect();
            boolean fluid = view.getIngredients(NeoForgeTypes.FLUID_STACK).findAny().isPresent();
            boolean output = view.getRole() == RecipeIngredientRole.OUTPUT;
            String prefix = fluid ? (output ? "ie_fluid_output" : "ie_fluid_input_") : output ? "ie_output_" : "ie_input_";
            boolean match = slots.stream().filter(e -> e.getId().startsWith(prefix)).anyMatch(e ->
                    near(e.getContentX() - panel.getPositionX(), rect.getX())
                    && near(e.getContentY() - panel.getPositionY(), rect.getY())
                    && near(e.getContentWidth(), rect.getWidth()) && near(e.getContentHeight(), rect.getHeight()));
            if (!match) errors.add(prefix + " native " + rect.getX() + "," + rect.getY() + " " + rect.getWidth() + "x" + rect.getHeight());
        }
        context.attach("geometry_" + entry.getType().getPath(), String.join("\n", errors));
        context.check("all populated native slots have matching editor coordinates and sizes", errors.isEmpty(), "no differences", errors);
        for (int i = 0; i < slots.size(); i++) {
            var a = slots.get(i);
            for (int j = i + 1; j < slots.size(); j++) {
                var b = slots.get(j);
                boolean overlaps = a.getPositionX() < b.getPositionX() + b.getSizeWidth() - .1f
                        && a.getPositionX() + a.getSizeWidth() > b.getPositionX() + .1f
                        && a.getPositionY() < b.getPositionY() + b.getSizeHeight() - .1f
                        && a.getPositionY() + a.getSizeHeight() > b.getPositionY() + .1f;
                context.check(a.getId() + " does not overlap " + b.getId(), !overlaps);
            }
        }
    }

    private static boolean near(float actual, int expected) { return Math.abs(actual - expected) < .2f; }

    private static String encoded(RecipeEntry entry, TestContext context) {
        return Recipe.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess()), entry.compile()).getOrThrow().toString();
    }

    private static UIElement at(UIElement element, int x, int y, int width, int height) {
        return element.layout(l -> { l.positionType(TaffyPosition.ABSOLUTE); l.left(x); l.top(y); l.width(width); l.height(height); });
    }
}
