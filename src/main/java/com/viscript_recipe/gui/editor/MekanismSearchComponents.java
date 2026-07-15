package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import com.viscript_lib.gui.components.search.RegistrySearchBox;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class MekanismSearchComponents {
    private MekanismSearchComponents() {
    }

    static UIElement chemical(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var registry = MekanismAPI.CHEMICAL_REGISTRY;
        var fallback = fallbackChemical();
        var current = registry.getOptional(Objects.requireNonNullElse(supplier.get(), registry.getKey(fallback)))
                .filter(MekanismSearchComponents::isUsableChemical)
                .orElse(fallback);
        var searchBox = new ChemicalSearchBox(current);
        searchBox.setCandidateFilter(MekanismSearchComponents::isUsableChemical);
        return configure(nameKey, searchBox, value -> {
            var id = registry.getKey(value);
            if (id != null && !Objects.equals(id, supplier.get())) {
                consumer.accept(id);
                onChanged.run();
            }
        });
    }

    static UIElement chemicalTag(
            String nameKey,
            Supplier<ResourceLocation> supplier,
            Consumer<ResourceLocation> consumer,
            Runnable onChanged
    ) {
        var current = TagKey.create(
                MekanismAPI.CHEMICAL_REGISTRY_NAME,
                Objects.requireNonNullElse(supplier.get(),
                        ResourceLocation.fromNamespaceAndPath("mekanism", "clean"))
        );
        return configure(nameKey, new ChemicalTagSearchBox(current), tag -> {
            if (!Objects.equals(tag.location(), supplier.get())) {
                consumer.accept(tag.location());
                onChanged.run();
            }
        });
    }

    private static <T> UIElement configure(
            String nameKey,
            RegistrySearchBox<T> searchBox,
            Consumer<T> onSelected
    ) {
        searchBox.setOnValueChanged(value -> {
            if (value != null) {
                onSelected.accept(value);
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

    private static Chemical fallbackChemical() {
        var registry = MekanismAPI.CHEMICAL_REGISTRY;
        var oxygen = registry.getOptional(ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen"));
        if (oxygen.isPresent() && isUsableChemical(oxygen.get())) {
            return oxygen.get();
        }
        for (var chemical : registry) {
            if (isUsableChemical(chemical)) {
                return chemical;
            }
        }
        return registry.get(registry.getDefaultKey());
    }

    private static boolean isUsableChemical(Chemical chemical) {
        return chemical != null && !MekanismAPI.EMPTY_CHEMICAL_KEY.location().equals(
                MekanismAPI.CHEMICAL_REGISTRY.getKey(chemical));
    }

    private static final class ChemicalSearchBox extends RegistrySearchBox<Chemical> {
        private ChemicalSearchBox(Chemical defaultValue) {
            super(
                    defaultValue,
                    () -> MekanismAPI.CHEMICAL_REGISTRY,
                    MekanismAPI.CHEMICAL_REGISTRY::getKey,
                    value -> Objects.toString(MekanismAPI.CHEMICAL_REGISTRY.getKey(value), ""),
                    ChemicalSearchBox::search,
                    UIElementProvider.text(Chemical::getTextComponent)
            );
        }

        private static void search(String word, IResultHandler<Chemical> result) {
            searchRegistry(
                    MekanismAPI.CHEMICAL_REGISTRY,
                    word,
                    result,
                    value -> value.getTranslationKey() + " " + value.getTextComponent().getString()
            );
        }
    }

    private static final class ChemicalTagSearchBox extends RegistrySearchBox<TagKey<Chemical>> {
        private ChemicalTagSearchBox(TagKey<Chemical> defaultValue) {
            super(
                    defaultValue,
                    () -> MekanismAPI.CHEMICAL_REGISTRY,
                    TagKey::location,
                    tag -> tag.location().toString(),
                    ChemicalTagSearchBox::search,
                    UIElementProvider.text(tag -> Component.literal("#" + tag.location()))
            );
        }

        private static void search(String word, IResultHandler<TagKey<Chemical>> result) {
            var query = word.toLowerCase(Locale.ROOT);
            MekanismAPI.CHEMICAL_REGISTRY.getTagNames()
                    .sorted(Comparator.comparing(tag -> tag.location().toString()))
                    .takeWhile(tag -> !Thread.currentThread().isInterrupted())
                    .filter(tag -> tag.location().toString().toLowerCase(Locale.ROOT).contains(query))
                    .forEach(result::acceptResult);
        }
    }
}
