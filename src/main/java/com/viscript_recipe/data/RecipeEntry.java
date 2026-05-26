package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.vanilla.CookingRecipeData;
import com.viscript_recipe.data.vanilla.ShapedCraftingRecipeData;
import com.viscript_recipe.data.vanilla.ShapelessCraftingRecipeData;
import com.viscript_recipe.data.vanilla.SmithingTransformRecipeData;
import com.viscript_recipe.data.vanilla.StonecuttingRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeEntry implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.entry.enabled")
    private boolean enabled = true;

    @Configurable(name = "viscript_recipe.config.entry.operation")
    @ConfigSelector(candidate = {"add", "replace", "remove"})
    private RecipeOperation operation = RecipeOperation.REPLACE;

    @Configurable(name = "viscript_recipe.config.entry.recipe_id")
    private ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath("viscript_recipe", "example");

    @Configurable(name = "viscript_recipe.config.entry.type")
    private ResourceLocation type = RecipeEditorTypes.CRAFTING_SHAPED;

    @Configurable(name = "viscript_recipe.config.entry.shaped", subConfigurable = true)
    private ShapedCraftingRecipeData shaped = new ShapedCraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.shapeless", subConfigurable = true)
    private ShapelessCraftingRecipeData shapeless = new ShapelessCraftingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.cooking", subConfigurable = true)
    private CookingRecipeData cooking = new CookingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.stonecutting", subConfigurable = true)
    private StonecuttingRecipeData stonecutting = new StonecuttingRecipeData();

    @Configurable(name = "viscript_recipe.config.entry.smithing_transform", subConfigurable = true)
    private SmithingTransformRecipeData smithingTransform = new SmithingTransformRecipeData();

    public Recipe<?> compile() {
        return RecipeEditorTypes.require(getType()).compile(this);
    }

    public ResourceLocation getType() {
        return type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
    }

    public RecipeEntry setType(ResourceLocation type) {
        this.type = type == null ? RecipeEditorTypes.CRAFTING_SHAPED : type;
        return this;
    }

    public boolean isType(ResourceLocation type) {
        return getType().equals(type);
    }
}
