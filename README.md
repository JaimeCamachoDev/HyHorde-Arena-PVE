# HyHorde Arena PVE

Mod para Hytale con sistema de hordas PVE por rondas.

## Ayuda

El comando oficial de ayuda es:

- `/horda help`

Muestra la informacion en chat.

## Comandos activos

### Comando horda (legacy)

- `/horda` -> crea una horda simple de prueba alrededor del jugador.
- `/horda help` -> muestra ayuda en chat.

### Comando principal de horda PVE

- `/hordapve` -> abre la UI de configuracion.
- `/hordepve` -> alias de `/hordapve`.
- `/hordapve start` -> inicia horda.
- `/hordapve stop` -> detiene horda y limpia enemigos generados.
- `/hordapve status` -> estado actual.
- `/hordapve logs` -> ruta de logs detectada.
- `/hordapve setspawn` -> guarda centro de spawn desde tu posicion.
- `/hordapve enemy <tipo>` -> tipo de enemigo.
- `/hordapve tipos` -> diagnostico tipo -> rol real.
- `/hordapve role <rolNpc|auto>` -> fuerza rol NPC.
- `/hordapve roles` -> lista roles NPC disponibles.
- `/hordapve reward <rondas>` -> frecuencia de recompensa.

### Recarga

- `/hordareload config`
- `/hordareload mod`

## Flujo rapido

1. Usa `/hordapve`.
2. Configura centro con `Usar mi posicion actual` o `/hordapve setspawn`.
3. Ajusta rondas/tipo/recompensas.
4. Guarda config.
5. Inicia con `/hordapve start`.
6. Deten con `/hordapve stop`.

## Tipos de enemigo definidos

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

Nota: en cada modpack pueden variar los roles reales disponibles. Usa `/hordapve tipos` para verificar.

## Configuracion en disco

Archivo: `horde-config.json` en la carpeta de datos del plugin.

## Build

```powershell
.\gradlew.bat clean jar
```

Salida esperada:

- `build/libs/HyHorde-Arena-PVE-<version>.jar`