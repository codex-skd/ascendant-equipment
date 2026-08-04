package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class TelepathicAffix extends Affix {
   public static final Codec<TelepathicAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), CommonToolkitCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities)).apply(inst, TelepathicAffix::new)
   );
   public static Vec3 blockDropTargetPos = null;
   protected Set<LootRarity> rarities;

   public TelepathicAffix(AffixDefinition def, Set<LootRarity> rarities) {
      super(def);
      this.rarities = rarities;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return (cat.isRanged() || cat.isMelee() || cat.isBreaker()) && this.rarities.contains(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      LootCategory cat = LootCategory.forItem(inst.stack());
      String type = !cat.isRanged() && !cat.isMelee() ? "tool" : "weapon";
      return Component.translatable("affix." + this.id() + ".desc." + type);
   }

   @Override
   public boolean enablesTelepathy() {
      return true;
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return true;
   }

   public static void drops(LivingDropsEvent e) {
      DamageSource src = e.getSource();
      boolean canTeleport = false;
      Vec3 targetPos = null;
      if (src.getDirectEntity() instanceof AbstractArrow arrow && arrow.getOwner() != null) {
         canTeleport = AffixHelper.streamAffixes(arrow).anyMatch(AffixInstance::enablesTelepathy);
         targetPos = arrow.getOwner().position();
      } else if (src.getDirectEntity() instanceof LivingEntity living) {
         ItemStack weapon = living.getMainHandItem();
         canTeleport = AffixHelper.streamAffixes(weapon).anyMatch(AffixInstance::enablesTelepathy);
         targetPos = living.position();
      }

      if (canTeleport && !targetPos.equals(Vec3.ZERO)) {
         for (ItemEntity item : e.getDrops()) {
            item.setPos(targetPos.x, targetPos.y, targetPos.z);
            item.setPickUpDelay(0);
         }
      }
   }

   public static void drops(BlockDropsEvent e) {
      if (e.getBreaker() instanceof LivingEntity living && !living.position().equals(Vec3.ZERO)) {
         ItemStack tool = living.getMainHandItem();
         if (AffixHelper.streamAffixes(tool).anyMatch(AffixInstance::enablesTelepathy)) {
            Vec3 targetPos = living.position();

            for (ItemEntity item : e.getDrops()) {
               item.setPos(targetPos.x, targetPos.y, targetPos.z);
               item.setPickUpDelay(0);
            }
         }
      }
   }
}
