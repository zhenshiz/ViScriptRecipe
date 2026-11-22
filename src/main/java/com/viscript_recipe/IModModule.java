package com.viscript_recipe;

import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.registry.ILDLRegister;
import com.viscript_recipe.compat.jei.JeiRecipeCompatibility;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.recipe.RecipeCompatibility;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public interface IModModule extends ILDLRegister<IModModule, Supplier<IModModule>> {
    String ID = ViScriptRecipe.MOD_ID + ":mod_module";
    AutoRegistry.LDLibRegister<IModModule, Supplier<IModModule>> MODULES =
            AutoRegistry.LDLibRegister.create(ResourceLocation.parse(ID), IModModule.class, AutoRegistry::noArgsCreator);

    RecipeImportHandler importHandler();

    void registerEditorTypes();

    default void registerRuntime(IEventBus modEventBus) {}

    /**
     * 声明此模块在原生配方重载和同步中的可选处理。
     *
     * @param registration 接收配方兼容处理的注册入口
     */
    default void registerRecipeCompatibility(RecipeCompatibility.Registration registration) {}

    /**
     * 声明客户端 JEI 运行时使用的配方分类和刷新处理。
     *
     * @param registration 接收 JEI 兼容处理的注册入口
     */
    default void registerJeiCompatibility(JeiRecipeCompatibility.Registration registration) {}

    default void registerCategory(RecipeEditorCategory category) {RecipeEditorTypes.registerCategory(category);}

    default void registerEditorType(RecipeEditorType editorType) {RecipeEditorTypes.register(editorType);}
}
