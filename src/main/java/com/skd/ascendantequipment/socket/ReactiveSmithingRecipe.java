package com.skd.ascendantequipment.socket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface ReactiveSmithingRecipe {
   void onCraft(Container var1, ServerPlayer var2, ItemStack var3);
}
