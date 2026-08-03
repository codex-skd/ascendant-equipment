# Flujo de trabajo — Ascendant Equipment (NeoForge)

> **Versión del workflow**: 1.16.0 (codex-docs)
> Este archivo pertenece al proyecto **Ascendant Equipment**. Cambios aquí solo afectan a este proyecto.
> **Trabaja directamente con este archivo**: es el workflow operativo del mod, autocontenido. No leas `codex-docs/WORKFLOW_AGENT.md` ni `WORKFLOW_GENERIC.md` de forma rutinaria.
> On-demand (solo si la tarea lo necesita): `codex-docs/reference/CURSEFORGE.md` (formato HTML al publicar), `codex-docs/reference/GRAPHIFY.md` (backend LLM de Graphify), `codex-docs/reference/REPO_SETUP.md` (setup único de repo).

## Específico del mod

| Dato | Valor |
|---|---|
| Mod ID (`gradle.properties`) | `ascendant_equipment` |
| Clase principal | `AscendantEquipment` |
| Display name (Title Case) | `Ascendant Equipment` |
| Versiones de Minecraft | `26.2` |
| Rama | `minecraft/26.2/neoforge-26.2.0.32-beta/production` |

### Notas específicas de este mod

- **No es un fork**: es un mod propio, scaffoldeado desde `codex-docs/mod_template/neoforge/26.2-26.2.0.32-beta`. No hay atribución que mantener.
- **package**: `com.skd.ascendantequipment`
- **Minecraft / NeoForge**: `26.2` / `26.2.0.32-beta` (heredado del esqueleto, no actualizar sin pedirlo explícitamente)
- **Referencia de análisis en `lib_ext/`**: `Apotheosis-26.1.2-9.0.3.jar` (mod de equipamiento/afijos de Shadows_of_Fire, NeoForge 26.1.2). Se usa **solo como material de estudio de mecánicas** (compilado, sin fuente) — su licencia es `MIT (código) / All Rights Reserved (assets)`, por lo que **no se puede decompilar y reutilizar código ni assets tal cual** bajo este mod sin permiso explícito del autor. `lib_ext/` no se versiona (ver `.gitignore`).

## Convenciones de nomenclatura

| Convención | Uso | Ejemplo |
|---|---|---|
| **snake_case** | `mod_id`, assets/, packages Java | `ascendant_equipment` |
| **PascalCase** | Clases Java principales | `AscendantEquipment` |
| **camelCase** | Variables, métodos, config keys | `ascendantEquipmentConfig` |
| **Title Case** | Display name (README, CHANGELOG, docs, CurseForge) | `Ascendant Equipment` |

## Organización y ramas

- Un repo GitLab por mod, una rama `minecraft/<mc>/neoforge-<neo>/production` por versión. Este clon local trabaja en la rama `production` de esta versión.
- Carpetas: `<mod_id>/<framework>/<mc-version>/` — este clon vive en `ascendant_equipment/neoforge/26.2/`.
- `*/main` y CI/CD: setup único al crear el repo (`codex-docs/reference/REPO_SETUP.md`) — no releer ni modificar.

## Estructura del proyecto

`build.gradle` · `gradle.properties` (mod_id, mod_version, mod_group_id, mod_framework) · `settings.gradle` · `src/main/java/<package>/` · `src/main/resources/assets/<mod_id>/` · `META-INF/neoforge.mods.toml` · `libs/` (versionado) · `lib_ext/` y `temp/` (no versionados) · `docs/` (WORKFLOW + curseforge/) · `CHANGELOG.md` · `README.md` · `graphify-out/` (versionado).

## Versionado

- Beta `0.0.0-beta.X` · Release `X.Y.Z` (SemVer: MAJOR breaking / MINOR feature / PATCH fix)
- `mod_version` y `mod_framework` en `gradle.properties`. JAR: `<mod_id>-<mc>-<framework>-<version>.jar`

## Commits (Conventional Commits)

`<tipo>[<ámbito>]: <descripción>` · tipos `feat fix refactor docs chore style perf test` · el mensaje incluye la versión (`v<version>`).

## Tags

Cada subida a CurseForge crea tag: beta `<mc>-neoforge-beta.X` · release `<mc>-neoforge-X.Y.Z`.

## Flujo por tarea

**0. Alcance** — si el mod tiene varias versiones, preguntar con la herramienta `question`: **"Todas"** o una versión. No asumir.

**1. Desarrollo**

```bash
git checkout minecraft/26.2/neoforge-26.2.0.32-beta/production
./gradlew.bat build
git add -A
git commit -m "feat: <descripción>

v<version>"
git push
```

**2. CurseForge** — solo si el usuario confirma:
- Bump `mod_version` en gradle.properties → `./gradlew.bat clean build`
- Release notes `docs/curseforge/versions/<version>.md` (HTML) + actualizar `CHANGELOG.md`
- Commit `chore: bump version to <version>` → tag `<mc>-neoforge-<version>` → push
- Subir JAR: `powershell -File ../../codex-docs/scripts/curseforge-upload.ps1` (desde este repo)
- Formato HTML de descripciones/changelog: `codex-docs/reference/CURSEFORGE.md`

**3. Release estable** — bump `X.Y.Z` + tag.

**4. Graphify** — tras cada push a remoto. Versión 0.9.12: **`build` no existe**, usar `extract` (1ª vez) o `update . --force` (tras cambios):

```bash
GRAPHIFY="C:\Users\llagu\AppData\Local\Packages\PythonSoftwareFoundation.Python.3.13_qbz5n2kfra8p0\LocalCache\local-packages\Python313\Scripts\graphify.exe"
"$GRAPHIFY" update . --force
git add graphify-out/ && git commit -m "chore: update knowledge graph" && git push
```

Leer siempre `GRAPH_REPORT.md`, nunca `graph.json`/`graph.html` (pesan >1MB). Sin copias fechadas de `graphify-out/`. Backend LLM: `codex-docs/reference/GRAPHIFY.md`.

## Buenas prácticas

- Un commit por cambio lógico · commit+push tras cada cambio funcional y de docs
- `clean build` antes del JAR final · versionar antes de CurseForge · CHANGELOG al día
- Graphify actualizado tras cada release · nomenclatura consistente · sin basura en repo (`nul`, `*_errors.txt`, `TEMPLATE_LICENSE.txt`) · `.gitignore` excluye `temp/` y `lib_ext/`
- README en inglés siempre actualizado · sin residuos del template (paquetes, clases, toml, lang, assets)

## Idioma

| Ámbito | Idioma |
|---|---|
| código, logs, commits | en-US |
| README.md | en-US |
| docs internas (docs/, CHANGELOG, este archivo) | es-ES |
| CurseForge | en-US |
