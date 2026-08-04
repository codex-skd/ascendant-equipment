package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.mixin.LivingEntityInvoker;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.commontoolkit.util.StepFunction;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class ExecutingAffix extends Affix {
   public static final Codec<ExecutingAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, ExecutingAffix::new)
   );
   protected final Set<LootCategory> categories;
   protected final Map<LootRarity, StepFunction> values;

   public ExecutingAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, StepFunction> values) {
      super(def);
      this.categories = categories;
      this.values = values;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return this.categories.contains(cat) && this.values.containsKey(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), inst.level()))});
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      Component minComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 0.0F))});
      Component maxComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 1.0F))});
      return comp.append(valueBounds(minComp, maxComp));
   }

   private float getTrueLevel(LootRarity rarity, float level) {
      return this.values.get(rarity).get(level);
   }

   @Override
   public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
      float threshold = this.getTrueLevel(inst.getRarity(), inst.level());
      if (AscendantAttributes.getLocalAtkStrength(user) >= 0.98
         && target instanceof LivingEntity living
         && !living.level().isClientSide()
         && !living.isDeadOrDying()
         && living.getHealth() / living.getMaxHealth() < threshold) {
         DamageSource src = living.damageSources().source(AscEq.DamageTypes.EXECUTE, user);
         if (!((LivingEntityInvoker)living).callCheckTotemDeathProtection(src)) {
            SoundEvent soundevent = ((LivingEntityInvoker)living).callGetDeathSound();
            if (soundevent != null) {
               living.playSound(soundevent, ((LivingEntityInvoker)living).callGetSoundVolume(), living.getVoicePitch());
            }

            living.setLastHurtByMob(user);
            if (user instanceof Player p) {
               living.setLastHurtByPlayer(p, 100);
            }

            living.getCombatTracker().recordDamage(src, 99999.0F);
            living.setHealth(0.0F);
            living.die(src);
         }
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }
}
