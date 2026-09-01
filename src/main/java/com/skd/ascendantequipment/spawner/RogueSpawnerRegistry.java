package com.skd.ascendantequipment.spawner;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.WeightedDynamicRegistry;

public class RogueSpawnerRegistry extends WeightedDynamicRegistry<RogueSpawner> {
   public static final RogueSpawnerRegistry INSTANCE = new RogueSpawnerRegistry();

   public RogueSpawnerRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("rogue_spawners"), RegistrySerializer.simple(RogueSpawner.CODEC));
   }
}
