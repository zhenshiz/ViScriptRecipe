package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.canvas.MissingRecipeCanvas;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 缺少兼容模组时原样保存配方数据，编辑保存后仍可在模组恢复加载时读取。 */
@Getter
@Setter
@Accessors(chain = true)
public class MissingRecipeTypeHolder implements IVSRecipeData {
    public static final String MISSING = "missing";
    public static final RecipeEditorType TYPE = RecipeEditorType.of(
            ViScriptRecipe.id(MISSING), ViScriptRecipe.id(MISSING), MISSING,
            MissingRecipeTypeHolder.class, MissingRecipeCanvas.class);
    public static final RecipeEditorCategory CATEGORY = new RecipeEditorCategory(ResourceLocation.withDefaultNamespace("barrier"), MISSING, MISSING, ResourceLocation.parse(MISSING));
    static final List<String> entryKeys = List.of("enabled", "operation", "type", "recipeId");

    String missingDataName = "";
    CompoundTag missingData = new CompoundTag();

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        throw new UnsupportedOperationException("The mod for " + missingDataName + " is not loaded.");
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            if (entryKeys.contains(key)) continue;
            setMissingDataName(key).setMissingData(tag.getCompound(key));
            break;
        }
    }
}
