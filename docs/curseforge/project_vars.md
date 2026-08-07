# Project Variables — Ascendant Equipment

Variables for CurseForge upload. Used by `codex-docs/scripts/curseforge-upload.ps1`.

## Required
project_id = 123456
api_token = ee776b0a-ee95-4850-b554-06be02a8657f
game_versions = 9638, 9639, 16498, 10150
release_type = beta

## Optional
relations = common_toolkit:requiredDependency, ascendant_attributes:requiredDependency

---

### Field Descriptions

**project_id**: CurseForge project ID for Ascendant Equipment. Find it in the project URL: `https://www.curseforge.com/minecraft/mods/<project_id>/` or via API.

**api_token**: Your CurseForge API token (account-level, same for all projects). Keep this private. See CurseForge Core → Settings → API Tokens.

**game_versions**: Comma-separated CurseForge game version IDs (no spaces). Must include BOTH Client and Server IDs for "Client & Server" environment:
- `9638` = Client
- `9639` = Server
- `16498` = Minecraft 1.26.2
- `10150` = NeoForge (latest; adjust if version changes)

Reference: Use CurseForge API (`GET /mods/<projectId>/files/<fileId>`) to find correct IDs.

**release_type**: `beta` (development) or `release` (stable). Default: `beta`.

**relations** (optional): Dependency list as `slug:type`. Types: `embeddedLibrary`, `optionalDependency`, `requiredDependency`, `tool`, `incompatible`, `include`.
Example: `common_toolkit:requiredDependency, ascendant_attributes:requiredDependency`
