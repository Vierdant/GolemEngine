package me.arkon.golemengine.action;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

public class BlockAction implements GolemAction {
    public static final BuilderCodec<BlockAction> CODEC =
            BuilderCodec.builder(BlockAction.class, BlockAction::new)
                    .append(
                            new KeyedCodec<>("Target", Vector3i.CODEC),
                            (a, v) -> a.target = v,
                            BlockAction::getTarget
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("BlockTYpe", Codec.STRING),
                            (a, v) -> a.blockType = v,
                            BlockAction::getBlockType
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("Placing", Codec.BOOLEAN),
                            (a, v) -> a.placing = v,
                            BlockAction::isPlacing
                    )
                    .add()
                    .build();


    public Vector3i target;
    public String blockType;
    public boolean placing;

    public BlockAction() {}

    public BlockAction(Vector3i target, String blockType, boolean placing) {
        this.target = target;
        this.blockType = blockType;
        this.placing = placing;
    }

    public Vector3i getTarget() {
        return this.target;
    }

    public boolean isPlacing() {
        return this.placing;
    }

    public String getBlockType() {
        return this.blockType;
    }

    @Override
    public GolemActionTypes getType() {
        return GolemActionTypes.BLOCK;
    }
}
