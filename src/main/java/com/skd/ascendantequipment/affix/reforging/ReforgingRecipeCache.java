package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.AscEq;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

public final class ReforgingRecipeCache {
   private static volatile List<RecipeHolder<ReforgingRecipe>> RECIPES = List.of();

   private ReforgingRecipeCache() {
   }

   public static void rebuildFromMap(RecipeMap map) {
      Collection<RecipeHolder<ReforgingRecipe>> holders = map.byType(AscEq.RecipeTypes.REFORGING);
      RECIPES = List.copyOf(holders);
   }

   public static void clear() {
      RECIPES = List.of();
   }

   public static List<RecipeHolder<ReforgingRecipe>> all() {
      return RECIPES;
   }
}
