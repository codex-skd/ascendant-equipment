package com.skd.ascendantequipment.mixin.client;

import com.skd.ascendantequipment.client.RadialProgressTracker;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MultiPlayerGameMode.class, remap = false)
public class MultiPlayerGameModeMixin {
   @Inject(method = "destroyBlock", at = @At("RETURN"))
   private void apoth_onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if ((Boolean)cir.getReturnValue()) {
         RadialProgressTracker.breakClientBlocks((MultiPlayerGameMode)(Object)this, pos);
      }
   }
}
