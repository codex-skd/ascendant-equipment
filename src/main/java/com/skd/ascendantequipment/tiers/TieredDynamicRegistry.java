package com.skd.ascendantequipment.tiers;

import com.google.common.base.Predicates;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.codec.CodecProvider;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import org.slf4j.Logger;

public abstract class TieredDynamicRegistry<V extends CodecProvider<? super V> & TieredWeights.Weighted> extends DynamicRegistry<V> {

   public TieredDynamicRegistry(Logger logger, ResourceLocation id, RegistrySerializer<V> serializer) {
      super(logger, id, serializer);
   }

   @Nullable
   public V getRandomItem(GenContext ctx) {
      return this.getRandomItem(ctx, Predicates.alwaysTrue());
   }

   @Nullable
   @SafeVarargs
   public final V getRandomItem(GenContext ctx, Predicate<? super V>... filters) {
      List<Wrapper<V>> list = new ArrayList<>(this.registry.size());
      var stream = this.registry.values().stream();

      for (Predicate<? super V> filter : filters) {
         stream = stream.filter(filter);
      }

      stream.map(l -> l.<V>wrap(ctx.tier(), ctx.luck())).forEach(list::add);
      return WeightedRandom.getRandomItem(ctx.rand(), list).map(Wrapper::data).orElse(null);
   }

   public final V getRandomItem(GenContext ctx, Set<V> pool) {
      V v = this.getRandomItem(ctx, pool.isEmpty() ? s -> true : pool::contains);
      if (v == null) {
         return ApothMiscUtil.getRandomElement(pool.isEmpty() ? this.getValues() : pool, ctx.rand());
      }
      return v;
   }

   public final V getRandomItemFromHolders(GenContext ctx, Set<DynamicHolder<V>> pool) {
      return getRandomItem(ctx, pool.stream().filter(DynamicHolder::isBound).map(DynamicHolder::get).collect(Collectors.toSet()));
   }
}
