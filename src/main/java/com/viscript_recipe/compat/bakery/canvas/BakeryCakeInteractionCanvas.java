package com.viscript_recipe.compat.bakery.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.viscript_recipe.compat.bakery.data.BakeryCakeInteractionRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.*;
import com.viscript_recipe.gui.views.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.bakery.core.block.cake.BlankCakeBlock;
import net.satisfy.bakery.core.registry.ObjectRegistry;

import java.util.List;

import static com.viscript_recipe.compat.farm_and_charm.canvas.FarmCharmCanvasFactory.*;

/** 展示实际蛋糕方块的加工前后形态，交互字段使用现有属性栏编辑。 */
public final class BakeryCakeInteractionCanvas extends RecipeCanvas<BakeryCakeInteractionRecipeData> {
    private int alternative;

    /**
     * 创建指定蛋糕交互配方的画布。
     * @param navigation 配方导航视图
     * @param entry 当前配方条目
     */
    public BakeryCakeInteractionCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        var body = panel("bakery_cake_interaction", 216, 110);
        var before = scene(inputState()).setId("bakery_cake_before");
        before.addEventListener(UIEvents.MOUSE_DOWN, e -> { selectRecipe(); e.stopPropagation(); });
        var after = scene(outputState()).setId("bakery_cake_after");
        after.addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(SlotSelection.result(0)); e.stopPropagation(); });
        var returned = createExtraItemSlot(JEI_SLOT_SIZE, Component.translatable(key("return_item")));
        returned.registerValueListener(stack -> {
            getData().setReturnItem(!stack.isEmpty());
            if (!stack.isEmpty()) getData().setItem(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        });
        body.addChildren(at(before, 5, 20, 80, 80), at(after, 131, 20, 80, 80),
                at(new UIElement().style(s -> s.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90))), 95, 56, 26, 16),
                at(createIngredientSlot(0, SLOT_SIZE).setId("bakery_cake_material"), 96, 10, 24, 24),
                at(returned.setId("bakery_cake_return"), 162, 89, 18, 18));
        body.addChild(at(RecipeEditorUi.label(stageName(getData().getStage())), 0, 0, 80, 14));
        return centered(body);
    }

    private static Scene scene(BlockState state) {
        var world = new TrackedDummyWorld();
        world.addBlock(BlockPos.ZERO, new BlockInfo(state));
        return new Scene().createScene(world).setRenderFacing(false).setRenderSelect(false)
                .setDraggable(false).setScalable(false).setIntractable(false).setTickWorld(false)
                .useOrtho().useCacheBuffer().setRenderedCore(List.of(BlockPos.ZERO)).setZoom(1f);
    }

    private BlockState inputState() {
        return ObjectRegistry.BLANK_CAKE.get().defaultBlockState()
                .setValue(BlankCakeBlock.CAKE, getData().getStage().equals("CAKE"))
                .setValue(BlankCakeBlock.CUPCAKE, getData().getStage().equals("CUPCAKE"))
                .setValue(BlankCakeBlock.COOKIE, getData().getStage().equals("COOKIE"));
    }

    private BlockState outputState() {
        if (getData().isReplaceBlock()) return BuiltInRegistries.BLOCK.get(getData().getBlock()).defaultBlockState();
        if (getData().isPatchState()) return inputState().setValue(BlankCakeBlock.CAKE, getData().isCake())
                .setValue(BlankCakeBlock.CUPCAKE, getData().isCupcake()).setValue(BlankCakeBlock.COOKIE, getData().isCookie());
        return inputState();
    }

    @Override
    public void load() {
        var values = getData().getInput().getAlternatives();
        alternative = Math.min(alternative, Math.max(0, values.size() - 1));
        loadIngredientSlot(0, values.isEmpty() ? RecipeIngredient.empty() : values.get(alternative));
        setExtraItem(getData().isReturnItem() ? new ItemStack(BuiltInRegistries.ITEM.get(getData().getItem())) : ItemStack.EMPTY);
    }

    @Override
    public void save() {
        var values = getData().getInput().getAlternatives();
        var current = getVisualIngredient(0);
        if (!current.isEmpty() || !values.isEmpty()) {
            while (values.size() <= alternative) values.add(RecipeIngredient.empty());
            values.set(alternative, current);
        }
        var returned = getExtraItem();
        if (!returned.isEmpty()) getData().setItem(BuiltInRegistries.ITEM.getKey(returned.getItem()));
    }

    private void change(Runnable update) {
        save(); update.run(); navigationView.loadCanvas(); reloadProperties();
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        content.addChild(intField("viscript_recipe.editor.farm_and_charm.alternative", alternative + 1, 1,
                Math.max(1, getData().getInput().getAlternatives().size() + 1), value -> {
                    save(); alternative = value - 1;
                    var values = getData().getInput().getAlternatives();
                    while (values.size() <= alternative) values.add(RecipeIngredient.empty());
                    loadIngredientSlot(0, values.get(alternative)); reloadProperties();
                }).setId("bakery_cake_alternative"));
        super.buildIngredientProperties(content);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        var data = getData();
        content.addChild(switchField(key("replace_block"), data.isReplaceBlock(), value -> change(() -> data.setReplaceBlock(value)))
                .setId("bakery_replace_block"));
        if (data.isReplaceBlock()) content.addChild(RecipeSearchComponents.block(key("output_block"), data::getBlock,
                data::setBlock, () -> change(() -> {}), ObjectRegistry.CHOCOLATE_CAKE.get())
                .setId("bakery_output_block"));
        content.addChild(switchField(key("patch_state"), data.isPatchState(), value -> change(() -> data.setPatchState(value)),
                Component.translatable(key("patch_state_hint"))).setId("bakery_patch_state"));
        if (data.isPatchState()) content.addChildren(
                switchField(key("stage.cake"), data.isCake(), v -> change(() -> data.setCake(v))).setId("bakery_patch_cake"),
                switchField(key("stage.cupcake"), data.isCupcake(), v -> change(() -> data.setCupcake(v))).setId("bakery_patch_cupcake"),
                switchField(key("stage.cookie"), data.isCookie(), v -> change(() -> data.setCookie(v))).setId("bakery_patch_cookie"));
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChild(switchField(key("return_item"), getData().isReturnItem(), value -> change(() -> getData().setReturnItem(value)))
                .setId("bakery_return_item"));
        if (getData().isReturnItem()) content.addChild(PropertiesView.removeCountConfig(PropertiesView.createItemStackConfigurator(
                key("returned_item"), this::getExtraItem, stack -> {
                    if (!stack.isEmpty()) getData().setItem(BuiltInRegistries.ITEM.getKey(stack.getItem()));
                    else getData().setReturnItem(false);
                    setExtraItem(stack);
                })));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(selector(key("stage"), List.of("CAKE", "CUPCAKE", "COOKIE"), data.getStage(),
                        BakeryCakeInteractionCanvas::stageName, value -> change(() -> data.setStage(value))).setId("bakery_stage"),
                intField(key("priority"), data.getPriority(), Integer.MIN_VALUE, Integer.MAX_VALUE, data::setPriority,
                        Component.translatable(key("priority_hint"))).setId("bakery_priority"),
                switchField(key("consume_one"), data.isConsumeOne(), data::setConsumeOne).setId("bakery_consume_one"),
                switchField(key("particles"), data.isParticles(), data::setParticles).setId("bakery_particles"),
                intField(key("cooldown"), data.getCooldownTicks(), 0, Integer.MAX_VALUE, data::setCooldownTicks).setId("bakery_cooldown"),
                switchField(key("play_sound"), data.isPlaySound(), value -> change(() -> data.setPlaySound(value))).setId("bakery_play_sound"));
        if (data.isPlaySound()) content.addChild(resourceField(key("sound"), data.getSound(), data::setSound).setId("bakery_sound"));
    }

    private static String key(String path) { return "viscript_recipe.editor.bakery." + path; }
    private static Component stageName(String stage) { return Component.translatable(key("stage." + stage.toLowerCase(java.util.Locale.ROOT))); }
}
