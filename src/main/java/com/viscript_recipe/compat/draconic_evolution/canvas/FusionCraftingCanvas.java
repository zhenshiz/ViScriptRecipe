package com.viscript_recipe.compat.draconic_evolution.canvas;

import com.brandon3055.brandonscore.api.TechLevel;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionIngredientData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionRecipeData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicIngredientData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Editable native JEI layout. Pages retain every injector, even for unusually large custom recipes. */
public class FusionCraftingCanvas extends RecipeCanvas<DraconicFusionRecipeData> {
    public static final int PAGE_SIZE = 32;
    private final Map<Integer, Integer> selectedAlternatives = new HashMap<>();
    private Label tierLabel;
    private Label energyLabel;
    private int page;
    private int visibleInjectors;

    public FusionCraftingCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() {
        loadInput(0, getData().getCatalyst());
        for (int i = 0; i < visibleInjectors; i++) loadInput(i + 1, injector(i + 1).getIngredient());
        setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        saveInput(0, getData().getCatalyst());
        for (int i = 0; i < visibleInjectors; i++) saveInput(i + 1, injector(i + 1).getIngredient());
        getData().setResult(getVisualOutput(0).getItem());
    }

    private int inputKey(int slot) { return slot == 0 ? -1 : page * PAGE_SIZE + slot - 1; }

    private DraconicFusionIngredientData injector(int slot) {
        return getData().getInjectors().get(inputKey(slot));
    }

    private DraconicIngredientData input(int slot) {
        return slot == 0 ? getData().getCatalyst() : injector(slot).getIngredient();
    }

    private void loadInput(int slot, DraconicIngredientData input) {
        int selected = Math.min(selectedAlternatives.getOrDefault(inputKey(slot), 0),
                Math.max(0, input.getAlternatives().size() - 1));
        selectedAlternatives.put(inputKey(slot), selected);
        loadIngredientSlot(slot, input.getAlternatives().isEmpty() ? RecipeIngredient.empty()
                : input.getAlternatives().get(selected).copy().setCount(input.getCount()));
    }

    private void saveInput(int slot, DraconicIngredientData input) {
        int selected = selectedAlternatives.getOrDefault(inputKey(slot), 0);
        var current = getVisualIngredient(slot);
        if (input.getAlternatives().isEmpty() && current.isEmpty()) return;
        while (input.getAlternatives().size() <= selected) input.getAlternatives().add(RecipeIngredient.empty());
        // The count belongs to the whole native StackIngredient, rather than one alternative.
        input.getAlternatives().set(selected, current.copy().setCount(1));
    }

    @Override
    public UIElement createCanvas() {
        int size = getData().getInjectors().size();
        page = Math.min(page, Math.max(0, (size - 1) / PAGE_SIZE));
        visibleInjectors = Math.min(PAGE_SIZE, size - page * PAGE_SIZE);
        boolean skin = FusionCraftingCanvasFactory.hasJeiSkin();
        var catalyst = createIngredientSlot(0, JEI_SLOT_SIZE);
        catalyst.setId("de_catalyst");
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        output.setId("de_output");
        tooltip(catalyst, "viscript_recipe.editor.draconicevolution.catalyst");
        if (skin) configureJeiOverlaySlotVisual(catalyst, output);
        var injectors = new UIElement[visibleInjectors];
        for (int i = 0; i < injectors.length; i++) {
            var slot = createIngredientSlot(i + 1, JEI_SLOT_SIZE);
            slot.setId("de_injector_" + (page * PAGE_SIZE + i));
            tooltip(slot, Component.translatable("viscript_recipe.editor.draconicevolution.injector", page * PAGE_SIZE + i + 1));
            if (skin) configureJeiOverlaySlotVisual(slot);
            injectors[i] = slot;
        }
        tierLabel = RecipeEditorUi.label(getData().getTechLevel().getDisplayName());
        energyLabel = RecipeEditorUi.label(energyText());
        return FusionCraftingCanvasFactory.create(catalyst, injectors, output, tierLabel, energyLabel, skin);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.draconicevolution.fusion_crafting"),
                selector("viscript_recipe.config.draconicevolution.tech_level", List.of(TechLevel.values()),
                        data.getTechLevel(), TechLevel::getDisplayName, value -> {
                            data.setTechLevel(value);
                            tierLabel.setText(value.getDisplayName());
                        }),
                field("viscript_recipe.config.draconicevolution.total_energy", RecipeEditorUi.longField(
                        data.getTotalEnergy(), 1, Long.MAX_VALUE, value -> {
                            data.setTotalEnergy(value);
                            energyLabel.setText(energyText());
                        })),
                intField("viscript_recipe.config.draconicevolution.injector_slots", data.getInjectors().size(),
                        1, Math.max(1024, data.getInjectors().size()), this::resizeInjectors,
                        Component.translatable("viscript_recipe.config.draconicevolution.injector_slots_hint")));
        if (data.getInjectors().size() > PAGE_SIZE) {
            content.addChild(intField("viscript_recipe.config.draconicevolution.page", page + 1,
                    1, (data.getInjectors().size() + PAGE_SIZE - 1) / PAGE_SIZE, value -> showPage(value - 1)));
        }
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int slot = selectedSlotIndex();
        var input = input(slot);
        saveInput(slot, input);
        int selected = selectedAlternatives.getOrDefault(inputKey(slot), 0);
        content.addChildren(intField("viscript_recipe.config.draconicevolution.alternative", selected + 1,
                1, input.getAlternatives().size() + 1, value -> {
                    saveInput(slot, input);
                    while (input.getAlternatives().size() < value) input.getAlternatives().add(RecipeIngredient.empty());
                    selectedAlternatives.put(inputKey(slot), value - 1);
                    loadInput(slot, input);
                    reloadProperties();
                }, Component.translatable("viscript_recipe.config.draconicevolution.alternative_hint")),
                intField("viscript_recipe.config.draconicevolution.count", input.getCount(), 1, Integer.MAX_VALUE,
                        value -> { saveInput(slot, input); input.setCount(value); loadInput(slot, input); },
                        Component.translatable(slot == 0 ? "viscript_recipe.config.draconicevolution.catalyst_count_hint"
                                : "viscript_recipe.config.draconicevolution.injector_count_hint")));
        if (slot > 0) content.addChild(switchField("viscript_recipe.config.draconicevolution.consume",
                injector(slot).isConsume(), injector(slot)::setConsume));
        super.buildIngredientProperties(content);
    }

    private Component energyText() {
        return Component.literal(String.format(java.util.Locale.ROOT, "%,d OP", getData().getTotalEnergy()));
    }

    public void showPage(int nextPage) {
        save();
        page = Math.clamp(nextPage, 0, Math.max(0, (getData().getInjectors().size() - 1) / PAGE_SIZE));
        rebuild();
    }

    private void resizeInjectors(int size) {
        save();
        var inputs = getData().getInjectors();
        while (inputs.size() < size) inputs.add(new DraconicFusionIngredientData());
        while (inputs.size() > size) inputs.removeLast();
        rebuild();
    }

    private void rebuild() {
        clearAllChildren();
        initVisualState();
        load();
        if (navigationView != null) selectRecipe();
    }
}
