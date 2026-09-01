package com.skd.ascendantequipment.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityMixin {
   @Shadow
   public abstract Component getCustomName();

   @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
   public void apoth_getTeamColor(CallbackInfoReturnable<Integer> cir) {
      int color = cir.getReturnValueI();
      if (color == 16777215) {
         Component name = this.getCustomName();
         if (name != null && name.getStyle().getColor() != null) {
            color = name.getStyle().getColor().getValue();
         }
      }

      cir.setReturnValue(color);
   }
}
