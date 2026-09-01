package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.util.StepFunction;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class EnlightenedAffix extends Affix {
   public static final Codec<EnlightenedAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, EnlightenedAffix::new)
   );
   protected final Map<LootRarity, StepFunction> values;

   public EnlightenedAffix(AffixDefinition def, Map<LootRarity, StepFunction> values) {
      super(def);
      this.values = values;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat.isBreaker() && this.values.containsKey(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{this.getTrueLevel(inst.getRarity(), inst.level())});
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      Component minComp = Component.literal(fmt(this.getTrueLevel(inst.getRarity(), 0.0F)));
      Component maxComp = Component.literal(fmt(this.getTrueLevel(inst.getRarity(), 1.0F)));
      return comp.append(valueBounds(minComp, maxComp));
   }

   @Override
   public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
      Player player = ctx.getPlayer();
      if (EquipmentConfig.torchItem.useOn(ctx).consumesAction()) {
         if (ctx.getItemInHand().isEmpty()) {
            ctx.getItemInHand().grow(1);
         }

         int cost = this.getTrueLevel(inst.getRarity(), inst.level());
            player.getItemInHand(ctx.getHand()).hurtAndBreak(cost, player, LivingEntity.getSlotForHand(ctx.getHand()));
         return InteractionResult.SUCCESS;
      } else {
         return super.onItemUse(inst, ctx);
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }

   protected int getTrueLevel(LootRarity rarity, float level) {
      return this.values.get(rarity).getInt(level);
   }
}
