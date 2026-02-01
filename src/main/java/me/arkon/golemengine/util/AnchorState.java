package me.arkon.golemengine.util;

public enum AnchorState {
    INACTIVE, // Upon placing, this is the default state
    CRYSTAL, // Anchor has received a crystal shard from a player
    MONITOR, // Anchor got interacted with by a player and it started monitoring
    ACTIVE // Anchor is controlling a golem and executing various actions
}
