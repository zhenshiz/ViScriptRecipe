package com.viscript_recipe.compat.bakery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.satisfy.bakery.core.recipe.BlankCakeInteractionRecipe;
import net.satisfy.bakery.core.recipe.BlankCakeStage;
import net.satisfy.bakery.core.registry.ObjectRegistry;

/** 保存 Bakery 蛋糕交互的完整结果，区分可选字段缺省与显式方块形态。 */
@Getter
@Setter
@Accessors(chain = true)
public final class BakeryCakeInteractionRecipeData implements IVSRecipeData {
    @Persisted private String stage = "CAKE";
    @Persisted private FarmCharmIngredientData input = new FarmCharmIngredientData();
    @Persisted private int priority = 10;
    @Persisted private boolean replaceBlock = true;
    @Persisted private ResourceLocation block = ResourceLocation.parse("bakery:chocolate_cake");
    @Persisted private boolean patchState;
    @Persisted private boolean cake = true;
    @Persisted private boolean cupcake;
    @Persisted private boolean cookie;
    @Persisted private boolean returnItem = true;
    @Persisted private ResourceLocation item = ResourceLocation.parse("bakery:jar");
    @Persisted private boolean playSound = true;
    @Persisted private ResourceLocation sound = ResourceLocation.parse("minecraft:slime_block_place");
    @Persisted private boolean consumeOne = true;
    @Persisted private boolean particles = true;
    @Persisted private int cooldownTicks;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new BlankCakeInteractionRecipe(BlankCakeStage.valueOf(stage), input.compile(),
                new BlankCakeInteractionRecipe.Result(replaceBlock ? block : null,
                        patchState ? new BlankCakeInteractionRecipe.StatePatch(cake, cupcake, cookie) : null,
                        returnItem ? item : null, playSound ? sound : null, consumeOne, particles, cooldownTicks), priority);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        stage = "CAKE";
        input = FarmCharmIngredientData.of(RecipeIngredient.item(ObjectRegistry.CHOCOLATE_JAM.get().asItem()));
        priority = 10;
        replaceBlock = true;
        block = ResourceLocation.parse("bakery:chocolate_cake");
        patchState = false;
        cake = true;
        cupcake = cookie = false;
        returnItem = playSound = consumeOne = particles = true;
        item = ResourceLocation.parse("bakery:jar");
        sound = ResourceLocation.parse("minecraft:slime_block_place");
        cooldownTicks = 0;
    }
}
