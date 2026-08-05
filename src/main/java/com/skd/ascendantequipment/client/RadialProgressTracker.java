package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skd.ascendantequipment.affix.effect.RadialAffix;
import com.skd.ascendantequipment.socket.gem.bonus.special.RadialBonus;
import com.skd.ascendantequipment.util.RadialUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@EventBusSubscriber(modid = "ascendant_equipment", value = Dist.CLIENT)
public class RadialProgressTracker {
   @Nullable
   private static RadialProgressTracker.CacheKey lastKey = null;
   private static Set<BlockPos> knownAOEBlocks = Set.of();

   private static Set<BlockPos> getAOEBlocks() {
      Level level = Minecraft.getInstance().level;
      Player player = Minecraft.getInstance().player;
      HitResult res = Minecraft.getInstance().hitResult;
      ItemStack tool = player == null ? ItemStack.EMPTY : player.getMainHandItem();
      if (level != null
         && player != null
         && res != null
         && res.getType() == Type.BLOCK
         && !tool.isEmpty()
         && RadialUtil.RadialState.isRadialMiningEnabled(player)) {
         BlockHitResult blockTrace = (BlockHitResult)res;
         BlockPos pos = blockTrace.getBlockPos();
         Direction dir = blockTrace.getDirection();
         RadialProgressTracker.CacheKey key = new RadialProgressTracker.CacheKey(pos, tool, dir, player.getDirection());
         if (lastKey != null && lastKey.equals(key)) {
            return knownAOEBlocks;
         }

         lastKey = key;
         RadialUtil.RadialData afxData = RadialAffix.getRadialData(tool);
         RadialUtil.RadialData gemData = RadialBonus.getRadialData(tool);
         Set<BlockPos> positions = new HashSet<>();
         if (afxData != null) {
            positions.addAll(RadialUtil.getBrokenBlocks(player, dir, pos, afxData));
         }

         if (gemData != null) {
            positions.addAll(RadialUtil.getBrokenBlocks(player, dir, pos, gemData));
         }

         knownAOEBlocks = positions;
         return knownAOEBlocks;
      } else {
         lastKey = null;
         knownAOEBlocks = Set.of();
         return knownAOEBlocks;
      }
   }

   public static void breakClientBlocks(MultiPlayerGameMode mode, BlockPos srcPos) {
      if (lastKey != null && lastKey.pos.equals(srcPos)) {
         Level level = Minecraft.getInstance().level;

         for (BlockPos pos : getAOEBlocks()) {
            level.removeBlock(pos, false);
         }

         lastKey = null;
         knownAOEBlocks = Set.of();
      }
   }

   @SubscribeEvent
   public static void submitOutlines(SubmitCustomGeometryEvent e) {
      Set<BlockPos> blocks = getAOEBlocks();
      if (!blocks.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         Level level = mc.level;
         if (level != null) {
            Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
            PoseStack pose = e.getPoseStack();
            SubmitNodeCollector collector = e.getSubmitNodeCollector();

            for (BlockPos pos : blocks) {
               BlockState state = level.getBlockState(pos);
               if (!state.isAir()) {
                  VoxelShape shape = state.getShape(level, pos);
                  if (!shape.isEmpty()) {
                     double x = pos.getX() - camPos.x;
                     double y = pos.getY() - camPos.y;
                     double z = pos.getZ() - camPos.z;
                     collector.submitCustomGeometry(pose, RenderTypes.lines(), (p, buffer) -> shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                        Vector3f normal = new Vector3f((float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1)).normalize();
                        buffer.addVertex(p, (float)(x1 + x), (float)(y1 + y), (float)(z1 + z)).setColor(0, 0, 0, 102).setNormal(p, normal).setLineWidth(2.0F);
                        buffer.addVertex(p, (float)(x2 + x), (float)(y2 + y), (float)(z2 + z)).setColor(0, 0, 0, 102).setNormal(p, normal).setLineWidth(2.0F);
                     }));
                  }
               }
            }

            submitCrumbling(e, blocks, mc, level, camPos, pose, collector);
         }
      }
   }

   private static void submitCrumbling(
      SubmitCustomGeometryEvent e, Set<BlockPos> blocks, Minecraft mc, Level level, Vec3 camPos, PoseStack pose, SubmitNodeCollector collector
   ) {
      MultiPlayerGameMode controller = mc.gameMode;
      if (controller != null && controller.isDestroying()) {
         Player player = mc.player;
         if (player != null && lastKey != null) {
            BlockPos target = lastKey.pos;
            int progress = -1;

            for (BlockBreakingRenderState entry : e.getLevelRenderState().blockBreakingRenderStates) {
               if (entry.blockPos().equals(target)) {
                  progress = entry.progress();
                  break;
               }
            }

            if (progress >= 0) {
               BlockState targetState = level.getBlockState(target);
               if (RadialUtil.isEffective(targetState, player, target)) {
                  BlockStateModelSet models = mc.getModelManager().getBlockStateModelSet();

                  for (BlockPos pos : blocks) {
                     BlockState state = level.getBlockState(pos);
                     if (!state.isAir()) {
                        BlockStateModel model = models.get(state);
                        pose.pushPose();
                        pose.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                        List<BlockStateModelPart> parts = new ArrayList<>();
                        model.collectParts(RandomSource.create(state.getSeed(pos)), parts);
                        collector.submitBreakingBlockModel(pose, parts, progress);
                        pose.popPose();
                     }
                  }
               }
            }
         }
      }
   }

   private static final class CacheKey {
      private final BlockPos pos;
      private final ItemStack tool;
      private final Direction hitDir;
      private final Direction playerDir;

      private CacheKey(BlockPos pos, ItemStack tool, Direction hitDir, Direction playerDir) {
         this.pos = pos;
         this.tool = tool;
         this.hitDir = hitDir;
         this.playerDir = playerDir;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof RadialProgressTracker.CacheKey other)
            ? false
            : this.pos.equals(other.pos)
               && this.hitDir == other.hitDir
               && this.playerDir == other.playerDir
               && ItemStack.isSameItemSameComponents(this.tool, other.tool);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.pos, this.hitDir, this.playerDir, this.tool.getItem(), this.tool.getComponentsPatch());
      }
   }
}
