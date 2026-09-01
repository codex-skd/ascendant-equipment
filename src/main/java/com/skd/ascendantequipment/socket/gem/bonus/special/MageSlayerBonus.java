package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.Tags.DamageTypes;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class MageSlayerBonus extends GemBonus {
   public static Codec<MageSlayerBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(gemClass(), Purity.mapCodec(Codec.floatRange(0.0F, 1.0F)).fieldOf("values").forGetter(a -> a.values))
         .apply(inst, MageSlayerBonus::new)
   );
   protected final Map<Purity, Float> values;

   public MageSlayerBonus(GemClass gemClass, Map<Purity, Float> values) {
      super(gemClass);
      this.values = values;
   }

   @Override
   public float onHurt(GemInstance inst, DamageSource src, LivingEntity user, float amount) {
      float value = this.values.get(inst.purity());
      if (src.is(DamageTypes.IS_MAGIC)) {
         user.heal(amount * value);
         return amount * (1.0F - value);
      } else {
         return super.onHurt(inst, src, user, amount);
      }
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      float value = this.values.get(inst.purity());
      return Component.translatable("bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(value * 100.0F)}).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }
}
