package com.viscript_recipe.gui.editor;

import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.Polarice3.Goety.common.ritual.ModRituals;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class GoetyRitualSearchComponents {
    private static final ResourceLocation DEFAULT_RITUAL_TYPE =
            ResourceLocation.fromNamespaceAndPath("goety", "craft");

    private GoetyRitualSearchComponents() {
    }

    static UIElement ritualType(
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var registry = ModRituals.REGISTRY;
        var fallback = registry.getOptional(DEFAULT_RITUAL_TYPE)
                .orElseGet(() -> registry.getAny().orElseThrow().value());
        var current = registry.getOptional(Objects.requireNonNullElse(supplier.get(), DEFAULT_RITUAL_TYPE))
                .orElse(fallback);
        var searchBox = new RitualTypeSearchBox(current);
        searchBox.setOnValueChanged(value -> {
            var id = registry.getKey(value);
            if (id != null && !Objects.equals(id, supplier.get())) {
                consumer.accept(id);
                onChanged.run();
            }
        });
        searchBox.searchStyle(style -> {
            style.maxItemCount(8);
            style.scrollerViewHeight(120);
            style.closeAfterSelect(true);
        });
        searchBox.layout(layout -> {
            layout.widthPercent(100);
            layout.height(18);
        });
        return RecipeEditorUi.fieldGroup("viscript_recipe.config.goety.ritual.ritual_type", searchBox);
    }

    static UIElement enchantment(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        return RecipeSearchComponents.enchantment(nameKey, supplier, consumer, onChanged);
    }

    private static Component ritualTypeName(ResourceLocation id) {
        if (id == null) {
            return Component.translatable("viscript_recipe.editor.goety.ritual.behavior.unknown");
        }
        var key = "viscript_recipe.editor.goety.ritual.behavior."
                + id.getNamespace() + "." + id.getPath().replace('/', '.');
        return Component.translatableWithFallback(key, id.toString());
    }

    private static final class RitualTypeSearchBox extends RegistrySearchBox<ModRitualFactory> {
        private RitualTypeSearchBox(ModRitualFactory defaultValue) {
            super(
                    defaultValue,
                    () -> ModRituals.REGISTRY,
                    ModRituals.REGISTRY::getKey,
                    value -> Objects.toString(ModRituals.REGISTRY.getKey(value), ""),
                    RitualTypeSearchBox::search,
                    UIElementProvider.text(value -> ritualTypeName(ModRituals.REGISTRY.getKey(value)))
            );
        }

        private static void search(
                String word,
                com.lowdragmc.lowdraglib2.utils.search.IResultHandler<ModRitualFactory> result
        ) {
            var query = word.toLowerCase(Locale.ROOT);
            for (var value : ModRituals.REGISTRY) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                var id = ModRituals.REGISTRY.getKey(value);
                if (id != null && (id.toString().toLowerCase(Locale.ROOT).contains(query)
                        || ritualTypeName(id).getString().toLowerCase(Locale.ROOT).contains(query))) {
                    result.acceptResult(value);
                }
            }
        }
    }
}
