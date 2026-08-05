package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantattributes.api.AbilityCooldowns;
import com.skd.ascendantenchanting.Ench.DamageTypes;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class BloodyArrowBonus extends GemBonus {
   public static Codec<BloodyArrowBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(Purity.mapCodec(BloodyArrowBonus.Data.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, BloodyArrowBonus::new)
   );
   protected final Map<Purity, BloodyArrowBonus.Data> values;

   public BloodyArrowBonus(Map<Purity, BloodyArrowBonus.Data> values) {
      super(new GemClass(AscEq.LootCategories.BOW));
      this.values = values;
   }

   @Override
   public void onProjectileFired(GemInstance inst, LivingEntity user, Projectile proj) {
      if (proj instanceof AbstractArrow arrow && !user.level().isClientSide()) {
         BloodyArrowBonus.Data d = this.values.get(inst.purity());
         if (AbilityCooldowns.isOnCooldown(user, makeUniqueId(inst), d.cooldown)) {
            return;
         }

         user.hurtServer((ServerLevel)user.level(), user.damageSources().source(DamageTypes.CORRUPTED), user.getMaxHealth() * d.healthCost);
         arrow.setBaseDamage(arrow.baseDamage * d.dmgMultiplier);
         AbilityCooldowns.startCooldown(user, makeUniqueId(inst));
      }
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      BloodyArrowBonus.Data d = this.values.get(inst.purity());
      Component cooldown = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(d.cooldown, ctx.tickRate())});
      return Component.translatable(
            "bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(d.healthCost * 100.0F), Affix.fmt(100.0F * d.dmgMultiplier), cooldown}
         )
         .withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public record Data(float healthCost, float dmgMultiplier, int cooldown) {
      public static final Codec<BloodyArrowBonus.Data> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.FLOAT.fieldOf("health_cost").forGetter(BloodyArrowBonus.Data::healthCost),
               Codec.FLOAT.fieldOf("damage_mult").forGetter(BloodyArrowBonus.Data::dmgMultiplier),
               Codec.INT.fieldOf("cooldown").forGetter(BloodyArrowBonus.Data::cooldown)
            )
            .apply(inst, BloodyArrowBonus.Data::new)
      );
   }
}
