# Ascendant Equipment (1.21.1) — Changelog

Branch `minecraft/1.21.1/neoforge-21.1.249/production`. History independent of the 26.2 branch.

## [0.0.0-beta.2] - 2026-09-01

### Fixed

- **Hard client crash on resource load**: `RegisterShadersEvent` (`AdventureModuleClient.shaderRegistry`)
  threw `FileNotFoundException: ascendant_equipment:shaders/core/gray.json`. The 26.2 → 1.21.1
  asset port carried only `shaders/core/gray.fsh`, in the post-1.21.1 GLSL dialect
  (`#moj_import <minecraft:dynamictransforms.glsl>`, `sphericalVertexDistance`, `apply_fog(...)`).
  Restored the full `gray.json` / `gray.vsh` / `gray.fsh` triplet verbatim from upstream
  Apotheosis `1.21` (`#version 150`, `linear_fog`), namespace remapped to `ascendant_equipment`.
- **Item models `gem_dust` and `godforged_pearl`**: `"layer0"` used the 1.21.4+ texture-object
  syntax (`{ "sprite": ..., "neoforge_data": { "light_emission": 15 } }`), unparseable on 1.21.1
  → items rendered as the missing-model placeholder. Converted to plain-string `layer0` (the
  per-texture `light_emission` hint is dropped; `.mcmeta` animations kept).
- **Item `god_fused_pearl`**: no `models/item/god_fused_pearl.json` existed (only a 1.21.4-style
  `assets/ascendant_equipment/items/` definition, which 1.21.1 ignores). Added
  `{ "parent": "ascendant_equipment:item/godforged_pearl" }`, matching the 26.2 definition's intent.

### Notes

- Verified: `./gradlew build` OK. Shader GLSL compilation is only checkable in-client (GL context);
  sources are an as-is port of upstream Apotheosis 1.21 for the same Minecraft version.

## [0.0.0-beta.1] - 2026-09-01

### Added

- **Initial port to Minecraft 1.21.1 / NeoForge 21.1.249** (Java 21). Strategy: the 26.2 fork
  tree (302 files, `com.skd.ascendantequipment` identity, deps pointing at our 1.21.1 fork jars)
  with all 26.2-only Minecraft/NeoForge API reverted to 1.21.1, using upstream Apotheosis 1.21
  (v8.7.0) as the 1:1 reference.
- Full Apotheosis feature set: rarity tiers, the affix framework and affix effects, sockets and
  gems (+ cutting / storage / bonuses), reforging and salvaging, loot integration, elite/invader
  mobs, boss spawners, gear sets, world-gen features, the guide book (via Vellumli), commands and
  networking.

### Technical

- API reversion (~200+ compile errors) delegated to `opencode-go/mimo-v2.5`:
  `Identifier` → `ResourceLocation`; `net.minecraft.advancements.predicates`/`.triggers` →
  `net.minecraft.advancements.critereon`; `GuiGraphicsExtractor` → `GuiGraphics`;
  `extractRenderState()` → `render()`; `pushMatrix`/`popMatrix` → `pushPose`/`popPose`;
  `projectile.arrow.AbstractArrow` → `projectile.AbstractArrow`; the 26.2 `SlotDisplay`
  recipe-book API removed.
- Mixin set aligned with upstream Apotheosis 1.21: `EnderDragonFightMixin` → `EndDragonFightMixin`;
  `EntityInvoker` folded into `EntityMixin`; the 26.2 render mixins
  (`AbstractSkeletonRendererMixin`, `GuiGraphicsExtractorMixin`, `GuiItemAtlasMixin`,
  `ItemStackRenderStateMixin`) dropped; `client/GuiGraphicsAccessor` and `client/SkeletonModelMixin`
  added. `access­transformer.cfg` and `mixins.json` replaced with upstream's 1.21.1 versions.
- Operator recovery of runtime bugs the compiler could not catch:
  `ItemStackMixin.apoth_tryTickMalice` signature (`EquipmentSlot` → `int, boolean`);
  `MobMixin.dropFromLootTable` descriptor (drop `ServerLevel`);
  `AscEq` `potion_charm_infusion` registers `CharmInfusionRecipe.Serializer.INSTANCE` (it was
  duplicate-registering Ascendant Enchanting's inherited serializer);
  `BasicBossData` restored to upstream's `List<SetPredicate>` gear-set form.
- **Data**: `recipe/`, `gear_sets/`, `affixes/`, `gems/`, `tier_augments/`, `apothic_elites/`,
  `apothic_invaders/` replaced with upstream Apotheosis 1.21's 1.21.1-format JSON (the
  26.2-generated format was unparseable on 1.21.1 — bare-string ingredients, `{count, ingredient}`
  smithing, missing gear-set `tags` field, `minecraft:attack_speed` instead of
  `minecraft:generic.attack_speed`). Namespaces renamed `apotheosis` / `apothic_*` / `placebo` /
  `patchouli` → our fork ids.
- **Dependency fixes this port required** (published as their own versions):
  - `common_toolkit` `0.0.0-beta.3` — new `GearSetRegistry.getRandomSet(RandomSource, float,
    List<GearSet.SetPredicate>)` overload so boss/invader data can carry `#tag` gear-set refs.
  - `regalia_slots_api` `0.0.0-beta.5` — strip the leading `#` when resolving entity-type tag
    refs in `curios/entities` data.
- Build: `net.neoforged.moddev` template retargeted to NeoForge 21.1.249 / Java 21;
  `compat/gateways/**` excluded from compilation (no Gateways build); `modLoader`/`loaderVersion`
  in `neoforge.mods.toml`; `jspecify:1.0.0` + JEI `19.21.2.313` + Jade `15.10.6` `compileOnly`.
- Verified: `./gradlew build` OK; `./gradlew runServer` → `Done (4.5s)`, all 7 mods load
  (ascendant_equipment + common_toolkit + ascendant_attributes + ascendant_spawners +
  ascendant_enchanting + vellumli + regalia_slots_api), every ascendant_equipment mixin applies,
  0 FATAL, 0 recipe/data parse errors.
- Port detail: `docs/PORT_REPORT_1.21.1.md`.
