package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.attachments.BonusLootTables;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.commontoolkit.dynreg.tag.DynamicHolderSet;
import com.skd.commontoolkit.dynreg.tag.DynamicTagKey;
import com.skd.commontoolkit.json.NBTAdapter;
import com.skd.commontoolkit.systems.gear.GearSet;
import com.skd.commontoolkit.systems.gear.GearSetRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public record BasicBossData(
   TieredWeights weights,
   Constraints constraints,
   Component name,
   BonusLootTables bonusLoot,
   Map<WorldTier, DynamicHolderSet<GearSet>> gearSets,
   Optional<CompoundTag> nbt,
   Optional<SupportingEntity> mount,
   List<SupportingEntity> support,
   boolean finalizeSpawn,
   List<SpawnCondition> spawnConditions
) {
   public static final String NAME_GEN = "use_name_generation";
   public static final Codec<BasicBossData> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(BasicBossData::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(BasicBossData::constraints),
            ComponentSerialization.CODEC.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(BasicBossData::name),
            BonusLootTables.CODEC.optionalFieldOf("bonus_loot", BonusLootTables.EMPTY).forGetter(BasicBossData::bonusLoot),
            WorldTier.mapCodec(DynamicHolderSet.codec(GearSetRegistry.INSTANCE))
               .codec()
               .optionalFieldOf("valid_gear_sets", Map.of())
               .forGetter(BasicBossData::gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(BasicBossData::nbt),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(BasicBossData::mount),
            SupportingEntity.CODEC.listOf().optionalFieldOf("supporting_entities", Collections.emptyList()).forGetter(BasicBossData::support),
            Codec.BOOL.optionalFieldOf("finalize", false).forGetter(BasicBossData::finalizeSpawn),
            SpawnCondition.CODEC.listOf().optionalFieldOf("spawn_conditions", Collections.emptyList()).forGetter(BasicBossData::spawnConditions)
         )
         .apply(inst, BasicBossData::new)
   );
   public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.DOUBLE.fieldOf("width").forGetter(a -> Math.abs(a.maxX - a.minX)), Codec.DOUBLE.fieldOf("height").forGetter(a -> Math.abs(a.maxY - a.minY))
         )
         .apply(inst, (width, height) -> new AABB(0.0, 0.0, 0.0, width, height, width))
   );

   public boolean hasGearSets(WorldTier tier) {
      DynamicHolderSet<GearSet> set = this.gearSets.get(tier);
      return set != null && set.size() > 0;
   }

   public boolean hasNbt() {
      return this.nbt.isPresent();
   }

   public boolean hasMount() {
      return this.mount.isPresent();
   }

   public void applyEntityName(RandomSource rand, Mob mob) {
      String nameStr = this.name.getString();
      if ("use_name_generation".equals(nameStr)) {
         NameHelper.setEntityName(rand, mob);
      } else if (!nameStr.isBlank()) {
         mob.setCustomName(this.name);
      }

      if (mob.hasCustomName()) {
         mob.setCustomNameVisible(true);
      }
   }

   public void appendBonusLoot(Mob mob) {
      if (!this.bonusLoot.isEmpty()) {
         BonusLootTables existing = mob.getData(AscEq.Attachments.BONUS_LOOT_TABLES);
         mob.setData(AscEq.Attachments.BONUS_LOOT_TABLES, existing.mergeWith(this.bonusLoot));
      }
   }

   @Nullable
   public GearSet applyGearSet(Mob mob, GenContext ctx) {
      DynamicHolderSet<GearSet> sets = this.gearSets.get(ctx.tier());
      if (sets != null && sets.size() != 0) {
         GearSet set = GearSetRegistry.INSTANCE.getRandomSet(ctx.rand(), ctx.luck(), sets);
         if (set != null) {
            set.apply(mob);
         }

         return set;
      } else {
         return null;
      }
   }

   public Mob createMount(ServerLevelAccessor level, BlockPos pos, Mob rider) {
      if (!this.hasMount()) {
         AscendantEquipment.LOGGER.error("BasicBossData#createMount called when hasMount() was false!");
         return rider;
      }

      Mob mountedEntity = this.mount.get().create(level.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
      if (mountedEntity != null) {
         rider.startRiding(mountedEntity, true, true);
      }

      return mountedEntity != null ? mountedEntity : rider;
   }

   public boolean canSpawn(Mob mob, ServerLevelAccessor level, EntitySpawnReason type) {
      return SpawnCondition.checkAll(this.spawnConditions, mob, level, type);
   }

   public static BasicBossData.Builder builder() {
      return new BasicBossData.Builder();
   }

   public static class Builder {
      private TieredWeights weights;
      private Constraints constraints = Constraints.EMPTY;
      private Component name = CommonComponents.EMPTY;
      private BonusLootTables bonusLoot = BonusLootTables.EMPTY;
      private Map<WorldTier, DynamicHolderSet<GearSet>> gearSets = new HashMap<>();
      private Optional<CompoundTag> nbt = Optional.empty();
      private Optional<SupportingEntity> mount = Optional.empty();
      private List<SupportingEntity> support = new ArrayList<>();
      private boolean finalizeSpawn = false;
      private List<SpawnCondition> exclusions = new ArrayList<>();

      public BasicBossData.Builder weights(TieredWeights weights) {
         this.weights = weights;
         return this;
      }

      public BasicBossData.Builder weights(UnaryOperator<TieredWeights.Builder> config) {
         return this.weights(config.apply(TieredWeights.builder()).build());
      }

      public BasicBossData.Builder constraints(Constraints constraints) {
         this.constraints = constraints;
         return this;
      }

      public BasicBossData.Builder constraints(UnaryOperator<Constraints.Builder> config) {
         return this.constraints(config.apply(Constraints.builder()).build());
      }

      public BasicBossData.Builder name(Component name) {
         this.name = name;
         return this;
      }

      public BasicBossData.Builder bonusLoot(BonusLootTables bonusLoot) {
         this.bonusLoot = bonusLoot;
         return this;
      }

      @SafeVarargs
      public final BasicBossData.Builder bonusLoot(ResourceKey<LootTable>... tables) {
         this.bonusLoot = new BonusLootTables(Arrays.asList(tables));
         return this;
      }

      public BasicBossData.Builder gearSets(WorldTier tier, String tagId) {
         String stripped = tagId.startsWith("#") ? tagId.substring(1) : tagId;
         Identifier id = stripped.contains(":") ? Identifier.parse(stripped) : AscendantEquipment.loc(stripped);
         DynamicTagKey<GearSet> tag = new DynamicTagKey(GearSetRegistry.INSTANCE.getId(), id);
         this.gearSets.put(tier, GearSetRegistry.INSTANCE.getOrCreateTag(tag));
         return this;
      }

      public BasicBossData.Builder gearSets(WorldTier tier, DynamicHolderSet<GearSet> set) {
         this.gearSets.put(tier, set);
         return this;
      }

      public BasicBossData.Builder nbt(CompoundTag nbt) {
         this.nbt = Optional.of(nbt);
         return this;
      }

      public BasicBossData.Builder nbt(Consumer<CompoundTag> nbt) {
         CompoundTag current = this.nbt.orElse(new CompoundTag());
         nbt.accept(current);
         this.nbt = Optional.of(current);
         return this;
      }

      public BasicBossData.Builder mount(SupportingEntity mount) {
         this.mount = Optional.of(mount);
         return this;
      }

      public BasicBossData.Builder mount(UnaryOperator<SupportingEntity.Builder> config) {
         return this.mount(config.apply(SupportingEntity.builder()).build());
      }

      public BasicBossData.Builder support(SupportingEntity support) {
         this.support.add(support);
         return this;
      }

      public BasicBossData.Builder support(UnaryOperator<SupportingEntity.Builder> config) {
         return this.support(config.apply(SupportingEntity.builder()).build());
      }

      public BasicBossData.Builder support(List<SupportingEntity> support) {
         this.support = support;
         return this;
      }

      public BasicBossData.Builder finalizeSpawn(boolean finalizeSpawn) {
         this.finalizeSpawn = finalizeSpawn;
         return this;
      }

      public BasicBossData.Builder exclusion(SpawnCondition exclusion) {
         this.exclusions.add(exclusion);
         return this;
      }

      public BasicBossData.Builder exclusions(List<SpawnCondition> exclusions) {
         this.exclusions = exclusions;
         return this;
      }

      public BasicBossData build() {
         if (this.weights == null) {
            throw new IllegalStateException("Weights must be set");
         } else {
            return new BasicBossData(
               this.weights,
               this.constraints,
               this.name,
               this.bonusLoot,
               this.gearSets,
               this.nbt,
               this.mount,
               this.support,
               this.finalizeSpawn,
               this.exclusions
            );
         }
      }
   }
}
