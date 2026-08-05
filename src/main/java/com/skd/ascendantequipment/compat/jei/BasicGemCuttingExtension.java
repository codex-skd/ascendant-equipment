package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.socket.gem.cutting.BasicGemCuttingRecipe;
import java.util.stream.Stream;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class BasicGemCuttingExtension implements GemCuttingCategory.GemCuttingExtension<BasicGemCuttingRecipe> {
   public void setRecipe(IRecipeLayoutBuilder builder, BasicGemCuttingRecipe recipe, IFocusGroup focuses) {
      ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
      builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, recipe.base().display().resolveForStacks(ctx));
      builder.addSlot(RecipeIngredientRole.INPUT, 48, 4)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.top().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
      builder.addSlot(RecipeIngredientRole.INPUT, 19, 56)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
      builder.addSlot(RecipeIngredientRole.INPUT, 76, 56)
         .addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().flatMap(BasicGemCuttingExtension::sizedStacks).toList());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).add(VanillaTypes.ITEM_STACK, recipe.output());
   }

   private static Stream<ItemStack> sizedStacks(SizedIngredient si) {
      ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
      return si.ingredient().display().resolveForStacks(ctx).stream().map(s -> s.copyWithCount(si.count()));
   }
}
