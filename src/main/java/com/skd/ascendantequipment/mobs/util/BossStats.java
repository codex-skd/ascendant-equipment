package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.json.ChancedEffectInstance;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import com.skd.commontoolkit.util.StepFunction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public record BossStats(
   float enchantChance, BossStats.EnchantmentLevels enchLevels, List<ChancedEffectInstance> effects, List<RandomAttributeModifier> modifiers
) {
   public static final Identifier MODIFIER_BASE = AscendantEquipment.loc("boss_stats");
   public static final Codec<BossStats> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.FLOAT.fieldOf("enchant_chance").forGetter(BossStats::enchantChance),
            BossStats.EnchantmentLevels.CODEC.fieldOf("enchantment_levels").forGetter(BossStats::enchLevels),
            ChancedEffectInstance.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(BossStats::effects),
            RandomAttributeModifier.generatedCodec(MODIFIER_BASE).listOf().optionalFieldOf("attribute_modifiers", List.of()).forGetter(BossStats::modifiers)
         )
         .apply(inst, BossStats::new)
   );

   public static BossStats.Builder builder() {
      return new BossStats.Builder();
   }

   public static class Builder {
      private float enchantChance = 0.0F;
      private BossStats.EnchantmentLevels enchLevels;
      private List<ChancedEffectInstance> effects = new ArrayList<>();
      private List<RandomAttributeModifier> modifiers = new ArrayList<>();

      public BossStats.Builder enchantChance(float enchantChance) {
         this.enchantChance = enchantChance;
         return this;
      }

      public BossStats.Builder enchLevels(int primary, int secondary) {
         this.enchLevels = new BossStats.EnchantmentLevels(primary, secondary);
         return this;
      }

      public BossStats.Builder effect(ChancedEffectInstance effect) {
         this.effects.add(effect);
         return this;
      }

      public BossStats.Builder effect(float chance, Holder<MobEffect> effect) {
         return this.effect(chance, effect, StepFunction.constant(1.0F));
      }

      public BossStats.Builder effect(float chance, Holder<MobEffect> effect, StepFunction amplifier) {
         return this.effect(new ChancedEffectInstance(chance, effect, amplifier, true, false));
      }

      public BossStats.Builder modifier(Holder<Attribute> attribute, Operation operation, float min, float max) {
         return this.modifier(attribute, operation, StepFunction.fromBounds(min, max));
      }

      public BossStats.Builder modifier(Holder<Attribute> attribute, Operation operation, StepFunction value) {
         this.modifiers.add(RandomAttributeModifier.generated(attribute, operation, value, BossStats.MODIFIER_BASE));
         return this;
      }

      public BossStats build() {
         if (this.enchLevels == null) {
            throw new IllegalStateException("EnchantmentLevels must be set");
         } else {
            return new BossStats(this.enchantChance, this.enchLevels, this.effects, this.modifiers);
         }
      }
   }

   public record EnchantmentLevels(int primary, int secondary) {
      public static final Codec<BossStats.EnchantmentLevels> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.INT.fieldOf("primary").forGetter(BossStats.EnchantmentLevels::primary),
               Codec.INT.fieldOf("secondary").forGetter(BossStats.EnchantmentLevels::secondary)
            )
            .apply(inst, BossStats.EnchantmentLevels::new)
      );
   }
}
