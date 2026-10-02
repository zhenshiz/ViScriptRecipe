package com.viscript_recipe.compat.ae2.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.ArrayList;
import java.util.List;

/** 保留原生状态约束的多值匹配和范围匹配语义。 */
@Getter
@Setter
@Accessors(chain = true)
public class Ae2StatePropertyData implements ISkipDefaultedSerialize {
    public enum Mode { SINGLE, MULTIPLE, RANGE }
    @Persisted private String name = "";
    @Persisted private Mode mode = Mode.SINGLE;
    @Persisted private String value = "";
    @Persisted private List<String> values = new ArrayList<>();
    @Persisted private String min = "";
    @Persisted private String max = "";
}
