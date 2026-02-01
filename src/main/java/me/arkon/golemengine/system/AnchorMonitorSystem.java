package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
        if (transform == null) return;
        Vector3d currentPos = transform.getPosition();

        if (monitor.lastPosition == null) {
            monitor.lastPosition = new Vector3d(currentPos);
            return;
        }

        Vector3d location = currentPos.clone();

        if (location.distanceSquaredTo(monitor.lastPosition) > (5 * 5)) {
            ActionUtil.flushWaitAction(monitor);
            monitor.actions.add(new MoveAction(location, transform.getTransform().getDirection()));
            monitor.lastPosition = new Vector3d(location);
            GolemEngine.LOGGER.atInfo().log("Player has moved! New location: " + currentPos.x + " " + currentPos.y + " " + currentPos.z);
        }
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), AnchorMonitorComponent.getComponentType());
    }
}
