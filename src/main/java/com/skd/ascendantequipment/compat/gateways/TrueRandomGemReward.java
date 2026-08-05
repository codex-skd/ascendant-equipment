package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;

public record TrueRandomGemReward(Set<Purity> purities) implements Reward {
   public static final Codec<TrueRandomGemReward> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(CommonToolkitCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities))
         .apply(inst, TrueRandomGemReward::new)
   );

   public Codec<? extends Reward> getCodec() {
      return CODEC;
   }

   public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
      GenContext gCtx = GenContext.forPlayer(summoner);
      Gem gem = TrueRandomInvaderWaveEntity.getTrulyRandomItem(GemRegistry.INSTANCE, gCtx);
      if (gem == null) {
         AscendantEquipment.LOGGER.error("Failed to resolve a random gem when generating a TrueRandomGemReward!");
      } else {
         Purity purity = Purity.random(gCtx, this.purities);
         ItemStack stack = gem.toStack(purity);
         list.accept(stack);
      }
   }

   public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
      list.accept(AscendantEquipment.lang("reward", "true_random_gem"));
   }

   public static TrueRandomGemReward create(Purity... purities) {
      return new TrueRandomGemReward(ApothMiscUtil.linkedSet(purities));
   }

   public static TrueRandomGemReward create() {
      return new TrueRandomGemReward(Set.of());
   }
}
