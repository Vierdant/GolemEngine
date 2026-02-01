package me.arkon.golemengine.util;

import com.hypixel.hytale.protocol.MovementStates;

public enum ActionMovementState {
    IDLE(new MovementStates(true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)),
    WALKING(new MovementStates(false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false));

    private MovementStates movementStates;

    ActionMovementState(MovementStates movementStates) {
        this.movementStates = movementStates;
    }

    public MovementStates getMovementStates() {
        return movementStates;
    }
}
