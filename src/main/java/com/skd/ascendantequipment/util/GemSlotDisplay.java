package com.skd.ascendantequipment.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.dynreg.tag.DynamicHolderSet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory.ForStacks;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.Type;

import java.util.stream.Stream;

public record GemSlotDisplay(DynamicHolderSet<Gem> gems, Purity purity) implements SlotDisplay {
    public static final MapCodec<GemSlotDisplay> CODEC = RecordCodecBuilder.mapCodec(
        inst -> inst.group(
            DynamicHolderSet.codec(GemRegistry.INSTANCE).optionalFieldOf("gems", DynamicHolderSet.empty()).forGetter(GemSlotDisplay::gems),
            Purity.CODEC.fieldOf("purity").forGetter(GemSlotDisplay::purity)
        )
            .apply(inst, GemSlotDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, GemSlotDisplay> STREAM_CODEC = StreamCodec.composite(
        DynamicHolderSet.streamCodec(GemRegistry.INSTANCE), GemSlotDisplay::gems, Purity.STREAM_CODEC, GemSlotDisplay::purity, GemSlotDisplay::new);
    public static final Type<GemSlotDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForStacks<T> stacks
            ? GemRegistry.INSTANCE.getValues().stream()
                .filter(gem -> this.gems.size() == 0 || this.gems.contains(gem))
                .map(gem -> GemItem.createStack(gem, this.purity, 1))
                .filter(gem -> GemItem.getPurity(gem) == this.purity)
                .map(stacks::forStack)
            : Stream.empty();
    }

    @Override
    public Type<GemSlotDisplay> type() {
        return TYPE;
    }
}
