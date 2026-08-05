package com.skd.ascendantequipment.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RarityParticleData(float red, float green, float blue) implements ParticleOptions {
   public static final MapCodec<RarityParticleData> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            Codec.FLOAT.fieldOf("r").forGetter(RarityParticleData::red),
            Codec.FLOAT.fieldOf("g").forGetter(RarityParticleData::green),
            Codec.FLOAT.fieldOf("b").forGetter(RarityParticleData::blue)
         )
         .apply(inst, RarityParticleData::new)
   );
   public static final StreamCodec<ByteBuf, RarityParticleData> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.FLOAT,
      RarityParticleData::red,
      ByteBufCodecs.FLOAT,
      RarityParticleData::green,
      ByteBufCodecs.FLOAT,
      RarityParticleData::blue,
      RarityParticleData::new
   );

   public RarityParticleData(int r, int g, int b) {
      this(r / 255.0F, g / 255.0F, b / 255.0F);
   }

   public ParticleType<RarityParticleData> getType() {
      return AscEq.Particles.RARITY_GLOW;
   }
}
