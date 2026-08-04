package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.mixin.LivingEntityInvoker;
import com.skd.ascendantattributes.api.AbilityCooldowns;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.util.StepFunction;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class MobEffectAffix extends Affix {
   public static final Codec<MobEffectAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("mob_effect").forGetter(a -> a.effect),
            MobEffectAffix.Target.CODEC.fieldOf("target").forGetter(a -> a.target),
            LootRarity.mapCodec(MobEffectAffix.EffectData.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
            Codec.BOOL.optionalFieldOf("stack_on_reapply", false).forGetter(a -> a.stackOnReapply),
            Codec.intRange(1, 255).optionalFieldOf("stacking_limit", 255).forGetter(a -> a.stackingLimit)
         )
         .apply(inst, MobEffectAffix::new)
   );
   protected final Holder<MobEffect> effect;
   protected final MobEffectAffix.Target target;
   protected final Map<LootRarity, MobEffectAffix.EffectData> values;
   protected final Set<LootCategory> types;
   protected final boolean stackOnReapply;
   protected final int stackingLimit;

   public MobEffectAffix(
      AffixDefinition def,
      Holder<MobEffect> effect,
      MobEffectAffix.Target target,
      Map<LootRarity, MobEffectAffix.EffectData> values,
      Set<LootCategory> types,
      boolean stackOnReapply,
      int stackingLimit
   ) {
      super(def);
      this.effect = effect;
      this.target = target;
      this.values = values;
      this.types = types;
      this.stackOnReapply = stackOnReapply;
      this.stackingLimit = stackingLimit;
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      MobEffectInstance effectInst = this.values.get(inst.getRarity()).build(this.effect, inst.level());
      MutableComponent comp = this.target.toComponent(toComponent(effectInst, ctx.tickRate()));
      int cooldown = this.getCooldown(inst.getRarity());
      if (cooldown != 0) {
         Component cd = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(cooldown, ctx.tickRate())});
         comp = comp.append(" ").append(cd);
      }

      if (this.stackOnReapply) {
         comp = comp.append(" ").append(Component.translatable("affix.ascendant_equipment.stacking"));
      }

      return comp;
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      LootRarity rarity = inst.getRarity();
      MobEffectInstance effectInst = this.values.get(rarity).build(this.effect, inst.level());
      MutableComponent comp = this.target.toComponent(toComponent(effectInst, ctx.tickRate()));
      MobEffectInstance min = this.values.get(rarity).build(this.effect, 0.0F);
      MobEffectInstance max = this.values.get(rarity).build(this.effect, 1.0F);
      if (min.getAmplifier() != max.getAmplifier()) {
         Component minComp = min.getAmplifier() == 0 ? Component.literal("I") : Component.translatable("potion.potency." + min.getAmplifier());
         Component maxComp = Component.translatable("potion.potency." + max.getAmplifier());
         comp.append(valueBounds(minComp, maxComp));
      }

      if (!((MobEffect)this.effect.value()).isInstantaneous() && min.getDuration() != max.getDuration()) {
         Component minComp = MobEffectUtil.formatDuration(min, 1.0F, ctx.tickRate());
         Component maxComp = MobEffectUtil.formatDuration(max, 1.0F, ctx.tickRate());
         comp.append(valueBounds(minComp, maxComp));
      }

      int cooldown = this.getCooldown(rarity);
      if (cooldown != 0) {
         Component cd = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(cooldown, ctx.tickRate())});
         comp = comp.append(" ").append(cd);
      }

      if (this.stackOnReapply) {
         comp = comp.append(" ").append(Component.translatable("affix.ascendant_equipment.stacking"));
      }

      return comp;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return (this.types.isEmpty() || this.types.contains(cat)) && this.values.containsKey(rarity);
   }

   @Override
   public void doPostHurt(AffixInstance inst, LivingEntity user, DamageSource source) {
      if (this.target == MobEffectAffix.Target.HURT_SELF) {
         this.applyEffect(user, inst.getRarity(), inst.level());
      } else if (this.target == MobEffectAffix.Target.HURT_ATTACKER && source.getEntity() instanceof LivingEntity tLiving) {
         this.applyEffect(tLiving, inst.getRarity(), inst.level());
      }
   }

   @Override
   public void doPostAttack(AffixInstance inst, LivingEntity user, Entity target) {
      if (this.target == MobEffectAffix.Target.ATTACK_SELF) {
         this.applyEffect(user, inst.getRarity(), inst.level());
      } else if (this.target == MobEffectAffix.Target.ATTACK_TARGET && target instanceof LivingEntity tLiving) {
         this.applyEffect(tLiving, inst.getRarity(), inst.level());
      }
   }

   @Override
   public void onBlockBreak(AffixInstance inst, Player player, LevelAccessor world, BlockPos pos, BlockState state) {
      if (this.target == MobEffectAffix.Target.BREAK_SELF) {
         this.applyEffect(player, inst.getRarity(), inst.level());
      }
   }

   @Override
   public void onProjectileImpact(float level, LootRarity rarity, Projectile proj, HitResult res, Type type) {
      if (type == Type.ENTITY && ((EntityHitResult)res).getEntity() instanceof LivingEntity target) {
         switch (this.target) {
            case ARROW_SELF:
               if (proj instanceof AbstractArrow && proj.getOwner() instanceof LivingEntity owner) {
                  this.applyEffect(owner, rarity, level);
               }
               break;
            case ARROW_TARGET:
               if (proj instanceof AbstractArrow) {
                  this.applyEffect(target, rarity, level);
               }
            case BLOCK_SELF:
            case BLOCK_ATTACKER:
            default:
               break;
            case PROJECTILE_SELF:
               if (proj.getOwner() instanceof LivingEntity owner) {
                  this.applyEffect(owner, rarity, level);
               }
               break;
            case PROJECTILE_TARGET:
               this.applyEffect(target, rarity, level);
         }
      }
   }

   @Override
   public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
      if (this.target == MobEffectAffix.Target.BLOCK_SELF) {
         this.applyEffect(entity, inst.getRarity(), inst.level());
      } else if (this.target == MobEffectAffix.Target.BLOCK_ATTACKER && source.getDirectEntity() instanceof LivingEntity target) {
         this.applyEffect(target, inst.getRarity(), inst.level());
      }

      return amount;
   }

   protected int getCooldown(LootRarity rarity) {
      MobEffectAffix.EffectData data = this.values.get(rarity);
      return data.cooldown;
   }

   private void applyEffect(LivingEntity target, LootRarity rarity, float level) {
      if (!target.level().isClientSide()) {
         int cooldown = this.getCooldown(rarity);
         if (cooldown == 0 || !AbilityCooldowns.isOnCooldown(target, this.id(), cooldown)) {
            MobEffectAffix.EffectData data = this.values.get(rarity);
            MobEffectInstance inst = target.getEffect(this.effect);
            if (!this.stackOnReapply || inst == null) {
               target.addEffect(data.build(this.effect, level));
            } else if (inst != null) {
               int duration = Math.max(inst.getDuration(), data.duration.getInt(level));
               int amp = Math.min(this.stackingLimit, inst.getAmplifier() + 1 + data.amplifier.getInt(level));
               MobEffectInstance newInst = new MobEffectInstance(this.effect, duration, amp, inst.isAmbient(), inst.isVisible());
               inst.update(newInst);
               ((LivingEntityInvoker)target).callOnEffectUpdated(inst, true, null);
               inst.onEffectStarted(target);
            }

            if (cooldown != 0) {
               AbilityCooldowns.startCooldown(target, this.id());
            }
         }
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
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

   public static class Builder extends AffixBuilder<MobEffectAffix.Builder> {
      protected final Holder<MobEffect> effect;
      protected final MobEffectAffix.Target target;
      protected final Map<LootRarity, MobEffectAffix.EffectData> values = new LinkedHashMap<>();
      protected final Set<LootCategory> categories = new LinkedHashSet<>();
      protected boolean stacking = false;
      private int limit = 255;

      public Builder(Holder<MobEffect> effect, MobEffectAffix.Target target) {
         this.effect = effect;
         this.target = target;
      }

      public MobEffectAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public MobEffectAffix.Builder value(LootRarity rarity, int duration, int amplifier, int cooldown) {
         return this.value(rarity, StepFunction.constant(duration), StepFunction.constant(amplifier), cooldown);
      }

      public MobEffectAffix.Builder value(LootRarity rarity, int minDuration, int maxDuration, int amplifier, int cooldown) {
         return this.value(rarity, minDuration, maxDuration, StepFunction.constant(amplifier), cooldown);
      }

      public MobEffectAffix.Builder value(LootRarity rarity, int minDuration, int maxDuration, StepFunction amplifier, int cooldown) {
         return this.value(rarity, StepFunction.fromBounds(minDuration, maxDuration, 20.0F), amplifier, cooldown);
      }

      public MobEffectAffix.Builder value(LootRarity rarity, StepFunction duration, StepFunction amplifier, int cooldown) {
         return this.value(rarity, new MobEffectAffix.EffectData(duration, amplifier, cooldown));
      }

      public MobEffectAffix.Builder value(LootRarity rarity, MobEffectAffix.EffectData value) {
         this.values.put(rarity, value);
         return this;
      }

      public MobEffectAffix.Builder stacking() {
         this.stacking = true;
         return this;
      }

      public MobEffectAffix.Builder limit(int limit) {
         this.limit = limit;
         return this;
      }

      public MobEffectAffix build() {
         Preconditions.checkArgument(!this.values.isEmpty());
         return new MobEffectAffix(this.definition, this.effect, this.target, this.values, this.categories, this.stacking, this.limit);
      }
   }

   public record EffectData(StepFunction duration, StepFunction amplifier, int cooldown) {
      private static Codec<MobEffectAffix.EffectData> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               StepFunction.CODEC.fieldOf("duration").forGetter(MobEffectAffix.EffectData::duration),
               StepFunction.CODEC.fieldOf("amplifier").forGetter(MobEffectAffix.EffectData::amplifier),
               Codec.INT.optionalFieldOf("cooldown", 0).forGetter(MobEffectAffix.EffectData::cooldown)
            )
            .apply(inst, MobEffectAffix.EffectData::new)
      );

      public MobEffectInstance build(Holder<MobEffect> effect, float level) {
         return new MobEffectInstance(effect, this.duration.getInt(level), this.amplifier.getInt(level));
      }

      public boolean isConstant() {
         return this.duration.isConstant() && this.amplifier.isConstant();
      }
   }

   public enum Target {
      ATTACK_SELF("attack_self"),
      ATTACK_TARGET("attack_target"),
      HURT_SELF("hurt_self"),
      HURT_ATTACKER("hurt_attacker"),
      BREAK_SELF("break_self"),
      ARROW_SELF("arrow_self"),
      ARROW_TARGET("arrow_target"),
      BLOCK_SELF("block_self"),
      BLOCK_ATTACKER("block_attacker"),
      PROJECTILE_SELF("projectile_self"),
      PROJECTILE_TARGET("projectile_target");

      public static final Codec<MobEffectAffix.Target> CODEC = CommonToolkitCodecs.enumCodec(MobEffectAffix.Target.class);
      private final String id;

      Target(String id) {
         this.id = id;
      }

      public MutableComponent toComponent(Object... args) {
         return Component.translatable("affix.ascendant_equipment.target." + this.id, args);
      }
   }
}
