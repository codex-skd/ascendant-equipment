package com.skd.ascendantequipment.tiers;

import com.google.common.base.Predicates;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import org.slf4j.Logger;

public abstract class TieredDynamicRegistry<V extends TieredWeights.Weighted> extends DynamicRegistry<V> {
   public TieredDynamicRegistry(Logger logger, Identifier id, RegistrySerializer<V> serializer) {
      super(logger, id, serializer);
   }

   @Nullable
   public V getRandomItem(GenContext ctx) {
      return this.getRandomItem(ctx, Predicates.alwaysTrue());
   }

   @Nullable
   @SafeVarargs
   public final V getRandomItem(GenContext ctx, Predicate<? super V>... filters) {
      List<Weighted<V>> list = new ArrayList<>(this.registry.size());
      Stream<V> stream = this.registry.values().stream();

      for (Predicate<? super V> filter : filters) {
         stream = stream.filter(filter);
      }

      stream.mapMulti(TieredWeights.wrapFilter(ctx)).forEach(list::add);
      return WeightedRandom.getRandomItem(ctx.rand(), list, Weighted::weight).<V>map(Weighted::value).orElse(null);
   }

   public final V getRandomItem(GenContext ctx, Set<V> pool) {
      V v = this.getRandomItem(ctx, pool.isEmpty() ? s -> true : pool::contains);
      return v == null ? ApothMiscUtil.getRandomElement(pool.isEmpty() ? this.getValues() : pool, ctx.rand()) : v;
   }

   public final V getRandomItemFromHolders(GenContext ctx, Set<DynamicHolder<V>> pool) {
      return this.getRandomItem(ctx, pool.stream().filter(DynamicHolder::isBound).<V>map(DynamicHolder::get).collect(Collectors.toSet()));
   }
}
