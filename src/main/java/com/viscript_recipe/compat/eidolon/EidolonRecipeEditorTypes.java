package com.viscript_recipe.compat.eidolon;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.eidolon.canvas.CrucibleCanvas;
import com.viscript_recipe.compat.eidolon.canvas.DyeCanvas;
import com.viscript_recipe.compat.eidolon.canvas.RitualCanvas;
import com.viscript_recipe.compat.eidolon.canvas.WorktableCanvas;
import com.viscript_recipe.compat.eidolon.data.EidolonCrucibleRecipeData;
import com.viscript_recipe.compat.eidolon.data.EidolonDyeRecipeData;
import com.viscript_recipe.compat.eidolon.data.EidolonRitualRecipeData;
import com.viscript_recipe.compat.eidolon.data.EidolonWorktableRecipeData;
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

    @Override
    public RecipeImportHandler importHandler() { return EidolonRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        category(CRUCIBLE, CRUCIBLE);
        category(WORKTABLE, WORKTABLE);
        category(BRAZIER, GENERIC_RITUAL);
        registerEditorType(RecipeEditorType.of(CRUCIBLE, CRUCIBLE, key(CRUCIBLE),
                EidolonCrucibleRecipeData.class, CrucibleCanvas.class));
        registerEditorType(RecipeEditorType.of(WORKTABLE, WORKTABLE, key(WORKTABLE),
                EidolonWorktableRecipeData.class, WorktableCanvas.class));
        registerEditorType(RecipeEditorType.of(DYE, WORKTABLE, key(DYE),
                EidolonDyeRecipeData.class, DyeCanvas.class));
        for (var type : new ResourceLocation[]{GENERIC_RITUAL, ITEM_RITUAL, SUMMON_RITUAL, COMMAND_RITUAL, LOCATION_RITUAL}) {
            registerEditorType(RecipeEditorType.of(type, BRAZIER, key(type),
                    EidolonRitualRecipeData.class, RitualCanvas.class));
        }
    }

    private void category(ResourceLocation category, ResourceLocation type) {
        registerCategory(RecipeEditorCategory.of(category, "viscript_recipe.editor.category.eidolon." + category.getPath(),
                MOD_ID, type, category));
    }

    private static String key(ResourceLocation id) { return "viscript_recipe.editor.type.eidolon." + id.getPath(); }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
