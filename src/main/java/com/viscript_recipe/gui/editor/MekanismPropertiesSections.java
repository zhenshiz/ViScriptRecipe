package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientData;
import com.viscript_recipe.data.mekanism.MekanismChemicalIngredientKind;
import com.viscript_recipe.data.mekanism.MekanismChemicalStackData;
import com.viscript_recipe.data.mekanism.MekanismFluidIngredientData;
import com.viscript_recipe.data.mekanism.MekanismFluidIngredientKind;
import com.viscript_recipe.data.mekanism.MekanismRecipeKind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Builds the fields that are specific to Mekanism's native recipe codecs. */
final class MekanismPropertiesSections {
    private MekanismPropertiesSections() {
    }

    static void build(UIElement content, RecipeEditorController controller, RecipeEntry entry) {
        var kind = MekanismRecipeKind.byType(entry.getType()).orElse(null);
        if (kind == null) {
            return;
        }
        var data = entry.getMekanism();
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.mekanism"));

        if (kind.hasPerTickUsage()) {
            content.addChild(switchField("viscript_recipe.config.mekanism.per_tick_usage", data.isPerTickUsage(),
                    data::setPerTickUsage, controller));
        }
        if (kind.hasDuration()) {
            content.addChild(intField("viscript_recipe.config.mekanism.duration", data.getDuration(), 1,
                    Integer.MAX_VALUE, data::setDuration, controller));
        }
        if (kind.hasEnergyRequired()) {
            content.addChild(longField("viscript_recipe.config.mekanism.energy_required", data.getEnergyRequired(), 0,
                    Long.MAX_VALUE, data::setEnergyRequired, controller));
        }
        if (kind.hasEnergyMultiplier()) {
            content.addChild(longField("viscript_recipe.config.mekanism.energy_multiplier", data.getEnergyMultiplier(), 1,
                    Long.MAX_VALUE, data::setEnergyMultiplier, controller));
        }
        if (kind == MekanismRecipeKind.ENERGY_CONVERSION) {
            content.addChild(longField("viscript_recipe.config.mekanism.energy_output", data.getEnergyOutput(), 1,
                    Long.MAX_VALUE, data::setEnergyOutput, controller));
        }
    }

    static void buildSelectedChemical(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedMekanismChemicalSlot()) {
            return;
        }
        var selection = controller.getSlotSelection().index();
        if (controller.isSelectedMekanismChemicalIngredient()) {
            buildChemicalIngredient(content, controller, controller.getSelectedMekanismChemicalIngredient(),
                    selection == RecipeEditorController.MEKANISM_EXTRA_CHEMICAL_INPUT_SLOT
                            ? "viscript_recipe.config.mekanism.extra_chemical_input"
                            : "viscript_recipe.config.mekanism.chemical_input");
            return;
        }
        if (!buildSelectedReactionChemicalOutputEnabled(content, controller)) {
            return;
        }
        buildChemicalOutput(content, controller, controller.getSelectedMekanismChemicalOutput(),
                selection == RecipeEditorController.MEKANISM_SECONDARY_CHEMICAL_OUTPUT_SLOT
                        ? "viscript_recipe.config.mekanism.secondary_chemical_output"
                        : "viscript_recipe.config.mekanism.chemical_output", false);
    }

    static void buildSelectedFluid(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedMekanismFluidSlot()) {
            return;
        }
        if (controller.isSelectedMekanismFluidIngredient()) {
            buildFluidIngredient(content, controller, controller.getSelectedMekanismFluidIngredient());
            return;
        }
        buildFluidOutput(content, controller);
    }

    static void buildSelectedItem(UIElement content, RecipeEditorController controller) {
        if (!controller.isSelectedMekanismItemSlot() || controller.getSelectedEntry() == null) {
            return;
        }
        var data = controller.getSelectedEntry().getMekanism();
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.mekanism"),
                createItemStackConfigurator(
                        "viscript_recipe.config.mekanism.secondary_item_output",
                        controller::getSelectedMekanismItemOutput,
                        controller::setSelectedMekanismItemOutput,
                        controller
                )
        );
        var kind = MekanismRecipeKind.byType(controller.getSelectedEntry().getType()).orElse(null);
        if (kind != null && kind.hasSecondaryChance()) {
            content.addChild(RecipeEditorUi.fieldGroup(
                    "viscript_recipe.config.mekanism.secondary_chance",
                    RecipeEditorUi.doubleField(data.getSecondaryChance(), 0, 1, value -> {
                        data.setSecondaryChance(value);
                        controller.notifyChanged();
                    })
            ));
        }
    }

    static boolean buildSelectedReactionItemOutputEnabled(
            UIElement content,
            RecipeEditorController controller
    ) {
        var entry = controller.getSelectedEntry();
        if (entry == null || MekanismRecipeKind.byType(entry.getType()).orElse(null) != MekanismRecipeKind.REACTION) {
            return true;
        }
        var data = entry.getMekanism();
        var hasItem = data.getItemOutput() != null && !data.getItemOutput().isEmpty();
        content.addChild(switchField("viscript_recipe.config.mekanism.reaction.has_item_output", hasItem, value -> {
            if (value) {
                controller.setVisualResult(new ItemStack(Items.IRON_INGOT));
            } else {
                controller.setVisualResult(ItemStack.EMPTY);
                if (data.getChemicalOutput().isEmpty()) {
                    data.getChemicalOutput()
                            .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"))
                            .setAmount(1);
                }
            }
        }, controller));
        return hasItem;
    }

    static String structureSignature(RecipeEntry entry) {
        var kind = entry == null ? null : MekanismRecipeKind.byType(entry.getType()).orElse(null);
        if (kind == null) {
            return "";
        }
        var data = entry.getMekanism();
        return kind.name() + ':' + fluidKind(data.getFluidInput()) + ':' + chemicalKind(data.getChemicalInput()) + ':'
                + chemicalKind(data.getExtraChemicalInput()) + ':'
                + (data.getItemOutput() != null && !data.getItemOutput().isEmpty()) + ':'
                + !data.getChemicalOutput().isEmpty();
    }

    private static void buildFluidIngredient(UIElement content, RecipeEditorController controller,
                                             MekanismFluidIngredientData data) {
        var kind = fluidKind(data);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.mekanism.fluid_input"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.mekanism.fluid_ingredient.kind",
                        RecipeEditorUi.selector(List.of(MekanismFluidIngredientKind.values()), kind,
                                value -> Component.translatable("viscript_recipe.editor.mekanism.fluid_kind."
                                        + value.getSerializedName()), value -> {
                                    data.setKind(value);
                                    controller.notifyChanged();
                                }))
        );
        if (kind == MekanismFluidIngredientKind.TAG) {
            content.addChild(RecipeSearchComponents.fluidTag("viscript_recipe.config.mekanism.fluid_ingredient.tag",
                    data::getTag, data::setTag, controller::notifyChanged));
        } else {
            content.addChild(RecipeSearchComponents.fluid("viscript_recipe.config.mekanism.fluid_ingredient.fluid",
                    () -> fluidId(data.getFluid()), id -> {
                        var fluid = BuiltInRegistries.FLUID.get(id);
                        data.setFluid(new FluidStack(fluid == null ? Fluids.WATER : fluid, Math.max(1, data.getAmount())));
                    }, controller::notifyChanged, Fluids.WATER));
        }
        content.addChild(intField("viscript_recipe.config.mekanism.fluid_ingredient.amount", amount(data), 1,
                Integer.MAX_VALUE, value -> {
                    data.setAmount(value);
                    if (kind == MekanismFluidIngredientKind.FLUID && data.getFluid() != null && !data.getFluid().isEmpty()) {
                        data.setFluid(data.getFluid().copyWithAmount(value));
                    }
                }, controller));
    }

    private static void buildFluidOutput(UIElement content, RecipeEditorController controller) {
        var output = controller.getSelectedMekanismFluidOutput();
        var amount = output.isEmpty() ? 1 : Math.max(1, output.getAmount());
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.config.mekanism.fluid_output"),
                RecipeSearchComponents.fluid(
                        "viscript_recipe.config.mekanism.fluid_ingredient.fluid",
                        () -> fluidId(controller.getSelectedMekanismFluidOutput()),
                        id -> {
                            var fluid = BuiltInRegistries.FLUID.get(id);
                            controller.setSelectedMekanismFluidOutput(new FluidStack(
                                    fluid == null ? Fluids.WATER : fluid,
                                    Math.max(1, controller.getSelectedMekanismFluidOutput().getAmount())
                            ));
                        },
                        () -> { },
                        Fluids.WATER
                ),
                RecipeEditorUi.fieldGroup(
                        "viscript_recipe.config.mekanism.fluid_ingredient.amount",
                        RecipeEditorUi.intField(amount, 1, Integer.MAX_VALUE, value -> {
                            var selected = controller.getSelectedMekanismFluidOutput();
                            var fluid = selected.isEmpty() ? Fluids.WATER : selected.getFluid();
                            controller.setSelectedMekanismFluidOutput(new FluidStack(fluid, Math.max(1, value)));
                        })
                )
        );
    }

    private static void buildChemicalIngredient(UIElement content, RecipeEditorController controller,
                                                MekanismChemicalIngredientData data, String titleKey) {
        var kind = chemicalKind(data);
        content.addChildren(
                RecipeEditorUi.sectionTitle(titleKey),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.mekanism.chemical_ingredient.kind",
                        RecipeEditorUi.selector(List.of(MekanismChemicalIngredientKind.values()), kind,
                                value -> Component.translatable("viscript_recipe.editor.mekanism.chemical_kind."
                                        + value.getSerializedName()), value -> {
                                    data.setKind(value);
                                    controller.notifyChanged();
                                }))
        );
        if (kind == MekanismChemicalIngredientKind.TAG) {
            content.addChild(MekanismSearchComponents.chemicalTag("viscript_recipe.config.mekanism.chemical_ingredient.tag",
                    data::getTag, data::setTag, controller::notifyChanged));
        } else {
            content.addChild(MekanismSearchComponents.chemical("viscript_recipe.config.mekanism.chemical_ingredient.chemical",
                    data::getChemical, data::setChemical, controller::notifyChanged));
        }
        content.addChild(longField("viscript_recipe.config.mekanism.chemical_ingredient.amount", data.getAmount(), 1,
                Long.MAX_VALUE, data::setAmount, controller));
    }

    private static void buildChemicalOutput(UIElement content, RecipeEditorController controller,
                                            MekanismChemicalStackData data, String titleKey, boolean optional) {
        if (optional && data.isEmpty()) {
            return;
        }
        content.addChildren(
                MekanismSearchComponents.chemical(titleKey, data::getChemical, id -> {
                    data.setChemical(id);
                    if (data.getAmount() <= 0) {
                        data.setAmount(1);
                    }
                }, controller::notifyChanged),
                longField(titleKey + ".amount", Math.max(1, data.getAmount()), 1, Long.MAX_VALUE,
                        data::setAmount, controller)
        );
    }

    private static boolean buildSelectedReactionChemicalOutputEnabled(
            UIElement content,
            RecipeEditorController controller
    ) {
        var entry = controller.getSelectedEntry();
        if (entry == null
                || MekanismRecipeKind.byType(entry.getType()).orElse(null) != MekanismRecipeKind.REACTION
                || controller.getSlotSelection().index() != RecipeEditorController.MEKANISM_CHEMICAL_OUTPUT_SLOT) {
            return true;
        }
        var data = entry.getMekanism();
        var hasChemical = !data.getChemicalOutput().isEmpty();
        content.addChild(switchField("viscript_recipe.config.mekanism.reaction.has_chemical_output", hasChemical, value -> {
            if (value) {
                data.getChemicalOutput()
                        .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"))
                        .setAmount(1);
            } else {
                data.getChemicalOutput().setChemical(null).setAmount(0);
                if (data.getItemOutput() == null || data.getItemOutput().isEmpty()) {
                    controller.setVisualResult(new ItemStack(Items.IRON_INGOT));
                }
            }
        }, controller));
        return hasChemical;
    }

    private static UIElement createItemStackConfigurator(String nameKey, Supplier<ItemStack> supplier,
                                                         Consumer<ItemStack> setter, RecipeEditorController controller) {
        var configurator = new ItemStackAccessor().create(nameKey,
                () -> copyItem(supplier.get()), stack -> {
                    setter.accept(copyItem(stack));
                    controller.notifyChanged();
                }, true, null, null);
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private static UIElement intField(String key, int value, int min, int max, Consumer<Integer> setter,
                                      RecipeEditorController controller) {
        return RecipeEditorUi.fieldGroup(key, RecipeEditorUi.intField(value, min, max, updated -> {
            setter.accept(updated);
            controller.notifyChanged();
        }));
    }

    private static UIElement longField(String key, long value, long min, long max, Consumer<Long> setter,
                                       RecipeEditorController controller) {
        return RecipeEditorUi.fieldGroup(key, RecipeEditorUi.longField(value, min, max, updated -> {
            setter.accept(updated);
            controller.notifyChanged();
        }));
    }

    private static UIElement switchField(String key, boolean value, Consumer<Boolean> setter,
                                         RecipeEditorController controller) {
        return RecipeEditorUi.fieldGroup(key, new Switch().setOn(value, false).setOnSwitchChanged(updated -> {
            setter.accept(updated);
            controller.notifyChanged();
        }));
    }

    private static MekanismFluidIngredientKind fluidKind(MekanismFluidIngredientData data) {
        return data == null || data.getKind() == null ? MekanismFluidIngredientKind.FLUID : data.getKind();
    }

    private static MekanismChemicalIngredientKind chemicalKind(MekanismChemicalIngredientData data) {
        return data == null || data.getKind() == null ? MekanismChemicalIngredientKind.CHEMICAL : data.getKind();
    }

    private static int amount(MekanismFluidIngredientData data) {
        if (data == null) {
            return 1;
        }
        if (fluidKind(data) == MekanismFluidIngredientKind.FLUID && data.getFluid() != null && !data.getFluid().isEmpty()) {
            return Math.max(1, data.getFluid().getAmount());
        }
        return Math.max(1, data.getAmount());
    }

    private static ResourceLocation fluidId(FluidStack stack) {
        var fluid = stack == null || stack.isEmpty() ? Fluids.WATER : stack.getFluid();
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    private static ItemStack copyItem(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

}
