package com.skd.ascendantequipment.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record SuccessChanceFeatureConfig(float successChance) implements FeatureConfiguration {
   public static final Codec<SuccessChanceFeatureConfig> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(Codec.floatRange(0.0F, 1.0F).fieldOf("success_chance").forGetter(SuccessChanceFeatureConfig::successChance))
         .apply(inst, SuccessChanceFeatureConfig::new)
   );
}
