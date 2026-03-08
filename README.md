# HyHorde Arena PVE

Mod para Hytale que crea hordas PVE por rondas, con configuracion por UI y comandos.

## Que hace

- Sistema de rondas con dificultad progresiva.
- Centro de spawn configurable.
- Escalado por jugadores (`playerMultiplier`).
- Tipos de enemigo simplificados (`auto`, `random`, etc.) con deteccion dinamica de roles NPC disponibles.
- Recompensas por rondas (drop de item en el centro).
- Selector de idioma en la UI (`Espanol/English`).
- Anuncios grandes en pantalla:
  - Inicio de horda.
  - Countdown `3..2..1` al inicio.
  - Countdown final `3..2..1` antes de la siguiente ronda (si hay espera).
  - Inicio de ronda.
  - Fin de ronda.
  - Fin de horda.
- Si detienes con `/hordapve stop`, limpia los enemigos generados por esa horda.
- Panel de estado (`/hordapve hud`) con enemigos vivos, progreso, kills/deaths y leaderboard.

## Uso rapido

1. Ejecuta `/hordapve` para abrir la UI de configuracion.
2. Guarda el centro con `Usar mi posicion actual` o con `/hordapve setspawn`.
3. Ajusta rondas, enemigos, delay, idioma y recompensas.
4. Guarda con `Guardar config`.
5. Inicia con `/hordapve start`.
6. Revisa estado con `/hordapve status` o `/hordapve hud`.
7. Deten con `/hordapve stop`.

## Ayuda

- `/hordapve help` muestra ayuda en chat con descripciones.
- `/hordapve helpui` abre la ventana de ayuda.
- `/hordahelp` abre ayuda UI.
- `/hordahelp chat` muestra ayuda en chat.

## Comandos disponibles

### Comando base

- `/hordapve` (abre UI)
- Alias del comando base: `/hordepve`

### Subcomandos de `/hordapve`

- `help`, `ayuda`, `?`, `commands`, `comandos`
- `helpui`, `ayudaui`, `uihelp`, `manual`
- `start`
- `stop`
- `status`
- `logs`, `log`
- `hud`, `panel`
- `setspawn`, `spawn`
- `enemy`, `enemigo`, `tipo`, `enemytype`
- `enemies`, `enemigos`, `tipos`, `enemytypes`
- `role`, `npcrole`
- `roles`, `npcroles`
- `reward`

Usos importantes:

- `/hordapve enemy <tipo>`
- `/hordapve role <rolNpc|auto>`
- `/hordapve reward <rondas>`
- `/hordapve tipos` (diagnostico de tipos y roles)

### Otros comandos

- `/horda` (comando legacy: horda simple de prueba alrededor del jugador)
- `/horda help`
- `/hordareload config`
- `/hordareload mod` (tambien acepta `jar` y `plugin`)

## Tipos de enemigo simplificados

Tipos definidos por el mod:

- `auto`
- `random`
- `bandit`
- `goblin`
- `skeleton`
- `zombie`
- `spider`
- `wolf`
- `slime`
- `beetle`

Importante:

- No todos tienen por que existir en todos los modpacks.
- Usa `/hordapve tipos` para ver `tipo -> rol real detectado` en tu servidor.

## Recompensas

Configuracion principal:

- `rewardEveryRounds`
- `rewardItemId`
- `rewardItemQuantity`

El plugin valida el `rewardItemId` antes de dropear para evitar items invalidos.

## Configuracion en disco

Archivo: `horde-config.json` (carpeta de datos del plugin).

Campos principales:

- `spawnConfigured`, `worldName`, `spawnX`, `spawnY`, `spawnZ`
- `minSpawnRadius`, `maxSpawnRadius`
- `rounds`, `baseEnemiesPerRound`, `enemiesPerRoundIncrement`, `waveDelaySeconds`
- `playerMultiplier`
- `enemyType`, `npcRole`
- `language`
- `rewardEveryRounds`, `rewardItemId`, `rewardItemQuantity`

## Build

```powershell
.\gradlew.bat clean jar
```

Salida esperada:

- `build/libs/HyHorde-Arena-PVE-<version>.jar`
