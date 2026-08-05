package com.skd.ascendantequipment.mixin.client;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractSkeletonRenderer.class, remap = false)
public abstract class AbstractSkeletonRendererMixin {
   @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
   private void apoth_overrideCrossbowPose(AbstractSkeleton mob, HumanoidArm arm, CallbackInfoReturnable<ArmPose> cir) {
      if (mob.getMainArm() == arm && mob.isAggressive()) {
         ItemStack held = mob.getMainHandItem();
         if (held.getItem() instanceof CrossbowItem) {
            if (mob.isUsingItem() && mob.getUseItem() == held) {
               cir.setReturnValue(ArmPose.CROSSBOW_CHARGE);
            } else if (CrossbowItem.isCharged(held)) {
               cir.setReturnValue(ArmPose.CROSSBOW_HOLD);
            }
         }
      }
   }
}
