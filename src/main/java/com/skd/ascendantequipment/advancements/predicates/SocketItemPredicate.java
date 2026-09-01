package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class SocketItemPredicate implements SingleComponentItemPredicate<ItemContainerContents>, TypeAwareISP<SocketItemPredicate> {
   public static final SocketItemPredicate INSTANCE = new SocketItemPredicate();
   public static final Codec<SocketItemPredicate> CODEC = Codec.unit(SocketItemPredicate::new);

   public DataComponentType<ItemContainerContents> componentType() {
      return AscEq.Components.SOCKETED_GEMS;
   }

    @Override
    public boolean matches(ItemStack stack, ItemContainerContents value) {
      return SocketHelper.getGems(stack).stream().anyMatch(GemInstance::isValid);
   }

   @Override
   public ItemSubPredicate.Type<SocketItemPredicate> type() {
      return AscEq.DataComponentPredicates.SOCKETED_ITEM;
   }
}
