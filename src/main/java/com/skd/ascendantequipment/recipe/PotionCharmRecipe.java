package com.skd.ascendantequipment.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.item.PotionCharmItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class PotionCharmRecipe extends ShapedRecipe {

    public PotionCharmRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern) {
        super(group, category, pattern, new ItemStack(AscEq.Items.POTION_CHARM.value()));
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider regs) {
        ItemStack out = super.assemble(inv, regs);
        PotionContents contents = findPotion(inv);
        if (contents == PotionContents.EMPTY) {
            return ItemStack.EMPTY;
        }
        Holder<Potion> potion = contents.potion().get();
        out.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return out;
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        if (super.matches(inv, world)) {
            return findPotion(inv) != PotionContents.EMPTY;
        }
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static PotionContents findPotion(CraftingInput input) {
        PotionContents found = PotionContents.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents == null) {
                continue;
            }

            if (!PotionCharmItem.isValidPotion(contents.potion().orElse(Potions.WATER))) {
                return PotionContents.EMPTY;
            }

            if (found == PotionContents.EMPTY) {
                found = contents;
            }
            else if (!contents.equals(found)) {
                return PotionContents.EMPTY;
            }
        }

        return found;
    }

    public static class Serializer implements RecipeSerializer<PotionCharmRecipe> {

        public static final MapCodec<PotionCharmRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(
                com.mojang.serialization.Codec.STRING.optionalFieldOf("group", "").forGetter(PotionCharmRecipe::getGroup),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(PotionCharmRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern))
            .apply(inst, PotionCharmRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PotionCharmRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork);

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<PotionCharmRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PotionCharmRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static PotionCharmRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            return new PotionCharmRecipe(s, craftingbookcategory, shapedrecipepattern);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, PotionCharmRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
        }
    }

}
