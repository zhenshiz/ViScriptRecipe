package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeData;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeEditorTypes;
import com.viscript_recipe.gui.canvas.vanilla.VanillaEventRecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeProject;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.WorkBenchView;
import com.viscript_recipe.network.RecipeDeltaSnapshot;
import com.viscript_recipe.recipe.*;
import com.viscript_recipe.recipe.vanilla.VanillaEventRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

import java.nio.file.Files;
import java.util.List;

@LDLRegisterClient(name = "vanilla_event_recipes", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class VanillaEventRecipeScenario implements UIScenario {
    private static final ResourceLocation ANVIL_ID = ViScriptRecipe.id("uitest/vanilla/anvil");
    private static final ResourceLocation BREW_ID = ViScriptRecipe.id("uitest/vanilla/brewing");

    @Override
    public void configure(ScenarioOptions options) {options.defaultTimeoutMs(45_000);}

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.server("添加事件配方并同步", c -> {
            var file = fixtures(); c.put("file", file);
            c.put("entries", file.getEntries().stream().map(e -> e.serializeNBT(c.server().registryAccess())).toList());
            save(file, c);
            var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
            c.check("三条事件配方正常加载", result.applyResult().appliedEntryCount() == 3 && result.applyResult().failedEntryCount() == 0);
            RecipeReloadSyncService.syncFullToPlayers(c.server(), false);
            for (var entry : file.getEntries()) {
                var original = c.server().getRecipeManager().byKey(entry.getRecipeId()).orElseThrow();
                var imported = new VanillaEventRecipeEditorTypes().tryImport(original, c.server().registryAccess()).entry();
                c.check("事件配方可以导入 " + entry.getRecipeId(), RecipeDeltaSnapshot.encodeRecipe(c.server().registryAccess(), original)
                        .equals(RecipeDeltaSnapshot.encodeRecipe(c.server().registryAccess(), new RecipeHolder<>(entry.getRecipeId(), imported.compile()))));
            }
        }).waitUntil("客户端接收事件配方", c -> c.level().getRecipeManager().byKey(ANVIL_ID).isPresent()
                && c.level().potionBrewing().mix(new ItemStack(Items.NETHER_WART), water()).is(Items.EMERALD))
                .server("原生铁砧材料与经验验证", c -> {
                    c.put("gameMode", c.player().gameMode.getGameModeForPlayer());
                    c.put("levels", c.player().experienceLevel);
                    c.player().gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
                    c.player().giveExperienceLevels(20 - c.player().experienceLevel);
                    var menu = new AnvilMenu(100, c.player().getInventory(), ContainerLevelAccess.NULL);
                    menu.getSlot(0).set(new ItemStack(Items.IRON_INGOT, 2));
                    menu.getSlot(1).set(new ItemStack(Items.GOLD_INGOT, 5));
                    menu.createResult();
                    c.check("原生铁砧事件产生结果", menu.getSlot(2).getItem().is(Items.DIAMOND));
                    c.check("事件设置经验与材料费用", menu.getCost() == 3 && menu.repairItemCountCost == 2);
                    menu.setItemName("custom name");
                    c.check("铁砧重命名仍生效", menu.getSlot(2).getItem().get(DataComponents.CUSTOM_NAME).getString().equals("custom name"));
                    c.player().giveExperienceLevels(-20);
                    c.check("经验不足不能拿取", !menu.getSlot(2).mayPickup(c.player()));
                    c.player().giveExperienceLevels(20);
                    c.check("经验足够允许拿取", menu.getSlot(2).mayPickup(c.player()));
                    var output = menu.getSlot(2).remove(1); menu.getSlot(2).onTake(c.player(), output);
                    c.check("原生铁砧扣除等级", c.player().experienceLevel == 17);
                    c.check("左侧整组消耗且右侧按数量扣除", menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().getCount() == 3);
                    menu.getSlot(0).set(new ItemStack(Items.IRON_INGOT)); menu.getSlot(1).set(new ItemStack(Items.GOLD_INGOT)); menu.createResult();
                    c.check("材料不足时结果为空", menu.getSlot(2).getItem().isEmpty());
                    var sword = new ItemStack(Items.IRON_SWORD); sword.setDamageValue(100);
                    menu.getSlot(0).set(sword); menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT)); menu.createResult();
                    c.check("未匹配规则仍执行原版修复", menu.getSlot(2).getItem().is(Items.IRON_SWORD) && menu.getSlot(2).getItem().getDamageValue() < 100);
                }).server("原生酿造台三槽与消耗验证", c -> {
                    var pos = new BlockPos(0, 80, 0); c.level().setBlockAndUpdate(pos, Blocks.BREWING_STAND.defaultBlockState());
                    var tile = (BrewingStandBlockEntity) c.level().getBlockEntity(pos);
                    tile.setItem(0, water()); tile.setItem(1, water());
                    tile.setItem(2, PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));
                    tile.setItem(3, new ItemStack(Items.NETHER_WART, 2)); tile.setItem(4, new ItemStack(Items.BLAZE_POWDER));
                    for (int i = 0; i < 402; i++) BrewingStandBlockEntity.serverTick(c.level(), pos, tile.getBlockState(), tile);
                    c.check("两个匹配底部槽生成新结果", tile.getItem(0).is(Items.EMERALD) && tile.getItem(1).is(Items.EMERALD));
                    c.check("药水组件区分未匹配的第三槽", tile.getItem(2).get(DataComponents.POTION_CONTENTS).potion().orElseThrow().is(Potions.AWKWARD));
                    c.check("整批只扣除一件材料", tile.getItem(3).getCount() == 1);
                    c.check("沿用原版燃料消耗", tile.getItem(4).isEmpty());
                    c.check("注册事件允许自定义非药水输入", c.level().potionBrewing().isInput(new ItemStack(Items.STICK))
                            && c.level().potionBrewing().mix(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.STICK)).is(Items.DIAMOND));
                    c.check("未匹配原版酿造保持正常", c.level().potionBrewing().mix(new ItemStack(Items.GLOWSTONE_DUST), water())
                            .get(DataComponents.POTION_CONTENTS).potion().orElseThrow().is(Potions.THICK));
                });
        scenario.openModularUI("打开事件配方编辑器", c -> {
            var project = new RecipeProject();
            for (var nbt : c.<List<CompoundTag>>get("entries")) {
                var entry = new RecipeEntry(); entry.deserializeNBT(c.level().registryAccess(), nbt); project.getRecipeFile().getEntries().add(entry);
            }
            var editor = new RecipeEditor(); editor.layout(l -> l.widthPercent(100).heightPercent(100)); editor.loadProject(project, null);
            c.put("project", project); return new ModularUI(UI.of(editor), c.requirePlayer());
        }).awaitModularUI().step("选择铁砧并保存", c -> {
            var entry = select(c, 0); var data = entry.<VanillaEventRecipeData>getData();
            c.check("铁砧画布往返保留费用", data.getLevelCost() == 3 && data.getMaterialCost() == 2 && data.getResult().is(Items.DIAMOND));
        }).awaitElement("#vanilla_anvil_level_cost").checkVisible("#vanilla_event_input_0")
                .step("验证 JEI 铁砧槽位布局", c -> {
                    checkPosition(c, "vanilla_anvil_layout", "vanilla_event_input_0", 1, 1);
                    checkPosition(c, "vanilla_anvil_layout", "vanilla_event_input_1", 50, 1);
                    checkPosition(c, "vanilla_anvil_layout", "vanilla_event_output", 108, 1);
                })
                .typeInto("#vanilla_anvil_level_cost text-field", "5").step("属性编辑写入费用", c -> {
                    c.check("经验费用修改成功", canvas(c).getData().getLevelCost() == 5);
                    c.check("JEI 费用预览同步更新", c.el("#vanilla_anvil_cost_preview").text().equals(
                            net.minecraft.network.chat.Component.translatable("container.repair.cost", 5).getString()));
                }).screenshot("anvil_editor").step("选择酿造并保存", c -> {
                    var entry = select(c, 1); var data = entry.<VanillaEventRecipeData>getData();
                    c.check("酿造画布保留具体药水组件", data.getInput().getItem().get(DataComponents.POTION_CONTENTS).potion().orElseThrow().is(Potions.WATER));
                    c.check("酿造画布保留输出", data.getResult().is(Items.EMERALD));
                    canvas(c).setVisualOutput(0, new ItemStack(Items.DIAMOND, 12)); canvas(c).save();
                    c.check("酿造输出固定为单件", data.getResult().getCount() == 1);
                }).checkNotExists("#vanilla_anvil_level_cost").checkVisible("#vanilla_event_input_1")
                .step("验证 JEI 酿造台槽位布局", c -> {
                    checkPosition(c, "vanilla_brewing_layout", "vanilla_event_input_0", 1, 37);
                    checkPosition(c, "vanilla_brewing_layout", "vanilla_brewing_bottle_1", 24, 44);
                    checkPosition(c, "vanilla_brewing_layout", "vanilla_brewing_bottle_2", 47, 37);
                    checkPosition(c, "vanilla_brewing_layout", "vanilla_event_input_1", 24, 3);
                    checkPosition(c, "vanilla_brewing_layout", "vanilla_event_output", 81, 3);
                    c.check("三个瓶槽保留同一药水组件", bottle(c, 0).getIngredient().toStack().get(DataComponents.POTION_CONTENTS)
                            .equals(bottle(c, 1).getIngredient().toStack().get(DataComponents.POTION_CONTENTS))
                            && ItemStack.isSameItemSameComponents(bottle(c, 1).getValue(), bottle(c, 2).getValue()));
                    bottle(c, 2).setItem(new ItemStack(Items.STICK), true); canvas(c).save();
                    c.check("任意瓶槽编辑同步输入条件", canvas(c).getData().getInput().toStack().is(Items.STICK)
                            && bottle(c, 0).getValue().is(Items.STICK) && bottle(c, 1).getValue().is(Items.STICK));
                }).click("#vanilla_brewing_bottle_1", 1).step("验证三个瓶槽同步清空", c -> {
                    canvas(c).save();
                    c.check("右键清空任意瓶槽清除输入条件", canvas(c).getData().getInput().isEmpty()
                            && bottle(c, 0).getValue().isEmpty() && bottle(c, 2).getValue().isEmpty());
                    canvas(c).setVisualIngredient(0, RecipeIngredient.tag("minecraft:planks"));
                }).ticks(25).step("验证标签轮播保持输入条件", c -> {
                    canvas(c).save();
                    c.check("瓶槽轮播不会把标签改成具体物品", canvas(c).getData().getInput().getKind() == IngredientValueKind.TAG
                            && bottle(c, 1).getIngredient().getKind() == IngredientValueKind.TAG
                            && bottle(c, 2).getIngredient().getKind() == IngredientValueKind.TAG);
                    canvas(c).setSelectedIngredient(RecipeIngredient.item(water())); canvas(c).save();
                })
                .hover("#vanilla_brewing_layout").screenshot("brewing_editor").closeScreen();
        scenario.server("事件配方增量替换", c -> {
            RecipeFile file = c.get("file"); file.getEntries().get(1).setOperation(RecipeOperation.REPLACE)
                    .<VanillaEventRecipeData>getData().setResult(new ItemStack(Items.REDSTONE)); save(file, c);
            var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
            c.check("只同步一条变更", !result.requiresFullSync() && result.delta().changedRecipeCount() == 1);
            c.check("服务器重载后立即使用新酿造输出", c.level().potionBrewing().mix(new ItemStack(Items.NETHER_WART), water()).is(Items.REDSTONE));
            RecipeReloadSyncService.syncDeltaToPlayers(c.server(), result.delta());
        }).waitUntil("客户端增量更新酿造规则", c -> c.level().potionBrewing().mix(new ItemStack(Items.NETHER_WART), water()).is(Items.REDSTONE))
                .server("删除事件规则恢复原版", c -> {
                    RecipeFile file = c.get("file"); file.getEntries().forEach(e -> e.setOperation(RecipeOperation.REMOVE)); save(file, c);
                    var result = RecipeOverrideManager.reloadDelta(c.server().getRecipeManager(), c.server().registryAccess());
                    c.check("事件规则已移除", file.getEntries().stream().allMatch(e -> c.server().getRecipeManager().byKey(e.getRecipeId()).isEmpty()));
                    c.check("移除后恢复原版药水结果", c.level().potionBrewing().mix(new ItemStack(Items.NETHER_WART), water())
                            .get(DataComponents.POTION_CONTENTS).potion().orElseThrow().is(Potions.AWKWARD));
                    RecipeReloadSyncService.syncDeltaToPlayers(c.server(), result.delta());
                }).waitUntil("客户端清除规则恢复酿造", c -> c.level().getRecipeManager().byKey(BREW_ID).isEmpty()
                        && !c.level().potionBrewing().isInput(new ItemStack(Items.STICK)));
        scenario.teardownServer("清理事件配方", c -> {
            try {Files.deleteIfExists(RecipeAssetPaths.recipeDirectory().resolve("vanilla-events-uitest.recipe"));} catch (Exception e) {throw new RuntimeException(e);}
            RecipeOverrideManager.reload(c.server().getRecipeManager(), c.server().registryAccess());
            GameType mode = c.get("gameMode"); if (mode != null) c.player().gameMode.changeGameModeForPlayer(mode);
            Integer levels = c.get("levels"); if (levels != null) c.player().giveExperienceLevels(levels - c.player().experienceLevel);
        });
    }

    private static RecipeFile fixtures() {
        var file = new RecipeFile();
        file.getEntries().add(new RecipeEntry().setType(VanillaEventRecipeEditorTypes.ANVIL).setRecipeId(ANVIL_ID).setOperation(RecipeOperation.ADD)
                .setData(new VanillaEventRecipeData().setLevelCost(3).setMaterialCost(2)));
        file.getEntries().add(new RecipeEntry().setType(VanillaEventRecipeEditorTypes.BREWING).setRecipeId(BREW_ID).setOperation(RecipeOperation.ADD)
                .setData(new VanillaEventRecipeData().setInput(RecipeIngredient.item(water())).setMaterial(RecipeIngredient.item(Items.NETHER_WART))
                        .setResult(new ItemStack(Items.EMERALD))));
        file.getEntries().add(new RecipeEntry().setType(VanillaEventRecipeEditorTypes.BREWING).setRecipeId(ViScriptRecipe.id("uitest/vanilla/custom_input"))
                .setOperation(RecipeOperation.ADD).setData(new VanillaEventRecipeData().setInput(RecipeIngredient.item(Items.STICK))));
        return file;
    }

    private static ItemStack water() {return PotionContents.createItemStack(Items.POTION, Potions.WATER);}

    private static void save(RecipeFile file, ServerContext c) {
        try {RecipeFileLoader.save(RecipeAssetPaths.recipeDirectory().resolve("vanilla-events-uitest.recipe"), file, c.server().registryAccess());}
        catch (Exception e) {throw new RuntimeException(e);}
    }

    private static RecipeEntry select(TestContext c, int index) {
        RecipeProject p = c.get("project"); NavigationView nav = c.getField(p, "navigationView");
        var entry = p.getRecipeFile().getEntries().get(index); nav.selectEntry(entry); p.saveCurrentVisualState();
        return entry;
    }

    private static VanillaEventRecipeCanvas canvas(TestContext c) {
        WorkBenchView work = c.getField(c.<RecipeProject>get("project"), "workbenchView");
        return (VanillaEventRecipeCanvas) work.getCanvas();
    }

    private static IngredientDisplaySlot bottle(TestContext c, int index) {
        return c.el(index == 0 ? "#vanilla_event_input_0" : "#vanilla_brewing_bottle_" + index).as(IngredientDisplaySlot.class);
    }

    private static void checkPosition(TestContext c, String panelId, String slotId, int x, int y) {
        var panel = c.el("#" + panelId).element(); var slot = c.el("#" + slotId).element();
        c.check(slotId + " 使用 JEI 槽位坐标", Math.abs(slot.getPositionX() - panel.getPositionX() - x) < 0.1
                && Math.abs(slot.getPositionY() - panel.getPositionY() - y) < 0.1
                && slot.getSizeWidth() == 16 && slot.getSizeHeight() == 16);
    }
}
