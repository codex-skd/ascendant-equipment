package com.skd.ascendantequipment.util;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory.ForStacks;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.Type;

import java.util.stream.Stream;

public record SpawnEggSlotDisplay() implements SlotDisplay {
    public static final SpawnEggSlotDisplay INSTANCE = new SpawnEggSlotDisplay();
    public static final MapCodec<SpawnEggSlotDisplay> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnEggSlotDisplay> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final Type<SpawnEggSlotDisplay> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForStacks<T> stacks
            ? BuiltInRegistries.ITEM.stream().filter(item -> item instanceof SpawnEggItem).map(ItemStack::new).map(stacks::forStack)
            : Stream.empty();
    }

    @Override
    public Type<SpawnEggSlotDisplay> type() {
        return TYPE;
    }
}
