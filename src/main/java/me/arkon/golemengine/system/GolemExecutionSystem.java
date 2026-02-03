package me.arkon.golemengine.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.MoveAction;
import me.arkon.golemengine.action.WaitAction;
import me.arkon.golemengine.component.AnchorComponent;
import me.arkon.golemengine.component.GolemActionComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class GolemExecutionSystem extends EntityTickingSystem<EntityStore> {

    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer)
    {
        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);

        GolemActionComponent golem = store.getComponent(entityRef, GolemActionComponent.getComponentType());
        if (golem == null) return;
        NPCEntity npcComp = archetypeChunk.getComponent(index, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcComp == null) return;
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3i anchorPos = golem.anchorLocation;

        commandBuffer.run(entityStore -> {

            long chunkIndex = ChunkUtil.indexChunkFromBlock(anchorPos.x, anchorPos.z);
            World world = store.getExternalData().getWorld();
            WorldChunk chunk = world.getChunk(chunkIndex);
            if (chunk == null) return;

            Ref<ChunkStore> chunkRef = chunk.getBlockComponentEntity(anchorPos.x, anchorPos.y, anchorPos.z);
            if (chunkRef == null) {
                chunkRef = BlockModule.ensureBlockEntity(chunk, anchorPos.x, anchorPos.y, anchorPos.z);
                if (chunkRef == null) {
                    entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
                    return;
                }
            }

            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            AnchorComponent anchor = chunkStore.getComponent(chunkRef, AnchorComponent.getComponentType());

            if (anchor == null) {
                entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
                return;
            };

            if (anchor.golemPaused) {
                return;
            }

            if (golem.actions.isEmpty()) return;

            if (golem.waitTicks > 0) {
                golem.waitTicks--;
                return;
            }

            if (golem.moving) {
                return;
            }

            GolemAction action = golem.actions.get(golem.actionIndex);

            if (action instanceof MoveAction moveAction) {
                golem.moving = true;
                golem.target = moveAction.getLocation();
                return;

            } else if (action instanceof WaitAction waitAction) {
                golem.waitTicks = waitAction.getTicks();
            }

            golem.actionIndex++;

            if (golem.actionIndex >= golem.actions.size()) {
                golem.actionIndex = 0;
            }
        });
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return Query.and(GolemActionComponent.getComponentType());
    }
}
