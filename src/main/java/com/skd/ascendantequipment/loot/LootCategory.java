package com.skd.ascendantequipment.loot;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantattributes.modifiers.EntitySlotGroup;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class LootCategory {
   public static final Codec<LootCategory> CODEC = Codec.lazyInitialized(() -> AscEq.BuiltInRegs.LOOT_CATEGORY.byNameCodec());
   public static final Codec<Set<LootCategory>> SET_CODEC = CommonToolkitCodecs.setOf(CODEC);
   public static final StreamCodec<RegistryFriendlyByteBuf, LootCategory> STREAM_CODEC = ByteBufCodecs.registry(AscEq.BuiltInRegs.LOOT_CATEGORY.key());
   private static List<LootCategory> sortedCategories = new ArrayList<>();
   private final Predicate<ItemStack> validator;
   private final EntitySlotGroup slots;
   private final int priority;
   @Nullable
   private String descId;

   public LootCategory(Predicate<ItemStack> validator, EntitySlotGroup slots, int priority) {
      this.validator = Preconditions.checkNotNull(validator);
      this.slots = Preconditions.checkNotNull(slots);
      this.priority = priority;
   }

   public LootCategory(Predicate<ItemStack> validator, EntitySlotGroup slots) {
      this(validator, slots, 1000);
   }

   public String getDescId() {
      return this.getOrCreateDescriptionId();
   }

   public String getDescIdPlural() {
      return this.getDescId() + ".plural";
   }

   public Identifier getKey() {
      return AscEq.BuiltInRegs.LOOT_CATEGORY.getKey(this);
   }

   public int priority() {
      return this.priority;
   }

   public EntitySlotGroup getSlots() {
      return this.slots;
   }

   public boolean isValid(ItemStack stack) {
      return this.validator.test(stack);
   }

   @Deprecated(forRemoval = true)
   public boolean isArmor() {
      return this == AscEq.LootCategories.HELMET
         || this == AscEq.LootCategories.CHESTPLATE
         || this == AscEq.LootCategories.LEGGINGS
         || this == AscEq.LootCategories.BOOTS;
   }

   @Deprecated(forRemoval = true)
   public boolean isBreaker() {
      return this == AscEq.LootCategories.BREAKER;
   }

   @Deprecated(forRemoval = true)
   public boolean isRanged() {
      return this == AscEq.LootCategories.BOW || this == AscEq.LootCategories.TRIDENT;
   }

   @Deprecated(forRemoval = true)
   public boolean isDefensive() {
      return this.isArmor() || this == AscEq.LootCategories.SHIELD;
   }

   @Deprecated(forRemoval = true)
   public boolean isMelee() {
      return this == AscEq.LootCategories.MELEE_WEAPON || this == AscEq.LootCategories.TRIDENT;
   }

   @Deprecated(forRemoval = true)
   public boolean isMeleeOrShield() {
      return this.isMelee() || this == AscEq.LootCategories.SHIELD;
   }

   public boolean isNone() {
      return this == AscEq.LootCategories.NONE;
   }

   @Override
   public String toString() {
      return String.format("LootCategory[%s]", this.getKey());
   }

   protected String getOrCreateDescriptionId() {
      if (this.descId == null) {
         this.descId = Util.makeDescriptionId("loot_category", this.getKey());
      }

      return this.descId;
   }

   public static <T> MapCodec<Map<LootCategory, T>> mapCodec(Codec<T> codec) {
      return Codec.simpleMap(CODEC, codec, AscEq.BuiltInRegs.LOOT_CATEGORY::keys);
   }

   public static LootCategory forItem(ItemStack stack) {
      if (sortedCategories.isEmpty()) {
         throw new UnsupportedOperationException("Attempted to resolve the loot category for an item before loot categories were registered!");
      }

      if (stack.isEmpty()) {
         return AscEq.LootCategories.NONE;
      }

      LootCategory override = BuiltInRegistries.ITEM.getData(AscEq.DataMaps.LOOT_CATEGORY_OVERRIDES, stack.getItem().builtInRegistryHolder().getKey());
      if (override != null) {
         return override;
      }

      for (LootCategory c : sortedCategories) {
         if (c.isValid(stack)) {
            return c;
         }
      }

      return AscEq.LootCategories.NONE;
   }

   @Nullable
   private static Identifier readLocWithApothNamespace(String path) {
      try {
         return path.contains(":") ? Identifier.parse(path) : AscendantEquipment.loc(path);
      } catch (IdentifierException resourcelocationexception) {
         return null;
      }
   }

   @Internal
   public static class Inner {
      public static BakeCallback<LootCategory> rebuildSortedValueList() {
         return registry -> {
            ArrayList<LootCategory> list = new ArrayList<>();

            for (LootCategory cat : registry) {
               list.add(cat);
            }

            Collections.sort(list, Comparator.comparing(LootCategory::priority));
            LootCategory.sortedCategories = list;
         };
      }
   }
}
