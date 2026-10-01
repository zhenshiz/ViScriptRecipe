package com.viscript_recipe.compat.eidolon;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.eidolon.canvas.*;
import com.viscript_recipe.compat.eidolon.data.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = EidolonRecipeEditorTypes.MOD_ID, modID = EidolonRecipeEditorTypes.MOD_ID)
public final class EidolonRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "eidolon_repraised";
    public static final ResourceLocation CRUCIBLE = id("crucible");
    public static final ResourceLocation WORKTABLE = id("worktable");
    public static final ResourceLocation DYE = id("dye");
    public static final ResourceLocation BRAZIER = id("brazier");
    public static final ResourceLocation GENERIC_RITUAL = id("ritual_brazier");
    public static final ResourceLocation ITEM_RITUAL = id("ritual_brazier_crafting");
    public static final ResourceLocation SUMMON_RITUAL = id("ritual_brazier_summoning");
    public static final ResourceLocation COMMAND_RITUAL = id("ritual_brazier_command");
    public static final ResourceLocation LOCATION_RITUAL = id("ritual_brazier_location");
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return EidolonRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        category(CRUCIBLE, CRUCIBLE);
        category(WORKTABLE, WORKTABLE);
        category(BRAZIER, GENERIC_RITUAL);
        registerEditorType(RecipeEditorType.of(CRUCIBLE, CRUCIBLE, key(CRUCIBLE),
                EidolonCrucibleRecipeData.class, EidolonCrucibleRecipeData::new,
                (navigation, entry) -> new CrucibleCanvas(navigation, entry), MOD_ID));
        registerEditorType(RecipeEditorType.of(WORKTABLE, WORKTABLE, key(WORKTABLE),
                EidolonWorktableRecipeData.class, EidolonWorktableRecipeData::new,
                (navigation, entry) -> new WorktableCanvas(navigation, entry), MOD_ID));
        registerEditorType(RecipeEditorType.of(DYE, WORKTABLE, key(DYE),
                EidolonDyeRecipeData.class, EidolonDyeRecipeData::new,
                (navigation, entry) -> new DyeCanvas(navigation, entry), MOD_ID));
        for (var type : new ResourceLocation[]{GENERIC_RITUAL, ITEM_RITUAL, SUMMON_RITUAL, COMMAND_RITUAL, LOCATION_RITUAL}) {
            registerEditorType(RecipeEditorType.of(type, BRAZIER, key(type),
                    EidolonRitualRecipeData.class, EidolonRitualRecipeData::new,
                    (navigation, entry) -> new RitualCanvas(navigation, entry), MOD_ID));
        }
    }

    private void category(ResourceLocation category, ResourceLocation type) {
        registerCategory(RecipeEditorCategory.of(category, "viscript_recipe.editor.category.eidolon." + category.getPath(),
                MOD_ID, type, category));
    }

    private static String key(ResourceLocation id) { return "viscript_recipe.editor.type.eidolon." + id.getPath(); }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
