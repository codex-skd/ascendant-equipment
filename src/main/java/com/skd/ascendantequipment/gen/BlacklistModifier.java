package com.skd.ascendantequipment.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifier.Phase;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public record BlacklistModifier(HolderSet<Biome> blacklistedBiomes, Holder<PlacedFeature> feature) implements BiomeModifier {
   public static final MapCodec<BlacklistModifier> CODEC = RecordCodecBuilder.mapCodec(
      builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("blacklisted_biomes").forGetter(BlacklistModifier::blacklistedBiomes),
            PlacedFeature.CODEC.fieldOf("feature").forGetter(BlacklistModifier::feature)
         )
         .apply(builder, BlacklistModifier::new)
   );

   public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
      if (phase == Phase.ADD && !this.blacklistedBiomes.contains(biome)) {
         builder.getGenerationSettings().addFeature(Decoration.UNDERGROUND_STRUCTURES, this.feature);
      }
   }

   public MapCodec<? extends BiomeModifier> codec() {
      return CODEC;
   }
}
