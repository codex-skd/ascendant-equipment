# Project Variables — Ascendant Equipment (1.21.1)

> **Rama 1.21.1**: `game_versions = 9638, 9639, 11779, 10150` (Client, Server, **1.21.1** id `11779`, NeoForge). `release_type = beta`. JAR `ascendant_equipment-1.21.1-neoforge-21.1.249-<version>.jar`. Tag `1.21.1-neoforge-<version>`. Proyecto CurseForge compartido con la rama 26.2 (`1638146`).

## Required
project_id = 1638146
api_token = ee776b0a-ee95-4850-b554-06be02a8657f
game_versions = 9638, 9639, 11779, 10150
release_type = beta

## Optional
relations = common-toolkit:requiredDependency, ascendant-attributes:requiredDependency, ascendant-spawners:requiredDependency, ascendant-enchanting:requiredDependency, vellumli:requiredDependency, regalia-slots-api:optionalDependency

---

El script lee `project_id`, `api_token` y `game_versions` de este archivo, y `mod_id`, `mod_name`,
`minecraft_version`, `mod_version` de `gradle.properties`. Sube el JAR desde `build/libs/` con el
changelog de `docs/curseforge/versions/<version>.md`.

**Nota post-subida (manual)**: el API de subida no expone client/server ni la licencia. Tras subir,
editar el archivo en la web → entorno **Client & Server**. La licencia del proyecto ya es MIT (correcta).

**game_versions**: `9638` Client · `9639` Server · `11779` Minecraft 1.21.1 · `10150` NeoForge.

## Rama
minecraft/1.21.1/neoforge-21.1.249/production

## Tag
Formato: `<mc-version>-<framework>-<version>` — Ejemplo: `1.21.1-neoforge-0.0.0-beta.1`

## Repo GitLab
https://gitlab.com/stalking-dragons/minecraft/ascendant-equipment.git
