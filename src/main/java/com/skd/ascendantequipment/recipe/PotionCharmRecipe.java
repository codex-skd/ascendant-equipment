package com.skd.ascendantequipment.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.item.PotionCharmItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Recipe.CommonInfo;
import net.minecraft.world.level.Level;

public class PotionCharmRecipe extends ShapedRecipe {
   private final CommonInfo common;
   private final CraftingBookInfo book;
   public static final MapCodec<PotionCharmRecipe> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            CommonInfo.MAP_CODEC.forGetter(PotionCharmRecipe::commonInfo),
            CraftingBookInfo.MAP_CODEC.forGetter(PotionCharmRecipe::bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern)
         )
         .apply(inst, PotionCharmRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, PotionCharmRecipe> STREAM_CODEC = StreamCodec.composite(
      CommonInfo.STREAM_CODEC,
      PotionCharmRecipe::commonInfo,
      CraftingBookInfo.STREAM_CODEC,
      PotionCharmRecipe::bookInfo,
      ShapedRecipePattern.STREAM_CODEC,
      recipe -> recipe.pattern,
      PotionCharmRecipe::new
   );
   public static final RecipeSerializer<PotionCharmRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   public PotionCharmRecipe(CommonInfo commonInfo, CraftingBookInfo bookInfo, ShapedRecipePattern pattern) {
      super(commonInfo, bookInfo, pattern, new ItemStackTemplate(AscEq.Items.POTION_CHARM));
      this.common = commonInfo;
      this.book = bookInfo;
   }

   public CommonInfo commonInfo() {
      return this.common;
   }

   public CraftingBookInfo bookInfo() {
      return this.book;
   }

   public ItemStack assemble(CraftingInput inv) {
      ItemStack out = super.assemble(inv);
      PotionContents contents = findPotion(inv);
      if (contents == PotionContents.EMPTY) {
         return ItemStack.EMPTY;
      }

      Holder<Potion> potion = (Holder<Potion>)contents.potion().get();
      out.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
      return out;
   }

   public boolean matches(CraftingInput inv, Level world) {
      return super.matches(inv, world) ? findPotion(inv) != PotionContents.EMPTY : false;
   }

   @SuppressWarnings("unchecked")
   public RecipeSerializer<ShapedRecipe> getSerializer() {
      return (RecipeSerializer<ShapedRecipe>)(RecipeSerializer<?>)SERIALIZER;
   }

   public static PotionContents findPotion(CraftingInput input) {
      PotionContents found = PotionContents.EMPTY;

      for (int i = 0; i < input.size(); i++) {
         ItemStack stack = input.getItem(i);
         PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
         if (contents != null) {
            if (!PotionCharmItem.isValidPotion((Holder<Potion>)contents.potion().orElse(Potions.WATER))) {
               return PotionContents.EMPTY;
            }

            if (found == PotionContents.EMPTY) {
               found = contents;
            } else if (!contents.equals(found)) {
               return PotionContents.EMPTY;
            }
         }
      }

      return found;
   }
}
