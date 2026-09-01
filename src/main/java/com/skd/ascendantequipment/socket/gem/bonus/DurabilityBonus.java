package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DurabilityBonus extends GemBonus {
   public static Codec<DurabilityBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(gemClass(), Purity.mapCodec(Codec.floatRange(0.0F, 1.0F)).fieldOf("values").forGetter(a -> a.values))
         .apply(inst, DurabilityBonus::new)
   );
   protected final Map<Purity, Float> values;

   public DurabilityBonus(GemClass gemClass, Map<Purity, Float> values) {
      super(gemClass);
      this.values = values;
   }

   @Override
   public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
      float level = this.values.get(gem.purity());
      return Component.translatable("bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(100.0F * level)}).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public float getDurabilityBonusPercentage(GemInstance gem) {
      return this.values.get(gem.purity());
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   public static DurabilityBonus.Builder builder() {
      return new DurabilityBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private final Map<Purity, Float> values = new HashMap<>();

      public DurabilityBonus.Builder value(Purity purity, float value) {
         if (!(value < 0.0F) && !(value > 1.0F)) {
            this.values.put(purity, value);
            return this;
         } else {
            throw new IllegalArgumentException("Durability bonus values must be between 0 and 1.");
         }
      }

      public DurabilityBonus build(GemClass gClass) {
         return new DurabilityBonus(gClass, this.values);
      }
   }
}
