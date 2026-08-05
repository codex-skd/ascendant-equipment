package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;

public record PurityItemPredicate(Set<Purity> purities) implements SingleComponentItemPredicate<Purity>, TypeAwareDCP<PurityItemPredicate> {
   public static final Codec<PurityItemPredicate> CODEC = CommonToolkitCodecs.setOf(Purity.CODEC)
      .fieldOf("purities")
      .xmap(PurityItemPredicate::new, PurityItemPredicate::purities)
      .codec();

   public DataComponentType<Purity> componentType() {
      return AscEq.Components.PURITY;
   }

   public boolean matches(Purity value) {
      return this.purities.contains(value);
   }

   @Override
   public Type<PurityItemPredicate> type() {
      return AscEq.DataComponentPredicates.ITEM_WITH_PURITY;
   }
}
