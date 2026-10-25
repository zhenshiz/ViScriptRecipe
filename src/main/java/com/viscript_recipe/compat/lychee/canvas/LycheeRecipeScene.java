package com.viscript_recipe.compat.lychee.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import org.joml.Vector2fc;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.*;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.InputAction;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/** 在编辑器中沿用 Lychee 配方查看器的场景、动画和槽位坐标。 */
final class LycheeRecipeScene extends UIElement {
    static final int SCALE = 1;
    static final int SLOT = 18 * SCALE;
    private final RvCategoryInstance<ILycheeRecipe<LycheeContext>> instance;
    private final UIElement decorations = new UIElement();
    private final Consumer<String> editSection;

    @SuppressWarnings({"rawtypes", "unchecked"})
    LycheeRecipeScene(String type, RecipeHolder holder, int inputCount, int actionCount,
                      IntFunction<UIElement> input, IntFunction<UIElement> action, Consumer<String> editSection) {
        this.editSection = editSection;
        RvCategory<ILycheeRecipe<LycheeContext>> category = (RvCategory) switch (type) {
            case "block_interacting", "block_clicking" -> new BlockInteractingRecipeCategory();
            case "item_burning" -> new ItemBurningRecipeCategory();
            case "item_inside" -> new ItemInsideRecipeCategory();
            case "block_crushing" -> new BlockCrushingRecipeCategory();
            case "lightning_channeling" -> new LightningChannelingRecipeCategory();
            case "item_exploding" -> new ItemExplodingRecipeCategory();
            case "block_exploding" -> new BlockExplodingRecipeCategory();
            case "random_block_ticking" -> new ItemAndBlockCategory<>();
            case "dripstone_dripping" -> new DripstoneRecipeCategory();
            default -> new RvCategory<>();
        };
        category.width = switch (type) {
            case "block_interacting", "block_clicking" -> 150;
            case "item_inside", "block_crushing", "lightning_channeling", "item_exploding" -> 170;
            default -> 120;
        };
        category.id = ((snownee.lychee.util.recipe.LycheeRecipeType<?>) holder.value().getType()).categoryId;
        category.init();
        var helper = new EditorHelper();
        helper.init();
        var recipe = (ILycheeRecipe<LycheeContext>) holder.value();
        instance = RvCategoryInstanceProviders.<ILycheeRecipe<LycheeContext>>get(category.id).get(category, helper)
                .apply(RVs.composeCategoryIdentifier(category.id, ResourceLocation.parse(recipe.group())));
        instance.addRecipe(holder);
        setId("lychee_scene");
        layout(l -> l.width(instance.width() * SCALE).height(instance.height() * SCALE).flexShrink(0));
        decorations.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).widthPercent(100).heightPercent(100));
        addChild(decorations);
        refresh(holder);
        var ingredients = RVs.generateShapelessInputs(recipe);
        var visibleActions = java.util.stream.IntStream.range(0, actionCount)
                .filter(i -> !recipe.postActions().get(i).hidden()).boxed().toList();
        category.configureLayout(new RvCategoryLayoutBuilder.Wrapped<ILycheeRecipe<LycheeContext>>(instance, holder) {
            @Override
            protected void _ingredientGroup(ILycheeRecipe<LycheeContext> value, IngredientLayout layout) {
                int count = Math.min(inputCount, 9);
                for (int i = 0; i < count; i++) {
                    var position = layout.position(i, count);
                    var slot = input.apply(i);
                    if (slot instanceof ItemSlot itemSlot && i < ingredients.size()) {
                        skin(itemSlot, ingredients.get(i).type);
                        var tips = new java.util.ArrayList<>(slot.getStyle().tooltips().asList());
                        tips.addAll(ingredients.get(i).tooltips);
                        slot.style(s -> s.tooltips(tips.toArray(Component[]::new)));
                    }
                    addChild(at(slot, position.x(), position.y(), 18, 18));
                }
            }

            @Override
            protected void _actionGroup(ILycheeRecipe<LycheeContext> value, Vector2fc position) {
                int count = Math.min(visibleActions.size(), 9);
                int columns = (int) Math.ceil(Math.sqrt(count));
                int rows = columns == 0 ? 0 : (count + columns - 1) / columns;
                for (int i = 0; i < count; i++) {
                    addChild(at(action.apply(visibleActions.get(i)), position.x() - columns * 9 + i % columns * 19,
                            position.y() - rows * 9 + i / columns * 19, 18, 18));
                }
            }
        }, holder);
    }

    void refresh(RecipeHolder<ILycheeRecipe<LycheeContext>> holder) {
        decorations.clearAllChildren();
        var builder = new RvCategoryWidgetBuilder<ILycheeRecipe<LycheeContext>>(instance, holder) {
            private String key;

            @Override
            public void addElement(RenderElement element) {
                String section = switch (key) {
                    case "block_in", "falling_block", "landing_block", "source_block", "target_block" -> "targets";
                    case "info" -> "conditions";
                    default -> null;
                };
                var view = element(element, section == null ? null : () -> editSection.accept(section));
                view.setId("lychee_scene_" + key + "_" + decorations.getChildren().size());
                if (section != null) view.addClass("lychee_scene_" + section);
                decorations.addChild(at(view, element.x(), element.y(), Math.max(8, element.width()), Math.max(8, element.height())));
            }

            void build() {
                for (var entry : decorations().entrySet()) {
                    key = entry.getKey();
                    var condition = condition(key);
                    if ((renderDefault() || !instance.type().decorations.containsKey(key))
                            && (condition == null || condition.test(holder.value()))) entry.getValue().setup(this, holder);
                }
            }
        };
        builder.build();
    }

    static UIElement at(UIElement element, float x, float y, float width, float height) {
        return element.layout(l -> l.positionType(TaffyPosition.ABSOLUTE)
                .left(x * SCALE).top(y * SCALE).width(width * SCALE).height(height * SCALE));
    }

    static void skin(ItemSlot slot, SlotType type) {
        slot.layout(l -> l.paddingAll(SCALE));
        slot.slotStyle(s -> s.slotOverlay(IGuiTexture.EMPTY));
        slot.style(s -> s.backgroundTexture(slotTexture(type)));
    }

    static SpriteTexture slotTexture(SlotType type) {
        String sprite = switch (type) {
            case CHANCE -> "chance_slot";
            case CATALYST -> "catalyst_slot";
            default -> "slot";
        };
        return SpriteTexture.of(ResourceLocation.fromNamespaceAndPath("lychee", "textures/gui/sprites/" + sprite + ".png"))
                .setSprite(0, 0, 18, 18);
    }

    static UIElement element(RenderElement element, Runnable click) {
        var view = new UIElement() {
            @Override
            public void drawBackgroundAdditional(GUIContext context) {
                super.drawBackgroundAdditional(context);
                var graphics = context.graphics;
                graphics.flush();
                graphics.pose().pushPose();
                graphics.pose().translate(getPositionX(), getPositionY(), 0);
                graphics.pose().scale(SCALE, SCALE, 1);
                graphics.pose().translate(-element.x(), -element.y(), 0);
                element.render(graphics);
                graphics.flush();
                graphics.pose().popPose();
            }
        };
        view.addEventListener(UIEvents.MOUSE_ENTER, event -> {
            var tips = new java.util.ArrayList<Component>();
            if (element instanceof InteractiveRenderElement interactive && interactive.getTooltip() != null) tips.addAll(interactive.getTooltip());
            if (click != null) tips.add(Component.translatable("viscript_recipe.editor.lychee.click_to_edit"));
            view.style(s -> s.tooltips(tips.toArray(Component[]::new)));
        });
        if (click != null) view.addEventListener(UIEvents.MOUSE_DOWN, event -> {
            if (event.button == 0) { click.run(); event.stopPropagation(); }
        });
        return view;
    }

    private static final class EditorHelper extends RvHelper {
        @Override public boolean doAction(ItemStack stack, InputAction.Direct action) { return false; }
        @Override public boolean doAction(Fluid fluid, InputAction.Direct action) { return false; }
        @Override public Optional<InputAction.Direct> toDirectAction(InputAction action) { return Optional.empty(); }
        @Override public boolean appendModName() { return false; }
    }
}
