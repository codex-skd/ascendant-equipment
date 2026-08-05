package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public enum SurfaceType implements BiPredicate<ServerLevelAccessor, BlockPos> {
   NEEDS_SKY(BlockAndLightGetter::canSeeSky),
   NEEDS_SURFACE((level, pos) -> pos.getY() >= level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
   BELOW_SURFACE((level, pos) -> pos.getY() < level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
   CANNOT_SEE_SKY((level, pos) -> !level.canSeeSky(pos)),
   SURFACE_OUTER_END((level, pos) -> NEEDS_SURFACE.test(level, pos) && (Mth.abs(pos.getX()) > 1024 || Mth.abs(pos.getZ()) > 1024)),
   ANY((level, pos) -> true),
   NEEDS_SKY_OR_SAME_VERTICAL_SLICE(SurfaceType::skyOrSameVerticalSlice);

   public static final Codec<SurfaceType> CODEC = CommonToolkitCodecs.enumCodec(SurfaceType.class);
   BiPredicate<ServerLevelAccessor, BlockPos> pred;

   SurfaceType(BiPredicate<ServerLevelAccessor, BlockPos> pred) {
      this.pred = pred;
   }

   public boolean test(ServerLevelAccessor t, BlockPos u) {
      return this.pred.test(t, u);
   }

   private static boolean skyOrSameVerticalSlice(ServerLevelAccessor level, BlockPos pos) {
      if (NEEDS_SKY.test(level, pos)) {
         return true;
      }

      Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1.0, false);
      return Math.abs(player.position().y - pos.getY()) <= 8.0;
   }
}
