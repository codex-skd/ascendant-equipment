package com.skd.ascendantequipment.tiers.augments;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.SubtypedSerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TierAugmentRegistry extends DynamicRegistry<TierAugment> {
   public static final SubtypedSerializer<TierAugment> SERIALIZER = RegistrySerializer.<TierAugment>subtypedSynced("tier_augments")
      .register(AscendantEquipment.loc("attribute"), AttributeAugment.CODEC);
   public static TierAugmentRegistry INSTANCE = new TierAugmentRegistry();
   protected Map<TierAugmentRegistry.Key, List<TierAugment>> augmentsPerTier = new HashMap<>();

   private TierAugmentRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("tier_augments"), SERIALIZER);
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.augmentsPerTier.clear();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);

      for (TierAugment aug : this.registry.values()) {
         this.augmentsPerTier.computeIfAbsent(new TierAugmentRegistry.Key(aug.tier(), aug.target()), t -> new ArrayList<>()).add(aug);
      }

      for (List<TierAugment> augList : this.augmentsPerTier.values()) {
         augList.sort(Comparator.comparing(TierAugment::sortIndex));
      }
   }

   public static List<TierAugment> getAugments(WorldTier tier, TierAugment.Target target) {
      TierAugmentRegistry.Key key = new TierAugmentRegistry.Key(tier, target);
      return Collections.unmodifiableList(INSTANCE.augmentsPerTier.getOrDefault(key, List.of()));
   }

   private record Key(WorldTier tier, TierAugment.Target target) {
   }
}
