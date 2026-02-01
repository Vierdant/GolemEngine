package me.arkon.golemengine.npc;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionFind;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFind;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import me.arkon.golemengine.GolemEngine;
import me.arkon.golemengine.component.GolemActionComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.logging.Level;

public class BodyMotionGolem extends BodyMotionFind {

    public BodyMotionGolem(@NotNull BuilderBodyMotionGolem builderBodyMotionGolem, @NotNull BuilderSupport support) {
        super(builderBodyMotionGolem, support);
    }

    @Override
    public boolean canSwitchToSteering(
            @Nonnull Ref<EntityStore> ref, @Nonnull MotionController motionController, @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());

        assert transformComponent != null;
        assert golem != null;

        if (!golem.moving) return false;

        Vector3d position = transformComponent.getPosition();
        if (motionController.waypointDistanceSquared(position, golem.target) > this.switchToSteeringDistanceSquared) {
            return false;
        } else {
            if (this.dbgMotionState) {
                NPCPlugin.get().getLogger().at(Level.INFO).every(100).log("MotionFind: computing canSwitchToSteering");
            }

            return this.canReachTarget(
                    ref, motionController, position, this.getLastAccessibleTargetPosition(motionController, true, componentAccessor), componentAccessor
            );
        }
    }

    @Override
    public boolean shouldSkipSteering(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull MotionController activeMotionController,
            @Nonnull Vector3d position,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());
        assert golem != null;
        if (!golem.moving) return false;
        this.probeMoveData.setPosition(position).setTargetPosition(golem.target);
        activeMotionController.probeMove(ref, this.probeMoveData, componentAccessor);
        return !this.isGoalReached(ref, activeMotionController, this.probeMoveData.probePosition, golem.target, componentAccessor);
    }

    @Override
    public boolean computeSteering(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Role role,
            @Nonnull Vector3d position,
            @Nonnull Steering desiredSteering,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());
        assert golem != null;
        if (!golem.moving) return true;
        this.seek.setPositions(position, golem.target);
        MotionController motionController = role.getActiveMotionController();
        this.seek.setComponentSelector(motionController.getComponentSelector());
        double desiredAltitudeWeight = this.desiredAltitudeWeight >= 0.0 ? this.desiredAltitudeWeight : motionController.getDesiredAltitudeWeight();
        return this.scaleSteering(ref, role, this.seek, desiredSteering, desiredAltitudeWeight, componentAccessor);
    }

    @Override
    public boolean canComputeMotion(
            @Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider infoProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());
        assert golem != null;
        if (!golem.moving) return false;
        if (super.canComputeMotion(ref, role, infoProvider, componentAccessor)
                && (
                !(this.abortDistance > 0.0)
                        || !(role.getActiveMotionController().waypointDistanceSquared(ref, golem.target, componentAccessor) >= this.abortDistanceSquared)
        )) {
            if (this.selfBoundingBox != null && this.adjustRangeByHitboxSize) {
                double effectiveDistance = this.distance + Math.max(this.selfBoundingBox.width(), this.selfBoundingBox.depth());
                if (this.targetBoundingBox != null) {
                    effectiveDistance += Math.max(this.targetBoundingBox.width(), this.targetBoundingBox.depth());
                }

                this.effectiveDistanceSquared = effectiveDistance * effectiveDistance;
            } else {
                this.effectiveDistanceSquared = this.distanceSquared;
            }

            return this.selfBoundingBox != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean isGoalReached(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull MotionController motionController,
            @Nonnull Vector3d position,
            @Nonnull Vector3d targetPosition,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        TransformComponent transform = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());
        assert golem != null;
        if (!golem.moving) return true;
        targetPosition = golem.target;
        double differenceY = targetPosition.y - position.y;
        if (!(differenceY < this.heightDifferenceMin) && !(differenceY > this.heightDifferenceMax)) {
            boolean reached = !(motionController.waypointDistanceSquared(position, targetPosition) > this.effectiveDistanceSquared) && (!this.reachable || this.canReachTarget(ref, motionController, position, targetPosition, componentAccessor));
            if (this.containsPosition(transform.getPosition(), targetPosition)) {
                golem.moving = false;
                golem.target = new Vector3d();
                golem.direction = new Vector3d();
            }
            return reached;
        } else {
            return false;
        }
    }

    public void lookAtTarget(@Nonnull Ref<EntityStore> ref, @Nonnull Steering steering, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

        assert transformComponent != null;

        GolemActionComponent golem = componentAccessor.getComponent(ref, GolemActionComponent.getComponentType());
        assert golem != null;
        if (!golem.moving) return;

        Vector3d position = transformComponent.getPosition();
        Vector3f bodyRotation = transformComponent.getRotation();
        this.tempDirectionVector.assign(golem.target).subtract(position);
        steering.setYaw(NPCPhysicsMath.headingFromDirection(this.tempDirectionVector.x, this.tempDirectionVector.z, bodyRotation.getYaw()));
        steering.setPitch(
                NPCPhysicsMath.pitchFromDirection(this.tempDirectionVector.x, this.tempDirectionVector.y, this.tempDirectionVector.z, bodyRotation.getPitch())
        );
    }

    public boolean containsPosition(@Nonnull Vector3d position, @Nonnull Vector3d endPosition) {
        return containsPosition(position.x, this.selfBoundingBox.min.x, this.selfBoundingBox.max.x, endPosition.x)
                && containsPosition(position.y, this.selfBoundingBox.min.y, this.selfBoundingBox.max.y, endPosition.y)
                && containsPosition(position.z, this.selfBoundingBox.min.z, this.selfBoundingBox.max.z, endPosition.z);
    }

    public static boolean containsPosition(double p, double min, double max, double v) {
        v -= p;
        return v >= min && v < max;
    }
}
