package com.skd.ascendantequipment.loot;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import javax.annotation.Nullable;

public class AffixLootRegistry extends TieredDynamicRegistry<AffixLootEntry> {
   public static final AffixLootRegistry INSTANCE = new AffixLootRegistry();

   private AffixLootRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("affix_loot_entries"), RegistrySerializer.simple(AffixLootEntry.CODEC));
   }

   @Nullable
   public AffixLootEntry getRandomItem(GenContext ctx) {
      return this.getRandomItem(ctx, Constraints.eval(ctx));
   }
}
