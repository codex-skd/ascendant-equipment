package com.skd.ascendantequipment.affix.salvaging;

import com.skd.ascendantequipment.AscEq;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class SalvagingRecipeCache {
   private static volatile List<RecipeHolder<SalvagingRecipe>> RECIPES = List.of();

   private SalvagingRecipeCache() {
   }

   public static void rebuildFromManager(RecipeManager manager) {
      Collection<RecipeHolder<SalvagingRecipe>> holders = manager.getAllRecipesFor(AscEq.RecipeTypes.SALVAGING);
      RECIPES = List.copyOf(holders);
   }

   public static void replace(List<RecipeHolder<SalvagingRecipe>> recipes) {
      RECIPES = List.copyOf(recipes);
   }

   public static void clear() {
      RECIPES = List.of();
   }

   public static List<RecipeHolder<SalvagingRecipe>> findMatch(ItemStack stack) {
      return RECIPES.stream().filter(r -> ((SalvagingRecipe)r.value()).getInput().test(stack)).toList();
   }

   public static List<RecipeHolder<SalvagingRecipe>> all() {
      return RECIPES;
   }
}
