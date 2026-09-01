package com.skd.ascendantequipment.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.ReactiveSmithingRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class SizedUpgradeRecipe extends SmithingTransformRecipe implements ReactiveSmithingRecipe {

    protected SizedIngredient addition;

    public SizedUpgradeRecipe(Ingredient template, Ingredient base, SizedIngredient addition, ItemStack result) {
        super(template, base, addition.ingredient(), result);
        this.addition = addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return this.template.test(input.template()) && this.base.test(input.base()) && this.addition.test(input.addition());
    }

    @Override
    public void onCraft(Container inv, ServerPlayer player, ItemStack output) {
        int size = this.addition.count() - 1;
        ItemStack stack = inv.getItem(2);
        stack.shrink(size);
        inv.setItem(2, stack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public Ingredient template() {
        return this.template;
    }

    public Ingredient base() {
        return this.base;
    }

    public SizedIngredient addition() {
        return this.addition;
    }

    public ItemStack result() {
        return this.result.copy();
    }

    public static class Serializer implements RecipeSerializer<SizedUpgradeRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        private static final MapCodec<SizedUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(
                Ingredient.CODEC.fieldOf("template").forGetter(r -> r.template),
                Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                SizedIngredient.FLAT_CODEC.fieldOf("addition").forGetter(r -> r.addition),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result))
            .apply(inst, SizedUpgradeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SizedUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.template,
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.base,
            SizedIngredient.STREAM_CODEC, r -> r.addition,
            ItemStack.STREAM_CODEC, r -> r.result,
            SizedUpgradeRecipe::new);

        @Override
        public MapCodec<SizedUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SizedUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

}
