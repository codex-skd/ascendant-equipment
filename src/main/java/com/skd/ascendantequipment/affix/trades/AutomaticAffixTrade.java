package com.skd.ascendantequipment.affix.trades;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.AffixLootEntry;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

public class AutomaticAffixTrade extends LootItemConditionalFunction {
   public static final MapCodec<AutomaticAffixTrade> CODEC = RecordCodecBuilder.mapCodec(
      inst -> commonFields(inst)
         .and(
            inst.group(
               CommonToolkitCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
               AffixLootRegistry.INSTANCE.holderCodec().listOf().optionalFieldOf("entries", List.of()).forGetter(a -> a.entries)
            )
         )
         .apply(inst, AutomaticAffixTrade::new)
   );
   protected final Set<LootRarity> rarities;
   protected final List<DynamicHolder<AffixLootEntry>> entries;

   public AutomaticAffixTrade(List<LootItemCondition> predicates, Set<LootRarity> rarities, List<DynamicHolder<AffixLootEntry>> entries) {
      super(predicates);
      this.rarities = rarities;
      this.entries = entries;
   }

   public MapCodec<AutomaticAffixTrade> codec() {
      return CODEC;
   }

   protected ItemStack run(ItemStack stack, LootContext ctx) {
      Entity trader = (Entity)ctx.getOptionalParameter(LootContextParams.THIS_ENTITY);
      if (trader != null && !trader.level().isClientSide()) {
         Player player = trader.level().getNearestPlayer(trader, -1.0);
         if (player == null) {
            return stack;
         }

         GenContext gCtx = GenContext.forPlayer(ctx.getRandom(), player);
         ItemStack affixItem;
         if (this.entries.isEmpty()) {
            LootRarity rarity = LootRarity.random(gCtx, this.rarities);
            affixItem = LootController.createRandomLootItem(gCtx, rarity);
         } else {
            List<Weighted<AffixLootEntry>> resolved = this.entries
               .stream()
               .map(this::unwrap)
               .filter(Objects::nonNull)
               .mapMulti(TieredWeights.wrapFilter(gCtx))
               .toList();
            if (resolved.isEmpty()) {
               return ItemStack.EMPTY;
            }

            Optional<Weighted<AffixLootEntry>> picked = WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight);
            if (picked.isEmpty()) {
               return ItemStack.EMPTY;
            }

            AffixLootEntry entry = (AffixLootEntry)picked.get().value();
            LootRarity rarity = LootRarity.random(gCtx, this.rarities.isEmpty() ? entry.rarities() : this.rarities);
            affixItem = LootController.createLootItem(entry.stackTemplate().create(), rarity, gCtx);
         }

         if (affixItem.isEmpty()) {
            return ItemStack.EMPTY;
         }

         NameHelper.setItemName(ctx.getRandom(), affixItem);
         affixItem.set(AscEq.Components.FROM_TRADER, true);
         int scaledCost = gCtx.tier().ordinal() * 7;
         if (scaledCost > 0) {
            affixItem.set(DataComponents.ADDITIONAL_TRADE_COST, scaledCost);
         }

         return affixItem;
      } else {
         return stack;
      }
   }

   @Nullable
   private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("An AutomaticAffixTrade failed to resolve the Affix Loot Entry {}!", holder.getId());
         return null;
      } else {
         return (AffixLootEntry)holder.get();
      }
   }
}
