package me.arkon.golemengine.action;

public enum GolemActionTypes {
    WAIT("wait"),
    MOVE("move");

    private final String key;

    GolemActionTypes(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
