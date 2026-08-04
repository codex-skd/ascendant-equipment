package com.skd.ascendantequipment.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class TooltipBlockItem extends BlockItem {
   public TooltipBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
      tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
   }
}
