package com.skd.ascendantequipment.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.AffixLootEntry;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.util.LootPatternMatcher;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.Nullable;

public class AffixLootModifier extends ContextualLootModifier {
   public static final MapCodec<AffixLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst)
         .and(AffixLootModifier.AffixTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
         .apply(inst, AffixLootModifier::new)
   );
   protected final List<AffixLootModifier.AffixTableEntry> entries;

   public AffixLootModifier(LootItemCondition[] conditions, List<AffixLootModifier.AffixTableEntry> entries) {
      super(conditions);
      this.entries = entries;
   }

   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
      for (AffixLootModifier.AffixTableEntry entry : this.entries) {
         if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
            if (ctx.getRandom().nextFloat() <= entry.chance()) {
               AffixLootEntry lootEntry;
               if (!entry.entries.isEmpty()) {
                  List<WeightedEntry.Wrapper<AffixLootEntry>> resolved = entry.entries.stream().mapMulti(TieredWeights.wrapFilterHolders(gCtx)).toList();
                  lootEntry = WeightedRandom.getRandomItem(ctx.getRandom(), resolved).get().data();
               } else {
                  lootEntry = AffixLootRegistry.INSTANCE.getRandomItem(gCtx);
               }

               LootRarity rarity;
               if (!entry.rarities.isEmpty()) {
                  rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
               } else {
                  rarity = LootRarity.random(gCtx, lootEntry.rarities());
               }

               ItemStack affixItem = LootController.createLootItem(lootEntry.stack(), rarity, gCtx);
               if (!affixItem.isEmpty()) {
                  NameHelper.setItemName(ctx.getRandom(), affixItem);
                  affixItem.set(AscEq.Components.FROM_CHEST, true);
                  generatedLoot.add(affixItem);
               }
            }
            break;
         }
      }

      return generatedLoot;
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   @Nullable
   private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("An AffixLootModifier failed to resolve the AffixLootEntry {}!", holder.getId());
         return null;
      } else {
         return holder.get();
      }
   }

   public record AffixTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<AffixLootEntry>> entries, Set<DynamicHolder<LootRarity>> rarities) {
      public static final Codec<AffixLootModifier.AffixTableEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixLootModifier.AffixTableEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(AffixLootModifier.AffixTableEntry::chance),
               CommonToolkitCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("entries", Set.of())
                  .forGetter(AffixLootModifier.AffixTableEntry::entries),
               CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("rarities", Set.of())
                  .forGetter(AffixLootModifier.AffixTableEntry::rarities)
            )
            .apply(inst, AffixLootModifier.AffixTableEntry::new)
      );
   }
}
