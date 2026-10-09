package com.viscript_recipe.compat.eidolon.canvas;

import alexthw.eidolon_repraised.recipe.GenericRitualRecipe;
import alexthw.eidolon_repraised.registries.RitualRegistry;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.eidolon.EidolonRecipeEditorTypes;
import com.viscript_recipe.compat.eidolon.data.EidolonRitualRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public final class RitualCanvas extends EidolonItemCanvas<EidolonRitualRecipeData> {
    public RitualCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() {
        super.load();
        if (entry.isType(EidolonRecipeEditorTypes.ITEM_RITUAL)) setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        super.save();
        if (entry.isType(EidolonRecipeEditorTypes.ITEM_RITUAL)) getData().setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var data = getData();
        int total = data.getPedestals().size() + data.getFoci().size();
        boolean large = total > 6;
        int width = large ? 230 : 138;
        int height = large ? Math.max(214, 135 + ((total + 7) / 8) * 22) : 172;
        boolean invariants = entry.isType(EidolonRecipeEditorTypes.GENERIC_RITUAL) && !data.getInvariants().isEmpty();
        int invariantColumns = large ? 10 : 6;
        int invariantRows = (data.getInvariants().size() + invariantColumns - 1) / invariantColumns;
        var panel = EidolonCanvasFactory.page("jei_page_bg.png", width, height + (invariants ? 28 + invariantRows * 22 : 0));
        panel.addChild(EidolonCanvasFactory.label(text("health", data.getHealthRequirement()), 5, 12, width - 10));
        panel.addChild(EidolonCanvasFactory.slot(inputSlot(0, data.getReagent(), text("ritual_reagent")), width / 2 - 9, 84));
        int index = 1;
        var materials = new ArrayList<UIElement>();
        for (int i = 0; i < data.getPedestals().size(); i++) materials.add(inputSlot(index++, data.getPedestals().get(i), text("pedestal", i + 1)));
        for (int i = 0; i < data.getFoci().size(); i++) materials.add(data.getPedestals().size() / 2 + i,
                inputSlot(index++, data.getFoci().get(i), text("focus", i + 1)));
        for (int i = 0; i < materials.size(); i++) {
            int x, y;
            if (large) { x = 26 + i % 8 * 22; y = 127 + i / 8 * 22; }
            else {
                double step = Math.min(30, 180 / Math.max(1, total));
                double angle = Math.toRadians(90 - (total - 1) * step / 2 + i * step);
                x = (int) (69 + 48 * Math.cos(angle)) - 9;
                y = (int) (91 + 48 * Math.sin(angle)) - 9;
            }
            panel.addChild(EidolonCanvasFactory.slot(materials.get(i), x, y));
        }
        if (invariants) {
            panel.addChild(EidolonCanvasFactory.label(text("invariants"), 5, height + 4, width - 10));
            for (int i = 0; i < data.getInvariants().size(); i++) panel.addChild(EidolonCanvasFactory.slot(
                    inputSlot(index++, data.getInvariants().get(i), text("invariant", i + 1)), 6 + i % invariantColumns * 21, height + 20 + i / invariantColumns * 22));
        }
        if (entry.isType(EidolonRecipeEditorTypes.ITEM_RITUAL)) {
            var output = createOutputSlot(0, JEI_SLOT_SIZE); output.setId("eidolon_output");
            panel.addChild(EidolonCanvasFactory.slot(output, width / 2 - 9, 44));
        } else {
            Component outcome = entry.isType(EidolonRecipeEditorTypes.SUMMON_RITUAL)
                    ? BuiltInRegistries.ENTITY_TYPE.get(data.getEntity()).getDescription().copy().append(" × " + data.getSummonCount())
                    : entry.isType(EidolonRecipeEditorTypes.LOCATION_RITUAL) ? Component.literal("#" + data.getStructureTag().getPath())
                    : entry.isType(EidolonRecipeEditorTypes.COMMAND_RITUAL) ? text("commands")
                    : RitualRegistry.find(data.getRitual()) == null ? Component.literal(data.getRitual().getPath())
                    : RitualRegistry.find(data.getRitual()).getName();
            panel.addChild(EidolonCanvasFactory.label(outcome, 5, 44, width - 10));
        }
        return EidolonCanvasFactory.centered(panel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.category.eidolon.brazier"),
                floatField("viscript_recipe.config.eidolon.health", data.getHealthRequirement(), 0, Float.MAX_VALUE,
                        value -> { save(); data.setHealthRequirement(value); rebuild(); }),
                intField("viscript_recipe.config.eidolon.pedestal_slots", data.getPedestals().size(), 0, 256,
                        value -> { save(); resize(data.getPedestals(), value); rebuild(); }),
                intField("viscript_recipe.config.eidolon.focus_slots", data.getFoci().size(), 0, 256,
                        value -> { save(); resize(data.getFoci(), value); rebuild(); }));
        if (entry.isType(EidolonRecipeEditorTypes.GENERIC_RITUAL)) {
            var rituals = ritualIds();
            content.addChildren(selector("viscript_recipe.config.eidolon.ritual", rituals, data.getRitual(),
                    id -> RitualRegistry.find(id) == null ? Component.literal(id.toString()) : RitualRegistry.find(id).getName(), value -> { save(); data.setRitual(value); rebuild(); }),
                    resourceField("viscript_recipe.config.eidolon.ritual_id", data.getRitual(),
                            value -> { save(); data.setRitual(value); rebuild(); }),
                    intField("viscript_recipe.config.eidolon.invariant_slots", data.getInvariants().size(), 0, 256,
                            value -> { save(); resize(data.getInvariants(), value); rebuild(); }));
        }
        if (entry.isType(EidolonRecipeEditorTypes.ITEM_RITUAL)) content.addChild(switchField(
                "viscript_recipe.config.eidolon.keep_components", data.isKeepReagentComponents(), data::setKeepReagentComponents));
        if (entry.isType(EidolonRecipeEditorTypes.ITEM_RITUAL) || entry.isType(EidolonRecipeEditorTypes.COMMAND_RITUAL)) {
            content.addChildren(resourceField("viscript_recipe.config.eidolon.symbol", data.getSymbol(), data::setSymbol),
                    textField("viscript_recipe.config.eidolon.color", String.format(java.util.Locale.ROOT, "%08X", data.getColor()), value -> {
                        try {
                            var hex = value.replaceFirst("^(#|0[xX])", "");
                            if (hex.length() <= 8 && !hex.isBlank()) data.setColor((int) Long.parseUnsignedLong(hex, 16));
                        }
                        catch (NumberFormatException ignored) {}
                    }, Component.translatable("viscript_recipe.config.eidolon.color_hint")));
        }
        if (entry.isType(EidolonRecipeEditorTypes.SUMMON_RITUAL)) {
            content.addChildren(RecipeSearchComponents.entityType("viscript_recipe.config.eidolon.entity", data::getEntity,
                            value -> { save(); data.setEntity(value); rebuild(); }, () -> {}, EntityType.ZOMBIE),
                    intField("viscript_recipe.config.eidolon.summon_count", data.getSummonCount(), 1, Integer.MAX_VALUE,
                            value -> { save(); data.setSummonCount(value); rebuild(); }));
        }
        if (entry.isType(EidolonRecipeEditorTypes.LOCATION_RITUAL)) content.addChild(RecipeSearchComponents.structureTag(
                "viscript_recipe.config.eidolon.structure", data::getStructureTag,
                value -> { save(); data.setStructureTag(value); rebuild(); }, () -> {}));
        if (entry.isType(EidolonRecipeEditorTypes.COMMAND_RITUAL)) {
            var commands = data.getCommands();
            content.addChild(intField("viscript_recipe.config.eidolon.command_count", commands.size(), 1, Math.max(64, commands.size()), value -> {
                while (commands.size() < value) commands.add("");
                while (commands.size() > value) commands.removeLast();
                reloadProperties();
            }));
            for (int i = 0; i < commands.size(); i++) {
                int index = i;
                content.addChild(textField("viscript_recipe.config.eidolon.command", commands.get(i), value -> commands.set(index, value)));
            }
        }
    }

    private List<ResourceLocation> ritualIds() {
        var ids = new TreeSet<ResourceLocation>();
        for (String name : new String[]{"crystal", "allure", "repelling", "deceit", "daylight", "moonlight",
                "purify", "recharging_soulfire", "recharging_chill", "absorption"}) {
            var id = EidolonRecipeEditorTypes.id(name);
            if (RitualRegistry.find(id) != null) ids.add(id);
        }
        var level = Minecraft.getInstance().level;
        if (level != null) for (var holder : level.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof GenericRitualRecipe recipe && RitualRegistry.find(recipe.getId()) != null) ids.add(recipe.getId());
        }
        ids.add(getData().getRitual());
        return List.copyOf(ids);
    }

    @Override
    public void buildIngredientProperties(UIElement content) {
        super.buildIngredientProperties(content);
        int index = selectedSlotIndex();
        String key = index == 0 ? "ritual_reagent" : index <= getData().getPedestals().size() ? "pedestal_hint"
                : index <= getData().getPedestals().size() + getData().getFoci().size() ? "focus_hint" : "invariant_hint";
        content.addChild(sectionTitle("viscript_recipe.editor.eidolon." + key));
    }
}
