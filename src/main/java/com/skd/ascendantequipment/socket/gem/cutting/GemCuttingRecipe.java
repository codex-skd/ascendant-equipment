package com.skd.ascendantequipment.socket.gem.cutting;

import com.skd.ascendantequipment.AscEq;
import com.skd.commontoolkit.cap.InternalItemHandler;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.include.com.google.common.base.Preconditions;

public interface GemCuttingRecipe extends Recipe<GemCuttingRecipe.CuttingRecipeInput> {
   void decrementInputs(GemCuttingRecipe.CuttingRecipeInput var1, Level var2);

   boolean isValidBaseItem(GemCuttingRecipe.CuttingRecipeInput var1, ItemStack var2);

   boolean isValidTopItem(GemCuttingRecipe.CuttingRecipeInput var1, ItemStack var2);

   boolean isValidLeftItem(GemCuttingRecipe.CuttingRecipeInput var1, ItemStack var2);

   boolean isValidRightItem(GemCuttingRecipe.CuttingRecipeInput var1, ItemStack var2);

   default RecipeType<? extends GemCuttingRecipe> getType() {
      return AscEq.RecipeTypes.GEM_CUTTING;
   }

   default String group() {
      return "";
   }

   default boolean showNotification() {
      return false;
   }

   default PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   default boolean isSpecial() {
      return true;
   }

   default RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }

   default List<RecipeDisplay> display() {
      return List.of();
   }

   @Nullable
   static SizedIngredient getMatch(ItemStack stack, List<SizedIngredient> ingredients) {
      for (SizedIngredient si : ingredients) {
         if (si.test(stack)) {
            return si;
         }
      }

      return null;
   }

   static SizedIngredient getMatchOrThrow(ItemStack stack, List<SizedIngredient> ingredients) {
      return (SizedIngredient)Preconditions.checkNotNull(getMatch(stack, ingredients), "Failed to find a match for " + stack);
   }

   static boolean anyMatch(ItemStack stack, List<SizedIngredient> ingredients) {
      return getMatch(stack, ingredients) != null;
   }

   class CuttingRecipeInput implements RecipeInput {
      private final InternalItemHandler inv;

      public CuttingRecipeInput(InternalItemHandler inv) {
         this.inv = inv;
      }

      public int size() {
         return this.inv.size();
      }

      public ItemStack getItem(int slot) {
         return ((ItemResource)this.inv.getResource(slot)).toStack(this.inv.getAmountAsInt(slot));
      }

      public ItemStack getBase() {
         return this.getItem(0);
      }

      public ItemStack getTop() {
         return this.getItem(1);
      }

      public ItemStack getLeft() {
         return this.getItem(2);
      }

      public ItemStack getRight() {
         return this.getItem(3);
      }

      public void shrink(int slot, int amount) {
         ItemResource res = (ItemResource)this.inv.getResource(slot);
         int currentAmount = this.inv.getAmountAsInt(slot);
         int newAmount = Math.max(0, currentAmount - amount);
         if (newAmount == 0) {
            this.inv.set(slot, ItemResource.EMPTY, 0);
         } else {
            this.inv.set(slot, res, newAmount);
         }
      }
   }
}
