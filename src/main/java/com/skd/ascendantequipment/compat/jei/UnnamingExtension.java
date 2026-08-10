package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.UnnamingRecipe;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

public class UnnamingExtension implements ISmithingCategoryExtension<UnnamingRecipe> {
   private static final List<Item> DUMMY_ITEMS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW)
      .toList();

   public <T extends IIngredientAcceptor<T>> void setTemplate(UnnamingRecipe recipe, T acc) {
   }

   public <T extends IIngredientAcceptor<T>> void setBase(UnnamingRecipe recipe, T acc) {
      List<ItemStack> outputs = this.getDummyItems().toList();
      acc.addItemStacks(outputs);
   }

   public <T extends IIngredientAcceptor<T>> void setAddition(UnnamingRecipe recipe, T acc) {
      acc.add(new ItemStack(AscEq.Items.SIGIL_OF_UNNAMING));
   }

   public <T extends IIngredientAcceptor<T>> void setOutput(UnnamingRecipe recipe, T acc) {
      List<ItemStack> outputs = this.getDummyItems().map(stack -> {
         LootRarity rarity = (LootRarity)AffixHelper.getRarity(stack).get();
         Component comp = Component.translatable("%2$s", new Object[]{"", ""}).withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false));
         AffixHelper.setName(stack, comp);
         return (ItemStack)stack;
      }).toList();
      acc.addItemStacks(outputs);
   }

    private Stream<ItemStack> getDummyItems() {
       List<LootRarity> sorted = RarityRegistry.getSortedRarities();
       if (sorted.isEmpty()) {
          return Stream.empty();
       }
       RandomSource rand = new LegacyRandomSource(0L);
       LootRarity rarity = sorted.getLast();
      return DUMMY_ITEMS.stream().<ItemStack>map(ItemStack::new).map(stack -> {
         LootController.createLootItem(stack, rarity, GenContext.dummy(rand));
         return (ItemStack)stack;
      });
   }
}
