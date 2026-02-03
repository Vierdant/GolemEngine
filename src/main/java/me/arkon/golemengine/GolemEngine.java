package me.arkon.golemengine;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import it.unimi.dsi.fastutil.Pair;
import me.arkon.golemengine.component.PlayerMonitorComponent;
import me.arkon.golemengine.component.AnchorComponent;
import me.arkon.golemengine.component.GolemActionComponent;
import me.arkon.golemengine.interaction.GolemAnchorInteraction;
import me.arkon.golemengine.interaction.GolemCrystalInteraction;
import me.arkon.golemengine.interaction.GolemEntityInteraction;
import me.arkon.golemengine.npc.BuilderBodyMotionGolem;
import me.arkon.golemengine.system.AnchorMonitorSystem;
import me.arkon.golemengine.system.BreakBlockSystem;
import me.arkon.golemengine.system.GolemExecutionSystem;
import me.arkon.golemengine.system.UseBlockSystem;

public class GolemEngine extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static GolemEngine INSTANCE;

    public GolemEngine(JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC)
                .register("UseGolemAnchor", GolemAnchorInteraction.class, GolemAnchorInteraction.CODEC);

        AnchorComponent.TYPE = this.getChunkStoreRegistry().registerComponent(AnchorComponent.class, "GolemEngine_AnchorState", AnchorComponent.CODEC);
        PlayerMonitorComponent.TYPE = this.getEntityStoreRegistry().registerComponent(PlayerMonitorComponent.class, "GolemEngine_AnchorMonitor", PlayerMonitorComponent.CODEC);
        GolemActionComponent.TYPE = this.getEntityStoreRegistry().registerComponent(GolemActionComponent.class, "GolemEngine_GolemAction", GolemActionComponent.CODEC);

        this.getCodecRegistry(Interaction.CODEC).register("UseGolemCrystal", GolemCrystalInteraction.class, GolemCrystalInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("UseGolemEntity", GolemEntityInteraction.class, GolemEntityInteraction.CODEC);

        NPCPlugin.get().registerCoreComponentType("GolemAction", BuilderBodyMotionGolem::new);


        //this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
    }

    @Override
    protected void start() {
        this.getEntityStoreRegistry().registerSystem(new AnchorMonitorSystem());
        this.getEntityStoreRegistry().registerSystem(new BreakBlockSystem());
        this.getEntityStoreRegistry().registerSystem(new UseBlockSystem());
        this.getEntityStoreRegistry().registerSystem(new GolemExecutionSystem());
    }


    public static GolemEngine get() {
        return INSTANCE;
    }


    public void spawnGolemAtAnchor(Store<EntityStore> store, AnchorComponent anchor, PlayerMonitorComponent monitor) {
        NPCPlugin npcPlugin = NPCPlugin.get();

        int roleBuilderIndex = npcPlugin.getIndex("Golem_Construct");
        BuilderInfo roleInfo = npcPlugin.getRoleBuilderInfo(roleBuilderIndex);
        if (roleInfo == null) {
            GolemEngine.LOGGER.atSevere().log("Golem failed to spawn due to invalid role info fetch.");
            return;
        }
        Builder<Role> roleBuilder = npcPlugin.tryGetCachedValidRole(roleInfo.getIndex());

        if (!(roleBuilder instanceof ISpawnableWithModel spawnable) || !roleBuilder.isSpawnable()) {
            throw new IllegalStateException("Golem role is not spawnable");
        }

        SpawningContext spawningContext = new SpawningContext();
        if (!spawningContext.setSpawnable(spawnable)) {
            throw new GeneralCommandException(Message.translation("server.commands.npc.spawn.cantSetRolebuilder"));
        }

        Vector3d position = new Vector3d(monitor.getAnchorLocation());
        position.y += 0.5;

        Pair<Ref<EntityStore>, NPCEntity> npcPair =
                npcPlugin.spawnEntity(
                        store,
                        roleBuilderIndex,
                        position,
                        new Vector3f(),
                        spawningContext.getModel(),
                        null
                );

        assert npcPair != null;
        Ref<EntityStore> golemRef = npcPair.first();

        store.addComponent(golemRef, GolemActionComponent.getComponentType(), new GolemActionComponent(monitor.actions, monitor.getAnchorLocation()));

        Interactions interactions = new Interactions();
        interactions.setInteractionId(InteractionType.Use, "Golem_Entity");
        store.replaceComponent(golemRef, Interactions.getComponentType(), interactions);
        UUIDComponent uuid = store.getComponent(golemRef, UUIDComponent.getComponentType());
        if (uuid == null) return;

        anchor.golemUUID = uuid.getUuid();
    }
}
