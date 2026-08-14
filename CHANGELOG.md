# Changelog — Ascendant Equipment

All notable changes to this project will be documented in this file.

## [1.0.5] - 2026-08-14

### Fixed
- World Tier map lock tooltip: `button.tier_locked`, `button.tier_advancement` and `info.criteria_{done,unfinished,unknown}` were translated as static generic strings with no `%s`, so the tier name, advancement title, and per-criterion text passed by `WorldTierSelectScreen#tierLocked` were silently dropped — every locked tier showed the same generic "Tier Locked" / "Incomplete x5" instead of "Frontier (Locked)" / "Equip a Common Chestplate". Restored the original Apotheosis `%s` + checkbox-glyph templates
- Added the missing `button.ascendant_equipment.{frontier,ascent,summit,pinnacle}` tier-name keys — only `haven` existed, so those 4 tiers had nothing to substitute into the fixed `%s` templates

## [1.0.4] - 2026-08-13

### Fixed
- World Tier map: hovering a tier icon (Haven, Frontier, Ascent, Summit, Pinnacle) never showed its tooltip. `init()` recreated all buttons on every call instead of only the first time, leaving stale/duplicate widgets after a resize; and the tooltip queue for the tier buttons wasn't being honored by the screen's split render pipeline (`extractBackground`/`extractRenderState`/`extractContents`)

## [1.0.2] - 2026-08-12

### Fixed
- **Attribute modifier tooltips (regression, all items)**: `neoforge.modifier.plus`/`neoforge.modifier.take` were overridden with a one-argument `"+%d"`/`"-%d"` template, but NeoForge's `IAttributeExtension#toComponent()` calls these keys with two arguments (value, attribute name) to build every attribute-modifier tooltip line in the game. The broken override silently dropped the attribute name from every affixed item's stat lines, leaving only an icon and a bracketed value. Restored NeoForge's own `"+%s %s"` / `"%s %s"` templates
- `LootRarity#toComponent` built its translation key with a colon instead of a dot, never matching any lang entry and leaking the raw truncated key (`_equipment:common: 60%`) into drop-probability tooltips
- Added the missing `rarity.ascendant_equipment.*` (common/uncommon/rare/epic/mythic) and `purity.ascendant_equipment.*` (cracked/chipped/flawed/normal/flawless/perfect) keys — existed in code, never in lang
- Added `button.ascendant_equipment.haven`, missing unlike every other world tier button key
- Added the 5 missing damage-type labels (fire/fall/explosion/projectile/lightning) used by damage-reduction affix descriptions; only physical/magic existed, so e.g. fall-damage-reduction gear showed the raw key `misc.ascendant_equipment.fall`
- Dropped the unsubstituted `%s` from the World Tier difficulty label (never received an argument; difficulty is shown via the sword icons instead)
- es_es: fixed the typo "Raridad" → "Rareza"

## [1.0.1] - 2026-08-11

### Fixed
- Advancement `translate` keys (progression + challenge gates: Haven, Frontier, Ascent, Summit, Pinnacle and their gateways) pointed at the origin mod's namespace instead of `ascendant_equipment`, falling back to raw untranslated text
- Added the missing title/desc/criteria text for all 6 progression advancements and 5 challenge gates — these keys had never been written, not just misnamed
- Added the missing world tier name/description keys (`text.ascendant_equipment.world_tier.<tier>[.desc]`) and the 6 World Tier tutorial stage title/desc keys, previously showing raw lang keys on screen

### Project
- Reduced shipped locales to en_us and es_es; the other 10 never had the keys above translated either and are deferred to a future localization pass

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

## [1.0.3] - 2026-08-12

### Change

- **Nombre de JAR con versión del cargador**: el artefacto ahora se compila como `ascendant_equipment-26.2-neoforge-26.2.0.37-beta-1.0.3.jar` (se añade la versión de cargador/NeoForge al nombre del archivo). Empaquetado y documentación; sin cambios de funcionalidad.


For detailed version history, see individual release notes on CurseForge.
