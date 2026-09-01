package com.skd.ascendantequipment.socket.gem.cutting;

import com.skd.ascendantequipment.AscEq;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class GemCuttingRecipeCache {
   private static volatile List<RecipeHolder<GemCuttingRecipe>> RECIPES = List.of();

   private GemCuttingRecipeCache() {
   }

   public static void rebuildFromManager(RecipeManager manager) {
      Collection<RecipeHolder<GemCuttingRecipe>> holders = manager.getAllRecipesFor(AscEq.RecipeTypes.GEM_CUTTING);
      RECIPES = List.copyOf(holders);
   }

   public static void clear() {
      RECIPES = List.of();
   }

   public static List<RecipeHolder<GemCuttingRecipe>> all() {
      return RECIPES;
   }
}
