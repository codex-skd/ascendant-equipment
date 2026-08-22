# Changelog — Ascendant Equipment


## [1.2.1] - 2026-08-23

### Change

- **Transparent gems for unowned items**: gems in the Gem Case that the player does not own now render partially transparent to distinguish them from owned gems, improving the visual experience when opening the gem chest.

## [1.2.0] - 2026-08-22

### Feature

- **Full migration of Patchouli guidebook to Vellumli**: the "Shadows Chronicle" guidebook previously depended entirely on the real Patchouli mod, which was not even a declared dependency of this mod (it only "worked" in test packs because they happened to have Patchouli installed for another reason). Ported the 5 remaining languages (ja_jp, pt_br, tr_tr, uk_ua, zh_cn) to the new Vellumli system, completely removed old Patchouli content, and fixed the recipe to depend on Vellumli instead.
- **Spanish translation of the guidebook**: 126 content files plus name/subtitle/welcome keys.
- **Automatic guidebook delivery**: each player receives it on first connection (persists through death/reconnection); if lost, it can be crafted again (book + gold ingot).

### Fix

- **Guidebook not appearing in creative tab or JEI**: the `creative_tab` field of the book pointed to a non-existent vanilla tab (`tools_and_utilities`, which does not exist in this Minecraft version — the actual tab is simply `tools`). Fixed, and additionally registered explicitly in the mod's own Adventure tab to guarantee appearance.

## [1.1.1] - 2026-08-21

### Fixed

- **Tower chests generating empty**: the 4 tower structure variants (`tower_leaf`, `tower_main`, `tower_sand`, `tower_spruce`) had their chest `LootTable` tag pointing to `apotheosis:chests/tome_tower`, a leftover from when the structure was exported from the original Apotheosis without renaming the namespace. Since this mod does not depend on Apotheosis, the loot table never resolved and the server logged `does not exist or could not be loaded`, leaving the chest empty. Redirected to the mod's own loot table `ascendant_equipment:chests/tome_tower`, which already existed but was never referenced by the built structures. Only affects towers generated after this update; existing worlds with broken data remain in their chunks.

## [1.1.0] - 2026-08-20

### Change

- **NeoForge update**: updated from 26.2.0.45-beta to 26.2.0.57.
- **Unified configuration**: the actual config module (`EquipmentConfig`, bosses/augmenting/spawners/curios/etc) now loads and persists properly in `config/ascendant/equipment/ascendant_equipment.cfg`. Before, its load was never invoked, so the file was never generated and only default in-memory values were used.
- **Curios → Regalia Slots API**: the data condition enabling the "charm" slot now depends on our own `regalia_slots_api` mod (fork-compatible Curios) instead of `curios`.

## [1.0.10] - 2026-08-18

### Change

- **JAR version with loader version**: the artifact now compiles as `ascendant_equipment-26.2-neoforge-26.2.0.45-beta-1.0.10.jar`.
- **Workflow documentation updated**: `docs/WORKFLOW_ASCENDANT_EQUIPMENT_26-2.md` updated to reflect the new working branch.

## [1.0.9] - 2026-08-18

### Fixed

- `bonus.ascendant_equipment:enchantment.desc` (and its `.global`/`.mustExist` variants), used by `EnchantmentBonus` and `EnchantmentAffix`, was missing from both lang files — any gem or affix that grants or boosts an enchantment showed the raw translation key instead of its description
- `misc.ascendant_equipment.{iron,diamond,netherite}` were also missing: `OmneticBonus`/`OmneticAffix` build this key at runtime from the `"name"` field in the gem/affix data JSON (e.g. `gems/the_nether/molten_breach.json`, `affixes/breaker/effect/omnetic.json`), so the raw tier name leaked into the "effectiveness against all blocks" tooltip line

## [1.0.8] - 2026-08-18

### Fixed

- **Gem Case / Ender Gem Case**: both blocks were registered with `requiresCorrectToolForDrops()`, so breaking one with the wrong tool (or by hand) removed the block with zero drops -- the exact vanilla behavior for ores mined without a pickaxe. `GemCaseBlock.getDrops()` only embeds the stored gems into the dropped item when drops are actually generated, so every gem inside was permanently lost with nothing on the ground. Removed the tool requirement to match vanilla chests/barrels, which never gate their drops on tool choice

## [1.0.7] - 2026-08-17

### Fixed

- **Curios compatibility registration**: Added missing `CuriosCompat.register()` call in commonSetup to prevent "Unknown registry key: ascendant_equipment:charm" server crash

## [1.0.6] - 2026-08-14

### Fixed

- `button.ascendant_equipment.activate_tier` (the "Activate" button's tooltip on the World Tier map) lost the `": %s"` tier-name placeholder from the original Apotheosis template, so it showed a static "Activate Tier" instead of "Activate World Tier: Frontier". Found during a follow-up audit of the same lang file after the v1.0.5 fix
- Added the missing `button.ascendant_equipment.{frontier,ascent,summit,pinnacle}` tier-name keys — only `haven` existed, so those 4 tiers had nothing to substitute into the fixed `%s` templates

## [1.0.5] - 2026-08-14

### Fixed

- **World Tier map lock tooltip**: `button.tier_locked`, `button.tier_advancement` and `info.criteria_{done,unfinished,unknown}` were translated as static generic strings with no `%s`, so the tier name, advancement title, and per-criterion text passed by `WorldTierSelectScreen#tierLocked` were silently dropped — every locked tier showed the same generic "Tier Locked" / "Incomplete x5" instead of "Frontier (Locked)" / "Equip a Common Chestplate". Restored the original Apotheosis `%s` + checkbox-glyph templates
- Added the missing `button.ascendant_equipment.{frontier,ascent,summit,pinnacle}` tier-name keys — only `haven` existed, so those 4 tiers had nothing to substitute into the fixed `%s` templates

## [1.0.4] - 2026-08-13

### Fixed

- **World Tier map**: hovering a tier icon (Haven, Frontier, Ascent, Summit, Pinnacle) never showed its tooltip. `init()` recreated all buttons on every call instead of only the first time, leaving stale/duplicate widgets after a resize; and the tooltip queue for the tier buttons wasn't being honored by the screen's split render pipeline (`extractBackground`/`extractRenderState`/`extractContents`)

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