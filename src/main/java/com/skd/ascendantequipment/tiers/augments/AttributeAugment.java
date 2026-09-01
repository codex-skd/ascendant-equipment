package com.skd.ascendantequipment.tiers.augments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public record AttributeAugment(WorldTier tier, TierAugment.Target target, int sortIndex, RandomAttributeModifier modifier, ResourceLocation id) implements TierAugment {
   public static final Codec<AttributeAugment> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            WorldTier.CODEC.fieldOf("tier").forGetter(TierAugment::tier),
            TierAugment.Target.CODEC.fieldOf("target").forGetter(TierAugment::target),
            Codec.intRange(0, 2000).optionalFieldOf("sort_index", 1000).forGetter(TierAugment::sortIndex),
            RandomAttributeModifier.CONSTANT_CODEC.fieldOf("modifier").forGetter(AttributeAugment::modifier),
            ResourceLocation.CODEC.fieldOf("modifier_id").forGetter(AttributeAugment::id)
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
          AttributeModifier modif = this.modifier.createDeterministic(this.id);
          inst.addOrReplacePermanentModifier(modif);
       }
    }

    @Override
    public void remove(ServerLevelAccessor level, LivingEntity entity) {
       AttributeInstance inst = entity.getAttribute(this.modifier.attribute());
       if (inst != null) {
          inst.removeModifier(this.id);
       }
    }

    @Override
    public Component getDescription(AttributeTooltipContext ctx) {
       AttributeModifier modif = this.modifier.createDeterministic(this.id);
       return modifier.attribute().value().toComponent(modif, ctx.flag());
    }
}
