package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.effect.DamageReductionAffix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DamageReductionBonus extends GemBonus {
   protected final DamageReductionAffix.DamageType type;
   protected final Map<Purity, Float> values;
   public static Codec<DamageReductionBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            gemClass(),
            DamageReductionAffix.DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
            Purity.mapCodec(Codec.floatRange(0.0F, 1.0F)).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, DamageReductionBonus::new)
   );

   public DamageReductionBonus(GemClass gemClass, DamageReductionAffix.DamageType type, Map<Purity, Float> values) {
      super(gemClass);
      this.type = type;
      this.values = values;
   }

   @Override
   public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
      float level = this.values.get(gem.purity());
      return Component.translatable(
            "affix.ascendant_equipment:damage_reduction.desc",
            new Object[]{Component.translatable("misc.ascendant_equipment." + this.type.getSerializedName()), Affix.fmt(100.0F * level)}
         )
         .withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public float onHurt(GemInstance gem, DamageSource src, LivingEntity user, float amount) {
      if (!src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !src.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && this.type.test(src)) {
         float level = this.values.get(gem.purity());
         return amount * (1.0F - level);
      } else {
         return super.onHurt(gem, src, user, amount);
      }
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   public static DamageReductionBonus.Builder builder() {
      return new DamageReductionBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private DamageReductionAffix.DamageType type;
      private Map<Purity, Float> values = new HashMap<>();

      public DamageReductionBonus.Builder damageType(DamageReductionAffix.DamageType type) {
         this.type = type;
         return this;
      }

      public DamageReductionBonus.Builder value(Purity purity, float value) {
         if (!(value < 0.0F) && !(value > 1.0F)) {
            this.values.put(purity, value);
            return this;
         } else {
            throw new IllegalArgumentException("Value must be between 0 and 1");
         }
      }

      public DamageReductionBonus build(GemClass gemClass) {
         return new DamageReductionBonus(gemClass, this.type, this.values);
      }
   }
}
