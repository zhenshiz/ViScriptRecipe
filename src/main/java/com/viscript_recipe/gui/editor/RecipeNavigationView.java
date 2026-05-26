package com.viscript_recipe.gui.editor;

import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEntry;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;

public class RecipeNavigationView extends View {
    private final RecipeEditorController controller;
    private final UIElement entryList = RecipeEditorUi.column();
    private final UIElement addRow = createAddRow();

    public RecipeNavigationView(RecipeEditorController controller) {
        super("viscript_recipe.view.recipe_navigation", Icons.FILE);
        this.controller = controller;
        addChild(createRoot());
        controller.addListener(this::refresh);
        refresh();
    }

    public void saveCurrentVisualState() {
        controller.saveVisualState();
    }

    private UIElement createRoot() {
        var root = RecipeEditorUi.panelRoot();
        root.addChildren(
                RecipeEditorUi.sectionTitle("viscript_recipe.editor.entries"),
                RecipeEditorUi.fieldGroup("viscript_recipe.editor.workstation", createWorkstationSelector()),
                addRow,
                createEntryScroller()
        );
        return root;
    }

    private Selector<RecipeEditorCategory> createWorkstationSelector() {
        var selector = new Selector<RecipeEditorCategory>()
                .setCandidates(controller.availableCategories())
                .setCandidateUIProvider(this::createWorkstationCandidate)
                .setSelected(controller.getSelectedCategoryData(), false)
                .setOnValueChanged(controller::setSelectedCategory);
        selector.layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
        });
        return selector;
    }

    private UIElement createWorkstationCandidate(RecipeEditorCategory category) {
        if (category == null) {
            return RecipeEditorUi.label(Component.literal("---")).layout(layout -> {
                layout.widthPercent(100);
                layout.height(14);
            });
        }
        var name = RecipeEditorUi.label(category.displayName())
                .textStyle(style -> style
                        .textWrap(TextWrap.HOVER_ROLL));
        var owner = RecipeEditorUi.label(category.ownerName())
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.RIGHT)
                        .textColor(ColorPattern.LIGHT_GRAY.color)
                        .textWrap(TextWrap.HOVER_ROLL));
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(14);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(
                name.layout(layout -> layout.flex(1).heightPercent(100)),
                owner.layout(layout -> layout.width(72).heightPercent(100))
        );
    }

    private UIElement createAddRow() {
        return RecipeEditorUi.row().layout(layout -> {
            layout.widthPercent(100);
            layout.height(20);
            layout.gapAll(3);
        }).addChildren(
                RecipeEditorUi.iconButton(Icons.ADD, "viscript_recipe.editor.add_recipe", event -> controller.addEntry())
                        .layout(layout -> layout.flex(1).height(20))
        );
    }

    private ScrollerView createEntryScroller() {
        var scroller = new ScrollerView();
        scroller.layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
        });
        scroller.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        scroller.viewPort.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        scroller.viewPort.layout(layout -> layout.paddingAll(0));
        scroller.verticalScroller.headButton.setDisplay(false);
        scroller.verticalScroller.tailButton.setDisplay(false);
        scroller.horizontalScroller.setDisplay(false);
        scroller.addScrollViewChild(entryList.layout(layout -> {
            layout.widthPercent(100);
            layout.gapAll(2);
        }));
        return scroller;
    }

    private void refresh() {
        entryList.clearAllChildren();
        var entries = controller.recipeFile().getEntries();
        var visibleIndex = 0;
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            if (controller.isEntryInSelectedCategory(entry)) {
                entryList.addChild(createEntryRow(entry, visibleIndex++));
            }
        }
    }

    private UIElement createEntryRow(RecipeEntry entry, int index) {
        var selected = entry == controller.getSelectedEntry();
        var label = entryLabel(entry, index);
        var button = new Button()
                .setText(label)
                .setOnClick(event -> controller.selectEntry(entry))
                .textStyle(style -> style
                        .textAlignHorizontal(Horizontal.LEFT)
                        .textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveWidth(false)
                        .textColor(entryTextColor(entry)));
        button.text.layout(layout -> {
            layout.flex(1);
            layout.heightPercent(100);
        });
        button.text.setOverflowVisible(false);
        button.layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
        });
        button.setOverflowVisible(false);
        button.buttonStyle(style -> style
                .baseTexture(selected ? ColorPattern.GRAY.rectTexture() : ColorPattern.DARK_GRAY.rectTexture())
                .hoverTexture(ColorPattern.GRAY.rectTexture())
                .pressedTexture(ColorPattern.SEAL_BLACK.rectTexture()));
        return new UIElement().layout(layout -> {
            layout.widthPercent(100);
            layout.height(22);
            layout.gapAll(2);
            layout.flexDirection(FlexDirection.ROW);
        }).addChildren(
                button.layout(layout -> layout.flex(1)),
                RecipeEditorUi.iconButton(Icons.DELETE, "viscript_recipe.editor.delete", event -> controller.removeEntry(entry))
                        .layout(layout -> layout.width(20).height(22))
        );
    }

    private int entryTextColor(RecipeEntry entry) {
        if (!entry.isEnabled()) {
            return ColorPattern.LIGHT_GRAY.color;
        }
        return ColorPattern.WHITE.color;
    }

    private Component entryLabel(RecipeEntry entry, int index) {
        var id = entry.getRecipeId() == null ? "?" : entry.getRecipeId().toString();
        return Component.translatable(
                "viscript_recipe.editor.entry_label",
                index + 1,
                controller.typeDisplayName(entry),
                id
        );
    }
}
