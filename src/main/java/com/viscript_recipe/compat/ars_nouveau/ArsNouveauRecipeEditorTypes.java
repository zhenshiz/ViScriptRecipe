package com.viscript_recipe.compat.ars_nouveau;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.ars_nouveau.canvas.*;
import com.viscript_recipe.compat.ars_nouveau.data.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = ArsNouveauRecipeEditorTypes.MOD_ID, modID = ArsNouveauRecipeEditorTypes.MOD_ID)
public final class ArsNouveauRecipeEditorTypes implements IModModule{
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

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return ArsNouveauRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(RecipeEditorCategory.of(
                ENCHANTING_APPARATUS,
                "viscript_recipe.editor.category.ars_nouveau.enchanting_apparatus",
                MOD_ID, APPARATUS, ENCHANTING_APPARATUS
        ));
        registerCategory(RecipeEditorCategory.of(
                IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.category.ars_nouveau.imbuement_chamber",
                MOD_ID, IMBUEMENT, IMBUEMENT_CHAMBER
        ));
        registerCategory(RecipeEditorCategory.of(
                SCRIBES_TABLE,
                "viscript_recipe.editor.category.ars_nouveau.scribes_table",
                MOD_ID, GLYPH, SCRIBES_TABLE
        ));
        registerCategory(RecipeEditorCategory.of(
                CRUSHING,
                "viscript_recipe.editor.category.ars_nouveau.crush",
                MOD_ID, CRUSH, ars("glyph_crush")
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                APPARATUS, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchanting_apparatus",
                ArsNouveauApparatusRecipeData.class, ArsNouveauApparatusRecipeData::new,
                ApparatusCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                ARMOR_UPGRADE, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.armor_upgrade",
                ArsNouveauArmorUpgradeRecipeData.class, ArsNouveauArmorUpgradeRecipeData::new,
                ArmorUpgradeCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                ENCHANTMENT, ENCHANTING_APPARATUS,
                "viscript_recipe.editor.type.ars_nouveau.enchantment",
                ArsNouveauEnchantmentRecipeData.class, ArsNouveauEnchantmentRecipeData::new,
                EnchantmentCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                IMBUEMENT, IMBUEMENT_CHAMBER,
                "viscript_recipe.editor.type.ars_nouveau.imbuement",
                ArsNouveauImbuementRecipeData.class, ArsNouveauImbuementRecipeData::new,
                ImbuementCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                GLYPH, SCRIBES_TABLE,
                "viscript_recipe.editor.type.ars_nouveau.glyph",
                ArsNouveauGlyphRecipeData.class, ArsNouveauGlyphRecipeData::new,
                GlyphCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                CRUSH, CRUSHING,
                "viscript_recipe.editor.type.ars_nouveau.crush",
                ArsNouveauCrushRecipeData.class, ArsNouveauCrushRecipeData::new,
                CrushCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation ars(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
