package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscendantEquipment;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GemCaseBlockItem extends BlockItem {
   private final int maxCount;

   public GemCaseBlockItem(Block block, Properties properties) {
      super(block, properties);
      this.maxCount = block instanceof GemCaseBlock gcb ? gcb.maxCount : 0;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
      tooltip.accept(AscendantEquipment.lang("tooltip", "gem_case.capacity", GemCaseBlock.format(this.maxCount)).withStyle(ChatFormatting.GOLD));
      TypedEntityData<BlockEntityType<?>> data = (TypedEntityData<BlockEntityType<?>>)stack.get(DataComponents.BLOCK_ENTITY_DATA);
      if (data != null && data.contains("gems")) {
         int gems = data.getUnsafe().getCompoundOrEmpty("gems").size();
         if (gems > 0) {
            tooltip.accept(AscendantEquipment.lang("tooltip", "gem_case.unique_gems", gems).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
