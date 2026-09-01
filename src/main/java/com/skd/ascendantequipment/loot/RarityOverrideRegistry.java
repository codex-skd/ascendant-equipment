package com.skd.ascendantequipment.loot;

import com.google.common.base.Preconditions;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RarityOverrideRegistry extends DynamicRegistry<RarityOverride> {
   public static final RarityOverrideRegistry INSTANCE = new RarityOverrideRegistry();
   protected Map<LootCategory, RarityOverride> byCategory = new HashMap<>();

   public RarityOverrideRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("rarity_override"), RegistrySerializer.synced(RarityOverride.CODEC));
   }

   @Nullable
   public RarityOverride getOverride(LootCategory category) {
      return this.byCategory.get(category);
   }

   protected void validateItem(ResourceLocation key, RarityOverride value) {
      String path = key.getPath().replace('/', ':');
      ResourceLocation cat = ResourceLocation.tryParse(path);
      Preconditions.checkNotNull(cat, "Invalid category path: " + path);
      LootCategory category = AscEq.BuiltInRegs.LOOT_CATEGORY.get(cat);
      Preconditions.checkArgument(category != null && !category.isNone(), "Category not found: " + cat);
      Preconditions.checkArgument(value.category() == category, "Category mismatch: " + value.category() + " != " + category);
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.byCategory = new HashMap<>();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);
      this.registry.forEach((key, value) -> {
         String path = key.getPath().replace('/', ':');
         ResourceLocation cat = ResourceLocation.tryParse(path);
         LootCategory category = AscEq.BuiltInRegs.LOOT_CATEGORY.get(cat);
         RarityOverride old = this.byCategory.put(category, value);
         if (old != null) {
            this.logger.warn("Duplicate rarity override for category {}: Old: {}, New: {}", new Object[]{path, this.getKey(old), key});
         }
      });
   }
}
