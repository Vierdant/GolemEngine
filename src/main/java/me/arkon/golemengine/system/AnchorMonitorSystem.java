package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.MoveAction;
import me.arkon.golemengine.component.AnchorMonitorComponent;
import me.arkon.golemengine.util.ActionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnchorMonitorSystem extends EntityTickingSystem<EntityStore> {

    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer)
    {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getReference() == null) return;

        AnchorMonitorComponent monitor = store.getComponent(player.getReference(), AnchorMonitorComponent.getComponentType());
        if (monitor == null) return;

        int RECORD_INTERVAL = 3;
        monitor.ticksSinceLastAction++;
        monitor.tick++;
        if (monitor.tick < RECORD_INTERVAL) {
            return;
        }

        monitor.tick = 0;

        TransformComponent transform = store.getComponent(player.getReference(), TransformComponent.getComponentType());
        MovementStatesComponent movementStates = store.getComponent(player.getReference(), MovementStatesComponent.getComponentType());

        if (transform == null || movementStates == null) return;
        Vector3d currentPos = transform.getPosition();

        if (monitor.lastPosition == null) {
            monitor.lastPosition = new Vector3d(currentPos);
            return;
        }

        double MOVE_THRESHOLD_SQ = 5.0;

        Vector3d location = currentPos.clone();
        MovementStates movement = movementStates.getMovementStates();
        boolean isMoving = !movement.idle && !movement.horizontalIdle && !movement.sitting;


        if (isMoving) {
            if (location.distanceSquaredTo(monitor.lastPosition) > MOVE_THRESHOLD_SQ) {
                MoveAction moveAction = new MoveAction(new Vector3d(location), transform.getTransform().getDirection());
                monitor.actions.add(moveAction);
                monitor.lastPosition.assign(location);
                monitor.ticksSinceLastAction = 0;
            }

            return;
        }

        ActionUtil.flushWaitAction(monitor);
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), AnchorMonitorComponent.getComponentType());
    }
}
