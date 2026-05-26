package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.configurator.accessors.ItemStackAccessor;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.TagKeySearchComponent;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.data.vanilla.CraftingRemainderMode;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecipePropertiesView extends View {
    private final RecipeEditorController controller;
    private final UIElement content = RecipeEditorUi.column();
    private boolean rebuilding;

    public RecipePropertiesView(RecipeEditorController controller) {
        super("viscript_recipe.view.recipe_properties", Icons.SETTINGS);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(content.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(6);
        }));
        root.addChildren(scroller, createBottomActions());
        return root;
    }

    private UIElement createBottomActions() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
            layout.gapAll(4);
        }).addChildren(
                RecipeEditorUi.iconButton(Icons.SAVE, "viscript_recipe.editor.save", event -> controller.saveProject())
                        .layout(layout -> layout.flex(1).height(20))
        );
    }

    private void refresh() {
        if (rebuilding) {
            return;
        }
        rebuilding = true;
        try {
            content.clearAllChildren();
            var entry = controller.getSelectedEntry();
            if (entry == null) {
                content.addChild(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.no_entry")));
                return;
            }
            switch (controller.getSlotSelection().kind()) {
                case RECIPE -> buildRecipeProperties(entry);
                case INGREDIENT -> buildIngredientProperties(entry);
                case RESULT -> buildResultProperties();
            }
        } finally {
            rebuilding = false;
        }
    }

    private void buildRecipeProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.recipe"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.type",
                        RecipeEditorUi.selector(
                                controller.availableTypesForSelectedCategory(),
                                controller.getSelectedRecipeType(),
                                type -> type.displayName(),
                                controller::setSelectedRecipeType
                        )),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.enabled",
                        new Switch()
                                .setOn(entry.isEnabled(), false)
                                .setOnSwitchChanged(value -> {
                                    entry.setEnabled(value);
                                    controller.notifyChanged();
                                })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.recipe_id",
                        RecipeEditorUi.resourceLocationField(entry.getRecipeId(), value -> {
                            entry.setRecipeId(value);
                            controller.notifyChanged();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.entry.operation",
                        RecipeEditorUi.selector(
                                List.of(RecipeOperation.ADD, RecipeOperation.REPLACE, RecipeOperation.REMOVE),
                                entry.getOperation(),
                                operation -> Component.translatable("viscript_recipe.editor.operation." + operation.getSerializedName()),
                                operation -> {
                                    entry.setOperation(operation);
                                    controller.notifyChanged();
                                }
                        ))
        );
        if (controller.supportsNotification(entry)) {
            content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.recipe.show_notification",
                    new Switch()
                            .setOn(controller.showNotification(entry), false)
                            .setOnSwitchChanged(value -> controller.setShowNotification(entry, value))));
        }
        if (controller.isCookingEntry(entry)) {
            buildCookingProperties(entry);
        }
    }

    private void buildCookingProperties(RecipeEntry entry) {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.cooking"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.experience",
                        RecipeEditorUi.floatField(controller.getCookingExperience(entry), 0, Integer.MAX_VALUE, value -> controller.setCookingExperience(entry, value))),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.cooking.cooking_time",
                        RecipeEditorUi.intField(controller.getCookingTime(entry), 1, 72000, value -> controller.setCookingTime(entry, value)))
        );
    }

    private void buildIngredientProperties(RecipeEntry entry) {
        var ingredient = copyIngredient(controller.getSelectedIngredient());
        var value = editableValue(ingredient);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ingredient"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.ingredient.value.kind",
                        RecipeEditorUi.selector(
                                List.of(IngredientValueKind.ITEM, IngredientValueKind.TAG),
                                value.getKind(),
                                kind -> Component.translatable("viscript_recipe.editor.ingredient.kind." + kind.getSerializedName()),
                                kind -> setIngredientKind(ingredient, value, kind)
                        ))
        );

        if (value.getKind() == IngredientValueKind.ITEM) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.editor.ingredient.item_slot",
                    () -> ingredientItemStack(ingredient),
                    stack -> setIngredientItem(ingredient, stack)
            ));
        } else if (value.getKind() == IngredientValueKind.TAG) {
            content.addChild(createItemTagConfigurator(ingredient, value));
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            buildRemainderProperties();
        }
    }

    private void buildRemainderProperties() {
        var remainder = controller.getSelectedRemainder();
        var mode = remainderMode(remainder);
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.remainder"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.remainder.mode",
                        RecipeEditorUi.selector(
                                List.of(CraftingRemainderMode.DEFAULT, CraftingRemainderMode.CONSUME, CraftingRemainderMode.REPLACE),
                                mode,
                                value -> Component.translatable("viscript_recipe.editor.remainder.mode." + value.getSerializedName()),
                                value -> {
                                    var updated = remainder.copy();
                                    updated.setMode(value);
                                    if (value != CraftingRemainderMode.REPLACE) {
                                        updated.setItem(ItemStack.EMPTY);
                                    }
                                    controller.setSelectedRemainder(updated);
                                }
                        ),
                        Component.translatable("viscript_recipe.editor.remainder.tip.default"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.consume"),
                        Component.translatable("viscript_recipe.editor.remainder.tip.replace"))
        );

        if (mode == CraftingRemainderMode.REPLACE) {
            content.addChild(createItemStackConfigurator(
                    "viscript_recipe.config.remainder.item",
                    () -> remainder.getItem() == null ? ItemStack.EMPTY : remainder.getItem().copy(),
                    stack -> {
                        var updated = remainder.copy();
                        updated.setMode(CraftingRemainderMode.REPLACE);
                        updated.setItem(stack == null ? ItemStack.EMPTY : stack.copy());
                        controller.setSelectedRemainder(updated);
                    }
            ));
        }
    }

    private void buildResultProperties() {
        content.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.result"),
                createItemStackConfigurator(
                        "viscript_recipe.config.recipe.result",
                        controller::getSelectedResult,
                        stack -> controller.setSelectedResult(normalizeResultStack(stack))
                )
        );
    }

    private UIElement createItemStackConfigurator(String nameKey, Supplier<ItemStack> supplier, Consumer<ItemStack> consumer) {
        var configurator = new ItemStackAccessor().create(
                nameKey,
                () -> {
                    var stack = supplier.get();
                    return stack == null ? ItemStack.EMPTY : stack.copy();
                },
                stack -> {
                    if (!rebuilding) {
                        consumer.accept(stack == null ? ItemStack.EMPTY : stack.copy());
                    }
                },
                true,
                null,
                this
        );
        configurator.layout(layout -> layout.widthPercent(100));
        if (configurator instanceof ConfiguratorGroup group) {
            group.setCollapse(false);
        }
        return configurator;
    }

    private UIElement createItemTagConfigurator(RecipeIngredient ingredient, RecipeIngredientValue value) {
        var configurator = new TagKeySearchComponent.Item(
                "viscript_recipe.config.ingredient.value.tag",
                () -> itemTag(value.getTag()),
                tag -> {
                    if (!rebuilding) {
                        ingredient.getValues().clear();
                        value.setKind(IngredientValueKind.TAG);
                        value.setTag(tag.location());
                        ingredient.getValues().add(value);
                        controller.setSelectedIngredient(ingredient);
                    }
                },
                itemTag(defaultTag()),
                true
        );
        configurator.layout(layout -> layout.widthPercent(100));
        configurator.searchComponent.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
        });
        return configurator;
    }

    private TagKey<Item> itemTag(ResourceLocation tag) {
        return TagKey.create(Registries.ITEM, tag == null ? defaultTag() : tag);
    }

    private ResourceLocation defaultTag() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "planks");
    }

    private ItemStack normalizeResultStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Math.max(0, Math.min(99, copy.getCount())));
        return copy;
    }

    private void setIngredientKind(RecipeIngredient ingredient, RecipeIngredientValue currentValue, IngredientValueKind kind) {
        var updated = new RecipeIngredientValue().setKind(kind);
        ingredient.getValues().clear();
        if (kind == IngredientValueKind.ITEM) {
            var stack = currentValue.getItem() == null ? ItemStack.EMPTY : currentValue.getItem().copyWithCount(1);
            if (!stack.isEmpty()) {
                updated.setItem(stack);
                ingredient.getValues().add(updated);
            }
        } else {
            updated.setTag(currentValue.getTag() == null
                    ? ResourceLocation.fromNamespaceAndPath("minecraft", "planks")
                    : currentValue.getTag());
            ingredient.getValues().add(updated);
        }
        controller.setSelectedIngredient(ingredient);
    }

    private ItemStack ingredientItemStack(RecipeIngredient ingredient) {
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null) {
                return value.getItem().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private RecipeIngredientValue editableValue(RecipeIngredient ingredient) {
        if (ingredient.getValues().isEmpty()) {
            return new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(ItemStack.EMPTY);
        }
        return ingredient.getValues().getFirst();
    }

    private void setIngredientItem(RecipeIngredient ingredient, ItemStack stack) {
        ingredient.getValues().clear();
        if (stack != null && !stack.isEmpty()) {
            ingredient.getValues().add(new RecipeIngredientValue()
                    .setKind(IngredientValueKind.ITEM)
                    .setItem(stack.copyWithCount(1)));
        }
        controller.setSelectedIngredient(ingredient);
    }

    private CraftingRemainderMode remainderMode(CraftingRemainderRule remainder) {
        return remainder.getMode() == null ? CraftingRemainderMode.DEFAULT : remainder.getMode();
    }

    private RecipeIngredient copyIngredient(RecipeIngredient original) {
        var copy = new RecipeIngredient();
        for (var value : original.getValues()) {
            var valueCopy = new RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag());
            if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }
}
