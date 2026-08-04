package com.skd.ascendantequipment.affix;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum AffixType implements StringRepresentable {
   STAT("stat"),
   BASIC_EFFECT("basic_effect"),
   ABILITY("ability");

   public static final IntFunction<AffixType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.CLAMP);
   public static final Codec<AffixType> CODEC = StringRepresentable.fromValues(AffixType::values);
   public static final StreamCodec<ByteBuf, AffixType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
   private String name;

   AffixType(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
