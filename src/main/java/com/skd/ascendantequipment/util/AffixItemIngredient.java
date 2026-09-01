package com.skd.ascendantequipment.util;

import java.util.Arrays;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public class AffixItemIngredient implements ICustomIngredient {

    public static final MapCodec<AffixItemIngredient> CODEC = RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").xmap(AffixItemIngredient::new, a -> a.rarity);
    public static final StreamCodec<ByteBuf, AffixItemIngredient> STREAM_CODEC = RarityRegistry.INSTANCE.holderStreamCodec().map(AffixItemIngredient::new, a -> a.rarity);
    public static final IngredientType<AffixItemIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

    protected final DynamicHolder<LootRarity> rarity;

    public AffixItemIngredient(DynamicHolder<LootRarity> rarity) {
        this.rarity = rarity;
    }

    @Override
    public boolean test(ItemStack stack) {
        var rarity = AffixHelper.getRarity(stack);
        var affixes = AffixHelper.getAffixes(stack);
        return affixes.size() > 0 && rarity.isBound() && rarity == this.rarity;
    }

    @Override
    public Stream<ItemStack> getItems() {
        if (!this.rarity.isBound()) return Stream.empty();
        return createFakeDisplayItems(this.getRarity());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public LootRarity getRarity() {
        return this.rarity.get();
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    private static Stream<ItemStack> createFakeDisplayItems(LootRarity rarity) {
        RandomSource src = new LegacyRandomSource(0);
        return Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS).stream()
            .map(ItemStack::new)
            .map(stack -> {
                LootController.createLootItem(stack, rarity, GenContext.dummy(src));
                AffixHelper.setName(stack, Component.translatable("text.ascendant_equipment.any_x_item", rarity.toComponent(), "").withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false)));
                return stack;
            });
    }

}
