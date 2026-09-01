package com.skd.ascendantequipment.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.tiers.WorldTier;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TierGatedTrade extends LootItemConditionalFunction {
   public static final MapCodec<TierGatedTrade> CODEC = RecordCodecBuilder.mapCodec(
      inst -> commonFields(inst).and(WorldTier.CODEC.optionalFieldOf("min_tier", WorldTier.SUMMIT).forGetter(f -> f.minTier)).apply(inst, TierGatedTrade::new)
   );
   private final WorldTier minTier;

   public TierGatedTrade(List<LootItemCondition> predicates, WorldTier minTier) {
      super(predicates);
      this.minTier = minTier;
   }

   public MapCodec<TierGatedTrade> codec() {
      return CODEC;
   }

   @Override
   public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
      return AscEq.LootFunctions.TIER_GATED_COMPONENTS;
   }

   protected ItemStack run(ItemStack stack, LootContext ctx) {
      Entity trader = ctx.getParam(LootContextParams.THIS_ENTITY);
      Player player = trader.level().getNearestPlayer(trader, -1.0);
      WorldTier tier = player == null ? WorldTier.HAVEN : WorldTier.getTier(player);
      return tier.ordinal() >= this.minTier.ordinal() ? stack : ItemStack.EMPTY;
   }
}
