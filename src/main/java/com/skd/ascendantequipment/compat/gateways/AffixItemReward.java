package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.AffixLootEntry;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.ascendantequipment.util.NameHelper;
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

public class AffixItemReward implements Reward {
   public static final Codec<AffixItemReward> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
            CommonToolkitCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries)
         )
         .apply(inst, AffixItemReward::new)
   );
   private final Set<DynamicHolder<LootRarity>> rarities;
   private final Set<DynamicHolder<AffixLootEntry>> entries;
   private transient boolean validated = false;

   protected AffixItemReward(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
      this.rarities = rarities;
      this.entries = entries;
   }

   public Codec<? extends Reward> getCodec() {
      return CODEC;
   }

   public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
      if (!this.validated) {
         this.rarities.forEach(AffixItemReward::checkBound);
         this.entries.forEach(AffixItemReward::checkBound);
         this.validated = true;
      }

      GenContext gCtx = GenContext.forPlayer(summoner);
      ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, gCtx);
      if (!stack.isEmpty()) {
         NameHelper.setItemName(gate.getRandom(), stack);
         list.accept(stack);
      }
   }

   public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
      if (this.rarities.isEmpty()) {
         list.accept(AscendantEquipment.lang("reward", "random_affix_item"));
      } else {
         MutableComponent rarities = this.rarities
            .stream()
            .filter(DynamicHolder::isBound)
            .<LootRarity>map(DynamicHolder::get)
            .map(LootRarity::toComponent)
            .reduce((a, b) -> a.append("/").append(b))
            .get();
         MutableComponent text = AscendantEquipment.lang("reward", "affix_item", rarities);
         list.accept(text);
      }
   }

   public static AffixItemReward create(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
      return new AffixItemReward(rarities, entries);
   }

   @SafeVarargs
   public static AffixItemReward create(DynamicHolder<LootRarity>... rarities) {
      return new AffixItemReward(ApothMiscUtil.linkedSet(rarities), Set.of());
   }

   public static AffixItemReward create() {
      return new AffixItemReward(Set.of(), Set.of());
   }

   private static void checkBound(DynamicHolder<?> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("An AffixItemReward failed to resolve {}!", holder.toString());
      }
   }
}
