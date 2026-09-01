<h1 align="center">&#9876;&#65039; Ascendant Equipment</h1>

<p align="center"><strong>Affixes, sockets, gems, rarities, reforging and boss-tier loot &mdash; a full equipment-progression overhaul.</strong></p>

<p align="center">
<img src="https://img.shields.io/badge/loader-NeoForge-orange?style=plastic&logo=curseforge" alt="NeoForge">
<img src="https://img.shields.io/badge/minecraft-26.2%20%7C%201.21.1-blue?style=plastic" alt="Minecraft 26.2 and 1.21.1">
<img src="https://img.shields.io/badge/type-adventure-brightgreen?style=plastic" alt="Adventure">
<img src="https://img.shields.io/badge/license-MIT-lightgrey?style=plastic" alt="MIT License">
</p>

<br>

---

<br>

<h2>&#10024; Overview</h2>

<table>
<tr>
<td width="65%">
<p>Ascendant Equipment rebuilds gear progression from the ground up: randomized <strong>affixes</strong> on weapons and armor, socketable <strong>gems</strong>, item <strong>rarities</strong>, <strong>reforging / salvaging / augmenting</strong> workstations, an in-game guide book, boss-tier <strong>rogue spawners</strong> and elite/invader mobs, world tiers, and a large library of enchantments and attributes &mdash; all with fully original identifiers.</p>

<p>A re-fork of <a href="https://www.curseforge.com/minecraft/mc-mods/apotheosis"><strong>Apotheosis</strong></a> by <em>Shadows_of_Fire</em> / Stormraven Studios (MIT), rebranded and rebuilt on in-house replacements for Placebo, Apothic Attributes, Apothic Spawners, Apothic Enchanting and Patchouli. Not affiliated with or endorsed by the original author. No original art or assets from Apotheosis are included.</p>
</td>
<td width="35%" align="center">
<a href="https://codex.skdragons.com/" target="_blank"><img src="https://node-files.skdragons.com/uploads/MINECRAFT/Codex/logo_codex_stalking_dragons.png" alt="Codex Stalking Dragons" width="160"></a>
</td>
</tr>
</table>

<br>

<h2>&#127919; Affixes &amp; Rarities</h2>

<p>Gear rolls with a rarity (common &rarr; mythic) and a set of affixes drawn from that rarity's pool &mdash; stat bonuses, on-hit effects, movement and utility abilities. Higher world tiers unlock stronger rolls.</p>

<br>

<h2>&#128142; Sockets &amp; Gems</h2>

<p>Add sockets to gear at the Gem Cutting Table, then slot cut gems for set bonuses. Gems have their own rarity/purity system and a storage case for organizing a collection.</p>

<br>

<h2>&#128296; Workstations</h2>

<p>Reforging Table (re-roll affixes), Salvaging Table (break gear down into materials and affix dust), and the Augmenting Table (apply gem augments). Each has a simple and an advanced tier.</p>

<br>

<h2>&#128128; Bosses &amp; Rogue Spawners</h2>

<p>Elite mobs and invaders spawn with gear sets, custom names and bonus loot. Rogue spawners generate in the world as boss arenas scaled to the local world tier.</p>

<br>

<h2>&#129521; Mod Structure</h2>

<table>
<tr><th align="left">Area</th><th align="left">What it provides</th></tr>
<tr><td><code>affix</code></td><td>The affix framework, effects, and the reforging / salvaging / augmenting systems.</td></tr>
<tr><td><code>socket</code></td><td>Sockets, gems, gem bonuses, cutting and storage.</td></tr>
<tr><td><code>loot</code></td><td>Rarity, loot categories, and the loot-table integration that applies affixes to drops.</td></tr>
<tr><td><code>mobs</code> / <code>spawner</code> / <code>gen</code></td><td>Elite/invader mobs, boss spawners, and the world-gen features that place them.</td></tr>
<tr><td><code>tiers</code></td><td>World tiers and the weighting that scales rolls to progression.</td></tr>
<tr><td><code>compat</code></td><td>JEI and Jade integration &mdash; optional. Gateways compat ships in the source but is not built (no Gateways release to depend on).</td></tr>
</table>

<br>

<h2>&#129513; Required Dependencies</h2>

<p>In-house replacements for Apotheosis's original dependencies, published separately:</p>

<ul>
<li><strong>Common Toolkit</strong> &mdash; registry / utility library (fork of Placebo).</li>
<li><strong>Ascendant Attributes</strong> &mdash; extra attributes, effects and damage types.</li>
<li><strong>Ascendant Spawners</strong> &mdash; spawner utilities and rogue-spawner support.</li>
<li><strong>Ascendant Enchanting</strong> &mdash; enchantment infrastructure.</li>
<li><strong>Vellumli</strong> &mdash; in-game guide-book library (powers this mod's guide book).</li>
</ul>

<p><strong>Regalia Slots API</strong> (fork of Curios API) is an optional integration. JEI and Jade are supported when installed.</p>

<br>

<h2>&#128203; Requirements</h2>

<table>
<tr><td><strong>Minecraft / NeoForge / Java</strong></td><td>see <em>Available Versions</em> below</td></tr>
<tr><td><strong>Dependencies</strong></td><td>Common Toolkit, Ascendant Attributes, Ascendant Spawners, Ascendant Enchanting, Vellumli (all required)</td></tr>
<tr><td><strong>Side</strong></td><td>Client and Server (required on both)</td></tr>
</table>

<br>

<h2>&#128230; Available Versions</h2>

<table>
<tr><th align="left">Minecraft</th><th align="left">NeoForge</th><th align="left">Java</th><th align="left">Latest build</th><th align="left">Status</th></tr>
<tr><td>26.2</td><td>26.2.0.57+</td><td>25</td><td><code>1.2.2</code></td><td>Stable</td></tr>
<tr><td>1.21.1</td><td>21.1.249+</td><td>21</td><td><code>0.0.0-beta.1</code></td><td>Beta &mdash; re-fork port from upstream Apotheosis 1.21</td></tr>
</table>

<p><em>Both versions share this CurseForge project. Pick the file that matches your Minecraft version.</em></p>

<br>

---

<br>

<h2>&#128591; Credits &amp; License</h2>

<p>Ascendant Equipment is a fork of <a href="https://www.curseforge.com/minecraft/mc-mods/apotheosis">Apotheosis</a> by <strong>Shadows_of_Fire</strong> / <strong>Stormraven Studios, LLC</strong>, rebranded and rebuilt by <strong>Stalking Dragons</strong>. The <code>1.21.1</code> build is a re-fork from the upstream Apotheosis 1.21 sources.</p>

<p><strong>License:</strong> <strong>MIT</strong>, same as upstream (the original <code>Copyright (c) 2018-2025 Stormraven Studios, LLC</code> notice is kept in the jar and repository <code>LICENSE</code>). All in-game assets are original to this project. Required dependencies keep their own licenses (depended on, not bundled): Common Toolkit LGPL-2.1-or-later; Ascendant Attributes / Spawners / Enchanting MIT; Vellumli CC BY-NC-SA 3.0; Regalia Slots API (optional) LGPL-3.0-or-later.</p>

<br>
<br>

<p align="center">
  <a href="https://codex.skdragons.com/" target="_blank">
    <img src="https://node-files.skdragons.com/uploads/MINECRAFT/Codex/logo_codex_stalking_dragons.png" alt="Codex Stalking Dragons" width="200">
  </a>
  <br>
  <a href="https://codex.skdragons.com/">https://codex.skdragons.com/</a>
  <br>
  <em>Codex Stalking Dragons &mdash; Minecraft Modding</em>
</p>
