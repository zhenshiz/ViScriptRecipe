package com.viscript_recipe.data.ars_nouveau;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArsNouveauRecipeEditorTypes {
    public static final String MOD_ID = "ars_nouveau";

    public static final ResourceLocation ENCHANTING_APPARATUS = ars("enchanting_apparatus");
    public static final ResourceLocation IMBUEMENT_CHAMBER = ars("imbuement_chamber");
    public static final ResourceLocation SCRIBES_TABLE = ars("scribes_table");
    public static final ResourceLocation CRUSHING = ars("crushing");
    public static final ResourceLocation APPARATUS = ars("enchanting_apparatus");
    public static final ResourceLocation ARMOR_UPGRADE = ars("armor_upgrade");
    public static final ResourceLocation ENCHANTMENT = ars("enchantment");
    public static final ResourceLocation IMBUEMENT = ars("imbuement");
    public static final ResourceLocation GLYPH = ars("glyph");
    public static final ResourceLocation CRUSH = ars("crush");
    public static final ResourceLocation REACTIVE_ENCHANTMENT = ars("reactive_enchantment");
    public static final ResourceLocation SPELL_WRITE = ars("spell_write");
    public static final ResourceLocation PRESTIDIGITATION = ars("prestidigitation");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private ArsNouveauRecipeEditorTypes() {
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
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.category.ars_nouveau.enchanting_apparatus",
                MOD_ID,
                REQUIRED_MODS,
                APPARATUS,
                RecipeEditorLayout.ARS_NOUVEAU_APPARATUS,
                ENCHANTING_APPARATUS
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.category.ars_nouveau.imbuement_chamber",
                MOD_ID,
                REQUIRED_MODS,
                IMBUEMENT,
                RecipeEditorLayout.ARS_NOUVEAU_IMBUEMENT,
                IMBUEMENT_CHAMBER
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SCRIBES_TABLE,
                "viscript_recipe.editor.category.ars_nouveau.scribes_table",
                MOD_ID,
                REQUIRED_MODS,
                GLYPH,
                RecipeEditorLayout.ARS_NOUVEAU_GLYPH,
                SCRIBES_TABLE
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                CRUSHING,
                "viscript_recipe.editor.category.ars_nouveau.crush",
                MOD_ID,
                REQUIRED_MODS,
                CRUSH,
                RecipeEditorLayout.ARS_NOUVEAU_CRUSH,
                ars("glyph_crush")
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                APPARATUS, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchanting_apparatus",
                ArsNouveauApparatusRecipeData.class, ArsNouveauApparatusRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ARMOR_UPGRADE, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.armor_upgrade",
                ArsNouveauArmorUpgradeRecipeData.class, ArsNouveauArmorUpgradeRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ENCHANTMENT, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchantment",
                ArsNouveauEnchantmentRecipeData.class, ArsNouveauEnchantmentRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                IMBUEMENT, IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.type.ars_nouveau.imbuement",
                ArsNouveauImbuementRecipeData.class, ArsNouveauImbuementRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                GLYPH, SCRIBES_TABLE,
                "viscript_recipe.editor.type.ars_nouveau.glyph",
                ArsNouveauGlyphRecipeData.class, ArsNouveauGlyphRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                CRUSH, CRUSHING,
                "viscript_recipe.editor.type.ars_nouveau.crush",
                ArsNouveauCrushRecipeData.class, ArsNouveauCrushRecipeData::new,
                MOD_ID
        ));
    }

    static ItemStack firstCrushOutput(ArsNouveauCrushRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().getFirst();
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    static void setFirstCrushOutput(ArsNouveauCrushRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(new ArsNouveauCrushOutputData());
        }
        data.getOutputs().getFirst().setItem(copy(stack));
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static ResourceLocation ars(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
