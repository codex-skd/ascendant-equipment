package com.skd.ascendantequipment.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractSkeleton.class, remap = false)
public abstract class AbstractSkeletonMixin extends Monster implements CrossbowAttackMob {
    @Unique
    protected final RangedCrossbowAttackGoal<AbstractSkeletonMixin> apoth_crossbowGoal = new RangedCrossbowAttackGoal<>(this, 1, 15F);
    @Final
    @Shadow
    private RangedBowAttackGoal<AbstractSkeleton> bowGoal;
    @Final
    @Shadow
    private MeleeAttackGoal meleeGoal;

    protected AbstractSkeletonMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    public void apoth_performRangedCrossbowAttack(LivingEntity target, float velocity, CallbackInfo ci) {
        ItemStack crossbow = this.apoth_getCrossbow();
        if (!crossbow.isEmpty()) {
            this.performCrossbowAttack(this, 1.6F);
            ci.cancel();
        }
    }

    @Inject(method = "reassessWeaponGoal()V", at = @At("HEAD"), cancellable = true)
    public void apoth_pickCrossbowIfAvailable(CallbackInfo ci) {
        if (this.level() != null && !this.level().isClientSide) {
            this.goalSelector.removeGoal(this.apoth_crossbowGoal);
            ItemStack crossbow = this.apoth_getCrossbow();
            if (!crossbow.isEmpty()) {
                this.goalSelector.removeGoal(this.meleeGoal);
                this.goalSelector.removeGoal(this.bowGoal);
                this.goalSelector.addGoal(4, this.apoth_crossbowGoal);
                ci.cancel();
            }
        }
    }

    @Override
    public void setChargingCrossbow(boolean chargingCrossbow) {
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Unique
    private ItemStack apoth_getCrossbow() {
        ItemStack stack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, CrossbowItem.class::isInstance));
        if (stack.getItem() instanceof CrossbowItem) {
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
