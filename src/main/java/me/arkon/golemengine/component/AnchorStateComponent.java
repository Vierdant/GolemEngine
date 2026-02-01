package me.arkon.golemengine.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import me.arkon.golemengine.util.AnchorState;
import org.jetbrains.annotations.Nullable;

public class AnchorStateComponent implements Component<ChunkStore> {
    public static final BuilderCodec<AnchorStateComponent> CODEC;
    public static ComponentType<ChunkStore, AnchorStateComponent> TYPE;

    public AnchorState state;

    public AnchorStateComponent() {
        this.state = AnchorState.INACTIVE;
    }

    public static AnchorStateComponent withState(AnchorState state) {
        AnchorStateComponent comp = new AnchorStateComponent();
        comp.state = state;
        return comp;
    }

    @Override
    public @Nullable Component<ChunkStore> clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException _) {}

        return AnchorStateComponent.withState(this.state);
    }

    public static ComponentType<ChunkStore, AnchorStateComponent> getComponentType() {
        return AnchorStateComponent.TYPE;
    }

    static {
        CODEC = BuilderCodec.builder(AnchorStateComponent.class, AnchorStateComponent::new)
                .append(new KeyedCodec<>("State", new EnumCodec<>(AnchorState.class)),
                        (component, state) -> component.state = state,
                        (component) -> component.state).add().build();
    }
}
