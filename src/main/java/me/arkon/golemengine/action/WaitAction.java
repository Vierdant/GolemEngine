package me.arkon.golemengine.action;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;

public class WaitAction implements GolemAction {
    public static final BuilderCodec<WaitAction> CODEC =
            BuilderCodec.builder(WaitAction.class, WaitAction::new)
                    .append(
                            new KeyedCodec<>("Ticks", Codec.INTEGER),
                            (a, v) -> a.ticks = v,
                            WaitAction::getTicks
                    )
                    .addValidator(Validators.greaterThanOrEqual(0))
                    .add()
                    .build();

    public int ticks;

    public WaitAction() {}

    public WaitAction(int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return this.ticks;
    }

    @Override
    public GolemActionTypes getType() {
        return GolemActionTypes.WAIT;
    }
}
