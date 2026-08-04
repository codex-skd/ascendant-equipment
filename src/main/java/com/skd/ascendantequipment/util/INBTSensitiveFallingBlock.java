package com.skd.ascendantequipment.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface INBTSensitiveFallingBlock {
    ItemStack toStack(BlockState state, CompoundTag tag);
}
