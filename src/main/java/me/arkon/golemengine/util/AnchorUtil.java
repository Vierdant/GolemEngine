package me.arkon.golemengine.util;

public class AnchorUtil {

    private static final String[] VALID_ANCHOR_SHARDS = {
            "Ingredient_Crystal_Blue",
            "Ingredient_Crystal_Cyan",
            "Ingredient_Crystal_Green",
            "Ingredient_Crystal_Purple",
            "Ingredient_Crystal_Red",
            "Ingredient_Crystal_Yellow",
    };

    public static boolean validateAnchorShard(String id) {
        for (String shard : VALID_ANCHOR_SHARDS) {
            if (id.equals(shard)) return true;
        }
        return false;
    }

}
