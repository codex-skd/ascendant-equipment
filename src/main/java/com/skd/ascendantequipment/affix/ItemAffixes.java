package com.skd.ascendantequipment.affix;

import com.mojang.serialization.Codec;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ItemAffixes {
   public static final Codec<ItemAffixes> CODEC = Codec.unboundedMap(AffixRegistry.INSTANCE.holderCodec(), Codec.floatRange(0.0F, 2.0F))
      .xmap(Object2FloatOpenHashMap::new, Function.identity())
      .xmap(ItemAffixes::new, i -> i.affixes);
   public static final StreamCodec<ByteBuf, ItemAffixes> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(Object2FloatOpenHashMap::new, AffixRegistry.INSTANCE.holderStreamCodec(), ByteBufCodecs.FLOAT), ia -> ia.affixes, ItemAffixes::new
   );
   public static final ItemAffixes EMPTY = new ItemAffixes(new Object2FloatOpenHashMap());
   private final Object2FloatOpenHashMap<DynamicHolder<Affix>> affixes;

   private ItemAffixes(Object2FloatOpenHashMap<DynamicHolder<Affix>> affixes) {
      this.affixes = affixes;
      ObjectIterator var2 = affixes.object2FloatEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<DynamicHolder<Affix>> entry = (Entry<DynamicHolder<Affix>>)var2.next();
         float level = entry.getFloatValue();
         if (level < 0.0F || level > 2.0F) {
            throw new IllegalArgumentException("Affix " + entry.getKey() + " has invalid level " + level);
         }
      }
   }

   public float getLevel(DynamicHolder<Affix> key) {
      return this.affixes.getFloat(key);
   }

   public boolean isEmpty() {
      return this.affixes.isEmpty();
   }

   public int size() {
      return this.affixes.size();
   }

   public ObjectSet<Entry<DynamicHolder<Affix>>> entrySet() {
      return ObjectSets.unmodifiable(this.affixes.object2FloatEntrySet());
   }

   public ObjectSet<DynamicHolder<Affix>> keySet() {
      return ObjectSets.unmodifiable(this.affixes.keySet());
   }

   public Stream<Affix> liveAffixes() {
      return this.keySet().stream().filter(DynamicHolder::isBound).map(DynamicHolder::get);
   }

   @Override
   public boolean equals(Object other) {
      return this == other || other instanceof ItemAffixes iafxs && iafxs.affixes.equals(this.affixes);
   }

   @Override
   public int hashCode() {
      return this.affixes.hashCode();
   }

   @Override
   public String toString() {
      return "ItemAffixes{affixes=" + this.affixes + "}";
   }

   public ItemAffixes.Builder toBuilder() {
      return new ItemAffixes.Builder(this);
   }

   public static class Builder {
      Object2FloatOpenHashMap<DynamicHolder<Affix>> affixes = new Object2FloatOpenHashMap();

      public Builder(ItemAffixes base) {
         this.affixes.putAll(base.affixes);
      }

      public ItemAffixes.Builder put(DynamicHolder<Affix> affix, float level) {
         if (level <= 0.0F) {
            this.affixes.removeFloat(affix);
         } else {
            this.affixes.put(affix, Math.clamp(level, 0.0F, 2.0F));
         }

         return this;
      }

      public ItemAffixes.Builder upgrade(DynamicHolder<Affix> affix, float level) {
         if (level > 0.0F) {
            this.affixes.merge(affix, Math.clamp(level, 0.0F, 2.0F), Float::max);
         }

         return this;
      }

      public ItemAffixes.Builder remove(DynamicHolder<Affix> affix) {
         this.affixes.removeFloat(affix);
         return this;
      }

      public ItemAffixes.Builder removeIf(Predicate<DynamicHolder<Affix>> filter) {
         this.affixes.keySet().removeIf(filter);
         return this;
      }

      public float getLevel(DynamicHolder<Affix> key) {
         return this.affixes.getFloat(key);
      }

      public boolean isEmpty() {
         return this.affixes.isEmpty();
      }

      public int size() {
         return this.affixes.size();
      }

      public ObjectSet<Entry<DynamicHolder<Affix>>> entrySet() {
         return ObjectSets.unmodifiable(this.affixes.object2FloatEntrySet());
      }

      public ObjectSet<DynamicHolder<Affix>> keySet() {
         return ObjectSets.unmodifiable(this.affixes.keySet());
      }

      public ItemAffixes build() {
         return new ItemAffixes(this.affixes);
      }
   }
}
