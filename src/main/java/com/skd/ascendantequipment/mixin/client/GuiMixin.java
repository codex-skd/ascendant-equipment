package com.skd.ascendantequipment.mixin.client;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = Gui.class, remap = false)
public class GuiMixin {
   @ModifyConstant(method = "setOverlayMessage")
   public int apoth_extendTime(int old) {
      return 160;
   }
}
