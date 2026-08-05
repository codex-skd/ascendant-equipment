package com.skd.ascendantequipment.socket.gem.cutting;

import com.skd.ascendantequipment.AscEq;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

public final class GemCuttingRecipeCache {
   private static volatile List<RecipeHolder<GemCuttingRecipe>> RECIPES = List.of();

   private GemCuttingRecipeCache() {
   }

   public static void rebuildFromMap(RecipeMap map) {
      Collection<RecipeHolder<GemCuttingRecipe>> holders = map.byType(AscEq.RecipeTypes.GEM_CUTTING);
      RECIPES = List.copyOf(holders);
   }

   public static void clear() {
      RECIPES = List.of();
   }

   public static List<RecipeHolder<GemCuttingRecipe>> all() {
      return RECIPES;
   }
}
