package com.skd.ascendantequipment.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.skd.ascendantequipment.mobs.util.SurfaceType;
import com.skd.ascendantequipment.tiers.WorldTier;
import java.util.Map;
import java.util.Optional;

public record InvaderSpawnRules(Map<WorldTier, Float> spawnChances, Optional<Integer> cooldown, SurfaceType surfaceType) {
   public static final Codec<InvaderSpawnRules> CODEC = RecordCodecBuilder.create(
         (Instance<InvaderSpawnRules> inst) -> inst.group(
               WorldTier.mapCodec(Codec.floatRange(0.0F, 1.0F)).fieldOf("spawn_chances").forGetter(InvaderSpawnRules::spawnChances),
               Codec.intRange(0, 720000).optionalFieldOf("cooldown").forGetter(InvaderSpawnRules::cooldown),
               SurfaceType.CODEC.fieldOf("surface_type").forGetter(InvaderSpawnRules::surfaceType)
            )
            .apply(inst, InvaderSpawnRules::new)
      )
      .validate(InvaderSpawnRules::validate);

   private static DataResult<InvaderSpawnRules> validate(InvaderSpawnRules rules) {
      if (rules.spawnChances.size() == WorldTier.values().length) {
         return DataResult.success(rules);
      }

      StringBuilder sb = new StringBuilder("Missing Spawn Chances for the following world tiers: ");

      for (WorldTier tier : WorldTier.values()) {
         if (!rules.spawnChances.containsKey(tier)) {
            sb.append(tier.getSerializedName()).append(" ");
         }
      }

      return DataResult.error(() -> sb.toString());
   }
}
