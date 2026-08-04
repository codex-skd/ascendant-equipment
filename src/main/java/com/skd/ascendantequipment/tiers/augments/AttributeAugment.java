package com.skd.ascendantequipment.tiers.augments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public record AttributeAugment(WorldTier tier, TierAugment.Target target, int sortIndex, RandomAttributeModifier modifier) implements TierAugment {
   public static final Codec<AttributeAugment> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TierAugment::tier),
            TierAugment.Target.CODEC.fieldOf("target").forGetter(TierAugment::target),
            Codec.intRange(0, 2000).optionalFieldOf("sort_index", 1000).forGetter(TierAugment::sortIndex),
            RandomAttributeModifier.CODEC.fieldOf("modifier").forGetter(AttributeAugment::modifier)
         )
         .apply(inst, AttributeAugment::new)
   );

   public Codec<? extends AttributeAugment> getCodec() {
      return CODEC;
   }

   @Override
   public void apply(ServerLevelAccessor level, LivingEntity entity) {
      AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
      if (inst != null) {
         AttributeModifier modif = this.modifier.createDeterministic();
         inst.addOrReplacePermanentModifier(modif);
      }
   }

   @Override
   public void remove(ServerLevelAccessor level, LivingEntity entity) {
      AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
      if (inst != null) {
         inst.removeModifier(this.modifier.modifierId());
      }
   }

   @Override
   public Component getDescription(AttributeTooltipContext ctx) {
      AttributeModifier modif = this.modifier.createDeterministic();
      return ((Attribute)this.modifier.attribute().value()).toComponent(modif, ctx.flag());
   }
}
