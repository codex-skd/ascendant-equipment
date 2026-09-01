package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantattributes.api.AbilityCooldowns;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class BloodyArrowBonus extends GemBonus {
   public static Codec<BloodyArrowBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(Purity.mapCodec(BloodyArrowBonus.Data.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, BloodyArrowBonus::new)
   );
    protected final Map<Purity, BloodyArrowBonus.Data> values;

    public static final ResourceKey<DamageType> CORRUPTED = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ascendant_enchanting", "corrupted"));

   public BloodyArrowBonus(Map<Purity, BloodyArrowBonus.Data> values) {
      super(new GemClass(AscEq.LootCategories.BOW));
      this.values = values;
   }

    @Override
    public void onProjectileFired(GemInstance inst, LivingEntity user, Projectile proj) {
       if (proj instanceof AbstractArrow arrow) {
          BloodyArrowBonus.Data d = this.values.get(inst.purity());
          if (AbilityCooldowns.isOnCooldown(user, makeUniqueId(inst), d.cooldown)) {
             return;
          }

          DamageSource src = user.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
             .getHolder(CORRUPTED).map(DamageSource::new)
             .orElseGet(() -> user.damageSources().magic());
          user.hurt(src, user.getMaxHealth() * d.healthCost);
          arrow.setBaseDamage(arrow.getBaseDamage() * d.dmgMultiplier);
          AbilityCooldowns.startCooldown(user, makeUniqueId(inst));
       }
    }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      BloodyArrowBonus.Data d = this.values.get(inst.purity());
      Component cooldown = Component.translatable("affix.ascendant_equipment.cooldown", new Object[]{StringUtil.formatTickDuration(d.cooldown, ctx.tickRate())});
      return Component.translatable(
            "bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(d.healthCost * 100.0F), Affix.fmt(100.0F * d.dmgMultiplier), cooldown}
         )
         .withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public record Data(float healthCost, float dmgMultiplier, int cooldown) {
      public static final Codec<BloodyArrowBonus.Data> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.FLOAT.fieldOf("health_cost").forGetter(BloodyArrowBonus.Data::healthCost),
               Codec.FLOAT.fieldOf("damage_mult").forGetter(BloodyArrowBonus.Data::dmgMultiplier),
               Codec.INT.fieldOf("cooldown").forGetter(BloodyArrowBonus.Data::cooldown)
            )
            .apply(inst, BloodyArrowBonus.Data::new)
      );
   }
}
