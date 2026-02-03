package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.PlayerMonitorComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UseBlockSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {


    public UseBlockSystem() {
        super(UseBlockEvent.Post.class);
    }

    @Override
    public void handle(
            int index,
            @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NotNull Store<EntityStore> store,
            @NotNull CommandBuffer<EntityStore> commandBuffer,
            @NotNull UseBlockEvent.Post event)
    {

        Player player = store.getComponent(archetypeChunk.getReferenceTo(index), Player.getComponentType());
        if (player == null || player.getReference() == null) return;

        World world = Universe.get().getDefaultWorld();
        if (world == null) return;
        BlockType blockType = world.getBlockType(event.getTargetBlock());
        if (blockType == null) return;

        PlayerMonitorComponent monitor = store.getComponent(player.getReference(), PlayerMonitorComponent.getComponentType());
        if (monitor == null) return;

        GolemEngine.LOGGER.atInfo().log("Player interacted with a " + blockType.getId());
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), PlayerMonitorComponent.getComponentType());
    }
}
