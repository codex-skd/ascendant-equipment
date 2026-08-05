package com.skd.ascendantequipment.mixin.client;

import com.skd.ascendantequipment.client.AscEqRenderStateHolder;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemStackRenderState.class, remap = false)
public abstract class ItemStackRenderStateMixin implements AscEqRenderStateHolder {
   @Unique
   private Float apoth$renderAlpha = null;

   @Unique
   @Nullable
   @Override
   public Float apoth$getRenderAlpha() {
      return this.apoth$renderAlpha;
   }

   @Unique
   @Override
   public void apoth$setRenderAlpha(@Nullable Float alpha) {
      this.apoth$renderAlpha = alpha;
   }

   @Inject(method = "clear", at = @At("HEAD"))
   private void apoth_clearRenderAlpha(CallbackInfo ci) {
      this.apoth$renderAlpha = null;
   }
}
