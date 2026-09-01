package com.skd.ascendantequipment.net;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.util.RadialUtil;
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
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RadialStatePayload(RadialUtil.RadialState state) implements CustomPacketPayload {
   public static final Type<RadialStatePayload> TYPE = new Type(AscendantEquipment.loc("radial_state_change"));
   public static final StreamCodec<ByteBuf, RadialStatePayload> CODEC = RadialUtil.RadialState.STREAM_CODEC
      .map(RadialStatePayload::new, RadialStatePayload::state);

   public RadialStatePayload() {
      this(RadialUtil.RadialState.ENABLED);
   }

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static class Provider implements PayloadProvider<RadialStatePayload> {
      public Type<RadialStatePayload> getType() {
         return RadialStatePayload.TYPE;
      }

      public StreamCodec<? super RegistryFriendlyByteBuf, RadialStatePayload> getCodec() {
         return RadialStatePayload.CODEC;
      }

      public void handle(RadialStatePayload msg, IPayloadContext ctx) {
         net.minecraft.world.entity.player.Player player = ctx.player();
         if (ctx.flow().isClientbound()) {
            RadialUtil.RadialState.setState(player, msg.state);
         } else {
            RadialUtil.toggleRadialState(player);
         }
      }

      public List<ConnectionProtocol> getSupportedProtocols() {
         return List.of(ConnectionProtocol.PLAY);
      }

      public Optional<PacketFlow> getFlow() {
         return Optional.empty();
      }

      public String getVersion() {
         return "2";
      }
   }
}
