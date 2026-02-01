package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.action.GolemAction;

import java.util.ArrayList;

public class AnchorMonitorComponent implements Component<EntityStore> {
    public static final BuilderCodec<AnchorMonitorComponent> CODEC;
    public static ComponentType<EntityStore, AnchorMonitorComponent> TYPE;

    private final Vector3i anchorLocation;
    public final ArrayList<GolemAction> actions = new ArrayList<>();
    public int tick = 0;
    public int ticksSinceLastAction = 0;
    public Vector3d lastPosition;

    public AnchorMonitorComponent() {
        this.anchorLocation = null;
    }


    public AnchorMonitorComponent(Vector3i loc) {
        this.anchorLocation = loc;
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
