package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.cutting.BasicGemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingBlock;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import java.util.IdentityHashMap;
import java.util.Map;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class GemCuttingCategory implements IRecipeCategory<GemCuttingRecipe> {
   public static final Identifier TEXTURES = AscendantEquipment.loc("textures/gui/gem_cutting_jei.png");
   private static final Map<Class<?>, GemCuttingCategory.GemCuttingExtension<?>> EXTENSIONS = new IdentityHashMap<>();
   private final IDrawable background;
   private final IDrawable icon;

   public GemCuttingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 148, 78).addPadding(0, 0, 0, 0).build();
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack((ItemLike)AscEq.Blocks.GEM_CUTTING_TABLE.value()));
   }

   public IRecipeType<GemCuttingRecipe> getRecipeType() {
      return AdventureJEIPlugin.GEM_CUTTING;
   }

   public Component getTitle() {
      return GemCuttingBlock.NAME;
   }

   public int getWidth() {
      return 148;
   }

   public int getHeight() {
      return 78;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void draw(GemCuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
      this.background.draw(gfx);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, GemCuttingRecipe recipe, IFocusGroup focuses) {
      GemCuttingCategory.GemCuttingExtension ext = EXTENSIONS.get(recipe.getClass());
      if (ext != null) {
         ext.setRecipe(builder, recipe, focuses);
      }
   }

   public static <T extends GemCuttingRecipe> void registerExtension(Class<T> cls, GemCuttingCategory.GemCuttingExtension<T> ext) {
      EXTENSIONS.put(cls, ext);
   }

   static {
      registerExtension(PurityUpgradeRecipe.class, new PurityUpgradeExtension());
      registerExtension(BasicGemCuttingRecipe.class, new BasicGemCuttingExtension());
   }

   public interface GemCuttingExtension<T extends GemCuttingRecipe> {
      void setRecipe(IRecipeLayoutBuilder var1, T var2, IFocusGroup var3);
   }
}
