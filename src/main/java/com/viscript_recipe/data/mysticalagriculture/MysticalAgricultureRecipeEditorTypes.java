package com.viscript_recipe.data.mysticalagriculture;

import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Registers the six Recipe Codec-backed Mystical Agriculture editor types.
 *
 * <p>The JEI-only crux information page is intentionally excluded because it is generated from the crop registry
 * and is not represented by a recipe serializer.
 */
public final class MysticalAgricultureRecipeEditorTypes {
    public static final String MOD_ID = "mysticalagriculture";

    public static final ResourceLocation INFUSION_ALTAR = mystical("infusion_altar");
    public static final ResourceLocation AWAKENING_ALTAR = mystical("awakening_altar");
    public static final ResourceLocation ENCHANTER_BLOCK = mystical("enchanter");
    public static final ResourceLocation REPROCESSOR_BLOCK = mystical("seed_reprocessor");
    public static final ResourceLocation SOUL_EXTRACTOR_BLOCK = mystical("soul_extractor");
    public static final ResourceLocation SOULIUM_SPAWNER_BLOCK = mystical("soulium_spawner");

    public static final ResourceLocation INFUSION = mystical("infusion");
    public static final ResourceLocation AWAKENING = mystical("awakening");
    public static final ResourceLocation ENCHANTER = mystical("enchanter");
    public static final ResourceLocation REPROCESSOR = mystical("reprocessor");
    public static final ResourceLocation SOUL_EXTRACTION = mystical("soul_extraction");
    public static final ResourceLocation SOULIUM_SPAWNER = mystical("soulium_spawner");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private MysticalAgricultureRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategory(INFUSION_ALTAR, INFUSION, RecipeEditorLayout.MYSTICAL_AGRICULTURE_INFUSION);
        registerCategory(AWAKENING_ALTAR, AWAKENING, RecipeEditorLayout.MYSTICAL_AGRICULTURE_AWAKENING);
        registerCategory(ENCHANTER_BLOCK, ENCHANTER, RecipeEditorLayout.MYSTICAL_AGRICULTURE_ENCHANTER);
        registerCategory(REPROCESSOR_BLOCK, REPROCESSOR, RecipeEditorLayout.MYSTICAL_AGRICULTURE_REPROCESSOR);
        registerCategory(SOUL_EXTRACTOR_BLOCK, SOUL_EXTRACTION, RecipeEditorLayout.MYSTICAL_AGRICULTURE_SOUL_EXTRACTION);
        registerCategory(SOULIUM_SPAWNER_BLOCK, SOULIUM_SPAWNER, RecipeEditorLayout.MYSTICAL_AGRICULTURE_SOULIUM_SPAWNER);
        registerTypes();
    }

    private static void registerCategory(ResourceLocation category, ResourceLocation type, RecipeEditorLayout layout) {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                category,
                "viscript_recipe.editor.category.mysticalagriculture." + category.getPath(),
                MOD_ID,
                REQUIRED_MODS,
                type,
                layout,
                category
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(type(INFUSION, INFUSION_ALTAR,
                entry -> entry.getMysticalAgricultureInfusion().compile(),
                entry -> entry.getMysticalAgricultureInfusion().getResult(),
                (entry, stack) -> entry.getMysticalAgricultureInfusion().setResult(stack.copy())));
        RecipeEditorTypes.register(type(AWAKENING, AWAKENING_ALTAR,
                entry -> entry.getMysticalAgricultureAwakening().compile(),
                entry -> entry.getMysticalAgricultureAwakening().getResult(),
                (entry, stack) -> entry.getMysticalAgricultureAwakening().setResult(stack.copy())));
        RecipeEditorTypes.register(type(ENCHANTER, ENCHANTER_BLOCK,
                entry -> entry.getMysticalAgricultureEnchanter().compile(),
                entry -> MysticalAgricultureRecipeUiSupport.firstEnchantedBook(
                        entry.getMysticalAgricultureEnchanter().getEnchantment()),
                (entry, stack) -> {
                }));
        RecipeEditorTypes.register(type(REPROCESSOR, REPROCESSOR_BLOCK,
                entry -> entry.getMysticalAgricultureReprocessor().compile(),
                entry -> entry.getMysticalAgricultureReprocessor().getResult(),
                (entry, stack) -> entry.getMysticalAgricultureReprocessor().setResult(stack.copy())));
        RecipeEditorTypes.register(type(SOUL_EXTRACTION, SOUL_EXTRACTOR_BLOCK,
                entry -> entry.getMysticalAgricultureSoulExtraction().compile(),
                entry -> MysticalAgricultureRecipeUiSupport.soulJar(entry.getMysticalAgricultureSoulExtraction()),
                (entry, stack) -> {
                }));
        RecipeEditorTypes.register(type(SOULIUM_SPAWNER, SOULIUM_SPAWNER_BLOCK,
                entry -> entry.getMysticalAgricultureSouliumSpawner().compile(),
                entry -> MysticalAgricultureRecipeUiSupport.firstSpawnEgg(
                        entry.getMysticalAgricultureSouliumSpawner().getEntities()),
                (entry, stack) -> {
                }));
    }

    private static RecipeEditorType type(
            ResourceLocation id,
            ResourceLocation category,
            java.util.function.Function<com.viscript_recipe.data.RecipeEntry, net.minecraft.world.item.crafting.Recipe<?>> compiler,
            java.util.function.Function<com.viscript_recipe.data.RecipeEntry, net.minecraft.world.item.ItemStack> resultGetter,
            java.util.function.BiConsumer<com.viscript_recipe.data.RecipeEntry, net.minecraft.world.item.ItemStack> resultSetter
    ) {
        return new RecipeEditorType(
                id,
                category,
                "viscript_recipe.editor.type.mysticalagriculture." + id.getPath(),
                REQUIRED_MODS,
                false,
                compiler,
                entry -> false,
                (entry, value) -> {
                },
                resultGetter,
                resultSetter
        );
    }

    public static ResourceLocation mystical(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
