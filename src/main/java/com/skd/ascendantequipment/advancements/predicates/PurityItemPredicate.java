package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record PurityItemPredicate(Set<Purity> purities) implements SingleComponentItemPredicate<Purity>, TypeAwareISP<PurityItemPredicate> {
   public static final Codec<PurityItemPredicate> CODEC = CommonToolkitCodecs.setOf(Purity.CODEC)
      .fieldOf("purities")
      .xmap(PurityItemPredicate::new, PurityItemPredicate::purities)
      .codec();

   public DataComponentType<Purity> componentType() {
      return AscEq.Components.PURITY;
   }

   public boolean matches(ItemStack stack, Purity value) {
      return this.purities.contains(value);
   }

   @Override
   public ItemSubPredicate.Type<PurityItemPredicate> type() {
      return AscEq.DataComponentPredicates.ITEM_WITH_PURITY;
   }
}
