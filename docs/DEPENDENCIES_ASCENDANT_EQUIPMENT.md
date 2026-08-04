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
| Ascendant Enchanting | Pendiente de activar (Fase 8/15, enchantments) |

## Incidencias registradas

### Fase 1 (2026-08-04)

Tras portar `util/`, `attachments/`, `event/`, `AdventureConfig`→`EquipmentConfig`, `AdventureEvents`→`EquipmentEvents`, el build quedó con 169 errores — **todos** trazables a paquetes de fases futuras (`affix`, `socket`, `loot`, `tiers`, `commands`, `net`, `mobs.util`, `AscEq`), como se esperaba. Se detectaron y corrigieron 3 problemas reales (no relacionados con fases futuras):

1. **`CanSocketGemEvent.setCanceled` — artefacto del decompilador, no cambio de API.** El original decompilado tenía `super.setCanceled(canceled)`, pero `Event` no declara ese método — vive como default method en `ICancellableEvent`. Vineflower renderiza mal las llamadas explícitas `Interfaz.super.metodo()`, quitando el calificador de interfaz. Fix: `ICancellableEvent.super.setCanceled(canceled)`. **Vigilar este patrón en fases futuras** — cualquier clase que implemente una interfaz con default methods y llame a `super.metodo()` puede tener el mismo problema silencioso (compila si hay una superclase con ese nombre de método, o falla como aquí si no la hay).
2. **`ApothMiscUtil` accedía a `PlayerAdvancements.progress` / `ClientAdvancements.progress` (campos privados de vanilla).** Faltaba portar el `META-INF/accesstransformer.cfg` del Apotheosis original — el scaffold del template no lo incluía. Se añadió el archivo completo (todas las entradas del original, incluidas las de fases futuras: SmithingRecipe, BaseSpawner, LevelRenderer, etc. — no rompen nada estando de más, se validan cuando toque su fase). NeoForge lo detecta automáticamente en esa ruta (sin declarar `[[accessTransformers]]` en el toml, confirmado por el propio comentario de la plantilla).
3. **`AffixItemIngredient.items()` — `BuiltInRegistries.ITEM.listElements()` devuelve `Stream<Reference<Item>>`, no asignable a `Stream<Holder<Item>>` por invarianza de genéricos.** Confirmado como comportamiento real y actual de MC 26.2 (no un bug de nuestro port) revisando el propio código fuente de Minecraft (`Registry.java`, que hace el mismo cast explícito internamente). Fix: `.listElements().<Holder<Item>>map(i -> i).filter(...)`.
4. **Falso positivo, sin fix**: `SizedUpgradeRecipe` marcaba `onCraft` como "no override" — es cascada de que `ReactiveSmithingRecipe` (paquete `socket`, Fase 5) todavía no existe, no un problema real. Se resolverá solo cuando llegue esa fase.

**Añadido a `AscendantEquipment.java`** (no eran del alcance original de Fase 1, pero varios archivos de `util/` los necesitaban y son helpers propios de la clase principal, sin dependencias de fases futuras): `loc(String)`, `lang(String, String, Object...)`, `langKey(String, String)`, `sysMessageHeader()` — equivalentes directos de los que tenía `Apotheosis.java`, con el namespace/branding renombrados (`"Apoth"` → `"AscEq"` en el prefijo de chat).
