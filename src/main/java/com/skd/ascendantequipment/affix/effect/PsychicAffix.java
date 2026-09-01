package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.util.StepFunction;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class PsychicAffix extends Affix {
   public static final Codec<PsychicAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, PsychicAffix::new)
   );
   protected final Map<LootRarity, StepFunction> values;

   public PsychicAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
      super(def);
      this.values = values;
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{fmt(100.0F * this.getTrueLevel(inst))});
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      Component minComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 0.0F))});
      Component maxComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 1.0F))});
      return comp.append(valueBounds(minComp, maxComp));
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat == AscEq.LootCategories.SHIELD && this.values.containsKey(rarity);
   }

   @Override
   public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
      if (source.getDirectEntity() instanceof Projectile arrow && arrow.getOwner() instanceof LivingEntity living) {
         living.hurt(entity.damageSources().source(AscEq.DamageTypes.PSYCHIC, entity), amount * this.getTrueLevel(inst));
      }

      return super.onShieldBlock(inst, entity, source, amount);
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }

   private float getTrueLevel(AffixInstance inst) {
      return this.getTrueLevel(inst.getRarity(), inst.level());
   }

   private float getTrueLevel(LootRarity rarity, float level) {
      return this.values.get(rarity).get(level);
   }
}
