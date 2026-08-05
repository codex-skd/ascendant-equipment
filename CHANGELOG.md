# Changelog — Ascendant Equipment

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
