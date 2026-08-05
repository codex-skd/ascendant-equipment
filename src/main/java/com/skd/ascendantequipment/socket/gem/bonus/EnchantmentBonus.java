package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public class EnchantmentBonus extends GemBonus {
   public static Codec<EnchantmentBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            gemClass(),
            Enchantment.CODEC.fieldOf("enchantment").forGetter(a -> a.ench),
            EnchantmentBonus.Mode.CODEC.optionalFieldOf("mode", EnchantmentBonus.Mode.SINGLE).forGetter(a -> a.mode),
            Purity.mapCodec(Codec.intRange(1, 127)).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, EnchantmentBonus::new)
   );
   protected final Holder<Enchantment> ench;
   protected final EnchantmentBonus.Mode mode;
   protected final Map<Purity, Integer> values;

   public EnchantmentBonus(GemClass gemClass, Holder<Enchantment> ench, EnchantmentBonus.Mode mode, Map<Purity, Integer> values) {
      super(gemClass);
      this.ench = ench;
      this.values = values;
      this.mode = mode;
   }

   @Override
   public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
      int level = this.values.get(gem.purity());
      String desc = "bonus." + this.getTypeKey() + ".desc";
      if (this.mode == EnchantmentBonus.Mode.GLOBAL) {
         desc = desc + ".global";
      } else if (this.mode == EnchantmentBonus.Mode.EXISTING) {
         desc = desc + ".mustExist";
      }

      Component enchName = ((Enchantment)this.ench.value()).description().plainCopy();
      return Component.translatable(desc, new Object[]{level, Component.translatable("misc.ascendant_equipment.level" + (level > 1 ? ".many" : "")), enchName})
         .withStyle(ChatFormatting.GREEN);
   }

   @Override
   public void getEnchantmentLevels(GemInstance gem, GetEnchantmentLevelEvent event) {
      Mutable enchantments = event.getEnchantments();
      int level = this.values.get(gem.purity());
      if (this.mode == EnchantmentBonus.Mode.GLOBAL) {
         for (Holder<Enchantment> e : enchantments.keySet()) {
            int current = enchantments.getLevel(e);
            if (current > 0) {
               enchantments.upgrade(e, current + level);
            }
         }
      } else if (this.mode == EnchantmentBonus.Mode.EXISTING) {
         int current = enchantments.getLevel(this.ench);
         if (current > 0) {
            enchantments.upgrade(this.ench, current + level);
         }
      } else {
         enchantments.upgrade(this.ench, enchantments.getLevel(this.ench) + level);
      }
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   public static EnchantmentBonus.Builder builder() {
      return new EnchantmentBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private Holder<Enchantment> enchantment;
      private EnchantmentBonus.Mode mode;
      private Map<Purity, Integer> values = new HashMap<>();

      public Builder() {
         this.mode = EnchantmentBonus.Mode.SINGLE;
      }

      public EnchantmentBonus.Builder enchantment(Holder<Enchantment> enchantment) {
         this.enchantment = enchantment;
         return this;
      }

      public EnchantmentBonus.Builder mode(EnchantmentBonus.Mode mode) {
         this.mode = mode;
         return this;
      }

      public EnchantmentBonus.Builder value(Purity purity, int value) {
         if (value >= 1 && value <= 127) {
            this.values.put(purity, value);
            return this;
         } else {
            throw new IllegalArgumentException("EnchantmentBonus is limited to values between 1 and 127 (inclusive).");
         }
      }

      public EnchantmentBonus build(GemClass gemClass) {
         return new EnchantmentBonus(gemClass, this.enchantment, this.mode, this.values);
      }
   }

   public enum Mode {
      SINGLE,
      EXISTING,
      GLOBAL;

      public static final Codec<EnchantmentBonus.Mode> CODEC = CommonToolkitCodecs.enumCodec(EnchantmentBonus.Mode.class);
   }
}
