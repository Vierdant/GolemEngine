package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.GolemActionCodec;

import java.util.ArrayList;
import java.util.List;

public class GolemActionComponent implements Component<EntityStore> {
    public static final BuilderCodec<GolemActionComponent> CODEC;
    public static ComponentType<EntityStore, GolemActionComponent> TYPE;

    public final ArrayList<GolemAction> actions;
    public ArrayList<ItemStack> inventory;
    public Vector3i anchorLocation;
    public int waitTicks;
    public int actionIndex;
    public int activeMoveIndex;
    public int preventPickupTicks;
    public boolean moving;
    public Vector3d target;

    public GolemActionComponent() {
        this.actions = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.anchorLocation = Vector3i.ZERO;
        this.waitTicks = 0;
        this.activeMoveIndex = 0;
        this.actionIndex = 0;
        this.preventPickupTicks = 0;
        this.moving = false;
        this.target = new Vector3d();

    }

    public GolemActionComponent(ArrayList<GolemAction> actions) {
        this.actions = actions;
        this.inventory = new ArrayList<>();
        this.anchorLocation = Vector3i.ZERO;
        this.waitTicks = 0;
        this.actionIndex = 0;
        this.activeMoveIndex = 0;
        this.preventPickupTicks = 0;
        this.moving = false;
        this.target = new Vector3d();
    }

    public GolemActionComponent(ArrayList<GolemAction> actions, Vector3i anchorLocation) {
        this.actions = actions;
        this.inventory = new ArrayList<>();
        this.anchorLocation = anchorLocation;
        this.waitTicks = 0;
        this.actionIndex = 0;
        this.activeMoveIndex = 0;
        this.preventPickupTicks = 0;
        this.moving = false;
        this.target = new Vector3d();
    }

    public GolemActionComponent(ArrayList<GolemAction> actions, Vector3i anchorLocation, ArrayList<ItemStack> inventory, int waitTicks, int actionIndex) {
        this.actions = actions;
        this.inventory = inventory;
        this.anchorLocation = anchorLocation;
        this.waitTicks = waitTicks;
        this.actionIndex = actionIndex;
        this.activeMoveIndex = 0;
        this.preventPickupTicks = 0;
        this.moving = false;
        this.target = new Vector3d();
    }

    public static ComponentType<EntityStore, GolemActionComponent> getComponentType() {
        return GolemActionComponent.TYPE;
    }


    public Component<EntityStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return new GolemActionComponent(this.actions, this.anchorLocation, this.inventory, this.waitTicks, this.actionIndex);
    }


    static {
        CODEC = BuilderCodec.builder(GolemActionComponent.class, GolemActionComponent::new)
                .append(
                        new KeyedCodec<>("Actions",
                                new ArrayCodec<>(new GolemActionCodec(), GolemAction[]::new)
                        ),
                        (c, v) -> c.actions.addAll(List.of(v)),
                        c -> c.actions.toArray(GolemAction[]::new)
                )
                .add()
                .append(
                        new KeyedCodec<>("Inventory",
                                new ArrayCodec<>(ItemStack.CODEC, ItemStack[]::new)
                        ),
                        (c, v) -> c.inventory.addAll(List.of(v)),
                        c -> c.inventory.toArray(ItemStack[]::new)
                )
                .add()
                .append(
                        new KeyedCodec<>("AnchorLocation", Vector3i.CODEC),
                        (c, v) -> c.anchorLocation = v,
                        c -> c.anchorLocation
                )
                .add()
                .append(
                        new KeyedCodec<>("Index", Codec.INTEGER),
                        (c, v) -> c.actionIndex = v,
                        c -> c.actionIndex
                )
                .add()
                .build();
    }
}
