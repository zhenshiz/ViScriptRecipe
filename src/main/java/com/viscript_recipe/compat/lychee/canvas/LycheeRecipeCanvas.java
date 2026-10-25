package com.viscript_recipe.compat.lychee.canvas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.compat.lychee.LycheeRecipeEditorTypes;
import com.viscript_recipe.compat.lychee.LycheeRecipeFactory;
import com.viscript_recipe.compat.lychee.data.LycheeRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.VanillaEventCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.SlotSelection;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexWrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.action.DropItem;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** 保留原生 JSON，仅将用户修改过的可视槽位写回对应原料或掉落动作。 */
public class LycheeRecipeCanvas extends RecipeCanvas<LycheeRecipeData> {
    private JsonObject root;
    private final List<Input> inputs = new ArrayList<>();
    private final List<Output> outputs = new ArrayList<>();
    private final List<CompoundTag> originalInputs = new ArrayList<>();
    private final List<ItemStack> originalOutputs = new ArrayList<>();
    private final List<List<IngredientDisplaySlot>> inputViews = new ArrayList<>();
    private final List<Integer> unplacedInputs = new ArrayList<>();
    private final java.util.Map<Integer, Integer> outputActions = new java.util.HashMap<>();
    private boolean updatingInputs;
    private LycheeRecipeScene scene;
    private final Label anvilCost = RecipeEditorUi.label(Component.empty());
    private final Label previewStatus = RecipeEditorUi.label(Component.empty());

    public LycheeRecipeCanvas(NavigationView navigationView, RecipeEntry entry) { super(navigationView, entry); }

    @Override
    public UIElement createCanvas() {
        setId("lychee_canvas");
        root = JsonParser.parseString(getData().getRecipeJson()).getAsJsonObject();
        inputs.clear();
        outputs.clear();
        inputViews.clear();
        unplacedInputs.clear();
        outputActions.clear();
        scene = null;
        collectInputs();
        for (String key : List.of("item_out", "result")) {
            if (root.has(key)) outputs.add(new Output(root.get(key), value -> root.add(key, value)));
        }
        if (root.has("post")) collectDrops(root.get("post"));

        var content = RecipeEditorUi.column().layout(l -> l.alignItems(AlignItems.CENTER).gapAll(8).paddingAll(8));
        var recipe = previewRecipe();
        String type = entry.getType().getPath();
        if (type.equals("crafting")) content.addChild(craftingLayout(recipe));
        else if (type.equals("anvil_crafting")) content.addChild(anvilLayout());
        else if (recipe != null) {
            var actions = recipe.value().postActions();
            scene = new LycheeRecipeScene(type, recipe, inputs.size(), actions.size(), this::inputSlot,
                    index -> actionSlot(actions, index), this::showRuleEditor);
            content.addChild(scene);
            var overflow = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(340).flexWrap(FlexWrap.WRAP));
            for (int i = 9; i < Math.min(inputs.size(), MAX_INGREDIENT); i++) overflow.addChild(inputSlot(i));
            var visibleActions = java.util.stream.IntStream.range(0, actions.size()).filter(i -> !actions.get(i).hidden()).boxed().toList();
            for (int i = 9; i < visibleActions.size(); i++) overflow.addChild(actionSlot(actions, visibleActions.get(i)));
            if (!overflow.getChildren().isEmpty()) {
                var group = new com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup("", true);
                group.setLabel(Component.translatable(key("overflow_slots"), overflow.getChildren().size()));
                group.layout(l -> l.width(340).maxWidth(340));
                group.configuratorContainer.addChild(overflow);
                content.addChild(group);
            }
        } else content.addChild(fallbackSlots());
        if (recipe != null && List.of("crafting", "anvil_crafting").contains(type) && !recipe.value().postActions().isEmpty()) {
            var actions = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(340).flexWrap(FlexWrap.WRAP));
            for (int i = 0; i < recipe.value().postActions().size(); i++) {
                if (!recipe.value().postActions().get(i).hidden()) actions.addChild(actionSlot(recipe.value().postActions(), i));
            }
            content.addChild(actions);
        }
        var additional = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(340).flexWrap(FlexWrap.WRAP));
        for (int i = 0; i < Math.min(inputs.size(), MAX_INGREDIENT); i++) if (visualIngredientSlots[i] == null) {
            unplacedInputs.add(i);
            additional.addChild(inputSlot(i));
        }
        for (int i = 0; i < Math.min(outputs.size(), MAX_OUTPUT); i++) if (visualOutputSlots[i] == null) additional.addChild(outputSlot(i));
        // 被原生隐藏规则或自定义布局替换的槽位仍参与无损保存，但不另起一行重复显示。
        if (!additional.getChildren().isEmpty()) content.addChild(additional.setDisplay(false));
        if (recipe != null && recipe.value() instanceof EntityTickingRecipe entityRecipe) {
            var entityType = entityRecipe.predicate().entityType().stream().flatMap(set -> set.types().stream())
                    .map(net.minecraft.core.Holder::value).findFirst();
            var egg = entityType.map(SpawnEggItem::byId).orElse(null);
            content.addChild(RecipeEditorUi.textButton(entityType.map(net.minecraft.world.entity.EntityType::getDescription)
                            .orElse(Component.translatable(key("entity"))),
                    new ItemStackTexture(egg == null ? Items.NAME_TAG : egg), e -> showRuleEditor("targets"))
                    .setId("lychee_entity_target"));
        }
        var shortcuts = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(340).flexWrap(FlexWrap.WRAP).justifyContent(AlignContent.CENTER));
        shortcuts.addChildren(
                RecipeEditorUi.textButton(Component.translatable(key("edit_targets")), null, e -> showRuleEditor("targets")).setId("lychee_edit_rules"),
                RecipeEditorUi.textButton(Component.translatable(key("conditions_count"), count(root.has("if") ? root.get("if") : root.get("contextual"))),
                        null, e -> showRuleEditor("conditions")).setId("lychee_edit_conditions"),
                RecipeEditorUi.textButton(Component.translatable(key("actions_count"), count(root.get("post"))),
                        null, e -> showRuleEditor("actions")).setId("lychee_edit_actions"));
        if (List.of("crafting", "anvil_crafting").contains(type)) shortcuts.addChild(
                RecipeEditorUi.textButton(Component.translatable(key("assembling")), null,
                        e -> showRuleEditor("assembling")).setId("lychee_edit_assembling"));
        content.addChildren(shortcuts, RecipeEditorUi.label(Component.translatable(key("canvas_hint"))).layout(l -> l.width(310).height(14)),
                previewStatus.setId("lychee_preview_status").layout(l -> l.width(310).height(14)));
        return content;
    }

    private UIElement inputSlot(int index) {
        while (inputViews.size() <= index) inputViews.add(new ArrayList<>());
        var views = inputViews.get(index);
        var slot = createIngredientSlot(index, LycheeRecipeScene.SLOT);
        LycheeRecipeScene.skin(slot, SlotType.NORMAL);
        slot.setId(views.isEmpty() ? "lychee_input_" + index : "lychee_input_" + index + "_copy_" + views.size());
        views.add(slot);
        return slot;
    }

    private ItemSlot outputSlot(int index) {
        return outputSlot(index, null);
    }

    private ItemSlot outputSlot(int index, PostAction action) {
        ItemSlot slot;
        if (action != null && action.commonProperties().icon() != null) {
            var icon = new snownee.lychee.ui.SpriteElementRenderer(action.commonProperties().icon()).withSize(16);
            slot = new ItemSlot() {
                @Override
                protected void drawItemStack(com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext context, ItemStack stack) {
                    // ItemSlot 为原生物品渲染预留了 -200 的深度；精灵也要补回相同的物品绘制层。
                    var graphics = context.graphics;
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 232);
                    icon.render(graphics);
                    graphics.flush();
                    graphics.pose().popPose();
                }
            };
            slot = createOutputSlot(index, LycheeRecipeScene.SLOT, slot);
        } else slot = createOutputSlot(index, LycheeRecipeScene.SLOT);
        LycheeRecipeScene.skin(slot, SlotType.NORMAL);
        slot.setId("lychee_output_" + index);
        return slot;
    }

    private UIElement actionSlot(List<PostAction> actions, int index) {
        var action = actions.get(index);
        if (action instanceof DropItem) {
            int output = (root.has("item_out") ? 1 : 0) + (root.has("result") ? 1 : 0);
            for (int i = 0; i < index; i++) if (actions.get(i) instanceof DropItem) output++;
            if (output < MAX_OUTPUT) {
                outputActions.put(output, index);
                var slot = outputSlot(output, action);
                LycheeRecipeScene.skin(slot, action.conditions().showingCount() > 0 ? SlotType.CHANCE : SlotType.NORMAL);
                var tips = new ArrayList<Component>();
                ActionRenderer.appendConditionTooltips(tips, action, net.minecraft.client.Minecraft.getInstance().player);
                slot.style(s -> s.tooltips(tips.toArray(Component[]::new)));
                return slot;
            }
        }
        var candidates = new ArrayList<PostAction>();
        collectActionIcons(action, candidates);
        var sprites = new java.util.HashMap<ResourceLocation, RenderElement>();
        java.util.function.Supplier<PostAction> displayed = () -> snownee.lychee.util.CommonProxy.getCycledItem(candidates, action, 1000);
        var icon = InteractiveRenderElement.create(RenderElement.create((graphics, element) -> {
            var value = displayed.get();
            var sprite = value.commonProperties().icon();
            if (sprite == null) ActionRenderer.of(value).internalRender(value, graphics, 1, 1);
            else sprites.computeIfAbsent(sprite, id -> new snownee.lychee.ui.SpriteElementRenderer(id).withSize(16).at(1, 1)).render(graphics);
        }));
        icon.withSize(18);
        icon.onTooltip(() -> action instanceof snownee.lychee.action.RandomSelect random
                ? ActionRenderer.getTooltipsFromRandom(random, displayed.get(), net.minecraft.client.Minecraft.getInstance().player)
                : ActionRenderer.of(displayed.get()).getTooltips(displayed.get(), net.minecraft.client.Minecraft.getInstance().player));
        return LycheeRecipeScene.element(icon, () -> showRuleEditor("actions", index))
                .setId("lychee_action_" + index).layout(l -> l.width(LycheeRecipeScene.SLOT).height(LycheeRecipeScene.SLOT))
                .style(s -> s.backgroundTexture(LycheeRecipeScene.slotTexture(
                        action.conditions().showingCount() > 0 ? SlotType.CHANCE : SlotType.NORMAL)));
    }

    private static void collectActionIcons(PostAction action, List<PostAction> icons) {
        if (action.hidden()) return;
        if (action instanceof CompoundAction compound) compound.getChildActions().forEach(child -> collectActionIcons(child, icons));
        else icons.add(action);
    }

    private UIElement craftingLayout(RecipeHolder<ILycheeRecipe<LycheeContext>> recipe) {
        var grid = new UIElement().setId("lychee_crafting_grid").layout(l -> l.width(116).height(54).flexShrink(0));
        var slotLocation = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/slot.png");
        var slotTexture = com.viscript_recipe.ViScriptRecipe.isPresentResource(slotLocation)
                ? SpriteTexture.of(slotLocation).setSprite(0, 0, 18, 18)
                : SpriteTexture.of(ResourceLocation.withDefaultNamespace("textures/gui/container/anvil.png")).setSprite(26, 46, 18, 18);
        var symbols = new ArrayList<>(root.getAsJsonObject("key").keySet());
        var pattern = root.getAsJsonArray("pattern");
        int minX = 3, minY = 3, maxX = -1, maxY = -1;
        for (int y = 0; y < pattern.size(); y++) {
            String line = pattern.get(y).getAsString();
            for (int x = 0; x < line.length(); x++) if (line.charAt(x) != ' ') {
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            }
        }
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        // 与 JEI CraftingGridHelper 相同的居中方式，重复符号继续绑定同一个原料。
        int offsetX = width == 1 ? 1 : 0;
        int offsetY = height == 1 || width > 2 && height == 2 ? 1 : 0;
        for (int y = 0; y < 3; y++) {
            int patternY = y - offsetY;
            String line = patternY >= 0 && patternY < height ? pattern.get(patternY + minY).getAsString() : "";
            for (int x = 0; x < 3; x++) {
                int patternX = x - offsetX;
                int rawX = patternX + minX;
                int index = patternX >= 0 && patternX < width && rawX < line.length()
                        ? symbols.indexOf(line.substring(rawX, rawX + 1)) : -1;
                var slot = index < 0 ? new UIElement().addEventListener(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.MOUSE_DOWN,
                        e -> { if (e.button == 0) { showRuleEditor("targets"); e.stopPropagation(); } })
                        .setId("lychee_empty_" + y + "_" + x).layout(l -> l.width(LycheeRecipeScene.SLOT).height(LycheeRecipeScene.SLOT))
                        .style(s -> s.tooltips(Component.translatable(key("edit_pattern")))) : inputSlot(index);
                slot.style(s -> s.backgroundTexture(slotTexture));
                grid.addChild(LycheeRecipeScene.at(slot, x * 18, y * 18, 18, 18));
            }
        }
        var output = outputSlot(0);
        var outputTexture = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/output_slot.png");
        if (com.viscript_recipe.ViScriptRecipe.isPresentResource(outputTexture)) {
            output.layout(l -> l.paddingAll(5));
            output.style(s -> s.backgroundTexture(SpriteTexture.of(outputTexture).setSprite(0, 0, 26, 26)));
            grid.addChild(LycheeRecipeScene.at(output, 90, 14, 26, 26));
        } else grid.addChild(LycheeRecipeScene.at(output, 94, 18, 18, 18));
        grid.addChild(LycheeRecipeScene.at(arrow(), 61, 19, 22, 16));
        if (recipe != null && snownee.lychee.compat.recipeviewer.category.RvCategory.needInfo(recipe.value())) {
            grid.addChild(LycheeRecipeScene.at(LycheeRecipeScene.element(
                    snownee.lychee.compat.recipeviewer.category.RvCategory.infoIcon(recipe), () -> showRuleEditor("conditions")), 67, 11, 8, 8));
        }
        return grid;
    }

    private UIElement anvilLayout() {
        var left = (ItemSlot) inputSlot(0);
        var right = inputs.size() > 1 ? inputSlot(1) : new UIElement();
        var output = outputSlot(0);
        configureJeiOverlaySlotVisual(left, output);
        if (right instanceof ItemSlot slot) configureJeiOverlaySlotVisual(slot);
        left.layout(l -> l.paddingAll(0));
        right.layout(l -> l.paddingAll(0));
        output.layout(l -> l.paddingAll(0));
        updateAnvilCost();
        anvilCost.setId("lychee_anvil_cost");
        return VanillaEventCanvasFactory.anvil(left, right, output, anvilCost);
    }

    private UIElement fallbackSlots() {
        var row = RecipeEditorUi.row().layout(l -> l.gapAll(4).maxWidth(340).flexWrap(FlexWrap.WRAP).alignItems(AlignItems.CENTER));
        for (int i = 0; i < Math.min(inputs.size(), MAX_INGREDIENT); i++) row.addChild(inputSlot(i));
        row.addChild(arrow());
        for (int i = 0; i < Math.min(outputs.size(), MAX_OUTPUT); i++) row.addChild(outputSlot(i));
        return row;
    }

    private static UIElement arrow() {
        var texture = ResourceLocation.fromNamespaceAndPath("jei", "textures/jei/atlas/gui/recipe_arrow.png");
        return new UIElement().layout(l -> l.width(22).height(16)).style(s -> s.backgroundTexture(
                com.viscript_recipe.ViScriptRecipe.isPresentResource(texture) ? SpriteTexture.of(texture).setSprite(0, 0, 22, 16)
                        : SpriteTexture.of(ResourceLocation.withDefaultNamespace("textures/gui/container/anvil.png")).setSprite(102, 47, 22, 16)));
    }

    private void updateAnvilCost() {
        if (!entry.getType().getPath().equals("anvil_crafting")) return;
        int cost = root.has("level_cost") ? root.get("level_cost").getAsInt() : 1;
        anvilCost.setText(Component.translatable("container.repair.cost", cost));
        var player = net.minecraft.client.Minecraft.getInstance().player;
        boolean enough = player == null || player.isCreative() || cost < 40 && cost <= player.experienceLevel;
        anvilCost.textStyle(s -> s.textColor(enough ? 0xff80ff20 : 0xffff6060));
        updateAnvilMaterialDisplay();
    }

    private void updateAnvilMaterialDisplay() {
        if (root == null || !entry.getType().getPath().equals("anvil_crafting") || visualIngredientSlots[1] == null) return;
        var slot = visualIngredientSlots[1];
        int cost = root.has("material_cost") ? root.get("material_cost").getAsInt() : 1;
        // 数量属于 material_cost；只更新图标，不改写原生 Ingredient 或其复杂匹配条件。
        var display = slot.getValue().isEmpty() ? slot.getIngredient().toStack() : slot.getValue();
        slot.setItem(display.copyWithCount(cost), false);
    }

    @SuppressWarnings("unchecked")
    private RecipeHolder<ILycheeRecipe<LycheeContext>> previewRecipe() {
        try {
            var recipe = (ILycheeRecipe<LycheeContext>) LycheeRecipeFactory.compile(entry.getType(), root.toString());
            previewStatus.setText(Component.empty());
            previewStatus.setDisplay(false);
            return new RecipeHolder<>(entry.getRecipeId(), recipe);
        } catch (RuntimeException exception) {
            previewStatus.setText(Component.translatable(key("preview_invalid")));
            previewStatus.setDisplay(true);
            previewStatus.style(s -> s.tooltips(Component.literal(String.valueOf(exception.getMessage()))));
            return null;
        }
    }

    private static int count(JsonElement element) {
        return element == null ? 0 : element.isJsonArray() ? element.getAsJsonArray().size() : 1;
    }

    @Override
    public void setVisualIngredient(int index, RecipeIngredient ingredient) {
        if (updatingInputs) return;
        updatingInputs = true;
        try {
            if (index < inputViews.size()) for (var slot : inputViews.get(index)) slot.setIngredient(ingredient.copy());
            else super.setVisualIngredient(index, ingredient);
            updateAnvilMaterialDisplay();
        } finally { updatingInputs = false; }
    }

    @Override
    public void screenTick() {
        // 标签轮播只切换显示，不通过重复槽位的通知回写成具体物品。
        updatingInputs = true;
        try { super.screenTick(); updateAnvilMaterialDisplay(); } finally { updatingInputs = false; }
    }

    private void collectInputs() {
        boolean plain = entry.getType().getPath().equals("anvil_crafting");
        if (root.has("item_in")) {
            var value = root.get("item_in");
            if (value.isJsonArray()) {
                var array = value.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    int index = i;
                    inputs.add(new Input(array.get(i), json -> array.set(index, json), !plain));
                }
            } else inputs.add(new Input(value, json -> root.add("item_in", json), !plain));
        }
        if (root.has("key")) root.getAsJsonObject("key").entrySet().forEach(e ->
                inputs.add(new Input(e.getValue(), json -> root.getAsJsonObject("key").add(e.getKey(), json), false)));
    }

    private void collectDrops(JsonElement actions) {
        if (actions.isJsonArray()) actions.getAsJsonArray().forEach(this::collectDrops);
        else if (actions.isJsonObject()) {
            var action = actions.getAsJsonObject();
            if (action.has("type") && LycheeRegistries.POST_ACTION.getKey(PostActionTypes.DROP_ITEM).equals(
                    net.minecraft.resources.ResourceLocation.parse(action.get("type").getAsString()))) {
                outputs.add(new Output(action, value -> {
                    for (String key : List.of("id", "count", "components")) action.remove(key);
                    value.getAsJsonObject().entrySet().forEach(e -> action.add(e.getKey(), e.getValue()));
                }));
            }
        }
    }

    @Override
    public void load() {
        var ops = RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry());
        originalInputs.clear();
        originalOutputs.clear();
        for (int i = 0; i < Math.min(inputs.size(), MAX_INGREDIENT); i++) {
            var input = inputs.get(i);
            var sized = input.sized && input.json.isJsonObject() && input.json.getAsJsonObject().isEmpty() ? SizedIngredient.EMPTY
                    : input.sized ? LycheeCodecs.SIZED_INGREDIENT.parse(ops, input.json).getOrThrow()
                    : new SizedIngredient(LycheeCodecs.NONEMPTY_INGREDIENT.parse(ops, input.json).getOrThrow(), 1);
            RecipeIngredient visual;
            try {
                visual = RecipeImporter.importIngredient(sized.ingredient()).setCount(sized.count());
            } catch (RecipeImportException unsupported) {
                // 复杂原料仅提供代表图标；未被用户替换时保留其原生匹配语义。
                var stacks = sized.ingredient().getItems();
                visual = RecipeIngredient.item(stacks.length == 0 ? ItemStack.EMPTY : stacks[0]).setCount(sized.count());
                visualIngredientSlots[i].style(s -> s.tooltips(Component.translatable(key("complex_ingredient"))));
            }
            loadIngredientSlot(i, visual);
            originalInputs.add(getVisualIngredient(i).serializeNBT(Platform.getFrozenRegistry()));
        }
        for (int i = 0; i < Math.min(outputs.size(), MAX_OUTPUT); i++) {
            var stack = LycheeCodecs.ITEM_STACK.parse(ops, outputs.get(i).json).getOrThrow();
            setVisualOutput(i, stack);
            originalOutputs.add(stack.copy());
        }
    }

    @Override
    public void save() {
        if (root == null) return;
        var ops = RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry());
        for (int i = 0; i < originalInputs.size(); i++) {
            var ingredient = getVisualIngredient(i);
            var snapshot = ingredient.serializeNBT(Platform.getFrozenRegistry());
            if (snapshot.equals(originalInputs.get(i))) continue;
            var input = inputs.get(i);
            var json = ingredient.isEmpty() ? (input.sized ? new JsonObject() : JsonParser.parseString("[]"))
                    : input.sized ? SizedIngredient.CODEC.encodeStart(ops,
                    new SizedIngredient(ingredient.compile(), Math.max(1, ingredient.getCount()))).getOrThrow()
                    : Ingredient.CODEC.encodeStart(ops, ingredient.compile()).getOrThrow();
            input.write.accept(json);
            originalInputs.set(i, snapshot);
        }
        for (int i = 0; i < originalOutputs.size(); i++) {
            var stack = getVisualOutput(i).getItem();
            if (ItemStack.matches(stack, originalOutputs.get(i))) continue;
            // 清空槽位保留空堆栈，交由原生配方校验报告缺少产物，不隐式删除带条件的动作。
            var json = stack.isEmpty() ? JsonParser.parseString("{\"id\":\"minecraft:air\",\"count\":0}")
                    : LycheeCodecs.ITEM_STACK.encodeStart(ops, stack).getOrThrow();
            outputs.get(i).write.accept(json);
            originalOutputs.set(i, stack.copy());
        }
        String json = LycheeRecipeFactory.JSON.toJson(root);
        boolean changed = !json.equals(getData().getRecipeJson());
        getData().setRecipeJson(json);
        updateAnvilCost();
        if (changed && scene != null) {
            var recipe = previewRecipe();
            if (recipe != null) scene.refresh(recipe);
        }
    }

    @Override
    public boolean ingredientHasCount(int index) { return index >= 0 && index < inputs.size() && inputs.get(index).sized; }

    @Override
    public void buildResultProperties(UIElement content) {
        super.buildResultProperties(content);
        var actionIndex = outputActions.get(selectedSlotIndex());
        if (actionIndex != null) content.addChild(RecipeEditorUi.textButton(Component.translatable(key("edit_this_action")),
                null, e -> showRuleEditor("actions", actionIndex)).setId("lychee_edit_output_action"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        // 自定义分类可以替换原生原料组；这些原料仍能通过属性栏选中并编辑。
        for (int index : unplacedInputs) content.addChild(RecipeEditorUi.textButton(
                Component.translatable("viscript_recipe.editor.properties.ingredient").append(" " + (index + 1)),
                new ItemStackTexture(getVisualIngredient(index).toStack()), e -> selectSlot(SlotSelection.ingredient(index))));
        for (String field : List.of("group", "comment")) content.addChild(RecipeEditorUi.fieldGroup(key(field),
                RecipeEditorUi.textField(root.has(field) ? root.get(field).getAsString() : "", text -> {
                    if (field.equals("group") && !text.isBlank() && net.minecraft.resources.ResourceLocation.tryParse(text) == null) return;
                    if (text.isBlank()) root.remove(field); else root.addProperty(field, text);
                    save();
                }).setId("lychee_" + field)));
        for (String field : List.of("ghost", "hide_in_viewer")) {
            var toggle = new Switch().setValue(root.has(field) && root.get(field).getAsBoolean(), false);
            toggle.registerValueListener(value -> {
                root.addProperty(field, value);
                save();
            });
            content.addChild(RecipeEditorUi.fieldGroup(key(field), toggle));
        }
        for (String field : List.of("time", "level_cost", "material_cost", "interval")) {
            boolean supported = switch (field) {
                case "time" -> entry.getType().getPath().equals("item_inside");
                case "interval" -> entry.getType().getPath().equals("entity_ticking");
                default -> entry.getType().getPath().equals("anvil_crafting");
            };
            int min = field.equals("interval") || field.equals("level_cost") ? 1 : 0;
            if (supported) content.addChild(RecipeEditorUi.fieldGroup(key(field),
                    RecipeEditorUi.intField(root.has(field) ? root.get(field).getAsInt() : min == 1 || field.equals("material_cost") ? 1 : 0,
                            min, Integer.MAX_VALUE, value -> {
                        root.addProperty(field, value);
                        save();
                    })));
        }
        content.addChild(RecipeEditorUi.textButton(Component.translatable(key("edit_rules")), null, e -> showRuleEditor("targets")));
    }

    private void showRuleEditor(String section) {
        showRuleEditor(section, -1);
    }

    private void showRuleEditor(String section, int ruleIndex) {
        save();
        new LycheeRuleEditor(entry.getType(), root, section, ruleIndex, json -> {
            getData().setRecipeJson(json);
            // reloadCanvas 会保存旧画布；先移除旧绑定，避免覆盖已提交的新规则。
            root = null;
            reloadCanvas();
        }).show(getModularUI());
    }

    private static String key(String field) { return LycheeRecipeEditorTypes.key(field); }
    private record Input(JsonElement json, Consumer<JsonElement> write, boolean sized) {}
    private record Output(JsonElement json, Consumer<JsonElement> write) {}
}
