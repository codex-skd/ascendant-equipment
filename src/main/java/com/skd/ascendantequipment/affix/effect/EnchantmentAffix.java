package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.util.StepFunction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class EnchantmentAffix extends Affix {
   public static Codec<EnchantmentAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            Enchantment.CODEC.fieldOf("enchantment").forGetter(a -> a.ench),
            EnchantmentAffix.Mode.CODEC.optionalFieldOf("mode", EnchantmentAffix.Mode.SINGLE).forGetter(a -> a.mode),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories)
         )
         .apply(inst, EnchantmentAffix::new)
   );
   protected final Holder<Enchantment> ench;
   protected final EnchantmentAffix.Mode mode;
   protected final Map<LootRarity, StepFunction> values;
   protected final Set<LootCategory> categories;

   public EnchantmentAffix(
      AffixDefinition def, Holder<Enchantment> ench, EnchantmentAffix.Mode mode, Map<LootRarity, StepFunction> values, Set<LootCategory> categories
   ) {
      super(def);
      this.ench = ench;
      this.values = values;
      this.mode = mode;
      this.categories = categories;
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      int level = this.values.get(inst.getRarity()).getInt(inst.level());
      String desc = "bonus.ascendant_equipment:enchantment.desc";
      if (this.mode == EnchantmentAffix.Mode.GLOBAL) {
         desc = desc + ".global";
      } else if (this.mode == EnchantmentAffix.Mode.EXISTING) {
         desc = desc + ".mustExist";
      }

      Component enchName = ((Enchantment)this.ench.value()).description().plainCopy();
      return Component.translatable(desc, new Object[]{level, Component.translatable("misc.ascendant_equipment.level" + (level > 1 ? ".many" : "")), enchName})
         .withStyle(ChatFormatting.GREEN);
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      StepFunction value = this.values.get(inst.getRarity());
      return comp.append(valueBounds(Component.literal((int)value.min() + ""), Component.literal((int)value.max() + "")));
   }

   @Override
   public void getEnchantmentLevels(AffixInstance inst, GetEnchantmentLevelEvent event) {
      Mutable enchantments = event.getEnchantments();
      int level = this.values.get(inst.getRarity()).getInt(inst.level());
      if (this.mode == EnchantmentAffix.Mode.GLOBAL) {
         for (Holder<Enchantment> e : enchantments.keySet()) {
            int current = enchantments.getLevel(e);
            if (current > 0) {
               enchantments.upgrade(e, current + level);
            }
         }
      } else if (this.mode == EnchantmentAffix.Mode.EXISTING) {
         int current = enchantments.getLevel(this.ench);
         if (current > 0) {
            enchantments.upgrade(this.ench, current + level);
         }
      } else {
         enchantments.upgrade(this.ench, enchantments.getLevel(this.ench) + level);
      }
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat.isNone() ? false : (this.categories.isEmpty() || this.categories.contains(cat)) && this.values.containsKey(rarity);
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }

   public static class Builder extends AffixBuilder.ValuedAffixBuilder<EnchantmentAffix.Builder> {
      protected final Holder<Enchantment> enchantment;
      protected final EnchantmentAffix.Mode mode;
      protected final Set<LootCategory> categories = new HashSet<>();

      public Builder(Holder<Enchantment> enchantment, EnchantmentAffix.Mode mode) {
         this.enchantment = enchantment;
         this.mode = mode;
      }

      public EnchantmentAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public EnchantmentAffix build() {
         Preconditions.checkArgument(!this.values.isEmpty());
         return new EnchantmentAffix(this.definition, this.enchantment, this.mode, this.values, this.categories);
      }
   }

   public enum Mode {
      SINGLE,
      EXISTING,
      GLOBAL;

      public static final Codec<EnchantmentAffix.Mode> CODEC = CommonToolkitCodecs.enumCodec(EnchantmentAffix.Mode.class);
   }
}
