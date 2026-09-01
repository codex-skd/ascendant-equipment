package com.skd.ascendantequipment.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack;

import com.skd.commontoolkit.codec.CodecProvider;

public record AffixLootEntry(TieredWeights weights, Constraints constraints, ItemStack stack, Set<LootRarity> rarities)
   implements CodecProvider<AffixLootEntry>, TieredWeights.Weighted, Constraints.Constrained {
   public static final Codec<AffixLootEntry> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(TieredWeights.Weighted::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constraints.Constrained::constraints),
            ItemStack.CODEC.fieldOf("stack").forGetter(AffixLootEntry::stack),
            CommonToolkitCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(AffixLootEntry::rarities)
         )
         .apply(inst, AffixLootEntry::new)
   );

   @Override
   public Codec<AffixLootEntry> getCodec() {
      return CODEC;
   }

   public AffixLootEntry(TieredWeights weights, ItemStack stack) {
      this(weights, Constraints.EMPTY, stack, Set.of());
   }

   public LootCategory getType() {
      return LootCategory.forItem(this.stack);
   }

   public ItemStack stack() {
      return this.stack.copy();
   }
}
