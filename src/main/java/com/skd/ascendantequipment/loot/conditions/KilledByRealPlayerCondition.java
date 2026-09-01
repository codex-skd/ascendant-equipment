package com.skd.ascendantequipment.loot.conditions;

import com.skd.ascendantequipment.AscEq;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder;
import net.neoforged.neoforge.common.util.FakePlayer;

public class KilledByRealPlayerCondition implements LootItemCondition {
   public static final KilledByRealPlayerCondition INSTANCE = new KilledByRealPlayerCondition();
   public static final MapCodec<KilledByRealPlayerCondition> CODEC = MapCodec.unit(INSTANCE);

   private KilledByRealPlayerCondition() {
   }

   public LootItemConditionType getType() {
      return AscEq.LootConditions.KILLED_BY_REAL_PLAYER;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ATTACKING_ENTITY);
   }

   public boolean test(LootContext context) {
      Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
      return attacker instanceof Player && !(attacker instanceof FakePlayer);
   }

   public static Builder killedByPlayer() {
      return () -> INSTANCE;
   }
}
