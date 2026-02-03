package me.arkon.golemengine.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.Pair;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.PlayerMonitorComponent;
import me.arkon.golemengine.component.AnchorComponent;
import me.arkon.golemengine.component.GolemActionComponent;
import me.arkon.golemengine.util.AnchorState;

import javax.annotation.Nonnull;

public class GolemCrystalInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<GolemCrystalInteraction> CODEC = BuilderCodec.builder(
            GolemCrystalInteraction.class, GolemCrystalInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        World world = commandBuffer.getExternalData().getWorld();
        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
        Ref<EntityStore> ref = context.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null || player.getReference() == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player is null");
            return;
        }

        ItemStack itemStack = context.getHeldItem();
        if (itemStack == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("ItemStack is null");
            return;
        }

        if (context.getHeldItemContainer() == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player container is null");
            return;
        }

        PlayerMonitorComponent monitor = store.getComponent(player.getReference(), PlayerMonitorComponent.getComponentType());
        if (monitor == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player interacted with Crystal while not being monitored");
            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
            return;
        }

        Vector3i pos = monitor.getAnchorLocation();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Ref<ChunkStore> chunkRef = chunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
        if (chunkRef == null) {
            chunkRef = BlockModule.ensureBlockEntity(chunk, pos.x, pos.y, pos.z);
            if (chunkRef == null) {
                GolemEngine.LOGGER.atSevere().log("Failed to interact with crystal due to null chunk ref.");
                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        AnchorComponent anchor = chunkStore.getComponent(chunkRef, AnchorComponent.getComponentType());

        if (anchor == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player interacted with Crystal while not anchor is gone");
            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
            return;
        }

        if (anchor.state != AnchorState.MONITOR) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player interacted with Crystal while anchor is not monitoring");
            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
            return;
        }

        context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);

        commandBuffer.run(entityStore -> {
            GolemEngine.get().spawnGolemAtAnchor(entityStore, anchor, monitor);
            store.removeComponent(player.getReference(), PlayerMonitorComponent.getComponentType());
            anchor.state = AnchorState.ACTIVE;
            anchor.actions = monitor.actions;
        });
    }
}
