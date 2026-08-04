package com.skd.ascendantequipment.affix;

import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.util.StepFunction;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.function.TriFunction;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class AffixBuilder<T extends AffixBuilder<T>> {
   protected AffixDefinition definition;

   public T definition(AffixType type, UnaryOperator<AffixDefinition.Builder> op) {
      this.definition = op.apply(new AffixDefinition.Builder(type)).build();
      return (T)this;
   }

   public T definition(AffixType type, int weight, float quality) {
      this.definition = new AffixDefinition(type, Set.of(), TieredWeights.forAllTiers(weight, quality));
      return (T)this;
   }

   public static <T extends Affix> AffixBuilder.SimpleAffixBuilder<T> simple(BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory) {
      return new AffixBuilder.SimpleAffixBuilder<>(factory);
   }

   public static <T extends Affix> AffixBuilder.CategorizedAffixBuilder<T> categorized(
      TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory
   ) {
      return new AffixBuilder.CategorizedAffixBuilder<>(factory);
   }

   public static class CategorizedAffixBuilder<T extends Affix> extends AffixBuilder.ValuedAffixBuilder<AffixBuilder.CategorizedAffixBuilder<T>> {
      private final TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory;
      private final Set<LootCategory> categories = new LinkedHashSet<>();

      public CategorizedAffixBuilder(TriFunction<AffixDefinition, Set<LootCategory>, Map<LootRarity, StepFunction>, T> factory) {
         this.factory = factory;
      }

      public AffixBuilder.CategorizedAffixBuilder<T> category(LootCategory category) {
         this.categories.add(category);
         return this;
      }

      public AffixBuilder.CategorizedAffixBuilder<T> categories(LootCategory... categories) {
         for (LootCategory category : categories) {
            this.categories.add(category);
         }

         return this;
      }

      public T build() {
         Preconditions.checkArgument(this.definition != null);
         Preconditions.checkArgument(!this.values.isEmpty());
         Preconditions.checkArgument(!this.categories.isEmpty());
         return (T)((Affix)this.factory.apply(this.definition, this.categories, this.values));
      }
   }

   public static class SimpleAffixBuilder<T extends Affix> extends AffixBuilder.ValuedAffixBuilder<AffixBuilder.SimpleAffixBuilder<T>> {
      private final BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory;

      public SimpleAffixBuilder(BiFunction<AffixDefinition, Map<LootRarity, StepFunction>, T> factory) {
         this.factory = factory;
      }

      public T build() {
         Preconditions.checkArgument(this.definition != null);
         Preconditions.checkArgument(!this.values.isEmpty());
         return (T)((Affix)this.factory.apply(this.definition, this.values));
      }
   }

   public static class ValuedAffixBuilder<T extends AffixBuilder.ValuedAffixBuilder<T>> extends AffixBuilder<T> {
      protected final Map<LootRarity, StepFunction> values = new LinkedHashMap<>();
      protected float step = 0.01F;

      public T step(float step) {
         this.step = step;
         return (T)this;
      }

      public T value(LootRarity rarity, float min, float max) {
         return (T)this.value(rarity, StepFunction.fromBounds(min, max, this.step));
      }

      public T value(LootRarity rarity, float value) {
         return (T)this.value(rarity, StepFunction.constant(value));
      }

      public T value(LootRarity rarity, StepFunction function) {
         this.values.put(rarity, function);
         return (T)this;
      }
   }
}
