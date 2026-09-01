package com.skd.ascendantequipment.mixin;

import net.minecraft.core.HolderOwner;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AnyHolderSet.class, remap = false)
public class AnyHolderSetMixin {
   @Inject(at = @At("HEAD"), method = "canSerializeIn", cancellable = true)
   public void apoth_canSerialize(HolderOwner<?> owner, CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(true);
   }
}
