package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscendantEquipment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

public class GemCaseBlockItem extends BlockItem {
   private final int maxCount;

   public GemCaseBlockItem(Block block, Properties properties) {
      super(block, properties);
      this.maxCount = block instanceof GemCaseBlock gcb ? gcb.maxCount : 0;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(AscendantEquipment.lang("tooltip", "gem_case.capacity", GemCaseBlock.format(this.maxCount)).withStyle(ChatFormatting.GOLD));
      CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
      if (!data.isEmpty() && data.contains("gems")) {
         int gems = data.getUnsafe().getCompound("gems").size();
         if (gems > 0) {
            tooltip.add(AscendantEquipment.lang("tooltip", "gem_case.unique_gems", gems).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
