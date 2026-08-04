package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.util.DamageSourceExtension;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags.DamageTypes;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;

public class MagicalArrowAffix extends Affix {
   public static final Codec<MagicalArrowAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(affixDef(), CommonToolkitCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities)).apply(inst, MagicalArrowAffix::new)
   );
   protected Set<LootRarity> rarities;

   public MagicalArrowAffix(AffixDefinition def, Set<LootRarity> rarities) {
      super(def);
      this.rarities = rarities;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat.isRanged() && this.rarities.contains(rarity);
   }

   public static void modifyIncomingDamageTags(EntityInvulnerabilityCheckEvent e) {
      if (e.getSource().getDirectEntity() instanceof AbstractArrow arrow
         && AffixHelper.streamAffixes(arrow).anyMatch(a -> a.getAffix() instanceof MagicalArrowAffix)) {
         DamageSourceExtension ext = (DamageSourceExtension)e.getSource();
         ext.addTag(DamageTypes.IS_MAGIC);
         ext.addTag(DamageTypeTags.BYPASSES_ARMOR);
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return true;
   }
}
