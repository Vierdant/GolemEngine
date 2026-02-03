package me.arkon.golemengine.util;

import java.util.Map;

public class TreeUtil {

    public static final Map<String, String> TREE_TO_SAPLING =
            Map.ofEntries(
                    Map.entry("Apple", "Plant_Sapling_Apple"),
                    Map.entry("Ash", "Plant_Sapling_Ash"),
                    Map.entry("Beech", "Plant_Sapling_Beech"),
                    Map.entry("Birch", "Plant_Sapling_Birch"),
                    Map.entry("Crystal", "Plant_Sapling_Crystal"),
                    Map.entry("Dry", "Plant_Sapling_Dry"),
                    Map.entry("Oak", "Plant_Sapling_Oak"),
                    Map.entry("Palm", "Plant_Sapling_Palm"),
                    Map.entry("Poisoned", "Plant_Sapling_Poisoned"),
                    Map.entry("Redwood", "Plant_Sapling_Redwood"),
                    Map.entry("Spruce", "Plant_Sapling_Spruce"),
                    Map.entry("Frozen", "Plant_Sapling_Frozen"),
                    Map.entry("Sand", "Plant_Sapling_Sand")
            );

    public static String saplingFromTrunk(String blockId) {
        if (!blockId.endsWith("_Trunk")) return null;

        String tree = blockId.substring(5, blockId.length() - "_Trunk".length());
        return "Plant_Sapling_" + tree;
    }
}
