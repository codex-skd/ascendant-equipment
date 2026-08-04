package com.skd.ascendantequipment.recipe;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.socket.ReactiveSmithingRecipe;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
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

public class SupremacyRecipe extends ApothSmithingRecipe implements ReactiveSmithingRecipe {
   public SupremacyRecipe() {
      super(BASE_PLACEHOLDER, Ingredient.of((ItemLike)AscEq.Items.SIGIL_OF_SUPREMACY.value()), ItemStack.EMPTY);
   }

   @Override
   public boolean matches(SmithingRecipeInput inv, Level level) {
      ItemStack base = inv.getItem(1);
      ItemStack sigils = inv.getItem(2);
      return base.getCount() == 1 && sigils.is(AscEq.Items.SIGIL_OF_SUPREMACY) && AffixHelper.hasAffixes(base);
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput inv) {
      ItemStack out = inv.getItem(1).copy();
      AffixHelper.applySupremacy(out);
      return out;
   }

   @Override
   public void onCraft(Container inv, ServerPlayer player, ItemStack output) {
      player.level().playSound(null, player.blockPosition(), AscEq.Sounds.MALICE, SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.4F + 0.8F);
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return (RecipeSerializer<? extends SmithingRecipe>)AscEq.RecipeSerializers.SUPREMACY.value();
   }

   @Override
   public boolean isSpecial() {
      return true;
   }
}
