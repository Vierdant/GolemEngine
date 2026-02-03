package me.arkon.golemengine.action;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;

public class MoveAction implements GolemAction {
    public static final BuilderCodec<MoveAction> CODEC =
            BuilderCodec.builder(MoveAction.class, MoveAction::new)
                    .append(
                            new KeyedCodec<>("Location", Vector3d.CODEC),
                            (a, v) -> a.location = v,
                            MoveAction::getLocation
                    )
                    .add()
                    .build();

    public Vector3d location;

    public MoveAction() {}

    public MoveAction(Vector3d location) {
        this.location = location;
    }

    public Vector3d getLocation() {
        return this.location;
    }

    @Override
    public GolemActionTypes getType() {
        return GolemActionTypes.MOVE;
    }
}
