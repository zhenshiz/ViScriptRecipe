package com.viscript_recipe.gui.editor;

import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.settings.EditorSettings;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.viscript_lib.gui.ai.AiJson;
import com.viscript_lib.gui.editor.ViScriptEditor;
import com.viscript_lib.gui.editor.dev.AiAdapterScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.List;

/** 验证配方模板、类型注册 AI 契约、画布预览和整轮撤销。 */
@LDLRegisterClient(name = "recipe_ai_adapter", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class RecipeAiScenario extends AiAdapterScenario {
    private static final ResourceLocation CUSTOM = ResourceLocation.parse("ai_fixture:custom");

    @Override
    protected ViScriptEditor createEditor(TestContext ctx) {
        var vanilla = RecipeEditorTypes.require(RecipeEditorTypes.CRAFTING_SHAPELESS);
        ctx.put("custom-updates", new java.util.concurrent.atomic.AtomicInteger());
        RecipeEditorTypes.register(new RecipeEditorType(CUSTOM, vanilla.category(), vanilla.translationKey(), vanilla.dataClass(), vanilla.dataSupplier(), vanilla.canvasSupplier())
                .withAiSupport(new RecipeAiSupport() {
                    @Override public String guide() { return "Fixture output_count changes the diamond result; 13 is forbidden."; }
                    @Override public JsonObject read(RecipeEditorType type, IVSRecipeData data) {
                        var result = new JsonObject(); result.addProperty("output_count", ((ShapelessCraftingRecipeData) data).getResult().getCount()); return result;
                    }
                    @Override public JsonObject schema(RecipeEditorType type, IVSRecipeData data) {
                        var result = AiJson.object(); AiJson.property(result, "output_count", "integer", "Diamond count 1..64, except 13."); return result;
                    }
                    @Override public void update(RecipeEditorType type, IVSRecipeData data, JsonObject fields) {
                        AiJson.keys(fields, "output_count");
                        var counter = (java.util.concurrent.atomic.AtomicInteger) ctx.get("custom-updates"); counter.incrementAndGet();
                        ((ShapelessCraftingRecipeData) data).setResult(new ItemStack(Items.DIAMOND, AiJson.integer(fields, "output_count", 1, 1, 64)));
                    }
                    @Override public void validate(RecipeEditorType type, IVSRecipeData data) {
                        RecipeAiSupport.super.validate(type, data);
                        if (((ShapelessCraftingRecipeData) data).getResult().getCount() == 13) throw new IllegalArgumentException("Fixture rejects 13");
                    }
                }));
        var project = new RecipeProject();
        var keep = new RecipeEntry().setRecipeId(ResourceLocation.parse("ai_fixture:keep")).setType(RecipeEditorTypes.CRAFTING_SHAPELESS);
        ((ShapelessCraftingRecipeData) keep.getData()).setShowNotification(false);
        project.getRecipeFile().getEntries().add(keep);
        var editor = new RecipeEditor() {
            @Override protected EditorSettings createSettings() {
                return super.createSettings().setSettingsFile(Path.of(System.getProperty("ldlib2.uitest.out"), "recipe-ai-settings.json").toFile());
            }
        };
        editor.loadProject(project, null);
        return editor;
    }

    @Override
    protected List<JsonObject> reads(TestContext ctx) {
        return List.of(json("{\"section\":\"types\",\"query\":\"minecraft:crafting_shapeless\"}"),
                json("{\"section\":\"types\",\"query\":\"ai_fixture:custom\"}"), json("{\"section\":\"recipes\",\"limit\":1}"));
    }

    @Override
    protected JsonObject edits(TestContext ctx) {
        var result = json("""
                {"operations":[
                 {"op":"set_file","pack_id":"AI Pack","recipe_namespace":"ai_fixture"},
                 {"op":"put_recipe","recipe_id":"ai_fixture:new","type":"minecraft:crafting_shapeless","operation":"ADD","data":{"result":{"id":"minecraft:diamond","count":3}}},
                 {"op":"put_recipe","recipe_id":"ai_fixture:custom","type":"ai_fixture:custom","data":{"output_count":7}}
                ]}
                """);
        var ingredients = new com.google.gson.JsonArray();
        ingredients.add(com.viscript_lib.gui.ai.AiStructuredData.read(RecipeIngredient.tag("minecraft:planks").setCount(2)));
        result.getAsJsonArray("operations").get(1).getAsJsonObject().getAsJsonObject("data").add("ingredients", ingredients);
        return result;
    }

    @Override
    protected void verifyEdited(TestContext ctx) {
        var project = (RecipeProject) editor(ctx).getCurrentProject();
        var file = project.getRecipeFile();
        ctx.check("配方和文件设置已更新", file.getEntries().size() == 3 && file.getPackId().equals("AI Pack"));
        var added = file.getEntries().stream().filter(e -> e.getRecipeId().toString().equals("ai_fixture:new")).findFirst().orElseThrow();
        ctx.check("自动契约写入实际配方结果", ((ShapelessCraftingRecipeData) added.getData()).getResult().is(Items.DIAMOND)
                && ((ShapelessCraftingRecipeData) added.getData()).getResult().getCount() == 3);
        project.selectEntry(added);
        var ingredient = ((ShapelessCraftingRecipeData) added.getData()).getIngredients().getFirst();
        ctx.check("原料数组按实际编码更新", ingredient.getKind() == IngredientValueKind.TAG && ingredient.getTag().toString().equals("minecraft:planks") && ingredient.getCount() == 2);
        var custom = file.getEntries().stream().filter(e -> e.getType().equals(CUSTOM)).findFirst().orElseThrow();
        ctx.check("自定义类型契约生效", ((ShapelessCraftingRecipeData) custom.getData()).getResult().getCount() == 7
                && ((java.util.concurrent.atomic.AtomicInteger) ctx.get("custom-updates")).get() > 0);
        ctx.check("未修改条目及通知设置保留", !((ShapelessCraftingRecipeData) file.getEntries().getFirst().getData()).getShowNotification());
        ctx.check("类型读取返回模板和 Schema", readResults(ctx).getFirst().getAsJsonArray("entries").get(0).getAsJsonObject().has("data_schema"));
        ctx.check("注册的 AI 说明可以被模型读取", readResults(ctx).get(1).toString().contains("Fixture output_count"));
    }

    @Override
    protected void verifyValidation(TestContext ctx) {
        var project = (RecipeProject) editor(ctx).getCurrentProject();
        var before = project.serializeNBT(Platform.getFrozenRegistry()).copy();
        var draft = new RecipeProject(); draft.deserializeNBT(Platform.getFrozenRegistry(), before.copy());
        boolean rejected = false;
        try { editor(ctx).createAiEditorAdapter().edit(draft, json("""
                {"operations":[{"op":"set_file","pack_id":"bad"},{"op":"put_recipe","recipe_id":"ai_fixture:keep","data":{"invented":true}}]}
                """)); } catch (IllegalArgumentException exception) { rejected = true; }
        ctx.check("未知字段不会部分改动编辑器", rejected && before.equals(project.serializeNBT(Platform.getFrozenRegistry())));
        rejected = false;
        try { editor(ctx).createAiEditorAdapter().edit(draft, json("""
                {"operations":[{"op":"put_recipe","recipe_id":"ai_fixture:custom","type":"ai_fixture:custom","data":{"output_count":13}}]}
                """)); } catch (IllegalArgumentException exception) { rejected = true; }
        ctx.check("注册类型的自定义校验会执行", rejected);
    }
}
