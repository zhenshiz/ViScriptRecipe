package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigRL;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredientValue implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ingredient.value.kind")
    @ConfigSelector(candidate = {"item", "tag"})
    private IngredientValueKind kind = IngredientValueKind.ITEM;

    @Configurable(name = "viscript_recipe.config.ingredient.value.item")
    private ItemStack item = new ItemStack(Items.STONE);

    @Configurable(name = "viscript_recipe.config.ingredient.value.tag")
    @ConfigRL(ConfigRL.Type.ITEM_TAG_KEY)
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("minecraft", "planks");

    public static RecipeIngredientValue item(Item item) {
        return new RecipeIngredientValue().setKind(IngredientValueKind.ITEM).setItem(new ItemStack(item));
    }

    public static RecipeIngredientValue tag(ResourceLocation tag) {
        return new RecipeIngredientValue().setKind(IngredientValueKind.TAG).setTag(tag);
    }

    public Ingredient.Value compile() {
        return switch (kind) {
            case ITEM -> {
                var stack = item.copyWithCount(1);
                if (stack.isEmpty() || stack.is(Items.AIR)) {
                    throw new IllegalArgumentException("Ingredient item cannot be empty");
                }
                yield new Ingredient.ItemValue(stack);
            }
            case TAG -> {
                if (tag == null) {
                    throw new IllegalArgumentException("Ingredient tag cannot be empty");
                }
                var tagKey = TagKey.create(Registries.ITEM, tag);
                if (BuiltInRegistries.ITEM.getTag(tagKey).isEmpty()) {
                    throw new IllegalArgumentException("Unknown item tag: " + tag);
                }
                yield new Ingredient.TagValue(tagKey);
            }
        };
    }
}
