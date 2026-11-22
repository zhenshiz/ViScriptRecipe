package com.viscript_recipe.compat.forbidden_arcanus.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.mojang.serialization.Codec;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

abstract class ForbiddenArcanusCanvas<D extends IVSRecipeData> extends RecipeCanvas<D> {
    private static final ResourceLocation NONE = ResourceLocation.fromNamespaceAndPath("viscript_recipe", "no_registry_value");

    ForbiddenArcanusCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    abstract NativeIngredientData input(int index);

    abstract int inputCount();

    @Override
    public void load() {
        for (int i = 0; i < inputCount(); i++) {
            var input = input(i);
            if (input.getCustomJson().isBlank()) loadIngredientSlot(i, input.getValue());
        }
    }

    @Override
    public void save() {
        for (int i = 0; i < inputCount(); i++) {
            var input = input(i);
            if (input.getCustomJson().isBlank()) input.setValue(getVisualIngredient(i));
        }
    }

    final ItemSlot ingredientSlot(int index, int size) {
        var input = input(index);
        if (input.getCustomJson().isBlank()) return createIngredientSlot(index, size);
        var slot = previewSlot(size);
        var candidates = ingredientPreviews(input);
        if (candidates.length > 0) {
            slot.bindDataSource(SupplierDataSource.of(() ->
                    candidates[(int) (System.currentTimeMillis() / 1000 % candidates.length)].copy()));
        }
        slot.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            selectSlot(SlotSelection.ingredient(index));
            event.stopPropagation();
        });
        return slot;
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        var input = input(selectedSlotIndex());
        if (input.getCustomJson().isBlank()) {
            super.buildIngredientProperties(content);
            return;
        }
        content.addChildren(RecipeEditorUi.label(Component.translatable(key("preserved_ingredient"))),
                RecipeEditorUi.textButton(Component.translatable(key("replace_ingredient")), null, event -> {
                    save();
                    input.setCustomJson("").setValue(RecipeIngredient.empty());
                    reloadCanvas();
                    reloadProperties();
                }));
    }

    static String key(String name) {return "viscript_recipe.editor.forbidden_arcanus." + name;}

    static HolderLookup.Provider registryAccess() {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }

    static ItemSlot previewSlot(int size) {
        var slot = new ItemSlot().slotStyle(style -> style.showItemTooltips(true));
        slot.layout(layout -> layout.width(size).height(size));
        return slot;
    }

    static UIElement panel(int width, int height) {
        return new UIElement().addClass("panel_bg").layout(layout -> layout.width(width).height(height));
    }

    static UIElement centered(UIElement panel) {
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChild(panel);
    }

    static UIElement at(UIElement child, int x, int y, int width, int height) {
        return child.layout(layout -> {
            layout.positionType(TaffyPosition.ABSOLUTE);
            layout.left(x);
            layout.top(y);
            layout.width(width);
            layout.height(height);
        });
    }

    static UIElement arrow() {
        return new UIElement().style(style -> style.backgroundTexture(Icons.DOWN_ARROW_NO_BAR.copy().rotate(-90)));
    }

    private static ItemStack[] ingredientPreviews(NativeIngredientData input) {
        var provider = registryAccess();
        try {
            return provider == null ? new ItemStack[0] : input.compile(provider).getItems();
        } catch (RuntimeException exception) {
            return new ItemStack[0];
        }
    }

    static <T> UIElement registryField(String name, ResourceKey<Registry<T>> key, Supplier<String> value, boolean optional,
                                       Function<Holder.Reference<T>, Component> label, Consumer<String> setter) {
        var provider = registryAccess();
        var old = value.get();
        if (provider == null || old.startsWith("{") || old.startsWith("[")) return textField(name, old, setter);
        var lookup = provider.lookupOrThrow(key);
        var ids = lookup.listElementIds().map(ResourceKey::location).sorted(Comparator.comparing(ResourceLocation::toString)).toList();
        var candidates = optional ? Stream.concat(Stream.of(NONE), ids.stream()).toList() : ids;
        return RecipeSearchComponents.catalog(name, () -> value.get().isBlank() ? NONE : ResourceLocation.tryParse(value.get()),
                selected -> setter.accept(selected.equals(NONE) ? "" : selected.toString()),
                () -> {}, candidates, id -> id.equals(NONE) ? Component.translatable(ForbiddenArcanusCanvas.key("none"))
                        : lookup.get(ResourceKey.create(key, id)).map(label).orElse(Component.literal(id.toString())),
                candidates.isEmpty() ? NONE : candidates.getFirst());
    }

    static <T> T readValue(Codec<T> codec, String value) {
        var provider = registryAccess();
        if (value.isBlank() || provider == null) return null;
        try {
            return ForbiddenArcanusRecipeValues.decode(codec, provider, value);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    static Component enhancerName(Holder.Reference<EnhancerDefinition> enhancer) {
        return enhancer.value().displayItem().value().getDefaultInstance().getHoverName();
    }
}
