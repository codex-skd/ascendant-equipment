# Dependencias de Apotheosis — mapa de sustitución (control)

> Documento vivo. Aquí se registra cualquier incompatibilidad, símbolo no encontrado o duda que surja al portar código que dependía de Placebo/Patchouli/Apothic Attributes/Apothic Spawners/Apothic Enchanting hacia sus reemplazos. Se actualiza en cada fase del roadmap que toque una de estas dependencias.

## Mapa de sustitución

| Dependencia original (Apotheosis) | Reemplazo | mod_id | Versión | Jar en `lib_ext/` | NeoForge declarado |
|---|---|---|---|---|---|
| Placebo | [Common Toolkit](https://www.curseforge.com/minecraft/mc-mods/common-toolkit) | `common_toolkit` | `0.0.0-beta.1` | `common_toolkit-26.2-neoforge-0.0.0-beta.1.jar` | `26.2.0.32-beta` |
| Patchouli (opcional en el original) | [Vellumli](https://www.curseforge.com/minecraft/mc-mods/vellumli/preview) | `vellumli` | `0.0.0-beta.2` | `vellumli-26.2-neoforge-0.0.0-beta.2.jar` | `26.2.0.32-beta` |
| Apothic Attributes | [Ascendant Attributes](https://www.curseforge.com/minecraft/mc-mods/ascendant-attributes) | `ascendant_attributes` | `0.0.0-beta.2` | `ascendant_attributes-26.2-neoforge-0.0.0-beta.2.jar` | `26.2.0.32-beta` |
| Apothic Spawners | [Ascendant Spawners](https://www.curseforge.com/minecraft/mc-mods/ascendant-spawners) | `ascendant_spawners` | `0.0.0-beta.5` | `ascendant_spawners-26.2-neoforge-0.0.0-beta.5.jar` | `26.2.0.32-beta` |
| Apothic Enchanting | [Ascendant Enchanting](https://www.curseforge.com/minecraft/mc-mods/ascendant-enchanting) | `ascendant_enchanting` | `0.0.0-beta.3` | `ascendant_enchanting-26.2-neoforge-0.0.0-beta.3.jar` | `26.2.0.32-beta` |

Todos declaran NeoForge `26.2.0.32-beta` y Minecraft `26.2` — compatibles con nuestro target, sin necesidad de esperar builds adicionales.

## Hallazgo clave

Los 5 reemplazos **no son mods de API distinta que haya que adaptar desde cero** — son **ports del mismo estudio** (mismo patrón que estamos aplicando a Apotheosis), confirmado en el propio `neoforge.mods.toml` de Common Toolkit:

> `credits="Ported from Placebo by Shadows_of_Fire (MIT License). See NOTICE.md."`

Package raíz `com.skd.<nombre>`, misma metodología de renombrado. Verificado con una prueba directa: `dev.shadowsoffire.placebo.util.EnchantmentUtils` (usada por `ApothMiscUtil` en Apotheosis) existe **1:1 por nombre de clase y de paquete relativo** en `com.skd.commontoolkit.util.EnchantmentUtils`.

**Estrategia de mapeo por defecto**: sustituir el prefijo de paquete y probar primero si el resto de la ruta y el nombre de clase/método se mantienen igual:

```
dev.shadowsoffire.placebo.*             → com.skd.commontoolkit.*
dev.shadowsoffire.apothic_attributes.*  → com.skd.ascendantattributes.*
dev.shadowsoffire.apothic_spawners.*    → com.skd.ascendantspawners.*
dev.shadowsoffire.apothic_enchanting.*  → com.skd.ascendantenchanting.*
(patchouli)                             → com.skd.vellumli.* (namespace/API distinta, ver nota abajo)
```

Esto **no está garantizado para el 100% de los símbolos** — cuando un mapeo directo no exista, se documenta aquí como excepción.

## Fuente de referencia para el mapeo

Los 5 jars se decompilaron con Vineflower a `temp/dependency-src/<nombre>/` (no versionado, igual que `temp/apotheosis-src/`):

| Carpeta | Origen |
|---|---|
| `temp/apotheosis-src/` | Apotheosis 26.1.2-9.0.3 (lo que se porta) |
| `temp/dependency-src/common_toolkit/` | Common Toolkit 0.0.0-beta.1 |
| `temp/dependency-src/vellumli/` | Vellumli 0.0.0-beta.2 |
| `temp/dependency-src/ascendant_attributes/` | Ascendant Attributes 0.0.0-beta.2 |
| `temp/dependency-src/ascendant_spawners/` | Ascendant Spawners 0.0.0-beta.5 |
| `temp/dependency-src/ascendant_enchanting/` | Ascendant Enchanting 0.0.0-beta.3 |

Antes de portar cualquier clase de Apotheosis que importe una de estas 4 dependencias, se busca el símbolo equivalente en la carpeta `dependency-src` correspondiente. Si no existe, se documenta abajo.

## Nota — Vellumli vs. Patchouli

Vellumli sustituye a Patchouli, pero Patchouli era **dependencia opcional** en el Apotheosis original (el libro de guía `apoth_chronicle`). Su API de construcción de libros (`com.skd.vellumli.api.*`) es la referencia para portar `assets/apotheosis/patchouli_books/apoth_chronicle` — el contenido del libro es texto/estructura, no arte, así que se puede portar y reescribir sin tocar la política de "sin assets copiados" (los iconos/imágenes del libro sí necesitan arte propio, Fase 16).

## Cómo se declaran en el proyecto

**Aún no declaradas** en `neoforge.mods.toml` ni en `build.gradle` — se añaden en la fase donde realmente se empiece a usar cada una (siguiendo el patrón `compileOnly` + `localRuntime` desde `lib_ext/`, igual que `equivalent_legacy` hace con Curios), no todas de golpe. Se actualiza esta tabla marcando cuándo se activa cada una:

| Dependencia | Estado |
|---|---|
| Common Toolkit | ✅ Activada (Fase 1) |
| Vellumli | Pendiente de activar (Fase 15, libro de guía) |
| Ascendant Attributes | ✅ Activada (Fase 1, usada por `CommonTooltipUtil`/`EquipmentEvents`) |
| Ascendant Spawners | ✅ Activada (Fase 1, usada por `PresetSpawnerStats`) |
| Ascendant Enchanting | ✅ Activada (Fase 2, usada por `AscEq.Items` — `GlowyItem` en `GOD_FUSED_PEARL`/`INFUSED_SPAWNER_RUNE`) |

## Incidencias registradas

### Fase 1 (2026-08-04)

Tras portar `util/`, `attachments/`, `event/`, `AdventureConfig`→`EquipmentConfig`, `AdventureEvents`→`EquipmentEvents`, el build quedó con 169 errores — **todos** trazables a paquetes de fases futuras (`affix`, `socket`, `loot`, `tiers`, `commands`, `net`, `mobs.util`, `AscEq`), como se esperaba. Se detectaron y corrigieron 3 problemas reales (no relacionados con fases futuras):

1. **`CanSocketGemEvent.setCanceled` — artefacto del decompilador, no cambio de API.** El original decompilado tenía `super.setCanceled(canceled)`, pero `Event` no declara ese método — vive como default method en `ICancellableEvent`. Vineflower renderiza mal las llamadas explícitas `Interfaz.super.metodo()`, quitando el calificador de interfaz. Fix: `ICancellableEvent.super.setCanceled(canceled)`. **Vigilar este patrón en fases futuras** — cualquier clase que implemente una interfaz con default methods y llame a `super.metodo()` puede tener el mismo problema silencioso (compila si hay una superclase con ese nombre de método, o falla como aquí si no la hay).
2. **`ApothMiscUtil` accedía a `PlayerAdvancements.progress` / `ClientAdvancements.progress` (campos privados de vanilla).** Faltaba portar el `META-INF/accesstransformer.cfg` del Apotheosis original — el scaffold del template no lo incluía. Se añadió el archivo completo (todas las entradas del original, incluidas las de fases futuras: SmithingRecipe, BaseSpawner, LevelRenderer, etc. — no rompen nada estando de más, se validan cuando toque su fase). NeoForge lo detecta automáticamente en esa ruta (sin declarar `[[accessTransformers]]` en el toml, confirmado por el propio comentario de la plantilla).
3. **`AffixItemIngredient.items()` — `BuiltInRegistries.ITEM.listElements()` devuelve `Stream<Reference<Item>>`, no asignable a `Stream<Holder<Item>>` por invarianza de genéricos.** Confirmado como comportamiento real y actual de MC 26.2 (no un bug de nuestro port) revisando el propio código fuente de Minecraft (`Registry.java`, que hace el mismo cast explícito internamente). Fix: `.listElements().<Holder<Item>>map(i -> i).filter(...)`.
4. **Falso positivo, sin fix**: `SizedUpgradeRecipe` marcaba `onCraft` como "no override" — es cascada de que `ReactiveSmithingRecipe` (paquete `socket`, Fase 5) todavía no existe, no un problema real. Se resolverá solo cuando llegue esa fase.

**Añadido a `AscendantEquipment.java`** (no eran del alcance original de Fase 1, pero varios archivos de `util/` los necesitaban y son helpers propios de la clase principal, sin dependencias de fases futuras): `loc(String)`, `lang(String, String, Object...)`, `langKey(String, String)`, `sysMessageHeader()` — equivalentes directos de los que tenía `Apotheosis.java`, con el namespace/branding renombrados (`"Apoth"` → `"AscEq"` en el prefijo de chat).

### Fase 2 (2026-08-04)

Tras portar `item/` (4 clases) y `Apoth.java` → `AscEq.java` (completo, 29 subclases anidadas), el build quedó con **376 errores — todos** trazables a paquetes de fases futuras (`advancements`, `affix`, `gen`, `loot`, `mobs`, `particle`, `recipe`, `socket`, `tiers`) y a símbolos de esos paquetes referenciados desde archivos de Fase 1 (`EquipmentEvents`, `util/*`) y de `BossSummonerItem`. Verificado que **todos** los errores son `cannot find symbol`/`package does not exist` de clases de fases futuras (o cascadas de esos). Detalles reales encontrados:

1. **`MobEffect.isInstantenous()` ya no existe en MC 26.2 → `isInstantaneous()`.** El decompilado del original (26.1.2) llamaba `effect.isInstantenous()` en `PotionCharmItem.isValidPotion`. En MC 26.2 el método se llama `isInstantaneous()` (verificado con `javap` sobre `minecraft-patched-26.2.0.32-beta-merged.jar`). Fix aplicado en `item/PotionCharmItem.java:185`. **Esperado**: es un rename de vanilla entre 26.1.2 y 26.2, no un problema del port — pero hay que vigilar otras llamadas a `isInstantenous()` en fases futuras.
2. **Firma distinta en Common Toolkit: `structureProcessor` devuelve `MapCodec<T>`, no `StructureProcessorType<T>`.** El original de Placebo (por el tipo del campo en el Apoth decompilado) devolvía `StructureProcessorType<T>`; Common Toolkit lo declara como `MapCodec<T>` (`DeferredHelper.structureProcessor`, línea 392). Impacto: en `AscEq.Features.ITEM_FRAME_GEMS` el campo está declarado como `StructureProcessorType<ItemFrameGemsProcessor>` y, cuando exista `ItemFrameGemsProcessor` (Fase 9, `gen`), la asignación no compilará — habrá que cambiar el tipo del campo a `MapCodec<ItemFrameGemsProcessor>` (el propio `ItemFrameGemsProcessor.CODEC` sí es un `StructureProcessorType`, que es subtipo de `MapCodec`, así que la llamada en sí es válida). **No es un error actual** porque el símbolo no existe todavía; queda anotado para la Fase 9.
3. **Dependencia activada para esta fase: Ascendant Enchanting.** `AscEq.Items` usa `GlowyItem` (`com.skd.ascendantenchanting.objects.GlowyBlockItem.GlowyItem`) en `GOD_FUSED_PEARL` y `INFUSED_SPAWNER_RUNE`. Estaba "pendiente de activar (Fase 8/15)" en la tabla, pero la clase raíz la necesita ya; se activó `compileOnly`+`localRuntime` desde `lib_ext/` en `build.gradle` (mismo patrón que las demás) y se actualizó la tabla. El jar es 26.2 y resuelve correctamente (constructor `(Item.Properties)`).
4. **Falso positivo, sin fix**: `SizedUpgradeRecipe.onCraft` sigue marcando "method does not override" — misma cascada ya documentada en Fase 1: `ReactiveSmithingRecipe` (paquete `socket`, Fase 5) todavía no existe. Se resolverá solo cuando llegue esa fase.
5. **Posible bug en el original (portado fielmente, sin alterar): `AscEq.Items.rarity(String path)` ignora su parámetro** y siempre construye el holder con `AscendantEquipment.loc("uncommon")` (decompilado de `Apoth.java`). Las runas de upgrade (`FRONTIER`/`ASCENT`/`SUMMIT`/`PINNACLE`) pasan `"uncommon"/"rare"/"epic"/"mythic"` pero todas acabarían con rareza "uncommon". Puede ser un bug real del código fuente original o un artefacto del decompilador; se mantuvo el comportamiento literal del decompilado. Decidir en QA (Fase 17) si hay que usar `path`.

### Fase 3 (2026-08-04)

**Alcance ajustado**: se añadió `affix/augmenting/` (5 archivos) a esta fase — el roadmap original no la asignaba a ninguna fase, y usa directamente `tiers/augments` (ya incluido aquí). Total: `tiers/` (9), `affix/` raíz (11), `affix/effect/` (16), `affix/augmenting/` (5) = 41 archivos fuente (44 creados, algún fichero decompilado incluye clases anidadas que quedaron en archivos propios). Build con **706 errores**, todos trazables a fases futuras o cascadas de estas (verificado independientemente). OpenCode encontró y arregló varios bugs del decompilador (diamond operators `new ArrayList()` → `new ArrayList<>()`, variable shadowing en `AugmentingScreen`, argumento de más en el constructor de `UpdatingSlot`, `Either.map` mal generado en `TieredWeights`, method reference `Pair::getKey`/`Pair::getValue` no resuelto). Se quedó bloqueado por la misma restricción de sandbox de la Fase 1 (no puede escribir fuera del repo, `/tmp`) al intentar aislar un caso de prueba — completé la investigación yo mismo:

1. **`AscEq.Features.ITEM_FRAME_GEMS` — aplicado el fix ya anotado en Fase 2.** Cambiado el tipo de campo de `StructureProcessorType<ItemFrameGemsProcessor>` a `MapCodec<ItemFrameGemsProcessor>` para que coincida con `DeferredHelper.structureProcessor` de Common Toolkit. Import `StructureProcessorType` eliminado (quedaba sin uso).
2. **Inferencia de genéricos rota en cadenas `RegistrySerializer.subtypedSynced(...).register(...)`.** `AffixRegistry.SERIALIZER` y `TierAugmentRegistry.SERIALIZER` fallaban con "`Codec<X> cannot be converted to Codec<? extends R>`" — el compilador no lograba inferir `R` (p.ej. `Affix`) a través de toda la cadena de `.register(...)` encadenados a partir del contexto de asignación (`public static final SubtypedSerializer<Affix> SERIALIZER = ...`), pese a que `Affix implements CodecProvider<Affix>` cumple el bound `R extends CodecProvider<? super R>` correctamente. El original (con Placebo) compilaba con la misma sintaxis exacta — probable diferencia de versión/comportamiento de javac entre toolchains (este proyecto compila con Java 25). Fix: type witness explícito `RegistrySerializer.<Affix>subtypedSynced(...)` / `RegistrySerializer.<TierAugment>subtypedSynced(...)`. **Vigilar el mismo patrón en fases futuras** — cualquier registro con `subtyped`/`subtypedSynced` encadenado puede necesitar el mismo type witness.
3. **`FestiveAffix` — cast a interfaz inyectada por Mixin contra una clase `final`.** `(IFestiveMarker) stack` no compila porque `ItemStack` es `final` en MC 26.2 (confirmado leyendo el fuente real de `minecraft-patched-26.2.0.32-beta-sources.jar`) y no implementa `IFestiveMarker` — esa interfaz se inyecta vía `mixin/ItemStackMixin.java` (Fase 14, `mixin/`, aún no portada). El original compilaba porque en algún punto de su toolchain esta relación era visible en fuente; aquí no lo es hasta que exista el Mixin. Fix: el idiom estándar para casts a interfaces inyectadas por Mixin, `(IFestiveMarker)(Object) stack` (bypasa la comprobación de "inconvertible types" del compilador; el cast real solo se verifica en runtime, cuando el Mixin ya ha transformado la clase). Aplicado en las 4 ocurrencias de `FestiveAffix.java`. **Vigilar el mismo patrón para cualquier otro cast a una interfaz de `mixin/` antes de la Fase 14.**
4. **Falso positivo confirmado, sin fix (cascada de Fase 11 `client/`)**: `AugmentingScreen` marca 3 métodos `@Override` como "does not override" (`extractWidgetRenderState`, `extractContents`, `renderToolTip`) — sus clases anidadas extienden `DropDownList`/`SimpleTexButton` de `client/` (Fase 11), que no existe todavía. Mismo patrón que `SizedUpgradeRecipe.onCraft` en Fase 1/2 (interfaz/superclase no resuelta → cualquier `@Override` sobre ella falla igual). Se resolverá solo cuando llegue la Fase 11.
5. **`AscEq.java:264/272` — cascada ya conocida de Fase 2** (`RarityRegistry`/`GemRegistry` no existen, Fases 6/5). Sin cambios.
