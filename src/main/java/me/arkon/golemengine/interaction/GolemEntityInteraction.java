package me.arkon.golemengine.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.GolemActionComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class GolemEntityInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<GolemEntityInteraction> CODEC = BuilderCodec.builder(
                    GolemEntityInteraction.class, GolemEntityInteraction::new, SimpleInstantInteraction.CODEC
            )
            .documentation("Interacts with a golem entity")
            .build();

    @Override
    protected void firstRun(
            @NonNullDecl InteractionType type,
            @NonNullDecl InteractionContext context,
            @NonNullDecl CooldownHandler handler)
    {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        assert commandBuffer != null;
        Ref<EntityStore> targetRef = context.getTargetEntity();
        GolemEngine.LOGGER.atInfo().log("Interacted");
        if (targetRef == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("failed ref");
            return;
        }

        GolemActionComponent golem = commandBuffer.getComponent(targetRef, GolemActionComponent.getComponentType());
        if (golem == null) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("failed golem");
            return;
        }

        if (golem.inventory.isEmpty()) {
            context.getState().state = InteractionState.Failed;
            GolemEngine.LOGGER.atInfo().log("inv is empty");
            return;
        }
        GolemEngine.LOGGER.atInfo().log("went through");

        golem.preventPickupTicks = 600;

        for (ItemStack stack : golem.inventory) {
            ItemUtils.dropItem(targetRef, stack, commandBuffer);
        }

        golem.inventory.clear();
    }

}
