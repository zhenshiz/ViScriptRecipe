package com.viscript_recipe.gui.editor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.configurator.EditAction;
import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.viscript_lib.gui.ai.AiEditorAdapter;
import com.viscript_lib.gui.ai.AiJson;
import com.viscript_lib.gui.ai.AiStructuredData;
import com.viscript_lib.gui.editor.ViScriptEditor;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeFile;
import com.viscript_recipe.data.RecipeOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** 将配方文件及实际注册的配方类型接入 AI 草稿和原生撤销历史。 */
public final class RecipeAiAdapter implements AiEditorAdapter {
    @Override
    public String guide() {
        return """
                This is ViScriptRecipe. Left: recipe types and entries; center: recipe canvas; right: properties.
                read_editor section=recipes (default) reads entries. section=types lists the ACTUALLY registered
                editor recipe types and their complete default data templates. Use query to find a type or ID.
                Before creating a recipe, read its exact type template. data uses LDLib2's returned JSON encoding,
                including item stacks {id, count, components}, ingredient kinds and enum values. Do not guess fields.
                put_recipe creates or updates by recipe_id; omit type to keep it. data recursively merges object
                fields and replaces arrays. Existing fields omitted from data are preserved. Changing type resets
                that recipe's data to the new type's defaults. operation ADD/REPLACE/REMOVE means runtime recipe
                registration behavior; delete_recipe removes the entry from this editing file. set_file edits
                pack_id and recipe_namespace. Registered mod integrations use the same type/template workflow.
                Only local project data is edited. Saving, exporting and uploading are the player's menu actions.
                """;
    }

    @Override
    public JsonObject readSchema() { return AiStructuredData.readSchema("recipes (default) or types (registered recipe types and data templates)."); }

    @Override
    public JsonObject describe(IProject project, int offset, int limit) { return describe(project, new JsonObject(), offset, limit); }

    @Override
    public JsonObject describe(IProject project, JsonObject arguments, int offset, int limit) {
        AiJson.keys(arguments, "offset", "limit", "section", "query");
        var file = ((RecipeProject) project).getRecipeFile();
        String section = arguments.has("section") ? AiJson.string(arguments, "section") : "recipes";
        var rows = new JsonArray();
        if (section.equals("types")) for (var type : RecipeEditorTypes.all()) {
            var row = new JsonObject();
            row.addProperty("type", type.id().toString());
            row.addProperty("name", type.displayName().getString());
            var entry = new RecipeEntry().setType(type.id());
            entry.applyDefaultData();
            row.addProperty("editable", type.aiSupport().editable());
            row.addProperty("guide", type.aiSupport().guide());
            row.add("data_template", type.aiSupport().read(type, entry.getData()));
            row.add("data_schema", type.aiSupport().schema(type, entry.getData()));
            rows.add(row);
        } else if (section.equals("recipes")) for (var entry : file.getEntries()) {
            var row = new JsonObject();
            row.addProperty("recipe_id", entry.getRecipeId().toString());
            row.addProperty("type", entry.getType().toString());
            row.addProperty("enabled", entry.isEnabled());
            row.addProperty("operation", entry.getOperation().name());
            var type = RecipeEditorTypes.require(entry.getType());
            row.addProperty("editable", type.aiSupport().editable());
            row.add("data", type.aiSupport().read(type, entry.getData()));
            rows.add(row);
        } else throw new IllegalArgumentException("Unknown recipe section: " + section);
        var result = AiStructuredData.page(rows, arguments, offset, limit);
        result.addProperty("pack_id", file.getPackId());
        result.addProperty("recipe_namespace", file.getRecipeNamespace());
        return result;
    }

    @Override
    public JsonObject editSchema() {
        var schema = AiJson.object();
        var operations = AiJson.property(schema, "operations", "array", "Ordered recipe edits. Each request is atomic.");
        var variants = new JsonArray();
        var put = operation("put_recipe");
        AiJson.property(put, "recipe_id", "string", "Full recipe ID; identifies an entry uniquely.");
        var type = AiJson.property(put, "type", "string", "Required for new entries. Read section=types query=<type> first.");
        var types = new JsonArray();
        RecipeEditorTypes.all().forEach(t -> types.add(t.id().toString()));
        type.add("enum", types);
        AiJson.property(put, "enabled", "boolean", "Whether this entry is enabled.");
        var mode = AiJson.property(put, "operation", "string", "Runtime registration behavior.");
        var modes = new JsonArray();
        for (var value : RecipeOperation.values()) modes.add(value.name());
        mode.add("enum", modes);
        AiJson.property(put, "data", "object", "Partial fields using the exact JSON template returned for this type; objects merge, arrays replace.");
        AiJson.required(put, "op", "recipe_id");
        variants.add(put);
        var remove = operation("delete_recipe");
        AiJson.property(remove, "recipe_id", "string", "Entry to delete from this file.");
        AiJson.required(remove, "op", "recipe_id");
        variants.add(remove);
        var file = operation("set_file");
        AiJson.property(file, "pack_id", "string", "Recipe pack ID.");
        AiJson.property(file, "recipe_namespace", "string", "Valid resource namespace for exported recipes.");
        variants.add(file);
        var item = new JsonObject(); item.add("oneOf", variants); operations.add("items", item); operations.addProperty("minItems", 1);
        AiJson.required(schema, "operations");
        return schema;
    }

    private static JsonObject operation(String name) {
        var result = AiJson.object();
        var values = new JsonArray(); values.add(name);
        AiJson.property(result, "op", "string", "Operation.").add("enum", values);
        AiJson.required(result, "op");
        return result;
    }

    @Override
    public List<String> edit(IProject draft, JsonObject arguments) {
        AiJson.keys(arguments, "operations");
        if (!arguments.has("operations") || !arguments.get("operations").isJsonArray() || arguments.getAsJsonArray("operations").isEmpty())
            throw new IllegalArgumentException("Expected nonempty operations array");
        var file = ((RecipeProject) draft).getRecipeFile();
        var changes = new ArrayList<String>();
        for (var raw : arguments.getAsJsonArray("operations")) {
            var op = raw.getAsJsonObject();
            String kind = AiJson.string(op, "op");
            if (kind.equals("set_file")) {
                AiJson.keys(op, "op", "pack_id", "recipe_namespace");
                if (op.has("pack_id")) {
                    var value = AiJson.string(op, "pack_id");
                    changes.add(change("pack_id", file.getPackId(), value)); file.setPackId(value);
                }
                if (op.has("recipe_namespace")) {
                    var value = AiJson.string(op, "recipe_namespace");
                    if (!RecipeFile.isValidRecipeNamespace(value)) throw new IllegalArgumentException("Invalid recipe namespace");
                    changes.add(change("recipe_namespace", file.getRecipeNamespace(), value)); file.setRecipeNamespace(value);
                }
                continue;
            }
            var id = new ResourceLocation(AiJson.string(op, "recipe_id"));
            var matches = file.getEntries().stream().filter(e -> e.getRecipeId().equals(id)).toList();
            if (matches.size() > 1) throw new IllegalArgumentException("Ambiguous duplicate recipe ID: " + id);
            var entry = matches.isEmpty() ? null : matches.get(0);
            if (entry != null && !RecipeEditorTypes.require(entry.getType()).aiSupport().editable())
                throw new IllegalArgumentException("AI editing is disabled for recipe type: " + entry.getType());
            String before = entry == null ? "∅" : describeEntry(entry);
            if (kind.equals("delete_recipe")) {
                AiJson.keys(op, "op", "recipe_id");
                if (entry == null) throw new IllegalArgumentException("Unknown recipe: " + id);
                file.getEntries().remove(entry);
                changes.add(change(id.toString(), before, "∅"));
            } else if (kind.equals("put_recipe")) {
                AiJson.keys(op, "op", "recipe_id", "type", "enabled", "operation", "data");
                if (entry == null) {
                    if (!op.has("type")) throw new IllegalArgumentException("New recipe requires type");
                    entry = new RecipeEntry().setRecipeId(id); file.getEntries().add(entry);
                }
                if (op.has("type")) {
                    var type = new ResourceLocation(AiJson.string(op, "type"));
                    if (RecipeEditorTypes.get(type).isEmpty()) throw new IllegalArgumentException("Unavailable recipe type: " + type);
                    if (matches.isEmpty() || !entry.getType().equals(type)) {
                        entry.setType(type);
                        entry.setData(RecipeEditorTypes.require(type).dataSupplier().get());
                        entry.applyDefaultData();
                    }
                }
                if (op.has("enabled")) entry.setEnabled(AiJson.bool(op, "enabled"));
                if (op.has("operation")) entry.setOperation(RecipeOperation.valueOf(AiJson.string(op, "operation")));
                var type = RecipeEditorTypes.require(entry.getType());
                if (!type.aiSupport().editable()) throw new IllegalArgumentException("AI editing is disabled for recipe type: " + type.id());
                if (op.has("data")) type.aiSupport().update(type, entry.getData(), op.getAsJsonObject("data"));
                if (entry.isEnabled() && entry.getOperation() != RecipeOperation.REMOVE) type.aiSupport().validate(type, entry.getData());
                changes.add(change(id.toString(), before, describeEntry(entry)));
            } else throw new IllegalArgumentException("Unknown recipe operation: " + kind);
        }
        return changes;
    }

    private static String describeEntry(RecipeEntry entry) {
        var type = RecipeEditorTypes.require(entry.getType());
        return entry.getType() + " / " + entry.getOperation() + " / " + entry.isEnabled() + " / " + type.aiSupport().read(type, entry.getData());
    }
    private static String change(String field, Object before, Object after) { return Component.translatable("viscript_recipe.ai.change", field, before, after).getString(); }

    @Override
    public EditAction createAction(ViScriptEditor editor, IProject draft) {
        var target = (RecipeProject) editor.getCurrentProject();
        var before = target.getRecipeFile();
        var after = ((RecipeProject) draft).getRecipeFile();
        return EditAction.of(() -> target.installAiFile(after), () -> target.installAiFile(before));
    }

    @Override
    public void setPreviewMode(ViScriptEditor editor, boolean previewing) { ((RecipeProject) editor.getCurrentProject()).setAiPreviewing(previewing); }

    @Override
    public void refresh(ViScriptEditor editor) { ((RecipeProject) editor.getCurrentProject()).refreshAiViews(); }
}
