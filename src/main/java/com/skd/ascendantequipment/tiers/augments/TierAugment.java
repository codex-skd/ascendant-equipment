package com.skd.ascendantequipment.tiers.augments;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.codec.CodecProvider;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public interface TierAugment extends CodecProvider<TierAugment> {
   WorldTier tier();

   TierAugment.Target target();

   int sortIndex();

   void apply(ServerLevelAccessor var1, LivingEntity var2);

   void remove(ServerLevelAccessor var1, LivingEntity var2);

   Component getDescription(AttributeTooltipContext var1);

   enum Target implements StringRepresentable {
      PLAYERS("players"),
      MONSTERS("monsters");

      public static final IntFunction<TierAugment.Target> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
      public static final Codec<TierAugment.Target> CODEC = StringRepresentable.fromValues(TierAugment.Target::values);
      public static final StreamCodec<ByteBuf, TierAugment.Target> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
      private String name;

      Target(String name) {
         this.name = name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
