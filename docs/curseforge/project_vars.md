# CurseForge — Variables del proyecto

## Proyecto

| Variable | Valor |
|----------|-------|
| `curseforge_project_id` | `1638146` |
| `mod_id` | `ascendant_equipment` |
| `display_name` | `Ascendant Equipment` |

## Tokens

| API | Token | Uso |
|-----|-------|-----|
| Upload | `ee776b0a-ee95-4850-b554-06be02a8657f` | Subir archivos JAR |
| Core (GET) | `$2a$10$yGwryAfmRkS9ZJsJUDf5YOKZpOIsmHB8Fji2D8JVCKBSZEKYlwmaO` | Consultar datos del mod |

Autenticación Upload: cabecera `X-Api-Token`
Autenticación Core: cabecera `x-api-key`

> Token de cuenta (mismo para todos los mods, ver `ageforged_armor/neoforge/26.2/docs/curseforge/project_vars.md` u otros).

## Variables para script (lectura automática)

project_id = 1638146
api_token = ee776b0a-ee95-4850-b554-06be02a8657f
release_type = beta
game_versions = 9638, 9639, 16498, 10150
relations = common-toolkit:requiredDependency,vellumli:requiredDependency,ascendant-attributes:requiredDependency,ascendant-spawners:requiredDependency,ascendant-enchanting:requiredDependency,jei:optionalDependency,jade:optionalDependency,max-health-fix:optionalDependency,enchantment-descriptions:optionalDependency

El script lee `project_id`, `api_token` y `game_versions` de este archivo, y `mod_id`, `mod_name`, `minecraft_version`, `mod_version` de `gradle.properties`. Sube automáticamente el JAR desde `build/libs/` con el changelog de `docs/curseforge/versions/<version>.md`.

## Historial de subidas

| Versión | File ID | Fecha |
|---------|---------|-------|
| 0.0.0-beta.10 | 8591145 | 2026-08-07 |
| 0.0.0-beta.9 | 8589911 | 2026-08-06 |
| 0.0.0-beta.8 | 8586830 | 2026-08-04 |

## Nota

La **primera subida a CurseForge se hace manual** (proyecto recién creado, sin archivos previos que verificar por API). A partir de la segunda subida se puede usar el script `codex-docs/scripts/curseforge-upload.ps1`.

## Rama

```
minecraft/26.2/neoforge-26.2.0.32-beta/production
```

## Tag

Formato: `<mc-version>-<framework>-<version>`
Ejemplo: `26.2-neoforge-0.0.0-beta.1`
