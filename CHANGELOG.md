# Changelog — Ascendant Equipment

## 0.0.0-beta.11

- **Critical fix — Item registration conflict**: Fixed game-breaking issue where no items loaded due to conflicting registration systems in `AscendantEquipment.java`. Removed scaffolding `DeferredRegister` instances (BLOCKS, ITEMS, CREATIVE_MODE_TABS) that were competing with the primary `AscEq.R` (DeferredHelper) registration system. Now uses only `AscEq.R` via `AscEq.bootstrap()`, matching Apotheosis original pattern exactly.
- **Registry system simplified**: Constructor now only manages `AscEq.bootstrap()`, `commonSetup`, event bus registration, and mod config — identical to upstream Apotheosis initialization.

## 0.0.0-beta.10

- **Item model namespace fix**: Fixed all 77 item model JSON files and blockstate JSONs to reference textures from `ascendant_equipment:` namespace instead of `apotheosis:`. Items are now visible in-game with correct textures. This fixes a critical issue where items registered in code were missing their model definitions, causing them to fail to load visually.
- **Asset namespace migration complete**: All model definitions, blockstates, and block models now correctly reference assets from the ascendant_equipment namespace, enabling proper rendering of all items and blocks.

## 0.0.0-beta.9

- **Gem Loot Modifiers syntax fix**: Corrected NeoForge 26.2 entity predicate syntax in `gem_entity_drops.json` and `gem_entity_drops_from_real_players.json`. Replaced invalid `type_specific` wrapper with `sub_predicate` to properly reference the `is_monster` entity sub-predicate. The old Apotheosis 26.1.2 syntax was incompatible with NeoForge 26.2 predicate parsing.
- **Loot modifiers now parse correctly** without data file errors on server startup, enabling proper gem drops from mobs.

## 0.0.0-beta.8

- **Recompiled against NeoForge `26.2.0.37-beta`**: bump of `neo_version` in `gradle.properties` (`26.2.0.32-beta` -> `26.2.0.37-beta`). Verified with `runServer` (startup without errors).
- **Wired the ported AscEq content into the mod entrypoint**: `AscEq.bootstrap()` now properly initializes all ported content registries.

## 0.0.0-beta.6

- **Fases 14-15 completas**: Mixins (25 archivos) + Data-driven JSON (11 advancements, 542 archivos).
- **Fase 14 — Mixins**: Port completo de 25 archivos mixins (AbstractSkeleton, ItemStack, LivingEntity, client renderers, etc.) adaptados a NeoForge 26.2.
- **Fase 15 — Data-driven**: 11 advancements + 542 JSONs (affixes, gemas, rarezas, recetas, loot, tags, worldgen, villager trades).
- **JEI/Jade como optional**: Declaradas como dependencias opcionales en neoforge.mods.toml (compat code ya portado en Fase 13).
- **Graphify actualizado**: Conocimiento de fase post-release integrado (5219 nodos, 12892 edges).
- **Build reproducible**: Todas las dependencias versionadas en `libs/`, compilación limpia desde checkout fresco.
- Subido a CurseForge vía `curseforge-upload.ps1` (file ID `8584229`).

## 0.0.0-beta.5

- **Traducciones completas en 4 idiomas**: generadas y completadas íntegramente las traducciones para `en_us.json`, `es_es.json`, `fr_fr.json` y `de_de.json`. Cobertura de 389 claves por idioma incluyendo affixes, gemas, items, bloques, UI, tooltips, advancements, y modificadores. Mejora de 17% a 100% de cobertura de traducción.
- **Auditoría de integridad**: verificación exhaustiva del mod confirmó compilación limpia, estructura correcta, assets completos (1737 archivos), mixins bien definidos, dependencias correctas, y conformidad con workflow.
- **Fix ciclo de carga (crash de arranque)**: floor de `ascendant_spawners` subido a `[0.0.0-beta.7,)` — las betas 0.0.0-beta.5/6 de Ascendant Spawners declaraban un `ordering="AFTER"` recíproco hacia este mod, formando un ciclo mutuo que NeoForge no podía ordenar (`Mod Sorting failed. Detected Cycles`) e impedía arrancar el cliente. Dependencia de compilación en `libs/` actualizada a beta.7.
- Subido a CurseForge vía `curseforge-upload.ps1` (file ID `8583901`).

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
- Primera subida a CurseForge (proyecto `1638146`) para validar el proyecto mientras se espera a que Placebo/Apothic Attributes/Apothic Spawners/Apothic Enchanting publiquen build para NeoForge 26.2.---

## [0.0.0-beta.7] - 2026-08-05

### Change

- **Recompilado contra NeoForge `26.2.0.37-beta`**: bump de `neo_version` en `gradle.properties` (`26.2.0.32-beta` -> `26.2.0.37-beta`). Verificado con `runServer` (arranque sin errores).
- **Wired the ported AscEq content into the mod entrypoint (AscEq.bootstrap); previously the whole module was never registered and data referencing its registries failed to load.**

## [angelog — Ascendant Equipment

## 0.0.0-beta.6

- **Fases 14-15 completas**: Mixins (25 archivos) + Data-driven JSON (11 advancements, 542 archivos).
- **Fase 14 — Mixins**: Port completo de 25 archivos mixins (AbstractSkeleton, ItemStack, LivingEntity, client renderers, etc.) adaptados a NeoForge 26.2.
- **Fase 15 — Data-driven**: 11 advancements + 542 JSONs (affixes, gemas, rarezas, recetas, loot, tags, worldgen, villager trades).
- **JEI/Jade como optional**: Declaradas como dependencias opcionales en neoforge.mods.toml (compat code ya portado en Fase 13).
- **Graphify actualizado**: Conocimiento de fase post-release integrado (5219 nodos, 12892 edges).
- **Build reproducible**: Todas las dependencias versionadas en `libs/`, compilación limpia desde checkout fresco.
- Subido a CurseForge vía `curseforge-upload.ps1` (file ID `8584229`).

## 0.0.0-beta.5

- **Traducciones completas en 4 idiomas**: generadas y completadas íntegramente las traducciones para `en_us.json`, `es_es.json`, `fr_fr.json` y `de_de.json`. Cobertura de 389 claves por idioma incluyendo affixes, gemas, items, bloques, UI, tooltips, advancements, y modificadores. Mejora de 17% a 100% de cobertura de traducción.
- **Auditoría de integridad**: verificación exhaustiva del mod confirmó compilación limpia, estructura correcta, assets completos (1737 archivos), mixins bien definidos, dependencias correctas, y conformidad con workflow.
- **Fix ciclo de carga (crash de arranque)**: floor de `ascendant_spawners` subido a `[0.0.0-beta.7,)` — las betas 0.0.0-beta.5/6 de Ascendant Spawners declaraban un `ordering="AFTER"` recíproco hacia este mod, formando un ciclo mutuo que NeoForge no podía ordenar (`Mod Sorting failed. Detected Cycles`) e impedía arrancar el cliente. Dependencia de compilación en `libs/` actualizada a beta.7.
- Subido a CurseForge vía `curseforge-upload.ps1` (file ID `8583901`).

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
