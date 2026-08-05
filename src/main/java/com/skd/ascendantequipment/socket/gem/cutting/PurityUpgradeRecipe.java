package com.skd.ascendantequipment.socket.gem.cutting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record PurityUpgradeRecipe(Purity purity, List<SizedIngredient> left, List<SizedIngredient> right) implements GemCuttingRecipe {
   public static MapCodec<PurityUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            Purity.CODEC.fieldOf("purity").forGetter(PurityUpgradeRecipe::purity),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("left").forGetter(PurityUpgradeRecipe::left),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("right").forGetter(PurityUpgradeRecipe::right)
         )
         .apply(inst, PurityUpgradeRecipe::new)
   );
   public static StreamCodec<RegistryFriendlyByteBuf, PurityUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
      Purity.STREAM_CODEC,
      PurityUpgradeRecipe::purity,
      SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
      PurityUpgradeRecipe::left,
      SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
      PurityUpgradeRecipe::right,
      PurityUpgradeRecipe::new
   );
   public static final RecipeSerializer<PurityUpgradeRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   public ItemStack assemble(GemCuttingRecipe.CuttingRecipeInput input) {
      ItemStack out = input.getBase().copy();
      GemItem.setPurity(out, GemItem.getPurity(out).next());
      return out;
   }

   public RecipeSerializer<? extends GemCuttingRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public void decrementInputs(GemCuttingRecipe.CuttingRecipeInput input, Level level) {
      SizedIngredient left = GemCuttingRecipe.getMatchOrThrow(input.getLeft(), this.left);
      SizedIngredient right = GemCuttingRecipe.getMatchOrThrow(input.getRight(), this.right);
      input.shrink(1, 1);
      input.shrink(2, left.count());
      input.shrink(3, right.count());
   }

   public boolean matches(GemCuttingRecipe.CuttingRecipeInput input, Level level) {
      UnsocketedGem baseInst = UnsocketedGem.of(input.getBase());
      UnsocketedGem topInst = UnsocketedGem.of(input.getTop());
      return baseInst.isValid() && baseInst.purity() == this.purity && baseInst.equals(topInst)
         ? GemCuttingRecipe.anyMatch(input.getLeft(), this.left) && GemCuttingRecipe.anyMatch(input.getRight(), this.right)
         : false;
   }

   @Override
   public boolean isValidBaseItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      UnsocketedGem inst = UnsocketedGem.of(stack);
      return inst.isValid() && !inst.isPerfect();
   }

   @Override
   public boolean isValidTopItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      UnsocketedGem baseInst = UnsocketedGem.of(input.getBase());
      if (baseInst.isValid() && !baseInst.isPerfect()) {
         UnsocketedGem inst = UnsocketedGem.of(stack);
         return baseInst.equals(inst);
      } else {
         return false;
      }
   }

   @Override
   public boolean isValidLeftItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return this.left.stream().<Ingredient>map(SizedIngredient::ingredient).anyMatch(i -> i.test(stack));
   }

   @Override
   public boolean isValidRightItem(GemCuttingRecipe.CuttingRecipeInput input, ItemStack stack) {
      return this.right.stream().<Ingredient>map(SizedIngredient::ingredient).anyMatch(i -> i.test(stack));
   }
}
