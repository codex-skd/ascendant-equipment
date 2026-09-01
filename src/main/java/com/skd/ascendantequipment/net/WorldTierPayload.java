package com.skd.ascendantequipment.net;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorldTierPayload(WorldTier tier) implements CustomPacketPayload {
   public static final Type<WorldTierPayload> TYPE = new Type(AscendantEquipment.loc("world_tier"));
   public static final StreamCodec<ByteBuf, WorldTierPayload> CODEC = WorldTier.STREAM_CODEC.map(WorldTierPayload::new, WorldTierPayload::tier);

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static class Provider implements PayloadProvider<WorldTierPayload> {
      public Type<WorldTierPayload> getType() {
         return WorldTierPayload.TYPE;
      }

      public StreamCodec<? super RegistryFriendlyByteBuf, WorldTierPayload> getCodec() {
         return WorldTierPayload.CODEC;
      }

      public void handle(WorldTierPayload msg, IPayloadContext ctx) {
         Player player = ctx.player();
         if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            WorldTier.setTier(player, msg.tier);
         } else {
            if (EquipmentConfig.enableManualWorldTierChanges) {
               if (WorldTier.isUnlocked(player, msg.tier)) {
                  WorldTier.setTier(player, msg.tier);
               }
            } else if (((ServerPlayer)player).getStats().getValue(Stats.CUSTOM.get(AscEq.Stats.WORLD_TIERS_ACTIVATED)) == 0) {
               player.awardStat(AscEq.Stats.WORLD_TIERS_ACTIVATED);
            } else {
               ctx.connection().disconnect(AscendantEquipment.lang("disconnect", "tier_changes_disabled"));
            }
         }
      }

      public List<ConnectionProtocol> getSupportedProtocols() {
         return List.of(ConnectionProtocol.PLAY);
      }

      public Optional<PacketFlow> getFlow() {
         return Optional.empty();
      }

      public String getVersion() {
         return "1";
      }
   }
}
