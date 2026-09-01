package com.skd.ascendantequipment.mixin;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class MHFMixinLivingEntity {
    @Unique
    @Nullable
    private Float actualHealth = null;

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void maxhealthfix$readAdditionalSaveData(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains("Health", Tag.TAG_ANY_NUMERIC)) {
            final float savedHealth = tag.getFloat("Health");
            if (savedHealth > this.getMaxHealth() && savedHealth > 0) {
                this.actualHealth = savedHealth;
            }
        }
    }

    @Inject(method = "detectEquipmentUpdates()V", at = @At("RETURN"))
    private void maxhealthfix$detectEquipmentUpdates(CallbackInfo callback) {
        if (this.actualHealth != null) {
            if (this.actualHealth > 0 && this.actualHealth > this.getHealth()) {
                this.setHealth(this.actualHealth);
            }
            this.actualHealth = null;
        }
    }

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float newHealth);
}
