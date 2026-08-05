package com.skd.ascendantequipment.mobs.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mixin.EntityInvoker;
import com.skd.ascendantequipment.mobs.util.BasicBossData;
import com.skd.ascendantequipment.mobs.util.BossStats;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.ascendantattributes.modifiers.EquipmentSlotCompat;
import com.skd.ascendantenchanting.asm.EnchHooks;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.json.ChancedEffectInstance;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import com.skd.commontoolkit.systems.gear.GearSet;
import com.skd.commontoolkit.systems.gear.GearSetRegistry;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;

public record Invader(BasicBossData basicData, EntityType<?> entity, AABB size, Map<LootRarity, BossStats> stats)
   implements Constraints.Constrained,
   TieredWeights.Weighted {
   public static final String BOSS_KEY = "apoth.boss";
   public static final String RARITY_KEY = "apoth.boss.rarity";
   public static final Codec<Invader> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Invader::basicData),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(Invader::entity),
            BasicBossData.AABB_CODEC.fieldOf("size").forGetter(Invader::size),
            LootRarity.mapCodec(BossStats.CODEC).fieldOf("stats").forGetter(Invader::stats)
         )
         .apply(inst, Invader::new)
   );
   public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal
      && ((NearestAttackableTargetGoal)a).targetType == Villager.class;

   @Override
   public TieredWeights weights() {
      return this.basicData.weights();
   }

   @Override
   public Constraints constraints() {
      return this.basicData.constraints();
   }

   public Mob createBoss(ServerLevelAccessor world, BlockPos pos, GenContext ctx) {
      return this.createBoss(world, pos, ctx, null);
   }

   public Mob createBoss(ServerLevelAccessor level, BlockPos pos, GenContext ctx, @Nullable LootRarity rarity) {
      Optional<CompoundTag> nbt = this.basicData.nbt();
      CompoundTag fakeNbt = nbt.map(CompoundTag::copy).orElse(new CompoundTag());
      fakeNbt.putString("id", EntityType.getKey(this.entity).toString());
      Mob entity = (Mob)EntityType.loadEntityRecursive(fakeNbt, level.getLevel(), new EntitySpawnRequest(EntitySpawnReason.EVENT, false), EntityProcessor.NOP);
      this.initBoss(entity, ctx, rarity);
      if (this.basicData.finalizeSpawn()) {
      }

      if (nbt.isPresent()) {
         ScopedCollector reporter = new ScopedCollector(entity.problemPath(), AscendantEquipment.LOGGER);

         try {
            ((EntityInvoker)entity).callReadAdditionalSaveData(TagValueInput.create(reporter, level.registryAccess(), nbt.get()));
         } catch (Throwable var12) {
            try {
               reporter.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }

            throw var12;
         }

         reporter.close();
      }

      entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);
      if (this.basicData.hasMount()) {
         entity = this.basicData.createMount(level, pos, entity);
      }

      entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);
      return entity;
   }

   public void initBoss(Mob mob, GenContext ctx, @Nullable LootRarity rarity) {
      RandomSource rand = ctx.rand();
      if (rarity == null) {
         rarity = LootRarity.random(ctx, this.stats.keySet());
      }

      BossStats stats = this.stats.get(rarity);
      int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

      for (ChancedEffectInstance inst : stats.effects()) {
         if (rand.nextFloat() <= inst.chance()) {
            mob.addEffect(inst.create(rand, duration));
         }
      }

      for (RandomAttributeModifier modif : stats.modifiers()) {
         modif.apply(rand, mob);
      }

      mob.goalSelector.getAvailableGoals().removeIf(IS_VILLAGER_ATTACK);
      this.basicData.applyEntityName(rand, mob);
      GearSet set = this.basicData.applyGearSet(mob, ctx);
      if (set != null) {
         boolean anyValid = false;

         for (EquipmentSlot t : EquipmentSlot.values()) {
            ItemStack s = mob.getItemBySlot(t);
            if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
               anyValid = true;
               break;
            }
         }

         if (!anyValid) {
            AscendantEquipment.LOGGER
               .error("Attempted to apply boss gear set " + GearSetRegistry.INSTANCE.getKey(set) + " but it had no valid affix loot items generated.");
         }
      } else {
         ItemStack affixItem = LootController.createRandomLootItem(ctx, rarity);
         LootCategory cat = LootCategory.forItem(affixItem);
         EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
            .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
            .findAny()
            .orElse(EquipmentSlot.MAINHAND);
         mob.setItemSlot(slot, affixItem);
      }

      EquipmentSlot[] slots = EquipmentSlot.values();
      EquipmentSlot guaranteed = slots[rand.nextInt(6)];
      int tries = 50;
      ItemStack temp = mob.getItemBySlot(guaranteed);

      while (temp.isEmpty() || LootCategory.forItem(temp) == AscEq.LootCategories.NONE) {
         guaranteed = slots[rand.nextInt(6)];
         temp = mob.getItemBySlot(guaranteed);
         if (tries-- <= 0) {
            break;
         }
      }

      for (EquipmentSlot s : EquipmentSlot.values()) {
         ItemStack stack = mob.getItemBySlot(s);
         if (!stack.isEmpty()) {
            if (s == guaranteed) {
               mob.setDropChance(s, 2.0F);
               mob.setItemSlot(s, modifyBossItem(stack, mob.getName(), ctx, rarity, stats.enchLevels().primary(), mob.level().registryAccess()));
               mob.setCustomName(mob.getName().copy().withStyle(Style.EMPTY.withColor(rarity.color())));
            } else if (rand.nextFloat() < stats.enchantChance()) {
               enchantBossItem(rand, stack, stats.enchLevels().secondary(), true, mob.level().registryAccess());
               mob.setItemSlot(s, stack);
            }
         }
      }

      mob.getPersistentData().putBoolean("apoth.boss", true);
      mob.getPersistentData().putString("apoth.boss.rarity", RarityRegistry.INSTANCE.getKey(rarity).toString());
      mob.setHealth(mob.getMaxHealth());
      if (EquipmentConfig.bossGlowOnSpawn) {
         mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 3600, 0, true, false));
      }

      this.basicData.appendBonusLoot(mob);
   }

   public static void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure, RegistryAccess reg) {
      Stream<Holder<Enchantment>> available = reg.lookupOrThrow(Registries.ENCHANTMENT)
         .get(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
         .<Stream<Holder<Enchantment>>>map(HolderSet::stream)
         .orElse(Stream.empty());
      List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, available);
      Mutable builder = new Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
      ench.stream().filter(d -> !d.enchantment().is(EnchantmentTags.CURSE)).forEach(i -> builder.upgrade(i.enchantment(), i.level()));
      EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
   }

   public static ItemStack modifyBossItem(ItemStack stack, Component bossName, GenContext ctx, LootRarity rarity, int enchLevel, RegistryAccess reg) {
      RandomSource rand = ctx.rand();
      if (enchLevel > 0) {
         enchantBossItem(rand, stack, enchLevel, true, reg);
      }

      NameHelper.setItemName(rand, stack);
      stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, ctx);
      ItemAffixes.Builder builder = stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
      ObjectIterator<DynamicHolder<Affix>> bossOwnerName = builder.keySet().iterator();

      while (bossOwnerName.hasNext()) {
         DynamicHolder<Affix> afx = bossOwnerName.next();
         builder.upgrade(afx, Math.min(1.0F, builder.getLevel(afx) + Mth.nextFloat(rand, 0.1F, 0.25F)));
      }

      AffixHelper.setAffixes(stack, builder.build());
      Component bossOwnerNamex = Component.translatable(NameHelper.ownershipFormat, new Object[]{bossName});
      Component name = AffixHelper.getName(stack);
      if (name != null && name.getContents() instanceof TranslatableContents tc) {
         String oldKey = tc.getKey();
         String newKey = "misc.ascendant_equipment.affix_name.two".equals(oldKey) ? "misc.ascendant_equipment.affix_name.three" : "misc.ascendant_equipment.affix_name.four";
         Object[] newArgs = new Object[tc.getArgs().length + 1];
         newArgs[0] = bossOwnerNamex;

         for (int i = 1; i < newArgs.length; i++) {
            newArgs[i] = tc.getArgs()[i - 1];
         }

         Component copy = Component.translatable(newKey, newArgs).withStyle(name.getStyle().withItalic(false));
         RegistryFriendlyByteBuf rfbb = new RegistryFriendlyByteBuf(Unpooled.buffer(), reg, ConnectionType.NEOFORGE);
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode(rfbb, copy);
         Component deserialized = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(rfbb);
         AffixHelper.setName(stack, deserialized);
      }

      Mutable enchMap = new Mutable(ItemEnchantments.EMPTY);

      for (Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
         if (e.getKey() != null) {
            enchMap.upgrade(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey().value()), e.getIntValue() + rand.nextInt(2)));
         }
      }

      if (EquipmentConfig.curseBossItems) {
         List<Reference<Enchantment>> curses = reg.lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .filter(e -> e.is(EnchantmentTags.CURSE) && e.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT))
            .toList();
         if (!curses.isEmpty()) {
            Holder<Enchantment> curse = curses.get(rand.nextInt(curses.size()));
            enchMap.upgrade(curse, Mth.nextInt(rand, 1, EnchHooks.getMaxLevel(curse.value())));
         }
      }

      EnchantmentHelper.setEnchantments(stack, enchMap.toImmutable());
      stack.set(AscEq.Components.FROM_BOSS, true);
      return stack;
   }

   public static Invader.Builder builder() {
      return new Invader.Builder();
   }

   public static class Builder {
      private BasicBossData basicData;
      private EntityType<? extends Mob> entity;
      private AABB size;
      private Map<LootRarity, BossStats> stats = new HashMap<>();

      public Invader.Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
         this.basicData = config.apply(BasicBossData.builder()).build();
         return this;
      }

      public Invader.Builder entity(EntityType<? extends Mob> entity) {
         this.entity = entity;
         return this;
      }

      public Invader.Builder size(double width, double height) {
         this.size = new AABB(0.0, 0.0, 0.0, width, height, width);
         return this;
      }

      public Invader.Builder stats(LootRarity rarity, UnaryOperator<BossStats.Builder> config) {
         this.stats.put(rarity, config.apply(BossStats.builder()).build());
         return this;
      }

      public Invader build() {
         if (this.basicData == null) {
            throw new IllegalStateException("BasicBossData must be set");
         } else if (this.entity == null) {
            throw new IllegalStateException("Entity type must be set");
         } else if (this.size == null) {
            throw new IllegalStateException("Size must be set");
         } else if (this.stats.isEmpty()) {
            throw new IllegalStateException("Stats must not be empty");
         } else {
            return new Invader(this.basicData, this.entity, this.size, this.stats);
         }
      }
   }
}
