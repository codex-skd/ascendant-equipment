package com.skd.ascendantequipment.loot.conditions;

import com.skd.ascendantequipment.AscEq;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record MatchesBlockCondition(HolderSet<Block> blocks) implements LootItemCondition {
   public static final MapCodec<MatchesBlockCondition> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks").forGetter(MatchesBlockCondition::blocks))
         .apply(inst, MatchesBlockCondition::new)
   );

   public boolean test(LootContext ctx) {
      if (ctx.hasParam(LootContextParams.BLOCK_STATE)) {
         BlockState state = ctx.getParam(LootContextParams.BLOCK_STATE);
         return this.blocks.contains(state.getBlock().builtInRegistryHolder());
      } else {
         return false;
      }
   }

   public LootItemConditionType getType() {
      return AscEq.LootConditions.MATCHES_BLOCK;
   }
}
