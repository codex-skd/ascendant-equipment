package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class RetreatingAffix extends Affix {
   public static final Codec<RetreatingAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), CommonToolkitCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities)).apply(inst, RetreatingAffix::new)
   );
   protected Set<LootRarity> rarities;

   public RetreatingAffix(AffixDefinition def, Set<LootRarity> rarities) {
      super(def);
      this.rarities = rarities;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat == AscEq.LootCategories.SHIELD && this.rarities.contains(rarity);
   }

   @Override
   public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
      Entity tSource = source.getEntity();
      if (tSource != null && tSource.distanceToSqr(entity) <= 9.0) {
         Vec3 look = entity.getLookAngle();
         entity.setDeltaMovement(new Vec3(1.0 * -look.x, 0.25, 1.0 * -look.z));
         entity.hurtMarked = true;
         entity.setOnGround(false);
      }

      return super.onShieldBlock(inst, entity, source, amount);
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return true;
   }
}
