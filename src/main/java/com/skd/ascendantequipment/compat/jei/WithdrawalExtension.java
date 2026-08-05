package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.WithdrawalRecipe;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.GenContext;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class WithdrawalExtension implements ISmithingCategoryExtension<WithdrawalRecipe> {
   private static final List<ItemStack> DUMMY_INPUTS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW)
      .<ItemStack>map(ItemStack::new)
      .toList();

   public <T extends IIngredientAcceptor<T>> void setTemplate(WithdrawalRecipe recipe, T acc) {
   }

   public <T extends IIngredientAcceptor<T>> void setBase(WithdrawalRecipe recipe, T acc) {
      List<ItemStack> outputs = DUMMY_INPUTS.stream().<ItemStack>map(ItemStack::copy).map(s -> {
         SocketHelper.setSockets(s, 1);
         Gem gem = GemRegistry.INSTANCE.getRandomItem(GenContext.forPlayer(Minecraft.getInstance().player), g -> g.isValidIn(s, Purity.FLAWED));
         if (gem != null) {
            ItemStack gemStack = gem.toStack(Purity.FLAWED);
            return SocketHelper.socketGemInItem(s, gemStack);
         } else {
            return (ItemStack)s;
         }
      }).toList();
      acc.addItemStacks(outputs);
   }

   public <T extends IIngredientAcceptor<T>> void setAddition(WithdrawalRecipe recipe, T acc) {
      acc.add(new ItemStack(AscEq.Items.SIGIL_OF_WITHDRAWAL));
   }

   public <T extends IIngredientAcceptor<T>> void setOutput(WithdrawalRecipe recipe, T acc) {
      List<ItemStack> outputs = DUMMY_INPUTS.stream().<ItemStack>map(ItemStack::copy).map(s -> {
         SocketHelper.setSockets(s, 1);
         Component text = AscendantEquipment.lang("text", "gems_returned").withStyle(ChatFormatting.AQUA);
         s.set(DataComponents.LORE, new ItemLore(List.of(text), List.of(text)));
         return (ItemStack)s;
      }).toList();
      acc.addItemStacks(outputs);
   }
}
