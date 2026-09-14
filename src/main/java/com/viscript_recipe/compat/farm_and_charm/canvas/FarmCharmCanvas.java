package com.viscript_recipe.compat.farm_and_charm.canvas;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.farm_and_charm.FarmCharmRecipeKind;
import com.viscript_recipe.compat.farm_and_charm.data.*;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static com.viscript_recipe.compat.farm_and_charm.canvas.FarmCharmCanvasFactory.*;
import static com.viscript_recipe.gui.views.PropertiesView.createItemStackConfigurator;

/** Uses each native JEI layout with editable empty cells and preserves all ingredient alternatives. */
public final class FarmCharmCanvas extends RecipeCanvas<FarmCharmRecipeData> {
    private final FarmCharmRecipeKind kind;
    private final Map<Integer, FarmCharmIngredientData> ingredients = new HashMap<>();
    private final Map<Integer, Integer> selectedAlternatives = new HashMap<>();

    public FarmCharmCanvas(NavigationView navigation, RecipeEntry entry) {
        super(navigation, entry);
        kind = FarmCharmRecipeKind.byType(entry.getType()).orElseThrow();
    }

    @Override
    public UIElement createCanvas() {
        int width = kind == FarmCharmRecipeKind.CRAFTING_BOWL ? 176 : kind.hasProcessingCategory() ? 150 : 124;
        int height = kind == FarmCharmRecipeKind.CRAFTING_BOWL ? 85 : kind.hasProcessingCategory() ? 50 : 60;
        var panel = panel("farm_charm_" + kind.typeId().getPath(), width, height);
        String texture = switch (kind) {
            case COOKING_POT -> "pot_gui";
            case ROASTER -> "roaster_gui";
            case STOVE -> "stove_gui";
            case CRAFTING_BOWL -> "crafting_bowl";
            default -> null;
        };
        if (texture != null) {
            panel.style(style -> style.backgroundTexture(texture(texture,
                    kind == FarmCharmRecipeKind.CRAFTING_BOWL ? 0 : 26,
                    kind == FarmCharmRecipeKind.CRAFTING_BOWL ? 0 : 13, width, height)));
        }
        for (int i = 0; i < kind.inputCount(); i++) {
            var input = createIngredientSlot(i, 18);
            int x = switch (kind) {
                case COOKING_POT, ROASTER -> 3 + i % 3 * 18;
                case STOVE -> 2 + i * 18;
                case CRAFTING_BOWL -> i < 2 ? 49 : 31;
                default -> 29;
            };
            int y = switch (kind) {
                case COOKING_POT, ROASTER -> 3 + i / 3 * 18;
                case STOVE -> 4;
                case CRAFTING_BOWL -> 24 + i % 2 * 18;
                default -> 14;
            };
            if (texture == null || kind == FarmCharmRecipeKind.CRAFTING_BOWL) panel.addChild(standardSlot(input, "farm_charm_input_" + i, x, y));
            else {
                configureJeiOverlaySlotVisual(input);
                panel.addChild(slot(input, "farm_charm_input_" + i, x, y));
            }
        }
        var output = createOutputSlot(0, 18);
        int outputX = switch (kind) {
            case COOKING_POT, ROASTER -> 97;
            case CRAFTING_BOWL -> 109;
            default -> 99;
        };
        int outputY = switch (kind) {
            case STOVE -> 28;
            case CRAFTING_BOWL -> 34;
            default -> 14;
        };
        if (texture == null) {
            panel.addChildren(standardSlot(output, "farm_charm_output", outputX, outputY), arrow(64, 15));
        } else {
            configureJeiOverlaySlotVisual(output);
            panel.addChild(slot(output, "farm_charm_output", outputX, outputY));
        }
        if (kind.hasContainer()) {
            var container = createExtraItemSlot(18, Component.translatable(key("container")));
            configureJeiOverlaySlotVisual(container);
            panel.addChild(slot(container, "farm_charm_container", 68, 41));
            panel.addChild(at(new UIElement().style(style -> style.backgroundTexture(texture(texture, 178, 15, 18, 30))), 69, 1, 18, 30));
        } else if (kind == FarmCharmRecipeKind.STOVE) {
            panel.addChildren(at(new UIElement().style(style -> style.backgroundTexture(texture(texture, 178, 20, 18, 25))), 67, 19, 18, 25),
                    at(new UIElement().style(style -> style.backgroundTexture(texture(texture, 176, 0, 17, 15))), 36, 36, 17, 15));
        }
        return centered(panel);
    }

    private static SpriteTexture texture(String name, int u, int v, int width, int height) {
        return SpriteTexture.of(FarmCharmRecipeKind.id("textures/gui/" + name + ".png")).setSprite(u, v, width, height);
    }

    @Override
    public void load() {
        var data = getData();
        for (int i = 0; i < kind.inputCount(); i++) {
            loadInput(i, i < data.getInputs().size() ? data.getInputs().get(i) : new FarmCharmIngredientData());
        }
        setVisualOutput(0, data.getResult());
        if (kind.hasContainer()) extraItemSlots[0].setItem(data.getContainer().copy(), false);
    }

    private void loadInput(int index, FarmCharmIngredientData ingredient) {
        ingredients.put(index, ingredient);
        int selected = Math.min(selectedAlternatives.getOrDefault(index, 0), Math.max(0, ingredient.getAlternatives().size() - 1));
        selectedAlternatives.put(index, selected);
        loadIngredientSlot(index, ingredient.getAlternatives().isEmpty() ? RecipeIngredient.empty() : ingredient.getAlternatives().get(selected));
    }

    private FarmCharmIngredientData saveInput(int index) {
        var data = ingredients.computeIfAbsent(index, unused -> new FarmCharmIngredientData());
        int selected = selectedAlternatives.getOrDefault(index, 0);
        var current = getVisualIngredient(index);
        if (!current.isEmpty() || !data.getAlternatives().isEmpty()) {
            while (data.getAlternatives().size() <= selected) data.getAlternatives().add(RecipeIngredient.empty());
            data.getAlternatives().set(selected, current);
        }
        return data;
    }

    @Override
    public void save() {
        var inputs = new ArrayList<FarmCharmIngredientData>();
        for (int i = 0; i < kind.inputCount(); i++) inputs.add(saveInput(i));
        // Keep invalid overflow data available for validation instead of silently dropping it.
        if (getData().getInputs().size() > kind.inputCount()) inputs.addAll(getData().getInputs().subList(kind.inputCount(), getData().getInputs().size()));
        getData().setInputs(inputs).setResult(getVisualOutput(0).getItem());
        if (kind.hasContainer()) getData().setContainer(getExtraItem(0));
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        int index = selectedSlotIndex();
        var ingredient = saveInput(index);
        content.addChild(intField(key("alternative"), selectedAlternatives.getOrDefault(index, 0) + 1, 1,
                Math.max(1, ingredient.getAlternatives().size() + 1), value -> {
                    saveInput(index);
                    while (ingredient.getAlternatives().size() < value) ingredient.getAlternatives().add(RecipeIngredient.empty());
                    selectedAlternatives.put(index, value - 1);
                    loadInput(index, ingredient);
                    reloadProperties();
                }, Component.translatable(key("alternative_hint"))).setId("farm_charm_alternative"));
        super.buildIngredientProperties(content);
    }

    @Override
    public void buildExtraItemProperties(UIElement content) {
        content.addChild(sectionTitle(key("container")));
        content.addChild(createItemStackConfigurator(key("container"), () -> getExtraItem(0),
                stack -> extraItemSlots[0].setItem(stack.copy(), true)));
        buildRecipeProperties(content);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChild(sectionTitle(kind.translationKey()));
        if (kind == FarmCharmRecipeKind.COOKING_POT) content.addChild(switchField(key("require_container"), data.isContainerRequired(),
                data::setContainerRequired).setId("farm_charm_require_container"));
        if (kind.hasLearning()) content.addChild(switchField(key("requires_learning"), data.isRequiresLearning(),
                data::setRequiresLearning, Component.translatable(key("requires_learning_hint"))).setId("farm_charm_requires_learning"));
        if (kind == FarmCharmRecipeKind.STOVE) content.addChild(floatField(key("experience"), data.getExperience(), -Float.MAX_VALUE,
                Float.MAX_VALUE, data::setExperience).setId("farm_charm_experience"));
        if (kind.hasProcessingCategory()) content.addChild(textField(key("processing_category"), data.getProcessingCategory(),
                data::setProcessingCategory, Component.translatable(key("processing_category_hint"))).setId("farm_charm_processing_category"));
    }

    private static String key(String path) { return "viscript_recipe.editor.farm_and_charm." + path; }
}
