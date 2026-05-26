package com.viscript_recipe.gui.editor;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeFile;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import com.viscript_recipe.data.vanilla.SmithingTransformRecipeData;
import com.viscript_recipe.data.vanilla.StonecuttingRecipeData;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class RecipeEditorController {
    private static final char[] SHAPED_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    @Getter
    private final RecipeProject project;
    private final List<Runnable> listeners = new ArrayList<>();

    @Getter
    @Nullable
    private RecipeEntry selectedEntry;
    @Getter
    private ResourceLocation selectedCategory = RecipeEditorTypes.CRAFTING_TABLE;
    @Getter
    private WorkbenchSlotSelection slotSelection = WorkbenchSlotSelection.RECIPE;

    private ItemStack[] visualIngredients = emptyIngredientStacks();
    private RecipeIngredient[] visualIngredientData = emptyIngredientData();
    private CraftingRemainderRule[] visualRemainders = emptyRemainderData();
    private ItemStack visualResult = ItemStack.EMPTY;
    private ItemStack[] loadedIngredientStacks = emptyIngredientStacks();
    private CraftingRemainderRule[] loadedRemainders = emptyRemainderData();
    @Getter
    private boolean selectedContainsUnsupportedIngredients;
    @Getter
    private boolean refreshing;

    public RecipeEditorController(RecipeProject project) {
        this.project = project;
        selectedEntry = findFirstAvailableEntry();
        selectedCategory = selectedEntry == null ? firstAvailableCategoryId() : categoryOf(selectedEntry);
        if (selectedEntry == null) {
            addEntry();
        } else {
            loadSelectedEntryToVisualState();
        }
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void notifyChanged() {
        for (var listener : List.copyOf(listeners)) {
            listener.run();
        }
    }

    public RecipeFile recipeFile() {
        return project.getRecipeFile();
    }

    private ResourceLocation firstAvailableCategoryId() {
        return RecipeEditorTypes.availableCategories().stream()
                .findFirst()
                .map(RecipeEditorCategory::id)
                .orElse(RecipeEditorTypes.CRAFTING_TABLE);
    }

    @Nullable
    private RecipeEntry findFirstAvailableEntry() {
        for (var entry : recipeFile().getEntries()) {
            if (RecipeEditorTypes.get(entry.getType())
                    .filter(RecipeEditorType::isAvailable)
                    .flatMap(type -> RecipeEditorTypes.getCategory(type.category()))
                    .filter(RecipeEditorCategory::isAvailable)
                    .isPresent()) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private RecipeEntry findFirstEntryInCategory(ResourceLocation category) {
        for (var entry : recipeFile().getEntries()) {
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private RecipeEntry findNearestEntryInCategory(int preferredIndex, ResourceLocation category) {
        var entries = recipeFile().getEntries();
        if (entries.isEmpty()) {
            return null;
        }
        for (int i = Math.min(preferredIndex, entries.size() - 1); i >= 0; i--) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        for (int i = Math.max(0, preferredIndex); i < entries.size(); i++) {
            var entry = entries.get(i);
            if (isEntryInCategory(entry, category)) {
                return entry;
            }
        }
        return null;
    }

    public boolean isEntryInSelectedCategory(RecipeEntry entry) {
        return isEntryInCategory(entry, selectedCategory);
    }

    private boolean isEntryInCategory(RecipeEntry entry, ResourceLocation category) {
        return RecipeEditorTypes.isInCategory(entry.getType(), category);
    }

    private ResourceLocation categoryOf(RecipeEntry entry) {
        return RecipeEditorTypes.get(entry.getType())
                .map(RecipeEditorType::category)
                .orElse(selectedCategory);
    }

    public void addEntry() {
        saveVisualStateToSelectedEntry();
        var type = RecipeEditorTypes.defaultTypeForCategory(selectedCategory);
        var entry = new RecipeEntry()
                .setType(type)
                .setRecipeId(ViScriptRecipe.id("recipe_" + (recipeFile().getEntries().size() + 1)));
        applyDefaultDataForType(entry, type);
        recipeFile().getEntries().add(entry);
        selectedEntry = entry;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void addWorkbenchEntry() {
        setSelectedCategory(RecipeEditorTypes.requireCategory(RecipeEditorTypes.CRAFTING_TABLE));
        addEntry();
    }

    public void addShapedEntry() {
        addWorkbenchEntry();
    }

    private void applyDefaultDataForType(RecipeEntry entry, ResourceLocation type) {
        if (type.equals(RecipeEditorTypes.BLASTING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.SMOKING)) {
            entry.getCooking().setCookingTime(100);
        } else if (type.equals(RecipeEditorTypes.CAMPFIRE_COOKING)) {
            entry.getCooking().setCookingTime(600);
        } else if (type.equals(RecipeEditorTypes.SMELTING)) {
            entry.getCooking().setCookingTime(200);
        }
    }

    public void removeEntry(RecipeEntry entry) {
        var entries = recipeFile().getEntries();
        var removedIndex = entries.indexOf(entry);
        entries.remove(entry);
        var nextEntry = findNearestEntryInCategory(removedIndex, selectedCategory);
        if (nextEntry == null) {
            selectedEntry = null;
            slotSelection = WorkbenchSlotSelection.RECIPE;
            clearVisualState();
        } else {
            selectedEntry = nextEntry;
            slotSelection = WorkbenchSlotSelection.RECIPE;
            loadSelectedEntryToVisualState();
        }
        notifyChanged();
    }

    public List<RecipeEditorCategory> availableCategories() {
        return RecipeEditorTypes.availableCategories();
    }

    public List<RecipeEditorType> availableTypesForSelectedCategory() {
        return RecipeEditorTypes.availableInCategory(selectedCategory);
    }

    @Nullable
    public RecipeEditorCategory getSelectedCategoryData() {
        return RecipeEditorTypes.getCategory(selectedCategory).orElse(null);
    }

    public Component selectedCategoryDisplayName() {
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::displayName)
                .orElseGet(() -> Component.translatable("viscript_recipe.editor.category.unknown", selectedCategory.toString()));
    }

    public void setSelectedCategory(RecipeEditorCategory category) {
        if (category == null || !category.isAvailable() || selectedCategory.equals(category.id())) {
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedCategory = category.id();
        selectedEntry = findFirstEntryInCategory(selectedCategory);
        slotSelection = WorkbenchSlotSelection.RECIPE;
        if (selectedEntry == null) {
            clearVisualState();
        } else {
            loadSelectedEntryToVisualState();
        }
        notifyChanged();
    }

    @Nullable
    public RecipeEditorType getSelectedRecipeType() {
        return selectedEntry == null ? null : RecipeEditorTypes.get(selectedEntry.getType()).orElse(null);
    }

    @Nullable
    public RecipeEditorType getSelectedWorkbenchType() {
        return getSelectedRecipeType();
    }

    public Component typeDisplayName(RecipeEntry entry) {
        var typeId = entry.getType();
        return RecipeEditorTypes.get(typeId)
                .map(RecipeEditorType::displayName)
                .orElseGet(() -> Component.translatable("viscript_recipe.editor.type.unknown", typeId.toString()));
    }

    public void setSelectedRecipeType(RecipeEditorType type) {
        if (selectedEntry == null || type == null || selectedEntry.getType().equals(type.id())) {
            return;
        }
        if (!type.isAvailable() || !type.category().equals(selectedCategory)) {
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedEntry.setType(type.id());
        applyDefaultDataForType(selectedEntry, type.id());
        if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            writeShapedRecipe(selectedEntry.getShaped());
        } else if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            writeShapelessRecipe(selectedEntry.getShapeless());
        } else if (isCookingEntry(selectedEntry)) {
            writeCookingRecipe(selectedEntry.getCooking());
        } else if (isStonecuttingEntry(selectedEntry)) {
            writeStonecuttingRecipe(selectedEntry.getStonecutting());
        } else if (isSmithingTransformEntry(selectedEntry)) {
            writeSmithingTransformRecipe(selectedEntry.getSmithingTransform());
        }
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void setSelectedWorkbenchType(RecipeEditorType type) {
        setSelectedRecipeType(type);
    }

    public void selectEntry(RecipeEntry entry) {
        if (selectedEntry == entry) {
            selectRecipeProperties();
            return;
        }
        saveVisualStateToSelectedEntry();
        selectedEntry = entry;
        slotSelection = WorkbenchSlotSelection.RECIPE;
        loadSelectedEntryToVisualState();
        notifyChanged();
    }

    public void selectRecipeProperties() {
        slotSelection = WorkbenchSlotSelection.RECIPE;
        notifyChanged();
    }

    public void selectIngredientSlot(int index) {
        if (selectedEntry == null) {
            return;
        }
        if (index < 0 || index >= 9) {
            return;
        }
        if (isSelectedSingleInputLayout() && index != 0) {
            return;
        }
        if (isSelectedSmithingLayout() && index > 2) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.ingredient(index);
        notifyChanged();
    }

    public void selectResultSlot() {
        if (selectedEntry == null) {
            return;
        }
        slotSelection = WorkbenchSlotSelection.RESULT;
        notifyChanged();
    }

    public ItemStack getVisualIngredient(int index) {
        return visualIngredients[index].copy();
    }

    public ItemStack[] getVisualIngredientTagStacks(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return new ItemStack[0];
        }
        var tag = getVisualIngredientTag(index);
        return tag == null ? new ItemStack[0] : itemsFromTag(tag);
    }

    @Nullable
    public ResourceLocation getVisualIngredientTag(int index) {
        if (index < 0 || index >= visualIngredientData.length) {
            return null;
        }
        var ingredient = visualIngredientData[index];
        if (ingredient == null || ingredient.getValues().size() != 1) {
            return null;
        }
        var value = ingredient.getValues().getFirst();
        return value.getKind() == IngredientValueKind.TAG ? value.getTag() : null;
    }

    public void setVisualIngredient(int index, ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        if (index < 0 || index >= visualIngredients.length) {
            return;
        }
        visualIngredients[index] = normalizeStack(stack);
        visualIngredientData[index] = visualIngredients[index].isEmpty()
                ? new RecipeIngredient()
                : RecipeIngredient.item(visualIngredients[index].getItem());
        if (visualIngredients[index].isEmpty()) {
            visualRemainders[index] = CraftingRemainderRule.defaultRule();
        }
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualIngredient(int index) {
        setVisualIngredient(index, ItemStack.EMPTY);
    }

    public ItemStack getVisualResult() {
        return visualResult.copy();
    }

    public void setVisualResult(ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        visualResult = stack == null ? ItemStack.EMPTY : stack.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void clearVisualResult() {
        setVisualResult(ItemStack.EMPTY);
    }

    public RecipeIngredient getSelectedIngredient() {
        if (selectedEntry == null || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return new RecipeIngredient();
        }
        var ingredient = visualIngredientData[slotSelection.index()];
        return ingredient == null ? getIngredientForSlot(selectedEntry, slotSelection.index()) : ingredient;
    }

    public CraftingRemainderRule getSelectedRemainder() {
        if (selectedEntry == null || !selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return CraftingRemainderRule.defaultRule();
        }
        var remainder = visualRemainders[slotSelection.index()];
        return remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
    }

    public void setSelectedRemainder(CraftingRemainderRule remainder) {
        if (selectedEntry == null || !selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED) || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return;
        }
        visualRemainders[slotSelection.index()] = remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
        saveVisualStateToSelectedEntry();
        notifyChanged();
    }

    public void setSelectedIngredient(RecipeIngredient ingredient) {
        if (selectedEntry == null || slotSelection.kind() != WorkbenchSlotSelection.Kind.INGREDIENT) {
            return;
        }
        setIngredientForSlot(selectedEntry, slotSelection.index(), ingredient);
        if (!selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            refreshVisualStateFromData();
        } else {
            refreshUnsupportedIngredientStatus();
        }
        notifyChanged();
    }

    public ItemStack getSelectedResult() {
        if (selectedEntry == null) {
            return ItemStack.EMPTY;
        }
        return getResult(selectedEntry).copy();
    }

    public void setSelectedResult(ItemStack stack) {
        if (selectedEntry == null) {
            return;
        }
        setResult(selectedEntry, stack == null ? ItemStack.EMPTY : stack.copy());
        visualResult = getResult(selectedEntry).copy();
        notifyChanged();
    }

    public boolean showNotification(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).showNotification(entry);
    }

    public boolean supportsNotification(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).supportsNotification();
    }

    public void setShowNotification(RecipeEntry entry, boolean value) {
        var type = RecipeEditorTypes.require(entry.getType());
        if (!type.supportsNotification()) {
            return;
        }
        type.setShowNotification(entry, value);
        notifyChanged();
    }

    public ItemStack getResult(RecipeEntry entry) {
        return RecipeEditorTypes.require(entry.getType()).result(entry);
    }

    public void setResult(RecipeEntry entry, ItemStack result) {
        RecipeEditorTypes.require(entry.getType()).setResult(entry, result);
    }

    public void saveProject() {
        saveVisualStateToSelectedEntry();
        if (project.getEditor() != null) {
            project.getEditor().saveProject(this::notifyChanged);
        }
    }

    public void refreshVisualStateFromData() {
        loadSelectedEntryToVisualState();
    }

    public void saveVisualState() {
        saveVisualStateToSelectedEntry();
    }

    public boolean isSelectedCookingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.COOKING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.COOKING;
    }

    public boolean isSelectedSingleInputLayout() {
        if (selectedEntry != null) {
            var layout = RecipeEditorTypes.layoutForType(selectedEntry.getType());
            return layout == RecipeEditorLayout.COOKING || layout == RecipeEditorLayout.SINGLE_INPUT;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .map(layout -> layout == RecipeEditorLayout.COOKING || layout == RecipeEditorLayout.SINGLE_INPUT)
                .orElse(false);
    }

    public boolean isSelectedSmithingLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.SMITHING;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.SMITHING;
    }

    public boolean isSelectedCraftingGridLayout() {
        if (selectedEntry != null) {
            return RecipeEditorTypes.layoutForType(selectedEntry.getType()) == RecipeEditorLayout.CRAFTING_GRID;
        }
        return RecipeEditorTypes.getCategory(selectedCategory)
                .map(RecipeEditorCategory::layout)
                .orElse(RecipeEditorLayout.CRAFTING_GRID) == RecipeEditorLayout.CRAFTING_GRID;
    }

    public boolean isCookingEntry(RecipeEntry entry) {
        return RecipeEditorTypes.layoutForType(entry.getType()) == RecipeEditorLayout.COOKING;
    }

    public boolean isStonecuttingEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.STONECUTTING);
    }

    public boolean isSmithingTransformEntry(RecipeEntry entry) {
        return entry.isType(RecipeEditorTypes.SMITHING_TRANSFORM);
    }

    public float getCookingExperience(RecipeEntry entry) {
        return Math.max(0, Math.min(Integer.MAX_VALUE, entry.getCooking().getExperience()));
    }

    public void setCookingExperience(RecipeEntry entry, float experience) {
        entry.getCooking().setExperience(Math.max(0, Math.min(Integer.MAX_VALUE, experience)));
        notifyChanged();
    }

    public int getCookingTime(RecipeEntry entry) {
        return Math.max(1, entry.getCooking().getCookingTime());
    }

    public void setCookingTime(RecipeEntry entry, int cookingTime) {
        entry.getCooking().setCookingTime(Math.max(1, cookingTime));
        notifyChanged();
    }

    private void saveVisualStateToSelectedEntry() {
        if (refreshing || selectedEntry == null) {
            return;
        }
        if (selectedContainsUnsupportedIngredients && !ingredientSlotsChanged() && !remainderSlotsChanged()) {
            writeResultOnly();
            return;
        }
        if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            writeShapedRecipe(selectedEntry.getShaped());
        } else if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            writeShapelessRecipe(selectedEntry.getShapeless());
        } else if (isCookingEntry(selectedEntry)) {
            writeCookingRecipe(selectedEntry.getCooking());
        } else if (isStonecuttingEntry(selectedEntry)) {
            writeStonecuttingRecipe(selectedEntry.getStonecutting());
        } else if (isSmithingTransformEntry(selectedEntry)) {
            writeSmithingTransformRecipe(selectedEntry.getSmithingTransform());
        }
        selectedContainsUnsupportedIngredients = false;
        loadedIngredientStacks = copyStacks(visualIngredients);
        loadedRemainders = copyRemainders(visualRemainders);
    }

    private void writeResultOnly() {
        if (selectedEntry != null) {
            setResult(selectedEntry, visualResult.copy());
        }
    }

    private void writeShapedRecipe(ShapedCraftingRecipeData shaped) {
        var bounds = findBounds();
        if (bounds == null) {
            shaped.setPattern(new ArrayList<>());
            shaped.setKey(new ArrayList<>());
            shaped.setRemainders(new ArrayList<>());
            shaped.setResult(visualResult.copy());
            return;
        }

        var itemSymbols = new LinkedHashMap<String, Character>();
        var keyEntries = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var remainders = new ArrayList<CraftingRemainderRule>();
        var symbolIndex = 0;
        for (int row = bounds.minRow; row <= bounds.maxRow; row++) {
            var builder = new StringBuilder();
            for (int col = bounds.minCol; col <= bounds.maxCol; col++) {
                var slot = row * 3 + col;
                var stack = visualIngredients[slot];
                var ingredient = ingredientForVisualSlot(slot);
                if (stack.isEmpty() && isIngredientEmpty(ingredient)) {
                    builder.append(' ');
                    remainders.add(CraftingRemainderRule.defaultRule());
                    continue;
                }
                var ingredientKey = ingredientKey(ingredient, stack, slot);
                var symbol = itemSymbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        continue;
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    itemSymbols.put(ingredientKey, symbol);
                    keyEntries.add(ShapedKeyEntry.of(String.valueOf(symbol), ingredient));
                }
                builder.append(symbol);
                remainders.add(visualRemainders[slot] == null ? CraftingRemainderRule.defaultRule() : visualRemainders[slot].copy());
            }
            pattern.add(builder.toString());
        }
        shaped.setPattern(pattern);
        shaped.setKey(keyEntries);
        shaped.setRemainders(remainders);
        shaped.setResult(visualResult.copy());
    }

    private void writeShapelessRecipe(ShapelessCraftingRecipeData shapeless) {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (int i = 0; i < visualIngredients.length; i++) {
            var ingredient = ingredientForVisualSlot(i);
            if (!isIngredientEmpty(ingredient)) {
                ingredients.add(ingredient);
            }
        }
        shapeless.setIngredients(ingredients);
        shapeless.setResult(visualResult.copy());
    }

    private void writeCookingRecipe(CookingRecipeData cooking) {
        cooking.setIngredient(ingredientForVisualSlot(0));
        cooking.setResult(visualResult.copy());
    }

    private void writeStonecuttingRecipe(StonecuttingRecipeData stonecutting) {
        stonecutting.setIngredient(ingredientForVisualSlot(0));
        stonecutting.setResult(visualResult.copy());
    }

    private void writeSmithingTransformRecipe(SmithingTransformRecipeData smithingTransform) {
        smithingTransform.setTemplate(ingredientForVisualSlot(0));
        smithingTransform.setBase(ingredientForVisualSlot(1));
        smithingTransform.setAddition(ingredientForVisualSlot(2));
        smithingTransform.setResult(visualResult.copy());
    }

    private RecipeIngredient getIngredientForSlot(RecipeEntry entry, int index) {
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            return getShapedSlotIngredient(entry.getShaped(), index);
        }
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS) && index < entry.getShapeless().getIngredients().size()) {
            return entry.getShapeless().getIngredients().get(index);
        }
        if (isCookingEntry(entry) && index == 0) {
            return entry.getCooking().getIngredient();
        }
        if (isStonecuttingEntry(entry) && index == 0) {
            return entry.getStonecutting().getIngredient();
        }
        if (isSmithingTransformEntry(entry)) {
            return getSmithingSlotIngredient(entry.getSmithingTransform(), index);
        }
        return new RecipeIngredient();
    }

    private RecipeIngredient getSmithingSlotIngredient(SmithingTransformRecipeData smithingTransform, int index) {
        return switch (index) {
            case 0 -> smithingTransform.getTemplate();
            case 1 -> smithingTransform.getBase();
            case 2 -> smithingTransform.getAddition();
            default -> new RecipeIngredient();
        };
    }

    private RecipeIngredient getShapedSlotIngredient(ShapedCraftingRecipeData shaped, int index) {
        var row = index / 3;
        var col = index % 3;
        if (row >= shaped.getPattern().size()) {
            return new RecipeIngredient();
        }
        var line = shaped.getPattern().get(row);
        if (col >= line.length()) {
            return new RecipeIngredient();
        }
        var symbol = line.charAt(col);
        for (var keyEntry : shaped.getKey()) {
            if (keyEntry.getSymbol() != null && keyEntry.getSymbol().length() == 1 && keyEntry.getSymbol().charAt(0) == symbol) {
                return keyEntry.getIngredient();
            }
        }
        return new RecipeIngredient();
    }

    private void setIngredientForSlot(RecipeEntry entry, int index, RecipeIngredient ingredient) {
        if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
            setShapedSlotIngredient(entry.getShaped(), index, ingredient);
        } else if (entry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
            setShapelessSlotIngredient(entry.getShapeless(), index, ingredient);
        } else if (isCookingEntry(entry) && index == 0) {
            setCookingSlotIngredient(entry.getCooking(), ingredient);
        } else if (isStonecuttingEntry(entry) && index == 0) {
            setStonecuttingSlotIngredient(entry.getStonecutting(), ingredient);
        } else if (isSmithingTransformEntry(entry) && index >= 0 && index <= 2) {
            setSmithingSlotIngredient(entry.getSmithingTransform(), index, ingredient);
        }
    }

    private void setSmithingSlotIngredient(SmithingTransformRecipeData smithingTransform, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeSmithingTransformRecipe(smithingTransform);
    }

    private void setStonecuttingSlotIngredient(StonecuttingRecipeData stonecutting, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeStonecuttingRecipe(stonecutting);
    }

    private void setCookingSlotIngredient(CookingRecipeData cooking, RecipeIngredient ingredient) {
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        writeCookingRecipe(cooking);
    }

    private void setShapelessSlotIngredient(ShapelessCraftingRecipeData shapeless, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
        writeShapelessRecipe(shapeless);
    }

    private void setShapedSlotIngredient(ShapedCraftingRecipeData shaped, int index, RecipeIngredient ingredient) {
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        if (isIngredientEmpty(visualIngredientData[index])) {
            visualRemainders[index] = CraftingRemainderRule.defaultRule();
        }
        writeShapedRecipe(shaped);
    }

    private boolean isIngredientEmpty(RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues().isEmpty()) {
            return true;
        }
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null && !value.getItem().isEmpty()) {
                return false;
            }
            if (value.getKind() == IngredientValueKind.TAG && value.getTag() != null) {
                return false;
            }
        }
        return true;
    }

    private void loadSelectedEntryToVisualState() {
        refreshing = true;
        try {
            clearVisualState();
            selectedContainsUnsupportedIngredients = false;
            if (selectedEntry == null) {
                return;
            }
            if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPED)) {
                loadShaped(selectedEntry.getShaped());
            } else if (selectedEntry.isType(RecipeEditorTypes.CRAFTING_SHAPELESS)) {
                loadShapeless(selectedEntry.getShapeless());
            } else if (isCookingEntry(selectedEntry)) {
                loadCooking(selectedEntry.getCooking());
            } else if (isStonecuttingEntry(selectedEntry)) {
                loadStonecutting(selectedEntry.getStonecutting());
            } else if (isSmithingTransformEntry(selectedEntry)) {
                loadSmithingTransform(selectedEntry.getSmithingTransform());
            }
            loadedIngredientStacks = copyStacks(visualIngredients);
            loadedRemainders = copyRemainders(visualRemainders);
        } finally {
            refreshing = false;
        }
    }

    private void clearVisualState() {
        visualIngredients = emptyIngredientStacks();
        visualIngredientData = emptyIngredientData();
        visualRemainders = emptyRemainderData();
        visualResult = ItemStack.EMPTY;
        loadedIngredientStacks = emptyIngredientStacks();
        loadedRemainders = emptyRemainderData();
        selectedContainsUnsupportedIngredients = false;
    }

    private void loadShaped(ShapedCraftingRecipeData shaped) {
        var key = new LinkedHashMap<Character, ItemStack>();
        for (var entry : shaped.getKey()) {
            var symbol = entry.getSymbol();
            if (symbol == null || symbol.length() != 1) {
                continue;
            }
            if (containsUnsupportedIngredientValue(entry.getIngredient())) {
                selectedContainsUnsupportedIngredients = true;
            }
            key.put(symbol.charAt(0), itemFromIngredient(entry.getIngredient()));
        }
        for (int row = 0; row < Math.min(3, shaped.getPattern().size()); row++) {
            var line = shaped.getPattern().get(row);
            for (int col = 0; col < Math.min(3, line.length()); col++) {
                var symbol = line.charAt(col);
                var stack = key.getOrDefault(symbol, ItemStack.EMPTY);
                if (line.charAt(col) != ' ' && stack.isEmpty()) {
                    selectedContainsUnsupportedIngredients = true;
                }
                visualIngredients[row * 3 + col] = stack.copy();
                visualIngredientData[row * 3 + col] = ingredientForSymbol(shaped, symbol);
                visualRemainders[row * 3 + col] = remainderForPatternSlot(shaped, row, col);
            }
        }
        visualResult = shaped.getResult().copy();
    }

    private void loadShapeless(ShapelessCraftingRecipeData shapeless) {
        for (int i = 0; i < Math.min(9, shapeless.getIngredients().size()); i++) {
            var ingredient = shapeless.getIngredients().get(i);
            if (containsUnsupportedIngredientValue(ingredient)) {
                selectedContainsUnsupportedIngredients = true;
            }
            visualIngredients[i] = itemFromIngredient(ingredient);
            visualIngredientData[i] = ingredient;
        }
        visualResult = shapeless.getResult().copy();
    }

    private void loadCooking(CookingRecipeData cooking) {
        var ingredient = cooking.getIngredient();
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        visualResult = cooking.getResult().copy();
    }

    private void loadStonecutting(StonecuttingRecipeData stonecutting) {
        var ingredient = stonecutting.getIngredient();
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[0] = itemFromIngredient(ingredient);
        visualIngredientData[0] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[0] = CraftingRemainderRule.defaultRule();
        visualResult = stonecutting.getResult().copy();
    }

    private void loadSmithingTransform(SmithingTransformRecipeData smithingTransform) {
        loadSmithingIngredientSlot(0, smithingTransform.getTemplate());
        loadSmithingIngredientSlot(1, smithingTransform.getBase());
        loadSmithingIngredientSlot(2, smithingTransform.getAddition());
        visualResult = smithingTransform.getResult().copy();
    }

    private void loadSmithingIngredientSlot(int index, RecipeIngredient ingredient) {
        if (containsUnsupportedIngredientValue(ingredient)) {
            selectedContainsUnsupportedIngredients = true;
        }
        visualIngredients[index] = itemFromIngredient(ingredient);
        visualIngredientData[index] = ingredient == null ? new RecipeIngredient() : ingredient;
        visualRemainders[index] = CraftingRemainderRule.defaultRule();
    }

    private boolean ingredientSlotsChanged() {
        if (loadedIngredientStacks.length != visualIngredients.length) {
            return true;
        }
        for (int i = 0; i < visualIngredients.length; i++) {
            if (!sameIngredientItem(loadedIngredientStacks[i], visualIngredients[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean sameIngredientItem(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() == right.isEmpty();
        }
        return left.is(right.getItem());
    }

    private boolean remainderSlotsChanged() {
        if (loadedRemainders.length != visualRemainders.length) {
            return true;
        }
        for (int i = 0; i < visualRemainders.length; i++) {
            if (!sameRemainder(loadedRemainders[i], visualRemainders[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean sameRemainder(CraftingRemainderRule left, CraftingRemainderRule right) {
        var leftRule = left == null ? CraftingRemainderRule.defaultRule() : left;
        var rightRule = right == null ? CraftingRemainderRule.defaultRule() : right;
        if (leftRule.getMode() != rightRule.getMode()) {
            return false;
        }
        var leftItem = leftRule.getItem() == null ? ItemStack.EMPTY : leftRule.getItem();
        var rightItem = rightRule.getItem() == null ? ItemStack.EMPTY : rightRule.getItem();
        return ItemStack.matches(leftItem, rightItem);
    }

    private boolean containsUnsupportedIngredientValue(@Nullable RecipeIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        if (ingredient.getValues().isEmpty()) {
            return false;
        }
        if (ingredient.getValues().size() != 1) {
            return true;
        }
        var value = ingredient.getValues().getFirst();
        return switch (value.getKind()) {
            case ITEM -> value.getItem() == null || value.getItem().isEmpty();
            case TAG -> value.getTag() == null || itemsFromTag(value.getTag()).length == 0;
        };
    }

    private ItemStack itemFromIngredient(@Nullable RecipeIngredient ingredient) {
        if (ingredient == null) {
            return ItemStack.EMPTY;
        }
        for (var value : ingredient.getValues()) {
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null && !value.getItem().isEmpty()) {
                return value.getItem().copyWithCount(1);
            }
            if (value.getKind() == IngredientValueKind.TAG && value.getTag() != null) {
                var tagItems = itemsFromTag(value.getTag());
                if (tagItems.length > 0) {
                    return tagItems[0].copyWithCount(1);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack[] itemsFromTag(ResourceLocation tag) {
        if (tag == null) {
            return new ItemStack[0];
        }
        return BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tag))
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .filter(item -> item != Items.AIR)
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                        .toArray(ItemStack[]::new))
                .orElseGet(() -> new ItemStack[0]);
    }

    private void refreshUnsupportedIngredientStatus() {
        selectedContainsUnsupportedIngredients = false;
        for (var ingredient : visualIngredientData) {
            if (containsUnsupportedIngredientValue(ingredient)) {
                selectedContainsUnsupportedIngredients = true;
                return;
            }
        }
    }

    private RecipeIngredient ingredientForSymbol(ShapedCraftingRecipeData shaped, char symbol) {
        if (symbol == ' ') {
            return new RecipeIngredient();
        }
        for (var entry : shaped.getKey()) {
            if (entry.getSymbol() != null && entry.getSymbol().length() == 1 && entry.getSymbol().charAt(0) == symbol) {
                return entry.getIngredient();
            }
        }
        return new RecipeIngredient();
    }

    private CraftingRemainderRule remainderForPatternSlot(ShapedCraftingRecipeData shaped, int row, int col) {
        var width = shaped.getPattern().isEmpty() ? 0 : shaped.getPattern().getFirst().length();
        var remainderIndex = row * width + col;
        if (remainderIndex < 0 || remainderIndex >= shaped.getRemainders().size()) {
            return CraftingRemainderRule.defaultRule();
        }
        var remainder = shaped.getRemainders().get(remainderIndex);
        return remainder == null ? CraftingRemainderRule.defaultRule() : remainder.copy();
    }

    private RecipeIngredient ingredientForVisualSlot(int slot) {
        var ingredient = visualIngredientData[slot];
        if (!isIngredientEmpty(ingredient)) {
            return ingredient;
        }
        var stack = visualIngredients[slot];
        return stack.isEmpty() ? new RecipeIngredient() : RecipeIngredient.item(stack.getItem());
    }

    private String ingredientKey(RecipeIngredient ingredient, ItemStack stack, int slot) {
        if (ingredient != null && ingredient.getValues().size() == 1) {
            var value = ingredient.getValues().getFirst();
            return switch (value.getKind()) {
                case ITEM -> value.getItem() == null || value.getItem().isEmpty()
                        ? "empty:" + slot
                        : "item:" + BuiltInRegistries.ITEM.getKey(value.getItem().getItem());
                case TAG -> "tag:" + value.getTag();
            };
        }
        if (!stack.isEmpty()) {
            return "item:" + BuiltInRegistries.ITEM.getKey(stack.getItem());
        }
        return "ingredient:" + slot;
    }

    @Nullable
    private Bounds findBounds() {
        var minRow = 3;
        var minCol = 3;
        var maxRow = -1;
        var maxCol = -1;
        for (int i = 0; i < visualIngredients.length; i++) {
            if (visualIngredients[i].isEmpty() && isIngredientEmpty(visualIngredientData[i])) {
                continue;
            }
            var row = i / 3;
            var col = i % 3;
            minRow = Math.min(minRow, row);
            minCol = Math.min(minCol, col);
            maxRow = Math.max(maxRow, row);
            maxCol = Math.max(maxCol, col);
        }
        if (maxRow < 0) {
            return null;
        }
        return new Bounds(minRow, minCol, maxRow, maxCol);
    }

    private ItemStack normalizeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(1);
    }

    private static ItemStack[] emptyIngredientStacks() {
        var stacks = new ItemStack[9];
        Arrays.fill(stacks, ItemStack.EMPTY);
        return stacks;
    }

    private static CraftingRemainderRule[] emptyRemainderData() {
        var remainders = new CraftingRemainderRule[9];
        Arrays.setAll(remainders, ignored -> CraftingRemainderRule.defaultRule());
        return remainders;
    }

    private static RecipeIngredient[] emptyIngredientData() {
        var ingredients = new RecipeIngredient[9];
        Arrays.setAll(ingredients, ignored -> new RecipeIngredient());
        return ingredients;
    }

    private static ItemStack[] copyStacks(ItemStack[] stacks) {
        var copy = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copy[i] = stacks[i].copy();
        }
        return copy;
    }

    private static CraftingRemainderRule[] copyRemainders(CraftingRemainderRule[] remainders) {
        var copy = new CraftingRemainderRule[remainders.length];
        for (int i = 0; i < remainders.length; i++) {
            copy[i] = remainders[i] == null ? CraftingRemainderRule.defaultRule() : remainders[i].copy();
        }
        return copy;
    }

    private record Bounds(int minRow, int minCol, int maxRow, int maxCol) {
    }
}
