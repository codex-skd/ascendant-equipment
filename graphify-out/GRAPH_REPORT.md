# Graph Report - .  (2026-08-04)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 48 nodes · 68 edges · 11 communities (6 shown, 5 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 310 input · 98 output

## Graph Freshness
- Built from commit: `a0b2d1dc`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Ascendant Equipment
- Mod Configuration
- Client Setup
- Build Tools
- Event Handling
- Server Startup
- Creative Mode Tab
- Common Setup
- Mod Icon

## God Nodes (most connected - your core abstractions)
1. `AscendantEquipment` - 17 edges
2. `Config` - 7 edges
3. `AscendantEquipmentClient` - 5 edges
4. `Mod Icon` - 0 edges

## Surprising Connections (you probably didn't know these)
- `AscendantEquipment` --references--> `Item`  [EXTRACTED]
  src/main/java/com/skd/ascendantequipment/AscendantEquipment.java →   _Bridges community 0 → community 1_

## Import Cycles
- None detected.

## Communities (11 total, 5 thin omitted)

### Community 0 - "Ascendant Equipment"
Cohesion: 0.28
Nodes (12): Block, BlockItem, Blocks, CreativeModeTab, DeferredBlock, DeferredHolder, DeferredItem, DeferredRegister (+4 more)

### Community 1 - "Mod Configuration"
Cohesion: 0.25
Nodes (7): BooleanValue, Builder, ConfigValue, IntValue, Item, ModConfigSpec, Config

### Community 2 - "Client Setup"
Cohesion: 0.36
Nodes (6): EventBusSubscriber, FMLClientSetupEvent, AscendantEquipmentClient, Mod, ModContainer, SubscribeEvent

### Community 3 - "Build Tools"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **1 isolated node(s):** `Mod Icon`
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AscendantEquipment` connect `Ascendant Equipment` to `Mod Configuration`, `Event Handling`, `Server Startup`, `Creative Mode Tab`, `Common Setup`?**
  _High betweenness centrality (0.261) - this node is a cross-community bridge._
- **What connects `Mod Icon` to the rest of the system?**
  _1 weakly-connected nodes found - possible documentation gaps or missing edges._