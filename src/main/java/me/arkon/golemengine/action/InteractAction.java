package me.arkon.golemengine.action;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

public class InteractAction implements GolemAction {
    public static final BuilderCodec<InteractAction> CODEC =
            BuilderCodec.builder(InteractAction.class, InteractAction::new)
                    .append(
                            new KeyedCodec<>("Target", Vector3i.CODEC),
                            (a, v) -> a.target = v,
                            InteractAction::getTarget
                    )
                    .add()
                    .build();


    public Vector3i target;

    public InteractAction() {}

    public InteractAction(Vector3i target) {
        this.target = target;
    }

    public Vector3i getTarget() {
        return this.target;
    }

    @Override
    public GolemActionTypes getType() {
        return GolemActionTypes.INTERACT;
    }
}
