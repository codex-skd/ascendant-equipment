package com.skd.ascendantequipment.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.dynreg.DynamicHolder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory.ForStacks;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.Type;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

import java.util.Arrays;
import java.util.stream.Stream;

public record AffixItemSlotDisplay(DynamicHolder<LootRarity> rarity) implements SlotDisplay {
    public static final MapCodec<AffixItemSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(RarityRegistry.INSTANCE.holderCodec().fieldOf("rarity").forGetter(AffixItemSlotDisplay::rarity)).apply(i, AffixItemSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AffixItemSlotDisplay> STREAM_CODEC = RarityRegistry.INSTANCE.holderStreamCodec()
        .map(AffixItemSlotDisplay::new, AffixItemSlotDisplay::rarity).cast();
    public static final Type<AffixItemSlotDisplay> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof ForStacks<T> stacks && this.rarity.isBound()) {
            LootRarity r = this.rarity.get();
            RandomSource src = new LegacyRandomSource(0L);
            return Arrays.asList(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS)
                .stream()
                .<ItemStack>map(ItemStack::new)
                .map(stack -> {
                    LootController.createLootItem(stack, r, GenContext.dummy(src));
                    AffixHelper.setName(stack, Component.translatable("text.ascendant_equipment.any_x_item", new Object[] { r.toComponent(), "" }).withStyle(Style.EMPTY.withColor(r.color()).withItalic(false)));
                    return stack;
                })
                .map(stacks::forStack);
        }
        return Stream.empty();
    }

    @Override
    public Type<AffixItemSlotDisplay> type() {
        return TYPE;
    }
}
