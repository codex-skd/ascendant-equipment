package com.skd.ascendantequipment.mobs.registries;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import org.jetbrains.annotations.Nullable;

public class InvaderRegistry extends TieredDynamicRegistry<Invader> {
   public static final InvaderRegistry INSTANCE = new InvaderRegistry();

   public InvaderRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("apothic_invaders"), RegistrySerializer.simple(Invader.CODEC));
   }

   @Nullable
   public Invader getRandomItem(GenContext ctx) {
      return this.getRandomItem(ctx, Constraints.eval(ctx));
   }
}
