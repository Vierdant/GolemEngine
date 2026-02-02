package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.action.GolemAction;

import java.util.ArrayList;

public class GolemActionComponent implements Component<EntityStore> {
    public static final BuilderCodec<GolemActionComponent> CODEC;
    public static ComponentType<EntityStore, GolemActionComponent> TYPE;

    public final ArrayList<GolemAction> actions;
    public int waitTicks;
    public int actionIndex;
    public int activeMoveIndex;
    public boolean moving;
    public Vector3d target;
    public Vector3d direction;

    public GolemActionComponent() {
        this.actions = new ArrayList<>();
        this.waitTicks = 0;
        this.activeMoveIndex = 0;
        this.actionIndex = 0;
        this.moving = false;
        this.target = new Vector3d();
        this.direction = new Vector3d();

    }

    public GolemActionComponent(ArrayList<GolemAction> actions) {
        this.actions = actions;
        this.waitTicks = 0;
        this.actionIndex = 0;
        this.activeMoveIndex = 0;
        this.moving = false;
        this.target = new Vector3d();
        this.direction = new Vector3d();
    }

    public GolemActionComponent(ArrayList<GolemAction> actions, int waitTicks, int actionIndex) {
        this.actions = actions;
        this.waitTicks = waitTicks;
        this.actionIndex = actionIndex;
        this.activeMoveIndex = 0;
        this.moving = false;
        this.target = new Vector3d();
        this.direction = new Vector3d();
    }

    public static ComponentType<EntityStore, GolemActionComponent> getComponentType() {
        return GolemActionComponent.TYPE;
    }


    public Component<EntityStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return new GolemActionComponent(this.actions, this.waitTicks, this.actionIndex);
    }


    static {
        CODEC = BuilderCodec.builder(GolemActionComponent.class, GolemActionComponent::new).build();
    }
}
