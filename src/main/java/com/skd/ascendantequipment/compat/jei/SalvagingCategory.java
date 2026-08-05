package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.salvaging.SalvagingRecipe;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.joml.Matrix3x2fStack;

public class SalvagingCategory implements IRecipeCategory<SalvagingRecipe> {
   public static final Identifier TEXTURES = AscendantEquipment.loc("textures/gui/salvage_jei.png");
   private final Component title = Component.translatable("title.ascendant_equipment.salvaging");
   private final IDrawable background;
   private final IDrawable icon;

   public SalvagingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 98, 74).addPadding(0, 0, 0, 0).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(AscEq.Items.SALVAGING_TABLE));
   }

   public IRecipeType<SalvagingRecipe> getRecipeType() {
      return AdventureJEIPlugin.SALVAGING;
   }

   public Component getTitle() {
      return this.title;
   }

   public int getWidth() {
      return 98;
   }

   public int getHeight() {
      return 74;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void draw(SalvagingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
      this.background.draw(gfx);
   }

   public void createRecipeExtras(IRecipeExtrasBuilder builder, SalvagingRecipe recipe, IFocusGroup focuses) {
      builder.addWidget(new SalvagingCategory.OutputCountWidget(recipe.getOutputs()));
   }

   public void setRecipe(IRecipeLayoutBuilder builder, SalvagingRecipe recipe, IFocusGroup focuses) {
      ItemStack focusStack = focuses.getFocuses(VanillaTypes.ITEM_STACK)
         .findFirst()
         .map(IFocus::getTypedValue)
         .<ItemStack>map(ITypedIngredient::getIngredient)
         .orElse(ItemStack.EMPTY);
      List<ItemStack> input;
      if (!focusStack.isEmpty() && recipe.getInput().test(focusStack)) {
         input = List.of(focusStack);
      } else {
         ContextMap ctx = SlotDisplayContext.fromLevel(Minecraft.getInstance().level);
         input = recipe.getInput().display().resolveForStacks(ctx);
      }

      builder.addSlot(RecipeIngredientRole.INPUT, 5, 29).addIngredients(VanillaTypes.ITEM_STACK, input);
      List<SalvagingRecipe.OutputData> outputs = recipe.getOutputs();
      int idx = 0;

      for (SalvagingRecipe.OutputData d : outputs) {
         builder.addSlot(RecipeIngredientRole.OUTPUT, 59 + 18 * (idx % 2), 11 + 18 * (idx / 2)).addIngredient(VanillaTypes.ITEM_STACK, d.stack().create());
         idx++;
      }
   }

   private static class OutputCountWidget implements IRecipeWidget {
      private final List<SalvagingRecipe.OutputData> outputs;

      OutputCountWidget(List<SalvagingRecipe.OutputData> outputs) {
         this.outputs = outputs;
      }

      public ScreenPosition getPosition() {
         return new ScreenPosition(0, 0);
      }

      public void drawWidget(GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
         Font font = Minecraft.getInstance().font;
         Matrix3x2fStack pose = gfx.pose();
         int idx = 0;

         for (SalvagingRecipe.OutputData d : this.outputs) {
            pose.pushMatrix();
            String text = String.format("%d-%d", d.min(), d.max());
            float x = 59 + 18 * (idx % 2) + (16.0F - font.width(text) * 0.5F);
            float y = 23.0F + 18 * (idx / 2);
            float scale = 0.5F;
            pose.scale(scale, scale);
            gfx.text(font, text, (int)(x / scale), (int)(y / scale), -1);
            idx++;
            pose.popMatrix();
         }
      }
   }
}
