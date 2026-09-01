package com.skd.ascendantequipment.tiers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;

public record TieredWeights(Map<WorldTier, TieredWeights.Weight> weights) {
   public static final MapCodec<TieredWeights> CODEC = Codec.mapEither(
         TieredWeights.Weight.CODEC, Codec.simpleMap(WorldTier.CODEC, TieredWeights.Weight.CODEC.codec(), StringRepresentable.keys(WorldTier.values()))
      )
      .xmap(TieredWeights::fromEither, TieredWeights::toEither)
      .xmap(TieredWeights::new, TieredWeights::weights);
   private static final StreamCodec<ByteBuf, Map<WorldTier, TieredWeights.Weight>> MAP_STREAM_CODEC = ByteBufCodecs.map(
      IdentityHashMap::new, WorldTier.STREAM_CODEC, TieredWeights.Weight.STREAM_CODEC, 5
   );
   public static final StreamCodec<ByteBuf, TieredWeights> STREAM_CODEC = MAP_STREAM_CODEC.map(TieredWeights::new, TieredWeights::weights);
   public static TieredWeights EMPTY = new TieredWeights(Map.of());

   public int getWeight(GenContext ctx) {
      return this.getWeight(ctx.tier(), ctx.luck());
   }

   public int getWeight(WorldTier tier, float luck) {
      return this.weights.getOrDefault(tier, TieredWeights.Weight.ZERO).getWeight(luck);
   }

   private static Map<WorldTier, TieredWeights.Weight> fillAll(TieredWeights.Weight weight) {
      return Arrays.stream(WorldTier.values()).collect(Collectors.toMap(Function.identity(), v -> weight));
   }

   private static Map<WorldTier, TieredWeights.Weight> fromEither(Either<TieredWeights.Weight, Map<WorldTier, TieredWeights.Weight>> either) {
      return either.map(TieredWeights::fillAll, Function.identity());
   }

   public static TieredWeights.Builder builder() {
      return new TieredWeights.Builder();
   }

   public static TieredWeights onlyFor(WorldTier tier, int weight, float quality) {
      return new TieredWeights(Map.of(tier, new TieredWeights.Weight(weight, quality)));
   }

   public static TieredWeights forTiersAbove(WorldTier tier, int weight, float quality) {
      Map<WorldTier, TieredWeights.Weight> map = new HashMap<>();
      TieredWeights.Weight _weight = new TieredWeights.Weight(weight, quality);

      for (int i = tier.ordinal(); i < WorldTier.values().length; i++) {
         map.put(WorldTier.BY_ID.apply(i), _weight);
      }

      return new TieredWeights(map);
   }

   public static TieredWeights forAllTiers(int weight, float quality) {
       return new TieredWeights(fillAll(new TieredWeights.Weight(weight, quality)));
    }

    public static <T extends TieredWeights.Weighted> BiConsumer<T, Consumer<Wrapper<T>>> wrapFilter(GenContext ctx) {
       return (obj, sink) -> {
          int weight = obj.weights().getWeight(ctx);
          if (weight > 0) {
             sink.accept(WeightedEntry.wrap((T) obj, weight));
          }
       };
    }

   public static <T extends TieredWeights.Weighted> BiConsumer<DynamicHolder<T>, Consumer<Wrapper<T>>> wrapFilterHolders(
      GenContext ctx
   ) {
      return (holder, sink) -> {
         if (holder.isBound()) {
            int weight = ((TieredWeights.Weighted)holder.get()).weights().getWeight(ctx);
            if (weight > 0) {
               sink.accept(WeightedEntry.wrap((T)holder.get(), weight));
            }
         }
      };
   }

   private static Either<TieredWeights.Weight, Map<WorldTier, TieredWeights.Weight>> toEither(Map<WorldTier, TieredWeights.Weight> value) {
      if (value.size() == 5) {
         TieredWeights.Weight weight = value.getOrDefault(WorldTier.HAVEN, TieredWeights.Weight.ZERO);
         if (value.values().stream().allMatch(weight::equals)) {
            return Either.left(weight);
         }
      }

      return Either.right(value);
   }

   public static class Builder {
      com.google.common.collect.ImmutableMap.Builder<WorldTier, TieredWeights.Weight> mapBuilder = ImmutableMap.builder();

      public TieredWeights.Builder with(WorldTier tier, int weight, float quality) {
         return this.with(tier, new TieredWeights.Weight(weight, quality));
      }

      public TieredWeights.Builder with(WorldTier tier, TieredWeights.Weight weight) {
         this.mapBuilder.put(tier, weight);
         return this;
      }

      public TieredWeights build() {
         return new TieredWeights(this.mapBuilder.build());
      }
   }

   public record Weight(int weight, float quality) {
      public static TieredWeights.Weight ZERO = new TieredWeights.Weight(0, 0.0F);
      public static MapCodec<TieredWeights.Weight> CODEC = RecordCodecBuilder.mapCodec(
         inst -> inst.group(
               Codec.intRange(0, 65536).fieldOf("weight").forGetter(TieredWeights.Weight::weight),
               Codec.floatRange(-16.0F, 16.0F).optionalFieldOf("quality", 0.0F).forGetter(TieredWeights.Weight::quality)
            )
            .apply(inst, TieredWeights.Weight::new)
      );
      public static StreamCodec<ByteBuf, TieredWeights.Weight> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.VAR_INT, TieredWeights.Weight::weight, ByteBufCodecs.FLOAT, TieredWeights.Weight::quality, TieredWeights.Weight::new
      );

      public int getWeight(float luck) {
         return this.weight + Math.round(luck * this.quality);
      }
   }

   public interface Weighted {
      public static net.minecraft.util.random.Weight SAFE_ZERO = net.minecraft.util.random.Weight.of(0);

      TieredWeights weights();

      @SuppressWarnings("unchecked")
      default <T extends Weighted> Wrapper<T> wrap(WorldTier tier, float luck) {
         return wrap((T) this, tier, luck);
      }

      static <T extends Weighted> Wrapper<T> wrap(T item, WorldTier tier, float luck) {
         int weight = Math.max(0, item.weights().getWeight(tier, luck));
         if (weight == 0) {
            return new WeightedEntry.Wrapper<>(item, SAFE_ZERO);
         }
         return WeightedEntry.wrap(item, weight);
      }
   }
}
