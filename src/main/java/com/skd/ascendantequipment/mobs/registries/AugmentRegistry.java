package com.skd.ascendantequipment.mobs.registries;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.types.Augmentation;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import java.util.Set;

public class AugmentRegistry extends DynamicRegistry<Augmentation> {
   public static final AugmentRegistry INSTANCE = new AugmentRegistry();

   public AugmentRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("apothic_augments"), RegistrySerializer.simple(Augmentation.CODEC));
   }

   public static Set<Augmentation> getAll() {
      return INSTANCE.registry.values();
   }
}
