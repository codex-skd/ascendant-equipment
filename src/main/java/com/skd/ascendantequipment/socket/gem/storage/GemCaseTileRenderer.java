package com.skd.ascendantequipment.socket.gem.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;

public class GemCaseTileRenderer implements BlockEntityRenderer<GemCaseTile, GemCaseTileRenderer.State> {
   private static final float PX = 0.0625F;
   private static final float SCALE = 0.16666667F;
   private final ItemModelResolver itemModelResolver;

   public GemCaseTileRenderer(Context context) {
      this.itemModelResolver = context.itemModelResolver();
   }

   public GemCaseTileRenderer.State createRenderState() {
      return new GemCaseTileRenderer.State();
   }

   public void extractRenderState(
      GemCaseTile blockEntity, GemCaseTileRenderer.State state, float partialTicks, Vec3 cameraPosition, CrumblingOverlay breakProgress
   ) {
      BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
      state.entries.clear();
      if (blockEntity.getLevel() != null) {
         Direction facing = (Direction)blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);

         state.facingAngle = switch (facing) {
            case NORTH -> 0.0F;
            case EAST -> 270.0F;
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            default -> 0.0F;
         };
         GemCaseAnimationState anim = blockEntity.getAnimationState();
         int gemIndex = 0;
         ObjectIterator var9 = blockEntity.gems.entrySet().iterator();

         while (var9.hasNext()) {
            Map.Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>> entry = (Map.Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>>)var9.next();
            if (gemIndex >= 16) {
               break;
            }

            DynamicHolder<Gem> holder = entry.getKey();
            if (holder.isBound()) {
               EnumMap<Purity, Integer> purityMap = entry.getValue();
               Purity highestStocked = null;

               for (Purity p : Purity.values()) {
                  if (purityMap.get(p) > 0 && (highestStocked == null || p.ordinal() > highestStocked.ordinal())) {
                     highestStocked = p;
                  }
               }

               if (highestStocked != null) {
                  ItemStack stack = GemItem.createStack((Gem)holder.get(), highestStocked, 1);
                  ItemStackRenderState renderState = new ItemStackRenderState();
                  this.itemModelResolver
                     .updateForTopItem(
                        renderState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, blockEntity.getBlockPos().hashCode() + gemIndex
                     );
                  GemCaseAnimationState.PositionInfo info = anim.getPosition(gemIndex, partialTicks);
                  state.entries.add(new GemCaseTileRenderer.Entry(renderState, info, gemIndex));
                  gemIndex++;
               }
            }
         }
      }
   }

   public void submit(GemCaseTileRenderer.State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
      for (GemCaseTileRenderer.Entry entry : state.entries) {
         int slot = entry.position.baseSlot();
         float gridX = slot % 4 + entry.position.offsetX();
         float gridZ = slot / 4 + entry.position.offsetZ();
         poseStack.pushPose();
         poseStack.translate(0.5F, 0.0F, 0.5F);
         poseStack.mulPose(Axis.YP.rotationDegrees(state.facingAngle));
         poseStack.translate(-0.5F, 0.0F, -0.5F);
         poseStack.translate(0.0F, 1.0F, 0.0F);
         poseStack.scale(0.16666667F, 0.16666667F, 0.16666667F);
         float tx = (2.5F + gridX * 3.75F) / 0.16666667F * 0.0625F;
         float ty = -0.75F + 0.01F * entry.iterIndex;
         float tz = (3.5F + gridZ * 3.25F) / 0.16666667F * 0.0625F;
         poseStack.translate(tx, ty, tz);
         poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
         entry.renderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
         poseStack.popPose();
      }
   }

   public record Entry(ItemStackRenderState renderState, GemCaseAnimationState.PositionInfo position, int iterIndex) {
   }

   public static class State extends BlockEntityRenderState {
      public final List<GemCaseTileRenderer.Entry> entries = new ArrayList<>();
      public float facingAngle;
   }
}
