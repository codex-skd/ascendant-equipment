package com.skd.ascendantequipment.mobs.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.mixin.EntityInvoker;
import com.skd.ascendantequipment.mobs.registries.EliteRegistry;
import com.skd.ascendantequipment.mobs.util.AffixData;
import com.skd.ascendantequipment.mobs.util.BasicBossData;
import com.skd.ascendantequipment.mobs.util.BossStats;
import com.skd.ascendantequipment.mobs.util.SupportingEntity;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.json.ChancedEffectInstance;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

public record Elite(BasicBossData basicData, float chance, HolderSet<EntityType<?>> entities, BossStats stats, AffixData afxData)
   implements Constraints.Constrained,
   TieredWeights.Weighted,
   EliteRegistry.IEntityMatch {
   public static final String MINIBOSS_KEY = "apoth.miniboss";
   public static final String PLAYER_KEY = "apoth.miniboss.player";
   public static final Codec<Elite> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Elite::basicData),
            Codec.floatRange(0.0F, 1.0F).fieldOf("success_chance").forGetter(Elite::chance),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Elite::entities),
            BossStats.CODEC.fieldOf("stats").forGetter(Elite::stats),
            AffixData.CODEC.optionalFieldOf("affix_data", AffixData.DEFAULT).forGetter(Elite::afxData)
         )
         .apply(inst, Elite::new)
   );

   @Override
   public TieredWeights weights() {
      return this.basicData.weights();
   }

   @Override
   public Constraints constraints() {
      return this.basicData.constraints();
   }

   public float getChance() {
      return this.chance;
   }

   @Override
   public HolderSet<EntityType<?>> getEntities() {
      return this.entities;
   }

   public void transformMiniboss(ServerLevelAccessor level, Mob mob, GenContext ctx) {
      Vec3 pos = mob.getPosition(0.0F);
      Optional<CompoundTag> optNbt = this.basicData.nbt();
      if (optNbt.isPresent()) {
         CompoundTag nbt = optNbt.get();
         if (nbt.contains("Passengers")) {
            ListTag passengers = nbt.getListOrEmpty("Passengers");

            for (int i = 0; i < passengers.size(); i++) {
               Entity entity = EntityType.loadEntityRecursive(passengers.getCompoundOrEmpty(i), level.getLevel(), new EntitySpawnRequest(EntitySpawnReason.EVENT, false), EntityProcessor.NOP);
               if (entity != null) {
                  entity.setPos(pos);
                  level.addFreshEntityWithPassengers(entity);
                  entity.startRiding(mob, true, true);
               }
            }
         }
      }

      mob.setPos(pos);
      this.initElite(mob, ctx);
      if (optNbt.isPresent()) {
         ScopedCollector reporter = new ScopedCollector(mob.problemPath(), AscendantEquipment.LOGGER);

         try {
            ((EntityInvoker)mob).callReadAdditionalSaveData(TagValueInput.create(reporter, level.registryAccess(), optNbt.get()));
         } catch (Throwable var11) {
            try {
               reporter.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }

            throw var11;
         }

         reporter.close();
      }

      if (this.basicData.hasMount()) {
         Mob mount = this.basicData().createMount(level, BlockPos.containing(pos), mob);
         level.addFreshEntity(mount);
      }

      for (SupportingEntity support : this.basicData.support()) {
         Mob supportingMob = support.create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
         if (supportingMob != null) {
            level.addFreshEntity(supportingMob);
         }
      }
   }

   public void initElite(Mob mob, GenContext ctx) {
      RandomSource rand = ctx.rand();
      mob.getPersistentData().putBoolean("apoth.miniboss", true);
      int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

      for (ChancedEffectInstance inst : this.stats.effects()) {
         if (rand.nextFloat() <= inst.chance()) {
            mob.addEffect(inst.create(rand, duration));
         }
      }

      for (RandomAttributeModifier modif : this.stats.modifiers()) {
         modif.apply(rand, mob);
      }

      this.basicData.applyEntityName(rand, mob);
      this.basicData.applyGearSet(mob, ctx);
      EquipmentSlot affixedSlot = this.afxData.applyTo(mob, ctx, this.stats.enchLevels().primary(), true);

      for (EquipmentSlot s : EquipmentSlot.values()) {
         ItemStack stack = mob.getItemBySlot(s);
         if (!stack.isEmpty() && s != affixedSlot && rand.nextFloat() < this.stats.enchantChance()) {
            Invader.enchantBossItem(rand, stack, this.stats.enchLevels().secondary(), true, mob.level().registryAccess());
            mob.setItemSlot(s, stack);
         }
      }

      mob.setHealth(mob.getMaxHealth());
      this.basicData.appendBonusLoot(mob);
   }

   public static Elite.Builder builder() {
      return new Elite.Builder();
   }

   public static class Builder {
      private BasicBossData basicData;
      private float chance = -1.0F;
      private HolderSet<EntityType<?>> entities = null;
      private BossStats stats;
      private AffixData afxData = AffixData.DEFAULT;

      public Elite.Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
         this.basicData = config.apply(BasicBossData.builder()).build();
         return this;
      }

      public Elite.Builder chance(float chance) {
         this.chance = chance;
         return this;
      }

      public Elite.Builder entities(HolderSet<EntityType<?>> entities) {
         if (this.entities == null) {
            this.entities = entities;
         } else {
            this.entities = new OrHolderSet(new HolderSet[]{this.entities, entities});
         }

         return this;
      }

      public Elite.Builder entities(TagKey<EntityType<?>> entities) {
         return this.entities(BuiltInRegistries.ENTITY_TYPE.get(entities).orElseThrow());
      }

      @SafeVarargs
      public final Elite.Builder entities(EntityType<? extends Mob>... entities) {
         return this.entities(HolderSet.direct(EntityType::builtInRegistryHolder, entities));
      }

      public Elite.Builder stats(UnaryOperator<BossStats.Builder> config) {
         this.stats = config.apply(BossStats.builder()).build();
         return this;
      }

      public Elite.Builder affixes(float chance, Set<LootRarity> rarities) {
         this.afxData = new AffixData(chance, rarities);
         return this;
      }

      public Elite build() {
         if (this.basicData == null) {
            throw new IllegalStateException("BasicBossData must be set");
         } else if (this.chance <= 0.0F) {
            throw new IllegalStateException("Chance value must be positive");
         } else if (this.entities == null) {
            throw new IllegalStateException("Entities must be set");
         } else if (this.stats == null) {
            throw new IllegalStateException("Stats must be set");
         } else {
            return new Elite(this.basicData, this.chance, this.entities, this.stats, this.afxData);
         }
      }
   }
}
