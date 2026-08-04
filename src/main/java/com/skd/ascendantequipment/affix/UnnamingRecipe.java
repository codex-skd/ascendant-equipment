package com.skd.ascendantequipment.affix;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class UnnamingRecipe extends ApothSmithingRecipe {
   public UnnamingRecipe() {
      super(BASE_PLACEHOLDER, Ingredient.of((ItemLike)AscEq.Items.SIGIL_OF_UNNAMING.value()), ItemStack.EMPTY);
   }

   @Override
   public boolean matches(SmithingRecipeInput pInv, Level pLevel) {
      ItemStack base = pInv.getItem(1);
      return base.has(AscEq.Components.AFFIX_NAME) && pInv.getItem(2).is(AscEq.Items.SIGIL_OF_UNNAMING);
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput pInv) {
      ItemStack out = pInv.getItem(1).copy();
      DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(out);
      if (!rarity.isBound()) {
         return ItemStack.EMPTY;
      }

      Component comp = Component.translatable("%2$s", new Object[]{"", ""})
         .withStyle(Style.EMPTY.withColor(((LootRarity)rarity.get()).color()).withItalic(false));
      AffixHelper.setName(out, comp);
      return out;
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return (RecipeSerializer<? extends SmithingRecipe>)AscEq.RecipeSerializers.UNNAMING.value();
   }

   @Override
   public boolean isSpecial() {
      return true;
   }
}
