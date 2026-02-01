package me.arkon.golemengine.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.AnchorMonitorComponent;
import me.arkon.golemengine.component.AnchorStateComponent;
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

        AnchorMonitorComponent monitor = store.getComponent(player.getReference(), AnchorMonitorComponent.getComponentType());
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
        AnchorStateComponent anchorState = chunkStore.getComponent(chunkRef, AnchorStateComponent.getComponentType());

        if (anchorState == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player interacted with Crystal while not anchor is gone");
            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
            return;
        }

        if (anchorState.state != AnchorState.MONITOR) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("Player interacted with Crystal while anchor is not monitoring");
            context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);
            return;
        }

        context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), context.getHeldItem(), 1);

        commandBuffer.run(entityStore -> {
            store.removeComponent(player.getReference(), AnchorMonitorComponent.getComponentType());
            anchorState.state = AnchorState.ACTIVE;
            GolemEngine.LOGGER.atInfo().log("ACTIVATED!");
        });
    }
}
