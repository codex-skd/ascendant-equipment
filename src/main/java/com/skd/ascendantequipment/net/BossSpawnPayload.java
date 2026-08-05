package com.skd.ascendantequipment.net;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureModuleClient;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.mutable.MutableInt;

public record BossSpawnPayload(BlockPos pos, DynamicHolder<LootRarity> rarity) implements CustomPacketPayload {
   public static final Type<BossSpawnPayload> TYPE = new Type(AscendantEquipment.loc("boss_spawn"));
   public static final StreamCodec<ByteBuf, BossSpawnPayload> CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, BossSpawnPayload::pos, RarityRegistry.INSTANCE.holderStreamCodec(), BossSpawnPayload::rarity, BossSpawnPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public record BossSpawnData(BlockPos pos, LootRarity rarity, MutableInt ticks) {
   }

   public static class Provider implements PayloadProvider<BossSpawnPayload> {
      public Type<BossSpawnPayload> getType() {
         return BossSpawnPayload.TYPE;
      }

      public StreamCodec<? super RegistryFriendlyByteBuf, BossSpawnPayload> getCodec() {
         return BossSpawnPayload.CODEC;
      }

      public void handleClient(BossSpawnPayload msg, IPayloadContext ctx) {
         AdventureModuleClient.onBossSpawn(msg.pos, msg.rarity);
      }

      public List<ConnectionProtocol> getSupportedProtocols() {
         return List.of(ConnectionProtocol.PLAY);
      }

      public Optional<PacketFlow> getFlow() {
         return Optional.of(PacketFlow.CLIENTBOUND);
      }

      public String getVersion() {
         return "2";
      }
   }
}
