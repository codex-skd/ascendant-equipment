package com.skd.ascendantequipment.loot;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixType;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LootController {
   static Random jRand = new Random();

   public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, GenContext ctx) {
      LootCategory cat = LootCategory.forItem(stack);
      return cat.isNone() ? stack : createLootItem(stack, cat, rarity, ctx);
   }

   public static ItemStack createLootItem(ItemStack stack, LootCategory cat, LootRarity rarity, GenContext ctx) {
      stack.set(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      AffixHelper.setRarity(stack, rarity);

      for (LootRule rule : rarity.getRules(cat)) {
         rule.execute(stack, rarity, ctx);
      }

      ItemAffixes loaded = (ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      if (loaded.size() == 0) {
         return stack;
      }

      List<Affix> nameList = new ArrayList<>(loaded.size());
      ObjectIterator<DynamicHolder<Affix>> key = loaded.keySet().iterator();

      while (key.hasNext()) {
         DynamicHolder<Affix> a = key.next();
         nameList.add(a.get());
      }

      jRand.setSeed(ctx.rand().nextLong());
      Collections.shuffle(nameList, jRand);
      String keyx = nameList.size() > 1 ? "misc.ascendant_equipment.affix_name.three" : "misc.ascendant_equipment.affix_name.two";
      MutableComponent name = Component.translatable(
            keyx, new Object[]{nameList.get(0).getName(true), "", nameList.size() > 1 ? nameList.get(1).getName(false) : ""}
         )
         .withStyle(Style.EMPTY.withColor(rarity.color()).withItalic(false));
      AffixHelper.setName(stack, name);
      stack.remove(AscEq.Components.TOUCHED_BY_MALICE);
      return stack;
   }

   public static ItemStack createRandomLootItem(GenContext ctx, @Nullable LootRarity rarity) {
      AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(ctx);
      if (entry == null) {
         return ItemStack.EMPTY;
      }

      if (rarity == null) {
         rarity = LootRarity.random(ctx, entry.rarities());
      }

      return createLootItem(entry.stack(), entry.getType(), rarity, ctx);
   }

   public static Stream<DynamicHolder<Affix>> getAvailableAffixes(ItemStack stack, LootRarity rarity, AffixType type) {
      LootCategory cat = LootCategory.forItem(stack);
      ItemAffixes current = (ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      return AffixHelper.byType(type)
         .stream()
         .filter(a -> ((Affix)a.get()).canApplyTo(stack, cat, rarity))
         .filter(a -> ((Affix)a.get()).isCompatibleWith(current));
   }

   public static Stream<DynamicHolder<Affix>> getAlternativeAffixes(Player player, ItemStack stack, LootRarity rarity, DynamicHolder<Affix> affix) {
      ItemStack copy = stack.copy();
      ItemAffixes fixed = ((ItemAffixes)copy.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY)).toBuilder().remove(affix).build();
      copy.set(AscEq.Components.AFFIXES, fixed);
      return getAvailableAffixes(copy, rarity, ((Affix)affix.get()).definition().type()).filter(a -> !a.equals(affix)).filter(hasPositiveWeight(player));
   }

   public static List<Weighted<Affix>> getWeightedAffixes(ItemStack stack, LootRarity rarity, AffixType type, GenContext ctx) {
      return getAvailableAffixes(stack, rarity, type).mapMulti(TieredWeights.wrapFilterHolders(ctx)).toList();
   }

   public static ItemStack createAffixItemFromPools(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries, GenContext gCtx) {
      ItemStack stack;
      if (entries.isEmpty()) {
         LootRarity selectedRarity = LootRarity.randomFromHolders(gCtx, rarities);
         stack = createRandomLootItem(gCtx, selectedRarity);
      } else {
         Set<AffixLootEntry> resolved = entries.stream().filter(DynamicHolder::isBound).<AffixLootEntry>map(DynamicHolder::get).collect(Collectors.toSet());
         AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(gCtx, resolved::contains);
         if (entry == null) {
            return ItemStack.EMPTY;
         }

         LootRarity rarity;
         if (rarities.isEmpty()) {
            rarity = LootRarity.random(gCtx, entry.rarities());
         } else {
            rarity = LootRarity.randomFromHolders(gCtx, rarities);
         }

         stack = createLootItem(entry.stack(), rarity, gCtx);
      }

      return stack;
   }

   private static Predicate<DynamicHolder<Affix>> hasPositiveWeight(Player player) {
      WorldTier tier = WorldTier.getTier(player);
      float luck = player.getLuck();
      return a -> ((Affix)a.get()).weights().getWeight(tier, luck) > 0;
   }
}
