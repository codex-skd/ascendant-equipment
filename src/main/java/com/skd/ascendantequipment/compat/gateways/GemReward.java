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
import com.skd.commontoolkit.dynreg.DynamicHolder;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Reward;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;

public class GemReward implements Reward {
   public static final Codec<GemReward> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            CommonToolkitCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(a -> a.purities),
            CommonToolkitCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(a -> a.gems)
         )
         .apply(inst, GemReward::new)
   );
   private final Set<Purity> purities;
   private final Set<DynamicHolder<Gem>> gems;
   private transient boolean validated = false;

   protected GemReward(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
      this.purities = purities;
      this.gems = gems;
   }

   public Codec<? extends Reward> getCodec() {
      return CODEC;
   }

   public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
      if (!this.validated) {
         this.gems.forEach(GemReward::checkBound);
         this.validated = true;
      }

      GenContext gCtx = GenContext.forPlayer(summoner);
      Gem gem;
      if (!this.gems.isEmpty()) {
         gem = GemRegistry.INSTANCE.getRandomItemFromHolders(gCtx, this.gems);
      } else {
         gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
      }

      Purity purity = Purity.random(gCtx, this.purities);
      ItemStack stack = gem.toStack(purity);
      list.accept(stack);
   }

   public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
      if (this.purities.isEmpty()) {
         list.accept(AscendantEquipment.lang("reward", "random_gem"));
      } else {
         MutableComponent rarities = this.purities.stream().map(Purity::toComponent).reduce((a, b) -> a.append("/").append(b)).get();
         MutableComponent text = AscendantEquipment.lang("reward", "gem", rarities);
         list.accept(text);
      }
   }

   public static GemReward create(Set<Purity> purities, Set<DynamicHolder<Gem>> gems) {
      return new GemReward(purities, gems);
   }

   public static GemReward create(Purity... purities) {
      return new GemReward(ApothMiscUtil.linkedSet(purities), Set.of());
   }

   public static GemReward create() {
      return new GemReward(Set.of(), Set.of());
   }

   private static void checkBound(DynamicHolder<?> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("A GemReward failed to resolve {}!", holder.toString());
      }
   }
}
