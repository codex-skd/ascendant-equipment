package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.skd.ascendantequipment.loot.RarityRenderData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class ShadowRenderer {
   static void renderShadow(
      PoseStack poseStack, SubmitNodeCollector collector, Entity entity, float partialTicks, LevelReader level, RarityRenderData.ShadowData data, int color
   ) {
      float size = data.size();
      if (!(size <= 0.0F)) {
         double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
         double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
         double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
         int xMin = Mth.floor(x - size);
         int xMax = Mth.floor(x + size);
         int yMin = Mth.floor(y - 2.0);
         int yMax = Mth.floor(y);
         int zMin = Mth.floor(z - size);
         int zMax = Mth.floor(z + size);
         collector.submitCustomGeometry(poseStack, AscEqRenderTypes.affixShadow(data.texture()), (pose, vtx) -> {
            MutableBlockPos pos = new MutableBlockPos();

            for (int zi = zMin; zi <= zMax; zi++) {
               for (int xi = xMin; xi <= xMax; xi++) {
                  pos.set(xi, 0, zi);
                  ChunkAccess chunkaccess = level.getChunk(pos);

                  for (int yi = yMin; yi <= yMax; yi++) {
                     pos.setY(yi);
                     renderBlockShadow(pose, vtx, entity, chunkaccess, partialTicks, level, pos, x, y, z, data, color);
                  }
               }
            }
         });
      }
   }

   private static void renderBlockShadow(
      Pose pose,
      VertexConsumer vertexConsumer,
      Entity entity,
      ChunkAccess chunk,
      float partialTicks,
      LevelReader level,
      BlockPos pos,
      double x,
      double y,
      double z,
      RarityRenderData.ShadowData data,
      int color
   ) {
      BlockPos blockpos = pos;
      BlockState blockstate = chunk.getBlockState(blockpos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         VoxelShape voxelshape = blockstate.getShape(chunk, blockpos);
         if (!voxelshape.isEmpty() && Block.isFaceFull(voxelshape, Direction.DOWN)) {
            int frames = data.frames();
            float frameTime = data.frameTime();
            float frame = (int)(((float)Minecraft.getInstance().level.getGameTime() + partialTicks) / frameTime % frames);
            float size = data.size();
            AABB aabb = voxelshape.bounds();
            aabb = aabb.intersect(
               new AABB(
                  entity.getX() - pos.getX() - size,
                  0.0,
                  entity.getZ() - pos.getZ() - size,
                  entity.getX() - pos.getX() + size,
                  aabb.maxY,
                  entity.getZ() - pos.getZ() + size
               )
            );
            double minX = pos.getX() + aabb.minX;
            double maxX = pos.getX() + aabb.maxX;
            double minY = pos.getY() + aabb.maxY;
            double minZ = pos.getZ() + aabb.minZ;
            double maxZ = pos.getZ() + aabb.maxZ;
            float xi = (float)(minX - x);
            float xp = (float)(maxX - x);
            float yi = (float)(minY - y) + 0.001F;
            float zi = (float)(minZ - z);
            float zp = (float)(maxZ - z);
            float u1 = -xi / 2.0F / size + 0.5F;
            float u2 = -xp / 2.0F / size + 0.5F;
            float v1 = -zi / 2.0F / size + 0.5F;
            float v2 = -zp / 2.0F / size + 0.5F;
            shadowVertex(pose, vertexConsumer, color, xi, yi, zi, u1, v1 / frames + frame / frames);
            shadowVertex(pose, vertexConsumer, color, xi, yi, zp, u1, v2 / frames + frame / frames);
            shadowVertex(pose, vertexConsumer, color, xp, yi, zp, u2, v2 / frames + frame / frames);
            shadowVertex(pose, vertexConsumer, color, xp, yi, zi, u2, v1 / frames + frame / frames);
         }
      }
   }

   private static void shadowVertex(Pose pose, VertexConsumer consumer, int color, float offsetX, float offsetY, float offsetZ, float u, float v) {
      Vector3f vector3f = pose.pose().transformPosition(offsetX, offsetY, offsetZ, new Vector3f());
      consumer.addVertex(vector3f.x(), vector3f.y(), vector3f.z(), color, u, v, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
   }
}
