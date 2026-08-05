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

## Dependencias externas — RESUELTO

El `neoforge.mods.toml` original declara como **obligatorias**: `placebo`, `apothic_attributes`, `apothic_spawners`, `apothic_enchanting` (todas del mismo autor, mods separados) y como opcional `patchouli` (libro de guía).

Ninguna tiene build para NeoForge 26.2 todavía. Se sustituyen por los ports propios del estudio (mismo patrón que este proyecto), ya disponibles en `lib_ext/`:

| Original | Reemplazo |
|---|---|
| Placebo | Common Toolkit (`common_toolkit`) |
| Patchouli | Vellumli (`vellumli`) |
| Apothic Attributes | Ascendant Attributes (`ascendant_attributes`) |
| Apothic Spawners | Ascendant Spawners (`ascendant_spawners`) |
| Apothic Enchanting | Ascendant Enchanting (`ascendant_enchanting`) |

Detalle completo, mapa de símbolos y registro de incidencias: **`docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`**. Se activan como `compileOnly`/`localRuntime` desde `lib_ext/` (patrón de `equivalent_legacy` con Curios) fase a fase, no todas de golpe — ver tabla de activación en ese documento.

## Fases

Cada fase = un encargo a OpenCode. Orden pensado por dependencia técnica (lo que no depende de nada va primero) y por tamaño (las fases grandes son las que más clases tienen en el original).

| Fase | Alcance | Paquetes origen (nº clases) | Depende de |
|---|---|---|---|
| **0** ✅ | Setup: decompilar jar a `temp/apotheosis-src/` (hecho), resolver dependencias externas (hecho, ver `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`), decompilar los 5 reemplazos a `temp/dependency-src/` (hecho) | — | — |
| **1** ✅ | Núcleo: config, utilidades base, attachments, eventos comunes | `util` (32), `attachments` (1), `event` (3), raíz `AdventureConfig`/`AdventureEvents` (2) | Fase 0 |
| **2** ✅ | Registro base. **Alcance ajustado** (2026-08-04): `Apoth.java` es una clase monolítica de 717 líneas con sus 29 subclases anidadas interdependientes entre sí vía el registrador `Apoth.R` — no se puede partir en un subconjunto limpio como se planteó originalmente. Se porta **completo** como `AscEq.java` | `item` (4), raíz `Apoth` completo (29 subclases anidadas) → `AscEq.java` | Fase 1 |
| **3** ✅ | Sistema de rareza y afijos (núcleo del mod): tiers de rareza, framework de afijos, efectos de afijo. **Alcance ajustado** (2026-08-04): se añade `affix/augmenting/` (mesa de aumentos), que el roadmap original no asignaba a ninguna fase — usa directamente `tiers/augments`, ya incluido aquí | `tiers` (9 archivos), `affix` raíz (11), `affix/effect` (16), `affix/augmenting` (5) — 41 archivos | Fase 2 |
| **4** ✅ | Reforging y salvaging. **Alcance corregido** (2026-08-04): no existen subcarpetas `recipe/reforging`/`recipe/salvaging` — `ReforgingRecipe`/`SalvagingRecipe` ya viven dentro de `affix/reforging`/`affix/salvaging`. Se añade el paquete `recipe/` real (misceláneo, sin fase asignada) por no depender de nada específico de esta fase | `affix/reforging` (8), `affix/salvaging` (7), `recipe/` (4) — 19 archivos | Fase 3 |
| **5** ✅ | Sockets y gemas | `socket` (48 archivos fuente) | Fase 3 |
| **6** ✅ | Loot integration: condiciones, entradas, funciones y modifiers de loot table que aplican afijos/rareza a drops | `loot` (24 archivos fuente) | Fase 3, 4, 5 |
| **7** ✅ | Comercio: trades de aldeanos con afijos | `affix/trades` (2 archivos) | Fase 3, 6 |
| **8** ✅ | Spawners y mobs de élite/invasores | `spawner` (2 archivos), `mobs` (16 archivos) | Fase 3, 6 |
| **9** ✅ | Gateways (portales de boss) y su compat | `compat/gateways` (13 archivos), `gen` (6) | Fase 6, 8 |
| **10** ✅ | Generación de mundo (datos, no código): `data/apotheosis/worldgen/` + `data/apotheosis/gateways/` | 24 archivos JSON | Fase 9 |
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

**Fases 0, 1 y 2 completadas.** Fase 2: `item/` (4 clases) + `Apoth.java` completo → `AscEq.java` (29 subclases anidadas). Build con 377 errores, el 100% trazables a paquetes de fases futuras (verificado independientemente). `AscendantEquipment.java` deliberadamente sin wiring de `AscEq` todavía (los initializers estáticos de `AscEq.Items` etc. fallarían en runtime con `NoClassDefFoundError` hasta que existan sus dependencias — se conecta cuando el registro esté completo). Ascendant Enchanting activado como dependencia (antes prevista para Fase 8/15, pero `AscEq.Items` la necesita ya). 1 bug real de API de MC 26.2 corregido (`MobEffect.isInstantenous()` → `isInstantaneous()`), 1 diferencia de firma en Common Toolkit documentada para cuando llegue su fase, y 1 bug del propio Apotheosis original portado fielmente (`AscEq.Items.rarity(path)` ignora su parámetro) — anotado para revisar en QA (Fase 17), no corregido porque el mod debe comportarse igual que el original. Detalle completo: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 3 completada.** 44 archivos portados (`tiers/`, `affix/` raíz, `affix/effect/`, `affix/augmenting/` — alcance ampliado para cubrir el hueco de `affix/augmenting/` que el roadmap original no asignaba a ninguna fase). Build con 706 errores, el 100% trazables a fases futuras o cascadas de estas (verificado independientemente). 3 bugs reales corregidos: firma de `StructureProcessorType`/`MapCodec` en `AscEq.Features` (el fix anotado en Fase 2), inferencia de genéricos rota en cadenas `RegistrySerializer.subtypedSynced(...).register(...)` (type witness explícito), y casts a `IFestiveMarker` (interfaz inyectada por Mixin en Fase 14) contra `ItemStack` que es `final` en MC 26.2 (idiom `(Interfaz)(Object) valor`). Detalle completo: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 4 completada.** 19 archivos portados (`affix/reforging/`, `affix/salvaging/`, `recipe/` — alcance corregido, no existían las subcarpetas `recipe/reforging`/`recipe/salvaging` previstas). Build con 789 errores, el 100% trazables a fases futuras (`loot` F6, `client` F11, `socket` F5) o cascadas ya conocidas. 8 de los 19 archivos compilan con cero errores. 4 bugs reales corregidos, todos verificados contra bytecode del jar original o fuentes de MC 26.2: cast unchecked perdido por el decompilador en `PotionCharmRecipe.getSerializer()`, un argumento de más en `UpdatingSlot` (Placebo→Common Toolkit cambió su firma), el patrón ya conocido `Interfaz.super.metodo()` mal decompilado (esta vez en `BlockEntityRenderer.extractRenderState`, corregido también retroactivamente en `AugmentingTableTileRenderer` de la Fase 3, que tenía el mismo bug sin detectar), y un rename vanilla 26.1.2→26.2 (`Sheets.cutoutBlockSheet/translucentBlockSheet` → `cutoutBlockItemSheet/translucentBlockItemSheet`). Detalle completo: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 5 completada.** 48 archivos portados (`socket/` completo: raíz, `gem/`, `gem/bonus/`, `gem/bonus/special/`, `gem/cutting/`, `gem/storage/`). Build con 833 errores (789 en Fase 4), 37 de los 48 archivos compilan con cero errores. Se resolvieron las cascadas de `ReactiveSmithingRecipe` pendientes desde Fase 1 (`SizedUpgradeRecipe`, `MaliceRecipe`, `SupremacyRecipe` ya no marcan "does not override"). 2 renames vanilla reales de MC 26.1.2→26.2 corregidos y verificados con `javap`/fuente real: `advancements.criterion.ContextAwarePredicate` → `advancements.predicates.ContextAwarePredicate`, y `Minecraft.screen` → `Minecraft.gui.screen()`. Patrones ya conocidos (Interfaz.super, UpdatingSlot de 5 args) aplicados proactivamente y verificados, sin sorpresas. Detalle completo: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 6 completada.** 24 archivos portados (`loot/` completo), **los 24 compilan con cero errores**. Build bajó de 833 a 380 errores — se resolvieron todas las cascadas pendientes por `LootRarity`/`LootCategory`/`RarityRegistry` desde la Fase 1 (incluidas 8 en archivos de fases 4-5). 1 bug real corregido: `LootPoolSingletonContainer.EntryConstructor` es `protected` en MC 26.2, se referencia por nombre simple heredado en vez de import calificado. Detalle completo: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 7 completada.** 2 archivos portados (`affix/trades/`: `AffixTrade`, `AutomaticAffixTrade`), ambos compilan con cero errores. Build bajó de 379 a 376. Resuelta la referencia pendiente de Fase 6 (`AscEq.LootFunctions.AUTOMATIC_AFFIX_TRADE`/`TIER_GATED_COMPONENTS`). Único hallazgo: mapeo `PlaceboCodecs`→`CommonToolkitCodecs` aplicado proactivamente (ya establecido desde Fase 6). Detalle: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 8 completada.** 18 archivos portados (`spawner/` ×2, `mobs/` completo ×16, incluida `ApothMobEvents`→`AscEqMobEvents`). Build bajó de 376 a 370 errores; resueltas las forward-refs de `BossStats`/`Invader`/`InvaderRegistry`/`InvaderSpawnRules`/`BossSpawnerBlock` pendientes desde Fase 6 (43 errores de `AscEq.java`). 3 bugs reales corregidos y verificados con `javap` contra el bytecode real de MC 26.2: `EntityType.PLAYER`→`EntityTypes.PLAYER` (rename), `loadEntityRecursive(CompoundTag,...)` ahora toma `EntitySpawnRequest` en vez de `EntitySpawnReason` (adición de NeoForge), y un fallo de inferencia de genéricos en `RecordCodecBuilder.create` (variante del patrón de la Fase 3) resuelto con type witness en el lambda. Detalle: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 9 completada.** 19 archivos portados (`gen/` completo ×6, `compat/gateways/` completo ×13). `gen/` compila con cero errores propios. Build subió a 558 errores — aumento esperado, no regresión: `compat/gateways/` es integración con el mod de terceros **Gateways**, que no tenemos disponible ni sustituido (nuevo caso, distinto a "fase futura nuestra" — documentado en la tabla de dependencias). 1 bug real corregido y verificado con `javap`: `StructureProcessor` pasó de clase abstracta a interfaz en MC 26.2 (`ItemFrameGemsProcessor`), resolviendo el pendiente anotado en la Fase 2. Detalle: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

**Fase 10 completada.** 24 archivos JSON portados (worldgen + gateways), hecho directamente sin delegar (sustitución de namespace, no código Java). **No se copiaron los `.nbt`** de estructura (torres + mazmorra de jefe) — contenido creativo, política de assets — así que el worldgen de torres/mazmorra no generará nada real hasta la Fase 16. Detalle: `docs/DEPENDENCIES_ASCENDANT_EQUIPMENT.md`.

Próximo paso: **Fase 11** (cliente y render: pantallas, HUD, partículas, shaders — resuelve la mayor cascada de errores pendiente).
