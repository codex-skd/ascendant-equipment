package com.skd.ascendantequipment.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder;

public record WorldTierCondition(Set<WorldTier> tiers) implements LootItemCondition {
   public static final MapCodec<WorldTierCondition> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(CommonToolkitCodecs.setOf(WorldTier.CODEC).fieldOf("tiers").forGetter(WorldTierCondition::tiers)).apply(inst, WorldTierCondition::new)
   );

   public MapCodec<WorldTierCondition> codec() {
      return CODEC;
   }

   public Set<ContextKey<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
   }

   public boolean test(LootContext ctx) {
      GenContext gCtx = GenContext.forLoot(ctx);
      return gCtx != null && this.tiers.contains(gCtx.tier());
   }

   public static Builder onlyInTiers(WorldTier... tiers) {
      LinkedHashSet<WorldTier> set = new LinkedHashSet<>();

      for (WorldTier tier : tiers) {
         set.add(tier);
      }

      return () -> new WorldTierCondition(set);
   }
}
