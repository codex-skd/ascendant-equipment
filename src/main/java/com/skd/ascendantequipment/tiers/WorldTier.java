package com.skd.ascendantequipment.tiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.net.WorldTierPayload;
import com.skd.ascendantequipment.tiers.augments.TierAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public enum WorldTier implements StringRepresentable {
   HAVEN("haven"),
   FRONTIER("frontier"),
   ASCENT("ascent"),
   SUMMIT("summit"),
   PINNACLE("pinnacle");

   public static final IntFunction<WorldTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<WorldTier> CODEC = StringRepresentable.fromValues(WorldTier::values);
   public static final StreamCodec<ByteBuf, WorldTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
   private String name;

   WorldTier(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public MutableComponent toComponent() {
      return AscendantEquipment.lang("text", "world_tier." + this.getSerializedName());
   }

   public Identifier getUnlockAdvancement() {
      return switch (this) {
         case HAVEN -> AscEq.Advancements.WORLD_TIER_HAVEN;
         case FRONTIER -> AscEq.Advancements.WORLD_TIER_FRONTIER;
         case ASCENT -> AscEq.Advancements.WORLD_TIER_ASCENT;
         case SUMMIT -> AscEq.Advancements.WORLD_TIER_SUMMIT;
         case PINNACLE -> AscEq.Advancements.WORLD_TIER_PINNACLE;
      };
   }

   public static WorldTier getTier(Player player) {
      if (player instanceof FakePlayer fp) {
         MinecraftServer server = fp.level().getServer();
         ServerPlayer realPlayer = server.getPlayerList().getPlayer(fp.getUUID());
         if (realPlayer != null) {
            WorldTier realTier = getTier(realPlayer);
            fp.setData(AscEq.Attachments.WORLD_TIER, realTier);
            return realTier;
         }
      }

      return (WorldTier)player.getData(AscEq.Attachments.WORLD_TIER);
   }

   public static void setTier(Player player, WorldTier tier) {
      WorldTier oldTier = (WorldTier)player.getData(AscEq.Attachments.WORLD_TIER);
      if (oldTier != tier || isTutorialActive(player)) {
         player.setData(AscEq.Attachments.WORLD_TIER, tier);
         if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new WorldTierPayload(tier), new CustomPacketPayload[0]);

            for (TierAugment aug : TierAugmentRegistry.getAugments(oldTier, TierAugment.Target.PLAYERS)) {
               aug.remove(sp.level(), player);
            }

            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, TierAugment.Target.PLAYERS)) {
               aug.apply(sp.level(), player);
            }

            player.setData(AscEq.Attachments.TIER_AUGMENTS_APPLIED, true);
            player.awardStat(AscEq.Stats.WORLD_TIERS_ACTIVATED);
         }
      }
   }

   public static boolean isUnlocked(Player player, WorldTier tier) {
      return ApothMiscUtil.hasAdvancement(player, tier.getUnlockAdvancement());
   }

   public static boolean isTutorialActive(Player player) {
      return FMLEnvironment.getDist().isClient() && player.level().isClientSide()
         ? WorldTier.ClientAccess.isTutorialActive(player)
         : getTier(player) == HAVEN && ((ServerPlayer)player).getStats().getValue(Stats.CUSTOM.get(AscEq.Stats.WORLD_TIERS_ACTIVATED)) == 0;
   }

   public static <T> MapCodec<Map<WorldTier, T>> mapCodec(Codec<T> elementCodec) {
      return Codec.simpleMap(CODEC, elementCodec, Keyable.forStrings(() -> Arrays.stream(values()).map(StringRepresentable::getSerializedName)));
   }

   private static class ClientAccess {
      private static boolean isTutorialActive(Player player) {
         return WorldTier.getTier(player) == WorldTier.HAVEN
            && Minecraft.getInstance().player.getStats().getValue(Stats.CUSTOM.get(AscEq.Stats.WORLD_TIERS_ACTIVATED)) == 0;
      }
   }
}
