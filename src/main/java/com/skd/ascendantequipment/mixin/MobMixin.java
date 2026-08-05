package com.skd.ascendantequipment.mixin;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.attachments.BonusLootTables;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Mob.class, remap = false)
public abstract class MobMixin extends LivingEntity {
   protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
      super(entityType, level);
   }

   @Inject(at = @At("TAIL"), method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V")
   public void apoth_dropBonusLootTables(ServerLevel level, DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci) {
      if (this.hasData(AscEq.Attachments.BONUS_LOOT_TABLES)) {
         ((BonusLootTables)this.getData(AscEq.Attachments.BONUS_LOOT_TABLES)).drop((Mob)(Object)this, damageSource, hitByPlayer);
      }
   }
}
