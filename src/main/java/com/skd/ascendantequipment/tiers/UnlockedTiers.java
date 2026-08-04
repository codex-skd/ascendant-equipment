package com.skd.ascendantequipment.tiers;

import com.mojang.serialization.Codec;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;

public record UnlockedTiers(Set<WorldTier> tiers) {
   public static final Codec<UnlockedTiers> CODEC = CommonToolkitCodecs.setOf(WorldTier.CODEC).xmap(UnlockedTiers::new, UnlockedTiers::tiers);

   public boolean contains(WorldTier tier) {
      return this.tiers.contains(tier);
   }
}
