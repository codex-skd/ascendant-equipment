package com.skd.ascendantequipment.socket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.util.ApothSmithingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

public class AddSocketsRecipe extends ApothSmithingRecipe {
   public static final MapCodec<AddSocketsRecipe> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            Ingredient.CODEC.fieldOf("input").forGetter(AddSocketsRecipe::getInput),
            Codec.intRange(0, 16).fieldOf("max_sockets").forGetter(AddSocketsRecipe::getMaxSockets)
         )
         .apply(inst, AddSocketsRecipe::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, AddSocketsRecipe> STREAM_CODEC = StreamCodec.composite(
      Ingredient.CONTENTS_STREAM_CODEC, AddSocketsRecipe::getInput, ByteBufCodecs.VAR_INT, AddSocketsRecipe::getMaxSockets, AddSocketsRecipe::new
   );
   private final Ingredient input;
   private final int maxSockets;
   public static final RecipeSerializer<AddSocketsRecipe> SERIALIZER = new RecipeSerializer(CODEC, STREAM_CODEC);

   public AddSocketsRecipe(Ingredient input, int maxSockets) {
      super(BASE_PLACEHOLDER, input, ItemStack.EMPTY);
      this.input = input;
      this.maxSockets = maxSockets;
   }

   @Override
   public boolean matches(SmithingRecipeInput inv, Level level) {
      ItemStack in = inv.getItem(1);
      return !LootCategory.forItem(in).isNone() && SocketHelper.getSockets(in) < this.getMaxSockets() && this.getInput().test(inv.getItem(2));
   }

   @Override
   public ItemStack assemble(SmithingRecipeInput inv) {
      ItemStack out = inv.getItem(1).copy();
      if (out.isEmpty()) {
         return ItemStack.EMPTY;
      }

      int sockets = SocketHelper.getSockets(out) + 1;
      SocketHelper.setSockets(out, sockets);
      return out;
   }

   public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   public Ingredient getInput() {
      return this.input;
   }

   public int getMaxSockets() {
      return this.maxSockets;
   }
}
