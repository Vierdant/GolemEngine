package me.arkon.golemengine.npc;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFind;
import me.arkon.golemengine.GolemEngine;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BuilderBodyMotionGolem extends BuilderBodyMotionFind {

    @Override
    public BodyMotionGolem build(@Nonnull BuilderSupport builderSupport) {
        return new BodyMotionGolem(this, builderSupport);
    }

    @Override
    public @NotNull String getShortDescription() {
        return "Process the moving action of a golem";
    }

    @Nonnull
    public BuilderBodyMotionGolem readConfig(@Nonnull JsonElement data) {
        super.readConfig(data);
        return this;
    }
}
