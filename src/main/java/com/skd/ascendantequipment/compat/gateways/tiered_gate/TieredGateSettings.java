package com.skd.ascendantequipment.compat.gateways.tiered_gate;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.WorldTier;
import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms;
import dev.shadowsoffire.gateways.gate.Gateway.Size;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms.SpawnAlgorithm;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;

public record TieredGateSettings(WorldTier tier, Size size, TextColor color, Holder<SoundEvent> soundtrack, SpawnAlgorithm spawnAlgo) {
   public static final Codec<TieredGateSettings> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TieredGateSettings::tier),
            Size.CODEC.fieldOf("size").forGetter(TieredGateSettings::size),
            TextColor.CODEC.fieldOf("color").forGetter(TieredGateSettings::color),
            SoundEvent.CODEC.optionalFieldOf("soundtrack", GatewayObjects.GATE_AMBIENT).forGetter(TieredGateSettings::soundtrack),
            SpawnAlgorithms.CODEC.optionalFieldOf("spawn_algorithm", SpawnAlgorithms.OPEN_FIELD).forGetter(TieredGateSettings::spawnAlgo)
         )
         .apply(inst, TieredGateSettings::new)
   );

   public static TieredGateSettings.Builder builder() {
      return new TieredGateSettings.Builder();
   }

   public static class Builder {
      private WorldTier tier = null;
      private Size size = null;
      private TextColor color = TextColor.fromRgb(16777215);
      private Holder<SoundEvent> soundtrack = GatewayObjects.GATE_AMBIENT;
      private SpawnAlgorithm spawnAlgo = SpawnAlgorithms.OPEN_FIELD;

      public TieredGateSettings.Builder tier(WorldTier tier) {
         this.tier = tier;
         return this;
      }

      public TieredGateSettings.Builder size(Size size) {
         this.size = size;
         return this;
      }

      public TieredGateSettings.Builder color(TextColor color) {
         this.color = color;
         return this;
      }

      public TieredGateSettings.Builder color(int color) {
         this.color = TextColor.fromRgb(color);
         return this;
      }

      public TieredGateSettings.Builder soundtrack(Holder<SoundEvent> soundtrack) {
         this.soundtrack = soundtrack;
         return this;
      }

      public TieredGateSettings.Builder spawnAlgo(SpawnAlgorithm spawnAlgo) {
         this.spawnAlgo = spawnAlgo;
         return this;
      }

      public TieredGateSettings build() {
         Preconditions.checkNotNull(this.tier, "Tier must be set");
         Preconditions.checkNotNull(this.size, "Size must be set");
         return new TieredGateSettings(this.tier, this.size, this.color, this.soundtrack, this.spawnAlgo);
      }
   }
}
