package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;
import net.minecraft.world.item.component.ItemContainerContents;

public class SocketItemPredicate implements SingleComponentItemPredicate<ItemContainerContents>, TypeAwareDCP<SocketItemPredicate> {
   public static final SocketItemPredicate INSTANCE = new SocketItemPredicate();
   public static final Codec<SocketItemPredicate> CODEC = MapCodec.unit(INSTANCE).codec();

   public DataComponentType<ItemContainerContents> componentType() {
      return AscEq.Components.SOCKETED_GEMS;
   }

   public boolean matches(ItemContainerContents value) {
      return value.nonEmptyItemCopyStream().map(UnsocketedGem::of).anyMatch(UnsocketedGem::isValid);
   }

   @Override
   public Type<SocketItemPredicate> type() {
      return AscEq.DataComponentPredicates.SOCKETED_ITEM;
   }
}
