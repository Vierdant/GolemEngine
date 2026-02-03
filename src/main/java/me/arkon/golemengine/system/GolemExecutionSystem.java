package me.arkon.golemengine.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.*;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.BlockInteractionUtils;
import com.hypixel.hytale.server.core.modules.interaction.BlockPlaceUtils;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.action.*;
import me.arkon.golemengine.component.AnchorComponent;
import me.arkon.golemengine.component.GolemActionComponent;
import me.arkon.golemengine.util.TreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
        NPCEntity npcComp = archetypeChunk.getComponent(index, Objects.requireNonNull(NPCEntity.getComponentType()));
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        ModelComponent model = store.getComponent(entityRef, ModelComponent.getComponentType());
        Interactions interactions = store.getComponent(entityRef, Interactions.getComponentType());
        if (transform == null || model == null || npcComp == null || golem == null || interactions == null) return;

        Vector3i anchorPos = golem.anchorLocation;
        long chunkIndex = ChunkUtil.indexChunkFromBlock(anchorPos.x, anchorPos.z);
        World world = store.getExternalData().getWorld();
        WorldChunk chunk = world.getChunk(chunkIndex);
        if (chunk == null) return;

        Ref<ChunkStore> chunkRef = chunk.getBlockComponentEntity(anchorPos.x, anchorPos.y, anchorPos.z);
        if (chunkRef == null) {
            chunkRef = BlockModule.ensureBlockEntity(chunk, anchorPos.x, anchorPos.y, anchorPos.z);
            if (chunkRef == null) {
                commandBuffer.run(entityStore -> {
                    entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
                });
                return;
            }
        }

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        AnchorComponent anchor = chunkStore.getComponent(chunkRef, AnchorComponent.getComponentType());

        if (anchor == null) {
            commandBuffer.run(entityStore -> {
                entityStore.removeEntity(entityRef, RemoveReason.REMOVE);
            });
            return;
        };

        if (!Objects.equals(interactions.getInteractionId(InteractionType.Use), "Golem_Entity")) {
            interactions.setInteractionId(InteractionType.Use, "Golem_Entity");
        }

        if (anchor.golemPaused) {
            return;
        }

        if (golem.preventPickupTicks <= 0) {
            performDropPickup(golem, transform, model, commandBuffer);
            golem.preventPickupTicks = 0;
        } else {
            golem.preventPickupTicks--;
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

        } else if (action instanceof BlockAction blockAction) {
            if (blockAction.target.toVector3d().distanceSquaredTo(transform.getPosition()) <= 25.0) { // 5 blocks
                if (blockAction.placing) {
                    // BROKEN! Minimal use case - scrapped for mod jam version
                    boolean hasBlock = false;
                    for (ItemStack stack : golem.inventory) {
                        if (stack.getItemId().equals(blockAction.blockType)) {
                            hasBlock = true;
                            int quantity = stack.getQuantity();
                            if (quantity == 0) {
                                golem.inventory.remove(stack);
                            } else {
                                stack = stack.withQuantity( - 1);
                            }

                            break;
                        }
                    }

                    if (hasBlock) {
                        Vector3i blockPos = blockAction.target;
                        world.setBlock(blockPos.x, blockPos.y, blockPos.z, blockAction.blockType);
                        world.performBlockUpdate(blockPos.x, blockPos.y, blockPos.z);
                    }
                } else {
                    // break block
                    String sapling = TreeUtil.saplingFromTrunk(blockAction.blockType);
                    Vector3i blockPos = blockAction.target;
                    BlockType currentType = world.getBlockType(blockPos.x, blockPos.y, blockPos.z);
                    if (sapling != null && currentType != null) {
                        if (currentType.getId().equals(blockAction.blockType)) {
                            performBlockBreak(entityRef, blockAction.target, commandBuffer);
                            world.setBlock(blockPos.x, blockPos.y, blockPos.z, sapling);
                            world.performBlockUpdate(blockPos.x, blockPos.y, blockPos.z);
                        }
                    } else {
                        world.performBlockUpdate(blockPos.x, blockPos.y, blockPos.z);
                    }
                }
            }
        } else if (action instanceof InteractAction interactAction) {
            Vector3i blockPos = interactAction.getTarget();
            long blockChunkIndex = ChunkUtil.indexChunkFromBlock(blockPos.x, blockPos.z);
            WorldChunk blockChunk = world.getChunk(blockChunkIndex);
            if (blockChunk != null) {
                Ref<ChunkStore> blockChunkRef = blockChunk.getBlockComponentEntity(blockPos.x, blockPos.y, blockPos.z);
                if (blockChunkRef == null) {
                    blockChunkRef = BlockModule.ensureBlockEntity(blockChunk, blockPos.x, blockPos.y, blockPos.z);
                }

                if (blockChunkRef != null) {

                    Store<ChunkStore> blockChunkStore = world.getChunkStore().getStore();

                    if (BlockState.getBlockState(blockChunkRef, blockChunkStore)  instanceof ItemContainerState chest) {
                        if (golem.inventory.isEmpty()) {
                            golem.inventory.addAll(chest.getItemContainer().removeAllItemStacks());
                        } else {
                            chest.getItemContainer().addItemStacks(golem.inventory);
                            golem.inventory.clear();
                        }
                    }
                }
            }
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

    private void performBlockBreak(Ref<EntityStore> ref, Vector3i blockPosition, CommandBuffer<EntityStore> commandBuffer) {
        World world = commandBuffer.getExternalData().getWorld();
        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z);
        Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);
        if (chunkReference != null) {
            Vector3i position = new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
            BlockType blockType = world.getBlockType(blockPosition);
            if (blockType == null) return;
            if (blockType.getMaterial() == BlockMaterial.Empty) return;

            int quantity = 1;
            String itemId = null;
            String dropListId = null;
            BlockGathering blockGathering = blockType.getGathering();
            if (blockGathering != null) {
                PhysicsDropType physics = blockGathering.getPhysics();
                BlockBreakingDropType breaking = blockGathering.getBreaking();
                SoftBlockDropType soft = blockGathering.getSoft();
                HarvestingDropType harvest = blockGathering.getHarvest();
                if (physics != null) {
                    itemId = physics.getItemId();
                    dropListId = physics.getDropListId();
                } else if (breaking != null) {
                    quantity = breaking.getQuantity();
                    itemId = breaking.getItemId();
                    dropListId = breaking.getDropListId();
                } else if (soft != null) {
                    itemId = soft.getItemId();
                    dropListId = soft.getDropListId();
                } else if (harvest != null) {
                    itemId = harvest.getItemId();
                    dropListId = harvest.getDropListId();
                }
            }


            BlockHarvestUtils.performBlockBreak(world, position, blockType, null, quantity, itemId, dropListId, 0, ref, chunkReference, commandBuffer, chunkStore.getStore());
        }
    }


    private void performDropPickup(GolemActionComponent golem, TransformComponent transform, ModelComponent modelcomponent, CommandBuffer<EntityStore> commandBuffer) {
        Vector3d golemPos = transform.getPosition().clone().add(0, modelcomponent.getModel().getEyeHeight(), 0);
        List<Ref<EntityStore>> nearby = new ArrayList<>();
        SpatialResource<Ref<EntityStore>, EntityStore> itemSpatialResource = commandBuffer.getResource(EntityModule.get().getItemSpatialResourceType());
        itemSpatialResource.getSpatialStructure().collect(golemPos, 5, nearby);
        for (Ref<EntityStore> ref : nearby) {
            TransformComponent entityPos = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
            ItemComponent pickup = commandBuffer.getComponent(ref, ItemComponent.getComponentType());
            if (entityPos != null && pickup != null && pickup.canPickUp()) {
                entityPos.getPosition().assign(Vector3d.lerp(entityPos.getPosition(), golemPos, 1));
                if (entityPos.getPosition().distanceSquaredTo(transform.getPosition()) <= 10) {
                    ItemStack drop = pickup.getItemStack();
                    if (drop == null) continue;
                    AtomicBoolean merged = new AtomicBoolean(false);
                    commandBuffer.run(store -> {
                        for (int i = 0; i < golem.inventory.size(); i++) {
                            ItemStack stack = golem.inventory.get(i);

                            if (stack.getItemId().equals(drop.getItemId())) {
                                int newQuantity = stack.getQuantity() + drop.getQuantity();

                                ItemStack mergedStack = stack.withQuantity(newQuantity);

                                golem.inventory.set(i, mergedStack);
                                merged.set(true);
                                break;
                            }
                        }

                        if (!merged.get()) {
                            golem.inventory.add(drop);
                        }

                        store.removeEntity(ref, RemoveReason.REMOVE);
                    });
                }
            }
        }
    }

}
