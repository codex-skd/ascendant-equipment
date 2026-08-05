package com.skd.ascendantequipment.net;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseMenu;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseScreen;
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

public record GemCaseSelectPayload(DynamicHolder<Gem> gem) implements CustomPacketPayload {
   public static final Type<GemCaseSelectPayload> TYPE = new Type(AscendantEquipment.loc("gem_case_select"));
   public static final StreamCodec<ByteBuf, GemCaseSelectPayload> CODEC = GemRegistry.INSTANCE
      .holderStreamCodec()
      .map(GemCaseSelectPayload::new, GemCaseSelectPayload::gem);

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static class Provider implements PayloadProvider<GemCaseSelectPayload> {
      public Type<GemCaseSelectPayload> getType() {
         return GemCaseSelectPayload.TYPE;
      }

      public StreamCodec<? super RegistryFriendlyByteBuf, GemCaseSelectPayload> getCodec() {
         return GemCaseSelectPayload.CODEC;
      }

      public void handleClient(GemCaseSelectPayload msg, IPayloadContext ctx) {
         GemCaseScreen.handleSelectedGem(msg.gem());
      }

      public void handleServer(GemCaseSelectPayload msg, IPayloadContext ctx) {
         if (ctx.player().containerMenu instanceof GemCaseMenu menu) {
            menu.setSelectedGem(msg.gem());
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
