package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.mojang.serialization.JsonOps;
import com.viscript_lib.gui.editor.EditorServerUploads;
import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.client.RecipeDeltaClientState;
import com.viscript_recipe.compat.jei.RecipeDeltaJeiSynchronizer;
import com.viscript_recipe.data.*;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.*;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.*;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@LDLRegisterClient(name = "viscript_recipe_replacement", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class RecipeReplacementScenario implements UIScenario {
    private static final ResourceLocation EXISTING = ResourceLocation.withDefaultNamespace("stick");
    private static final ResourceLocation MISSING = ResourceLocation.fromNamespaceAndPath("replacement_test", "nested/new_recipe");

    @Override
    public void configure(ScenarioOptions options) {
        options.guiScale(2).defaultTimeoutMs(30_000).scenarioTimeoutMs(180_000).tags("recipe", "replacement", "sync");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void define(ScenarioBuilder s) {
        s.waitUntil("JEI is ready", ctx -> runtime() != null)
                .server("record original recipes", sc -> {
                    sc.check("normal recipe mode", !Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get());
                    var manager = sc.server().getRecipeManager();
                    var original = manager.byKey(EXISTING).orElseThrow();
                    sc.check("target starts as the vanilla stick recipe", original.value() instanceof ShapedRecipe
                            && original.value().getResultItem(sc.server().registryAccess()).is(Items.STICK));
                    sc.check("new ID does not exist", manager.byKey(MISSING).isEmpty());
                    sc.put("original", original);
                    sc.put("baseCount", manager.getRecipes().size());
                    sc.put("path", RecipeAssetPaths.recipeDirectory().resolve("uitest_replace_" + UUID.randomUUID() + ".recipe"));
                })
                .openModularUI("open production recipe editor", ctx -> RecipeEditor.createUI()).awaitModularUI()
                .step("load new replacement project", ctx -> {
                    var project = new RecipeProject();
                    var entry = new RecipeEntry().setType(RecipeEditorTypes.CRAFTING_SHAPELESS)
                            .setRecipeId(ViScriptRecipe.id("generated_test_id")).setOperation(RecipeOperation.ADD);
                    project.getRecipeFile().getEntries().add(entry);
                    project.getRecipeFile().getEntries().add(new RecipeEntry().setRecipeId(MISSING)
                            .setType(RecipeEditorTypes.SMELTING).setOperation(RecipeOperation.REPLACE)
                            .setData(cooking(3)));
                    ctx.requireUI().getElementsByType(RecipeEditor.class).getFirst().loadProject(project, null);
                    ctx.requireUI().getElementsByType(NavigationView.class).getFirst().selectEntry(entry);
                    ctx.put("project", project);
                }).awaitElement("#recipe_entry_id")
                .focus("#recipe_entry_id").key(GLFW.GLFW_KEY_HOME)
                .keyDown(GLFW.GLFW_KEY_LEFT_SHIFT).key(GLFW.GLFW_KEY_END).keyUp(GLFW.GLFW_KEY_LEFT_SHIFT)
                .key(GLFW.GLFW_KEY_BACKSPACE).type(EXISTING.toString()).blur()
                .step("set replacement and edit ingredient and result slots", ctx -> {
                    Selector<RecipeOperation> operation = ctx.el("#recipe_entry_operation").as(Selector.class);
                    operation.setSelected(RecipeOperation.REPLACE);
                    RecipeCanvas.visualIngredientSlots[0].setItem(new ItemStack(Items.DIRT), true);
                    RecipeCanvas.visualOutputSlots[0].setItem(new ItemStack(Items.DIAMOND, 3), true);
                    var project = ctx.<RecipeProject>get("project");
                    var tag = project.serializeRuntimeFile(ctx.level().registryAccess());
                    var file = new RecipeFile();
                    file.deserializeNBT(ctx.level().registryAccess(), tag);
                    var entry = file.getEntries().getFirst();
                    ctx.check("input displays the requested ID", ctx.el("#recipe_entry_id").as(TextField.class).getValue().equals(EXISTING.toString()));
                    ctx.check("typed ID survives editor export", entry.getRecipeId().equals(EXISTING), EXISTING, entry.getRecipeId());
                    ctx.check("operation selector saves REPLACE", entry.getOperation() == RecipeOperation.REPLACE);
                    ctx.check("slot input survives editor export", entry.compile().getIngredients().getFirst().test(new ItemStack(Items.DIRT)));
                    ctx.check("slot output and count survive editor export", entry.<ShapelessCraftingRecipeData>getData().getResult().is(Items.DIAMOND)
                            && entry.<ShapelessCraftingRecipeData>getData().getResult().getCount() == 3);
                    ctx.put("file", file);
                    ctx.put("upload", tag);
                    EditorServerUploads.uploadToServer(RecipeProjectType.FORMAT, ctx.<Path>get("path").getFileName().toString(), tag);
                })
                .waitUntilServer("RPC upload writes the exact exported file", sc -> uploaded(sc.<Path>get("path"), sc.get("upload")))
                .closeScreen()
                .server("apply uploaded replacement through delta reload", sc -> reload(sc, true));
        verify(s, "first replacement", false, 3);
        s.step("edit the same IDs a second time", ctx -> {
            var file = ctx.<RecipeFile>get("file");
            file.getEntries().getFirst().<ShapelessCraftingRecipeData>getData().setResult(new ItemStack(Items.DIAMOND, 5));
            file.getEntries().get(1).<CookingRecipeData>getData().setResult(new ItemStack(Items.EMERALD, 5));
        }).server("save and replace again", sc -> { save(sc); reload(sc, true); });
        verify(s, "repeat replacement", false, 5);
        s.step("change the existing ID to a different recipe type", ctx -> {
            ctx.<RecipeFile>get("file").getEntries().getFirst().setType(RecipeEditorTypes.SMELTING)
                    .setData(new CookingRecipeData().setIngredient(RecipeIngredient.item(Items.DIRT))
                            .setResult(new ItemStack(Items.DIAMOND, 7)).setExperience(2.5f).setCookingTime(37));
            ctx.<RecipeFile>get("file").getEntries().get(1).<CookingRecipeData>getData().setResult(new ItemStack(Items.EMERALD, 7));
        }).server("replace across recipe types", sc -> { save(sc); reload(sc, true); });
        verify(s, "cross type replacement", true, 7);
        s.step("record JEI runtime before full reload", ctx -> ctx.put("runtimeBeforeReload", runtime()))
                .timeoutMs(90_000)
                .server("start full data pack reload", sc -> sc.put("reloadFuture",
                        sc.server().reloadResources(sc.server().getPackRepository().getSelectedIds())))
                .timeoutMs(90_000).waitUntilServer("data pack reload finishes", sc -> sc.<CompletableFuture<Void>>get("reloadFuture").isDone())
                .server("verify full reload completes", sc -> {
                    sc.<CompletableFuture<Void>>get("reloadFuture").join();
                    sc.check("full reload reads both persisted entries", RecipeOverrideManager.getLastResult().failedEntryCount() == 0);
                    sc.put("revision", -1L);
                })
                .waitUntil("full recipe packet rebuilds JEI", ctx -> runtime() != null && runtime() != ctx.<IJeiRuntime>get("runtimeBeforeReload"));
        verify(s, "full data pack reload", true, 7);
        s.step("disable both replacements", ctx -> ctx.<RecipeFile>get("file").getEntries().forEach(entry -> entry.setEnabled(false)))
                .server("save disabled entries and reload", sc -> { save(sc); reload(sc, false); })
                .waitUntil("restored recipes reach the client", ctx -> synced(ctx) && ctx.level().getRecipeManager().byKey(MISSING).isEmpty())
                .server("disabled entries restore the original server recipe", sc -> verifyRestored(sc.level(), sc.get("original"), sc.get("baseCount"), sc::check))
                .step("disabled entries restore client and JEI", ctx -> {
                    verifyRestored(ctx.level(), ctx.get("original"), ctx.get("baseCount"), ctx::check);
                    ctx.check("JEI restores one original crafting recipe", visible(EXISTING, RecipeEditorTypes.CRAFTING_SHAPED).size() == 1
                            && visible(EXISTING, RecipeEditorTypes.CRAFTING_SHAPED).getFirst().value().getResultItem(ctx.level().registryAccess()).is(Items.STICK));
                    ctx.check("JEI removes both replacement smelting recipes", visible(EXISTING, RecipeEditorTypes.SMELTING).isEmpty()
                            && visible(MISSING, RecipeEditorTypes.SMELTING).isEmpty());
                })
                .teardown("close editor and upload result dialogs", ctx -> ctx.mc().setScreen(null))
                .teardownServer("remove test file and restore recipe state", sc -> {
                    Path path = sc.get("path");
                    if (path != null) {
                        try { Files.deleteIfExists(path); }
                        catch (IOException e) { throw new IllegalStateException(e); }
                        var result = RecipeOverrideManager.reloadDelta(sc.server().getRecipeManager(), sc.server().registryAccess());
                        if (result.requiresFullSync()) RecipeReloadSyncService.syncFullToPlayers(sc.server(), false);
                        else RecipeReloadSyncService.syncDeltaToPlayers(sc.server(), result.delta());
                        sc.check("test file removed", !Files.exists(path));
                    }
                });
    }

    private static void verify(ScenarioBuilder s, String phase, boolean cooking, int count) {
        s.waitUntil(phase + " reaches client and JEI", ctx -> synced(ctx) && runtime() != null
                        && matchesResult(ctx.level().getRecipeManager().byKey(EXISTING).orElse(null), ctx.level().registryAccess(), Items.DIAMOND, count))
                .server(phase + " authoritative server recipes", sc -> verifyRecipes(sc.level(), sc.get("file"), sc.get("baseCount"), cooking, sc::check))
                .step(phase + " client, import and JEI", ctx -> {
                    verifyRecipes(ctx.level(), ctx.get("file"), ctx.get("baseCount"), cooking, ctx::check);
                    for (var entry : ctx.<RecipeFile>get("file").getEntries()) {
                        var imported = RecipeImporter.importRecipe(entry.getRecipeId()).entry();
                        ctx.check("can import the replacement under its original ID: " + entry.getRecipeId(), imported != null
                                && imported.getRecipeId().equals(entry.getRecipeId())
                                && sameRecipe(ctx.level().registryAccess(), imported.compile(), entry.compile()));
                        var type = entry.isType(RecipeEditorTypes.SMELTING) ? RecipeEditorTypes.SMELTING : RecipeEditorTypes.CRAFTING_SHAPED;
                        var jei = visible(entry.getRecipeId(), type);
                        ctx.check("JEI has exactly one replacement: " + entry.getRecipeId(), jei.size() == 1);
                        ctx.check("JEI uses the edited contents: " + entry.getRecipeId(), jei.size() == 1
                                && sameRecipe(ctx.level().registryAccess(), jei.getFirst().value(), entry.compile()));
                    }
                    if (cooking) ctx.check("JEI removes the old crafting category entry", visible(EXISTING, RecipeEditorTypes.CRAFTING_SHAPED).isEmpty());
                });
    }

    private static void verifyRecipes(Level level, RecipeFile file, int baseCount, boolean cooking, BiConsumer<String, Boolean> check) {
        var manager = level.getRecipeManager();
        check.accept("existing ID is overwritten and only missing ID increases recipe count", manager.getRecipes().size() == baseCount + 1);
        for (var entry : file.getEntries()) {
            var actual = manager.byKey(entry.getRecipeId()).orElse(null);
            check.accept("exact ID exists once: " + entry.getRecipeId(), actual != null
                    && manager.getRecipes().stream().filter(holder -> holder.id().equals(entry.getRecipeId())).count() == 1);
            check.accept("all recipe contents match edited data: " + entry.getRecipeId(), actual != null
                    && sameRecipe(level.registryAccess(), actual.value(), entry.compile()));
        }
        var oldInput = CraftingInput.of(1, 2, List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS)));
        check.accept("original stick recipe no longer matches", manager.getRecipesFor(RecipeType.CRAFTING, oldInput, level).stream()
                .noneMatch(holder -> holder.id().equals(EXISTING)));
        if (cooking) {
            check.accept("old crafting type has no target ID", manager.getAllRecipesFor(RecipeType.CRAFTING).stream().noneMatch(holder -> holder.id().equals(EXISTING)));
            check.accept("new furnace input matches the target ID", manager.getRecipesFor(RecipeType.SMELTING,
                    new SingleRecipeInput(new ItemStack(Items.DIRT)), level).stream().anyMatch(holder -> holder.id().equals(EXISTING)));
        } else {
            var newInput = CraftingInput.of(1, 1, List.of(new ItemStack(Items.DIRT)));
            var holder = manager.getRecipesFor(RecipeType.CRAFTING, newInput, level).stream().filter(recipe -> recipe.id().equals(EXISTING)).findFirst().orElse(null);
            check.accept("edited crafting input really matches", holder != null);
            check.accept("crafting assembles the edited item and count", holder != null && ItemStack.matches(
                    holder.value().assemble(newInput, level.registryAccess()), file.getEntries().getFirst().<ShapelessCraftingRecipeData>getData().getResult()));
        }
        check.accept("missing ID behaves as an added furnace recipe", manager.getRecipesFor(RecipeType.SMELTING,
                new SingleRecipeInput(new ItemStack(Items.COBBLESTONE)), level).stream().anyMatch(holder -> holder.id().equals(MISSING)));
    }

    private static void verifyRestored(Level level, RecipeHolder<?> original, int baseCount, BiConsumer<String, Boolean> check) {
        var manager = level.getRecipeManager();
        check.accept("original recipe restored", manager.byKey(EXISTING).map(holder -> sameRecipe(level.registryAccess(), holder.value(), original.value())).orElse(false));
        check.accept("missing ID removed when disabled", manager.byKey(MISSING).isEmpty());
        check.accept("original total count restored", manager.getRecipes().size() == baseCount);
    }

    private static void reload(ServerContext sc, boolean expectChanges) {
        var result = RecipeOverrideManager.reloadDelta(sc.server().getRecipeManager(), sc.server().registryAccess());
        sc.check("no failed entries", result.applyResult().failedEntryCount() == 0);
        sc.check("small replacement uses incremental sync", !result.requiresFullSync());
        if (result.requiresFullSync()) throw new IllegalStateException("Unexpected full-sync fallback: " + result.fallbackReason());
        var delta = result.delta();
        sc.put("revision", delta.revision());
        if (expectChanges) {
            sc.check("delta upserts the two exact IDs", delta.upsertedRecipes().stream().map(RecipeHolder::id).collect(java.util.stream.Collectors.toSet())
                    .equals(java.util.Set.of(EXISTING, MISSING)));
            sc.check("replacement does not separately remove target IDs", delta.removedRecipeIds().isEmpty());
        }
        RecipeReloadSyncService.syncDeltaToPlayers(sc.server(), delta);
    }

    private static void save(ServerContext sc) {
        try { RecipeFileLoader.save(sc.get("path"), sc.get("file"), sc.server().registryAccess()); }
        catch (IOException e) { throw new IllegalStateException(e); }
    }

    private static boolean uploaded(Path path, CompoundTag expected) {
        try { return Files.exists(path) && expected.equals(NbtIo.read(path)); }
        catch (IOException e) { throw new IllegalStateException(e); }
    }

    private static boolean synced(TestContext ctx) {
        return RecipeDeltaClientState.jeiSyncState().revision() == ctx.<Long>get("revision");
    }

    private static CookingRecipeData cooking(int count) {
        return new CookingRecipeData().setIngredient(RecipeIngredient.item(Items.COBBLESTONE))
                .setResult(new ItemStack(Items.EMERALD, count)).setExperience(1.25f).setCookingTime(43);
    }

    private static boolean sameRecipe(RegistryAccess provider, Recipe<?> actual, Recipe<?> expected) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        return Recipe.CODEC.encodeStart(ops, actual).getOrThrow().equals(Recipe.CODEC.encodeStart(ops, expected).getOrThrow());
    }

    private static boolean matchesResult(RecipeHolder<?> holder, RegistryAccess provider, net.minecraft.world.item.Item item, int count) {
        if (holder == null) return false;
        var result = holder.value().getResultItem(provider);
        return result.is(item) && result.getCount() == count;
    }

    private static List<RecipeHolder<?>> visible(ResourceLocation id, ResourceLocation type) {
        var manager = runtime().getRecipeManager();
        var jeiType = manager.getRecipeType(type.equals(RecipeEditorTypes.CRAFTING_SHAPED)
                ? ResourceLocation.withDefaultNamespace("crafting") : type).orElseThrow();
        return manager.createRecipeLookup(jeiType).get().filter(RecipeHolder.class::isInstance)
                .map(candidate -> (RecipeHolder<?>) candidate).filter(holder -> holder.id().equals(id)
                        || (holder.id().getNamespace().equals(ViScriptRecipe.MOD_ID) && holder.id().getPath().startsWith("jei_delta/")
                        && holder.id().getPath().endsWith("/" + id.getNamespace() + "/" + id.getPath())))
                .collect(java.util.stream.Collectors.toList());
    }

    private static IJeiRuntime runtime() {
        try {
            var field = RecipeDeltaJeiSynchronizer.class.getDeclaredField("runtime");
            field.setAccessible(true);
            return (IJeiRuntime) field.get(null);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
