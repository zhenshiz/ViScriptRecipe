package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialLaserDrillRarityData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.biome_whitelist")
    @ConfigList
    private List<ResourceLocation> biomeWhitelist = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.biome_blacklist")
    @ConfigList
    private List<ResourceLocation> biomeBlacklist = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.dimension_whitelist")
    @ConfigList
    private List<ResourceLocation> dimensionWhitelist = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.dimension_blacklist")
    @ConfigList
    private List<ResourceLocation> dimensionBlacklist = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.depth_min")
    private int depthMin = -64;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.depth_max")
    private int depthMax = 320;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.rarity.weight")
    private int weight = 1;
}
