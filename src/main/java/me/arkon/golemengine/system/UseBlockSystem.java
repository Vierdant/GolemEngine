package me.arkon.golemengine.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.InteractAction;
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
        Vector3i blockPos = event.getTargetBlock();
        if (player == null || player.getReference() == null) return;

        World world = Universe.get().getDefaultWorld();
        if (world == null) return;
        BlockType blockType = world.getBlockType(blockPos);
        if (blockType == null) return;

        PlayerMonitorComponent monitor = store.getComponent(player.getReference(), PlayerMonitorComponent.getComponentType());
        if (monitor == null) return;

        GolemEngine.LOGGER.atInfo().log("Player interacted with a " + blockType.getId());

        //long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z);
        //WorldChunk chunk = world.getChunk(index);
        //if (chunk == null) {
        //    return;
        //}
//
        //Ref<ChunkStore> chunkRef = chunk.getBlockComponentEntity(blockPos.x, blockPos.y, blockPos.z);
        //if (chunkRef == null) {
        //    chunkRef = BlockModule.ensureBlockEntity(chunk, blockPos.x, blockPos.y, blockPos.z);
        //    if (chunkRef == null) {
        //        return;
        //    }
        //}
//
        //Store<ChunkStore> chunkStore = world.getChunkStore().getStore();

        monitor.actions.add(new InteractAction(event.getTargetBlock()));

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType(), PlayerMonitorComponent.getComponentType());
    }
}
