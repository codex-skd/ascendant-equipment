package com.skd.ascendantequipment.recipe;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.socket.ReactiveSmithingRecipe;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class MaliceRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {
   public MaliceRecipe() {
      super(BASE_PLACEHOLDER, Ingredient.of((ItemLike)AscEq.Items.SIGIL_OF_MALICE.value()), ItemStack.EMPTY);
   }

   @Override
   public boolean matches(SmithingRecipeInput inv, Level level) {
      ItemStack base = inv.getItem(1);
      ItemStack sigils = inv.getItem(2);
      return base.getCount() == 1
         && sigils.is(AscEq.Items.SIGIL_OF_MALICE)
         && AffixHelper.getAffixes(base).size() >= 2
         && !(Boolean)base.getOrDefault(AscEq.Components.TOUCHED_BY_MALICE, false);
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput inv, HolderLookup.Provider registries) {
      ItemStack base = inv.getItem(1).copy();
      base.set(AscEq.Components.MALICE_MARKER, true);
      return base;
   }

   @Override
   public void onCraft(Container inv, ServerPlayer player, ItemStack output) {
      if (!output.isEmpty()) {
         AffixHelper.applyMalice(player, output);
         output.remove(AscEq.Components.MALICE_MARKER);
      }

      player.level().playSound(null, player.blockPosition(), AscEq.Sounds.MALICE.value(), SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return (RecipeSerializer<? extends SmithingRecipe>)AscEq.RecipeSerializers.MALICE.value();
   }

   @Override
   public boolean isSpecial() {
      return true;
   }
}
