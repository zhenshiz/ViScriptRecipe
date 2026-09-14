package com.viscript_recipe.compat.ae2.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.ae2.data.Ae2EntropyRecipeData;
import com.viscript_recipe.compat.ae2.data.Ae2StatePropertyData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.ae2.canvas.Ae2CanvasFactory.*;

/** Edits entropy block/fluid conditions, output states, and additional item drops. */
public class EntropyCanvas extends RecipeCanvas<Ae2EntropyRecipeData> {
    private static final int PAGE_SIZE = 8;
    private int page;
    private int dropSlots;
    public EntropyCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public UIElement createCanvas() {
        var data = getData();
        dropSlots = Math.min(PAGE_SIZE, Math.max(1, data.getDrops().size() - page * PAGE_SIZE + 1));
        int width = Math.max(150, 111 + Math.min(4, dropSlots) * 18);
        var panel = panel("ae2_entropy", width, 80).addChildren(
                label(Component.translatable("viscript_recipe.editor.ae2." + (data.isHeat() ? "heat" : "cool")), 29, 0, 100),
                worldSlot(true, true, 28, 14), worldSlot(false, true, 84, 14), arrow(53, 15),
                worldSlot(true, false, 28, 38), worldSlot(false, false, 84, 38),
                label(Component.translatable("viscript_recipe.editor.ae2.block"), 0, 17, 28),
                label(Component.translatable("viscript_recipe.editor.ae2.fluid"), 0, 41, 28),
                label(Component.translatable("viscript_recipe.editor.ae2.drops"), 111, 1, 72),
                label(Component.translatable("viscript_recipe.editor.ae2.entropy_hint"), 0, 65, width));
        for (int i = 0; i < dropSlots; i++) {
            panel.addChild(standardSlot(createOutputSlot(i, 18), "ae2_drop_" + i, 111 + i % 4 * 18, 14 + i / 4 * 24));
        }
        return centered(panel);
    }

    private UIElement worldSlot(boolean input, boolean block, int x, int y) {
        var data = getData();
        ResourceLocation id = block ? (input ? data.getInputBlock() : data.getOutputBlock())
                : (input ? data.getInputFluid() : data.getOutputFluid());
        boolean enabled = block ? (input ? data.isInputBlockEnabled() : data.isOutputBlockEnabled())
                : (input ? data.isInputFluidEnabled() : data.isOutputFluidEnabled());
        ItemStack stack = enabled ? (block ? BuiltInRegistries.BLOCK.get(id).asItem().getDefaultInstance()
                : BuiltInRegistries.FLUID.get(id).getBucket().getDefaultInstance()) : ItemStack.EMPTY;
        var slot = new ItemSlot().xeiPhantom().setItem(stack, false);
        tooltip(slot, Component.translatable("viscript_recipe.editor.ae2." + (input ? "input_" : "output_") + (block ? "block" : "fluid")),
                enabled ? Component.literal(id.toString()) : Component.translatable("viscript_recipe.editor.ae2." + (input ? "unconstrained" : "unchanged")));
        slot.registerValueListener(value -> {
            if (block && value.getItem() instanceof BlockItem item) change(() -> {
                if (input) data.setInputBlockEnabled(true).setInputBlock(BuiltInRegistries.BLOCK.getKey(item.getBlock())).getInputBlockProperties().clear();
                else data.setOutputBlockEnabled(true).setOutputBlock(BuiltInRegistries.BLOCK.getKey(item.getBlock())).getOutputBlockProperties().clear();
            });
            else if (!block && value.getItem() instanceof BucketItem bucket) change(() -> {
                if (input) data.setInputFluidEnabled(true).setInputFluid(BuiltInRegistries.FLUID.getKey(bucket.content)).getInputFluidProperties().clear();
                else data.setOutputFluidEnabled(true).setOutputFluid(BuiltInRegistries.FLUID.getKey(bucket.content)).getOutputFluidProperties().clear();
            });
            else slot.setItem(stack, false);
        });
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> { selectRecipe(); event.stopPropagation(); });
        return standardSlot(slot, "ae2_" + (input ? "input_" : "output_") + (block ? "block" : "fluid"), x, y);
    }

    @Override
    public void load() {
        for (int i = 0; i < dropSlots; i++) {
            int index = page * PAGE_SIZE + i;
            setVisualOutput(i, index < getData().getDrops().size() ? getData().getDrops().get(index) : ItemStack.EMPTY);
        }
    }

    @Override
    public void save() {
        var drops = new ArrayList<>(getData().getDrops());
        while (drops.size() < page * PAGE_SIZE + dropSlots) drops.add(ItemStack.EMPTY);
        for (int i = 0; i < dropSlots; i++) drops.set(page * PAGE_SIZE + i, getVisualOutput(i).getItem());
        while (!drops.isEmpty() && drops.getLast().isEmpty()) drops.removeLast();
        getData().setDrops(drops);
    }

    private void change(Runnable change) {
        save(); change.run(); clearAllChildren(); initVisualState(); load(); reloadProperties();
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.type.ae2.entropy"),
                switchField("viscript_recipe.editor.ae2.heat", data.isHeat(), heat -> change(() -> data.setHeat(heat)),
                        Component.translatable("viscript_recipe.editor.ae2.heat_hint")).setId("ae2_heat"));
        blockProperties(content, true);
        fluidProperties(content, true);
        blockProperties(content, false);
        fluidProperties(content, false);
        content.addChild(intField("viscript_recipe.editor.ae2.drops_page", page + 1, 1,
                Math.max(page + 2, data.getDrops().size() / PAGE_SIZE + 2), value -> change(() -> page = value - 1)).setId("ae2_drops_page"));
    }

    private void blockProperties(UIElement content, boolean input) {
        var data = getData();
        String key = "viscript_recipe.editor.ae2." + (input ? "input_block" : "output_block");
        boolean enabled = input ? data.isInputBlockEnabled() : data.isOutputBlockEnabled();
        content.addChild(switchField(key, enabled, value -> change(() -> {
            if (input) data.setInputBlockEnabled(value); else data.setOutputBlockEnabled(value);
        }), Component.translatable("viscript_recipe.editor.ae2." + (input ? "unconstrained" : "unchanged"))));
        if (!enabled) return;
        ResourceLocation id = input ? data.getInputBlock() : data.getOutputBlock();
        var rows = input ? data.getInputBlockProperties() : data.getOutputBlockProperties();
        content.addChild(PropertiesView.createBlockConfigurator(key, () -> BuiltInRegistries.BLOCK.get(id), block -> change(() -> {
            if (input) data.setInputBlock(BuiltInRegistries.BLOCK.getKey(block));
            else data.setOutputBlock(BuiltInRegistries.BLOCK.getKey(block));
            rows.clear();
        })));
        if (!input) content.addChild(switchField("viscript_recipe.editor.ae2.keep_properties", data.isKeepBlockProperties(), data::setKeepBlockProperties,
                Component.translatable("viscript_recipe.editor.ae2.air_hint")));
        stateProperties(content, BuiltInRegistries.BLOCK.get(id).getStateDefinition(), rows, input);
    }

    private void fluidProperties(UIElement content, boolean input) {
        var data = getData();
        String key = "viscript_recipe.editor.ae2." + (input ? "input_fluid" : "output_fluid");
        boolean enabled = input ? data.isInputFluidEnabled() : data.isOutputFluidEnabled();
        content.addChild(switchField(key, enabled, value -> change(() -> {
            if (input) data.setInputFluidEnabled(value); else data.setOutputFluidEnabled(value);
        }), Component.translatable("viscript_recipe.editor.ae2." + (input ? "unconstrained" : "unchanged"))));
        if (!enabled) return;
        ResourceLocation id = input ? data.getInputFluid() : data.getOutputFluid();
        var rows = input ? data.getInputFluidProperties() : data.getOutputFluidProperties();
        content.addChild(PropertiesView.removeCountConfig(PropertiesView.createFluidStackConfigurator(key,
                () -> new FluidStack(BuiltInRegistries.FLUID.get(id), 1000), fluid -> change(() -> {
                    if (input) data.setInputFluid(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
                    else data.setOutputFluid(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
                    rows.clear();
                }))));
        if (!input) content.addChild(switchField("viscript_recipe.editor.ae2.keep_properties", data.isKeepFluidProperties(), data::setKeepFluidProperties));
        stateProperties(content, BuiltInRegistries.FLUID.get(id).getStateDefinition(), rows, input);
    }

    private static void stateProperties(UIElement content, StateDefinition<?, ?> definition, List<Ae2StatePropertyData> rows, boolean input) {
        for (var property : definition.getProperties()) {
            var stored = rows.stream().filter(row -> property.getName().equals(row.getName())).findFirst();
            var values = property.getPossibleValues().stream().map(value -> valueName(property, value)).toList();
            content.addChild(switchField(property.getName(), stored.isPresent(), enabled -> {
                if (enabled) rows.add(new Ae2StatePropertyData().setName(property.getName()).setValue(values.getFirst())
                        .setValues(new ArrayList<>(List.of(values.getFirst()))).setMin(values.getFirst()).setMax(values.getLast()));
                else rows.removeIf(row -> property.getName().equals(row.getName()));
                reloadProperties();
            }));
            if (stored.isEmpty()) continue;
            var row = stored.get();
            if (input) content.addChild(selector("viscript_recipe.editor.ae2.match_mode", List.of(Ae2StatePropertyData.Mode.values()), row.getMode(),
                    mode -> Component.translatable("viscript_recipe.editor.ae2.match." + mode.name().toLowerCase(java.util.Locale.ROOT)),
                    mode -> {
                        row.setMode(mode);
                        if (row.getValue().isEmpty()) row.setValue(values.getFirst());
                        if (row.getMin().isEmpty()) row.setMin(values.getFirst());
                        if (row.getMax().isEmpty()) row.setMax(values.getLast());
                        reloadProperties();
                    }));
            if (!input || row.getMode() == Ae2StatePropertyData.Mode.SINGLE) {
                content.addChild(selector("viscript_recipe.editor.ae2.property_value", values, row.getValue(), Component::literal, row::setValue));
            } else if (row.getMode() == Ae2StatePropertyData.Mode.RANGE) {
                content.addChildren(selector("viscript_recipe.editor.ae2.range_min", values, row.getMin(), Component::literal, row::setMin),
                        selector("viscript_recipe.editor.ae2.range_max", values, row.getMax(), Component::literal, row::setMax));
            } else {
                for (var value : values) content.addChild(switchField(value, row.getValues().contains(value), enabled -> {
                    if (enabled && !row.getValues().contains(value)) row.getValues().add(value);
                    else if (!enabled) row.getValues().remove(value);
                }));
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String valueName(Property property, Comparable value) { return property.getName(value); }
}
