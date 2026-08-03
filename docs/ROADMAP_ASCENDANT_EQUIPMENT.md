# Roadmap — Ascendant Equipment (port de Apotheosis)

> Documento de planificación. No es el workflow operativo (ese es `WORKFLOW_ASCENDANT_EQUIPMENT_26-2.md`) — este archivo define **qué construir y en qué orden**, para ir alimentando el trabajo a OpenCode fase a fase.

## Naturaleza del proyecto

**Ascendant Equipment es un port declarado de [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) por Shadows_of_Fire**, de NeoForge 26.1.2 (v9.0.3) a NeoForge 26.2, con todos los identificadores (paquetes, clases, campos, mod id) renombrados a nuestra convención. No es un mod "inspirado en" — es un port funcional 1:1 del código.

### Base legal — obligatorio mantener siempre

- **Código**: licencia MIT del original. Podemos copiar, modificar y renombrar libremente, pero el aviso de copyright/atribución **debe** conservarse en `LICENSE`, `README.md`, `docs/curseforge/project_description.md` y el campo `credits` de `neoforge.mods.toml`. Frase fija a usar en los cuatro sitios: *"Ascendant Equipment is a port of [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) by Shadows_of_Fire, ported from NeoForge 26.1.2 to NeoForge 26.2."*
- **Assets**: el original es `All Rights Reserved` — **no se copia ni un archivo** de `assets/apotheosis/` (texturas, modelos, sonidos, shaders, libro Patchouli, logo). Cada fase que necesite un asset lo sustituye por uno propio (placeholder al principio, arte final después). El mod debe compilar y funcionar con placeholders — sustituir el arte es un trabajo aparte, no bloqueante.
- **Fuente de referencia**: `Apotheosis-26.1.2-9.0.3.jar` en `lib_ext/` es solo bytecode compilado (536 clases, sin nombres de variables locales garantizados). Fase 0 lo decompila a `temp/apotheosis-src/` (no versionado) como referencia de lectura — nunca se commitea el código decompilado tal cual, se reescribe fase a fase dentro de `src/`.

## Convención de renombrado

| Original | Ascendant Equipment |
|---|---|
| Paquete raíz `dev.shadowsoffire.apotheosis` | `com.skd.ascendantequipment` |
| Clase principal `Apotheosis` (`@Mod`) | `AscendantEquipment` |
| Clase de registro `Apoth` (contenedor de `Apoth.Items`, `Apoth.Blocks`, etc.) | `AscEq` (`AscEq.Items`, `AscEq.Blocks`, ...) |
| MODID `apotheosis` | `ascendant_equipment` |
| Namespace de assets/data `apotheosis:` | `ascendant_equipment:` |
| `AdventureConfig`, `AdventureEvents` (config/eventos generales) | `EquipmentConfig`, `EquipmentEvents` |

Regla general: cada subpaquete (`affix`, `socket`, `loot`, `mobs`...) se mantiene igual en minúsculas (son nombres de dominio, no de marca), solo cambia el paquete raíz y las clases que llevan el nombre del mod.

## Dependencias externas — decisión pendiente

El `neoforge.mods.toml` original declara como **obligatorias**: `placebo`, `apothic_attributes`, `apothic_spawners`, `apothic_enchanting` (todas del mismo autor, mods separados) y como opcionales `gateways`, `patchouli`, más integración con `curios`/`jei`.

**No están incluidas en este roadmap** — replicarlas también multiplicaría el alcance por 5. Asunción de trabajo (a confirmar antes de la Fase 2, que es donde empiezan a hacer falta):

- Opción recomendada: mantenerlas como **dependencias reales** (añadir sus JARs para NeoForge 26.2 a `libs/` si existen builds compatibles, o `optional`/soft-dependency si no).
- Alternativa: fusionar solo las piezas de esos mods que Apotheosis usa directamente (ej. atributos custom de `apothic_attributes`) dentro de `ascendant_equipment`, eliminando la dependencia dura.

Se pregunta al usuario en la Fase 2 con la herramienta `question` — no se asume.

## Fases

Cada fase = un encargo a OpenCode. Orden pensado por dependencia técnica (lo que no depende de nada va primero) y por tamaño (las fases grandes son las que más clases tienen en el original).

| Fase | Alcance | Paquetes origen (nº clases) | Depende de |
|---|---|---|---|
| **0** | Setup: decompilar jar a `temp/apotheosis-src/`, resolver dependencias externas (ver arriba), definir mapping de paquetes/clases en `docs/ASCENDANT_RENAME_MAP.md` | — | — |
| **1** | Núcleo: config, utilidades base, attachments, eventos comunes | `util` (32), `attachments` (1), `event` (3), raíz `AdventureConfig`/`AdventureEvents` (2) | Fase 0 |
| **2** | Registro base: items, bloques, tiles, tabs, stats, triggers (el "esqueleto" de contenido, sin lógica de afijos aún) | `item` (5), parte de raíz `Apoth`/`Apoth$Items`/`Apoth$Blocks`/`Apoth$Tiles`/`Apoth$Tabs`/`Apoth$Stats`/`Apoth$Triggers` (~10 de 36) | Fase 1 |
| **3** | Sistema de rareza y afijos (núcleo del mod): tiers de rareza, framework de afijos, efectos de afijo | `tiers` (18), `affix` — subset base (parte de 93) | Fase 2 |
| **4** | Reforging y salvaging | `affix/reforging`, `affix/salvaging` (parte de 93), `recipe/reforging`, `recipe/salvaging` (parte de 4) | Fase 3 |
| **5** | Sockets y gemas | `socket` (86) | Fase 3 |
| **6** | Loot integration: condiciones, entradas, funciones y modifiers de loot table que aplican afijos/rareza a drops | `loot` (43) | Fase 3, 4, 5 |
| **7** | Comercio: trades de aldeanos con afijos | `affix/trades` (parte de 93) | Fase 3, 6 |
| **8** | Spawners y mobs de élite/invasores | `spawner` (3), `mobs` (38) | Fase 3, 6 |
| **9** | Gateways (portales de boss) y su compat | `compat/gateways`, `gen` (6) | Fase 6, 8 |
| **10** | Generación de mundo (estructuras, features asociadas a loot de Apotheosis) | `data/gateways` y resto de `data` relacionado con worldgen (parte de 34) | Fase 9 |
| **11** | Cliente y render: pantallas, HUD, partículas, shaders | `client` (30), `particle` (1) | Fases 2–9 según feature |
| **12** | Comandos y red | `commands` (10), `net` (13) | Fase 1 |
| **13** | Compat opcional: Curios, JEI, Patchouli | `compat/curios`, `compat/jei`, resto de `compat` (parte de 44) | Fases 4–11 |
| **14** | Mixins (se hacen al final: tocan clases vanilla y son lo más frágil de portar entre versiones de Minecraft) | `mixin` (25) | Todas las anteriores relevantes |
| **15** | Contenido data-driven: recetas, tags, advancements, loot tables JSON (equivalentes propios, no copiados) | `advancements` (11) + JSONs de `data/` no cubiertos antes | Fases 3–14 |
| **16** | Arte propio: sustituir placeholders por texturas/modelos/sonidos/libro de guía originales | — (todo `assets/`) | Trabajo paralelo, no bloquea el resto |
| **17** | QA de paridad funcional: probar que el comportamiento replica el original fase por fase | — | Todas |

## Cómo se alimenta a OpenCode

1. Antes de cada fase: confirmar contigo el alcance exacto (qué subpaquete, qué clases del original) — no se abre una fase sin fase anterior mergeada y compilando.
2. El prompt a OpenCode por fase incluye: ruta al código decompilado de referencia en `temp/apotheosis-src/<paquete>/`, la convención de renombrado de este documento, y el resultado esperado (`src/main/java/com/skd/ascendantequipment/<paquete>/...` compilando con `./gradlew.bat build`).
3. Al cerrar cada fase: build verde, commit (`feat[<paquete>]: port <subsistema> from Apotheosis`, versión bump beta), push, actualizar `CHANGELOG.md` y marcar la fase como hecha en este documento.
4. Graphify se actualiza tras cada fase (no solo al final) para que el grafo de conocimiento no se quede desfasado en un proyecto de este tamaño.

## Estado

Ninguna fase iniciada. Próximo paso: **Fase 0** (decompilar + resolver dependencias externas).
