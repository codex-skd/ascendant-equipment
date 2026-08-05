# Changelog — Ascendant Equipment

## 0.0.0-beta.4

- **Assets binarios completos**: texturas (169) de bloques, ítems, GUI, rarezas, partículas y pinturas; sonidos (10) incluyendo los 3 discos de música; y las 5 estructuras NBT de jefe/torre que el worldgen referenciaba. Ahora el JAR empaqueta todo lo que los models/blockstates/sounds.json ya declaraban.
- **Build reproducible en clones limpios**: las dependencias de compilación/runtime se movieron de `lib_ext/` (ignorado por git) a `libs/` (versionado). Los JARs de Common Toolkit, Ascendant Attributes, Ascendant Spawners, Ascendant Enchanting, Vellumli, JEI y Jade quedan commiteados — un checkout limpio compila sin restaurar jars manualmente.

## 0.0.0-beta.3

- **Fix crítico**: `META-INF/neoforge.mods.toml` vivía en `src/main/resources/templates/` pero `generateModMetadata` en `build.gradle` lee de `src/main/templates/` (sin `resources/`) — el mismatch hacía que la tarea corriera siempre como `NO-SOURCE`, así que **ningún jar hasta ahora (ni beta.1 ni beta.2) llevaba un `mods.toml` real**, solo la plantilla sin expandir en la ruta equivocada. NeoForge rechazaba el jar como "not a valid mod file" — el mod nunca llegó a cargar en partida. Confirmado con log real del juego, corregido moviendo el archivo a la ruta correcta.
- Rellenadas las relaciones de dependencia de CurseForge (`docs/curseforge/project_vars.md`) para que la app instale automáticamente las 5 dependencias requeridas (Common Toolkit, Ascendant Attributes, Ascendant Spawners, Ascendant Enchanting, Vellumli) y liste las 4 opcionales (JEI, Jade, Max Health Fix, Enchantment Descriptions) — no aplicaban en la subida de la beta.2.
- Corregido `release_type` en `project_vars.md` (estaba en `release`, ahora `beta`).

## 0.0.0-beta.2

- Port completo de todo el contenido data-driven de Apotheosis (Fases 0-15 del roadmap): items, afijos, gemas, rarezas, recetas, loot tables, tags, comercios de aldeanos, advancements, mixins, comandos, red, compat opcional.
- Libro de guía in-game portado de Patchouli a Vellumli (`apoth_chronicle`, solo `en_us`).
- Metadata de assets portada (blockstates, modelos de bloque/ítem, partículas, registro de sonidos) y los shaders GLSL reales del efecto fantasma/gris de previsualización de ítems.
- Dependencias requeridas declaradas en `neoforge.mods.toml`: Common Toolkit, Ascendant Attributes, Ascendant Spawners, Ascendant Enchanting, Vellumli.
- JEI y Jade cableados como dependencias opcionales reales (compilan contra builds NeoForge 26.2 verificados), igual que en el Apotheosis original. Gateways excluido de la compilación (sin build para 26.2).
- Corregidos 2 bugs reales de API de JEI 30.x en `compat/jei/` (cast perdido por el decompilador, mismo patrón visto en fases anteriores).
- El mod compila y empaqueta un JAR completo por primera vez desde que se introdujeron las dependencias opcionales.

## 0.0.0-beta.1

- Scaffold inicial desde el esqueleto `codex-docs/mod_template/neoforge/26.2-26.2.0.32-beta` (NeoForge 26.2 / NeoForge 26.2.0.32-beta).
- Repo creado en `stalking-dragons/minecraft/ascendant-equipment`.
- Declarado como port de Apotheosis (Shadows_of_Fire, MIT) — atribución en README/LICENSE/mods.toml/CurseForge, roadmap por fases en `docs/ROADMAP_ASCENDANT_EQUIPMENT.md`.
- Icono del mod añadido y wireado en `neoforge.mods.toml`.
- Corregido `archivesName` en `build.gradle` para seguir la convención `<mod_id>-<mc>-neoforge`.
- Primera subida a CurseForge (proyecto `1638146`) para validar el proyecto mientras se espera a que Placebo/Apothic Attributes/Apothic Spawners/Apothic Enchanting publiquen build para NeoForge 26.2.
