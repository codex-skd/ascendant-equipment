package com.skd.ascendantequipment.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public class TooltipItem extends Item {
   public TooltipItem(Properties properties) {
      super(properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
      list.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
   }

   public static class GlowyTooltipItem extends TooltipItem {
      public GlowyTooltipItem(Properties properties) {
         super(properties);
      }

      public boolean isFoil(ItemStack itemStack) {
         return true;
      }
   }
}
