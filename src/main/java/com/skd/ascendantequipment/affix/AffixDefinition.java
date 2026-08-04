package com.skd.ascendantequipment.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.LinkedHashSet;
import java.util.Set;
import org.spongepowered.include.com.google.common.base.Preconditions;

public record AffixDefinition(AffixType type, Set<DynamicHolder<Affix>> exclusiveSet, TieredWeights weights) {
   public static final Codec<AffixDefinition> CODEC = Codec.lazyInitialized(
      () -> RecordCodecBuilder.create(
         inst -> inst.group(
               AffixType.CODEC.fieldOf("affix_type").forGetter(AffixDefinition::type),
               CommonToolkitCodecs.setOf(AffixRegistry.INSTANCE.holderCodec()).fieldOf("exclusive_set").forGetter(AffixDefinition::exclusiveSet),
               TieredWeights.CODEC.fieldOf("weights").forGetter(AffixDefinition::weights)
            )
            .apply(inst, AffixDefinition::new)
      )
   );

   public static AffixDefinition.Builder builder(AffixType type) {
      return new AffixDefinition.Builder(type);
   }

   public static class Builder {
      private final AffixType type;
      private final Set<DynamicHolder<Affix>> exclusiveSet = new LinkedHashSet<>();
      private TieredWeights weights;

      public Builder(AffixType type) {
         this.type = type;
      }

      public AffixDefinition.Builder exclusiveWith(DynamicHolder<Affix> affix) {
         this.exclusiveSet.add(affix);
         return this;
      }

      public AffixDefinition.Builder weights(TieredWeights weights) {
         this.weights = weights;
         return this;
      }

      public AffixDefinition build() {
         Preconditions.checkNotNull(this.weights);
         return new AffixDefinition(this.type, this.exclusiveSet, this.weights);
      }
   }
}
