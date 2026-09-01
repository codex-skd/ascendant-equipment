package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.ItemAffixes;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public class AffixItemPredicate implements SingleComponentItemPredicate<ItemAffixes>, TypeAwareISP<AffixItemPredicate> {
   public static final AffixItemPredicate INSTANCE = new AffixItemPredicate();
   public static final Codec<AffixItemPredicate> CODEC = Codec.unit(AffixItemPredicate::new);

   public DataComponentType<ItemAffixes> componentType() {
      return AscEq.Components.AFFIXES;
   }

   public boolean matches(ItemStack stack, ItemAffixes value) {
      return !value.isEmpty();
   }

   @Override
   public ItemSubPredicate.Type<AffixItemPredicate> type() {
      return AscEq.DataComponentPredicates.AFFIXED_ITEM;
   }
}
