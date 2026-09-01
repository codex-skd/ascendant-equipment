# Port Report — Ascendant Equipment 1.21.1 / NeoForge 21.1.249

## Summary

Successfully reverted ~200+ compilation errors from 26.2-only Minecraft/NeoForge API to their 1.21.1 equivalents. The build now compiles cleanly with `./gradlew compileJava`.

---

## 26.2 → 1.21.1 API Reversion Patterns Applied

### 1. ResourceLocation (67 files)
- `net.minecraft.resources.Identifier` → `net.minecraft.resources.ResourceLocation`
- `Identifier` type usages → `ResourceLocation`
- `net.minecraft.IdentifierException` → `net.minecraft.ResourceLocationException`
- `IdentifierArgument` → `ResourceLocationArgument` (commands)
- `IdentifierArgument.id()` → `ResourceLocationArgument.id()`
- `IdentifierArgument.getId()` → `ResourceLocationArgument.getId()`

### 2. Advancements/Critereon (9 files)
- `net.minecraft.advancements.predicates.*` → `net.minecraft.advancements.critereon.*`
- `net.minecraft.advancements.predicates.entity.*` → `net.minecraft.advancements.critereon.*`
- `net.minecraft.advancements.triggers.Criterion` → `net.minecraft.advancements.Criterion`
- `net.minecraft.advancements.triggers.SimpleCriterionTrigger` → `net.minecraft.advancements.critereon.SimpleCriterionTrigger`
- `DataComponentMatchers.ANY` → `DataComponentPredicate.EMPTY`
- `TypeAwareDCP` → `TypeAwareISP` (extends `ItemSubPredicate` instead of `DataComponentPredicate`)
- `DataComponentPredicate.Type` → `ItemSubPredicate.Type`
- `SingleComponentItemPredicate` package: `predicates` → `critereon`

### 3. DataComponentPredicate (6 files)
- `net.minecraft.core.component.predicates.DataComponentPredicate` → `net.minecraft.core.component.DataComponentPredicate`

### 4. Projectile Arrow (6 files)
- `net.minecraft.world.entity.projectile.arrow.AbstractArrow` → `net.minecraft.world.entity.projectile.AbstractArrow`

### 5. Weighted / WeightedRandom (10 files)
- `net.minecraft.util.random.Weighted` → `net.minecraft.util.random.WeightedEntry` (vanilla)
- `Weighted<T>` → `WeightedEntry.Wrapper<T>`
- `Weighted::value()` → `Wrapper::data()`
- `WeightedRandom.getRandomItem(rand, list, Weighted::weight)` → `WeightedRandom.getRandomItem(rand, list)`
- `WeightedList` API changes for spawner

### 6. TooltipDisplay (13 files)
- `net.minecraft.world.item.component.TooltipDisplay` removed (26.2-only)
- `appendHoverText(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` → `appendHoverText(ItemStack, TooltipContext, List<Component>, TooltipFlag)`
- `tooltip.accept(...)` → `tooltip.add(...)`

### 7. HoverEvent (2 files)
- `net.minecraft.network.chat.HoverEvent.ShowText` → `new HoverEvent(HoverEvent.Action.SHOW_TEXT, component)`

### 8. AbstractVillager (3 files)
- `net.minecraft.world.entity.npc.villager.AbstractVillager` → `net.minecraft.world.entity.npc.AbstractVillager`

### 9. Equipable (2 files)
- `net.minecraft.world.item.equipment.Equippable` → `net.minecraft.world.item.Equipable`

### 10. FMLEnvironment (6 files)
- `FMLEnvironment.getDist()` → `FMLEnvironment.dist`
- `FMLEnvironment.isProduction()` → `FMLEnvironment.production`

### 11. Level.isClientSide (20+ files)
- `level.isClientSide()` (method) → `level.isClientSide` (field)

### 12. Client Rendering (20+ files)
- `GuiGraphicsExtractor` → `GuiGraphics`
- `extractRenderState()` → `render()`
- `extractBackground()` → `renderBg()`
- `extractLabels()` → `renderLabels()`
- `extractTooltip()` → `renderTooltip()`
- `pushMatrix()/popMatrix()` → `pushPose()/popPose()`
- `gfx.text()` → `gfx.drawString()`
- `RenderPipelines.GUI_TEXTURED` → removed (use direct `gfx.blit()`)
- `SubmitNodeCollector` → `MultiBufferSource.BufferSource`
- `SubmitCustomGeometryEvent` → `RenderLevelStageEvent`
- `net.minecraft.client.input.*` → standard Screen methods (`mouseClicked`, `keyPressed`, etc.)
- `net.minecraft.client.renderer.block.dispatch` → removed (1.21.1 renderer API)
- `net.minecraft.client.renderer.entity.state` → removed
- `net.minecraft.client.renderer.feature.ModelFeatureRenderer` → removed
- `net.minecraft.client.renderer.state.level` → removed
- `net.minecraft.client.resources.model.geometry/sprite` → removed
- `net.neoforged.neoforge.client.pipeline` → removed
- `net.neoforged.neoforge.client.model.standalone` → removed

### 13. NeoForge Transfer API (10+ files)
- `net.neoforged.neoforge.transfer.*` → `net.neoforged.neoforge.items.IItemHandler`
- `ResourceHandler<T>` → `IItemHandler` / `IItemHandlerModifiable`
- `Transaction` / `TransactionContext` → removed
- `ItemResource` → `ItemStack`
- `ValueInput` / `ValueOutput` → `CompoundTag` / `HolderLookup.Provider`

### 14. Recipe API (15+ files)
- `net.minecraft.world.item.crafting.display` → removed (26.2-only)
- `RecipeDisplay` / `PlacementInfo` → removed
- `SlotDisplay` → removed (26.2-only)
- `RecipeBookCategories` → removed
- `ItemStackTemplate` → `ItemStack`
- `RecipeMap` → `RecipeManager`
- `Serializer` inner class pattern with `codec()` / `streamCodec()`

### 15. Loot API
- `getType()` override required for `LootItemCondition` implementations
- `LootItemConditionType(codec)` wrapper for condition registration
- `LootPoolEntryType(codec)` wrapper for entry registration
- `LootModifier` constructor: removed `int priority` parameter
- `LootItemFunctionType(codec)` wrapper for function registration

### 16. Packet/Payload API
- `ClientPacketDistributor` → `PacketDistributor`
- `handleClient` / `handleServer` → `handle(T, IPayloadContext)`
- `PacketDistributor.sendToPlayer(sp, payload, new CustomPacketPayload[0])` → `PacketDistributor.sendToPlayer(sp, payload)`

### 17. Entity/Spawn API
- `EntitySpawnReason` → `MobSpawnType`
- `EntitySpawnRequest` → removed
- `EntitySpawnReason.of()` → `MobSpawnType`

### 18. Misc API
- `Util.makeDescriptionId` → `net.minecraft.Util.makeDescriptionId`
- `net.minecraft.util.random.WeightedRandom` → same (still exists)
- `BlockEvent.BreakEvent` (replaced `BreakBlockEvent`)
- `AbstractGolem` package: `animal.golem` → `animal`
- `WanderingTraderSpawner` package: `npc.wanderingtrader` → `npc`
- `StructureProcessorType` registration changes
- `ItemAttributeModifiers.compute()` signature changes

---

## Files Deleted

| File | Reason |
|------|--------|
| `mixin/client/AbstractSkeletonRendererMixin.java` | 26.2-render mixin, no 1.21.1 equivalent |
| `mixin/client/GuiGraphicsExtractorMixin.java` | 26.2-render mixin, `GuiGraphicsExtractor` doesn't exist |
| `mixin/client/GuiItemAtlasMixin.java` | 26.2-render mixin |
| `mixin/client/ItemStackRenderStateMixin.java` | 26.2-render mixin |
| `mixin/EntityInvoker.java` | Folded into `EntityMixin` upstream |
| `util/AffixItemSlotDisplay.java` | `SlotDisplay` doesn't exist in 1.21.1 |
| `util/GemSlotDisplay.java` | `SlotDisplay` doesn't exist in 1.21.1 |
| `util/SpawnEggSlotDisplay.java` | `SlotDisplay` doesn't exist in 1.21.1 |

## Files Renamed

| Old Name | New Name | Reason |
|----------|----------|--------|
| `mixin/EnderDragonFightMixin.java` | `mixin/EndDragonFightMixin.java` | Match upstream 1.21 naming |
| `advancements/predicates/TypeAwareDCP.java` | `advancements/predicates/TypeAwareISP.java` | Match upstream 1.21 naming (`ItemSubPredicate` instead of `DataComponentPredicate`) |

## New Files Created

| File | Reason |
|------|--------|
| `mixin/client/GuiGraphicsAccessor.java` | Accessor mixin for `GuiGraphics.tooltipStack` field (needed by `AdventureModuleClient`) |

---

## Guessed / Uncertain API Mappings

1. **`BonusLootTables` codec registration**: Used `MapCodec.codec()` to convert `MapCodec<T>` to `Codec<T>` for the `serialize()` call. This may need verification.

2. **`AscEqRenderTypes.grayShader`**: Changed from `private static` to `static` for package-private access. The shader registration via `RegisterShadersEvent` pattern may need adjustment.

3. **`GemModel`**: Rewrote as `BakedModel` with `ItemOverrides` instead of the newer 26.2 `ItemModel` API. This is based on the upstream 1.21 pattern.

4. **`PipelinedRenderer`**: Replaced `GuiGraphicsExtractor`-based rendering with standard `GuiGraphics` pipeline. The gray fake item rendering was ported using `ItemRenderer.renderStatic()`.

5. **`RadialProgressTracker`**: Complete rewrite from `SubmitCustomGeometryEvent` to `RenderHighlightEvent.Block` + `RenderLevelStageEvent`. May need visual verification.

6. **`AdventureModuleClient` model registration**: Changed from `SubmitCustomGeometryEvent` to `ModelEvent.RegisterAdditional` + `ModelEvent.ModifyBakingResult`. The exact registration pattern may need adjustment.

7. **`SingletonRecipeSerializer`**: Rewrote as a proper `RecipeSerializer<T>` implementation. The original 26.2 version used a different pattern.

8. **`EntityModifier.CODEC`**: Changed from `generatedCodec()` to `CODEC` field reference.

---

## Intentionally Dropped 26.2 Features

1. **`SlotDisplay` system**: The entire `SlotDisplay`, `SlotDisplayContext`, and `ContextMap` API doesn't exist in 1.21.1. Recipe display classes (`AffixItemSlotDisplay`, `GemSlotDisplay`, `SpawnEggSlotDisplay`) were deleted. The `AscEq.SlotDisplays` class was removed.

2. **26.2 renderer pipeline**: The `SubmitNodeCollector`, `SubmitCustomGeometryEvent`, `RenderPipelines`, and related 26.2 rendering APIs were replaced with 1.21.1 equivalents (`MultiBufferSource`, `RenderLevelStageEvent`, etc.).

3. **26.2 transfer API**: The entire `net.neoforged.neoforge.transfer.*` package was replaced with `net.neoforged.neoforge.items.IItemHandler`.

---

## Build Status

```
./gradlew compileJava → BUILD SUCCESSFUL
```

**Note**: The `compat/gateways/**` package is excluded from compilation per the PORT_BRIEF and was not modified.
