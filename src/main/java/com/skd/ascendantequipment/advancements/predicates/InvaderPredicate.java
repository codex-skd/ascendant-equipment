package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class InvaderPredicate implements EntitySubPredicate {
   public static final InvaderPredicate INSTANCE = new InvaderPredicate();
   public static final Codec<InvaderPredicate> CODEC = MapCodec.unit(INSTANCE).codec();

   public boolean matches(Entity entity, ServerLevel level, Vec3 position) {
      return entity.getPersistentData().contains("apoth.boss");
   }
}
