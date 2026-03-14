package com.hyhorde.arenapve.horde;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class HordeConfigPage
extends CustomUIPage {
    private static final String LAYOUT = "Pages/HordeConfigPage.ui";
    private static final String TAB_GENERAL = "general";
    private static final String TAB_HORDE = "horde";
    private static final String TAB_PLAYERS = "players";
    private static final String TAB_SOUNDS = "sounds";
    private static final String TAB_REWARDS = "rewards";
    private static final String TAB_HELP = "help";
    private static final int MAX_AUDIENCE_ROWS = Integer.MAX_VALUE;
    private static final UiFieldBinding[] SNAPSHOT_FIELDS = new UiFieldBinding[]{
            new UiFieldBinding("spawnX", "SpawnX", "#SpawnX.Value"),
            new UiFieldBinding("spawnY", "SpawnY", "#SpawnY.Value"),
            new UiFieldBinding("spawnZ", "SpawnZ", "#SpawnZ.Value"),
            new UiFieldBinding("minRadius", "MinRadius", "#MinRadius.Value"),
            new UiFieldBinding("maxRadius", "MaxRadius", "#MaxRadius.Value"),
            new UiFieldBinding("arenaJoinRadius", "ArenaJoinRadius", "#ArenaJoinRadius.Value"),
            new UiFieldBinding("rounds", "Rounds", "#Rounds.Value"),
            new UiFieldBinding("baseEnemies", "BaseEnemies", "#BaseEnemies.Value"),
            new UiFieldBinding("enemiesPerRound", "EnemiesPerRound", "#EnemiesPerRound.Value"),
            new UiFieldBinding("waveDelay", "WaveDelay", "#WaveDelay.Value"),
            new UiFieldBinding("enemyType", "EnemyType", "#EnemyType.Value", "role", "@Role", "Role"),
            new UiFieldBinding("language", "Language", "#Language.Value"),
            new UiFieldBinding("rewardCategory", "RewardCategory", "#RewardCategory.Value"),
            new UiFieldBinding("rewardItemId", "RewardItemId", "#RewardItemId.Value"),
            new UiFieldBinding("rewardItemQuantity", "RewardItemQuantity", "#RewardItemQuantity.Value"),
            new UiFieldBinding("finalBossEnabled", "FinalBossEnabled", "#FinalBossEnabled.Value"),
            new UiFieldBinding("roundStartSoundId", "RoundStartSoundId", "#RoundStartSoundId.Value"),
            new UiFieldBinding("roundVictorySoundId", "RoundVictorySoundId", "#RoundVictorySoundId.Value")
    };
    private final HordeService hordeService;
    private String activeTab;

    private HordeConfigPage(PlayerRef playerRef, HordeService hordeService) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.hordeService = hordeService;
        this.activeTab = TAB_GENERAL;
    }

    public static void open(Ref<EntityStore> playerEntityRef, Store<EntityStore> store, Player player, PlayerRef playerRef, HordeService hordeService) {
        HordeConfigPage page = new HordeConfigPage(playerRef, hordeService);
        player.getPageManager().openCustomPage(playerEntityRef, store, (CustomUIPage)page);
    }

    public void build(Ref<EntityStore> playerEntityRef, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        HordeService.HordeConfig config = this.hordeService.getConfigSnapshot();
        boolean english = HordeService.isEnglishLanguage(config.language);
        boolean active = this.hordeService.isActive();
        List<String> enemyTypeOptions = this.hordeService.getEnemyTypeOptionsForCurrentRoles();
        List<String> rewardCategoryOptions = this.hordeService.getRewardCategoryOptions();
        List<String> roundStartSoundOptions = this.hordeService.getRoundStartSoundOptions();
        List<String> roundVictorySoundOptions = this.hordeService.getRoundVictorySoundOptions();
        String rewardCategory = HordeConfigPage.firstNonEmpty(config.rewardCategory, this.hordeService.getRewardCategory());
        List<String> rewardItemSuggestions = this.hordeService.getRewardItemSuggestions(rewardCategory);
        String enemyTypeValue = config.enemyType == null ? "undead" : config.enemyType;
        List<DropdownEntryInfo> enemyTypeEntries = HordeConfigPage.buildDropdownEntries(enemyTypeOptions, enemyTypeValue);
        List<DropdownEntryInfo> rewardCategoryEntries = HordeConfigPage.buildDropdownEntries(rewardCategoryOptions, rewardCategory);
        List<DropdownEntryInfo> rewardItemEntries = HordeConfigPage.buildDropdownEntries(rewardItemSuggestions, config.rewardItemId);
        List<DropdownEntryInfo> roundStartSoundEntries = HordeConfigPage.buildDropdownEntries(roundStartSoundOptions, this.hordeService.getRoundStartSoundSelection());
        List<DropdownEntryInfo> roundVictorySoundEntries = HordeConfigPage.buildDropdownEntries(roundVictorySoundOptions, this.hordeService.getRoundVictorySoundSelection());
        String tab = HordeConfigPage.normalizeTab(this.activeTab);
        this.activeTab = tab;
        EntityStore entityStore = (EntityStore)store.getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        List<HordeService.AudiencePlayerSnapshot> audienceRows = world == null ? List.of() : this.hordeService.getArenaAudiencePlayers(world);
        commandBuilder.append(LAYOUT)
                .set("#SpawnX.Value", HordeConfigPage.formatDouble(config.spawnX))
                .set("#SpawnY.Value", HordeConfigPage.formatDouble(config.spawnY))
                .set("#SpawnZ.Value", HordeConfigPage.formatDouble(config.spawnZ))
                .set("#EnemyType.Value", enemyTypeValue)
                .set("#EnemyType.Entries", enemyTypeEntries)
                .set("#Language.Value", HordeService.normalizeLanguage(config.language))
                .set("#RewardCategory.Value", rewardCategory)
                .set("#RewardCategory.Entries", rewardCategoryEntries)
                .set("#RewardItemId.Value", config.rewardItemId == null ? "" : config.rewardItemId)
                .set("#RewardItemId.Entries", rewardItemEntries)
                .set("#FinalBossEnabled.Value", config.finalBossEnabled)
                .set("#RoundStartSoundId.Value", this.hordeService.getRoundStartSoundSelection())
                .set("#RoundStartSoundId.Entries", roundStartSoundEntries)
                .set("#RoundVictorySoundId.Value", this.hordeService.getRoundVictorySoundSelection())
                .set("#RoundVictorySoundId.Entries", roundVictorySoundEntries)
                .set("#EnemyLevelMin.Value", Integer.toString(config.enemyLevelMin))
                .set("#EnemyLevelMax.Value", Integer.toString(config.enemyLevelMax))
                .set("#AudienceInfoLabel.Text", HordeConfigPage.buildAudienceInfo(config.arenaJoinRadius, audienceRows.size(), english))
                .set("#PlayersCountValue.Text", Integer.toString(audienceRows.size()))
                .set("#PlayersListHint.Text", HordeConfigPage.buildAudienceRowsHint(audienceRows.size(), english))
                .set("#AudiencePlayersEmptyLabel.Text", audienceRows.isEmpty() ? (english ? "No players detected in the current arena radius." : "No hay jugadores detectados en el radio actual de arena.") : "")
                .set("#SpawnStateLabel.Text", HordeConfigPage.buildSpawnLabel(config, english))
                .set("#ReloadModButton.Visible", true)
                .set("#StartButton.Visible", !active)
                .set("#StopButton.Visible", active)
                .set("#SkipRoundButton.Visible", active);
        this.setLocalizedTexts(commandBuilder, english);
        this.applyTabVisibility(commandBuilder, tab);
        this.populateAudienceRows(commandBuilder, eventBuilder, audienceRows, english);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of((String)"action", (String)"close"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabGeneralButton", EventData.of((String)"action", (String)"tab_general"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabHordeButton", EventData.of((String)"action", (String)"tab_horde"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabPlayersButton", EventData.of((String)"action", (String)"tab_players"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabSoundsButton", EventData.of((String)"action", (String)"tab_sounds"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabRewardsButton", EventData.of((String)"action", (String)"tab_rewards"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#TabHelpButton", EventData.of((String)"action", (String)"tab_help"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#SetSpawnButton", EventData.of((String)"action", (String)"set_spawn_here"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#PlayersRefreshButton", EventData.of((String)"action", (String)"refresh_players"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#ReloadModButton", EventData.of((String)"action", (String)"reload_config"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", this.buildConfigSnapshotEvent("save"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#StartButton", this.buildConfigSnapshotEvent("start"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#StopButton", EventData.of((String)"action", (String)"stop"))
                .addEventBinding(CustomUIEventBindingType.Activating, "#SkipRoundButton", EventData.of((String)"action", (String)"skip_round"));
    }

    public void handleDataEvent(Ref<EntityStore> playerEntityRef, Store<EntityStore> store, String payloadText) {
        JsonObject payload;
        boolean english = this.isEnglish();
        try {
            payload = JsonParser.parseString((String)payloadText).getAsJsonObject();
        }
        catch (Exception ex) {
            this.playerRef.sendMessage(Message.raw((String)(english ? "Could not parse the UI event payload." : "No se pudo interpretar el evento de la UI.")));
            return;
        }
        try {
            String action = HordeConfigPage.read(payload, "action");
            EntityStore entityStore = (EntityStore)store.getExternalData();
            World world = entityStore == null ? null : entityStore.getWorld();
            if (world == null && HordeConfigPage.requiresWorld(action)) {
                this.playerRef.sendMessage(Message.raw((String)(english ? "Could not access the active world to process this UI action." : "No se pudo acceder al mundo actual para procesar la accion de UI.")));
                this.safeRebuild();
                return;
            }
            HordeService.OperationResult result = null;
            switch (action) {
                case "close": {
                    this.close();
                    return;
                }
                case "tab_general": {
                    this.activeTab = TAB_GENERAL;
                    break;
                }
                case "tab_horde": {
                    this.activeTab = TAB_HORDE;
                    break;
                }
                case "tab_players": {
                    this.activeTab = TAB_PLAYERS;
                    break;
                }
                case "tab_sounds": {
                    this.activeTab = TAB_SOUNDS;
                    break;
                }
                case "tab_rewards": {
                    this.activeTab = TAB_REWARDS;
                    break;
                }
                case "tab_help": {
                    this.activeTab = TAB_HELP;
                    break;
                }
                case "set_spawn_here": {
                    result = this.hordeService.setSpawnFromPlayer(this.playerRef, world);
                    break;
                }
                case "refresh_players": {
                    break;
                }
                case "reload_config":
                case "reload_mod": {
                    result = this.hordeService.reloadConfigFromDisk();
                    break;
                }
                case "save": {
                    result = this.hordeService.applyUiConfig(HordeConfigPage.extractConfigValues(payload), world);
                    break;
                }
                case "start": {
                    result = this.hordeService.applyUiConfig(HordeConfigPage.extractConfigValues(payload), world);
                    if (!result.isSuccess()) break;
                    result = this.hordeService.start(store, this.playerRef, world);
                    break;
                }
                case "stop": {
                    result = this.hordeService.stop(true);
                    break;
                }
                case "skip_round": {
                    result = this.hordeService.skipToNextRound(world);
                    break;
                }
                default: {
                    result = this.handleAudienceAction(action, world, english);
                }
            }
            if (result != null) {
                this.playerRef.sendMessage(Message.raw((String)result.getMessage()));
            }
        }
        catch (Exception ex) {
            this.playerRef.sendMessage(Message.raw((String)(english ? "Internal error while processing horde UI. Check server logs and try again." : "Error interno al procesar la UI de horda. Revisa logs e intenta de nuevo.")));
        }
        this.safeRebuild();
    }

    private static boolean requiresWorld(String action) {
        if (action == null || action.isBlank()) {
            return false;
        }
        if (action.startsWith("audience_set:")) {
            return true;
        }
        switch (action) {
            case "set_spawn_here":
            case "save":
            case "skip_round":
            case "start": {
                return true;
            }
        }
        return false;
    }

    private HordeService.OperationResult handleAudienceAction(String action, World world, boolean english) {
        if (action == null || !action.startsWith("audience_set:")) {
            return HordeService.OperationResult.fail(english ? "Unknown UI action: " + action : "Accion de UI desconocida: " + action);
        }
        String[] parts = action.split(":", 3);
        if (parts.length != 3) {
            return HordeService.OperationResult.fail(english ? "Invalid audience action payload." : "Accion de audiencia invalida.");
        }
        String mode = parts[1];
        String rawPlayerId = parts[2];
        UUID playerId;
        try {
            playerId = UUID.fromString(rawPlayerId);
        }
        catch (Exception ignored) {
            return HordeService.OperationResult.fail(english ? "Could not parse selected player UUID." : "No se pudo interpretar el UUID del jugador seleccionado.");
        }
        return this.hordeService.setArenaAudienceMode(playerId, mode, world);
    }

    private void safeRebuild() {
        try {
            this.rebuild();
        }
        catch (Exception ignored) {
            // avoid bubbling UI rebuild failures to the caller thread
        }
    }

    private EventData buildConfigSnapshotEvent(String action) {
        EventData eventData = EventData.of((String)"action", (String)action);
        for (UiFieldBinding field : SNAPSHOT_FIELDS) {
            eventData = eventData.append("@" + field.payloadAlias, field.uiValueSelector);
        }
        return eventData;
    }

    private void populateAudienceRows(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, List<HordeService.AudiencePlayerSnapshot> rows, boolean english) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        int rowIndex = 0;
        for (HordeService.AudiencePlayerSnapshot row : rows) {
            if (row == null || row.playerId == null || rowIndex >= MAX_AUDIENCE_ROWS) continue;
            commandBuilder.append("#AudiencePlayersRows", "Pages/HordeAudiencePlayerRow.ui");
            String mode = HordeConfigPage.normalizeAudienceMode(row.mode);
            boolean playerMode = "player".equals(mode);
            boolean spectatorMode = "spectator".equals(mode);
            boolean exitMode = "exit".equals(mode);
            commandBuilder.set("#AudiencePlayersRows[" + rowIndex + "] #RowName.Text", HordeConfigPage.compactName(row.username, 30))
                    .set("#AudiencePlayersRows[" + rowIndex + "] #RowMode.Text", HordeConfigPage.audienceModeDisplay(mode, english))
                    .set("#AudiencePlayersRows[" + rowIndex + "] #SetPlayerButton.Text", playerMode ? (english ? "Player *" : "Jugador *") : (english ? "Player" : "Jugador"))
                    .set("#AudiencePlayersRows[" + rowIndex + "] #SetSpectatorButton.Text", spectatorMode ? (english ? "Spectator *" : "Espectador *") : (english ? "Spectator" : "Espectador"))
                    .set("#AudiencePlayersRows[" + rowIndex + "] #SetExitButton.Text", exitMode ? (english ? "Exit *" : "Salir *") : (english ? "Exit" : "Salir"));
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#AudiencePlayersRows[" + rowIndex + "] #SetPlayerButton", EventData.of((String)"action", (String)HordeConfigPage.buildAudienceAction("player", row.playerId)))
                    .addEventBinding(CustomUIEventBindingType.Activating, "#AudiencePlayersRows[" + rowIndex + "] #SetSpectatorButton", EventData.of((String)"action", (String)HordeConfigPage.buildAudienceAction("spectator", row.playerId)))
                    .addEventBinding(CustomUIEventBindingType.Activating, "#AudiencePlayersRows[" + rowIndex + "] #SetExitButton", EventData.of((String)"action", (String)HordeConfigPage.buildAudienceAction("exit", row.playerId)));
            ++rowIndex;
        }
        int hiddenRows = Math.max(0, rows.size() - MAX_AUDIENCE_ROWS);
        if (hiddenRows > 0) {
            commandBuilder.set("#PlayersListHint.Text", english ? "Showing first " + MAX_AUDIENCE_ROWS + " players (" + hiddenRows + " more not shown)." : "Mostrando primeros " + MAX_AUDIENCE_ROWS + " jugadores (" + hiddenRows + " mas sin mostrar).");
        }
    }

    private static String buildAudienceAction(String mode, UUID playerId) {
        return "audience_set:" + mode + ":" + playerId;
    }

    private static Map<String, String> extractConfigValues(JsonObject payload) {
        HashMap<String, String> values = new HashMap<String, String>();
        for (UiFieldBinding field : SNAPSHOT_FIELDS) {
            values.put(field.configKey, HordeConfigPage.extractFieldValue(payload, field));
        }
        values.put("rewardEveryRounds", HordeConfigPage.firstNonEmpty(HordeConfigPage.read(payload, "rewardEveryRounds"), HordeConfigPage.read(payload, "@RewardEveryRounds"), HordeConfigPage.read(payload, "RewardEveryRounds")));
        values.put("enemyLevelMin", HordeConfigPage.firstNonEmpty(HordeConfigPage.read(payload, "enemyLevelMin"), HordeConfigPage.read(payload, "@EnemyLevelMin"), HordeConfigPage.read(payload, "EnemyLevelMin")));
        values.put("enemyLevelMax", HordeConfigPage.firstNonEmpty(HordeConfigPage.read(payload, "enemyLevelMax"), HordeConfigPage.read(payload, "@EnemyLevelMax"), HordeConfigPage.read(payload, "EnemyLevelMax")));
        return values;
    }

    private static String extractFieldValue(JsonObject payload, UiFieldBinding field) {
        String value = HordeConfigPage.firstNonEmpty(HordeConfigPage.read(payload, field.configKey), HordeConfigPage.read(payload, "@" + field.payloadAlias), HordeConfigPage.read(payload, field.payloadAlias));
        if (field.extraPayloadKeys != null) {
            for (String key : field.extraPayloadKeys) {
                value = HordeConfigPage.firstNonEmpty(value, HordeConfigPage.read(payload, key));
            }
        }
        return value;
    }

    private static String buildSpawnLabel(HordeService.HordeConfig config, boolean english) {
        if (!config.spawnConfigured) {
            if (english) {
                return "Horde center not configured. You can use your current position.";
            }
            return "Centro de horda no configurado. Puedes usar tu posicion actual.";
        }
        if (english) {
            return String.format(Locale.ROOT, "Current center: %.2f %.2f %.2f | World: %s", config.spawnX, config.spawnY, config.spawnZ, config.worldName);
        }
        return String.format(Locale.ROOT, "Centro actual: %.2f %.2f %.2f | Mundo: %s", config.spawnX, config.spawnY, config.spawnZ, config.worldName);
    }

    private static List<DropdownEntryInfo> buildDropdownEntries(List<String> options, String selectedValue) {
        ArrayList<String> values = new ArrayList<String>();
        if (options != null) {
            for (String option : options) {
                if (option == null) continue;
                String trimmed = option.trim();
                if (trimmed.isBlank()) continue;
                if (HordeConfigPage.containsIgnoreCase(values, trimmed)) continue;
                values.add(trimmed);
            }
        }
        String selected = selectedValue == null ? "" : selectedValue.trim();
        if (!selected.isBlank() && !HordeConfigPage.containsIgnoreCase(values, selected)) {
            values.add(0, selected);
        }
        ArrayList<DropdownEntryInfo> entries = new ArrayList<DropdownEntryInfo>(values.size());
        for (String value : values) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(value), value));
        }
        return entries;
    }

    private static boolean containsIgnoreCase(List<String> values, String value) {
        if (values == null || values.isEmpty() || value == null) {
            return false;
        }
        for (String candidate : values) {
            if (candidate == null || !candidate.equalsIgnoreCase(value)) continue;
            return true;
        }
        return false;
    }

    private boolean isEnglish() {
        return HordeService.isEnglishLanguage(this.hordeService.getLanguage());
    }

    private void setLocalizedTexts(UICommandBuilder commandBuilder, boolean english) {
        commandBuilder.set("#TitleLabel.Text", english ? "Horde PVE Config" : "Horda PVE Config")
                .set("#SubTitleLabel.Text", "")
                .set("#TabGeneralButton.Text", english ? "General" : "General")
                .set("#TabHordeButton.Text", english ? "Horde" : "Horda")
                .set("#TabPlayersButton.Text", english ? "Players" : "Jugadores")
                .set("#TabSoundsButton.Text", english ? "Sounds" : "Sonidos")
                .set("#TabRewardsButton.Text", english ? "Rewards" : "Recompensas")
                .set("#TabHelpButton.Text", english ? "Help" : "Ayuda")
                .set("#TabHintLabel.Text", "")
                .set("#SpawnLabel.Text", english ? "Center (X Y Z)" : "Centro (X Y Z)")
                .set("#SetSpawnButton.Text", english ? "Use my current position" : "Usar mi posicion actual")
                .set("#RadiusLabel.Text", english ? "Spawn radius setup" : "Configuracion de radio")
                .set("#MinRadiusLabel.Text", english ? "Minimum radius" : "Radio minimo")
                .set("#MaxRadiusLabel.Text", english ? "Maximum radius" : "Radio maximo")
                .set("#ArenaJoinRadiusLabel.Text", english ? "Players area radius" : "Radio de jugadores")
                .set("#PlayersListTitle.Text", english ? "Players inside current area" : "Jugadores dentro del area actual")
                .set("#PlayersCountLabel.Text", english ? "Detected" : "Detectados")
                .set("#PlayersHeaderName.Text", english ? "Player" : "Jugador")
                .set("#PlayersHeaderMode.Text", english ? "Mode" : "Modo")
                .set("#PlayersRefreshButton.Text", english ? "Refresh list" : "Actualizar lista")
                .set("#AudienceHelpLabel.Text", english ? "Changes apply to next start. If horde is active, they are applied to current lock immediately." : "Cambios aplican al siguiente inicio. Si la horda esta activa, se aplican al bloqueo actual.")
                .set("#RoundLabel.Text", english ? "Rounds" : "Rondas")
                .set("#BaseEnemiesLabel.Text", english ? "Base / round" : "Base ronda")
                .set("#EnemiesPerRoundLabel.Text", english ? "Inc. per round" : "Inc. por ronda")
                .set("#WaveDelayLabel.Text", english ? "Delay (s)" : "Espera (s)")
                .set("#RoleLabel.Text", english ? "Horde category" : "Categoria de horda")
                .set("#LanguageLabel.Text", english ? "Interface language" : "Idioma interfaz")
                .set("#EnemyLevelRangeLabel.Text", english ? "Enemy level range" : "Rango nivel enemigos")
                .set("#EnemyLevelWipLabel.Text", english ? "WIP: this system is temporarily disabled." : "WIP: este sistema esta desactivado temporalmente.")
                .set("#RewardEveryRoundsLabel.Text", english ? "Reward every round(s)" : "Recompensa por ronda(s)")
                .set("#RewardCategoryLabel.Text", english ? "Reward category" : "Categoria recompensa")
                .set("#RewardCommandsLabel.Text", english ? "Reward item" : "Item recompensa")
                .set("#RewardItemQuantityLabel.Text", english ? "Qty." : "Cant.")
                .set("#FinalBossLabel.Text", english ? "Final boss" : "Boss final")
                .set("#RoundStartSoundLabel.Text", english ? "Round start sound" : "Sonido inicio ronda")
                .set("#RoundVictorySoundLabel.Text", english ? "Round victory sound" : "Sonido victoria ronda")
                .set("#StatusTitleLabel.Text", "")
                .set("#ReloadModButton.Text", english ? "Reload config" : "Recargar config")
                .set("#SaveButton.Text", english ? "Save config" : "Guardar config")
                .set("#StartButton.Text", english ? "Start horde" : "Iniciar horda")
                .set("#StopButton.Text", english ? "Stop horde" : "Detener horda")
                .set("#SkipRoundButton.Text", english ? "Skip round" : "Pasar ronda")
                .set("#HelpIntroLabel.Text", english ? "Quick guide for Horde PVE usage" : "Guia rapida para usar Horde PVE")
                .set("#HelpCommandsLabel.Text", english ? "Main commands" : "Comandos principales")
                .set("#HelpCommandsLine1.Text", english ? "/hordeconfig (aliases: /hconfig /hordecfg /hordepve /spawnve /spawnpve): open config UI." : "/hordeconfig (alias: /hconfig /hordecfg /hordepve /spawnve /spawnpve): abre la UI.")
                .set("#HelpCommandsLine2.Text", english ? "/hordeconfig start | stop | status | logs | setspawn | reload." : "/hordeconfig start | stop | status | logs | setspawn | reload.")
                .set("#HelpCommandsLine3.Text", english ? "/hordeconfig enemy <type> | enemytypes | role <npcRole|auto> | roles | reward <rounds> | spectator <on|off> | player | arearadius <blocks>." : "/hordeconfig enemy <tipo> | tipos | role <rolNpc|auto> | roles | reward <rondas> | spectator <on|off> | player | arearadius <bloques>.")
                .set("#HelpConfigLabel.Text", english ? "horde-config.json (persistent settings)" : "horde-config.json (config persistente)")
                .set("#HelpConfigLine1.Text", english ? "spawnConfigured, worldName, spawnX/Y/Z, minSpawnRadius, maxSpawnRadius, arenaJoinRadius." : "spawnConfigured, worldName, spawnX/Y/Z, minSpawnRadius, maxSpawnRadius, arenaJoinRadius.")
                .set("#HelpConfigLine2.Text", english ? "rounds, baseEnemiesPerRound, enemiesPerRoundIncrement, waveDelaySeconds." : "rounds, baseEnemiesPerRound, enemiesPerRoundIncrement, waveDelaySeconds.")
                .set("#HelpConfigLine3.Text", english ? "enemyType, npcRole, language, rewardEveryRounds, rewardCategory, rewardItemId, rewardItemQuantity, roundStartSoundId, roundVictorySoundId, finalBossEnabled." : "enemyType, npcRole, language, rewardEveryRounds, rewardCategory, rewardItemId, rewardItemQuantity, roundStartSoundId, roundVictorySoundId, finalBossEnabled.")
                .set("#HelpExternalLabel.Text", english ? "External JSON files (plugin data folder)" : "JSON externos (carpeta de datos del plugin)")
                .set("#HelpExternalLine1.Text", english ? "enemy-categories.json: categories, finalBossRoles, blockedRoleHints." : "enemy-categories.json: categorias, finalBossRoles, blockedRoleHints.")
                .set("#HelpExternalLine2.Text", english ? "reward-items.json: reward pools by category for UI selector and random modes." : "reward-items.json: pools por categoria para selector UI y modos random.")
                .set("#HelpExternalLine3.Text", english ? "horde-sounds.json: hints and filters for round start/victory auto sounds." : "horde-sounds.json: hints y filtros para sonidos auto de inicio/victoria.")
                .set("#HelpReloadLabel.Text", english ? "Reload and deployment notes" : "Recarga y despliegue")
                .set("#HelpReloadLine1.Text", english ? "/hordareload config or 'Reload config' applies horde-config.json plus external JSON without restart." : "/hordareload config o 'Recargar config' aplica horde-config.json y JSON externos sin reinicio.")
                .set("#HelpReloadLine2.Text", english ? "Replacing the .jar mod still requires server restart." : "Reemplazar el .jar del mod sigue requiriendo reinicio del servidor.")
                .set("#CloseButton.Text", english ? "Close" : "Cerrar");
    }

    private void applyTabVisibility(UICommandBuilder commandBuilder, String tab) {
        boolean generalTab = TAB_GENERAL.equals(tab);
        boolean hordeTab = TAB_HORDE.equals(tab);
        boolean playersTab = TAB_PLAYERS.equals(tab);
        boolean soundsTab = TAB_SOUNDS.equals(tab);
        boolean rewardsTab = TAB_REWARDS.equals(tab);
        boolean helpTab = TAB_HELP.equals(tab);

        this.setVisible(commandBuilder, generalTab, "#SpawnStateLabel", "#SpawnLabel", "#SpawnX", "#SpawnY", "#SpawnZ", "#SetSpawnButton", "#RadiusLabel", "#MinRadiusLabel", "#MinRadius", "#MaxRadiusLabel", "#MaxRadius", "#LanguageLabel", "#Language", "#ArenaJoinRadiusLabel", "#ArenaJoinRadius");
        this.setVisible(commandBuilder, hordeTab, "#RoundLabel", "#Rounds", "#BaseEnemiesLabel", "#BaseEnemies", "#EnemiesPerRoundLabel", "#EnemiesPerRound", "#WaveDelayLabel", "#WaveDelay", "#RoleLabel", "#EnemyType", "#FinalBossLabel", "#FinalBossEnabled", "#EnemyLevelRangeLabel", "#EnemyLevelWipLabel");
        this.setVisible(commandBuilder, playersTab, "#ArenaJoinRadiusLabel", "#ArenaJoinRadius", "#AudienceInfoLabel", "#PlayersListTitle", "#PlayersCountLabel", "#PlayersCountValue", "#PlayersListHint", "#PlayersRefreshButton", "#PlayersHeaderName", "#PlayersHeaderMode", "#AudiencePlayersRows", "#AudiencePlayersEmptyLabel", "#AudienceHelpLabel");
        this.setVisible(commandBuilder, soundsTab, "#RoundStartSoundLabel", "#RoundStartSoundId", "#RoundVictorySoundLabel", "#RoundVictorySoundId");
        this.setVisible(commandBuilder, rewardsTab, "#RewardCategoryLabel", "#RewardCategory", "#RewardCommandsLabel", "#RewardItemId", "#RewardItemQuantityLabel", "#RewardItemQuantity");
        this.setVisible(commandBuilder, helpTab, "#HelpIntroLabel", "#HelpCommandsLabel", "#HelpCommandsLine1", "#HelpCommandsLine2", "#HelpCommandsLine3", "#HelpConfigLabel", "#HelpConfigLine1", "#HelpConfigLine2", "#HelpConfigLine3", "#HelpExternalLabel", "#HelpExternalLine1", "#HelpExternalLine2", "#HelpExternalLine3", "#HelpReloadLabel", "#HelpReloadLine1", "#HelpReloadLine2");
        this.setVisible(commandBuilder, false, "#SubTitleLabel", "#TabHintLabel", "#StatusTitleLabel", "#StatusPanel", "#StatusLabel", "#RoleHelpLabel", "#RoundSoundHelpLabel", "#RewardCommandsHelpLabel", "#PlayerMultiplierLabel", "#PlayerMultiplier", "#EnemyLevelMin", "#EnemyLevelRangeSeparator", "#EnemyLevelMax", "#LanguagePrevButton", "#LanguageNextButton", "#FinalBossPrevButton", "#FinalBossNextButton", "#RoundStartSoundPrevButton", "#RoundStartSoundNextButton", "#RoundVictorySoundPrevButton", "#RoundVictorySoundNextButton", "#RewardCategoryPrevButton", "#RewardCategoryNextButton", "#RewardItemPrevButton", "#RewardItemNextButton", "#RewardEveryRoundsLabel", "#RewardEveryRounds");
    }

    private void setVisible(UICommandBuilder commandBuilder, boolean visible, String ... elementIds) {
        if (elementIds == null) {
            return;
        }
        for (String elementId : elementIds) {
            if (elementId == null || elementId.isBlank()) continue;
            commandBuilder.set(elementId + ".Visible", visible);
        }
    }

    private static String normalizeTab(String tab) {
        if (tab == null || tab.isBlank()) {
            return TAB_GENERAL;
        }
        String normalized = tab.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case TAB_GENERAL:
            case TAB_HORDE:
            case TAB_PLAYERS:
            case TAB_SOUNDS:
            case TAB_REWARDS:
            case TAB_HELP: {
                return normalized;
            }
        }
        return TAB_GENERAL;
    }

    private static String audienceModeDisplay(String mode, boolean english) {
        String normalized = HordeConfigPage.normalizeAudienceMode(mode);
        if ("spectator".equals(normalized)) {
            return english ? "Spectator" : "Espectador";
        }
        if ("exit".equals(normalized)) {
            return english ? "Exit area" : "Salir del area";
        }
        return english ? "Player" : "Jugador";
    }

    private static String normalizeAudienceMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "player";
        }
        String normalized = mode.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('\u00e1', 'a').replace('\u00e9', 'e').replace('\u00ed', 'i').replace('\u00f3', 'o').replace('\u00fa', 'u');
        normalized = normalized.replace('_', '-').replace(' ', '-');
        if ("spectator".equals(normalized) || "espectador".equals(normalized) || "observer".equals(normalized) || "observador".equals(normalized)) {
            return "spectator";
        }
        if ("exit".equals(normalized) || "salir".equals(normalized) || "out".equals(normalized) || "leave".equals(normalized)) {
            return "exit";
        }
        return "player";
    }

    private static String buildAudienceInfo(double arenaJoinRadius, int playersInArea, boolean english) {
        if (english) {
            return String.format(Locale.ROOT, "Current arena radius: %.2f blocks | Players inside area: %d", arenaJoinRadius, playersInArea);
        }
        return String.format(Locale.ROOT, "Radio actual de arena: %.2f bloques | Jugadores dentro del area: %d", arenaJoinRadius, playersInArea);
    }

    private static String buildAudienceRowsHint(int playersInArea, boolean english) {
        if (english) {
            return playersInArea > 0 ? "Use each row to set Player, Spectator or Exit mode." : "Move players inside the arena radius to manage them here.";
        }
        return playersInArea > 0 ? "Usa cada fila para poner modo Jugador, Espectador o Salir." : "Mueve jugadores dentro del radio de arena para gestionarlos aqui.";
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String normalizeEnemyTypeInput(String value) {
        if (value == null || value.isBlank()) {
            return "undead";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('\u00e1', 'a').replace('\u00e9', 'e').replace('\u00ed', 'i').replace('\u00f3', 'o').replace('\u00fa', 'u');
        normalized = normalized.replace('_', '-').replace(' ', '-');
        switch (normalized) {
            case "no-muerto":
            case "no-muertos":
            case "nomuerto":
            case "nomuertos":
            case "undead":
            case "zombie":
            case "zombies":
            case "skeleton":
            case "skeletons":
            case "auto":
            case "role":
                return "undead";
            case "random":
            case "aleatorio":
            case "aleatoria":
            case "rand":
            case "rnd":
            case "azar": {
                return "random";
            }
            case "random-all":
            case "randomall":
            case "random-total":
            case "aleatorio-total":
            case "aleatorio-totales": {
                return "random-all";
            }
            case "goblin":
            case "goblins": {
                return "goblins";
            }
            case "scarak":
            case "insecto":
            case "insectos":
            case "colmena":
            case "hive": {
                return "scarak";
            }
            case "void":
            case "vacio":
            case "corrupcion":
            case "corruption": {
                return "void";
            }
            case "wild":
            case "agresiva":
            case "agresivas":
            case "agresivo":
            case "agresivos":
            case "criaturas-agresivas":
            case "bestia":
            case "bestias":
            case "bandit":
            case "outlander":
            case "trork":
            case "spider":
            case "wolf":
            case "slime":
            case "beetle": {
                return "wild";
            }
            case "elemental":
            case "elementales":
            case "elementals": {
                return "elementals";
            }
        }
        return normalized;
    }

    private static String normalizeRewardCategoryInput(String value) {
        if (value == null || value.isBlank()) {
            return "mithril";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replace('\u00e1', 'a').replace('\u00e9', 'e').replace('\u00ed', 'i').replace('\u00f3', 'o').replace('\u00fa', 'u');
        normalized = normalized.replace(' ', '_').replace('-', '_');
        switch (normalized) {
            case "mithril":
                return "mithril";
            case "onyxium":
                return "onyxium";
            case "gemas":
            case "gems":
                return "gemas";
            case "metales":
            case "metals":
                return "metales";
            case "materiales_raros":
            case "material_raro":
            case "rare_materials":
                return "materiales_raros";
            case "armas_especiales":
            case "special_weapons":
                return "armas_especiales";
            case "items_especiales":
            case "trofeos":
            case "special_items":
                return "items_especiales";
        }
        return "mithril";
    }

    private static String read(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString();
    }

    private static String firstNonEmpty(String ... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value == null || value.isBlank()) continue;
            return value;
        }
        return "";
    }

    private static String compactName(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "Jugador";
        }
        String safe = value.trim();
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, Math.max(0, maxLength - 1)) + ".";
    }

    private static final class UiFieldBinding {
        private final String configKey;
        private final String payloadAlias;
        private final String uiValueSelector;
        private final String[] extraPayloadKeys;

        private UiFieldBinding(String configKey, String payloadAlias, String uiValueSelector, String ... extraPayloadKeys) {
            this.configKey = configKey;
            this.payloadAlias = payloadAlias;
            this.uiValueSelector = uiValueSelector;
            this.extraPayloadKeys = extraPayloadKeys;
        }
    }
}



