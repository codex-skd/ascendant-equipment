package com.skd.ascendantequipment.affix.salvaging;

import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;

public class SalvageItem extends Item {
   protected final DynamicHolder<LootRarity> rarity;

   public SalvageItem(DynamicHolder<LootRarity> rarity, Properties pProperties) {
      super(pProperties);
      this.rarity = rarity;
   }

   public Component getName(ItemStack pStack) {
      return (Component)(!this.rarity.isBound()
         ? super.getName(pStack)
         : Component.translatable(this.getDescriptionId()).withStyle(Style.EMPTY.withColor(((LootRarity)this.rarity.get()).color())));
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
      if (this.rarity.isBound()) {
         tooltip.accept(
            Component.translatable("info.ascendant_equipment.rarity_material", new Object[]{((LootRarity)this.rarity.get()).toComponent()})
               .withStyle(ChatFormatting.GRAY)
         );
      }
   }
}
