package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class PurityUpgradeExtension implements GemCuttingCategory.GemCuttingExtension<PurityUpgradeRecipe> {
   public void setRecipe(IRecipeLayoutBuilder builder, PurityUpgradeRecipe recipe, IFocusGroup focuses) {
      DynamicHolder<Gem> focus = GemItem.getGem(
         focuses.getFocuses(VanillaTypes.ITEM_STACK)
            .findFirst()
            .map(IFocus::getTypedValue)
            .map(ITypedIngredient::getIngredient)
            .orElse(ItemStack.EMPTY)
      );
      List<ItemStack> inputs = new ArrayList<>();
      List<ItemStack> outputs = new ArrayList<>();
      if (focus.isBound()) {
         inputs.add(((Gem)focus.get()).toStack(recipe.purity()));
         outputs.add(((Gem)focus.get()).toStack(recipe.purity().next()));
      } else {
         GemRegistry.INSTANCE.getValues().stream().forEachOrdered(g -> {
            if (recipe.purity().isAtLeast(g.getMinPurity())) {
               inputs.add(g.toStack(recipe.purity()));
               outputs.add(g.toStack(recipe.purity().next()));
            }
         });
      }

      builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, inputs);
      builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, inputs);
      builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().flatMap(si -> Arrays.stream(si.getItems())).toList());
      builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().flatMap(si -> Arrays.stream(si.getItems())).toList());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredients(VanillaTypes.ITEM_STACK, outputs);
   }
}
