package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeImporter;
import com.viscript_recipe.compat.irons_spellbooks.canvas.IronAlchemistCauldronCanvas;
import com.viscript_recipe.compat.irons_spellbooks.canvas.IronNoAdditionSmithingCanvas;
import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.compat.irons_spellbooks.data.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.WorkBenchView;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import com.viscript_recipe.recipe.*;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.jei.AlchemistCauldronRecipeCategory;
import io.redspace.ironsspellbooks.recipe_types.NoAdditionSmithingTransformRecipe;
import io.redspace.ironsspellbooks.registries.BlockRegistry;
import mezz.jei.common.Internal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@LDLRegisterClient(name = "irons_standard_recipes", group = ViScriptRecipe.MOD_ID, modID = "irons_spellbooks",
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public class IronSpellbooksRecipeScenario implements UIScenario {
    private static final ResourceLocation BREW_ID = ViScriptRecipe.id("uitest/irons/brew");
    private static final List<ResourceLocation> TYPES = List.of(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL,
            IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY, IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW,
            IronSpellbooksRecipeEditorTypes.SMITHING_TRANSFORM_NO_ADDITION);

    @Override
    public void configure(ScenarioOptions options) {options.defaultTimeoutMs(45_000);}

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.server("原生配方导入与序列化", c -> {
            var provider = c.server().registryAccess();
            int count = 0;
            for (var holder : c.server().getRecipeManager().getRecipes()) {
                if (!IronSpellbooksRecipeImporter.INSTANCE.canImport(holder)) continue;
                try {
                    var entry = IronSpellbooksRecipeImporter.INSTANCE.tryImport(holder, provider).entry();
                    var copy = new RecipeEntry(); copy.deserializeNBT(provider, entry.serializeNBT(provider));
                    c.check("原生配方往返 " + holder.id(), RecipeDeltaSnapshot.encodeRecipe(provider, holder)
                            .equals(RecipeDeltaSnapshot.encodeRecipe(provider, new RecipeHolder<>(holder.id(), copy.compile()))));
                    count++;
                } catch (Exception e) {throw new RuntimeException(e);}
            }
            c.check("加载了原生炼药锅配方", count > 0);
            var vanillaTypes = c.server().getRecipeManager().getRecipes().stream()
                    .filter(h -> h.id().getNamespace().equals("irons_spellbooks") && RecipeImporter.VANILLA_HANDLER.canImport(h))
                    .map(h -> h.value().getSerializer()).distinct().count();
            c.check("原版配方类型仍支持导入", vanillaTypes == 8);
            c.check("奥术铁砧没有编辑入口", RecipeEditorTypes.get(ResourceLocation.parse("irons_spellbooks:arcane_anvil_transform")).isEmpty());
            for (var type : TYPES) c.check("标准类型已注册 " + type, RecipeEditorTypes.get(type).isPresent());
            var smithing = new NoAdditionSmithingTransformRecipe(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.IRON_SWORD), new ItemStack(Items.GOLDEN_SWORD));
            try {
                var imported = IronSpellbooksRecipeImporter.INSTANCE.tryImport(new RecipeHolder<>(ViScriptRecipe.id("uitest/irons/smithing"), smithing), provider).entry();
                c.check("无追加材料锻造导入往返", RecipeDeltaSnapshot.encodeRecipe(provider, new RecipeHolder<>(imported.getRecipeId(), smithing))
                        .equals(RecipeDeltaSnapshot.encodeRecipe(provider, new RecipeHolder<>(imported.getRecipeId(), imported.compile()))));
            } catch (Exception e) {throw new RuntimeException(e);}
            var base = new ItemStack(Items.IRON_SWORD); base.set(DataComponents.CUSTOM_NAME, Component.literal("base name"));
            c.check("锻造保留基础物品数据", smithing.assemble(new SmithingRecipeInput(new ItemStack(Items.GOLD_INGOT), base, ItemStack.EMPTY), provider)
                    .get(DataComponents.CUSTOM_NAME).equals(base.get(DataComponents.CUSTOM_NAME)));
            var file = fixtures(); c.put("file", file);
            c.put("entries", file.getEntries().stream().map(e -> e.serializeNBT(provider)).toList());
        }).waitUntil("JEI 运行时可用", c -> Internal.getOptionalJeiRuntime().isPresent())
                .server("新增配方及真实工作站执行", c -> {
                    RecipeFile file = c.get("file"); save(file, c);
                    RecipeReloadSyncService.syncBaselineToPlayer(c.player());
                    var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
                    c.check("四类配方正常新增", result.applyResult().appliedEntryCount() == 4 && result.applyResult().failedEntryCount() == 0);
                    c.check("标准配方增量同步", !result.requiresFullSync() && result.delta().changedRecipeCount() == 4);
                    RecipeReloadSyncService.syncDeltaToPlayers(c.server(), result.delta());
                    var smithingInput = new SmithingRecipeInput(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_SWORD), ItemStack.EMPTY);
                    c.check("原生锻造台匹配无追加材料配方", c.server().getRecipeManager().getRecipeFor(RecipeType.SMITHING, smithingInput, c.level())
                            .map(h -> h.id().equals(ViScriptRecipe.id("uitest/irons/smithing"))).orElse(false));
                    var tile = new AlchemistCauldronTile(new BlockPos(0, 80, 0), BlockRegistry.ALCHEMIST_CAULDRON.get().defaultBlockState());
                    tile.setLevel(c.level());
                    c.check("原生注入执行自定义配方", tile.tryExecuteRecipeInteractions(c.level(), new ItemStack(Items.SPIDER_EYE)).is(Items.STONE)
                            && tile.getFluidAmount() == 100);
                    c.check("原生取出执行自定义配方", tile.tryExecuteRecipeInteractions(c.level(), new ItemStack(Items.ENDER_PEARL)).is(Items.DIAMOND)
                            && tile.getFluidAmount() == 0);
                    tile.fluidInventory.fill(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
                    var reagent = new ItemStack(Items.COBWEB); tile.tryMeltInput(reagent);
                    c.check("原生酿造消耗材料并生成多个流体", reagent.isEmpty() && tile.getFluidAmount() == 250
                            && tile.fluidInventory.fluids().size() == 2);
                    c.check("原生酿造生成副产物", tile.inputItems.stream().anyMatch(s -> s.is(Items.GOLD_NUGGET)));
                }).waitUntil("客户端及 JEI 展示新配方", c -> {
                    return c.level().getRecipeManager().byKey(BREW_ID).isPresent() && jeiByproduct(Items.GOLD_NUGGET);
                });
        scenario.openModularUI("打开真实配方编辑器", c -> {
            var project = new RecipeProject();
            for (var nbt : c.<List<CompoundTag>>get("entries")) {
                var entry = new RecipeEntry(); entry.deserializeNBT(c.level().registryAccess(), nbt); project.getRecipeFile().getEntries().add(entry);
            }
            var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100));
            editor.loadProject(project, null); c.put("project", project);
            return new ModularUI(UI.of(editor), c.requirePlayer());
        }).awaitModularUI();
        for (int i = 0; i < TYPES.size(); i++) {
            int index = i;
            scenario.step("画布保存往返 " + TYPES.get(i), c -> {
                RecipeProject p = c.get("project"); NavigationView nav = c.getField(p, "navigationView"); WorkBenchView work = c.getField(p, "workbenchView");
                var entry = p.getRecipeFile().getEntries().get(index);
                var before = RecipeDeltaSnapshot.encodeRecipe(c.level().registryAccess(), new RecipeHolder<>(entry.getRecipeId(), entry.compile()));
                nav.selectEntry(entry); p.saveCurrentVisualState();
                c.check("画布类型 " + entry.getType(), entry.getType().equals(TYPES.get(3)) ? work.getCanvas() instanceof IronNoAdditionSmithingCanvas : work.getCanvas() instanceof IronAlchemistCauldronCanvas);
                c.check("UI 保存保留配方 " + entry.getType(), before.equals(RecipeDeltaSnapshot.encodeRecipe(c.level().registryAccess(), new RecipeHolder<>(entry.getRecipeId(), entry.compile()))));
            }).frames(2).checkVisible(i == 3 ? "#irons_smithing_input_0" : "#irons_cauldron_input")
                    .screenshot("canvas_" + TYPES.get(i).getPath()).frames(2);
        }
        scenario.step("选择酿造配方", c -> {
            RecipeProject p = c.get("project"); NavigationView nav = c.getField(p, "navigationView");
            nav.selectEntry(p.getRecipeFile().getEntries().get(2));
        }).awaitElement("#irons_cauldron_add_fluid").checkCount("#irons_cauldron_fluid_2", 1)
                .click("#irons_cauldron_add_fluid").awaitElement("#irons_cauldron_fluid_3")
                .step("新增输出槽位保存", c -> {
                    var canvas = cauldron(c); canvas.setVisualFluidOutput(3, new FluidStack(Fluids.WATER, 25)); canvas.save();
                    c.check("第三个输出可编辑", canvas.getData().getResultFluids().size() == 3 && canvas.getData().getResultFluids().get(2).getAmount() == 25);
                }).click("#irons_cauldron_remove_fluid").checkNotExists("#irons_cauldron_fluid_3")
                .step("移除槽位保留原输出", c -> {
                    var canvas = cauldron(c); canvas.save(); c.check("两个输出均保留", canvas.getData().getResultFluids().size() == 2
                            && canvas.getData().getResultFluids().get(0).getAmount() == 125 && canvas.getData().getResultFluids().get(1).getAmount() == 125);
                }).rightClick("#irons_cauldron_item_output").step("副产物可独立清空", c -> {
                    var canvas = cauldron(c); canvas.save(); c.check("清空物品不影响流体", canvas.getData().getByproduct().isEmpty() && canvas.getData().getResultFluids().size() == 2);
                }).step("显示副产物单独输出", c -> {
                    RecipeProject p = c.get("project"); NavigationView nav = c.getField(p, "navigationView");
                    var entry = new RecipeEntry().setType(TYPES.get(2)).setData(new IronAlchemistCauldronRecipeData()
                            .setInput(RecipeIngredient.item(Items.COBWEB)).setResultFluids(new ArrayList<>()).setByproduct(new ItemStack(Items.DIAMOND)));
                    nav.selectEntry(entry); p.saveCurrentVisualState();
                    c.check("仅副产物配方可保存", ((io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe) entry.compile()).results().isEmpty());
                }).screenshot("byproduct_only").closeScreen();
        scenario.server("替换配方与 JEI 刷新", c -> {
            RecipeFile file = c.get("file"); file.getEntries().get(2).setOperation(RecipeOperation.REPLACE)
                    .<IronAlchemistCauldronRecipeData>getData().setByproduct(new ItemStack(Items.IRON_NUGGET)); save(file, c);
            var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
            c.check("替换仅同步一个配方", !result.requiresFullSync() && result.delta().changedRecipeCount() == 1);
            RecipeReloadSyncService.syncDeltaToPlayers(c.server(), result.delta());
        }).waitUntil("JEI 显示替换后的副产物", c -> jeiByproduct(Items.IRON_NUGGET) && !jeiByproduct(Items.GOLD_NUGGET))
                .server("删除配方", c -> {
                    RecipeFile file = c.get("file"); file.getEntries().forEach(e -> e.setOperation(RecipeOperation.REMOVE)); save(file, c);
                    var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
                    c.check("四类配方均删除", file.getEntries().stream().allMatch(e -> c.server().getRecipeManager().byKey(e.getRecipeId()).isEmpty()));
                    RecipeReloadSyncService.syncDeltaToPlayers(c.server(), result.delta());
                }).waitUntil("客户端和 JEI 删除配方", c -> c.level().getRecipeManager().byKey(BREW_ID).isEmpty() && !jeiByproduct(Items.IRON_NUGGET));
        scenario.teardownServer("清理测试配方", c -> {
            try {Files.deleteIfExists(RecipeAssetPaths.recipeDirectory().resolve("irons-uitest.recipe"));} catch (Exception e) {throw new RuntimeException(e);}
            RecipeOverrideManager.reload(c.server().getRecipeManager(), c.server().registryAccess());
        });
    }

    private static RecipeFile fixtures() {
        var file = new RecipeFile();
        file.getEntries().add(entry(0, "fill", new IronAlchemistCauldronRecipeData().setInput(RecipeIngredient.item(Items.SPIDER_EYE))
                .setResult(new ItemStack(Items.STONE)).setFluid(new FluidStack(Fluids.WATER, 100))));
        file.getEntries().add(entry(1, "empty", new IronAlchemistCauldronRecipeData().setInput(RecipeIngredient.item(Items.ENDER_PEARL))
                .setResult(new ItemStack(Items.DIAMOND)).setFluid(new FluidStack(Fluids.WATER, 100))));
        file.getEntries().add(entry(2, "brew", new IronAlchemistCauldronRecipeData().setInput(RecipeIngredient.item(Items.COBWEB))
                .setBaseFluid(new FluidStack(Fluids.WATER, 250)).setResultFluids(new ArrayList<>(List.of(new FluidStack(Fluids.WATER, 125), new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("irons_spellbooks:blood")), 125))))
                .setByproduct(new ItemStack(Items.GOLD_NUGGET))));
        file.getEntries().add(entry(3, "smithing", new IronNoAdditionSmithingRecipeData()));
        return file;
    }

    private static RecipeEntry entry(int index, String path, IVSRecipeData data) {
        return new RecipeEntry().setType(TYPES.get(index)).setRecipeId(ViScriptRecipe.id("uitest/irons/" + path)).setOperation(RecipeOperation.ADD).setData(data);
    }

    private static void save(RecipeFile file, ServerContext context) {
        try {RecipeFileLoader.save(RecipeAssetPaths.recipeDirectory().resolve("irons-uitest.recipe"), file, context.server().registryAccess());}
        catch (Exception e) {throw new RuntimeException(e);}
    }

    private static IronAlchemistCauldronCanvas cauldron(TestContext context) {
        WorkBenchView work = context.getField(context.<RecipeProject>get("project"), "workbenchView");
        return (IronAlchemistCauldronCanvas) work.getCanvas();
    }

    private static boolean jeiByproduct(Item item) {
        return Internal.getOptionalJeiRuntime().map(runtime -> runtime.getRecipeManager()
                .createRecipeLookup(AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE).get()
                .anyMatch(recipe -> recipe.itemIn().test(new ItemStack(Items.COBWEB)) && recipe.resultByproduct().is(item)
                        && recipe.resultByproduct().getCount() == 1)).orElse(false);
    }
}
