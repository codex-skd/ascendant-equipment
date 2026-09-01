package com.skd.ascendantequipment.mixin;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = LivingEntity.class, remap = false)
public interface LivingEntityInvoker {
   @Invoker
   boolean callCheckTotemDeathProtection(DamageSource var1);

   @Invoker
   SoundEvent callGetDeathSound();

   @Invoker
   float callGetSoundVolume();

   @Invoker
   void callOnEffectUpdated(MobEffectInstance var1, boolean var2, @Nullable Entity var3);
}
