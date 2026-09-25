package com.viscript_recipe.compat.immersive_engineering.canvas;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.viscript_recipe.compat.immersive_engineering.data.IERecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.gui.canvas.FluidRecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.viscript_recipe.compat.immersive_engineering.canvas.IECanvasVisuals.*;

/**
 * Edits IE recipes in the coordinate system of their native JEI categories.
 * Unused variable slots remain accessible below the native recipe, without moving its contents.
 */
public final class IECanvas extends FluidRecipeCanvas<IERecipeData> {
    private int itemInputs;
    private int itemOutputs;
    private int fluidInputs;
    private boolean fluidOutput;
    private boolean creosoteOutput;
    private List<Object> layoutState = List.of();
    private final List<Integer> placedInputs = new ArrayList<>();
    private final List<Integer> placedOutputs = new ArrayList<>();

    public IECanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override public UIElement createCanvas() {
        itemInputs = itemOutputs = fluidInputs = 0;
        fluidOutput = creosoteOutput = false;
        placedInputs.clear();
        placedOutputs.clear();
        var kind = entry.getType().getPath();
        var panel = panel("ie_" + kind, 176, 77);
        int width = 176, height = 77;
        switch (kind) {
            case "alloy" -> {
                width = 106; height = 56; itemInputs = 2; itemOutputs = 1;
                background(panel, "alloy_smelter", 36, 15, width, height);
                in(panel, 0, 2, 2, false); in(panel, 1, 30, 2, false); out(panel, 0, 84, 20, false);
                image(panel, sprite("alloy_smelter", 177, 0, 14, 14), 18, 21, 14, 14);
                image(panel, sprite("alloy_smelter", 176, 14, 24, 17), 47, 20, 24, 17);
            }
            case "arc_furnace" -> {
                width = 166; height = 72; itemInputs = getData().isRecycling() ? 1 : 5;
                itemOutputs = getData().isRecycling() ? 4 : 11;
                var main = new ArrayList<>(outputs(0, 4, true));
                if (!getData().isRecycling()) main.addAll(outputs(9, 11, false));
                var secondary = getData().isRecycling() ? List.<Integer>of() : outputs(5, 9, false);
                var recycled = getData().isRecycling() ? recyclingPreview() : List.<RecyclingPreview>of();
                int outputCount = getData().isRecycling() ? recycled.size() : main.size();
                int recipeWidth = 86 + (outputCount > 1 ? 18 : 0) + (secondary.isEmpty() ? 0 : 40);
                int x = (148 - recipeWidth) / 2;
                in(panel, 0, x + 9, 1, true);
                if (!getData().isRecycling())
                    for (int i = 0; i < 4; i++) in(panel, i + 1, x + 1 + i % 2 * 18, 19 + i / 2 * 18, true);
                if (getData().isRecycling()) {
                    for (int i = 0; i < recycled.size(); i++) {
                        var preview = recycled.get(i);
                        var slot = new ItemSlot();
                        slot.setValue(preview.stack(), false);
                        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> selectSlot(com.viscript_recipe.gui.editor.SlotSelection.result(preview.index())));
                        slot(panel, slot, "ie_output_preview_" + i, x + 69 + i % 2 * 18, i / 2 * 18 + 1, true);
                    }
                    for (int i = 0; i < 4; i++) imageBehindSlot(panel, x + 1 + i % 2 * 18, 19 + i / 2 * 18);
                    if (outputCount <= 4) imageBehindSlot(panel, x + 69, 37);
                } else {
                    for (int i = 0; i < main.size(); i++) out(panel, main.get(i), x + 69 + i % 2 * 18, i / 2 * 18 + 1, true);
                }
                int sx = x + (main.size() > 1 ? 106 : 88);
                for (int i = 0; i < secondary.size(); i++) {
                    int index = secondary.get(i);
                    out(panel, index, sx + 1, i * 18 + 1, true);
                    chance(panel, index, sx + 20, i * 18 + 6);
                }
                if (!getData().isRecycling() && main.size() <= 4) out(panel, 4, x + 69, 37, true);
                image(panel, sprite("jei_elements", 19, 4, 24, 18), x + 40, 10, 24, 18);
                height = Math.max(54, Math.max(secondary.size() * 18, ((outputCount + 1) / 2) * 18));
            }
            case "blast_furnace_fuel" -> {
                width = 86; height = 36; itemInputs = 1;
                image(panel, vanillaFurnace(55, 36, 18, 36), 0, 0, 18, 36);
                image(panel, vanillaFurnace(176, 0, 14, 14), 1, 0, 14, 14);
                in(panel, 0, 1, 17, false);
                text(panel, () -> seconds(getData().getTime()), 24, 12, false);
            }
            case "blast_furnace" -> {
                width = 100; height = 64; itemInputs = 1; itemOutputs = 2;
                background(panel, "blast_furnace", 42, 9, width, height);
                in(panel, 0, 10, 8, false); out(panel, 0, 70, 8, false); out(panel, 1, 70, 44, false);
                image(panel, sprite("blast_furnace", 177, 0, 14, 14), 12, 27, 14, 14);
                image(panel, sprite("blast_furnace", 176, 14, 24, 17), 33, 26, 24, 17);
                text(panel, () -> seconds(getData().getTime()), 68, 48, true);
            }
            case "blueprint" -> {
                width = 176; height = 54; itemInputs = 6; itemOutputs = 1;
                background(panel, "workbench", 0, 11, width, height);
                imageBehindSlot(panel, 25, 6);
                image(panel, IGuiTexture.dynamic(() -> new ItemStackTexture(BlueprintCraftingRecipe.getTypedBlueprint(getData().getBlueprintCategory()))), 25, 6, 16, 16);
                var inputs = inputs(0, 6, true);
                int y = inputs.size() <= 4 ? 13 : 1;
                for (int i = 0; i < inputs.size(); i++) in(panel, inputs.get(i), 81 + i % 2 * 18, y + i / 2 * 18, true);
                out(panel, 0, 141, 15, true);
            }
            case "bottling_machine" -> {
                width = 120; height = 56; itemInputs = itemOutputs = 4;
                var inputs = inputs(0, 4, true); var outputs = outputs(0, 4, true);
                int y = 29 - Math.min(inputs.size(), 3) * 9;
                for (int i = 0; i < inputs.size(); i++) in(panel, inputs.get(i), 1, y + i * 18, true);
                y = 29 - Math.min(outputs.size(), 3) * 9;
                for (int i = 0; i < outputs.size(); i++) out(panel, outputs.get(i), 101, y + i * 18, true);
                tankFrame(panel, 24, 2, 16, 52);
                fluidIn(panel, 24, 2, 16, 52, 1000, null);
                machine(panel, 42, 0);
                height = Math.max(height, 2 + Math.max(inputs.size(), outputs.size()) * 18);
            }
            case "fertilizer" -> {
                width = 150; height = 50; itemInputs = 1;
                machine(panel, -3, 3);
                image(panel, new ItemStackTexture(IEItems.Misc.FERTILIZER.get().getDefaultInstance()), 19, 25, 29, 29);
                in(panel, 0, 33, 13, true);
                text(panel, () -> Component.translatable("desc.immersiveengineering.jei.cloche_modifier", number(getData().getGrowthModifier())), 53, 17, false);
            }
            case "cloche" -> {
                itemInputs = 2; itemOutputs = 4;
                background(panel, "cloche", 0, 0, width, height);
                in(panel, 0, 62, 34, false); in(panel, 1, 62, 54, false);
                for (int i = 0; i < 4; i++) {
                    final int index = i;
                    out(panel, i, 116 + i % 2 * 18, 34 + i / 2 * 18, false);
                    visualOutputSlots[i].style(style -> style.backgroundTexture(IGuiTexture.dynamic(() ->
                            getVisualOutput(index).getChance() < 1 ? sprite("jei_elements", 0, 59, 18, 18) : IGuiTexture.EMPTY)));
                }
                fluidIn(panel, 6, 6, 20, 51, 4000, sprite("cloche", 176, 30, 20, 51));
                image(panel, sprite("cloche", 181, 1, 13, 13), 101, 35, 13, 13);
                var fertilizers = new ArrayList<net.minecraft.world.item.ItemStack>();
                fertilizers.add(net.minecraft.world.item.ItemStack.EMPTY);
                var level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) {
                    for (var holder : blusunrize.immersiveengineering.api.crafting.ClocheFertilizer.RECIPES.getRecipes(level))
                        fertilizers.addAll(java.util.Arrays.asList(holder.value().input.getItems()));
                }
                image(panel, new ItemStackTexture(fertilizers.toArray(net.minecraft.world.item.ItemStack[]::new)), 8, 59, 16, 16);
            }
            case "coke_oven" -> {
                width = 123; height = 55; itemInputs = itemOutputs = 1;
                background(panel, "coke_oven", 26, 16, width, height);
                in(panel, 0, 4, 19, false); out(panel, 0, 59, 19, false);
                image(panel, sprite("coke_oven", 177, 0, 14, 14), 31, 20, 14, 14);
                creosoteOutput = true;
                fluidOut(panel, 103, 4, 16, 47, 12000, sprite("coke_oven", 178, 33, 16, 47));
            }
            case "crusher" -> {
                width = 172; height = 54; itemInputs = 1; itemOutputs = 5;
                machine(panel, 24, 0); in(panel, 0, 2, 20, true);
                var secondary = outputs(1, 5, false);
                int y = 1 + (secondary.isEmpty() ? 18 : secondary.size() < 2 ? 9 : 0);
                out(panel, 0, 78, y, true);
                for (int i = 0; i < secondary.size(); i++) {
                    int index = secondary.get(i), sx = 78 + i / 2 * 44, sy = y + 18 + i % 2 * 18;
                    out(panel, index, sx, sy, true); chance(panel, index, sx + 20, sy + 5);
                }
            }
            case "fermenter", "squeezer" -> {
                width = 126; height = 59; itemInputs = itemOutputs = 1;
                background(panel, kind, 6, 12, width, height);
                in(panel, 0, 2, kind.equals("fermenter") ? 7 : 23, false); out(panel, 0, 85, 41, false);
                fluidOut(panel, 106, 9, 16, 47, 250, sprite(kind, 179, 33, 16, 47));
            }
            case "metal_press" -> {
                width = 100; height = 50; itemInputs = 2; itemOutputs = 1;
                machine(panel, 15, 0);
                in(panel, 0, 1, 13, true); in(panel, 1, 57, 1, true); out(panel, 0, 83, 13, true);
            }
            case "mixer" -> {
                width = 157; height = 60; itemInputs = 6;
                image(panel, sprite("mixer", 68, 8, 74, 60), 40, 0, 74, 60);
                image(panel, sprite("mixer", 178, 17, 18, 13), 117, 19, 18, 13);
                fluidIn(panel, 48, 3, 58, 47, 2000, null);
                tankFrame(panel, 139, 3, 16, 47);
                fluidOut(panel, 139, 3, 16, 47, 2000, sprite("mixer", 179, 33, 16, 47));
                var inputs = inputs(0, 6, true);
                for (int i = 0; i < inputs.size(); i++) in(panel, inputs.get(i), 1 + i % 2 * 18, 1 + i / 2 * 18, true);
            }
            case "refinery" -> {
                width = 125; height = 62; itemInputs = 1;
                background(panel, "refinery", 6, 10, width, height);
                var overlay = sprite("refinery", 179, 33, 16, 47);
                fluidIn(panel, 7, 10, 16, 47, 50, overlay); fluidIn(panel, 34, 10, 16, 47, 50, overlay);
                in(panel, 0, 67, 16, false); fluidOut(panel, 103, 10, 16, 47, 50, overlay);
            }
            case "sawmill" -> {
                width = 128; height = 64; itemInputs = 1; itemOutputs = 10;
                image(panel, sprite("jei_elements", 0, 0, 114, 26), 2, 2, 114, 26);
                boolean stripping = !outputs(1, 2, false).isEmpty();
                if (!stripping) image(panel, sprite("jei_elements", 0, 26, 29, 16), 36, 7, 29, 16);
                image(panel, sprite("jei_elements", 29, stripping ? 26 : 42, 66, stripping ? 16 : 17), 22, 6, 66, stripping ? 16 : 17);
                in(panel, 0, 3, 7, false); out(panel, 0, 95, 7, false);
                if (stripping) out(panel, 1, 47, 7, false);
                var stripped = outputs(2, 6, false); var secondary = outputs(6, 10, false);
                for (int i = 0; i < stripped.size(); i++) out(panel, stripped.get(i), 47 + i % 2 * 18, 29 + i / 2 * 18, false);
                for (int i = 0; i < secondary.size(); i++) out(panel, secondary.get(i), 91 + i % 2 * 18, 29 + i / 2 * 18, true);
            }
            default -> throw new IllegalArgumentException("Unsupported IE editor canvas: " + kind);
        }
        var nativePanel = panel;
        int nativeWidth = width, nativeHeight = height;
        // Keep a distinct native rectangle for comparison with JEI and optional editor-only slots.
        nativePanel.layout(layout -> { layout.width(nativeWidth); layout.height(nativeHeight); })
                .style(style -> style.backgroundTexture(new com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture(0xFFC6C6C6)));
        var wrapper = panel("ie_editor", Math.max(176, width), height + 76);
        at(panel, (Math.max(176, width) - width) / 2, 0, width, height);
        wrapper.addChild(panel);
        int y = height + 8;
        y = spareSlots(wrapper, true, y);
        y = spareSlots(wrapper, false, y);
        int finalHeight = y;
        wrapper.layout(layout -> layout.height(finalHeight));
        return centered(wrapper);
    }

    private record RecyclingPreview(int index, net.minecraft.world.item.ItemStack stack) {}

    private List<RecyclingPreview> recyclingPreview() {
        var result = new ArrayList<RecyclingPreview>();
        for (int index : outputs(0, 4, false)) {
            var output = getData().getOutputs().get(index).getItem();
            double amount = index < getData().getRecyclingAmounts().size() ? getData().getRecyclingAmounts().get(index) : output.getCount();
            var recipe = new blusunrize.immersiveengineering.api.crafting.ArcRecyclingRecipe(
                    () -> (net.minecraft.core.RegistryAccess) com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry(),
                    List.of(com.mojang.datafixers.util.Pair.of(new blusunrize.immersiveengineering.api.crafting.TagOutput(output), amount)),
                    new blusunrize.immersiveengineering.api.crafting.IngredientWithSize(net.minecraft.world.item.crafting.Ingredient.EMPTY, 1), 1, 1);
            for (var stack : recipe.getBaseOutputs()) result.add(new RecyclingPreview(index, stack));
        }
        return result;
    }

    private List<Integer> inputs(int start, int end, boolean required) {
        var indices = IntStream.range(start, Math.min(end, getData().getInputs().size()))
                .filter(i -> !getData().getInputs().get(i).isEmpty()).boxed().toList();
        return indices.isEmpty() && required ? List.of(start) : indices;
    }

    private List<Integer> outputs(int start, int end, boolean required) {
        var indices = IntStream.range(start, Math.min(end, getData().getOutputs().size()))
                .filter(i -> !getData().getOutputs().get(i).isEmpty()).boxed().toList();
        return indices.isEmpty() && required ? List.of(start) : indices;
    }

    private int spareSlots(UIElement panel, boolean input, int y) {
        var placed = input ? placedInputs : placedOutputs;
        var indices = IntStream.range(0, input ? itemInputs : itemOutputs).filter(i -> !placed.contains(i)).toArray();
        if (indices.length == 0) return y;
        text(panel, () -> Component.translatable(input ? "viscript_recipe.editor.immersive_engineering.optional_inputs"
                : getData().isRecycling() ? "viscript_recipe.editor.immersive_engineering.recycling_materials"
                : "viscript_recipe.editor.immersive_engineering.optional_outputs"), 0, y, false);
        for (int i = 0; i < indices.length; i++) {
            if (input) in(panel, indices[i], 1 + i * 19, y + 13, true);
            else out(panel, indices[i], 1 + i * 19, y + 13, true);
        }
        return y + 34;
    }

    private void machine(UIElement panel, int x, int y) {
        image(panel, new ItemStackTexture(RecipeEditorTypes.requireCategory(entry.getType()).workstationStack()), x, y, 48, 48);
    }

    private void chance(UIElement panel, int index, int x, int y) {
        text(panel, () -> Component.literal(number(getVisualOutput(index).getChance() * 100) + "%"), x, y, false);
    }

    private void in(UIElement panel, int index, int x, int y, boolean frame) {
        placedInputs.add(index);
        slot(panel, createIngredientSlot(index, 18), "ie_input_" + index, x, y, frame);
    }

    private void out(UIElement panel, int index, int x, int y, boolean frame) {
        placedOutputs.add(index);
        slot(panel, createOutputSlot(index, 18), "ie_output_" + index, x, y, frame);
    }

    private void slot(UIElement panel, ItemSlot slot, String id, int x, int y, boolean frame) {
        configureJeiOverlaySlotVisual(slot);
        slot.layout(layout -> layout.paddingAll(1));
        if (frame) slot.style(style -> style.backgroundTexture(slotTexture()));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.addChild(at(slot.setId(id), x - 1, y - 1, 18, 18));
    }

    private void fluidIn(UIElement panel, int x, int y, int w, int h, int capacity, IGuiTexture overlay) {
        var slot = createFluidInputSlot(fluidInputs);
        // FluidDisplaySlot's tag model must follow fluids dragged from JEI as well as property edits.
        slot.registerValueListener(stack -> slot.setFluidIngredient(FluidIngredientData.fluid(stack.copy())));
        tank(panel, slot, "ie_fluid_input_" + fluidInputs++, x, y, w, h, capacity, overlay);
    }

    private void fluidOut(UIElement panel, int x, int y, int w, int h, int capacity, IGuiTexture overlay) {
        fluidOutput = true;
        var slot = createFluidOutputSlot(0);
        if (creosoteOutput) slot.registerValueListener(stack -> setVisualFluidOutput(0, stack));
        tank(panel, slot, "ie_fluid_output", x, y, w, h, capacity, overlay);
    }

    private void tank(UIElement panel, FluidSlot slot, String id, int x, int y, int w, int h, int capacity, IGuiTexture overlay) {
        configureJeiOverlayFluidSlotVisual(slot);
        slot.setCapacity(capacity);
        slot.slotStyle(style -> style.fillDirection(com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection.DOWN_TO_UP));
        slot.layout(layout -> layout.paddingAll(0));
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> event.stopPropagation());
        panel.addChild(at(slot.setId(id), x, y, w, h));
        if (overlay != null) {
            // Foreground is drawn after the fluid; a background overlay would be covered by it.
            slot.style(style -> style.overlayTexture(overlay));
        }
    }

    @Override public void load() {
        var data = getData();
        for (int i = 0; i < itemInputs; i++)
            loadIngredientSlot(i, i < data.getInputs().size() ? data.getInputs().get(i) : com.viscript_recipe.data.RecipeIngredient.empty());
        for (int i = 0; i < itemOutputs; i++)
            setVisualOutput(i, i < data.getOutputs().size() ? data.getOutputs().get(i) : RecipeOutputData.empty());
        for (int i = 0; i < fluidInputs; i++)
            setVisualFluidInput(i, i < data.getFluidInputs().size() ? data.getFluidInputs().get(i) : FluidIngredientData.empty());
        if (fluidOutput) setVisualFluidOutput(0, creosoteOutput
                ? new FluidStack(IEFluids.CREOSOTE.getStill(), data.getCreosote()) : data.getFluidOutput());
        if (entry.getType().getPath().equals("mixer")) {
            int capacity = Math.max(2000, Math.max(getVisualFluidInput(0).getAmount(), getVisualFluidOutput(0).getAmount()));
            fluidInputSlots[0].setCapacity(capacity);
            fluidOutputSlots[0].setCapacity(capacity);
        }
        layoutState = layoutState();
    }

    @Override public void screenTick() {
        super.screenTick();
        var state = layoutState();
        if (!state.equals(layoutState)) {
            save();
            clearAllChildren();
            initVisualState();
            load();
        }
        if (entry.getType().getPath().equals("mixer")) {
            int capacity = Math.max(2000, Math.max(getVisualFluidInput(0).getAmount(), getVisualFluidOutput(0).getAmount()));
            fluidInputSlots[0].setCapacity(capacity);
            fluidOutputSlots[0].setCapacity(capacity);
        }
    }

    private List<Object> layoutState() {
        var state = new ArrayList<Object>();
        for (int i = 0; i < itemInputs; i++) state.add(getVisualIngredient(i).isEmpty());
        for (int i = 0; i < itemOutputs; i++) {
            var output = getVisualOutput(i);
            state.add(output.isEmpty());
            if (getData().isRecycling()) state.add(output.getItem().toString());
        }
        if (getData().isRecycling()) state.addAll(getData().getRecyclingAmounts());
        return state;
    }

    @Override public void save() {
        var data = getData();
        var inputs = new ArrayList<com.viscript_recipe.data.RecipeIngredient>();
        var outputs = new ArrayList<RecipeOutputData>();
        var fluids = new ArrayList<FluidIngredientData>();
        for (int i = 0; i < itemInputs; i++) inputs.add(getVisualIngredient(i));
        for (int i = 0; i < itemOutputs; i++) outputs.add(getVisualOutput(i));
        for (int i = 0; i < fluidInputs; i++) {
            var value = getVisualFluidInput(i);
            if (i < data.getFluidInputs().size() && !sameFluidIngredient(value, data.getFluidInputs().get(i))) {
                while (data.getFluidInputCodecs().size() <= i) data.getFluidInputCodecs().add("");
                data.getFluidInputCodecs().set(i, "");
            }
            fluids.add(value);
        }
        data.setInputs(inputs).setOutputs(outputs).setFluidInputs(fluids);
        if (creosoteOutput) data.setCreosote(getVisualFluidOutput(0).getAmount());
        else if (fluidOutput) data.setFluidOutput(getVisualFluidOutput(0));
    }

    private static boolean sameFluidIngredient(FluidIngredientData a, FluidIngredientData b) {
        return a.getKind() == b.getKind() && a.getAmount() == b.getAmount()
                && java.util.Objects.equals(a.getTag(), b.getTag())
                && FluidStack.matches(a.getFluid(), b.getFluid());
    }

    @Override public boolean ingredientHasCount(int slotIndex) {
        return switch (entry.getType().getPath()) {
            case "crusher", "sawmill", "fertilizer", "cloche", "refinery", "blast_furnace_fuel" -> false;
            case "metal_press" -> slotIndex == 0;
            default -> true;
        };
    }

    @Override public void setVisualFluidOutput(int index, FluidStack stack) {
        // A coke oven always produces creosote; JEI drops may change its amount, never its identity.
        super.setVisualFluidOutput(index, creosoteOutput
                ? new FluidStack(IEFluids.CREOSOTE.getStill(), stack.getAmount()) : stack);
        if (creosoteOutput) getData().setCreosote(stack.getAmount());
    }

    @Override public void buildRecipeProperties(UIElement content) {
        var data = getData();
        var kind = entry.getType().getPath();
        content.addChild(sectionTitle("viscript_recipe.editor.type.immersive_engineering." + kind));
        if (switch (kind) { case "alloy", "arc_furnace", "blast_furnace", "blast_furnace_fuel", "cloche", "coke_oven" -> true; default -> false; })
            content.addChild(intField("viscript_recipe.config.immersive_engineering.time", data.getTime(), 1, Integer.MAX_VALUE, data::setTime));
        if (switch (kind) { case "arc_furnace", "crusher", "fermenter", "metal_press", "mixer", "refinery", "sawmill", "squeezer" -> true; default -> false; })
            content.addChild(intField("viscript_recipe.config.immersive_engineering.energy", data.getEnergy(), 1, Integer.MAX_VALUE, data::setEnergy));
        if (kind.equals("coke_oven")) content.addChild(intField("viscript_recipe.config.immersive_engineering.creosote", data.getCreosote(), 0, Integer.MAX_VALUE, value -> { data.setCreosote(value); setVisualFluidOutput(0, new FluidStack(IEFluids.CREOSOTE.getStill(), value)); }));
        if (kind.equals("fertilizer")) content.addChild(floatField("viscript_recipe.config.immersive_engineering.growth_modifier", data.getGrowthModifier(), 0.01f, 1000f, data::setGrowthModifier));
        if (kind.equals("blueprint")) content.addChild(textField("viscript_recipe.config.immersive_engineering.blueprint_category", data.getBlueprintCategory(), data::setBlueprintCategory));
        if (kind.equals("cloche")) content.addChild(textField("viscript_recipe.config.immersive_engineering.cloche_render", data.getClocheRender(), data::setClocheRender));
        if (kind.equals("arc_furnace")) content.addChild(switchField("viscript_recipe.config.immersive_engineering.recycling", data.isRecycling(), data::setRecycling, IECanvas::reloadCanvas));
    }

    @Override public void buildResultProperties(UIElement content) {
        super.buildResultProperties(content);
        var kind = entry.getType().getPath();
        var index = selectedSlotIndex();
        if (kind.equals("cloche") || kind.equals("crusher") && index > 0 || kind.equals("arc_furnace") && index >= 5 && index < 9) {
            content.addChild(floatField("viscript_recipe.config.immersive_engineering.chance", getSelectedOutput().getChance(), 0, 1, this::setSelectedOutput));
        }
        if (kind.equals("arc_furnace") && getData().isRecycling() && index < 4) {
            while (getData().getRecyclingAmounts().size() <= index) getData().getRecyclingAmounts().add(1d);
            content.addChild(floatField("viscript_recipe.config.immersive_engineering.recycling_amount",
                    getData().getRecyclingAmounts().get(index).floatValue(), 0.01f, 1024f,
                    value -> getData().getRecyclingAmounts().set(index, (double) value)));
        }
    }

    @Override public void buildFluidProperties(UIElement content) {
        if (creosoteOutput && selectedSlotIndex() == 2) {
            content.addChild(sectionTitle("viscript_recipe.config.immersive_engineering.creosote"));
            content.addChild(intField("viscript_recipe.config.immersive_engineering.creosote",
                    getVisualFluidOutput(0).getAmount(), 0, Integer.MAX_VALUE,
                    value -> setVisualFluidOutput(0, new FluidStack(IEFluids.CREOSOTE.getStill(), value))));
            return;
        }
        super.buildFluidProperties(content);
        int index = selectedSlotIndex();
        if (index >= fluidInputs) return;
        var codecs = getData().getFluidInputCodecs();
        while (codecs.size() <= index) codecs.add("");
        content.addChild(textField("viscript_recipe.config.immersive_engineering.fluid_predicate_json",
                codecs.get(index), value -> codecs.set(index, value)));
    }
}
