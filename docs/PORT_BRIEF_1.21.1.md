# Delegation brief — Ascendant Equipment: finish the 1.21.1 / NeoForge 21.1.249 port

## Mission

`ascendant_equipment` (our re-fork of **Apotheosis** by Shadows_of_Fire / Stormraven
Studios, MIT) currently exists only for Minecraft 26.2. We are creating a
**Minecraft 1.21.1 / NeoForge 21.1.249 / Java 21** version.

The scaffold is done. `src/main/java` currently holds the **26.2 fork's source,
unchanged** (302 files, already renamed to the `com.skd.ascendantequipment` identity,
deps already pointing at our 1.21.1 fork jars). It **does not compile on 1.21.1** —
`./gradlew compileJava` reports **~200+ errors**, essentially all from **26.2-only
Minecraft / NeoForge API** that must be reverted to its 1.21.1 form.

**The reference that makes this tractable**: `temp/ref/apotheosis-1.21-java/` is the
**upstream Apotheosis 1.21 source** (v8.7.0, MC 1.21.1, NeoForge 21.1.235, Java 21) —
i.e. the exact code the 26.2 fork was ported *up* from. Every broken symbol in our
tree has a working 1.21.1 counterpart in that reference. Your job is mostly:
*open the same-named file in `apotheosis-1.21-java` and use its 1.21.1 API*, keeping
our identity renames and any deliberate 26.2-fork behaviour changes.

## Paths (all inside the work dir — sandbox blocks reads outside `--dir`)

| What | Path |
|---|---|
| **Work dir** (edit here) | `G:/Proyectos/Mods_Minecraft/ascendant_equipment/neoforge/1.21.1` |
| Upstream Apotheosis 1.21 — Java (**1.21.1 API truth**) | `temp/ref/apotheosis-1.21-java/dev/shadowsoffire/apotheosis/` |
| Upstream Apotheosis 1.21 — resources | `temp/ref/apotheosis-1.21-resources/` |
| Upstream — gradle.properties | `temp/ref/apotheosis-1.21-gradle.properties` |
| The **26.2 fork** — Java (identity + scope reference, NOT API) | `temp/ref/ascendant-equipment-26.2-java/com/skd/ascendantequipment/` |
| 26.2 fork — build.gradle | `temp/ref/ascendant-equipment-26.2-build.gradle` |

`temp/` is gitignored.

### Identity rename map (already applied in `src/`, keep it)

- package `dev.shadowsoffire.apotheosis` → `com.skd.ascendantequipment`
- `dev.shadowsoffire.placebo` → `com.skd.commontoolkit`
- `dev.shadowsoffire.apothic_attributes` → `com.skd.ascendantattributes`
- `dev.shadowsoffire.apothic_enchanting` → `com.skd.ascendantenchanting`
- `dev.shadowsoffire.apothic_spawners` → `com.skd.ascendantspawners`
- `vazkii.patchouli` → `com.skd.vellumli`
- `top.theillusivec4.curios` → **stays verbatim** (Regalia Slots API ships the
  `top.theillusivec4.curios.api` package unchanged — do NOT rename curios imports)
- class `Apotheosis` (main `@Mod`) → `AscendantEquipment`; `Apoth` (the giant
  registry-holder) → `AscEq`; `AdventureConfig`/`AdventureEvents` →
  `EquipmentConfig`/`EquipmentEvents`; `ApothicAttributes` → `AscendantAttributes`,
  `ALObjects` → `AscendantAttributesObjects`, `ALConfig` → `AttributesConfig`,
  `PlaceboUtil`/`Placebo*` → `CommonToolkit*`
- modid `apotheosis` → `ascendant_equipment`; asset/data namespace `apotheosis:` →
  `ascendant_equipment:`

## Scaffold already done (do NOT redo)

- `src/main/java` = 26.2 fork tree (renamed identity), 302 files.
- `src/main/resources` = 26.2 fork resources (already `ascendant_equipment` namespace).
- `gradle.properties`: MC 1.21.1, range `[1.21.1,1.22)`, neo `21.1.249`,
  `loader_version_range=[1,)`, `mod_version=0.0.0-beta.1`, `mod_license=MIT`.
- `build.gradle`: Java 21; `mavenCentral()`; JEI `mezz.jei:jei-1.21.1-*:19.21.2.313`,
  Jade `maven.modrinth:jade:15.10.6+neoforge`, `org.jspecify:jspecify:1.0.0`;
  our six fork jars in `libs/` (`compileOnly` + `localRuntime`); `loader_version_range`
  in the `generateModMetadata` map; **`compat/gateways/**` excluded from compilation**
  (no Gateways build — matches the 26.2 line); `processResources` `duplicatesStrategy = 'include'`.
- `src/main/templates/META-INF/neoforge.mods.toml`: `modLoader`/`loaderVersion` added;
  deps unchanged (common_toolkit / ascendant_attributes / ascendant_spawners /
  ascendant_enchanting / vellumli required; regalia_slots_api / jei / jade optional).
- `src/main/resources/ascendant_equipment.mixins.json`: **replaced with the upstream
  1.21 mixin set** (renamed), `JAVA_21`.
- `src/main/resources/META-INF/accesstransformer.cfg`: **replaced with the upstream
  Apotheosis 1.21 AT** (1.21.1-correct descriptors).

## HARD CONSTRAINTS

1. **Target API = Minecraft 1.21.1 + NeoForge 21.1.249 + Java 21.** Upstream
   Apotheosis 1.21 (`temp/ref/apotheosis-1.21-java`) is the API truth. Known 26.2 →
   1.21.1 reversions you WILL hit repeatedly:
   - `net.minecraft.resources.Identifier` → `net.minecraft.resources.ResourceLocation`
     (`Identifier.of(...)`/`.fromNamespaceAndPath(...)` → `ResourceLocation.fromNamespaceAndPath(...)`
     / `ResourceLocation.parse(...)` — match upstream).
   - `net.minecraft.advancements.predicates.*` and `net.minecraft.advancements.triggers.*`
     (26.2) → `net.minecraft.advancements.critereon.*` (1.21.1). The whole `advancements/`
     and `advancements/predicates/` packages need this — check each against upstream
     `temp/ref/apotheosis-1.21-java/.../advancements/`.
   - `net.minecraft.core.component.predicates.DataComponentPredicate` (26.2 location) →
     the 1.21.1 `net.minecraft.advancements.critereon.DataComponentPredicate` (or
     wherever upstream imports it).
   - `GuiGraphicsExtractor` → `GuiGraphics`; `extractRenderState()` → `render(...)`;
     `pushMatrix()/popMatrix()` → `pushPose()/popPose()`; `submit*` render calls →
     the 1.21.1 direct-draw form — all per upstream.
   - `net.minecraft.client.input.*` (26.2) → `com.mojang.blaze3d.platform.InputConstants`
     / plain keycodes per upstream.
   - `net.minecraft.world.entity.projectile.arrow.AbstractArrow` (26.2) →
     `net.minecraft.world.entity.projectile.AbstractArrow` (1.21.1). (The AT already
     uses the 1.21.1 path.)
   - Registration / codec / `DataComponent` / payload / `RecipeManager` /
     `Holder`/`HolderSet` / `RegistryOps` APIs: the 1.21.1 form from the matching
     upstream file.
2. **Mixins**: the mixin package must match **upstream Apotheosis 1.21**'s mixin set,
   not the 26.2 fork's. Concretely, in `src/main/java/com/skd/ascendantequipment/mixin/`:
   - rename `EnderDragonFightMixin` → `EndDragonFightMixin`; delete `EntityInvoker`
     (upstream folded it into `EntityMixin` — move any needed `@Invoker` there).
   - client: delete the 26.2-render mixins `client/AbstractSkeletonRendererMixin`,
     `client/GuiGraphicsExtractorMixin`, `client/GuiItemAtlasMixin`,
     `client/ItemStackRenderStateMixin`; add upstream's `client/GuiGraphicsAccessor`
     and `client/SkeletonModelMixin` (down-port from
     `temp/ref/apotheosis-1.21-java/.../mixin/client/`). Keep `client/EntityRendererMixin`,
     `client/GuiMixin`, `client/MultiPlayerGameModeMixin` but revert their API.
   - `mixins.json` is already the upstream 1.21 list — make the classes match it.
3. **Match the 26.2 fork's non-mixin FILE SET** otherwise. Do not re-add upstream
   files the 26.2 fork deliberately dropped (e.g. the `data/` datagen providers if the
   fork replaced them with committed JSON — check `temp/ref/ascendant-equipment-26.2-java`).
   The only deletions allowed are 26.2-only shims with no 1.21.1 role once their API is
   reverted — note each in the report.
4. **`compat/gateways/**` stays in the tree but is excluded from compilation** (build.gradle
   already does this). Do not try to make it compile and do not delete it.
5. **Do NOT run git or gradle.** The operator builds and verifies.
6. **Do NOT bump any dependency or the NeoForge version.** Our fork jars in `libs/` are
   fixed; JEI `19.21.2.313`, Jade `15.10.6`, jspecify `1.0.0`, NeoForge `21.1.249`.
7. License headers: preserve verbatim (MIT, Stormraven copyright). Do not rename
   `Apotheosis`/`Apoth` inside license headers or upstream-describing Javadoc.
8. All code / comments / your report: **English**.

## TASK — make `src/main/java` compile on 1.21.1

Work package by package. Suggested order (leaves → roots):
`util/` → `attachments/` → `advancements/` + `advancements/predicates/` →
`tiers/` → `affix/` (+ `affix/effect/`, `affix/augmenting/`, `affix/reforging/`,
`affix/salvaging/`) → `socket/` (+ `gem/**`) → `loot/` → `recipe/` →
`spawner/` → `mobs/` → `gen/` → `item/` → `net/` → `commands/` → `particle/` →
`event/` → `client/` → `compat/` (except `gateways/`) → `mixin/` (+ `mixin/client/`) →
the root classes (`AscendantEquipment`, `AscEq`, `EquipmentConfig`, `EquipmentEvents`).

For each failing symbol: open the same-named file under
`temp/ref/apotheosis-1.21-java/`, apply the 1.21.1 API it uses, keep our renames.
When unsure whether a line is a fork change or a 26.2-API change, diff against
`temp/ref/ascendant-equipment-26.2-java` — fork changes stay, 26.2-API changes revert.

## Deliverable

1. `src/main/java` correct for `./gradlew build` on NeoForge 21.1.249 by inspection
   (the `compat/gateways/**` package is excluded — ignore it).
2. `docs/PORT_REPORT_1.21.1.md` (English): every file modified / renamed / deleted with
   the reason; every 26.2 → 1.21.1 API reversion pattern applied; anything where you
   guessed at the 1.21.1 API; any 26.2 feature/file intentionally dropped.

Work only inside `G:/Proyectos/Mods_Minecraft/ascendant_equipment/neoforge/1.21.1`.
