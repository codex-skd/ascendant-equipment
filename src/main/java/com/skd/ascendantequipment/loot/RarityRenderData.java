package com.skd.ascendantequipment.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;

public record RarityRenderData(
   float beamHeight,
   float beamRadius,
   Identifier beamTexture,
   float glowRadius,
   Identifier glowTexture,
   RarityRenderData.ShadowData shadow,
   RarityRenderData.ParticleData particle
) {
   public static final RarityRenderData DEFAULT = new RarityRenderData(
      3.5F,
      0.035F,
      AscendantEquipment.loc("textures/rarity/beam.png"),
      0.065F,
      AscendantEquipment.loc("textures/rarity/glow.png"),
      RarityRenderData.ShadowData.DEFAULT,
      RarityRenderData.ParticleData.DEFAULT
   );
   public static final Codec<RarityRenderData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.floatRange(0.0F, 256.0F).fieldOf("beam_height").forGetter(RarityRenderData::beamHeight),
            Codec.floatRange(0.0F, 5.0F).fieldOf("beam_radius").forGetter(RarityRenderData::beamRadius),
            Identifier.CODEC.fieldOf("beam_texture").forGetter(RarityRenderData::beamTexture),
            Codec.floatRange(0.0F, 7.0F).fieldOf("glow_radius").forGetter(RarityRenderData::glowRadius),
            Identifier.CODEC.fieldOf("glow_texture").forGetter(RarityRenderData::glowTexture),
            RarityRenderData.ShadowData.CODEC.optionalFieldOf("shadow", RarityRenderData.ShadowData.DEFAULT).forGetter(RarityRenderData::shadow),
            RarityRenderData.ParticleData.CODEC.optionalFieldOf("particle", RarityRenderData.ParticleData.DEFAULT).forGetter(RarityRenderData::particle)
         )
         .apply(instance, RarityRenderData::new)
   );

   public static class Builder {
      private float beamRadius;
      private float beamHeight;
      private Identifier beamTexture;
      private float glowRadius;
      private Identifier glowTexture;
      private RarityRenderData.ShadowData shadow;
      private boolean hasParticles;

      public Builder() {
         this.beamRadius = RarityRenderData.DEFAULT.beamRadius;
         this.beamHeight = RarityRenderData.DEFAULT.beamHeight;
         this.beamTexture = RarityRenderData.DEFAULT.beamTexture;
         this.glowRadius = RarityRenderData.DEFAULT.glowRadius;
         this.glowTexture = RarityRenderData.DEFAULT.glowTexture;
         this.shadow = RarityRenderData.DEFAULT.shadow;
         this.hasParticles = RarityRenderData.DEFAULT.particle.enabled;
      }

      public RarityRenderData.Builder beamHeight(float height) {
         this.beamHeight = height;
         return this;
      }

      public RarityRenderData.Builder beamRadius(float radius) {
         this.beamRadius = radius;
         return this;
      }

      public RarityRenderData.Builder beamTexture(Identifier texture) {
         this.beamTexture = texture;
         return this;
      }

      public RarityRenderData.Builder glowRadius(float radius) {
         this.glowRadius = radius;
         return this;
      }

      public RarityRenderData.Builder glowTexture(Identifier texture) {
         this.glowTexture = texture;
         return this;
      }

      public RarityRenderData.Builder shadow(UnaryOperator<RarityRenderData.ShadowData.Builder> config) {
         this.shadow = config.apply(new RarityRenderData.ShadowData.Builder()).build();
         return this;
      }

      public RarityRenderData.Builder particle(boolean particle) {
         this.hasParticles = particle;
         return this;
      }

      public RarityRenderData build() {
         return new RarityRenderData(
            this.beamHeight,
            this.beamRadius,
            this.beamTexture,
            this.glowRadius,
            this.glowTexture,
            this.shadow,
            new RarityRenderData.ParticleData(this.hasParticles)
         );
      }
   }

   public record ParticleData(boolean enabled) {
      public static final RarityRenderData.ParticleData DEFAULT = new RarityRenderData.ParticleData(false);
      public static final Codec<RarityRenderData.ParticleData> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(Codec.BOOL.fieldOf("enabled").forGetter(RarityRenderData.ParticleData::enabled))
            .apply(instance, RarityRenderData.ParticleData::new)
      );
   }

   public record ShadowData(float size, int alpha, Identifier texture, int frames, float frameTime) {
      public static final RarityRenderData.ShadowData DEFAULT = new RarityRenderData.ShadowData(
         0.35F, 255, AscendantEquipment.loc("textures/rarity/shadow.png"), 1, 1.0F
      );
      public static final Codec<RarityRenderData.ShadowData> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.floatRange(0.0F, 2.0F).fieldOf("size").forGetter(RarityRenderData.ShadowData::size),
               Codec.intRange(0, 255).fieldOf("alpha").forGetter(RarityRenderData.ShadowData::alpha),
               Identifier.CODEC.fieldOf("texture").forGetter(RarityRenderData.ShadowData::texture),
               Codec.intRange(1, 128).fieldOf("frames").orElse(1).forGetter(RarityRenderData.ShadowData::frames),
               Codec.floatRange(0.5F, 40.0F).fieldOf("frame_time").orElse(1.0F).forGetter(RarityRenderData.ShadowData::frameTime)
            )
            .apply(instance, RarityRenderData.ShadowData::new)
      );

      public static class Builder {
         private int alpha;
         private float size;
         private Identifier texture;
         private int frames;
         private float frameTime;

         public Builder() {
            this.alpha = RarityRenderData.ShadowData.DEFAULT.alpha;
            this.size = RarityRenderData.ShadowData.DEFAULT.size;
            this.texture = RarityRenderData.ShadowData.DEFAULT.texture;
            this.frames = RarityRenderData.ShadowData.DEFAULT.frames;
            this.frameTime = RarityRenderData.ShadowData.DEFAULT.frameTime;
         }

         public RarityRenderData.ShadowData.Builder size(float size) {
            this.size = size;
            return this;
         }

         public RarityRenderData.ShadowData.Builder alpha(int alpha) {
            this.alpha = alpha;
            return this;
         }

         public RarityRenderData.ShadowData.Builder texture(Identifier texture) {
            this.texture = texture;
            return this;
         }

         public RarityRenderData.ShadowData.Builder frames(int frames) {
            this.frames = frames;
            return this;
         }

         public RarityRenderData.ShadowData.Builder frameTime(float frameTime) {
            this.frameTime = frameTime;
            return this;
         }

         public RarityRenderData.ShadowData build() {
            return new RarityRenderData.ShadowData(this.size, this.alpha, this.texture, this.frames, this.frameTime);
         }
      }
   }
}
