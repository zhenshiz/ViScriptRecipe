package com.viscript_recipe.compat.irons_spellbooks;

import com.viscript_recipe.compat.irons_spellbooks.canvas.IronAlchemistCauldronCanvas;
import com.viscript_recipe.compat.irons_spellbooks.canvas.IronArcaneAnvilCanvas;
import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.compat.irons_spellbooks.data.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class IronSpellbooksRecipeEditorTypes {
    public static final String MOD_ID = "irons_spellbooks";

    public static final ResourceLocation ALCHEMIST_CAULDRON = iron("alchemist_cauldron");
    public static final ResourceLocation ARCANE_ANVIL = iron("arcane_anvil");
    public static final ResourceLocation ALCHEMIST_CAULDRON_FILL = iron("alchemist_cauldron_fill");
    public static final ResourceLocation ALCHEMIST_CAULDRON_EMPTY = iron("alchemist_cauldron_empty");
    public static final ResourceLocation ALCHEMIST_CAULDRON_BREW = iron("alchemist_cauldron_brew");
    public static final ResourceLocation ARCANE_ANVIL_TRANSFORM = iron("arcane_anvil_transform");
    public static final ResourceLocation SMITHING_TRANSFORM_NO_ADDITION = iron("smithing_transform_no_addition");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private IronSpellbooksRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.category.irons_spellbooks.alchemist_cauldron",
                MOD_ID,
                REQUIRED_MODS,
                ALCHEMIST_CAULDRON_FILL,
                RecipeEditorLayout.ALCHEMIST_CAULDRON
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ARCANE_ANVIL,
                "viscript_recipe.editor.category.irons_spellbooks.arcane_anvil",
                MOD_ID,
                REQUIRED_MODS,
                ARCANE_ANVIL_TRANSFORM,
                RecipeEditorLayout.SMITHING
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                ALCHEMIST_CAULDRON_FILL, ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_fill",
                IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new,
                IronAlchemistCauldronCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ALCHEMIST_CAULDRON_EMPTY, ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_empty",
                IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new,
                IronAlchemistCauldronCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ALCHEMIST_CAULDRON_BREW, ALCHEMIST_CAULDRON,
                "viscript_recipe.editor.type.irons_spellbooks.alchemist_cauldron_brew",
                IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new,
                IronAlchemistCauldronCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ARCANE_ANVIL_TRANSFORM, ARCANE_ANVIL,
                "viscript_recipe.editor.type.irons_spellbooks.arcane_anvil_transform",
                IronArcaneAnvilRecipeData.class, IronArcaneAnvilRecipeData::new,
                IronArcaneAnvilCanvas::new, MOD_ID
        ));
    }

    private static ResourceLocation iron(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
