package me.arkon.golemengine.action;

import com.hypixel.hytale.math.vector.Vector3d;

public record MoveAction(Vector3d location, Vector3d direction) implements GolemAction {
}
