package com.viscript_recipe.data;

import com.google.gson.JsonObject;
import com.viscript_lib.gui.ai.AiStructuredData;
import com.viscript_recipe.ViScriptRecipe;

/**
 * 配方类型在注册时提供的 AI 数据契约。
 *
 * <p>默认实现只读写数据类声明的持久化字段，使用其真实编码并执行编译校验。特殊配方
 * 可以覆盖说明、Schema、读取、修改或校验；所有修改目标均为隔离草稿，不得保存文件、
 * 执行命令或操作服务端。各回调在客户端线程执行。
 */
public interface RecipeAiSupport {
    RecipeAiSupport AUTO = new RecipeAiSupport() { };

    /**
     * 返回此类型特有的字段含义、单位和约束。
     *
     * @return 给模型的说明；默认使用数据类的持久化编码
     */
    default String guide() {
        return "Edit only the returned persistent fields. Partial objects merge; arrays replace. Item stacks and ingredients use the returned JSON encoding. Keep unrelated fields unchanged.";
    }

    /**
     * 判断此类型是否接受 AI 修改。
     *
     * @return 默认返回 <code>true</code>；返回 <code>false</code> 时仍可读取现有数据
     */
    default boolean editable() { return true; }

    /**
     * 描述此类型接受的部分字段修改。
     *
     * @param type 已注册的配方类型
     * @param data 当前配方数据或已应用类型默认值的数据
     * @return 字段对象的 JSON Schema
     */
    default JsonObject schema(RecipeEditorType type, IVSRecipeData data) { return AiStructuredData.schema(data); }

    /**
     * 读取此类型面向 AI 的数据。
     *
     * @param type 已注册的配方类型
     * @param data 当前配方数据
     * @return 可按 <code>schema</code> 修改的字段
     */
    default JsonObject read(RecipeEditorType type, IVSRecipeData data) { return AiStructuredData.read(data); }

    /**
     * 修改此类型的隔离数据，不执行保存或上传。
     *
     * @param type 已注册的配方类型
     * @param data 可丢弃的草稿数据
     * @param fields 符合此类型 Schema 的部分字段
     * @throws IllegalArgumentException 字段或值无效
     */
    default void update(RecipeEditorType type, IVSRecipeData data, JsonObject fields) { AiStructuredData.update(data, fields); }

    /**
     * 检查修改后的数据是否能作为此类型使用。
     *
     * @param type 已注册的配方类型
     * @param data 修改后的草稿数据
     * @throws IllegalArgumentException 数据不满足配方要求
     */
    default void validate(RecipeEditorType type, IVSRecipeData data) { data.compile(ViScriptRecipe.id("placeholder"), type.id()); }
}
