# Ascendant Equipment

Ascendant Equipment is a loot, affix, socket and equipment-progression overhaul for Minecraft 1.21.1 (NeoForge). It is a re-fork of [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) by Shadows_of_Fire, rebuilt with original identifiers on top of in-house replacements for Placebo, Apothic Attributes, Apothic Spawners, Apothic Enchanting and Patchouli.

> This mod is a fork of [Apotheosis](https://www.curseforge.com/minecraft/mc-mods/apotheosis) by Shadows_of_Fire / Stormraven Studios. Not affiliated with or endorsed by the original author.

## Status

Beta (`0.0.0-beta.1`). Initial port to Minecraft 1.21.1 / NeoForge 21.1.249 (Java 21) — the 26.2 fork tree with all 26.2 API reverted to 1.21.1 using upstream Apotheosis 1.21 (v8.7.0) as the reference. `./gradlew build` and `./gradlew runServer` verified: all mods load, every mixin applies, no FATAL and no data-parse errors. Not yet play-tested in a client.

The Gateways optional-compat package (`compat/gateways/**`) is excluded from compilation — there is no Gateways build to depend on, same as the 26.2 line.

## Requirements

| Component | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.249+ |
| Java | 21+ |
| [Common Toolkit](https://gitlab.com/stalking-dragons/minecraft/common-toolkit) | Required (fork of Placebo) — `0.0.0-beta.3+` |
| [Ascendant Attributes](https://gitlab.com/stalking-dragons/minecraft/ascendant-attributes) | Required (fork of Apothic Attributes) |
| [Ascendant Spawners](https://gitlab.com/stalking-dragons/minecraft/ascendant-spawners) | Required (fork of Apothic Spawners) |
| [Ascendant Enchanting](https://gitlab.com/stalking-dragons/minecraft/ascendant-enchanting) | Required (fork of Apothic Enchanting) — `0.0.0-beta.2+` |
| [Vellumli](https://gitlab.com/stalking-dragons/minecraft/vellumli) | Required (fork of Patchouli, powers the guide book) |
| [Regalia Slots API](https://gitlab.com/stalking-dragons/minecraft/regalia-slots-api) | Optional (fork of Curios API) — `0.0.0-beta.5+` |
| JEI / Jade | Optional (compat only) |

## Installation

1. Install [NeoForge](https://neoforge.net/) for Minecraft 1.21.1.
2. Install Common Toolkit, Ascendant Attributes, Ascendant Spawners, Ascendant Enchanting and Vellumli.
3. Download the mod jar and place it in your `mods/` folder.

## License

MIT — see [LICENSE](LICENSE). Upstream Apotheosis copyright (`Copyright (c) 2018-2025 Stormraven Studios, LLC`) is retained; all in-game assets are original to this project. Required dependencies keep their own licenses (see LICENSE).
