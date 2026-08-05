package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.AddSocketsRecipe;
import com.skd.ascendantequipment.socket.SocketHelper;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class AddSocketsExtension implements ISmithingCategoryExtension<AddSocketsRecipe> {
   private static final List<ItemStack> DUMMY_INPUTS = Stream.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW)
      .<ItemStack>map(ItemStack::new)
      .toList();

   public <T extends IIngredientAcceptor<T>> void setTemplate(AddSocketsRecipe recipe, T acc) {
   }

   public <T extends IIngredientAcceptor<T>> void setBase(AddSocketsRecipe recipe, T acc) {
      acc.addItemStacks(DUMMY_INPUTS);
   }

   public <T extends IIngredientAcceptor<T>> void setAddition(AddSocketsRecipe recipe, T acc) {
      acc.add(recipe.getInput());
   }

   public <T extends IIngredientAcceptor<T>> void setOutput(AddSocketsRecipe recipe, T acc) {
      List<ItemStack> outputs = DUMMY_INPUTS.stream().<ItemStack>map(ItemStack::copy).map(s -> {
         SocketHelper.setSockets(s, 1);
         Component text = AscendantEquipment.lang("text", "socket_limit", recipe.getMaxSockets()).withStyle(ChatFormatting.AQUA);
         s.set(DataComponents.LORE, new ItemLore(List.of(text), List.of(text)));
         return (ItemStack)s;
      }).toList();
      acc.addItemStacks(outputs);
   }
}
