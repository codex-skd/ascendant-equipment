package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.salvaging.SalvagingRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SalvagingCategory implements IRecipeCategory<SalvagingRecipe> {
   public static final ResourceLocation TEXTURES = AscendantEquipment.loc("textures/gui/salvage_jei.png");
   private final Component title = Component.translatable("title.ascendant_equipment.salvaging");
   private final IDrawable background;
   private final IDrawable icon;

   public SalvagingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 98, 74).addPadding(0, 0, 0, 0).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(AscEq.Items.SALVAGING_TABLE));
   }

   public RecipeType<SalvagingRecipe> getRecipeType() {
      return AdventureJEIPlugin.SALVAGING;
   }

   public Component getTitle() {
      return this.title;
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void draw(SalvagingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
      List<SalvagingRecipe.OutputData> outputs = recipe.getOutputs();
      Font font = Minecraft.getInstance().font;
      PoseStack pose = gfx.pose();
      int idx = 0;

      for (SalvagingRecipe.OutputData d : outputs) {
         pose.pushPose();
         pose.translate(0, 0, 200);
         String text = String.format("%d-%d", d.min(), d.max());
         float x = 59 + 18 * (idx % 2) + (16.0F - font.width(text) * 0.5F);
         float y = 23.0F + 18 * (idx / 2);
         float scale = 0.5F;
         pose.scale(scale, scale, 1);
         gfx.drawString(font, text, (int)(x / scale), (int)(y / scale), 0xFFFFFF);
         idx++;
         pose.popPose();
      }
   }

   public void setRecipe(IRecipeLayoutBuilder builder, SalvagingRecipe recipe, IFocusGroup focuses) {
      List<ItemStack> input = Arrays.asList(recipe.getInput().getItems());
      builder.addSlot(RecipeIngredientRole.INPUT, 5, 29).addIngredients(VanillaTypes.ITEM_STACK, input);
      List<SalvagingRecipe.OutputData> outputs = recipe.getOutputs();
      int idx = 0;

      for (SalvagingRecipe.OutputData d : outputs) {
         builder.addSlot(RecipeIngredientRole.OUTPUT, 59 + 18 * (idx % 2), 11 + 18 * (idx / 2)).addIngredient(VanillaTypes.ITEM_STACK, d.stack());
         idx++;
      }
   }
}
