package com.viscript_recipe.compat.lychee.uitest;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.ServerContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.lychee.LycheeRecipeEditorTypes;
import com.viscript_recipe.compat.lychee.LycheeRecipeFactory;
import com.viscript_recipe.compat.lychee.LycheeRecipeRuntimeSupport;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LycheeEntityType;
import snownee.lychee.util.recipe.ILycheeRecipe;

import java.util.ArrayList;
import java.util.List;

@LDLRegisterClient(name = "lychee_recipe_cache", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        modID = LycheeRecipeEditorTypes.MOD_ID, environment = RegistrationEnvironment.DEV_ONLY)
public final class LycheeRecipeCacheScenario implements UIScenario {
    private List<RecipeHolder<?>> originalRecipes;
    private List<RecipeHolder<?>> baselineRecipes;

    @Override
    public void configure(ScenarioOptions options) {
        options.defaultTimeoutMs(30_000).tags("recipe", "lychee", "cache");
    }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.waitUntil("JEI has finished initial recipe loading", context -> LDLibJEIPlugin.jeiRuntime != null)
                .server("establish empty Lychee cache baseline", context -> {
                    var manager = context.server().getRecipeManager();
                    originalRecipes = List.copyOf(manager.getRecipes());
                    baselineRecipes = originalRecipes.stream()
                            .filter(holder -> !(holder.value() instanceof ILycheeRecipe<?>)).toList();
                    replaceAndRefresh(manager, baselineRecipes);
                    verifyCaches(context, false, false);
                })
                .server("add Lychee recipes and rebuild native indexes", context -> {
                    replaceAndRefresh(context.server().getRecipeManager(), fixtures(false));
                    verifyCaches(context, true, false);
                })
                .step("refresh the real client recipe manager without linkage errors", context ->
                        LycheeRecipeRuntimeSupport.refreshCaches(context.level().getRecipeManager()))
                .server("client refresh preserves integrated-server recipe caches", context ->
                        verifyCaches(context, true, false))
                .server("replace Lychee block and entity targets", context -> {
                    replaceAndRefresh(context.server().getRecipeManager(), fixtures(true));
                    verifyCaches(context, true, true);
                })
                .server("disable or delete Lychee recipes", context -> {
                    replaceAndRefresh(context.server().getRecipeManager(), baselineRecipes);
                    verifyCaches(context, false, false);
                })
                .server("restore Lychee recipes after removal", context -> {
                    replaceAndRefresh(context.server().getRecipeManager(), fixtures(false));
                    verifyCaches(context, true, false);
                })
                .server("restore the server recipe snapshot", context ->
                        replaceAndRefresh(context.server().getRecipeManager(), originalRecipes))
                .teardownServer("restore original recipes and native caches", context -> {
                    if (originalRecipes != null) {
                        replaceAndRefresh(context.server().getRecipeManager(), originalRecipes);
                    }
                });
    }

    private List<RecipeHolder<?>> fixtures(boolean alternative) {
        var recipes = new ArrayList<>(baselineRecipes);
        for (var name : List.of("block_interacting", "entity_ticking", "random_block_ticking", "crafting")) {
            var type = LycheeRecipeEditorTypes.id(name);
            var json = JsonParser.parseString(LycheeRecipeFactory.defaults(type)).getAsJsonObject();
            if (alternative) {
                switch (name) {
                    case "block_interacting" -> json.addProperty("block_in", "minecraft:obsidian");
                    case "random_block_ticking" -> json.addProperty("block_in", "minecraft:bricks");
                    case "entity_ticking" -> json.add("entity", JsonParser.parseString("{\"type\":\"minecraft:cow\"}"));
                    default -> { }
                }
            }
            recipes.add(new RecipeHolder<>(ViScriptRecipe.id("uitest/lychee_" + name),
                    LycheeRecipeFactory.compile(type, json.toString())));
        }
        return recipes;
    }

    private static void replaceAndRefresh(RecipeManager manager, List<RecipeHolder<?>> recipes) {
        manager.replaceRecipes(recipes);
        LycheeRecipeRuntimeSupport.refreshCaches(manager);
    }

    private static void verifyCaches(ServerContext context, boolean enabled, boolean alternative) {
        for (var type : List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.ENTITY_TICKING, RecipeTypes.RANDOM_BLOCK_TICKING)) {
            int expected = enabled ? 1 : 0;
            context.check(type.id + " cached recipe count", type.recipes().size() == expected, expected, type.recipes().size());
            context.check(type.id + " empty state", type.isEmpty() == !enabled, !enabled, type.isEmpty());
        }
        boolean originalTarget = enabled && !alternative;
        boolean replacementTarget = enabled && alternative;
        context.check("stone interaction index", RecipeTypes.BLOCK_INTERACTING.has(Blocks.STONE) == originalTarget);
        context.check("obsidian interaction index", RecipeTypes.BLOCK_INTERACTING.has(Blocks.OBSIDIAN) == replacementTarget);
        context.check("cobblestone random-tick index", RecipeTypes.RANDOM_BLOCK_TICKING.has(Blocks.COBBLESTONE) == originalTarget);
        context.check("bricks random-tick index", RecipeTypes.RANDOM_BLOCK_TICKING.has(Blocks.BRICKS) == replacementTarget);
        context.check("cobblestone cached random-tick flag", Blocks.COBBLESTONE.defaultBlockState().isRandomlyTicking() == originalTarget);
        context.check("bricks cached random-tick flag", Blocks.BRICKS.defaultBlockState().isRandomlyTicking() == replacementTarget);
        context.check("pig entity index", ((LycheeEntityType) EntityType.PIG).lychee$tickingRecipes().size() == (originalTarget ? 1 : 0));
        context.check("cow entity index", ((LycheeEntityType) EntityType.COW).lychee$tickingRecipes().size() == (replacementTarget ? 1 : 0));
        context.check("advanced crafting flag", RecipeTypes.HAS_CRAFTING_RECIPES == enabled);
    }
}
