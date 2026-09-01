package com.skd.ascendantequipment.compat.jei;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.UnnamingRecipe;
import com.skd.ascendantequipment.affix.salvaging.SalvagingRecipe;
import com.skd.ascendantequipment.recipe.CharmInfusionRecipe;
import com.skd.ascendantequipment.recipe.MaliceRecipe;
import com.skd.ascendantequipment.recipe.PotionCharmRecipe;
import com.skd.ascendantequipment.recipe.SupremacyRecipe;
import com.skd.ascendantequipment.socket.AddSocketsRecipe;
import com.skd.ascendantequipment.socket.WithdrawalRecipe;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import com.skd.ascendantequipment.socket.gem.storage.GemCaseScreen;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import com.skd.ascendantequipment.util.SizedUpgradeRecipe;
import com.skd.ascendantenchanting.compat.InfusionRecipeCategory;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {
   public static final RecipeType<SmithingRecipe> APO_SMITHING = RecipeType.create(AscendantEquipment.MODID, "smithing", ApothSmithingRecipe.class);
   public static final RecipeType<SalvagingRecipe> SALVAGING = RecipeType.create(AscendantEquipment.MODID, "salvaging", SalvagingRecipe.class);
   public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = RecipeType.create(AscendantEquipment.MODID, "gem_cutting", PurityUpgradeRecipe.class);

   public ResourceLocation getPluginUid() {
      return AscendantEquipment.loc("adventure_module");
   }

   public void registerRecipes(IRecipeRegistration reg) {
      Component socketInfo = Component.translatable("info.ascendant_equipment.socketing");

      for (Gem gem : GemRegistry.INSTANCE.getValues()) {
         for (Purity purity : Purity.ALL_PURITIES) {
            if (purity.isAtLeast(gem.getMinPurity())) {
               reg.addIngredientInfo(gem.toStack(purity), VanillaTypes.ITEM_STACK, socketInfo);
            }
         }
      }

      reg.addIngredientInfo(
         new ItemStack(AscEq.Items.GEM_DUST), VanillaTypes.ITEM_STACK, Component.translatable("info.ascendant_equipment.gem_crushing")
      );
      reg.addIngredientInfo(
         new ItemStack(AscEq.Items.SIGIL_OF_UNNAMING), VanillaTypes.ITEM_STACK, Component.translatable("info.ascendant_equipment.unnaming")
      );
      reg.addRecipes(SALVAGING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(AscEq.RecipeTypes.SALVAGING).stream()
         .sorted(Comparator.comparing(RecipeHolder::id))
         .map(RecipeHolder::value)
         .toList());
      reg.addRecipes(GEM_CUTTING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(AscEq.RecipeTypes.GEM_CUTTING).stream()
         .map(RecipeHolder::value)
         .toList());
   }

   public void registerCategories(IRecipeCategoryRegistration reg) {
      reg.addRecipeCategories(new ApothSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
      reg.addRecipeCategories(new SalvagingCategory(reg.getJeiHelpers().getGuiHelper()));
      reg.addRecipeCategories(new GemCuttingCategory(reg.getJeiHelpers().getGuiHelper()));
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
      reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), APO_SMITHING);
      reg.addRecipeCatalyst(new ItemStack(AscEq.Blocks.SALVAGING_TABLE.value()), SALVAGING);
      reg.addRecipeCatalyst(new ItemStack(AscEq.Blocks.GEM_CUTTING_TABLE.value()), GEM_CUTTING);
   }

   public void registerGuiHandlers(IGuiHandlerRegistration reg) {
      reg.addGuiContainerHandler(GemCaseScreen.class, new IGuiContainerHandler<GemCaseScreen>() {
         public List<Rect2i> getGuiExtraAreas(GemCaseScreen screen) {
            return screen.getExclusionAreas();
         }
      });
   }

   public void registerItemSubtypes(ISubtypeRegistration reg) {
      reg.registerSubtypeInterpreter(AscEq.Items.GEM.value(), new GemSubtypes());
      reg.registerSubtypeInterpreter(AscEq.Items.POTION_CHARM.value(), new PotionCharmExtension.PotionCharmSubtypes());
   }

   public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration reg) {
      reg.getCraftingCategory().addExtension(PotionCharmRecipe.class, new PotionCharmExtension());
      if (AscendantEnchantingCompat.isLoaded()) {
         AEExtensions.register();
      }
      reg.getSmithingCategory().addExtension(SizedUpgradeRecipe.class, new SizedUpgradeRecipeExtension());
      reg.getSmithingCategory().addExtension(AddSocketsRecipe.class, new AddSocketsExtension());
      reg.getSmithingCategory().addExtension(WithdrawalRecipe.class, new WithdrawalExtension());
      reg.getSmithingCategory().addExtension(UnnamingRecipe.class, new UnnamingExtension());
      reg.getSmithingCategory().addExtension(MaliceRecipe.class, new MaliceExtension());
      reg.getSmithingCategory().addExtension(SupremacyRecipe.class, new SupremacyExtension());
   }

   private static class AscendantEnchantingCompat {
      static boolean isLoaded() {
         return FMLLoader.getLoadingModList().getModFileById("ascendant_enchanting") != null;
      }
   }

   private static class AEExtensions {
      static void register() {
         InfusionRecipeCategory.registerExtension(CharmInfusionRecipe.class, new CharmInfusionExtension());
      }
   }

   static class GemSubtypes implements ISubtypeInterpreter<ItemStack> {
      public String apply(ItemStack stack, UidContext context) {
         UnsocketedGem inst = UnsocketedGem.of(stack);
         if (!inst.isValid()) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         }
         return inst.gem().getId() + "@" + inst.purity().getSerializedName();
      }

      @Nullable
      public Object getSubtypeData(ItemStack ingredient, UidContext context) {
         return this.apply(ingredient, context);
      }

      @Nullable
      public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
         return this.apply(ingredient, context);
      }
   }
}
