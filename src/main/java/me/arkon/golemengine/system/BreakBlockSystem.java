package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.PlayerMonitorComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BreakBlockSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {


    public BreakBlockSystem() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(
            int index,
            @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NotNull Store<EntityStore> store,
            @NotNull CommandBuffer<EntityStore> commandBuffer,
            @NotNull BreakBlockEvent event)
    {
        Player player = store.getComponent(archetypeChunk.getReferenceTo(index), Player.getComponentType());
        if (player == null || player.getReference() == null) return;

        World world = Universe.get().getDefaultWorld();
        if (world == null) return;
        BlockType blockType = world.getBlockType(event.getTargetBlock());
        if (blockType == null) return;

        PlayerMonitorComponent monitor = store.getComponent(player.getReference(), PlayerMonitorComponent.getComponentType());
        if (monitor == null) return;

        if (!Objects.equals(blockType.getId(), "Empty")) {
            GolemEngine.LOGGER.atInfo().log("Player destroyed a " + blockType.getId());
        } else {
            GolemEngine.LOGGER.atInfo().log("Player placed a " + blockType.getId());
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), PlayerMonitorComponent.getComponentType());
    }
}
