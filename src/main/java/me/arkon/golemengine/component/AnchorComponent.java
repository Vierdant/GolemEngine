package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.GolemActionCodec;
import me.arkon.golemengine.util.AnchorState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnchorComponent implements Component<ChunkStore> {
    public static final BuilderCodec<AnchorComponent> CODEC;
    public static ComponentType<ChunkStore, AnchorComponent> TYPE;

    public ArrayList<GolemAction> actions;
    public AnchorState state;
    public UUID golemUUID;
    public boolean golemPaused;

    public AnchorComponent() {
        this.actions = new ArrayList<>();
        this.state = AnchorState.INACTIVE;
        this.golemPaused = false;
    }

    public AnchorComponent(ArrayList<GolemAction> actions) {
        this.actions = actions;
        this.state = AnchorState.INACTIVE;
        this.golemPaused = false;
    }

    @Override
    public @Nullable Component<ChunkStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        AnchorComponent clone = new AnchorComponent(this.actions);
        clone.state = this.state;
        clone.golemUUID = this.golemUUID;
        clone.golemPaused = this.golemPaused;

        return clone;
    }

    public static ComponentType<ChunkStore, AnchorComponent> getComponentType() {
        return AnchorComponent.TYPE;
    }


    static {
        CODEC = BuilderCodec.builder(AnchorComponent.class, AnchorComponent::new)
                .append(
                        new KeyedCodec<>("Actions",
                                new ArrayCodec<>(new GolemActionCodec(), GolemAction[]::new)
                        ),
                        (c, v) -> c.actions.addAll(List.of(v)),
                        c -> c.actions.toArray(GolemAction[]::new)
                )
                .add()
                .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY),
                        (component, uuid) -> component.golemUUID = uuid,
                        (component) -> component.golemUUID)
                .add()
                .append(new KeyedCodec<>("GolemPaused", Codec.BOOLEAN),
                        (component, paused) -> component.golemPaused = paused,
                        (component) -> component.golemPaused)
                .add()
                .append(new KeyedCodec<>("State", new EnumCodec<>(AnchorState.class)),
                        (component, state) -> component.state = state,
                        (component) -> component.state)
                .add()
                .build();
    }
}
