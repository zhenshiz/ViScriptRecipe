package com.viscript_recipe.compat.extradelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

/** 发酵阶段的追加原料、等待时间和盖子要求。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightVatStageData implements ISkipDefaultedSerialize {
    @Persisted private RecipeIngredient ingredient = RecipeIngredient.item(Items.SUGAR);
    @Persisted private String ingredientCondition = "";
    @Persisted private int time = 24000;
    @Persisted private boolean lid = true;
}
