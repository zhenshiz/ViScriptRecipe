package com.viscript_recipe.uitest;

import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.*;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.MissingRecipeTypeHolder;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.resources.ResourceLocation;

@LDLRegisterClient(name = "enderio_optional", group = ViScriptRecipe.MOD_ID,
        registry = UIScenario.REGISTRY, environment = RegistrationEnvironment.DEV_ONLY)
public final class EnderIoOptionalScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) { options.requiresWorld(false).tags("recipes", "optional"); }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.step("可选依赖缺失时保留未知类型", context -> {
            var type = ResourceLocation.parse("enderio:alloy_smelting");
            if (ViScriptRecipe.isModLoaded("enderio")) {
                context.check("已安装时注册 Ender IO 类型", RecipeEditorTypes.get(type).isPresent());
                return;
            }
            context.check("不注册 Ender IO 类型", RecipeEditorTypes.get(type).isEmpty());
            context.check("不显示 Ender IO 工作站", RecipeEditorTypes.availableCategories().stream().noneMatch(c -> c.ownerModId().equals("enderio")));
            context.check("旧条目使用未知类型占位", new RecipeEntry().setType(type).getData() instanceof MissingRecipeTypeHolder);
        });
    }
}
