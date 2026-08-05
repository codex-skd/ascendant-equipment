package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class PurityUpgradeExtension implements GemCuttingCategory.GemCuttingExtension<PurityUpgradeRecipe> {
   public void setRecipe(IRecipeLayoutBuilder builder, PurityUpgradeRecipe recipe, IFocusGroup focuses) {
      DynamicHolder<Gem> focus = GemItem.getGem(
         focuses.getFocuses(VanillaTypes.ITEM_STACK)
            .findFirst()
            .map(IFocus::getTypedValue)
            .<ItemStack>map(ITypedIngredient::getIngredient)
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
      builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, this.toStacks(recipe.left()));
      builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, this.toStacks(recipe.right()));
      builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredients(VanillaTypes.ITEM_STACK, outputs);
   }

   private List<ItemStack> toStacks(List<SizedIngredient> ingredients) {
      ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
      List<ItemStack> stacks = new ArrayList<>();

      for (SizedIngredient i : ingredients) {
         i.ingredient().display().resolveForStacks(ctx).forEach(stack -> stacks.add(stack.copyWithCount(i.count())));
      }

      return stacks;
   }
}
