# Custom UI Guardrails (HyHorde Arena PVE)

Este documento recoge errores reales vistos en logs y las reglas para evitarlos al tocar `HordeConfigPage.ui`.

## Error actual (2026-03-28)

Firma en cliente (`C:\Users\Jaime\AppData\Roaming\Hytale\UserData\Logs\2026-03-28_22-32-18_client.log`):

- `Failed to load CustomUI documents`
- `Failed to parse file Pages/HordeConfigPage.ui (2251:226)`
- `Could not resolve expression for property HorizontalAlignment to type LabelAlignment`

Causa:

- En `Label #ArenaHeaderCoords` se anadio `HorizontalAlignment: Center` dentro del estilo inline y este runtime fallo al resolver ese valor en ese punto del documento.

Solucion aplicada:

- Quitar `HorizontalAlignment` de `#ArenaHeaderCoords` y dejar solo `VerticalAlignment: Center`.
- Mantener alineaciones horizontales en headers criticos usando estilos ya probados, o validar en cliente tras cada cambio de enum/alineacion.

## Error actual (2026-03-27)

Firma en cliente (`C:\Users\Jaime\AppData\Roaming\Hytale\UserData\Logs\2026-03-27_18-57-04_client.log`):

- `Failed to load CustomUI documents`
- `Failed to parse file Pages/HordeConfigPage.ui (49:12)`
- `Could not resolve spread expression to type TextButtonStyleState`

Causa:

- En `@ListRowHitboxButtonStyle` se intento heredar estados con spread:
  - `...$C.@SmallSecondaryTextButtonStyle.Default`
  - `...$C.@SmallSecondaryTextButtonStyle.Hovered`
  - `...$C.@SmallSecondaryTextButtonStyle.Pressed`
- En este runtime, ese spread no se resuelve para `TextButtonStyleState`.

Solucion aplicada:

- Definir `Default`, `Hovered`, `Pressed` de forma explicita sin spread en ese bloque.

## Error previo de Custom UI (2026-03-27)

Firma en cliente:

- `Failed to load CustomUI documents`
- `Failed to parse file Pages/HordeConfigPage.ui (22:160)`
- `Could not resolve expression for property VerticalAlignment to type LabelAlignment`

Causa:

- Se uso `VerticalAlignment: Top` en `LabelStyle` de pestanas.

Solucion:

- Volver a `VerticalAlignment: Center`.
- Si hay que subir texto, usar `Padding` en vez de `VerticalAlignment: Top`.

## Errores frecuentes ya vistos

1. Documento no encontrado
- Firma: `Could not find document ... HordeConfigPage.ui for Custom UI Append command`.
- Causa: path incorrecto del layout.
- Regla: mantener `LAYOUT = "Pages/HordeConfigPage.ui"` en Java y archivo real en `Common/UI/Custom/Pages/HordeConfigPage.ui`.

2. Set command sobre selector no compatible
- Firma: `CustomUI Set command couldn't set value. Selector: #MinRadius.Value` (y similares).
- Causa: setear desde Java `.Value` de ciertos controles durante build/rebuild.
- Regla: no hacer `set` de esos `.Value` en `build()`. Solo leer payload al guardar/iniciar.

3. Selector inexistente
- Firma: `CustomUI Set command selector doesn't match a markup property`.
- Causa: selector cambiado en `.ui` sin sincronizar Java.
- Regla: cuando se renombre un `#Id` en UI, actualizar todos los selectors en `HordeConfigPage.java`.

4. Texturas de tabs con cruces rojas
- Causa: rutas de textura incorrectas.
- Regla: usar rutas relativas `../Common/...` para recursos compartidos de tabs.

5. Listas `TopScrolling` con filas desalineadas
- Firma visual: mas padding a derecha que a izquierda, filas "finas/largas", boton `X` deformado.
- Causa: filas append con `Width` fijo dentro de un contenedor con scrollbar/padding.
- Regla: en filas dinamicas (`Pages/HordeArenaRow.ui`), usar `Anchor: (Left: 0, Right: 0, ...)` y definir tamanos de icono/boton en px cuadrados.

6. Nuevo campo en editor no persiste
- Firma: el valor aparece en UI pero se pierde al guardar/reabrir.
- Causa: agregar `#Campo.Value` en `.ui` sin sincronizar Java/catalogo.
- Regla: para cada campo nuevo de editor, sincronizar siempre:
- `SNAPSHOT_FIELDS` + `shouldCaptureFieldFromPayload(...)`
- `build().set(...)` + `extract...ValuesForSave()`
- `ensure...DraftDefaults(...)` + `apply...DraftFromSnapshot(...)`
- `...Definition`/`...Snapshot` del catalogo y guardado JSON

## No confundir con errores de servidor no-CustomUI

En `server.log` de hoy tambien aparece:

- `Failed to load manifest for pack at mods\VZ.HytaleMod_Test`
- `Failed to decode`
- `Unexpected character: 22, '"' expected '{'!`

Esto es un problema de `manifest.json` de otro pack/mod, no del parser de Custom UI.

## Checklist rapido antes de compilar

1. Revisar que no hay `VerticalAlignment: Top` en estilos de label de tabs.
2. Revisar que no hay spreads de estados no compatibles en `TextButtonStyle` (evitar `...Style.Default` dentro de `Default/Hovered/Pressed` si falla parseo).
3. Revisar que selectors Java (`#...`) existen en el `.ui`.
4. Revisar que no se setean `.Value` problematicos en `build()`.
5. Compilar y probar apertura de `/hordeconfig` una vez.
6. Si hay crash de carga, mirar primero:
- `C:\Users\Jaime\AppData\Roaming\Hytale\UserData\Logs\*_client.log` (errores de parser Custom UI).
- `C:\Users\Jaime\AppData\Roaming\Hytale\UserData\Saves\Mod-Test\logs\*_server.log` (errores de plugin/manifest/arranque).
