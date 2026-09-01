package com.skd.ascendantequipment.socket;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class WithdrawalRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {
   public WithdrawalRecipe() {
      super(BASE_PLACEHOLDER, Ingredient.of((ItemLike)AscEq.Items.SIGIL_OF_WITHDRAWAL.value()), ItemStack.EMPTY);
   }

   @Override
   public boolean matches(SmithingRecipeInput inv, Level level) {
      ItemStack base = inv.getItem(1);
      ItemStack sigils = inv.getItem(2);
      return base.getCount() == 1 && sigils.is(AscEq.Items.SIGIL_OF_WITHDRAWAL) && SocketHelper.getGems(base).stream().anyMatch(GemInstance::isValid);
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput inv, HolderLookup.Provider registries) {
      ItemStack out = inv.getItem(1).copy();
      if (out.isEmpty()) {
         return ItemStack.EMPTY;
      }

      SocketHelper.setGems(out, SocketedGems.EMPTY);
      return out;
   }

   @Override
   public void onCraft(Container inv, ServerPlayer player, ItemStack output) {
      ItemStack base = inv.getItem(1);

      for (GemInstance gem : SocketHelper.getGems(base)) {
         ItemStack stack = gem.gemStack();
         if (!stack.isEmpty() && !player.addItem(stack)) {
            Block.popResource(player.level(), player.blockPosition(), stack);
         }
      }

      SocketHelper.setGems(base, SocketedGems.EMPTY);
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return (RecipeSerializer<? extends SmithingRecipe>)AscEq.RecipeSerializers.WITHDRAWAL.value();
   }

   @Override
   public boolean isSpecial() {
      return true;
   }
}
