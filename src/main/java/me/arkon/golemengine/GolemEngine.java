package me.arkon.golemengine;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.arkon.golemengine.component.AnchorMonitorComponent;
import me.arkon.golemengine.component.AnchorStateComponent;
import me.arkon.golemengine.interaction.GolemAnchorInteraction;
import me.arkon.golemengine.interaction.GolemCrystalInteraction;
import me.arkon.golemengine.system.AnchorMonitorSystem;
import me.arkon.golemengine.system.BreakBlockSystem;
import me.arkon.golemengine.system.UseBlockSystem;

public class GolemEngine extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public GolemEngine(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC)
                .register("UseGolemAnchor", GolemAnchorInteraction.class, GolemAnchorInteraction.CODEC);

        AnchorStateComponent.TYPE = this.getChunkStoreRegistry().registerComponent(AnchorStateComponent.class, "GolemEngine_AnchorState", AnchorStateComponent.CODEC);
        AnchorMonitorComponent.TYPE = this.getEntityStoreRegistry().registerComponent(AnchorMonitorComponent.class, "GolemEngine_AnchorMonitor", AnchorMonitorComponent.CODEC);

        this.getCodecRegistry(Interaction.CODEC).register("UseGolemCrystal", GolemCrystalInteraction.class, GolemCrystalInteraction.CODEC);


        //this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
    }

    @Override
    protected void start() {
        this.getEntityStoreRegistry().registerSystem(new AnchorMonitorSystem());
        this.getEntityStoreRegistry().registerSystem(new BreakBlockSystem());
        this.getEntityStoreRegistry().registerSystem(new UseBlockSystem());
    }
}
