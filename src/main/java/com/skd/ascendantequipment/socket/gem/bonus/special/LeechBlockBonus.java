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
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class LeechBlockBonus extends GemBonus {
   public static Codec<LeechBlockBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(Purity.mapCodec(LeechBlockBonus.Data.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, LeechBlockBonus::new)
   );
   protected final Map<Purity, LeechBlockBonus.Data> values;

   public LeechBlockBonus(Map<Purity, LeechBlockBonus.Data> values) {
      super(new GemClass(AscEq.LootCategories.SHIELD));
      this.values = values;
   }

   @Override
   public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
      LeechBlockBonus.Data d = this.values.get(inst.purity());
      if (!(amount <= 2.0F) && !AbilityCooldowns.isOnCooldown(entity, makeUniqueId(inst), d.cooldown)) {
         entity.heal(amount * d.healFactor);
         AbilityCooldowns.startCooldown(entity, makeUniqueId(inst));
         return amount;
      } else {
         return amount;
      }
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      LeechBlockBonus.Data d = this.values.get(inst.purity());
      Component cooldown = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(d.cooldown, ctx.tickRate())});
      return Component.translatable("bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(d.healFactor * 100.0F), cooldown})
         .withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public record Data(float healFactor, int cooldown) {
      public static final Codec<LeechBlockBonus.Data> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.FLOAT.fieldOf("heal_factor").forGetter(LeechBlockBonus.Data::healFactor),
               Codec.INT.fieldOf("cooldown").forGetter(LeechBlockBonus.Data::cooldown)
            )
            .apply(inst, LeechBlockBonus.Data::new)
      );
   }
}
