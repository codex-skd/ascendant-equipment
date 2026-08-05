package com.skd.ascendantequipment.socket;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class SocketingRecipe extends ApothSmithingRecipe {
   public SocketingRecipe() {
      super(BASE_PLACEHOLDER, Ingredient.of((ItemLike)AscEq.Items.GEM.value()), ItemStack.EMPTY);
   }

   @Override
   public boolean matches(SmithingRecipeInput inv, Level pLevel) {
      ItemStack input = inv.getItem(1);
      ItemStack gemStack = inv.getItem(2);
      return SocketHelper.canSocketGemInItem(input, gemStack);
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput inv) {
      ItemStack input = inv.getItem(1);
      ItemStack gemStack = inv.getItem(2);
      return SocketHelper.socketGemInItem(input, gemStack);
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return (RecipeSerializer<? extends SmithingRecipe>)AscEq.RecipeSerializers.SOCKETING.value();
   }

   @Override
   public boolean isSpecial() {
      return true;
   }
}
