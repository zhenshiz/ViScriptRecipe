package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Displays the recipes in the current file grouped by their editor workstation.
 * Each workstation starts collapsed so files containing many recipes remain easy to scan.
 */
final class RecipeWorkstationSummaryDialog {
    private RecipeWorkstationSummaryDialog() {
    }

    static void show(RecipeProject project, ModularUI modularUI) {
        project.saveCurrentVisualState();
        var groups = collectGroups(project.getRecipeFile().getEntries());
        var dialog = createDialog(project, groups);
        dialog.show(modularUI);
    }

    private static Dialog createDialog(RecipeProject project, List<WorkstationGroup> groups) {
        var dialog = new Dialog()
                .setTitle("viscript_recipe.editor.workstation_summary.title")
                .darkenBackground()
                .setAutoClose(true);
        dialog.setId("recipe_workstation_summary_dialog");
        dialog.overlay.layout(layout -> {
            layout.widthPercent(80);
            layout.minWidth(240);
            layout.maxWidth(420);
            layout.heightPercent(80);
            layout.minHeight(180);
            layout.maxHeight(320);
        });
        dialog.contentContainer.layout(layout -> {
            layout.flex(1);
            layout.minHeight(0);
        });

        var totalRecipes = groups.stream().mapToInt(group -> group.entries().size()).sum();
        var enabledRecipes = groups.stream().flatMap(group -> group.entries().stream())
                .filter(RecipeEntry::isEnabled)
                .count();
        var content = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.minHeight(0);
            layout.gapAll(4);
        });
        content.addChild(RecipeEditorUi.label(Component.translatable(
                "viscript_recipe.editor.workstation_summary.overview",
                groups.size(), totalRecipes, enabledRecipes
        )).layout(layout -> layout.widthPercent(100)));

        if (groups.isEmpty()) {
            content.addChild(RecipeEditorUi.label(Component.translatable(
                    "viscript_recipe.editor.workstation_summary.empty"
            )).layout(layout -> {
                layout.widthPercent(100);
                layout.flex(1);
            }));
        } else {
            content.addChild(createScroller(project, dialog, groups));
        }

        dialog.addContent(content);
        dialog.addButton(new Button()
                .setText("viscript_recipe.editor.workstation_summary.close")
                .setOnClick(event -> dialog.close())
                .addClass("__confirm-button__"));
        return dialog;
    }

    private static ScrollerView createScroller(RecipeProject project, Dialog dialog,
                                                List<WorkstationGroup> groups) {
        var list = RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(5);
        });
        for (var group : groups) {
            list.addChild(createWorkstationGroup(project, dialog, group));
        }

        var scroller = new ScrollerView();
        scroller.setId("recipe_workstation_summary_scroller");
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.minHeight(0);
        });
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(list);
        return scroller;
    }

    private static UIElement createWorkstationGroup(RecipeProject project, Dialog dialog,
                                                     WorkstationGroup group) {
        var enabledCount = group.entries().stream().filter(RecipeEntry::isEnabled).count();
        var counts = RecipeEditorUi.label(Component.translatable(
                "viscript_recipe.editor.workstation_summary.workstation_counts",
                group.entries().size(), enabledCount
        )).textStyle(style -> style
                .textAlignHorizontal(Horizontal.RIGHT)
                .textWrap(TextWrap.HOVER_ROLL)
                .adaptiveWidth(false));
        counts.layout(layout -> {
            layout.width(96);
            layout.height(18);
        });
        counts.setOverflowVisible(false);

        var groupElement = new ConfiguratorGroup();
        groupElement.setLabel(group.displayName());
        groupElement.setCollapse(true);
        groupElement.addClass("recipe_workstation_summary_group");
        groupElement.layout(layout -> layout.widthPercent(100));
        groupElement.label.textStyle(style -> style
                .textWrap(TextWrap.HOVER_ROLL)
                .adaptiveWidth(false));
        groupElement.label.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.height(18);
        });
        groupElement.label.setOverflowVisible(false);
        groupElement.lineContainer.addClass("recipe_workstation_summary_header");
        groupElement.lineContainer.layout(layout -> {
            layout.widthPercent(100);
            layout.minWidth(0);
            layout.height(22);
        });
        groupElement.inlineContainer.addChild(counts);
        groupElement.lineContainer.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.workstation_summary.toggle"
        )));
        groupElement.configuratorContainer.addClass("recipe_workstation_summary_entries");
        groupElement.configuratorContainer.layout(layout -> layout.widthPercent(100));

        for (var entry : group.entries()) {
            groupElement.configuratorContainer.addChild(createRecipeButton(project, dialog, entry));
        }
        return groupElement;
    }

    private static Button createRecipeButton(RecipeProject project, Dialog dialog, RecipeEntry entry) {
        var typeName = RecipeEditorTypes.get(entry.getType())
                .map(RecipeEditorType::displayName)
                .orElseGet(() -> Component.literal(entry.getType().toString()));
        var state = Component.translatable(entry.isEnabled()
                ? "viscript_recipe.editor.workstation_summary.enabled"
                : "viscript_recipe.editor.workstation_summary.disabled");
        var button = new Button()
                .addPreIcon(operationIcon(entry))
                .setText(Component.translatable(
                        "viscript_recipe.editor.workstation_summary.entry",
                        entry.getRecipeId().toString(), typeName, entry.getOperation().displayName(), state
                ))
                .setOnClick(event -> {
                    dialog.close();
                    project.selectEntry(entry);
                })
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false));
        button.addClass("recipe_workstation_summary_entry");
        button.layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
        });
        button.text.layout(layout -> {
            layout.flex(1);
            layout.minWidth(0);
            layout.heightPercent(100);
        });
        button.text.setOverflowVisible(false);
        button.style(style -> style.tooltips(Component.translatable(
                "viscript_recipe.editor.workstation_summary.locate", entry.getRecipeId().toString()
        )));
        return button;
    }

    private static IGuiTexture operationIcon(RecipeEntry entry) {
        return switch (entry.getOperation()) {
            case ADD -> Icons.ADD;
            case REPLACE -> Icons.EDIT_FILE;
            case REMOVE -> Icons.DELETE;
        };
    }

    private static List<WorkstationGroup> collectGroups(List<RecipeEntry> entries) {
        var grouped = new LinkedHashMap<ResourceLocation, List<RecipeEntry>>();
        for (var entry : entries) {
            var categoryId = RecipeEditorTypes.get(entry.getType())
                    .map(RecipeEditorType::category)
                    .orElse(entry.getType());
            grouped.computeIfAbsent(categoryId, ignored -> new ArrayList<>()).add(entry);
        }

        var groups = new ArrayList<WorkstationGroup>(grouped.size());
        for (var groupedEntries : grouped.entrySet()) {
            var category = RecipeEditorTypes.getCategory(groupedEntries.getKey()).orElse(null);
            groups.add(new WorkstationGroup(groupedEntries.getKey(), category, List.copyOf(groupedEntries.getValue())));
        }
        return List.copyOf(groups);
    }

    private record WorkstationGroup(ResourceLocation id, RecipeEditorCategory category, List<RecipeEntry> entries) {
        Component displayName() {
            if (category != null) {
                return Component.translatable(
                        "viscript_recipe.editor.workstation_summary.workstation",
                        category.displayName(), category.ownerName()
                );
            }
            return Component.translatable(
                    "viscript_recipe.editor.workstation_summary.unknown_workstation", id.toString()
            );
        }
    }
}
