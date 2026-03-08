package com.hyhorde.arenapve.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hyhorde.arenapve.horde.HordeHelpPage;
import com.hyhorde.arenapve.horde.HordeService;
import java.util.Locale;
import javax.annotation.Nonnull;

public final class HordeHelpCommand
extends AbstractPlayerCommand {
    private final HordeService hordeService;
    private final OptionalArg<String> modeArg;

    public HordeHelpCommand(@Nonnull String name, @Nonnull String description, HordeService hordeService) {
        super(name, description);
        this.hordeService = hordeService;
        this.modeArg = this.withOptionalArg("modo", "modo", (ArgumentType)ArgTypes.STRING);
    }

    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String mode = commandContext.provided(this.modeArg) ? ((String)commandContext.get(this.modeArg)).toLowerCase(Locale.ROOT) : "ui";
        if ("chat".equals(mode) || "texto".equals(mode) || "text".equals(mode)) {
            HordeHelpCommand.sendChatHelp(playerRef, this.hordeService);
            return;
        }
        this.openHelpUi(store, ref, playerRef);
    }

    private void openHelpUi(Store<EntityStore> store, Ref<EntityStore> playerEntityRef, PlayerRef playerRef) {
        Player player = (Player)store.getComponent(playerEntityRef, Player.getComponentType());
        if (player == null) {
            playerRef.sendMessage(Message.raw((String)"No se pudo abrir la ventana de ayuda, mostrando guia en chat."));
            HordeHelpCommand.sendChatHelp(playerRef, this.hordeService);
            return;
        }
        HordeHelpPage.open(playerEntityRef, store, player, playerRef, this.hordeService);
    }

    public static void sendChatHelp(PlayerRef playerRef) {
        HordeHelpCommand.sendChatHelp(playerRef, null);
    }

    public static void sendChatHelp(PlayerRef playerRef, HordeService hordeService) {
        String language = hordeService == null ? "es" : hordeService.getLanguage();
        String availableTypes = "auto, random, bandit, goblin, skeleton, zombie, spider, wolf, slime, beetle";
        if (hordeService != null) {
            String resolved = String.join(", ", hordeService.getEnemyTypeOptionsForCurrentRoles());
            if (!resolved.isBlank()) {
                availableTypes = resolved;
            }
        }
        if (HordeService.isEnglishLanguage(language)) {
            playerRef.sendMessage(Message.raw((String)"[Horde PVE] Commands and descriptions"));
            playerRef.sendMessage(Message.raw((String)"/hordapve or /hordepve - opens config UI"));
            playerRef.sendMessage(Message.raw((String)"/hordapve help - shows this chat help (recommended)"));
            playerRef.sendMessage(Message.raw((String)"/hordapve helpui - opens help window UI"));
            playerRef.sendMessage(Message.raw((String)"/hordapve start - starts horde with current config"));
            playerRef.sendMessage(Message.raw((String)"/hordapve stop - stops horde and cleans generated enemies"));
            playerRef.sendMessage(Message.raw((String)"/hordapve status - current horde summary"));
            playerRef.sendMessage(Message.raw((String)"/hordapve hud - opens live status panel"));
            playerRef.sendMessage(Message.raw((String)"/hordapve setspawn - saves horde center from your position"));
            playerRef.sendMessage(Message.raw((String)"/hordapve enemy <type> - sets enemy type"));
            playerRef.sendMessage(Message.raw((String)"/hordapve tipos - shows type -> real role diagnostics"));
            playerRef.sendMessage(Message.raw((String)"/hordapve role <npcRole|auto> - force exact NPC role"));
            playerRef.sendMessage(Message.raw((String)"/hordapve roles - lists available NPC roles"));
            playerRef.sendMessage(Message.raw((String)"/hordapve reward <rounds> - reward frequency"));
            playerRef.sendMessage(Message.raw((String)"/hordapve logs - shows detected logs path"));
            playerRef.sendMessage(Message.raw((String)"/hordahelp [chat] - opens help UI or chat"));
            playerRef.sendMessage(Message.raw((String)"/hordareload config|mod - reloads config/plugin"));
            playerRef.sendMessage(Message.raw((String)("Available enemy types now: " + availableTypes)));
            return;
        }
        playerRef.sendMessage(Message.raw((String)"[Horda PVE] Comandos y descripciones"));
        playerRef.sendMessage(Message.raw((String)"/hordapve o /hordepve - abre UI de configuracion"));
        playerRef.sendMessage(Message.raw((String)"/hordapve help - muestra esta ayuda en chat (recomendado)"));
        playerRef.sendMessage(Message.raw((String)"/hordapve helpui - abre la ventana UI de ayuda"));
        playerRef.sendMessage(Message.raw((String)"/hordapve start - inicia la horda con la config actual"));
        playerRef.sendMessage(Message.raw((String)"/hordapve stop - detiene la horda y limpia enemigos generados"));
        playerRef.sendMessage(Message.raw((String)"/hordapve status - resumen actual de la horda"));
        playerRef.sendMessage(Message.raw((String)"/hordapve hud - abre panel de estado en vivo"));
        playerRef.sendMessage(Message.raw((String)"/hordapve setspawn - guarda el centro desde tu posicion"));
        playerRef.sendMessage(Message.raw((String)"/hordapve enemy <tipo> - configura tipo de enemigo"));
        playerRef.sendMessage(Message.raw((String)"/hordapve tipos - diagnostico tipo -> rol real"));
        playerRef.sendMessage(Message.raw((String)"/hordapve role <rolNpc|auto> - fuerza rol NPC exacto"));
        playerRef.sendMessage(Message.raw((String)"/hordapve roles - lista roles NPC disponibles"));
        playerRef.sendMessage(Message.raw((String)"/hordapve reward <rondas> - frecuencia de recompensa"));
        playerRef.sendMessage(Message.raw((String)"/hordapve logs - muestra ruta de logs detectada"));
        playerRef.sendMessage(Message.raw((String)"/hordahelp [chat] - abre ayuda UI o chat"));
        playerRef.sendMessage(Message.raw((String)"/hordareload config|mod - recarga config/plugin"));
        playerRef.sendMessage(Message.raw((String)("Tipos disponibles ahora: " + availableTypes)));
    }
}
