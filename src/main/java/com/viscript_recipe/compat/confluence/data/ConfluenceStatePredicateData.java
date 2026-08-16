package com.viscript_recipe.compat.confluence.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceStatePredicateData implements IPersistedSerializable, IConfigurable {
    @Persisted
    private List<ConfluenceStatePropertyData> properties = new ArrayList<>();

    public String stateText() {
        return properties.isEmpty() ? "lit=true" : properties.getFirst().getName() + "=" + properties.getFirst().getValue();
    }

    public static ConfluenceStatePredicateData parseState(String value) {
        var p = new ConfluenceStatePropertyData();
        var split = value.isBlank() ? new String[]{"lit", "true"} : value.split("=", 2);
        p.setName(split[0].trim());
        p.setValue(split.length > 1 ? split[1].trim() : "true");
        return new ConfluenceStatePredicateData().setProperties(new ArrayList<>(List.of(p)));
    }

    public static List<ConfluenceStatePredicateData> parseStatePredicates(String value) {
        var predicates = new ArrayList<ConfluenceStatePredicateData>();
        if (value == null || value.isBlank()) return predicates;
        for (var rawPredicate : value.split(";")) {
            var properties = new ArrayList<ConfluenceStatePropertyData>();
            for (var rawProperty : rawPredicate.split(",")) {
                var text = rawProperty.trim();
                if (text.isBlank()) continue;
                var split = text.split("=", 2);
                var property = new ConfluenceStatePropertyData().setName(split[0].trim());
                var propertyValue = split.length > 1 ? split[1].trim() : "true";
                var range = propertyValue.split("\\.\\.", 2);
                if (range.length == 2) {
                    property.setRanged(true).setMin(range[0].trim()).setMax(range[1].trim());
                } else {
                    property.setValue(propertyValue);
                }
                if (!property.getName().isBlank()) properties.add(property);
            }
            if (!properties.isEmpty()) predicates.add(new ConfluenceStatePredicateData().setProperties(properties));
        }
        return predicates;
    }
}
