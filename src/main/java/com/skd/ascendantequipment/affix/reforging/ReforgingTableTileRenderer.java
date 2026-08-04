package com.skd.ascendantequipment.affix.reforging;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.skd.ascendantequipment.client.AdventureModuleClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class ReforgingTableTileRenderer implements BlockEntityRenderer<ReforgingTableTile, ReforgingTableTileRenderer.State> {
   public ReforgingTableTileRenderer(Context context) {
   }

   public ReforgingTableTileRenderer.State createRenderState() {
      return new ReforgingTableTileRenderer.State();
   }

   public void extractRenderState(
      ReforgingTableTile blockEntity, ReforgingTableTileRenderer.State state, float partialTicks, Vec3 cameraPosition, CrumblingOverlay breakProgress
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
      state.time = blockEntity.time;
      state.partialTicks = partialTicks;
      state.step1 = blockEntity.step1;
   }

   public void submit(ReforgingTableTileRenderer.State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      BlockStateModel model = (BlockStateModel)Minecraft.getInstance().getModelManager().getStandaloneModel(AdventureModuleClient.HAMMER_MODEL);
      if (model != null) {
         float px = 0.0625F;
         poseStack.pushPose();
         poseStack.scale(1.25F, 1.25F, 1.25F);
         poseStack.translate(8.5F * px / 1.25F, 16.0F * px / 1.25F - 0.015F, 7.0F * px / 1.25F);
         poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
         poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
         float sinSq;
         if (state.step1) {
            float factor = state.time % 60 + state.partialTicks;
            float sin = Mth.sin(factor * (float) Math.PI / 120.0F);
            sinSq = sin * sin;
         } else {
            float factor = state.time % 5 + state.partialTicks;
            float sin = Mth.sin((float) (Math.PI / 2) + factor * (float) Math.PI / 10.0F);
            sinSq = sin * sin;
         }

         poseStack.translate(0.125F * sinSq, 0.0F, -0.15F * sinSq);
         poseStack.mulPose(Axis.YN.rotationDegrees(45.0F * sinSq));
         List<BlockStateModelPart> parts = new ArrayList<>();
         model.collectParts(RandomSource.create(), parts);
         submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), parts, new int[]{-1}, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }
   }

   public static class State extends BlockEntityRenderState {
      public int time;
      public float partialTicks;
      public boolean step1;
   }
}
