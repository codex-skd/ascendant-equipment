package com.skd.ascendantequipment.net;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.affix.augmenting.AugmentingScreen;
import com.skd.commontoolkit.dynreg.DynamicHolder;
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

public record RerollResultPayload(DynamicHolder<Affix> newAffix) implements CustomPacketPayload {
   public static final Type<RerollResultPayload> TYPE = new Type(AscendantEquipment.loc("reroll_result"));
   public static final StreamCodec<ByteBuf, RerollResultPayload> CODEC = AffixRegistry.INSTANCE
      .holderStreamCodec()
      .map(RerollResultPayload::new, RerollResultPayload::newAffix);

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static class Provider implements PayloadProvider<RerollResultPayload> {
      public Type<RerollResultPayload> getType() {
         return RerollResultPayload.TYPE;
      }

      public StreamCodec<? super RegistryFriendlyByteBuf, RerollResultPayload> getCodec() {
         return RerollResultPayload.CODEC;
      }

      public void handle(RerollResultPayload msg, IPayloadContext ctx) {
         AugmentingScreen.handleRerollResult(msg.newAffix());
      }

      public List<ConnectionProtocol> getSupportedProtocols() {
         return List.of(ConnectionProtocol.PLAY);
      }

      public Optional<PacketFlow> getFlow() {
         return Optional.of(PacketFlow.CLIENTBOUND);
      }

      public String getVersion() {
         return "1";
      }
   }
}
