package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.GolemAction;
import me.arkon.golemengine.action.MoveAction;
import me.arkon.golemengine.action.WaitAction;
import me.arkon.golemengine.component.GolemActionComponent;
import me.arkon.golemengine.util.ActionMovementState;

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

        if (golem.actions.isEmpty()) return;

        if (golem.waitTicks > 0) {
            golem.waitTicks--;
            return;
        }

        if (golem.moving) {
            return;
        }

        GolemAction action = golem.actions.get(golem.actionIndex);

        if (action instanceof MoveAction(Vector3d location, Vector3d direction)) {
            GolemEngine.LOGGER.atInfo().log("MOVE ACTION SENT");
            golem.moving = true;
            golem.target = location;
            golem.direction = direction;
        } else if (action instanceof WaitAction(int ticks)) {
            golem.waitTicks = ticks;
        }

        golem.actionIndex++;

        if (golem.actionIndex >= golem.actions.size()) {
            golem.actionIndex = 0;
        }
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return Query.and(GolemActionComponent.getComponentType());
    }
}
