package com.viscript_recipe.compat.lychee.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.configurator.ui.ArrayConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Dialog;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.compat.lychee.LycheeRecipeEditorTypes;
import com.viscript_recipe.compat.lychee.LycheeRecipeFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 通过表单编辑 Lychee 原生规则的草稿，提交时统一调用原生配方校验。
 *
 * <p>未触碰的字段保持原样，包括第三方规则和附加数据；取消编辑不改变画布。
 */
final class LycheeRuleEditor extends Dialog {
    private final JsonObject draft;
    private final UIElement body = RecipeEditorUi.column().layout(l -> l.widthPercent(100).gapAll(5));
    private final ScrollerView scroller = new ScrollerView();
    private final TextField search = RecipeEditorUi.textField("", this::filterRules);
    private final UIElement tools = RecipeEditorUi.row().layout(l -> l.widthPercent(100).gapAll(4));
    private final UIElement rootControls = RecipeEditorUi.column().layout(l -> l.widthPercent(100));
    private final Map<JsonObject, Boolean> collapseState = new IdentityHashMap<>();
    private final Map<String, String> searches = new LinkedHashMap<>();
    private RulesGroup activeRules;
    private String currentSection;
    private UIElement revealTarget;
    private int revealPasses;
    private static final Map<String, JsonObject> ACTIONS = presets(true);
    private static final Map<String, JsonObject> CONDITIONS = presets(false);

    LycheeRuleEditor(ResourceLocation recipeType, JsonObject original, String section, int ruleIndex, Consumer<String> onApply) {
        draft = original.deepCopy();
        setTitle(key("edit_rules"));
        setAutoClose(false);
        setId("lychee_rules_dialog");
        overlay.layout(l -> l.widthPercent(84).heightPercent(86));
        contentContainer.layout(l -> l.flex(1).minHeight(0));
        var sections = new ArrayList<>(List.of("targets", "conditions", "actions"));
        if (List.of("crafting", "anvil_crafting").contains(recipeType.getPath())) sections.add("assembling");
        addContent(RecipeEditorUi.selector(sections, section, value -> text(value), this::showSection).setId("lychee_rules_section"));
        search.setId("lychee_rules_search").layout(l -> l.widthAuto().flex(1).minWidth(0));
        search.style(s -> s.tooltips(text("search_rules_hint")));
        tools.addChildren(RecipeEditorUi.label(text("search_rules")).layout(l -> l.width(54).flexShrink(0)), search,
                RecipeEditorUi.textButton(text("expand_rules"), null, e -> expandRules(false)).setId("lychee_rules_expand").layout(l -> l.flexShrink(0)),
                RecipeEditorUi.textButton(text("collapse_rules"), null, e -> expandRules(true)).setId("lychee_rules_collapse").layout(l -> l.flexShrink(0)));
        addContent(tools);
        addContent(rootControls);
        scroller.layout(l -> l.widthPercent(100).flex(1).minHeight(0));
        scroller.addScrollViewChild(body);
        scroller.horizontalScroller.setDisplay(false);
        addContent(scroller);
        var error = RecipeEditorUi.label(Component.empty());
        error.setId("lychee_rules_error").layout(l -> l.widthPercent(100).height(18));
        addContent(error);
        addButton(RecipeEditorUi.textButton(Component.translatable("gui.cancel"), null, e -> close()).setId("lychee_rules_cancel"));
        addButton(RecipeEditorUi.textButton(Component.translatable("gui.done"), null, e -> {
            try {
                var recipe = LycheeRecipeFactory.compile(recipeType, draft.toString());
                var encoded = LycheeRecipeFactory.encode(recipe, Platform.getFrozenRegistry());
                close();
                onApply.accept(encoded);
            } catch (RuntimeException exception) {
                error.setText(text("invalid_rules", exception.getMessage()));
                error.style(s -> s.tooltips(Component.literal(String.valueOf(exception.getMessage()))));
            }
        }).setId("lychee_rules_apply"));
        showSection(section);
        if (activeRules != null && ruleIndex >= 0) activeRules.focusRule(ruleIndex);
    }

    private void showSection(String section) {
        currentSection = section;
        activeRules = null;
        revealTarget = null;
        body.clearAllChildren();
        rootControls.clearAllChildren();
        switch (section) {
            case "conditions" -> body.addChild(activeRules = rules(draft, "if", false, "lychee_if", false));
            case "actions" -> body.addChild(activeRules = rules(draft, "post", true, "lychee_post", false));
            case "assembling" -> body.addChild(activeRules = rules(draft, "assembling", true, "lychee_assembling", false));
            default -> {
                for (String field : List.of("block_in", "falling_block", "landing_block", "source_block", "target_block")) {
                    if (draft.has(field)) body.addChild(blockPredicate(draft, field, "lychee_" + field));
                }
                if (draft.has("entity")) entityPredicate();
                if (draft.has("pattern")) craftingPattern();
                if (body.getChildren().isEmpty()) body.addChild(RecipeEditorUi.label(text("no_targets")));
            }
        }
        tools.setDisplay(activeRules != null);
        rootControls.setDisplay(activeRules != null);
        if (activeRules != null) {
            activeRules.removeChild(activeRules.buttonGroup);
            rootControls.addChild(activeRules.buttonGroup);
        }
        search.setText(searches.getOrDefault(section, ""), false);
        filterRules(search.getValue());
        scroller.verticalScroller.setNormalizedValue(0);
    }

    private void filterRules(String query) {
        revealTarget = null;
        if (currentSection != null) searches.put(currentSection, query);
        if (activeRules != null) activeRules.filter(query);
        scroller.verticalScroller.setNormalizedValue(0);
    }

    private void reveal(UIElement target) {
        revealTarget = target;
        revealPasses = 3;
    }

    @Override
    public void screenTick() {
        super.screenTick();
        // 原生 scrollToChild 只接受直接子节点；这里把嵌套表单位置交给同一个滚动条，布局稳定后定位。
        if (revealTarget == null || --revealPasses > 0) return;
        float range = scroller.getContainerHeight() - scroller.viewPort.getContentHeight();
        if (range > 0) {
            float offset = revealTarget.getPositionY() - scroller.viewContainer.getPositionY() - 4;
            scroller.verticalScroller.setNormalizedValue(Math.clamp(offset / range, 0, 1));
        }
        revealTarget = null;
    }

    private void expandRules(boolean collapse) {
        if (activeRules != null) activeRules.setCollapse(false);
        if (activeRules != null) for (var item : activeRules.getConfigurators()) {
            if (item.isDisplayed() && item instanceof ArrayConfiguratorGroup<?>.ItemConfigurator row && row.inner instanceof RuleCard card)
                card.setCollapse(collapse);
        }
    }

    private RulesGroup rules(JsonObject owner, String field, boolean action, String id, boolean collapsed) {
        var values = list(owner.has(field) ? owner.get(field) : field.equals("if") ? owner.get("contextual") : null);
        return new RulesGroup(owner, field, action, id, collapsed, values, new AtomicInteger());
    }

    /** 仅适配原生列表的标题、过滤和复制；增删、选中、排序继续由 ArrayConfiguratorGroup 管理。 */
    private final class RulesGroup extends ArrayConfiguratorGroup<JsonObject> {
        private final String field;
        private final com.lowdragmc.lowdraglib2.gui.ui.elements.Label empty = RecipeEditorUi.label(text("no_matching_rules"));
        private ResourceLocation addType;
        private String query = "";

        RulesGroup(JsonObject owner, String field, boolean action, String id, boolean collapsed,
                   List<JsonObject> values, AtomicInteger index) {
            super(key(field), collapsed, () -> values,
                    (getter, setter) -> LycheeRuleEditor.this.new RuleCard(getter.get(), action,
                            id + "_" + index.getAndIncrement(), setter, collapsed || values.size() > 4), false);
            this.field = field;
            addType = ResourceLocation.withDefaultNamespace(action ? "drop_item" : "weather");
            setAddDefault(() -> preset(action, addType.getPath()));
            setOnUpdate(updated -> {
                values.clear();
                values.addAll(updated);
                var array = new JsonArray();
                values.forEach(array::add);
                owner.add(field, array);
                if (field.equals("if")) owner.remove("contextual");
                filter(query);
            });
            setId(id).layout(l -> l.widthPercent(100));
            var choices = (action ? ACTIONS : CONDITIONS).keySet().stream().map(ResourceLocation::parse).filter(type -> action
                    ? LycheeRegistries.POST_ACTION.containsKey(type) : LycheeRegistries.CONTEXTUAL.containsKey(type)).toList();
            var type = RecipeSearchComponents.catalog(null, () -> addType, value -> addType = value, () -> {},
                    choices, value -> ruleName(value.toString(), action), addType);
            type.setId(id + "_add_type").layout(l -> l.widthAuto().flex(1).minWidth(0));
            type.style(s -> s.tooltips(text("add_type_hint")));
            addButton.setId(id + "_add").layout(l -> l.width(42).height(18));
            addButton.setText(text("add_rule")).style(s -> s.tooltips(text("add_rule_hint")));
            removeButton.setId(id + "_remove").layout(l -> l.width(42).height(18));
            removeButton.setText(text("remove_rule")).style(s -> s.tooltips(text("remove_rule_hint")));
            var duplicate = RecipeEditorUi.textButton(text("duplicate_rule"), null, this::duplicate);
            duplicate.setId(id + "_duplicate").setActive(false);
            duplicate.style(s -> s.tooltips(text("duplicate_rule_hint")));
            setOnSelectedChanged(value -> duplicate.setActive(value != null));
            buttonGroup.layout(l -> l.widthPercent(100).gapAll(3));
            buttonGroup.addChildAt(type, 0).addChild(duplicate);
            removeChild(buttonGroup);
            addChildAt(buttonGroup, 1);
            configuratorContainer.addChild(empty);
            filter("");
        }

        @Override
        protected void onAdd(UIEvent event) {
            var before = List.copyOf(getConfigurators());
            super.onAdd(event);
            search.setText("", true);
            for (var item : getConfigurators()) if (!before.contains(item) && item instanceof ArrayConfiguratorGroup<?>.ItemConfigurator row) {
                setSelected((ItemConfigurator) row);
                if (row.inner instanceof RuleCard card) card.setCollapse(false);
                reveal(item);
            }
        }

        private void duplicate(UIEvent event) {
            if (selected == null) return;
            var copy = selected.object.deepCopy();
            var previous = addDefault;
            try { addDefault = () -> copy; onAdd(event); }
            finally { addDefault = previous; }
        }

        void filter(String text) {
            query = text == null ? "" : text.strip().toLowerCase(Locale.ROOT);
            int shown = 0;
            for (var item : getConfigurators()) if (item instanceof ArrayConfiguratorGroup<?>.ItemConfigurator row && row.inner instanceof RuleCard card) {
                boolean matches = query.isEmpty() || card.getLabel().getString().toLowerCase(Locale.ROOT).contains(query)
                        || ResourceLocation.parse(card.value.get("type").getAsString()).toString().toLowerCase(Locale.ROOT).contains(query)
                        || card.value.toString().toLowerCase(Locale.ROOT).contains(query);
                item.setDisplay(matches);
                if (matches) shown++;
                else if (item == selected) setSelected(null);
            }
            // 过滤只改变显示，原始顺序和隐藏条目始终保留；过滤期间不允许跨隐藏项拖动。
            setCanReorder(query.isEmpty());
            setLabel(LycheeRuleEditor.text("rule_list_count", LycheeRuleEditor.text(field), shown, getConfigurators().size()));
            empty.setDisplay(shown == 0);
        }

        void focusRule(int index) {
            if (index < 0 || index >= getConfigurators().size()) return;
            search.setText("", true);
            var item = (ItemConfigurator) getConfigurators().get(index);
            setSelected(item);
            ((RuleCard) item.inner).setCollapse(false);
            reveal(item);
        }
    }

    private final class RuleCard extends ConfiguratorGroup {
        private final JsonObject value;
        private final boolean action;
        private final String id;
        private final Consumer<JsonObject> changed;
        private boolean built;

        RuleCard(JsonObject value, boolean action, String id, Consumer<JsonObject> changed, boolean collapse) {
            super("", true);
            this.value = value;
            this.action = action;
            this.id = id;
            this.changed = changed;
            setId(id + "_card").layout(l -> l.widthPercent(100));
            lineContainer.setId(id + "_header");
            label.layout(l -> l.flex(1).minWidth(0));
            label.textStyle(s -> s.adaptiveWidth(false).textWrap(com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap.HOVER_ROLL));
            inlineContainer.setDisplay(false);
            setTips(text("rule_card_hint"));
            setCollapse(collapseState.getOrDefault(value, collapse));
        }

        @Override
        public ConfiguratorGroup setCollapse(boolean collapse) {
            if (value != null) {
                setLabel(ruleSummary(value, action));
                collapseState.put(value, collapse);
                if (!collapse && !built) { built = true; buildRule(this); }
            }
            return super.setCollapse(collapse);
        }
    }

    private ConfiguratorGroup rule(JsonObject value, boolean action, String id, Consumer<JsonObject> changed) {
        return new RuleCard(value, action, id, changed, false);
    }

    private void buildRule(RuleCard group) {
        var value = group.value;
        boolean action = group.action;
        String id = group.id;
        var content = group.configuratorContainer;
        content.layout(l -> l.gapAll(4));
        var fields = RecipeEditorUi.column().layout(l -> l.widthPercent(100).gapAll(4));
        var templates = action ? ACTIONS : CONDITIONS;
        var current = ResourceLocation.parse(value.get("type").getAsString()).toString();
        var choices = templates.keySet().stream().filter(type -> action
                ? LycheeRegistries.POST_ACTION.containsKey(ResourceLocation.parse(type))
                : LycheeRegistries.CONTEXTUAL.containsKey(ResourceLocation.parse(type))).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (!choices.contains(current)) choices.add(current);
        var typeChoices = choices.stream().map(ResourceLocation::parse).toList();
        content.addChild(RecipeSearchComponents.catalog(null, () -> ResourceLocation.parse(value.get("type").getAsString()), type -> {
            var replacement = templates.get(type.toString());
            if (replacement == null) return;
            value.entrySet().clear();
            replacement.deepCopy().entrySet().forEach(e -> value.add(e.getKey(), e.getValue()));
            fields.clearAllChildren();
            ruleFields(fields, value, action, id);
            group.setLabel(ruleSummary(value, action));
            group.changed.accept(value);
        }, () -> {}, typeChoices, type -> ruleName(type.toString(), action), ResourceLocation.parse(current)).setId(id + "_type"));
        content.addChild(fields);
        ruleFields(fields, value, action, id);
    }

    private static Component ruleName(String type, boolean action) {
        String canonical = ResourceLocation.parse(type).toString();
        return (action ? ACTIONS : CONDITIONS).containsKey(canonical)
                ? text((action ? "action." : "condition.") + ResourceLocation.parse(type).getPath()) : Component.literal(type);
    }

    private static Component ruleSummary(JsonObject value, boolean action) {
        String type = string(value, "type", "minecraft:custom");
        var name = ruleName(type, action).copy();
        String path = ResourceLocation.parse(type).getPath();
        if (List.of("drop_item", "set_item").contains(path) && value.has("id")) {
            var item = ResourceLocation.tryParse(value.get("id").getAsString());
            if (item != null) name.append(" · ").append(BuiltInRegistries.ITEM.get(item).getDescription())
                    .append(" ×" + (value.has("count") ? value.get("count").getAsInt() : 1));
        } else if (value.has("xp")) name.append(" · " + value.get("xp").getAsString() + " XP");
        else if (value.has("chance")) name.append(" · " + snownee.lychee.util.CommonProxy.chance(value.get("chance").getAsFloat()));
        else if (value.has("block")) {
            var block = value.get("block");
            if (block.isJsonObject()) block = block.getAsJsonObject().get("blocks");
            if (block != null && block.isJsonArray() && !block.getAsJsonArray().isEmpty()) block = block.getAsJsonArray().get(0);
            if (block != null && block.isJsonPrimitive()) {
                String target = block.getAsString();
                var id = ResourceLocation.tryParse(target);
                name.append(" · ").append(id != null && BuiltInRegistries.BLOCK.containsKey(id)
                        ? BuiltInRegistries.BLOCK.get(id).getName() : Component.literal(target));
            }
        }
        else for (String field : List.of("weather", "direction", "id", "command")) if (value.has(field) && value.get(field).isJsonPrimitive()) {
            String detail = value.get(field).getAsString();
            name.append(" · ").append(List.of("weather", "direction").contains(field) ? text("option." + detail) : Component.literal(detail));
            break;
        }
        if (action && value.has("if")) name.append(text("rule_conditions_suffix", count(value.get("if"))));
        int children = 0;
        for (String field : List.of("entries", "then", "else", "contextual")) children += count(value.get(field));
        if (children > 0) name.append(text("rule_children_suffix", children));
        return name;
    }

    private static int count(JsonElement value) { return value == null ? 0 : value.isJsonArray() ? value.getAsJsonArray().size() : 1; }

    private void ruleFields(UIElement ui, JsonObject value, boolean action, String id) {
        String type = ResourceLocation.parse(value.get("type").getAsString()).getPath();
        String canonical = ResourceLocation.parse(value.get("type").getAsString()).toString();
        if (!(action ? ACTIONS : CONDITIONS).containsKey(canonical)) {
            ui.addChild(RecipeEditorUi.label(text("preserved_rule")));
            return;
        }
        if (action) {
            switch (type) {
                case "drop_item", "set_item" -> {
                    ui.addChild(PropertiesView.createItemStackConfigurator(key("item"), () -> {
                        try { return LycheeCodecs.ITEM_STACK.parse(ops(), value).getOrThrow(); }
                        catch (RuntimeException ignored) { return ItemStack.EMPTY; }
                    }, stack -> {
                        for (var name : List.of("id", "count", "components")) value.remove(name);
                        if (stack.isEmpty()) { value.addProperty("id", "minecraft:air"); value.addProperty("count", 0); }
                        else LycheeCodecs.ITEM_STACK.encodeStart(ops(), stack).getOrThrow().getAsJsonObject().entrySet()
                                .forEach(e -> value.add(e.getKey(), e.getValue()));
                    }).setId(id + "_item"));
                    if (type.equals("set_item")) string(ui, value, "target", "default", id);
                }
                case "drop_xp" -> integer(ui, value, "xp", 1, 1, id);
                case "execute" -> { string(ui, value, "command", "", id); toggle(ui, value, "repeat", true, id); }
                case "place", "set_block", "cycle_state_property" -> {
                    ui.addChild(blockPredicate(value, "block", id + "_block"));
                    if (!type.equals("set_block")) offsets(ui, value, id);
                    if (type.equals("cycle_state_property")) {
                        string(ui, value, "property", "", id);
                        toggle(ui, value, "reversed", false, id);
                    }
                }
                case "damage_item" -> { integer(ui, value, "damage", 1, Integer.MIN_VALUE, id); string(ui, value, "target", "default", id); }
                case "anvil_damage_chance" -> decimal(ui, value, "chance", 0.12f, 0, 1, id);
                case "delay", "add_item_cooldown" -> {
                    decimal(ui, value, "s", 1, Float.MIN_NORMAL, Float.MAX_VALUE, id);
                    if (type.equals("add_item_cooldown")) {
                        var explicitItem = new Switch().setValue(value.has("item"), false);
                        var itemField = RecipeEditorUi.column().layout(l -> l.widthPercent(100));
                        item(itemField, value, "item", id);
                        itemField.setDisplay(value.has("item"));
                        explicitItem.registerValueListener(on -> {
                            if (on) value.addProperty("item", "minecraft:iron_ingot"); else value.remove("item");
                            itemField.setDisplay(on);
                        });
                        ui.addChild(RecipeEditorUi.fieldGroup(key("explicit_cooldown_item"), explicitItem));
                        ui.addChild(itemField);
                    }
                }
                case "explode" -> {
                    decimal(ui, value, "radius", 4, Float.MIN_NORMAL, Float.MAX_VALUE, id);
                    decimal(ui, value, "radius_step", 4, -Float.MAX_VALUE, Float.MAX_VALUE, id);
                    choice(ui, value, "block_interaction", List.of("keep", "destroy", "destroy_with_decay", "trigger_block"), "destroy", id);
                    toggle(ui, value, "fire", false, id);
                    offsets(ui, value, id);
                }
                case "move_towards_face" -> decimal(ui, value, "factor", 1, -Float.MAX_VALUE, Float.MAX_VALUE, id);
                case "move" -> {
                    var offset = value.has("offset") ? value.getAsJsonArray("offset").deepCopy() : JsonParser.parseString("[0,0,0]").getAsJsonArray();
                    var row = RecipeEditorUi.row().layout(l -> l.widthPercent(100).gapAll(4));
                    for (int i = 0; i < 3; i++) {
                        int axis = i;
                        row.addChild(RecipeEditorUi.fieldGroup(key("axis." + "xyz".charAt(i)), RecipeEditorUi.doubleField(offset.get(i).getAsDouble(),
                                -Double.MAX_VALUE, Double.MAX_VALUE, v -> { offset.set(axis, new JsonPrimitive(v)); value.add("offset", offset); }))
                                .layout(l -> l.flex(1)));
                    }
                    ui.addChild(row);
                    string(ui, value, "with", "", id);
                }
                case "random" -> {
                    integer(ui, value, "empty_weight", 0, 0, id);
                    bounds(ui, value, "rolls", true, id);
                    ui.addChild(rules(value, "entries", true, id + "_entries", false));
                }
                case "if" -> {
                    ui.addChild(rules(value, "then", true, id + "_then", false));
                    ui.addChild(rules(value, "else", true, id + "_else", false));
                }
                case "copy_component", "remove_component", "copy_durability" -> {
                    if (!type.equals("copy_durability")) {
                        var components = value.get("component");
                        if (components == null || components.isJsonPrimitive()) string(ui, value, "component", "*", id);
                        else ui.addChild(RecipeEditorUi.label(text("preserved_components")));
                    } else decimal(ui, value, "bonus", 0, -1, 999, id);
                    if (!type.equals("remove_component")) string(ui, value, "source", "/item_in/0", id);
                    string(ui, value, "target", "/item_out", id);
                }
                case "custom" -> {
                    string(ui, value, "id", "", id);
                    for (String flag : List.of("repeatable", "preventSync", "allowClientRun")) toggle(ui, value, flag, !flag.equals("preventSync"), id);
                    ui.addChild(RecipeEditorUi.label(text("preserved_data")));
                }
            }
            if (id.matches(".*_entries_\\d+$")) integer(ui, value, "weight", 1, 1, id);
            toggle(ui, value, "hide", false, id);
            ui.addChild(rules(value, "if", false, id + "_if", true));
        } else {
            switch (type) {
                case "weather" -> choice(ui, value, "weather", List.of("clear", "rain", "thunder"), "clear", id);
                case "chance" -> decimal(ui, value, "chance", 0.5f, Float.MIN_NORMAL, Math.nextDown(1f), id);
                case "difficulty" -> {
                    var selected = listValues(value.get("difficulty"));
                    for (String difficulty : List.of("peaceful", "easy", "normal", "hard")) {
                        var toggle = new Switch().setValue(selected.contains(difficulty), false);
                        toggle.registerValueListener(on -> {
                            if (on) { if (!selected.contains(difficulty)) selected.add(difficulty); } else selected.remove(difficulty);
                            var array = new JsonArray(); selected.forEach(array::add); value.add("difficulty", array);
                        });
                        ui.addChild(RecipeEditorUi.fieldGroup(key("option." + difficulty), toggle));
                    }
                }
                case "direction" -> choice(ui, value, "direction", List.of("down", "up", "north", "south", "west", "east", "sides", "forward", "axis"), "up", id);
                case "execute" -> { string(ui, value, "command", "", id); bounds(ui, value, "value", true, id); }
                case "fall_distance", "entity_health" -> bounds(ui, value, "range", false, id);
                case "sky_darken" -> { bounds(ui, value, "value", true, id); toggle(ui, value, "require_sky_light", false, id); }
                case "time" -> { bounds(ui, value, "value", true, id); integer(ui, value, "period", 24000, 1, id); }
                case "is_off_item_cooldown" -> item(ui, value, "item", id);
                case "and", "or" -> ui.addChild(rules(value, "contextual", false, id + "_children", false));
                case "not" -> {
                    var child = value.getAsJsonObject("contextual");
                    ui.addChild(rule(child, false, id + "_not", replacement -> value.add("contextual", replacement)));
                }
                case "location" -> {
                    offsets(ui, value, id);
                    // 位置谓词中的其余原生字段不被简化为某个方块或坐标，以免改变导入配方。
                    ui.addChild(RecipeEditorUi.label(text("preserved_location")));
                }
                case "custom", "param" -> {
                    for (String field : type.equals("custom") ? List.of("id") : List.of("key", "loot")) string(ui, value, field, "", id);
                    if (type.equals("param")) toggle(ui, value, "create", true, id);
                    else ui.addChild(RecipeEditorUi.label(text("preserved_data")));
                }
            }
        }
    }

    private ConfiguratorGroup blockPredicate(JsonObject owner, String field, String id) {
        JsonElement original = owner.has(field) ? owner.get(field) : new JsonPrimitive("*");
        var predicate = BlockPredicateExtensions.CODEC_FOR_TESTING.parse(ops(), original).getOrThrow();
        var canonical = original.isJsonObject() ? original.getAsJsonObject().deepCopy()
                : BlockPredicateExtensions.CODEC_FOR_TESTING.encodeStart(ops(), predicate).getOrThrow().getAsJsonObject();
        var group = new ConfiguratorGroup(key(field), false);
        group.setId(id).layout(l -> l.widthPercent(100));
        var fields = RecipeEditorUi.column().layout(l -> l.widthPercent(100).gapAll(3));
        var blockSet = canonical.get("blocks");
        String mode = BlockPredicateExtensions.isAny(predicate) ? "any" : blockSet != null && blockSet.isJsonPrimitive()
                && blockSet.getAsString().startsWith("#") ? "tag" : "single";
        Runnable write = () -> owner.add(field, canonical);
        Consumer<String> render = selected -> {
            fields.clearAllChildren();
            if (selected.equals("any")) return;
            if (selected.equals("tag")) {
                var tagSearch = RecipeSearchComponents.blockTag(null,
                        () -> ResourceLocation.tryParse(string(canonical, "blocks", "#minecraft:logs").replaceFirst("^#", "")),
                        tag -> { canonical.addProperty("blocks", "#" + tag); write.run(); }, () -> {});
                tagSearch.setId(id + "_tag");
                fields.addChild(RecipeEditorUi.fieldGroup(key("block_tag"), tagSearch));
            } else {
                Block[] chosen = {BlockPredicateExtensions.anyBlockState(BlockPredicateExtensions.CODEC_FOR_TESTING.parse(ops(), canonical).getOrThrow()).getBlock()};
                var blockSearch = RecipeSearchComponents.block(null,
                        () -> BuiltInRegistries.BLOCK.getKey(chosen[0]), block -> {
                    chosen[0] = BuiltInRegistries.BLOCK.get(block);
                    var blocks = new JsonArray(); blocks.add(block.toString());
                    canonical.add("blocks", blocks);
                    canonical.remove("state");
                    write.run();
                    renderBlockStates(fields, canonical, chosen[0], write, id);
                }, () -> {}, chosen[0]);
                blockSearch.setId(id + "_block");
                fields.addChild(RecipeEditorUi.fieldGroup(key("block"), blockSearch));
                renderBlockStates(fields, canonical, chosen[0], write, id);
            }
            if (canonical.has("nbt")) fields.addChild(RecipeEditorUi.label(text("preserved_nbt")));
        };
        group.configuratorContainer.addChild(RecipeEditorUi.selector(List.of("any", "single", "tag"), mode,
                selected -> text("match." + selected), selected -> {
                    if (selected.equals("any")) owner.addProperty(field, "*");
                    else {
                        if (selected.equals("tag")) canonical.addProperty("blocks", "#minecraft:logs");
                        else { var blocks = new JsonArray(); blocks.add("minecraft:stone"); canonical.add("blocks", blocks); }
                        canonical.remove("state"); write.run();
                    }
                    render.accept(selected);
                }).setId(id + "_mode"));
        group.configuratorContainer.addChild(fields);
        render.accept(mode);
        return group;
    }

    private void renderBlockStates(UIElement parent, JsonObject predicate, Block block, Runnable write, String id) {
        for (var old : new ArrayList<>(parent.getChildren())) if ((id + "_states").equals(old.getId())) parent.removeChild(old);
        var states = RecipeEditorUi.column().setId(id + "_states").layout(l -> l.widthPercent(100).gapAll(2));
        var state = predicate.has("state") ? predicate.getAsJsonObject("state") : new JsonObject();
        for (var property : block.getStateDefinition().getProperties()) {
            var choices = new ArrayList<>(List.of("*"));
            property.getPossibleValues().forEach(v -> choices.add(propertyName(property, v)));
            var selected = state.get(property.getName());
            var field = RecipeEditorUi.selector(choices, selected != null && selected.isJsonPrimitive() ? selected.getAsString() : "*",
                    Component::literal, v -> {
                        if (v.equals("*")) state.remove(property.getName()); else state.addProperty(property.getName(), v);
                        if (state.isEmpty()) predicate.remove("state"); else predicate.add("state", state);
                        write.run();
                    });
            states.addChild(RecipeEditorUi.column().addChildren(RecipeEditorUi.label(Component.literal(property.getName())), field));
        }
        parent.addChild(states);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyName(net.minecraft.world.level.block.state.properties.Property property, Comparable value) {
        return property.getName(value);
    }

    private void entityPredicate() {
        var entity = draft.getAsJsonObject("entity");
        var fields = RecipeEditorUi.column().layout(l -> l.widthPercent(100).gapAll(4));
        var type = entity.get("type");
        var originalType = type == null ? null : type.deepCopy();
        boolean complex = type != null && !type.isJsonPrimitive();
        String mode = type == null ? "any" : complex ? "preserved" : type.getAsString().startsWith("#") ? "tag" : "single";
        var modes = new ArrayList<>(List.of("any", "single", "tag"));
        if (complex) modes.add("preserved");
        Consumer<String> render = selected -> {
            fields.clearAllChildren();
            if (selected.equals("single")) fields.addChild(RecipeEditorUi.fieldGroup(key("entity_type"),
                    RecipeSearchComponents.entityType(null, () -> ResourceLocation.tryParse(string(entity, "type", "minecraft:pig")),
                            value -> entity.addProperty("type", value.toString()), () -> {}, EntityType.PIG).setId("lychee_entity_type")));
            else if (selected.equals("tag")) fields.addChild(RecipeEditorUi.fieldGroup(key("entity_tag"),
                    RecipeSearchComponents.entityTag(null, () -> ResourceLocation.tryParse(string(entity, "type", "#minecraft:undead").replaceFirst("^#", "")),
                            value -> entity.addProperty("type", "#" + value), () -> {}).setId("lychee_entity_tag")));
        };
        body.addChild(RecipeEditorUi.selector(modes, mode, selected -> text("entity_match." + selected), selected -> {
            if (selected.equals("any")) entity.remove("type");
            else if (selected.equals("single")) entity.addProperty("type", "minecraft:pig");
            else if (selected.equals("tag")) entity.addProperty("type", "#minecraft:undead");
            else if (selected.equals("preserved")) entity.add("type", originalType.deepCopy());
            render.accept(selected);
        }).setId("lychee_entity_mode"));
        body.addChild(fields);
        render.accept(mode);
        body.addChild(RecipeEditorUi.label(text("preserved_entity")));
    }

    private void craftingPattern() {
        var pattern = draft.getAsJsonArray("pattern");
        var keys = draft.getAsJsonObject("key");
        String[][] cells = new String[3][3];
        var choices = new ArrayList<>(List.of(" ", "A", "B", "C", "D", "E", "F", "G", "H", "I"));
        keys.keySet().forEach(k -> { if (!choices.contains(k)) choices.add(k); });
        Runnable write = () -> {
            var rows = new JsonArray();
            var used = new java.util.HashSet<String>();
            for (var row : cells) { rows.add(String.join("", row)); for (var symbol : row) if (!symbol.equals(" ")) used.add(symbol); }
            keys.keySet().removeIf(k -> !used.contains(k));
            for (var symbol : used) if (!keys.has(symbol)) keys.add(symbol, JsonParser.parseString("{\"item\":\"minecraft:iron_ingot\"}"));
            draft.add("pattern", rows);
        };
        body.addChild(RecipeEditorUi.label(text("pattern_hint")));
        for (int y = 0; y < 3; y++) {
            var row = RecipeEditorUi.row().layout(l -> l.gapAll(4));
            for (int x = 0; x < 3; x++) {
                int column = x, line = y;
                String original = y < pattern.size() ? pattern.get(y).getAsString() : "";
                cells[y][x] = x < original.length() ? original.substring(x, x + 1) : " ";
                row.addChild(RecipeEditorUi.selector(choices, cells[y][x], Component::literal,
                        symbol -> { cells[line][column] = symbol; write.run(); }).setId("lychee_pattern_" + y + "_" + x).layout(l -> l.width(48)));
            }
            body.addChild(row);
        }
    }

    private static void string(UIElement ui, JsonObject value, String field, String fallback, String id) {
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), RecipeEditorUi.textField(string(value, field, fallback),
                v -> value.addProperty(field, v)).setId(id + "_" + field)));
    }
    private static void integer(UIElement ui, JsonObject value, String field, int fallback, int min, String id) {
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), RecipeEditorUi.intField(value.has(field) ? value.get(field).getAsInt() : fallback,
                min, Integer.MAX_VALUE, v -> value.addProperty(field, v)).setId(id + "_" + field)));
    }
    private static void decimal(UIElement ui, JsonObject value, String field, float fallback, float min, float max, String id) {
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), RecipeEditorUi.floatField(value.has(field) ? value.get(field).getAsFloat() : fallback,
                min, max, v -> value.addProperty(field, v)).setId(id + "_" + field)));
    }
    private static void toggle(UIElement ui, JsonObject value, String field, boolean fallback, String id) {
        var toggle = new Switch().setValue(value.has(field) ? value.get(field).getAsBoolean() : fallback, false);
        toggle.registerValueListener(v -> value.addProperty(field, v));
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), toggle.setId(id + "_" + field)));
    }
    private static void choice(UIElement ui, JsonObject value, String field, List<String> choices, String fallback, String id) {
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), RecipeEditorUi.selector(choices, string(value, field, fallback),
                selected -> text("option." + selected), v -> value.addProperty(field, v)).setId(id + "_" + field)));
    }
    private static void item(UIElement ui, JsonObject value, String field, String id) {
        ui.addChild(PropertiesView.removeCountConfig(PropertiesView.createItemStackConfigurator(key(field), () -> {
            var name = ResourceLocation.tryParse(string(value, field, "minecraft:iron_ingot"));
            return name != null && BuiltInRegistries.ITEM.containsKey(name) ? new ItemStack(BuiltInRegistries.ITEM.get(name)) : Items.IRON_INGOT.getDefaultInstance();
        }, stack -> { if (!stack.isEmpty()) value.addProperty(field, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()); })).setId(id + "_" + field));
    }
    private static void offsets(UIElement ui, JsonObject value, String id) {
        for (String field : List.of("offsetX", "offsetY", "offsetZ")) integer(ui, value, field, 0, Integer.MIN_VALUE, id);
    }
    private static void bounds(UIElement ui, JsonObject value, String field, boolean integer, String id) {
        JsonElement original = value.get(field);
        var range = original != null && original.isJsonObject() ? original.getAsJsonObject().deepCopy() : new JsonObject();
        if (original == null && (field.equals("rolls") || field.equals("value") && string(value, "type", "").endsWith(":execute"))) {
            range.addProperty("min", 1);
            if (field.equals("rolls")) range.addProperty("max", 1);
        }
        if (original != null && original.isJsonPrimitive()) { range.add("min", original); range.add("max", original); }
        var row = RecipeEditorUi.row().layout(l -> l.widthPercent(100).gapAll(4));
        for (String bound : List.of("min", "max")) {
            var input = RecipeEditorUi.textField(range.has(bound) ? range.get(bound).getAsString() : "", v -> {
                if (v.isBlank()) range.remove(bound);
                else try { range.addProperty(bound, integer ? Integer.valueOf(v) : Double.valueOf(v)); }
                catch (NumberFormatException ignored) { return; }
                value.add(field, range);
            }).setId(id + "_" + field + "_" + bound);
            row.addChild(RecipeEditorUi.fieldGroup(key(bound), input).layout(l -> l.flex(1)));
        }
        ui.addChild(RecipeEditorUi.fieldGroup(key(field), row));
    }
    private static String string(JsonObject object, String field, String fallback) {
        return object.has(field) && object.get(field).isJsonPrimitive() ? object.get(field).getAsString() : fallback;
    }
    private static List<JsonObject> list(JsonElement element) {
        var values = new ArrayList<JsonObject>();
        if (element == null) return values;
        if (element.isJsonArray()) element.getAsJsonArray().forEach(v -> values.add(v.getAsJsonObject()));
        else values.add(element.getAsJsonObject());
        return values;
    }
    private static List<String> listValues(JsonElement element) {
        var values = new ArrayList<String>();
        if (element == null) return values;
        if (element.isJsonArray()) element.getAsJsonArray().forEach(v -> values.add(v.getAsString()));
        else values.add(element.getAsString());
        return values;
    }
    private static RegistryOps<JsonElement> ops() { return RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()); }
    private static String key(String field) { return LycheeRecipeEditorTypes.key(field); }
    private static Component text(String field, Object... args) { return Component.translatable(key(field), args); }
    private static JsonObject preset(boolean action, String type) { return (action ? ACTIONS : CONDITIONS).get("minecraft:" + type).deepCopy(); }

    private static Map<String, JsonObject> presets(boolean action) {
        // 仅提供原生规则的初始表单值；解析、执行及合法性均由 Lychee 自身负责。
        String definitions = action ? """
                {"drop_item":{"id":"minecraft:diamond","count":1},"drop_xp":{"xp":1},
                 "execute":{"command":"say Lychee","repeat":true},"place":{"block":"minecraft:stone"},
                 "damage_item":{"damage":1},"prevent_default":{},"anvil_damage_chance":{"chance":0.12},
                 "random":{"entries":[{"type":"drop_item","id":"minecraft:diamond","count":1,"weight":1}]},
                 "explode":{},"delay":{"s":1},"exit":{},"add_item_cooldown":{"s":1},
                 "cycle_state_property":{"block":"minecraft:oak_log","property":"axis"},
                 "move_towards_face":{},"move":{"offset":[0,1,0]},"set_item":{"id":"minecraft:diamond","count":1},
                 "custom":{"id":"custom_action"},"if":{"if":{"type":"weather","weather":"clear"},"then":[{"type":"drop_item","id":"minecraft:diamond","count":1}]},"set_block":{"block":"minecraft:stone"},
                 "copy_component":{"component":"*"},"remove_component":{"component":"*"},"copy_durability":{}}
                """ : """
                {"weather":{"weather":"clear"},"chance":{"chance":0.5},"difficulty":{"difficulty":"normal"},
                 "direction":{"direction":"up"},"is_sneaking":{},"execute":{"command":"time query daytime"},
                 "fall_distance":{"range":{"min":0}},"entity_health":{"range":{"min":1}},
                 "sky_darken":{"value":{"min":0,"max":15}},"time":{"value":{"min":0,"max":12000},"period":24000},
                 "is_off_item_cooldown":{"item":"minecraft:ender_pearl"},"and":{"contextual":[]},"or":{"contextual":[]},
                 "not":{"contextual":{"type":"is_sneaking"}},"location":{},"custom":{"id":"custom_condition"},
                 "param":{"key":"minecraft:random"}}
                """;
        var result = new LinkedHashMap<String, JsonObject>();
        JsonParser.parseString(definitions).getAsJsonObject().entrySet().forEach(e -> {
            var value = e.getValue().getAsJsonObject();
            value.addProperty("type", "minecraft:" + e.getKey());
            result.put("minecraft:" + e.getKey(), value);
        });
        return result;
    }
}
