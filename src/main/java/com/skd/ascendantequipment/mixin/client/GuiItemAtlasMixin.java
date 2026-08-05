package com.skd.ascendantequipment.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.skd.ascendantequipment.client.AdventureModuleClient;
import com.skd.ascendantequipment.client.AscEqRenderStateHolder;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GuiItemAtlas.class, remap = false)
public abstract class GuiItemAtlasMixin {
   // In 26.1.2 this wrapped BufferSource.endBatch() right after item rendering. In 26.2 the
   // whole GuiItemAtlas render path was rewritten around SubmitNodeStorage/SubmitNodeCollector;
   // MultiBufferSource no longer exists here, so the equivalent injection point is the render
   // call itself, ItemStackRenderState.submit(PoseStack, SubmitNodeCollector, int, int, int).
   @WrapOperation(
      method = "drawToSlot",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
      )
   )
   private void apoth_wrapSubmitWithModifier(
      ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int overlay, int seed, Operation<Void> original
   ) {
      Float alpha = ((AscEqRenderStateHolder)item).apoth$getRenderAlpha();
      if (alpha == null) {
         original.call(new Object[]{item, poseStack, collector, packedLight, overlay, seed});
      } else {
         ResourceKey<PipelineModifier> key;
         if (Float.isNaN(alpha)) {
            key = AdventureModuleClient.GRAY_ITEM;
         } else {
            key = AdventureModuleClient.GHOST_ITEM_TIERS[PipelinedRenderer.nearestTier(alpha)];
         }

         RenderSystem.pushPipelineModifier(key);

         try {
            original.call(new Object[]{item, poseStack, collector, packedLight, overlay, seed});
         } finally {
            RenderSystem.popPipelineModifier();
         }
      }
   }
}
