package me.arkon.golemengine.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.component.AnchorMonitorComponent;
import me.arkon.golemengine.util.AnchorState;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.AnchorStateComponent;
import me.arkon.golemengine.util.AnchorUtil;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class GolemAnchorInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<GolemAnchorInteraction> CODEC = BuilderCodec.builder(
                    GolemAnchorInteraction.class, GolemAnchorInteraction::new, SimpleBlockInteraction.CODEC
            )
            .documentation("Handles a golem anchor block behaviour.")
            .build();

    @Override
    protected void interactWithBlock(
            @Nonnull World world,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nullable ItemStack itemInHand,
            @Nonnull Vector3i pos,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        long index = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
        WorldChunk chunk = world.getChunk(index);
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
        AnchorStateComponent anchorStateComponent = chunkStore.getComponent(chunkRef, AnchorStateComponent.getComponentType());

        if (anchorStateComponent == null) {
            GolemEngine.LOGGER.atInfo().log("BAD COMP");
            context.getState().state = InteractionState.Failed;
            return;
        }

        Store<EntityStore> entityStore = context.getEntity().getStore();

        if (anchorStateComponent.state == AnchorState.INACTIVE) {
            GolemEngine.LOGGER.atInfo().log("INACTIVE");
            if (itemInHand == null || !AnchorUtil.validateAnchorShard(itemInHand.getItemId())) {
                context.getState().state = InteractionState.Failed;
                return;
            }


            if (context.getHeldItemContainer() != null) {
                ItemStackSlotTransaction itemstackslottransaction = context.getHeldItemContainer().removeItemStackFromSlot(context.getHeldItemSlot(), itemInHand, 1);
                if (!itemstackslottransaction.succeeded()) {
                    context.getState().state = InteractionState.Failed;
                    return;
                }
            }

            anchorStateComponent.state = AnchorState.CRYSTAL;
            GolemEngine.LOGGER.atInfo().log("SUCCESS - +CRYSTAL STATE");
            return;
        }

        if (anchorStateComponent.state == AnchorState.CRYSTAL) {
            Item item = Item.getAssetMap().getAsset("Tool_Golem_Crystal");
            if (item == null || context.getHeldItemContainer() == null) {
                GolemEngine.LOGGER.atSevere().log("Something went wrong! Golem Crystal or player inventory entry was not found.");
                context.getState().state = InteractionState.Failed;
                return;
            }
            commandBuffer.run(store -> {
                ItemStack stack = (new ItemStack(item.getId(), 1, null));
                Player player = entityStore.getComponent(context.getEntity(), Player.getComponentType());

                if (player == null || player.getReference() == null) {
                    GolemEngine.LOGGER.atSevere().log("Something went wrong! Player was not found.");
                    context.getState().state = InteractionState.Failed;
                    return;
                }

                ItemStackTransaction transaction = player.getInventory().getCombinedHotbarFirst().addItemStack(stack);
                ItemStack remainder = transaction.getRemainder();
                if (remainder != null && !remainder.isEmpty()) {
                    // not enough space
                    context.getState().state = InteractionState.Failed;
                    return;
                }

                player.sendMessage(Message.raw("YOO! YOU GOT THE BLOCKY CRYSTAL THING!"));
                entityStore.removeComponentIfExists(player.getReference(), AnchorMonitorComponent.getComponentType());
                entityStore.addComponent(player.getReference(), AnchorMonitorComponent.getComponentType(), new AnchorMonitorComponent(pos));
                anchorStateComponent.state = AnchorState.MONITOR;
            });
            return;
        }

        if (anchorStateComponent.state == AnchorState.ACTIVE) {
            GolemEngine.LOGGER.atInfo().log("THIS IS ACTIVE!");
            // toggle golem execution tick
            // if crouched reset golem to anchor location
        }
    }


    @Override
    protected void simulateInteractWithBlock(
            @Nonnull InteractionType type, @Nonnull InteractionContext context, @javax.annotation.Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
    ) {
        // intentionally empty
    }
}
