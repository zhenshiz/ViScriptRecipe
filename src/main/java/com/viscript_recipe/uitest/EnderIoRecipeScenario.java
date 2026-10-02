package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.enderio.*;
import com.viscript_recipe.compat.enderio.data.*;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.WorkBenchView;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@LDLRegisterClient(name = "enderio_recipes", group = ViScriptRecipe.MOD_ID, modID = "enderio",
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class EnderIoRecipeScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) { options.tags("recipes", "enderio").defaultTimeoutMs(30_000); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.server("原生配方导入、持久化、编译往返", context -> {
            var counts = new LinkedHashMap<String, Integer>();
            var errors = new ArrayList<String>();
            for (var holder : context.server().getRecipeManager().getRecipes()) {
                if (!EnderIoRecipeImporter.INSTANCE.canImport(holder)) continue;
                try {
                    var entry = importEntry(holder, context.player().registryAccess());
                    var compiled = entry.copy().compile();
                    if (!equivalent(holder.value(), compiled, context.player().registryAccess())) errors.add(holder.id().toString());
                    counts.merge(entry.getType().getPath(), 1, Integer::sum);
                } catch (Exception exception) { errors.add(holder.id() + ": " + exception); }
            }
            for (var type : EnderIoRecipeEditorTypes.TYPES) context.check("原生配方覆盖 " + type, counts.getOrDefault(type, 0) > 0);
            context.check("原生配方往返不丢字段 " + counts, errors.isEmpty(), "无差异", errors);
            for (var type : EnderIoRecipeEditorTypes.TYPES) {
                var template = EnderIoRecipeData.create(type).compile(EnderIoRecipeEditorTypes.id(type));
                var ops = RegistryOps.create(JsonOps.INSTANCE, context.player().registryAccess());
                context.check("默认模板可序列化 " + type, Recipe.CODEC.encodeStart(ops, template).isSuccess());
            }
            var invalid = EnderIoRecipeData.create("soul_binding").setMobCategory("monster");
            boolean rejected = false;
            try { invalid.compile(EnderIoRecipeEditorTypes.id("soul_binding")); } catch (IllegalArgumentException expected) { rejected = true; }
            context.check("拒绝相互冲突的灵魂条件", rejected);
            invalid = EnderIoRecipeData.create("sag_milling"); invalid.getOutputs().getFirst().setChance(Float.NaN);
            rejected = false;
            try { invalid.compile(EnderIoRecipeEditorTypes.id("sag_milling")); } catch (IllegalArgumentException expected) { rejected = true; }
            context.check("拒绝非法概率", rejected);
        });
        scenario.openModularUI("打开真实配方编辑器", context -> {
            var project = new RecipeProject();
            var fixtures = new LinkedHashMap<String, RecipeEntry>();
            var player = context.requirePlayer();
            var recipes = player.level().getRecipeManager().getRecipes().stream().sorted(Comparator.comparing(h -> h.id().toString())).toList();
            for (var holder : recipes) {
                if (!EnderIoRecipeImporter.INSTANCE.canImport(holder)) continue;
                var entry = importEntry(holder, player.registryAccess());
                fixtures.putIfAbsent(entry.getType().getPath(), entry);
            }
            project.getRecipeFile().getEntries().addAll(fixtures.values());
            var editor = new RecipeEditor();
            editor.layout(l -> { l.widthPercent(100); l.heightPercent(100); });
            editor.loadProject(project, null);
            context.put("project", project); context.put("fixtures", fixtures);
            return new ModularUI(UI.of(editor), player);
        }).awaitModularUI();
        for (var type : EnderIoRecipeEditorTypes.TYPES) {
            scenario.step("选择配方 " + type, context -> {
                var project = context.<RecipeProject>get("project");
                var entry = context.<Map<String, RecipeEntry>>get("fixtures").get(type);
                context.put("before", entry.copy().compile());
                context.<NavigationView>getField(project, "navigationView").selectEntry(entry);
            }).awaitElement("#enderio_canvas_" + type).checkVisible("#enderio_canvas_" + type).ticks(3);
            String target = switch (type) {
                case "fire_crafting" -> "#enderio_output_0";
                case "weather_change" -> "#enderio_fluid_output";
                default -> "#enderio_input_0";
            };
            scenario.click(target).step("槽位选择和保存 " + type, context -> {
                var project = context.<RecipeProject>get("project");
                var navigation = context.<NavigationView>getField(project, "navigationView");
                context.check("选中槽位 " + type, !navigation.getSlotSelection().equals(com.viscript_recipe.gui.editor.SlotSelection.RECIPE));
                project.saveCurrentVisualState();
                var entry = context.<Map<String, RecipeEntry>>get("fixtures").get(type);
                context.check("画布保存保留配方 " + type, equivalent(context.get("before"), entry.copy().compile(), context.requirePlayer().registryAccess()));
                navigation.selectRecipe();
            }).hover("#enderio_canvas_" + type).ticks(2).screenshot("enderio_" + type);
        }
        scenario.step("选择能量属性", context -> {
            var project = context.<RecipeProject>get("project");
            context.<NavigationView>getField(project, "navigationView").selectEntry(context.<Map<String, RecipeEntry>>get("fixtures").get("alloy_smelting"));
        }).awaitElement("#enderio_energy").click("#enderio_energy")
                .keyDown(net.minecraft.client.Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL)
                .key(GLFW.GLFW_KEY_A)
                .keyUp(net.minecraft.client.Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL)
                .key(GLFW.GLFW_KEY_BACKSPACE).type("4321").key(GLFW.GLFW_KEY_TAB)
                .step("属性编辑写入原生配方", context -> {
                    var project = context.<RecipeProject>get("project"); project.saveCurrentVisualState();
                    var entry = context.<Map<String, RecipeEntry>>get("fixtures").get("alloy_smelting");
                    context.check("能量字段写入 4321", entry.<EnderIoRecipeData>getData().getEnergy() == 4321);
                    var recipe = (com.enderio.enderio.content.machines.alloy.AlloySmeltingRecipe) entry.copy().compile();
                    context.check("原生配方使用新能量", recipe.energy() == 4321);
                }).ticks(2).checkTextContains("#enderio_summary", "4321").screenshot("enderio_energy_edited");
        scenario.step("选择储罐流体", context -> {
            var project = context.<RecipeProject>get("project");
            context.<NavigationView>getField(project, "navigationView").selectEntry(context.<Map<String, RecipeEntry>>get("fixtures").get("tank"));
        }).awaitElement("#enderio_fluid_input").rightClick("#enderio_fluid_input").step("复合流体保留特殊条件", context -> {
            context.<RecipeProject>get("project").saveCurrentVisualState();
            var data = context.<Map<String, RecipeEntry>>get("fixtures").get("tank").<EnderIoRecipeData>getData();
            context.check("只读预览不清除复合流体条件", !data.getFluidInput().getCustomJson().isBlank());
        }).step("替换复合流体条件", context -> {
            var bounds = context.query("#enderio_replace_fluid").one().bounds();
            context.input().mouseDown(bounds.centerX(), bounds.centerY(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
            context.input().mouseUp(bounds.centerX(), bounds.centerY(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }).awaitElement("#enderio_fluid_input").rightClick("#enderio_fluid_input")
                .step("清空流体输入后持久化", context -> {
            context.<RecipeProject>get("project").saveCurrentVisualState();
            var entry = context.<Map<String, RecipeEntry>>get("fixtures").get("tank").copy();
            context.check("流体右键清空写入数据", entry.<EnderIoRecipeData>getData().getFluidInput().getValue().isEmpty());
            boolean rejected = false;
            try { entry.compile(); } catch (IllegalArgumentException expected) { rejected = true; }
            context.check("拒绝空流体配方", rejected);
            var slot = context.query("#enderio_fluid_input").one().as(com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot.class);
            slot.setFluid(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, 750), true);
            context.<RecipeProject>get("project").saveCurrentVisualState();
            entry = context.<Map<String, RecipeEntry>>get("fixtures").get("tank").copy();
            var tank = (com.enderio.enderio.content.storage.fluid_tank.TankRecipe) entry.compile();
            context.check("流体拖放通知写入原生配方", tank.fluid().amount() == 750 && tank.fluid().getFluids()[0].is(net.minecraft.world.level.material.Fluids.WATER));
        }).closeScreen();
    }

    private static RecipeEntry importEntry(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        try { return Objects.requireNonNull(EnderIoRecipeImporter.INSTANCE.tryImport(holder, provider)).entry(); }
        catch (Exception exception) { throw new IllegalStateException(holder.id().toString(), exception); }
    }

    private static boolean equivalent(Recipe<?> first, Recipe<?> second, HolderLookup.Provider provider) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        if (first instanceof com.enderio.enderio.foundation.soul.ShapedEntityStorageRecipe a
                && second instanceof com.enderio.enderio.foundation.soul.ShapedEntityStorageRecipe b) {
            if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight() || !a.getGroup().equals(b.getGroup())
                    || a.category() != b.category() || a.showNotification() != b.showNotification()
                    || !net.minecraft.world.item.ItemStack.matches(a.getResultItem(provider), b.getResultItem(provider))) return false;
            for (int i = 0; i < a.getIngredients().size(); i++) {
                if (!net.minecraft.world.item.crafting.Ingredient.CODEC.encodeStart(ops, a.getIngredients().get(i)).getOrThrow()
                        .equals(net.minecraft.world.item.crafting.Ingredient.CODEC.encodeStart(ops, b.getIngredients().get(i)).getOrThrow())) return false;
            }
            return true;
        }
        return Recipe.CODEC.encodeStart(ops, first).getOrThrow().equals(Recipe.CODEC.encodeStart(ops, second).getOrThrow());
    }
}
