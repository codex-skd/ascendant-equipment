package com.skd.ascendantequipment.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public record RarityOverride(LootCategory category, Map<LootRarity, List<LootRule>> overrides) {
   public static final Codec<RarityOverride> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            LootCategory.CODEC.fieldOf("category").forGetter(RarityOverride::category),
            Codec.unboundedMap(LootRarity.CODEC, LootRule.CODEC.listOf()).fieldOf("overrides").forGetter(RarityOverride::overrides)
         )
         .apply(inst, RarityOverride::new)
   );

   public boolean hasRules(LootRarity rarity) {
      return this.overrides.containsKey(rarity);
   }

   @Nullable
   public List<LootRule> getRules(LootRarity rarity) {
      return this.overrides.get(rarity);
   }

   public static RarityOverride.Builder builder(LootCategory category) {
      return new RarityOverride.Builder(category);
   }

   public static class Builder {
      private final LootCategory category;
      private final Map<LootRarity, List<LootRule>> overrides = new IdentityHashMap<>();

      public Builder(LootCategory category) {
         this.category = category;
      }

      public RarityOverride.Builder override(LootRarity rarity, UnaryOperator<RarityOverride.Builder.RuleListBuilder> config) {
         final List<LootRule> list = new ArrayList<>();
         config.apply(new RarityOverride.Builder.RuleListBuilder() {
            @Override
            public RarityOverride.Builder.RuleListBuilder rule(LootRule rule) {
               list.add(rule);
               return this;
            }
         });
         this.overrides.put(rarity, list);
         return this;
      }

      public RarityOverride build() {
         return new RarityOverride(this.category, this.overrides);
      }

      public interface RuleListBuilder {
         RarityOverride.Builder.RuleListBuilder rule(LootRule var1);
      }
   }
}
