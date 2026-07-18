package com.viscript_recipe.data.create;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.create.CreateRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CreateSequencedAssemblyRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.GOLD_INGOT);

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.transitional_item")
    private ItemStack transitionalItem = ItemStack.EMPTY;

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.sequence")
    @ConfigList(addDefaultMethod = "createDefaultStep")
    private List<CreateSequencedAssemblyStepData> sequence = new ArrayList<>(List.of(
            new CreateSequencedAssemblyStepData(),
            new CreateSequencedAssemblyStepData().setKind(CreateSequencedAssemblyStepKind.PRESSING)
    ));

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.results")
    @ConfigList(addDefaultMethod = "createDefaultOutput")
    private List<CreateProcessingOutputData> outputs = new ArrayList<>(List.of(new CreateProcessingOutputData()
            .setItem(new ItemStack(Items.CLOCK))
            .setChance(1.0F)));

    @Configurable(name = "viscript_recipe.config.create.sequenced_assembly.loops")
    private int loops = 1;

    public CreateSequencedAssemblyStepData createDefaultStep() {
        return new CreateSequencedAssemblyStepData();
    }

    public CreateProcessingOutputData createDefaultOutput() {
        return new CreateProcessingOutputData();
    }

    @Override
    public ItemStack getResult() {return CreateRecipeEditorTypes.firstOutput(this);}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        CreateRecipeEditorTypes.setFirstOutput(this, result);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return CreateRecipeFactory.compileSequencedAssembly(this);
    }
}
