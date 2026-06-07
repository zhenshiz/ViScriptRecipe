package com.viscript_recipe.recipe.importer;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeImporter;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeImporter;
import com.viscript_recipe.compat.create.CreateRecipeImporter;
import com.viscript_recipe.compat.extendedcrafting.ExtendedCraftingRecipeImporter;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeImporter;
import com.viscript_recipe.compat.iceandfire.IceAndFireRecipeImporter;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeImporter;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeImporter;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import com.viscript_recipe.data.RecipeOperation;
import com.viscript_recipe.data.create.CreateMechanicalCraftingRecipeData;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.data.vanilla.CraftingRemainderRule;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import com.viscript_recipe.data.vanilla.SmithingTransformRecipeData;
import com.viscript_recipe.data.vanilla.StonecuttingRecipeData;
import com.viscript_recipe.mixin.SmithingTransformRecipeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public final class RecipeImporter {
    private static final char[] SHAPED_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.<>/?|~".toCharArray();
    private static final RecipeImportHandler VANILLA_HANDLER = new RecipeImportHandler() {
        @Override
        public boolean canImport(RecipeHolder<?> holder) {
            if (holder == null || holder.value() == null) {
                return false;
            }
            var recipe = holder.value();
            return recipe instanceof ShapedRecipe
                    || recipe instanceof ShapelessRecipe
                    || recipe instanceof AbstractCookingRecipe
                    || recipe instanceof StonecutterRecipe
                    || recipe instanceof SmithingTransformRecipe;
        }

        @Override
        public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
            var recipe = holder.value();
            if (recipe instanceof ShapedRecipe shaped) {
                return success(importShaped(holder.id(), shaped, provider));
            }
            if (recipe instanceof ShapelessRecipe shapeless) {
                return success(importShapeless(holder.id(), shapeless, provider));
            }
            if (recipe instanceof AbstractCookingRecipe cooking) {
                return success(importCooking(holder.id(), cooking, provider));
            }
            if (recipe instanceof StonecutterRecipe stonecutter) {
                return success(importStonecutting(holder.id(), stonecutter, provider));
            }
            if (recipe instanceof SmithingTransformRecipe smithing) {
                return success(importSmithingTransform(holder.id(), smithing));
            }
            return null;
        }
    };
    private static final List<RecipeImportHandler> HANDLERS = createHandlers();

    private RecipeImporter() {
    }

    public static RecipeImportResult importRecipe(ResourceLocation recipeId) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.no_world");
        }
        return minecraft.level.getRecipeManager()
                .byKey(recipeId)
                .map(holder -> importHolder(holder, minecraft.level.registryAccess()))
                .orElseGet(() -> RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.not_found", recipeId.toString()));
    }

    public static boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        for (var handler : HANDLERS) {
            if (handler.canImport(holder)) {
                return true;
            }
        }
        return false;
    }

    public static String recipeTypeName(RecipeHolder<?> holder) {
        return holder == null || holder.value() == null ? "" : recipeTypeName(holder.value());
    }

    private static RecipeImportResult importHolder(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        try {
            for (var handler : HANDLERS) {
                if (!handler.canImport(holder)) {
                    continue;
                }
                var result = handler.tryImport(holder, provider);
                if (result != null) {
                    return result;
                }
            }
            return RecipeImportResult.failure("viscript_recipe.editor.import_recipe.error.unsupported_type", recipeTypeName(holder));
        } catch (RecipeImportException exception) {
            return RecipeImportResult.failure(exception.component());
        }
    }

    private static List<RecipeImportHandler> createHandlers() {
        var handlers = new ArrayList<RecipeImportHandler>();
        handlers.add(VANILLA_HANDLER);
        addIfLoaded(handlers, "irons_spellbooks", () -> IronSpellbooksRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "iceandfire", () -> IceAndFireRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "farmersdelight", () -> FarmersDelightRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "create", () -> CreateRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "extendedcrafting", () -> ExtendedCraftingRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "ars_nouveau", () -> ArsNouveauRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "kaleidoscope_cookery", () -> KaleidoscopeCookeryRecipeImporter.INSTANCE);
        addIfLoaded(handlers, "avaritia", () -> AvaritiaRecipeImporter.INSTANCE);
        return List.copyOf(handlers);
    }

    private static void addIfLoaded(List<RecipeImportHandler> handlers, String modId, Supplier<RecipeImportHandler> handler) {
        if (ViScriptRecipe.isModLoaded(modId)) {
            handlers.add(handler.get());
        }
    }

    public static RecipeImportResult success(RecipeEntry entry) {
        return RecipeImportResult.success(entry, ComponentHelper.imported(entry));
    }

    private static RecipeEntry importShaped(ResourceLocation id, ShapedRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe.getWidth() > 3 || recipe.getHeight() > 3) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.shaped_too_large", recipe.getWidth(), recipe.getHeight());
        }
        var shapedPattern = importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new ShapedCraftingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setPattern(shapedPattern.pattern())
                .setKey(shapedPattern.key())
                .setRemainders(defaultRemainders(recipe.getWidth() * recipe.getHeight()))
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CRAFTING_SHAPED).setShaped(data);
    }

    private static RecipeEntry importShapeless(ResourceLocation id, ShapelessRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                ingredients.add(importIngredient(ingredient));
            }
        }
        if (ingredients.size() > 9) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), 9);
        }
        var data = new ShapelessCraftingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setIngredients(ingredients)
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CRAFTING_SHAPELESS).setShapeless(data);
    }

    private static RecipeEntry importCooking(ResourceLocation id, AbstractCookingRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var type = cookingEditorType(recipe);
        if (type == null) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", recipeTypeName(recipe));
        }
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var data = new CookingRecipeData()
                .setIngredient(importIngredient(ingredients.getFirst()))
                .setResult(copyResult(recipe, provider))
                .setExperience(recipe.getExperience())
                .setCookingTime(Math.max(1, recipe.getCookingTime()));
        return baseEntry(id, type).setCooking(data);
    }

    @Nullable
    private static ResourceLocation cookingEditorType(AbstractCookingRecipe recipe) {
        if (recipe instanceof SmeltingRecipe || recipe.getType() == RecipeType.SMELTING) {
            return RecipeEditorTypes.SMELTING;
        }
        if (recipe instanceof BlastingRecipe || recipe.getType() == RecipeType.BLASTING) {
            return RecipeEditorTypes.BLASTING;
        }
        if (recipe instanceof SmokingRecipe || recipe.getType() == RecipeType.SMOKING) {
            return RecipeEditorTypes.SMOKING;
        }
        if (recipe instanceof CampfireCookingRecipe || recipe.getType() == RecipeType.CAMPFIRE_COOKING) {
            return RecipeEditorTypes.CAMPFIRE_COOKING;
        }
        return null;
    }

    private static RecipeEntry importStonecutting(ResourceLocation id, StonecutterRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var data = new StonecuttingRecipeData()
                .setShowNotification(recipe.showNotification())
                .setIngredient(importIngredient(ingredients.getFirst()))
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.STONECUTTING).setStonecutting(data);
    }

    private static RecipeEntry importSmithingTransform(ResourceLocation id, SmithingTransformRecipe recipe) throws RecipeImportException {
        var accessor = (SmithingTransformRecipeAccessor) recipe;
        var data = new SmithingTransformRecipeData()
                .setShowNotification(recipe.showNotification())
                .setTemplate(importIngredient(accessor.viscriptRecipe$getTemplate()))
                .setBase(importIngredient(accessor.viscriptRecipe$getBase()))
                .setAddition(importIngredient(accessor.viscriptRecipe$getAddition()))
                .setResult(copyStack(accessor.viscriptRecipe$getResult()));
        return baseEntry(id, RecipeEditorTypes.SMITHING_TRANSFORM).setSmithingTransform(data);
    }

    public static RecipeEntry importMechanicalCrafting(ResourceLocation id, ShapedRecipe recipe, boolean acceptMirrored, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe.getWidth() > CreateMechanicalCraftingRecipeData.MAX_SIZE || recipe.getHeight() > CreateMechanicalCraftingRecipeData.MAX_SIZE) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.shaped_too_large", recipe.getWidth(), recipe.getHeight());
        }
        var shapedPattern = importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new CreateMechanicalCraftingRecipeData()
                .setWidth(recipe.getWidth())
                .setHeight(recipe.getHeight())
                .setAcceptMirrored(acceptMirrored)
                .setPattern(shapedPattern.pattern())
                .setKey(shapedPattern.key())
                .setResult(copyResult(recipe, provider));
        return baseEntry(id, RecipeEditorTypes.CREATE_MECHANICAL_CRAFTING).setCreateMechanicalCrafting(data);
    }

    public static ImportedShapedPattern importShapedPattern(List<Ingredient> ingredients, int width, int height) throws RecipeImportException {
        var ingredientSymbols = new LinkedHashMap<String, Character>();
        var key = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        var symbolIndex = 0;
        for (int row = 0; row < height; row++) {
            var builder = new StringBuilder();
            for (int col = 0; col < width; col++) {
                var ingredient = ingredientAt(ingredients, col + row * width);
                if (ingredient.isEmpty()) {
                    builder.append(' ');
                    continue;
                }
                var imported = importIngredient(ingredient);
                var keyString = ingredientKey(imported);
                var symbol = ingredientSymbols.get(keyString);
                if (symbol == null) {
                    if (symbolIndex >= SHAPED_SYMBOLS.length) {
                        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", symbolIndex + 1, SHAPED_SYMBOLS.length);
                    }
                    symbol = SHAPED_SYMBOLS[symbolIndex++];
                    ingredientSymbols.put(keyString, symbol);
                    key.add(ShapedKeyEntry.of(String.valueOf(symbol), imported));
                }
                builder.append(symbol);
            }
            pattern.add(builder.toString());
        }
        return new ImportedShapedPattern(pattern, key);
    }

    private static Ingredient ingredientAt(List<Ingredient> ingredients, int index) {
        if (index < 0 || index >= ingredients.size()) {
            return Ingredient.EMPTY;
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? Ingredient.EMPTY : ingredient;
    }

    public static RecipeIngredient importIngredient(Ingredient ingredient) throws RecipeImportException {
        var imported = new RecipeIngredient();
        appendIngredientValues(imported, ingredient);
        if (imported.getValues().isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        return imported;
    }

    public static List<RecipeIngredient> importIngredientList(List<Ingredient> ingredients, int max) throws RecipeImportException {
        var imported = new ArrayList<RecipeIngredient>();
        if (ingredients == null) {
            return imported;
        }
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), max);
        }
        for (var ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                imported.add(importIngredient(ingredient));
            }
        }
        return imported;
    }

    public static RecipeIngredient importItemStacks(List<ItemStack> stacks) throws RecipeImportException {
        var imported = new RecipeIngredient();
        if (stacks != null) {
            for (var stack : stacks) {
                appendItemValue(imported, stack);
            }
        }
        if (imported.getValues().isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        return imported;
    }

    private static void appendIngredientValues(RecipeIngredient imported, Ingredient ingredient) throws RecipeImportException {
        if (ingredient == null || ingredient.isEmpty()) {
            return;
        }
        if (ingredient.isCustom()) {
            appendCustomIngredientValues(imported, ingredient);
            return;
        }
        for (var value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue itemValue) {
                imported.getValues().add(RecipeIngredientValue.item(itemValue.item().copyWithCount(1)));
            } else if (value instanceof Ingredient.TagValue tagValue) {
                imported.getValues().add(RecipeIngredientValue.tag(tagValue.tag().location()));
            } else {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
            }
        }
    }

    private static void appendCustomIngredientValues(RecipeIngredient imported, Ingredient ingredient) throws RecipeImportException {
        var custom = ingredient.getCustomIngredient();
        if (custom instanceof CompoundIngredient compound) {
            for (var child : compound.children()) {
                appendIngredientValues(imported, child);
            }
            return;
        }
        if (custom instanceof DataComponentIngredient dataComponentIngredient) {
            for (var stack : dataComponentIngredient.getItems().toList()) {
                appendItemValue(imported, stack);
            }
            return;
        }
        if (custom != null && custom.isSimple()) {
            for (var stack : custom.getItems().toList()) {
                appendItemValue(imported, stack);
            }
            return;
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
    }

    private static void appendItemValue(RecipeIngredient imported, ItemStack stack) {
        if (stack != null && !stack.isEmpty() && !stack.is(Items.AIR)) {
            imported.getValues().add(RecipeIngredientValue.item(stack.copyWithCount(1)));
        }
    }

    public static RecipeEntry baseEntry(ResourceLocation id, ResourceLocation type) {
        return new RecipeEntry()
                .setEnabled(true)
                .setOperation(RecipeOperation.REPLACE)
                .setRecipeId(id)
                .setType(type);
    }

    public static ItemStack copyResult(Recipe<?> recipe, HolderLookup.Provider provider) {
        return copyStack(recipe.getResultItem(provider));
    }

    public static ItemStack copyStack(ItemStack stack) {
        return stack == null || stack.isEmpty() || stack.is(Items.AIR) ? ItemStack.EMPTY : stack.copy();
    }

    private static List<CraftingRemainderRule> defaultRemainders(int count) {
        var remainders = new ArrayList<CraftingRemainderRule>();
        for (int i = 0; i < count; i++) {
            remainders.add(CraftingRemainderRule.defaultRule());
        }
        return remainders;
    }

    private static String ingredientKey(RecipeIngredient ingredient) {
        var parts = new ArrayList<String>();
        for (var value : ingredient.getValues()) {
            var kind = value.getKind() == null ? IngredientValueKind.ITEM : value.getKind();
            parts.add(switch (kind) {
                case ITEM -> {
                    var item = value.getItem();
                    yield item == null || item.isEmpty()
                            ? "item:empty"
                            : "item:" + ItemStack.hashItemAndComponents(item);
                }
                case TAG -> "tag:" + value.getTag();
                case ITEM_ABILITY -> "item_ability:" + value.getItemAbility();
            });
        }
        return String.join("|", parts);
    }

    private static String recipeTypeName(Recipe<?> recipe) {
        var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
        if (typeId != null) {
            return typeId + " / " + serializerId;
        }
        return String.valueOf(serializerId);
    }

    public record ImportedShapedPattern(List<String> pattern, List<ShapedKeyEntry> key) {
    }

    private static final class ComponentHelper {
        private ComponentHelper() {
        }

        private static net.minecraft.network.chat.Component imported(RecipeEntry entry) {
            return net.minecraft.network.chat.Component.translatable(
                    "viscript_recipe.editor.import_recipe.success",
                    String.valueOf(entry.getRecipeId()),
                    RecipeEditorTypes.get(entry.getType())
                            .map(type -> type.displayName().getString())
                            .orElse(entry.getType().toString())
            );
        }
    }
}
