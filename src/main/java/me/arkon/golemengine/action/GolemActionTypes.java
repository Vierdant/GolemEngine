package me.arkon.golemengine.action;

public enum GolemActionTypes {
    WAIT("wait"),
    MOVE("move"),
    BLOCK("block"),
    INTERACT("interact");

    private final String key;

    GolemActionTypes(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
