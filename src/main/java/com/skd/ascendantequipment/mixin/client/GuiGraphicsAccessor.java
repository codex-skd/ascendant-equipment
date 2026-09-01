package com.skd.ascendantequipment.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Accessor("tooltipStack")
    ItemStack getTooltipStack();

    @Accessor("tooltipStack")
    @Mutable
    void setTooltipStack(ItemStack stack);
}
