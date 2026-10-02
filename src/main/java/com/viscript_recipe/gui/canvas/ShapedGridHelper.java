package com.viscript_recipe.gui.canvas;

import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.recipe.importer.RecipeImporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public final class ShapedGridHelper {

    public static void loadGrid(RecipeCanvas<?> canvas, List<String> pattern, List<ShapedKeyEntry> key,
                         int width, int height, int storageWidth) {
        var map = toMap(key);
        for (int row = 0; row < Math.min(height, pattern.size()); row++) {
            var line = pattern.get(row);
            for (int col = 0; col < Math.min(width, line.length()); col++) {
                var symbol = line.charAt(col);
                var ingredient = ingredientForSymbol(map, symbol);
                if (symbol != ' ' && ingredient.isEmpty()) canvas.containsUnsupportedIngredients = true;
                canvas.loadIngredientSlot(row * storageWidth + col, ingredient);
            }
        }
    }

    static HashMap<Character, RecipeIngredient> toMap(List<ShapedKeyEntry> key) {
        var map = new HashMap<Character, RecipeIngredient>();
        for (var entry : key) map.put(entry.compileSymbol(), entry.getIngredient());
        return map;
    }

    private static RecipeIngredient ingredientForSymbol(HashMap<Character, RecipeIngredient> keys, char symbol) {
        if (symbol == ' ') return RecipeIngredient.empty();
        var ingredient = keys.getOrDefault(symbol, RecipeIngredient.empty());
        return ingredient;
    }

    public static Pattern saveGrid(RecipeCanvas<?> canvas, int width, int height, int storageWidth) {
        var symbols = new LinkedHashMap<String, Character>();
        var key = new ArrayList<ShapedKeyEntry>();
        var pattern = new ArrayList<String>();
        int symbolIndex = 0;
        for (int row = 0; row < height; row++) {
            var line = new StringBuilder(width);
            for (int col = 0; col < width; col++) {
                var ingredient = canvas.getVisualIngredient(row * storageWidth + col);
                if (ingredient.isEmpty()) {
                    line.append(' ');
                    continue;
                }
                var ingredientKey = RecipeImporter.ingredientKey(ingredient);
                var symbol = symbols.get(ingredientKey);
                if (symbol == null) {
                    if (symbolIndex >= RecipeCanvas.SHAPED_SYMBOLS.length) {
                        line.append(' ');
                        continue;
                    }
                    symbol = RecipeCanvas.SHAPED_SYMBOLS[symbolIndex++];
                    symbols.put(ingredientKey, symbol);
                    key.add(ShapedKeyEntry.of(symbol, ingredient));
                }
                line.append(symbol);
            }
            pattern.add(line.toString());
        }
        return key.isEmpty() ? new Pattern(List.of(), List.of()) : new Pattern(cutEmptySide(pattern), key);
    }

    /**移除模板四周的空行和空列，避免无意义的空原料*/
    static ArrayList<String> cutEmptySide(ArrayList<String> pattern) {
        while (!pattern.isEmpty()) {
            if (pattern.getFirst().isBlank()) { pattern.removeFirst(); continue; }
            if (pattern.getLast().isBlank()) { pattern.removeLast(); continue; }
            boolean leftAllSpaces = true;
            for (String s : pattern) {
                if (s.charAt(0) != ' ') { leftAllSpaces = false; break; }
            }
            if (leftAllSpaces) {
                pattern.replaceAll(string -> string.substring(1));
                continue;
            }
            boolean rightAllSpaces = true;
            for (String s : pattern) {
                if (s.charAt(s.length() - 1) != ' ') { rightAllSpaces = false; break; }
            }
            if (rightAllSpaces) {
                pattern.replaceAll(s -> s.substring(0, s.length() - 1));
                continue;
            }
            break;
        }
        return pattern;
    }

    public record Pattern(List<String> pattern, List<ShapedKeyEntry> key) {
    }
}
