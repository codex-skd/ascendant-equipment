package com.skd.ascendantequipment.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantenchanting.table.EnchantingStatRegistry.Stats;
import com.skd.ascendantenchanting.table.infusion.InfusionRecipe;
import com.skd.ascendantequipment.AscEq;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.ItemLike;

public class CharmInfusionRecipe extends InfusionRecipe {
    public static final MapCodec<CharmInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(
        inst -> inst.group(
            Stats.CODEC.fieldOf("requirements").forGetter(InfusionRecipe::getRequirements),
            Stats.CODEC.optionalFieldOf("max_requirements", NO_MAX).forGetter(InfusionRecipe::getMaxRequirements))
            .apply(inst, CharmInfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CharmInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
        Stats.STREAM_CODEC, InfusionRecipe::getRequirements,
        Stats.STREAM_CODEC, InfusionRecipe::getMaxRequirements,
        CharmInfusionRecipe::new);

    public CharmInfusionRecipe(Stats requirements, Stats maxRequirements) {
        super(charm(), potion(), requirements, maxRequirements);
    }

    private static ItemStack charm() {
        ItemStack stack = new ItemStack((ItemLike) AscEq.Items.POTION_CHARM.value());
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return stack;
    }

    private static Ingredient potion() {
        return Ingredient.of((ItemLike) AscEq.Items.POTION_CHARM.value());
    }

    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        return !input.has(DataComponents.UNBREAKABLE) && super.matches(input, eterna, quanta, arcana);
    }

    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        ItemStack out = input.copy();
        out.setDamageValue(0);
        out.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        return out;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<CharmInfusionRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<CharmInfusionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CharmInfusionRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }

}
