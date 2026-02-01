package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AnchorMonitorComponent implements Component<EntityStore> {
    public static final BuilderCodec<AnchorMonitorComponent> CODEC;
    public static ComponentType<EntityStore, AnchorMonitorComponent> TYPE;

    private final Vector3i anchorLocation;
    public int tick = 0;
    public Vector3d lastPosition;
    public int RECORD_INTERVAL = 3;

    public AnchorMonitorComponent() {
        this.anchorLocation = null;
    }


    public AnchorMonitorComponent(Vector3i loc) {
        this.anchorLocation = loc;
    }


    public boolean hasMoved(Vector3d position) {
        // returns true if player moved
        if (lastPosition == null) {
            lastPosition = new Vector3d(position);
            return true;
        }

        double minDistance = 0.5;
        boolean moved = position.distanceSquaredTo(lastPosition) > (minDistance * minDistance);

        if (moved) {
            lastPosition = new Vector3d(position);
        }
        return moved;
    }

    public Vector3i getAnchorLocation() {
        return anchorLocation;
    }


    public static ComponentType<EntityStore, AnchorMonitorComponent> getComponentType() {
        return AnchorMonitorComponent.TYPE;
    }


    public Component<EntityStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return null;
    }

    static {
        CODEC = BuilderCodec.builder(AnchorMonitorComponent.class, AnchorMonitorComponent::new).build();
    }
}
