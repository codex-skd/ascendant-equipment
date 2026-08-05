package com.skd.ascendantequipment.socket.gem.cutting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record BasicGemCuttingRecipe(Ingredient base, List<SizedIngredient> top, List<SizedIngredient> left, List<SizedIngredient> right, ItemStack output)
   implements GemCuttingRecipe {
   public static MapCodec<BasicGemCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            Ingredient.CODEC.fieldOf("base").forGetter(BasicGemCuttingRecipe::base),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("top").forGetter(BasicGemCuttingRecipe::top),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("left").forGetter(BasicGemCuttingRecipe::left),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("right").forGetter(BasicGemCuttingRecipe::right),
            ItemStack.CODEC.fieldOf("output").forGetter(BasicGemCuttingRecipe::output)
         )
         .apply(inst, BasicGemCuttingRecipe::new)
   );
   public static StreamCodec<RegistryFriendlyByteBuf, BasicGemCuttingRecipe> STREAM_CODEC = StreamCodec.composite(
      Ingredient.CONTENTS_STREAM_CODEC,
      BasicGemCuttingRecipe::base,
      SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
      BasicGemCuttingRecipe::top,
      SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
      BasicGemCuttingRecipe::left,
      SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
      BasicGemCuttingRecipe::right,
      ItemStack.STREAM_CODEC,
      BasicGemCuttingRecipe::output,
      BasicGemCuttingRecipe::new
   );
   public static final RecipeSerializer<BasicGemCuttingRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   public boolean matches(GemCuttingRecipe.CuttingRecipeInput input, Level level) {
      return !this.base.test(input.getBase())
         ? false
         : GemCuttingRecipe.anyMatch(input.getTop(), this.top)
            && GemCuttingRecipe.anyMatch(input.getLeft(), this.left)
            && GemCuttingRecipe.anyMatch(input.getRight(), this.right);
   }

   public ItemStack assemble(GemCuttingRecipe.CuttingRecipeInput input) {
      return this.output.copy();
   }

   public RecipeSerializer<? extends GemCuttingRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public void decrementInputs(GemCuttingRecipe.CuttingRecipeInput input, Level level) {
      SizedIngredient top = GemCuttingRecipe.getMatchOrThrow(input.getTop(), this.top);
      SizedIngredient left = GemCuttingRecipe.getMatchOrThrow(input.getLeft(), this.left);
      SizedIngredient right = GemCuttingRecipe.getMatchOrThrow(input.getRight(), this.right);
      input.shrink(1, top.count());
      input.shrink(2, left.count());
      input.shrink(3, right.count());
   }

   @Override
   public boolean isValidBaseItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return this.base.test(stack);
   }

   @Override
   public boolean isValidTopItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return GemCuttingRecipe.anyMatch(stack, this.top);
   }

   @Override
   public boolean isValidLeftItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return GemCuttingRecipe.anyMatch(stack, this.left);
   }

   @Override
   public boolean isValidRightItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return GemCuttingRecipe.anyMatch(stack, this.right);
   }
}
