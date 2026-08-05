package com.skd.ascendantequipment.loot;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class RarityRegistry extends TieredDynamicRegistry<LootRarity> {
   public static final RarityRegistry INSTANCE = new RarityRegistry();
   protected BiMap<Item, DynamicHolder<LootRarity>> materialMap = HashBiMap.create();
   protected List<LootRarity> sorted = new ArrayList<>();

   private RarityRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("rarities"), RegistrySerializer.synced(LootRarity.LOAD_CODEC));
   }

   public static boolean isMaterial(Item item) {
      return getMaterialRarity(item).isBound();
   }

   public static DynamicHolder<LootRarity> getMaterialRarity(Item item) {
      return (DynamicHolder<LootRarity>)INSTANCE.materialMap.getOrDefault(item, INSTANCE.emptyHolder());
   }

   public static List<LootRarity> getSortedRarities() {
      return Collections.unmodifiableList(INSTANCE.sorted);
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.materialMap = HashBiMap.create();
      this.sorted.clear();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);

      for (LootRarity r : this.getValues()) {
         DynamicHolder<LootRarity> old = this.materialMap.put(r.getMaterial(), this.holder(r));
         if (old != null) {
            throw new RuntimeException("Two rarities may not share the same rarity material: " + this.getKey(r) + " conflicts with " + old.getId());
         }

         this.sorted.add(r);
      }

      this.sorted.sort(Comparator.comparing(LootRarity::sortIndex));
   }

   protected void validateItem(Identifier key, LootRarity item) {
      super.validateItem(key, item);
      Preconditions.checkNotNull(item.color());
      Preconditions.checkArgument(item.getMaterial() != null && item.getMaterial() != Items.AIR);
      Preconditions.checkArgument(!item.rules().isEmpty(), "A rarity must provide base rules.");
   }
}
