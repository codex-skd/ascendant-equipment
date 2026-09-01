package com.skd.ascendantequipment.affix.effect;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.commontoolkit.util.StepFunction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class CleavingAffix extends Affix {
   public static final Codec<CleavingAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), LootRarity.mapCodec(CleavingAffix.CleaveValues.CODEC).fieldOf("values").forGetter(a -> a.values))
         .apply(inst, CleavingAffix::new)
   );
   protected final Map<LootRarity, CleavingAffix.CleaveValues> values;
   private static boolean cleaving = false;

   public CleavingAffix(AffixDefinition def, Map<LootRarity, CleavingAffix.CleaveValues> values) {
      super(def);
      this.values = values;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat.isMelee() && this.values.containsKey(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable(
         "affix." + this.id() + ".desc",
         new Object[]{fmt(100.0F * this.getChance(inst.getRarity(), inst.level())), this.getTargets(inst.getRarity(), inst.level())}
      );
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      LootRarity rarity = inst.getRarity();
      float minChance = this.getChance(rarity, 0.0F);
      float maxChance = this.getChance(rarity, 1.0F);
      if (minChance != maxChance) {
         Component minComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * minChance)});
         Component maxComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * maxChance)});
         comp.append(valueBounds(minComp, maxComp));
      }

      int minTargets = this.getTargets(rarity, 0.0F);
      int maxTargets = this.getTargets(rarity, 1.0F);
      if (minTargets != maxTargets) {
         Component minComp = Component.literal(fmt(minTargets));
         Component maxComp = Component.literal(fmt(maxTargets));
         return comp.append(valueBounds(minComp, maxComp));
      } else {
         return comp;
      }
   }

   private float getChance(LootRarity rarity, float level) {
      return this.values.get(rarity).chance.get(level);
   }

   private int getTargets(LootRarity rarity, float level) {
      return this.values.get(rarity).targets.getInt(level);
   }

   @Override
   public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
      if (AscendantAttributes.getLocalAtkStrength(user) >= 0.98 && !cleaving && !user.level().isClientSide) {
         cleaving = true;
         float chance = this.getChance(inst.getRarity(), inst.level());
         int targets = this.getTargets(inst.getRarity(), inst.level());
         if (user.level().random.nextFloat() < chance && user instanceof Player player) {
            List<Entity> nearby = target.level().getEntities(target, new AABB(target.blockPosition()).inflate(6.0), cleavePredicate(user, target));
            for (Entity e : nearby) {
               if (targets > 0) {
                  user.attackStrengthTicker = 300;
                  player.attack(e);
                  targets--;
               }
            }
         }
         cleaving = false;
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }

   public static Predicate<Entity> cleavePredicate(Entity user, Entity target) {
      return e -> {
         if (e instanceof Animal && !(target instanceof Animal) || e instanceof AbstractVillager && !(target instanceof AbstractVillager)) {
            return false;
         }
         if ((!EquipmentConfig.cleaveHitsPlayers && e instanceof Player) || (target instanceof Enemy && !(e instanceof Enemy))) {
            return false;
         }
         return e != user && e instanceof LivingEntity le && le.isAlive();
      };
   }

   public static class Builder extends AffixBuilder<CleavingAffix.Builder> {
      protected final Map<LootRarity, CleavingAffix.CleaveValues> values = new HashMap<>();

      public CleavingAffix.Builder value(LootRarity rarity, float minChance, float maxChance, int minTargets, int maxTargets) {
         StepFunction chance = StepFunction.fromBounds(minChance, maxChance, 0.05F);
         StepFunction targets = StepFunction.fromBounds(minTargets, maxTargets, 1.0F);
         this.values.put(rarity, new CleavingAffix.CleaveValues(chance, targets));
         return this;
      }

      public CleavingAffix build() {
         Preconditions.checkNotNull(this.definition);
         Preconditions.checkArgument(this.values.size() > 0);
         return new CleavingAffix(this.definition, this.values);
      }
   }

   record CleaveValues(StepFunction chance, StepFunction targets) {
      public static final Codec<CleavingAffix.CleaveValues> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(StepFunction.CODEC.fieldOf("chance").forGetter(c -> c.chance), StepFunction.CODEC.fieldOf("targets").forGetter(c -> c.targets))
            .apply(inst, CleavingAffix.CleaveValues::new)
      );

      public boolean isConstant() {
         return this.chance.isConstant() && this.targets.isConstant();
      }
   }
}
