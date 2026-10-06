package com.viscript_recipe.compat.justdirethings;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.justdirethings.canvas.*;
import com.viscript_recipe.compat.justdirethings.data.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.vanilla.VanillaRecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** 注册 Just Dire Things 的世界转化和特殊锻造配方。 */
@LDLRegister(registry = IModModule.ID, name = JustDireRecipeEditorTypes.MOD_ID, modID = JustDireRecipeEditorTypes.MOD_ID)
public final class JustDireRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "justdirethings";
    public static final List<String> TYPES = List.of("goospread", "goospread_tag", "fluiddrop", "ability", "paxel");
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return JustDireRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        for (var type : TYPES) {
            if (type.equals("ability") || type.equals("paxel")) {
                // 共用原版锻造台分类，保留模组配方的能力安装及附魔继承逻辑。
                registerEditorType(RecipeEditorType.of(id(type), VanillaRecipeEditorTypes.SMITHING_TABLE, key(type), JustDireSmithingData.class,
                        () -> JustDireSmithingData.create(type), JustDireSmithingCanvas::new, MOD_ID));
            } else {
                var icon = id(type.equals("fluiddrop") ? "polymorphic_catalyst" : "gooblock_tier1");
                registerCategory(RecipeEditorCategory.of(id(type), key(type), MOD_ID, id(type), icon));
                registerEditorType(RecipeEditorType.of(id(type), id(type), key(type), JustDireTransformationData.class,
                        () -> JustDireTransformationData.create(type), JustDireTransformationCanvas::new, MOD_ID));
            }
        }
    }

    /**
     * 构造此模组的资源标识。
     * @param path 资源路径
     * @return 带有模组命名空间的标识
     */
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    /**
     * 构造编辑器的翻译键。
     * @param path 翻译键后缀
     * @return 完整翻译键
     */
    public static String key(String path) { return "viscript_recipe.editor.justdirethings." + path; }
}
