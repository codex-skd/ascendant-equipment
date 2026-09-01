package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.AscEq;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class ReforgingRecipeCache {
   private static volatile List<RecipeHolder<ReforgingRecipe>> RECIPES = List.of();

   private ReforgingRecipeCache() {
   }

   public static void rebuildFromManager(RecipeManager manager) {
      Collection<RecipeHolder<ReforgingRecipe>> holders = manager.getAllRecipesFor(AscEq.RecipeTypes.REFORGING);
      RECIPES = List.copyOf(holders);
   }

   public static void clear() {
      RECIPES = List.of();
   }

   public static List<RecipeHolder<ReforgingRecipe>> all() {
      return RECIPES;
   }
}
