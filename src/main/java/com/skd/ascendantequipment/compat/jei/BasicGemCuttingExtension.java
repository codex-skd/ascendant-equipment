package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.socket.gem.cutting.BasicGemCuttingRecipe;
import java.util.Arrays;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class BasicGemCuttingExtension implements GemCuttingCategory.GemCuttingExtension<BasicGemCuttingRecipe> {
   public void setRecipe(IRecipeLayoutBuilder builder, BasicGemCuttingRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, Arrays.asList(recipe.base().getItems()));
      builder.addSlot(RecipeIngredientRole.INPUT, 48, 4)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.top().stream().flatMap(si -> Arrays.stream(si.getItems())).toList());
      builder.addSlot(RecipeIngredientRole.INPUT, 19, 56)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().flatMap(si -> Arrays.stream(si.getItems())).toList());
      builder.addSlot(RecipeIngredientRole.INPUT, 76, 56)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().flatMap(si -> Arrays.stream(si.getItems())).toList());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredient(VanillaTypes.ITEM_STACK, recipe.output());
   }
}
