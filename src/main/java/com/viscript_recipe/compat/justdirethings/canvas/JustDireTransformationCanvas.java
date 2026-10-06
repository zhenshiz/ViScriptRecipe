package com.viscript_recipe.compat.justdirethings.canvas;

import com.direwolf20.justdirethings.setup.Registration;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.justdirethings.data.JustDireTransformationData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.List;
import java.util.function.Consumer;
import static com.viscript_recipe.compat.justdirethings.JustDireRecipeEditorTypes.key;
import static com.viscript_recipe.compat.justdirethings.canvas.JustDireCanvasLayout.*;

/** 编辑真实方块状态和方块标签；液体使用流体槽呈现。 */
public final class JustDireTransformationCanvas extends RecipeCanvas<JustDireTransformationData> {
    /**
     * 创建绑定到指定配方条目的画布。
     *
     * @param navigation 编辑器导航视图
     * @param entry 当前配方条目
     */
    public JustDireTransformationCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }
    private boolean fluid() { return entry.getType().getPath().equals("fluiddrop"); }
    private boolean tag() { return entry.getType().getPath().equals("goospread_tag"); }

    @Override public void load() {}
    @Override public void save() {} // 所有控件直接写入数据，不从循环预览反推材料。

    @Override
    public UIElement createCanvas() {
        var data = getData();
        var panel = panel(entry.getType().getPath(), 156, 70);
        int offset = fluid() ? 30 : 18;
        var input = tag() ? tagSlot() : stateSlot(data.getInput(), state -> change(() -> data.setInput(state)));
        var output = stateSlot(data.getOutput(), state -> change(() -> data.setOutput(state)));
        select(input, SlotSelection.ingredient(0)); select(output, SlotSelection.result(0));
        panel.addChildren(at(input.setId("justdire_input"), 9 + offset, 22, 18, 18),
                at(output.setId("justdire_output"), offset + (fluid() ? 68 : 88), 22, 18, 18), arrow(offset + (fluid() ? 34 : 54), 23));
        var catalyst = new ItemSlot(); skin(catalyst);
        if (fluid()) {
            catalyst.xeiPhantom(); catalyst.setItem(data.getCatalyst().getDefaultInstance(), false);
            catalyst.registerValueListener(stack -> data.setCatalyst(stack.getItem()));
            select(catalyst, SlotSelection.ingredient(1));
        } else {
            catalyst.bindDataSource(SupplierDataSource.of(() -> {
                var tiers = List.of(Registration.GooBlock_Tier1.get(), Registration.GooBlock_Tier2.get(), Registration.GooBlock_Tier3.get(), Registration.GooBlock_Tier4.get());
                int first = Math.clamp(data.getTier() - 1, 0, 3);
                return new ItemStack(tiers.get(first + (int) ((System.currentTimeMillis() / 1000) % (4 - first))));
            }));
            catalyst.addEventListener(UIEvents.MOUSE_DOWN, e -> { selectRecipe(); e.stopPropagation(); });
        }
        panel.addChild(at(catalyst.setId("justdire_catalyst"), offset + (fluid() ? 9 : 29), fluid() ? 0 : 22, 18, 18));
        panel.addChild(at(RecipeEditorUi.label(summary()).bindDataSource(SupplierDataSource.of(this::summary)).setId("justdire_summary"), 0, 52, 156, 14));
        return centered(panel);
    }

    private Component summary() {
        return fluid() ? Component.translatable(key("source_conversion"))
                : Component.translatable(key("goo_summary"), getData().getTier(), getData().getDuration());
    }

    private UIElement tagSlot() {
        var slot = new ItemSlot(); skin(slot);
        slot.bindDataSource(SupplierDataSource.of(() -> {
            var values = BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, getData().getInputTag()));
            if (values.isEmpty() || values.get().size() == 0) return ItemStack.EMPTY;
            return new ItemStack(values.get().get((int) ((System.currentTimeMillis() / 1000) % values.get().size())).value());
        }));
        return slot.style(s -> s.tooltips(Component.literal("#" + getData().getInputTag())));
    }

    private UIElement stateSlot(BlockState state, Consumer<BlockState> update) {
        if (state.getBlock() instanceof LiquidBlock) {
            var slot = new FluidSlot().xeiPhantom();
            slot.setFluid(new FluidStack(state.getFluidState().getType(), 1000), false);
            slot.registerValueListener(stack -> {
                if (!stack.isEmpty()) update.accept(stack.getFluid().defaultFluidState().createLegacyBlock());
            });
            return slot;
        }
        var slot = new ItemSlot().xeiPhantom(); skin(slot);
        slot.setItem(new ItemStack(state.getBlock()), false);
        slot.registerValueListener(stack -> {
            if (stack.getItem() instanceof BlockItem block) update.accept(block.getBlock().defaultBlockState());
            else slot.setItem(new ItemStack(state.getBlock()), false);
        });
        return slot.style(s -> s.tooltips(Component.literal(state.toString())));
    }

    private static void select(UIElement element, SlotSelection selection) {
        element.addEventListener(UIEvents.MOUSE_DOWN, e -> { selectSlot(selection); e.stopPropagation(); });
    }

    private void change(Runnable update) {
        update.run(); navigationView.loadCanvas(); reloadProperties();
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        if (fluid()) content.addChild(RecipeEditorUi.label(Component.translatable(key("fluid_hint"))));
        else content.addChildren(
                intField(key("tier"), getData().getTier(), 1, 4, getData()::setTier).setId("justdire_tier"),
                intField(key("duration"), getData().getDuration(), 1, Integer.MAX_VALUE, getData()::setDuration).setId("justdire_duration"));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        if (fluid() && selectedSlotIndex() == 1) {
            content.addChild(PropertiesView.createItemStackConfigurator(key("catalyst"), () -> getData().getCatalyst().getDefaultInstance(),
                    stack -> change(() -> getData().setCatalyst(stack.getItem()))));
        } else if (tag()) {
            content.addChild(RecipeSearchComponents.blockTag(key("block_tag"), getData()::getInputTag, getData()::setInputTag,
                    () -> { navigationView.loadCanvas(); reloadProperties(); }).setId("justdire_block_tag"));
        } else buildState(content, true);
    }

    @Override public void buildResultProperties(UIElement content) { buildState(content, false); }

    private void buildState(UIElement content, boolean input) {
        var state = input ? getData().getInput() : getData().getOutput();
        Consumer<BlockState> setter = value -> { if (input) getData().setInput(value); else getData().setOutput(value); };
        content.addChild(RecipeSearchComponents.block(key(input ? "input_block" : "output_block"),
                () -> BuiltInRegistries.BLOCK.getKey((input ? getData().getInput() : getData().getOutput()).getBlock()),
                id -> setter.accept(BuiltInRegistries.BLOCK.get(id).defaultBlockState()),
                () -> { navigationView.loadCanvas(); reloadProperties(); }, state.getBlock()).setId("justdire_block"));
        if (input && fluid()) content.addChild(RecipeEditorUi.label(Component.translatable(key("source_only"))));
        for (var property : state.getProperties()) addProperty(content, state, property, setter);
    }

    private <T extends Comparable<T>> void addProperty(UIElement content, BlockState state, Property<T> property, Consumer<BlockState> setter) {
        content.addChild(selector(property.getName(), List.copyOf(property.getPossibleValues()), state.getValue(property),
                value -> Component.literal(property.getName(value)), value -> change(() -> setter.accept(state.setValue(property, value))),
                Component.literal(property.getName())).setId("justdire_state_" + property.getName()));
    }
}
