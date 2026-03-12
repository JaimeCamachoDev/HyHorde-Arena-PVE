package com.hyhorde.arenapve.horde;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HordeHudSystem
extends EntityTickingSystem<EntityStore> {
    private static final Logger LOGGER = Logger.getLogger(HordeHudSystem.class.getName());
    private static final Query<EntityStore> QUERY = Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
    private static final int UPDATE_INTERVAL = 20;
    private final HordeService hordeService;
    private final Map<UUID, HordeRuntimeHud> huds;
    private final Map<UUID, Integer> tickCounters;

    public HordeHudSystem(HordeService hordeService) {
        this.hordeService = hordeService;
        this.huds = new ConcurrentHashMap<UUID, HordeRuntimeHud>();
        this.tickCounters = new ConcurrentHashMap<UUID, Integer>();
    }

    public void tick(float deltaTime, int tickCounter, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        EntityStore entityStore = (EntityStore)store.getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        // Known issue history (important for future maintenance):
        // - Client disconnect: "Failed to apply CustomUI HUD commands".
        // - Most frequent trigger: two plugins sending CustomUIHud commands to the same player
        //   at the same time (ownership conflict), especially with EconomySystem.
        // Mitigation strategy in this system:
        // 1) Keep updates in world tick only.
        // 2) Never force ownership if another mod already owns CustomUIHud.
        // 3) Never clear with setCustomHud(null) directly (use empty HUD payload instead).
        boolean hordeActiveInWorld = world != null && this.hordeService.isTrackingWorld(world);
        for (int entityIndex = 0; entityIndex < chunk.size(); ++entityIndex) {
            Player player = (Player)chunk.getComponent(entityIndex, Player.getComponentType());
            PlayerRef playerRef = (PlayerRef)chunk.getComponent(entityIndex, PlayerRef.getComponentType());
            if (player == null || playerRef == null || playerRef.getUuid() == null) {
                continue;
            }
            UUID playerId = playerRef.getUuid();
            boolean shouldShowHud = hordeActiveInWorld && this.hordeService.isArenaAudience(playerRef);
            HordeRuntimeHud trackedHud = this.huds.get(playerId);
            if (!shouldShowHud) {
                if (trackedHud != null || this.tickCounters.containsKey(playerId)) {
                    this.removeHudForPlayer(player, playerRef, playerId, trackedHud);
                } else {
                    this.tickCounters.remove(playerId);
                }
                continue;
            }
            if (trackedHud == null) {
                trackedHud = this.createHudForPlayer(player, playerRef, playerId);
                if (trackedHud == null) {
                    continue;
                }
            }
            if (player.getHudManager().getCustomHud() != trackedHud) {
                // Another plugin took ownership of the custom HUD slot.
                // Stop updating ours to avoid conflicting command streams.
                this.removeTrackingOnly(playerId);
                continue;
            }
            int localCounter = this.tickCounters.getOrDefault(playerId, 0) + 1;
            if (localCounter < UPDATE_INTERVAL) {
                this.tickCounters.put(playerId, localCounter);
                continue;
            }
            this.tickCounters.put(playerId, 0);
            try {
                trackedHud.setSnapshot(this.hordeService.getStatusSnapshot());
                trackedHud.updateHud();
            }
            catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to update Horde HUD for player: " + playerRef.getUsername(), ex);
                this.removeTrackingOnly(playerId);
            }
        }
    }

    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    private HordeRuntimeHud createHudForPlayer(Player player, PlayerRef playerRef, UUID playerId) {
        try {
            CustomUIHud currentHud = player.getHudManager().getCustomHud();
            if (currentHud instanceof HordeRuntimeHud) {
                HordeRuntimeHud existing = (HordeRuntimeHud)currentHud;
                this.huds.put(playerId, existing);
                this.tickCounters.put(playerId, 0);
                return existing;
            }
            if (currentHud instanceof EmptyRuntimeHud) {
                // Internal clear marker from previous horde end. Safe to replace with runtime HUD.
                currentHud = null;
            }
            if (currentHud != null) {
                // Compatibility guard:
                // another plugin already owns CustomUIHud for this player (EconomySystem, etc).
                // Forcing takeover can cause conflicting command streams and client disconnects.
                // In that case we skip Horde HUD for this player to preserve server stability.
                return null;
            }
            HordeRuntimeHud hud = new HordeRuntimeHud(playerRef, this.hordeService);
            player.getHudManager().setCustomHud(playerRef, (CustomUIHud)hud);
            hud.setSnapshot(this.hordeService.getStatusSnapshot());
            hud.updateHud();
            this.huds.put(playerId, hud);
            this.tickCounters.put(playerId, 0);
            return hud;
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to create Horde HUD for player: " + playerRef.getUsername(), ex);
            return null;
        }
    }

    private void removeHudForPlayer(Player player, PlayerRef playerRef, UUID playerId, HordeRuntimeHud trackedHud) {
        this.huds.remove(playerId);
        this.tickCounters.remove(playerId);
        if (trackedHud == null) {
            return;
        }
        try {
            CustomUIHud currentHud = player.getHudManager().getCustomHud();
            if (currentHud != trackedHud) {
                return;
            }
            // Avoid setCustomHud(null): some client builds can crash applying null custom HUD payloads.
            // We clear safely by attaching an empty HUD instead.
            player.getHudManager().setCustomHud(playerRef, (CustomUIHud)new EmptyRuntimeHud(playerRef));
        }
        catch (Exception ex) {
            LOGGER.log(Level.FINE, "Failed to clear Horde HUD binding for player: " + playerRef.getUsername(), ex);
        }
    }

    private void removeTrackingOnly(UUID playerId) {
        this.huds.remove(playerId);
        this.tickCounters.remove(playerId);
    }

    private static final class EmptyRuntimeHud
    extends CustomUIHud {
        private EmptyRuntimeHud(PlayerRef playerRef) {
            super(playerRef);
        }

        protected void build(UICommandBuilder commandBuilder) {
            // Intentionally empty. Sends an overwrite packet with no UI commands.
        }
    }

    private static final class HordeRuntimeHud
    extends CustomUIHud {
        private static final String LAYOUT = "Hud/HordeArenaHud.ui";
        private final HordeService hordeService;
        private HordeService.StatusSnapshot snapshot;

        private HordeRuntimeHud(PlayerRef playerRef, HordeService hordeService) {
            super(playerRef);
            this.hordeService = hordeService;
            this.snapshot = hordeService.getStatusSnapshot();
        }

        private void setSnapshot(HordeService.StatusSnapshot snapshot) {
            this.snapshot = snapshot == null ? this.hordeService.getStatusSnapshot() : snapshot;
        }

        protected void build(UICommandBuilder commandBuilder) {
            commandBuilder.append(LAYOUT);
            this.updateHudValues(commandBuilder);
        }

        private void updateHud() {
            UICommandBuilder commandBuilder = new UICommandBuilder();
            try {
                this.updateHudValues(commandBuilder);
                this.update(false, commandBuilder);
            }
            catch (Exception ex) {
                UICommandBuilder rebuildBuilder = new UICommandBuilder();
                try {
                    this.build(rebuildBuilder);
                    this.update(true, rebuildBuilder);
                }
                catch (Exception ignored) {
                    // Keep HUD silent if update fails repeatedly.
                }
            }
        }

        private void updateHudValues(UICommandBuilder commandBuilder) {
            HordeService.StatusSnapshot status = this.snapshot == null ? this.hordeService.getStatusSnapshot() : this.snapshot;
            boolean english = HordeService.isEnglishLanguage(status.language);
            String worldText = status.worldName == null || status.worldName.isBlank() ? "default" : status.worldName;
            String stateLine = english ? "State: " + (status.active ? "Active" : "Inactive") + " | World: " + worldText : "Estado: " + (status.active ? "Activa" : "Inactiva") + " | Mundo: " + worldText;
            String roundLine = english ? "Round: " + status.currentRound + "/" + status.totalRounds : "Ronda: " + status.currentRound + "/" + status.totalRounds;
            String enemiesLine = english ? "Enemies alive: " + status.aliveEnemies : "Enemigos vivos: " + status.aliveEnemies;
            String killsLine = english ? "Kills: " + status.totalKilled + " | Deaths: " + status.totalDeaths : "Bajas: " + status.totalKilled + " | Muertes: " + status.totalDeaths;
            String nextLine = english ? "Next round: " + (status.nextRoundInSeconds > 0L ? status.nextRoundInSeconds + "s" : "-") : "Siguiente: " + (status.nextRoundInSeconds > 0L ? status.nextRoundInSeconds + "s" : "-");
            String rewardLine = this.buildRewardLine(english);
            commandBuilder.set("#TitleLabel.Text", english ? "HORDE PVE" : "HORDA PVE").set("#StateLine.Text", stateLine).set("#RoundLine.Text", roundLine).set("#EnemiesLine.Text", enemiesLine).set("#KillsLine.Text", killsLine).set("#NextLine.Text", nextLine).set("#RewardLine.Text", rewardLine);
        }

        private String buildRewardLine(boolean english) {
            HordeService.HordeConfig config = this.hordeService.getConfigSnapshot();
            String itemId = config.rewardItemId == null ? "" : config.rewardItemId.trim();
            int quantity = Math.max(1, config.rewardItemQuantity);
            String mode;
            if (itemId.isBlank()) {
                mode = english ? "none" : "ninguno";
            } else if ("random".equalsIgnoreCase(itemId)) {
                mode = english ? "random" : "aleatorio";
            } else {
                mode = itemId;
            }
            if (english) {
                return "Reward: " + mode + " x" + quantity + " | Every " + config.rewardEveryRounds + " round(s)";
            }
            return "Recompensa: " + mode + " x" + quantity + " | Cada " + config.rewardEveryRounds + " ronda(s)";
        }
    }
}
