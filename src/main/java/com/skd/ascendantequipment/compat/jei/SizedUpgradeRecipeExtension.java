package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.util.SizedUpgradeRecipe;
import java.util.Arrays;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;

public class SizedUpgradeRecipeExtension implements ISmithingCategoryExtension<SizedUpgradeRecipe> {
   public <T extends IIngredientAcceptor<T>> void setTemplate(SizedUpgradeRecipe recipe, T acc) {
      acc.addIngredients(recipe.template());
   }

   public <T extends IIngredientAcceptor<T>> void setBase(SizedUpgradeRecipe recipe, T acc) {
      acc.addIngredients(recipe.base());
   }

   public <T extends IIngredientAcceptor<T>> void setAddition(SizedUpgradeRecipe recipe, T acc) {
      acc.addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.addition().getItems()));
   }

   public <T extends IIngredientAcceptor<T>> void setOutput(SizedUpgradeRecipe recipe, T acc) {
      acc.addItemStack(recipe.result());
   }
}
