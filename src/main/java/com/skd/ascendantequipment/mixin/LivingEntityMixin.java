package com.skd.ascendantequipment.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, remap = false)
public class LivingEntityMixin {
   @Unique
   @Nullable
   private Float originalHealthPercent;

   @Inject(method = "detectEquipmentUpdates", at = @At("HEAD"))
   private void apoth_cacheLastHealthPct(CallbackInfo ci) {
      if (!this.isPlayer()) {
         LivingEntity self = (LivingEntity)(Object)this;
         if (self.getHealth() > 0.0F && self.getMaxHealth() > 0.0F) {
            this.originalHealthPercent = self.getHealth() / self.getMaxHealth();
         } else {
            this.originalHealthPercent = null;
         }
      }
   }

   @Inject(method = "detectEquipmentUpdates", at = @At("TAIL"))
   private void apoth_updateHealthPct(CallbackInfo ci) {
      if (this.originalHealthPercent != null) {
         LivingEntity self = (LivingEntity)(Object)this;
         self.setHealth(self.getMaxHealth() * this.originalHealthPercent);
         this.originalHealthPercent = null;
      }
   }

   private boolean isPlayer() {
      return ((Object)this) instanceof Player;
   }
}
