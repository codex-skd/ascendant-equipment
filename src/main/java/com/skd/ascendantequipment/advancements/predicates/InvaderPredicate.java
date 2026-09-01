package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class InvaderPredicate implements EntitySubPredicate {
   public static final InvaderPredicate INSTANCE = new InvaderPredicate();
   public static final MapCodec<InvaderPredicate> CODEC = MapCodec.unit(INSTANCE);

   @Override
   public MapCodec<? extends EntitySubPredicate> codec() {
      return CODEC;
   }

   public boolean matches(Entity entity, ServerLevel level, Vec3 position) {
      return entity.getPersistentData().contains("apoth.boss");
   }
}
