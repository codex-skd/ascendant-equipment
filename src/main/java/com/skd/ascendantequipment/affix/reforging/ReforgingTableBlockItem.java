package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import java.util.Comparator;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

public class ReforgingTableBlockItem extends BlockItem {
   public ReforgingTableBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipFlag tooltipFlag, Consumer<Component> tooltip) {
      tooltip.accept(Component.translatable("block.ascendant_equipment.reforging_table.desc").withStyle(ChatFormatting.GRAY));
      LootRarity max = this.computeMaxRarity();
      if (max != null) {
         LootRarity globalMax = RarityRegistry.getSortedRarities().stream().max(Comparator.comparingInt(LootRarity::sortIndex)).orElse(null);
         if (globalMax != null && max.sortIndex() < globalMax.sortIndex()) {
            tooltip.accept(Component.translatable("block.ascendant_equipment.reforging_table.desc2", new Object[]{max.toComponent()}).withStyle(ChatFormatting.GRAY));
         }
      }
   }

   private LootRarity computeMaxRarity() {
      LootRarity best = null;

      for (RecipeHolder<ReforgingRecipe> holder : ReforgingRecipeCache.all()) {
         ReforgingRecipe recipe = (ReforgingRecipe)holder.value();
         if (recipe.tables().contains(this.getBlock().builtInRegistryHolder()) && recipe.rarity().isBound()) {
            LootRarity r = (LootRarity)recipe.rarity().get();
            if (best == null || r.sortIndex() > best.sortIndex()) {
               best = r;
            }
         }
      }

      return best;
   }
}
