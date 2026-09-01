package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.effect.MobEffectAffix;
import com.skd.ascendantequipment.mixin.LivingEntityInvoker;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantattributes.api.AbilityCooldowns;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class MobEffectBonus extends GemBonus {
   public static final Codec<MobEffectBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            gemClass(),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
            MobEffectAffix.Target.CODEC.fieldOf("target").forGetter(a -> a.target),
            Purity.mapCodec(MobEffectBonus.EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
            Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
            Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit)
         )
         .apply(inst, MobEffectBonus::new)
   );
   protected final Holder<MobEffect> effect;
   protected final MobEffectAffix.Target target;
   protected final Map<Purity, MobEffectBonus.EffectData> values;
   protected final boolean stackOnReapply;
   protected final int stackingLimit;

   public MobEffectBonus(
      GemClass gemClass,
      Holder<MobEffect> effect,
      MobEffectAffix.Target target,
      Map<Purity, MobEffectBonus.EffectData> values,
      boolean stackOnReapply,
      int stackingLimit
   ) {
      super(gemClass);
      this.effect = effect;
      this.target = target;
      this.values = values;
      this.stackOnReapply = stackOnReapply;
      this.stackingLimit = stackingLimit;
   }

   @Override
   public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
      MobEffectInstance inst = this.values.get(gem.purity()).build(this.effect);
      MutableComponent comp = this.target.toComponent(toComponent(inst, ctx.tickRate())).withStyle(ChatFormatting.YELLOW);
      int cooldown = this.getCooldown(gem.purity());
      if (cooldown != 0) {
         Component cd = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(cooldown, ctx.tickRate())});
         comp = comp.append(" ").append(cd);
      }

      if (this.stackOnReapply) {
         comp = comp.append(" ").append(Component.translatable("affix.ascendant_equipment.stacking"));
      }

      return comp;
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   @Override
   public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
      if (this.target == MobEffectAffix.Target.HURT_SELF) {
         this.applyEffect(inst, user);
      } else if (this.target == MobEffectAffix.Target.HURT_ATTACKER && source.getEntity() instanceof LivingEntity tLiving) {
         this.applyEffect(inst, tLiving);
      }
   }

   @Override
   public void doPostAttack(GemInstance inst, LivingEntity user, Entity target) {
      if (this.target == MobEffectAffix.Target.ATTACK_SELF) {
         this.applyEffect(inst, user);
      } else if (this.target == MobEffectAffix.Target.ATTACK_TARGET && target instanceof LivingEntity tLiving) {
         this.applyEffect(inst, tLiving);
      }
   }

   @Override
   public void onBlockBreak(GemInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
      if (this.target == MobEffectAffix.Target.BREAK_SELF) {
         this.applyEffect(inst, player);
      }
   }

   @Override
   public void onProjectileImpact(GemInstance inst, Projectile proj, HitResult res) {
      if (res.getType() == Type.ENTITY && ((EntityHitResult)res).getEntity() instanceof LivingEntity target) {
         switch (this.target) {
            case ARROW_SELF:
               if (proj instanceof AbstractArrow && proj.getOwner() instanceof LivingEntity owner) {
                  this.applyEffect(inst, owner);
               }
               break;
            case ARROW_TARGET:
               if (proj instanceof AbstractArrow) {
                  this.applyEffect(inst, target);
               }
               break;
            case PROJECTILE_SELF:
               if (proj.getOwner() instanceof LivingEntity owner) {
                  this.applyEffect(inst, owner);
               }
               break;
            case PROJECTILE_TARGET:
               this.applyEffect(inst, target);
         }
      }
   }

   @Override
   public float onShieldBlock(GemInstance inst, LivingEntity entity, DamageSource source, float amount) {
      if (this.target == MobEffectAffix.Target.BLOCK_SELF) {
         this.applyEffect(inst, entity);
      } else if (this.target == MobEffectAffix.Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
         this.applyEffect(inst, target);
      }

      return amount;
   }

   protected int getCooldown(Purity purity) {
      MobEffectBonus.EffectData data = this.values.get(purity);
      return data.cooldown;
   }

   private void applyEffect(GemInstance inst, LivingEntity target) {
      int cooldown = this.getCooldown(inst.purity());
      if (cooldown == 0 || !AbilityCooldowns.isOnCooldown(target, makeUniqueId(inst), cooldown)) {
         MobEffectBonus.EffectData data = this.values.get(inst.purity());
         MobEffectInstance effectInst = target.getEffect(this.effect);
         if (!this.stackOnReapply || effectInst == null) {
            target.addEffect(data.build(this.effect));
         } else if (inst != null) {
            int duration = Math.max(effectInst.getDuration(), data.duration);
            int amp = Math.min(this.stackingLimit, effectInst.getAmplifier() + 1 + data.amplifier);
            MobEffectInstance newInst = new MobEffectInstance(this.effect, duration, amp, effectInst.isAmbient(), effectInst.isVisible());
            effectInst.update(newInst);
            ((LivingEntityInvoker)target).callOnEffectUpdated(effectInst, true, null);
            effectInst.onEffectStarted(target);
         }

         if (cooldown != 0) {
            AbilityCooldowns.startCooldown(target, makeUniqueId(inst));
         }
      }
   }

   public static Component toComponent(MobEffectInstance inst, float tickRate) {
      MutableComponent mutablecomponent = Component.translatable(inst.getDescriptionId());
      Holder<MobEffect> mobeffect = inst.getEffect();
      if (inst.getAmplifier() > 0) {
         mutablecomponent = Component.translatable(
            "potion.withAmplifier", new Object[]{mutablecomponent, Component.translatable("potion.potency." + inst.getAmplifier())}
         );
      }

      if (inst.getDuration() > 20) {
         mutablecomponent = Component.translatable("potion.withDuration", new Object[]{mutablecomponent, MobEffectUtil.formatDuration(inst, 1.0F, tickRate)});
      }

      return mutablecomponent.withStyle(((MobEffect)mobeffect.value()).getCategory().getTooltipFormatting());
   }

   public static MobEffectBonus.Builder builder() {
      return new MobEffectBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private final Map<Purity, MobEffectBonus.EffectData> values;
      private Holder<MobEffect> effect;
      private MobEffectAffix.Target target;
      private boolean stacking;
      private int limit = 255;

      public Builder() {
         this.values = new HashMap<>();
         this.stacking = false;
      }

      public MobEffectBonus.Builder effect(Holder<MobEffect> effect) {
         this.effect = effect;
         return this;
      }

      public MobEffectBonus.Builder target(MobEffectAffix.Target target) {
         this.target = target;
         return this;
      }

      public MobEffectBonus.Builder value(Purity purity, int duration, int amplifier) {
         return this.value(purity, duration, amplifier, 0);
      }

      public MobEffectBonus.Builder value(Purity purity, int duration, int amplifier, int cooldown) {
         return this.value(purity, new MobEffectBonus.EffectData(duration, amplifier, cooldown));
      }

      public MobEffectBonus.Builder value(Purity purity, MobEffectBonus.EffectData value) {
         this.values.put(purity, value);
         return this;
      }

      public MobEffectBonus.Builder stacking() {
         this.stacking = true;
         return this;
      }

      public MobEffectBonus.Builder limit(int limit) {
         this.limit = limit;
         return this;
      }

      public MobEffectBonus build(GemClass gemClass) {
         return new MobEffectBonus(gemClass, this.effect, this.target, this.values, this.stacking, this.limit);
      }
   }

   public record EffectData(int duration, int amplifier, int cooldown) {
      private static Codec<MobEffectBonus.EffectData> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.INT.fieldOf("duration").forGetter(MobEffectBonus.EffectData::duration),
               Codec.INT.fieldOf("amplifier").forGetter(MobEffectBonus.EffectData::amplifier),
               Codec.INT.optionalFieldOf("cooldown", 0).forGetter(MobEffectBonus.EffectData::cooldown)
            )
            .apply(inst, MobEffectBonus.EffectData::new)
      );

      public MobEffectInstance build(Holder<MobEffect> effect) {
         return new MobEffectInstance(effect, this.duration, this.amplifier);
      }

      public MobEffectInstance buildStacking(Holder<MobEffect> effect, int existingAmp) {
         return new MobEffectInstance(effect, this.duration, this.amplifier + existingAmp);
      }
   }
}
