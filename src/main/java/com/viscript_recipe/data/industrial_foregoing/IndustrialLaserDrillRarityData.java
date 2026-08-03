package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialLaserDrillRarityData implements IPersistedSerializable {
    @Persisted
    private List<ResourceLocation> biomeWhitelist = new ArrayList<>();
    @Persisted
    private List<ResourceLocation> biomeBlacklist = new ArrayList<>();
    @Persisted
    private List<ResourceLocation> dimensionWhitelist = new ArrayList<>();
    @Persisted
    private List<ResourceLocation> dimensionBlacklist = new ArrayList<>();
    @Persisted
    private int depthMin = -64;
    @Persisted
    private int depthMax = 320;
    @Persisted
    private int weight = 1;
}
