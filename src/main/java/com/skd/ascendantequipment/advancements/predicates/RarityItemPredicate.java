package com.skd.ascendantequipment.advancements.predicates;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record RarityItemPredicate(Set<DynamicHolder<LootRarity>> rarities)
   implements SingleComponentItemPredicate<DynamicHolder<LootRarity>>,
   TypeAwareISP<RarityItemPredicate> {
   public static final Codec<RarityItemPredicate> CODEC = CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec())
      .fieldOf("rarities")
      .xmap(RarityItemPredicate::new, RarityItemPredicate::rarities)
      .codec();

   public DataComponentType<DynamicHolder<LootRarity>> componentType() {
      return AscEq.Components.RARITY;
   }

   public boolean matches(ItemStack stack, DynamicHolder<LootRarity> rarity) {
      return rarity.isBound() && this.rarities.contains(rarity);
   }

   @Override
   public ItemSubPredicate.Type<RarityItemPredicate> type() {
      return AscEq.DataComponentPredicates.ITEM_WITH_RARITY;
   }
}
