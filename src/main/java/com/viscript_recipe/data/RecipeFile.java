package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.ViScriptRecipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeFile implements IPersistedSerializable, IConfigurable {
    public static final int CURRENT_FORMAT_VERSION = 1;

    @Configurable(name = "viscript_recipe.config.file.format_version")
    @ConfigNumber(range = {1, 1}, type = ConfigNumber.Type.INTEGER)
    private int formatVersion = CURRENT_FORMAT_VERSION;

    @Configurable(name = "viscript_recipe.config.file.pack_id")
    private String packId = ViScriptRecipe.MOD_ID + ":main";

    @Configurable(name = "viscript_recipe.config.file.entries")
    @ConfigList(addDefaultMethod = "createDefaultEntry")
    private List<RecipeEntry> entries = new ArrayList<>();

    public RecipeEntry createDefaultEntry() {
        return new RecipeEntry();
    }
}
