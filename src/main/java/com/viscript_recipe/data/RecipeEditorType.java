package com.viscript_recipe.data;

import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public record RecipeEditorType(
        ResourceLocation id, ResourceLocation category,
        String translationKey,
        Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
        BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier,
        RecipeAiSupport aiSupport
) {
    /**
     * 使用自动持久化字段契约创建配方类型，兼容现有六参数注册代码。
     *
     * @param id 配方类型标识
     * @param category 工作站分类标识
     * @param translationKey 类型显示名称翻译键
     * @param dataClass 配方数据类
     * @param dataSupplier 数据实例工厂
     * @param canvasSupplier 客户端画布工厂；专用服务端可为 <code>null</code>
     */
    public RecipeEditorType(ResourceLocation id, ResourceLocation category, String translationKey,
                            Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
                            BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier) {
        this(id, category, translationKey, dataClass, dataSupplier, canvasSupplier, RecipeAiSupport.AUTO);
    }

    /**
     * 返回使用指定 AI 契约的同一配方类型定义。
     *
     * @param support 字段说明、读写及校验契约
     * @return 可传入 <code>registerEditorType</code> 的新类型定义
     */
    public RecipeEditorType withAiSupport(RecipeAiSupport support) {
        return new RecipeEditorType(id, category, translationKey, dataClass, dataSupplier, canvasSupplier, java.util.Objects.requireNonNull(support));
    }

    public static RecipeEditorType of(ResourceLocation id, ResourceLocation category, String translationKey,
                                      Class<? extends IVSRecipeData> dataClass, Class<? extends RecipeCanvas<?>> canvas) {
        return new RecipeEditorType(id, category, translationKey, dataClass, dataSupplier(dataClass), canvasSupplier(canvas));
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    private static Supplier<? extends IVSRecipeData> dataSupplier(Class<? extends IVSRecipeData> dataClass) {
        return () -> {
            try {
                return dataClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) { return null; }
        };
    }

    private static BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier(Class<? extends RecipeCanvas<?>> canvasClass) {
        if (FMLEnvironment.dist.isDedicatedServer()) return null;
        return (view, entry) -> {
            try {
                return canvasClass.getDeclaredConstructor(NavigationView.class, RecipeEntry.class).newInstance(view, entry);
            } catch (Exception e) { return null; }
        };
    }
}
