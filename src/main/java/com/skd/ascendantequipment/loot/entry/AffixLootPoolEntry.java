package com.skd.ascendantequipment.loot.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.AffixLootEntry;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AffixLootPoolEntry extends ContextualLootPoolEntry {
   public static final MapCodec<AffixLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(
      inst -> inst.group(
            CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
            CommonToolkitCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries)
         )
         .and(LootPoolSingletonContainer.singletonFields(inst))
         .apply(inst, AffixLootPoolEntry::new)
   );
   private final Set<DynamicHolder<LootRarity>> rarities;
   private final Set<DynamicHolder<AffixLootEntry>> entries;
   private transient boolean validated = false;

   public AffixLootPoolEntry(
      Set<DynamicHolder<LootRarity>> rarities,
      Set<DynamicHolder<AffixLootEntry>> entries,
      int weight,
      int quality,
      List<LootItemCondition> conditions,
      List<LootItemFunction> functions
   ) {
      super(weight, quality, conditions, functions);
      this.rarities = rarities;
      this.entries = entries;
   }

   @Override
   protected void createItemStack(Consumer<ItemStack> list, LootContext ctx, GenContext gCtx) {
      if (!this.validated) {
         this.rarities.forEach(AffixLootPoolEntry::checkBound);
         this.entries.forEach(AffixLootPoolEntry::checkBound);
         this.validated = true;
      }

      ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, gCtx);
      if (!stack.isEmpty()) {
         NameHelper.setItemName(ctx.getRandom(), stack);
         list.accept(stack);
      }
   }

   public MapCodec<AffixLootPoolEntry> codec() {
      return CODEC;
   }

   public static Builder<?> builder(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
      return LootPoolSingletonContainer.simpleBuilder(ctor(rarities, entries));
   }

   private static EntryConstructor ctor(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
      return (weight, quality, conditions, functions) -> new AffixLootPoolEntry(rarities, entries, weight, quality, conditions, functions);
   }

   private static void checkBound(DynamicHolder<?> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("An AffixLootPoolEntry failed to resolve {}!", holder.toString());
      }
   }
}
