package com.skd.ascendantequipment.affix.augmenting;

import com.mojang.blaze3d.vertex.PoseStack;
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
import org.joml.Quaternionf;

public class AugmentingTableTileRenderer implements BlockEntityRenderer<AugmentingTableTile, AugmentingTableTileRenderer.State> {
   public AugmentingTableTileRenderer(Context context) {
   }

   public AugmentingTableTileRenderer.State createRenderState() {
      return new AugmentingTableTileRenderer.State();
   }

   public void extractRenderState(
      AugmentingTableTile blockEntity, AugmentingTableTileRenderer.State state, float partialTicks, Vec3 cameraPosition, CrumblingOverlay breakProgress
   ) {
      super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
      state.time = blockEntity.time;
      state.partialTicks = partialTicks;
      state.stage = blockEntity.stage;
   }

   public void submit(AugmentingTableTileRenderer.State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      if (state.stage != AugmentingTableTile.AnimationStage.HIDING) {
         BlockStateModel model = (BlockStateModel)Minecraft.getInstance().getModelManager().getStandaloneModel(AdventureModuleClient.STAR_CUBE_MODEL);
         if (model != null) {
            float px = 0.0625F;
            poseStack.pushPose();
            poseStack.translate(5.0F * px, 5.0F * px, 5.0F * px);
            switch (state.stage) {
               case HIDING:
               default:
                  break;
               case RISING: {
                  float progress = (state.time + state.partialTicks) / AugmentingTableTile.RISE_TIME;
                  float rise = Mth.lerp(progress, 0.1F * px, 11.0F * px);
                  poseStack.translate(0.0F, rise, 0.0F);
                  break;
               }
               case FALLING: {
                  float progress = (AugmentingTableTile.RISE_TIME - state.time + state.partialTicks) / AugmentingTableTile.RISE_TIME;
                  float rise = Mth.lerp(progress, 11.0F * px, 0.1F * px);
                  poseStack.translate(0.0F, rise, 0.0F);
                  break;
               }
               case SPINNING:
                  float rotation = (state.time % 360 + state.partialTicks) * (float) Math.PI / 180.0F;
                  poseStack.translate(0.0F, 11.0F * px, 0.0F);
                  poseStack.translate(3.0F * px, 3.0F * px, 3.0F * px);
                  poseStack.mulPose(new Quaternionf().rotationXYZ(rotation, 0.0F, rotation));
                  poseStack.translate(-3.0F * px, -3.0F * px, -3.0F * px);
            }

            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(RandomSource.create(), parts);
            submitNodeCollector.submitBlockModel(
               poseStack, Sheets.translucentBlockSheet(), parts, new int[]{-1}, state.lightCoords, OverlayTexture.NO_OVERLAY, 0
            );
            poseStack.popPose();
         }
      }
   }

   public static class State extends BlockEntityRenderState {
      public int time;
      public float partialTicks;
      public AugmentingTableTile.AnimationStage stage;
   }
}
