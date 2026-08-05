package com.skd.ascendantequipment.mixin;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnchantmentHelper.class, remap = false)
public class EnchantmentHelperMixin {
   @Inject(
      at = @At("RETURN"),
      method = "getDamageProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F",
      cancellable = true
   )
   private static void apoth_getDamageProtection(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Float> cir) {
      float prot = cir.getReturnValueF();

      for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
         ItemStack s = entity.getItemBySlot(slot);
         prot += SocketHelper.getGems(s).getDamageProtection(source);
         Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(s);

         for (AffixInstance inst : affixes.values()) {
            prot += inst.getDamageProtection(source);
         }
      }

      cir.setReturnValue(prot);
   }

   @Inject(
      at = @At("RETURN"),
      method = "modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
      cancellable = true
   )
   private static void apoth_modifyDamage(
      ServerLevel level, ItemStack tool, Entity entity, DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir
   ) {
      float dmg = cir.getReturnValueF();
      dmg += SocketHelper.getGems(tool).getDamageBonus(entity);
      Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(tool);

      for (AffixInstance inst : affixes.values()) {
         dmg += inst.getDamageBonus(entity);
      }

      cir.setReturnValue(dmg);
   }

   @Inject(
      at = @At("TAIL"),
      method = "doPostAttackEffectsWithItemSource(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;)V"
   )
   private static void apoth_doPostAttackEffectsWithItemSource(
      ServerLevel level, Entity target, DamageSource damageSource, @Nullable ItemStack itemSource, CallbackInfo ci
   ) {
      if (damageSource.getEntity() instanceof LivingEntity user) {
         for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack s = user.getItemBySlot(slot);
            SocketHelper.getGems(s).doPostAttack(user, target);
            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(s);

            for (AffixInstance inst : affixes.values()) {
               int old = target.invulnerableTime;
               target.invulnerableTime = 0;
               inst.doPostAttack(user, target);
               target.invulnerableTime = old;
            }
         }
      }

      if (target instanceof LivingEntity livingTarget) {
         for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack s = livingTarget.getItemBySlot(slot);
            SocketHelper.getGems(s).doPostHurt(livingTarget, damageSource);
            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(s);

            for (AffixInstance inst : affixes.values()) {
               inst.doPostHurt(livingTarget, damageSource);
            }
         }
      }
   }

   @Inject(
      at = @At("RETURN"),
      method = "processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I",
      cancellable = true
   )
   private static void apoth_processAffixDurability(ServerLevel level, ItemStack stack, int damage, CallbackInfoReturnable<Integer> cir) {
      if (cir.getReturnValueI() > 0) {
         int amount = cir.getReturnValueI();
         double chance = ((Float)stack.getOrDefault(AscEq.Components.DURABILITY_BONUS, 0.0F)).floatValue();
         if (stack.has(AscEq.Components.SOCKETED_GEMS)) {
            double socketBonus = SocketHelper.getGems(stack).getDurabilityBonusPercentage().reduce(0.0, ApothMiscUtil::duraProd);
            chance = ApothMiscUtil.duraProd(chance, socketBonus);
         }

         if (stack.has(AscEq.Components.AFFIXES)) {
            double afxBonus = AffixHelper.streamAffixes(stack).mapToDouble(AffixInstance::getDurabilityBonusPercentage).reduce(0.0, ApothMiscUtil::duraProd);
            chance = ApothMiscUtil.duraProd(chance, afxBonus);
         }

         int delta = 1;
         int blocked = 0;
         if (chance < 0.0) {
            delta = -1;
            chance = -chance;
         }

         if (chance > 0.0) {
            for (int i = 0; i < amount; i++) {
               if (level.getRandom().nextFloat() <= chance) {
                  blocked += delta;
               }
            }
         }

         cir.setReturnValue(amount - blocked);
      }
   }
}
