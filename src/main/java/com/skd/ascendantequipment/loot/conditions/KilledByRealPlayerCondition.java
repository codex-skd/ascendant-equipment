package com.skd.ascendantequipment.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder;
import net.neoforged.neoforge.common.util.FakePlayer;

public class KilledByRealPlayerCondition implements LootItemCondition {
   public static final KilledByRealPlayerCondition INSTANCE = new KilledByRealPlayerCondition();
   public static final MapCodec<KilledByRealPlayerCondition> CODEC = MapCodec.unit(INSTANCE);

   private KilledByRealPlayerCondition() {
   }

   public MapCodec<KilledByRealPlayerCondition> codec() {
      return CODEC;
   }

   public Set<ContextKey<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
   }

   public boolean test(LootContext context) {
      Entity attacker = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
      return attacker instanceof Player && !(attacker instanceof FakePlayer);
   }

   public static Builder killedByPlayer() {
      return () -> INSTANCE;
   }
}
