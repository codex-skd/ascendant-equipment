# Changelog — Ascendant Equipment

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-08-10

First stable release.

### Fixed
- Gem tooltips: added the 21 missing gem name translations, all `gem_class.*` and `loot_category.*` category labels, and every `bonus.ascendant_equipment:*.desc` gem bonus description (durability, bloody arrow, leech block, all stats, mageslayer, multi-attribute) — previously shown as raw untranslated keys
- Potion Charm: item name now includes the potion effect (`Charm of %s`), and the missing `.desc`, `.desc3`, `.enabled`, `.disabled`, `.curios_only` tooltip lines were added
- `misc.ascendant_equipment.right_click_to_socket` now carries the gem/item name placeholders in both en_us and es_es (the message was silently dropping them)
- Removed two leftover placeholder lang keys (`gem_class.ascendant_equipment.`, `bonus.ascendant_equipment.`) that matched no real lookup
- es_es terminology: "Empotrar/Empotramiento" → "Acoplar/Acoplamiento" to match the established Apotheosis Spanish translation
- Normalized texture asset paths (`textures/blocks/` → `textures/block/`, `textures/items/` → `textures/item/`) and the model references pointing at them
- Removed stray build artifacts (`nul` files, misplaced root `assets/`/`com/` directories) that had leaked into the working tree

### Project
- Roadmap phases 16 (remaining art) and 17 (dedicated QA pass) closed as out of scope for this release — ships with the current placeholder art and without a dedicated parity QA phase

## [0.0.0-beta.19] - 2026-08-08

### Fixed
- Redeploy after verification of codec and registry initialization fixes

## [0.0.0-beta.18] - 2026-08-08

### Fixed
- **Critical**: `initCodecs()` calls were missing, causing all dynamic registry codec dispatchers (LootRule, SpawnCondition, EntityModifier, GemBonus) to be empty
- Rarities now load correctly (5/5: common, uncommon, rare, epic, mythic)
- Gems now load correctly (21/21 gems across all dimensions)
- Affixes now load correctly (93/93 with all categories: melee, ranged, armor, breaker, shield, generic)
- Invaders now load correctly (23/23 across overworld, nether, end)
- Elites and Augments now load and register properly
- Added missing `ConfigPayload` registration preventing config sync crash

### Known Issues
- `extra_gem_bonuses` registry empty (no data files shipped)
- `loot_category:charm` not recognized (deprecated category from upstream)

## [0.0.0-beta.15] - 2026-08-08

### Fixed
- **Critical**: Dynamic registries (RarityRegistry, GemRegistry, AffixRegistry, etc.) never received data from datapacks because `registerToBus()` calls were missing in `FMLCommonSetupEvent`
- JEI recipe extensions (Malice, Supremacy, Unnaming) no longer crash with `NoSuchElementException` on empty rarities list
- Added defensive empty-list checks in JEI extensions as secondary safeguard
- Registered `EquipmentEvents` and `AscEqMobEvents` on `NeoForge.EVENT_BUS` (event handlers were never active)
- Registered all network payloads (BossSpawn, RerollResult, RadialState, WorldTier, LinkItemToChat, GemCaseSelect)
- Added Mixin annotation processor for refmap generation in build output

## [0.0.0-beta.14] - 2026-08-08

### Fixed
- **Critical**: Item textures failed to load due to missing `src/main/resources` in Gradle build configuration
- Updated sourceSets.main.resources to explicitly include manual asset sources alongside generated resources
- All item textures now render correctly (gem_dust, materials, sigils, runes, etc.)
- Proper asset pipeline processing for models, textures, and blockstates

## [0.0.0-beta.13] - 2026-08-07

### Fixed
- **Critical**: Creative Mode tab was empty despite items being registered
- Implemented `TabFillingRegistry` to populate the Ascendant: Adventure tab with all mod items
- All 49 items from `AscEq.Items` now appear in creative mode (materials, gems, sigils, tables, etc.)
- Items now display with correct textures (no more magenta missing textures)

## [0.0.0-beta.12] - 2026-07-XX

### Added
- Initial port of Apotheosis 26.1 to NeoForge 26.2
- Core equipment and affix systems
- Gem socket system
- Spawner system
- World tier progression

---

For detailed version history, see individual release notes on CurseForge.
