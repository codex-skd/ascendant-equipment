package com.skd.ascendantequipment.compat.jei;

import java.util.IdentityHashMap;
import java.util.Map;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

public class ApothSmithingCategory implements IRecipeCategory<SmithingRecipe> {
   public static final ResourceLocation RECIPE_GUI_VANILLA = ResourceLocation.fromNamespaceAndPath("jei", "textures/gui/gui_vanilla.png");
   private static final Map<Class<? extends SmithingRecipe>, Extension<SmithingRecipe>> EXTENSIONS = new IdentityHashMap<>();
   private final Component title = Component.translatable("title.ascendant_equipment.smithing");
   private final IDrawable background;
   private final IDrawable icon;

   public ApothSmithingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.drawableBuilder(RECIPE_GUI_VANILLA, 0, 168, 125, 18).addPadding(0, 16, 0, 0).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.SMITHING_TABLE));
   }

   public RecipeType<SmithingRecipe> getRecipeType() {
      return AdventureJEIPlugin.APO_SMITHING;
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

   public void draw(SmithingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
      EXTENSIONS.get(recipe.getClass()).draw(recipe, recipeSlotsView, gfx, mouseX, mouseY);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, SmithingRecipe recipe, IFocusGroup focuses) {
      EXTENSIONS.get(recipe.getClass()).setRecipe(builder, recipe, focuses);
   }

   public boolean isHandled(SmithingRecipe recipe) {
      return EXTENSIONS.containsKey(recipe.getClass());
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static <R extends SmithingRecipe> void registerExtension(Class<R> clazz, Extension<R> ext) {
      EXTENSIONS.put(clazz, (Extension)ext);
   }

   public interface Extension<R extends SmithingRecipe> extends IRecipeCategoryExtension<R> {
      void setRecipe(IRecipeLayoutBuilder var1, R var2, IFocusGroup var3);

      void draw(R var1, IRecipeSlotsView var2, GuiGraphics var3, double var4, double var6);
   }
}
