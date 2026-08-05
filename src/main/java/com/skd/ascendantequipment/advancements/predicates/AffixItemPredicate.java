package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.ItemAffixes;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;

public class AffixItemPredicate implements SingleComponentItemPredicate<ItemAffixes>, TypeAwareDCP<AffixItemPredicate> {
   public static final AffixItemPredicate INSTANCE = new AffixItemPredicate();
   public static final Codec<AffixItemPredicate> CODEC = MapCodec.unit(INSTANCE).codec();

   public DataComponentType<ItemAffixes> componentType() {
      return AscEq.Components.AFFIXES;
   }

   public boolean matches(ItemAffixes value) {
      return !value.isEmpty();
   }

   @Override
   public Type<AffixItemPredicate> type() {
      return AscEq.DataComponentPredicates.AFFIXED_ITEM;
   }
}
