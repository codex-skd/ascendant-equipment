package com.skd.ascendantequipment.affix.salvaging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public class SalvagingRecipe implements Recipe<SingleRecipeInput> {
   public static final MapCodec<SalvagingRecipe> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(SalvagingRecipe::getInput),
            SalvagingRecipe.OutputData.CODEC.listOf().fieldOf("outputs").forGetter(SalvagingRecipe::getOutputs)
         )
         .apply(inst, SalvagingRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, SalvagingRecipe> STREAM_CODEC = StreamCodec.composite(
      Ingredient.CONTENTS_STREAM_CODEC,
      SalvagingRecipe::getInput,
      SalvagingRecipe.OutputData.STREAM_CODEC.apply(ByteBufCodecs.list()),
      SalvagingRecipe::getOutputs,
      SalvagingRecipe::new
   );
   protected final Ingredient input;
   protected final List<SalvagingRecipe.OutputData> outputs;
   public static final RecipeSerializer<SalvagingRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   public SalvagingRecipe(Ingredient input, List<SalvagingRecipe.OutputData> outputs) {
      this.input = input;
      this.outputs = outputs;
   }

   public boolean matches(SingleRecipeInput input, Level level) {
      return this.input.test(input.getItem(0));
   }

   public Ingredient getInput() {
      return this.input;
   }

   public List<SalvagingRecipe.OutputData> getOutputs() {
      return this.outputs;
   }

   public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
      return SERIALIZER;
   }

   public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
      return AscEq.RecipeTypes.SALVAGING;
   }

   @Deprecated
   public ItemStack assemble(SingleRecipeInput input) {
      return ItemStack.EMPTY;
   }

   public String group() {
      return "";
   }

   public boolean showNotification() {
      return false;
   }

   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   public boolean isSpecial() {
      return true;
   }

   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }

   public List<RecipeDisplay> display() {
      return List.of();
   }

   public record OutputData(ItemStackTemplate stack, int min, int max) {
      public static Codec<SalvagingRecipe.OutputData> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               ItemStackTemplate.CODEC.fieldOf("stack").forGetter(d -> d.stack),
               Codec.intRange(0, 99).fieldOf("min_count").forGetter(d -> d.min),
               Codec.intRange(1, 99).fieldOf("max_count").forGetter(d -> d.max)
            )
            .apply(inst, SalvagingRecipe.OutputData::new)
      );
      public static final Codec<List<SalvagingRecipe.OutputData>> LIST_CODEC = Codec.list(CODEC);
      public static final StreamCodec<RegistryFriendlyByteBuf, SalvagingRecipe.OutputData> STREAM_CODEC = StreamCodec.composite(
         ItemStackTemplate.STREAM_CODEC,
         SalvagingRecipe.OutputData::stack,
         ByteBufCodecs.VAR_INT,
         SalvagingRecipe.OutputData::min,
         ByteBufCodecs.VAR_INT,
         SalvagingRecipe.OutputData::max,
         SalvagingRecipe.OutputData::new
      );

      public OutputData(Item item, int min, int max) {
         this(new ItemStackTemplate(item), min, max);
      }
   }
}
