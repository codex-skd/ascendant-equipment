package com.skd.ascendantequipment.socket.gem;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public final class PurityWeightsRegistry extends DynamicRegistry<PurityWeightsRegistry.PurityWeights> {
   public static final PurityWeightsRegistry INSTANCE = new PurityWeightsRegistry();
   public static final Identifier TARGET_FILE = AscendantEquipment.loc("weights");
   private static final Map<Purity, TieredWeights> ERRORED = Map.of(
      Purity.CRACKED,
      TieredWeights.forAllTiers(1, 0.0F),
      Purity.CHIPPED,
      TieredWeights.EMPTY,
      Purity.FLAWED,
      TieredWeights.EMPTY,
      Purity.NORMAL,
      TieredWeights.EMPTY,
      Purity.FLAWLESS,
      TieredWeights.EMPTY,
      Purity.PERFECT,
      TieredWeights.EMPTY
   );
   private Map<Purity, TieredWeights> parsedWeights = Map.of();

   public PurityWeightsRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("purity_weights"), RegistrySerializer.synced(PurityWeightsRegistry.PurityWeights.CODEC));
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.parsedWeights = Map.of();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);
      if (this.registry.size() > 1) {
         this.logger.error("Additional purity weights files have been loaded. Only {} will be parsed.", TARGET_FILE);
      }

      if (this.registry.containsKey(TARGET_FILE)) {
         PurityWeightsRegistry.PurityWeights weights = (PurityWeightsRegistry.PurityWeights)this.registry.get(TARGET_FILE);
         Map<Purity, TieredWeights> weightMap = new HashMap<>();

         for (Purity p : Purity.ALL_PURITIES) {
            TieredWeights.Builder builder = TieredWeights.builder();

            for (WorldTier tier : WorldTier.values()) {
               Map<Purity, TieredWeights.Weight> tierMap = weights.weights().getOrDefault(tier, Map.of());
               TieredWeights.Weight weight = tierMap.getOrDefault(p, TieredWeights.Weight.ZERO);
               builder.with(tier, weight);
            }

            weightMap.put(p, builder.build());
         }

         this.parsedWeights = Collections.unmodifiableMap(weightMap);
      } else {
         this.logger.error("Purity weights file {} not loaded! All purity weights will be set to zero, meaning only cracked gems will spawn.", TARGET_FILE);
      }
   }

   public static Map<Purity, TieredWeights> getWeights() {
      return INSTANCE.parsedWeights.isEmpty() ? ERRORED : INSTANCE.parsedWeights;
   }

   public record PurityWeights(Map<WorldTier, Map<Purity, TieredWeights.Weight>> weights) {
      public static final Codec<PurityWeightsRegistry.PurityWeights> CODEC = WorldTier.mapCodec(Purity.mapCodec(TieredWeights.Weight.CODEC.codec()).codec())
         .fieldOf("weights")
         .xmap(PurityWeightsRegistry.PurityWeights::new, PurityWeightsRegistry.PurityWeights::weights)
         .codec();
   }
}
