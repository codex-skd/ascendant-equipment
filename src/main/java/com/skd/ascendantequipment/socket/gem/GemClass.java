package com.skd.ascendantequipment.socket.gem;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootCategory;
import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;

public record GemClass(String key, HolderSet<LootCategory> types) {
   public static Codec<GemClass> EXPLICIT_CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.STRING.fieldOf("key").forGetter(GemClass::key),
            RegistryCodecs.homogeneousList(AscEq.BuiltInRegs.LOOT_CATEGORY.key()).fieldOf("types").forGetter(GemClass::types)
         )
         .apply(inst, GemClass::new)
   );
   public static Codec<GemClass> CODEC = Codec.either(EXPLICIT_CODEC, LootCategory.CODEC)
      .xmap(e -> (GemClass)e.map(Function.identity(), GemClass::new), GemClass::toEither);

   public GemClass(LootCategory category) {
      this(category.getKey().getPath(), category);
   }

   public GemClass(String key, LootCategory... types) {
      this(key, HolderSet.direct(Arrays.stream(types).map(AscEq.BuiltInRegs.LOOT_CATEGORY::wrapAsHolder).toList()));
   }

   public GemClass(String key, HolderSet<LootCategory> types) {
      this.key = key;
      this.types = types;
      Preconditions.checkArgument(!Strings.isNullOrEmpty(this.key), "Invalid GemClass with null key");
      Preconditions.checkArgument(this.types != null && this.types.size() > 0, "Invalid GemClass with null or empty types");
   }

   private static Either<GemClass, LootCategory> toEither(GemClass gc) {
      return gc.types.size() == 1 ? Either.right((LootCategory)((Holder)gc.types.iterator().next()).value()) : Either.left(gc);
   }
}
