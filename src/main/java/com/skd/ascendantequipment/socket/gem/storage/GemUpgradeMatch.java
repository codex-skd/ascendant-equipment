package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingMenu;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import java.util.EnumMap;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

record GemUpgradeMatch(PurityUpgradeRecipe recipe, int leftSlot, int rightSlot, SizedIngredient leftIng, SizedIngredient rightIng) {
   void execute(Container matInv, EnumMap<Purity, Integer> map) {
      map.put(this.recipe.purity(), map.get(this.recipe.purity()) - 2);
      map.put(this.recipe.purity().next(), map.get(this.recipe.purity().next()) + 1);
      matInv.removeItem(this.leftSlot, this.leftIng.count());
      matInv.removeItem(this.rightSlot, this.rightIng.count());
      matInv.setChanged();
   }

   @Nullable
   static GemUpgradeMatch findMatch(Level level, Purity purity, EnumMap<Purity, Integer> map, Container matInv) {
      Purity prev = Purity.values()[purity.ordinal() - 1];
      if (map.get(prev) < 2) {
         return null;
      }

      for (RecipeHolder<GemCuttingRecipe> holder : GemCuttingMenu.getRecipes(level)) {
         if (holder.value() instanceof PurityUpgradeRecipe rec && rec.purity() == prev) {
            int leftSlot = -1;
            int rightSlot = -1;
            SizedIngredient leftIng = null;
            SizedIngredient rightIng = null;

            for (int i = 0; i < matInv.getContainerSize(); i++) {
               ItemStack stack = matInv.getItem(i);
               if (!stack.isEmpty()) {
                  if (leftIng == null) {
                     leftIng = GemCuttingRecipe.getMatch(stack, rec.left());
                     if (leftIng != null) {
                        leftSlot = i;
                        continue;
                     }
                  }

                  if (rightIng == null) {
                     rightIng = GemCuttingRecipe.getMatch(stack, rec.right());
                     if (rightIng != null) {
                        rightSlot = i;
                     }
                  }
               }
            }

            if (leftIng != null && rightIng != null) {
               return new GemUpgradeMatch(rec, leftSlot, rightSlot, leftIng, rightIng);
            }
         }
      }

      return null;
   }
}
