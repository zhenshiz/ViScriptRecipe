package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MysticalAgricultureSearchComponents {
    private MysticalAgricultureSearchComponents() {
    }

    public static UIElement mobSoulType(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var types = MobSoulTypeRegistry.getInstance().getMobSoulTypes().stream()
                .filter(MobSoulType::isEnabled)
                .toList();
        var current = currentType(types, supplier.get());
        var searchBox = new MobSoulTypeSearchBox(current, types);
        searchBox.setOnValueChanged(type -> {
            if (type != null && !Objects.equals(type.getId(), supplier.get())) {
                consumer.accept(type.getId());
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
        return RecipeEditorUi.fieldGroup(nameKey, searchBox);
    }

    @Nullable
    private static MobSoulType currentType(List<MobSoulType> types, @Nullable ResourceLocation id) {
        if (types.isEmpty()) {
            return null;
        }
        if (id == null) {
            return types.getFirst();
        }
        return types.stream().filter(type -> id.equals(type.getId())).findFirst().orElse(types.getFirst());
    }

    private static Component displayName(MobSoulType type) {
        return type.getEntityDisplayName().copy()
                .append(Component.literal(" (" + type.getId() + ")"));
    }

    private static String searchText(MobSoulType type) {
        var text = new StringBuilder(type.getId().toString())
                .append(' ')
                .append(type.getEntityDisplayName().getString());
        for (var entityId : type.getEntityIds()) {
            text.append(' ').append(entityId);
            BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).ifPresent(entity -> text
                    .append(' ')
                    .append(entity.getDescriptionId())
                    .append(' ')
                    .append(entity.getDescription().getString()));
        }
        return text.toString();
    }

    private static final class MobSoulTypeSearchBox extends RegistrySearchBox<MobSoulType> {
        private MobSoulTypeSearchBox(@Nullable MobSoulType defaultValue, List<MobSoulType> types) {
            super(
                    defaultValue,
                    () -> null,
                    MobSoulType::getId,
                    type -> type.getId().toString(),
                    (word, result) -> search(types, word, result),
                    UIElementProvider.text(MysticalAgricultureSearchComponents::displayName)
            );
        }

        private static void search(
                List<MobSoulType> types,
                String word,
                IResultHandler<MobSoulType> result
        ) {
            var query = word.toLowerCase(Locale.ROOT);
            for (var type : types) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (searchText(type).toLowerCase(Locale.ROOT).contains(query)) {
                    result.acceptResult(type);
                }
            }
        }
    }
}
