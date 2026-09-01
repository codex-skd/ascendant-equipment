package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.AscEq;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class PipelinedRenderer {
   public static void ghostFakeItem(GuiGraphics gfx, ItemStack stack, int x, int y, float alpha) {
      int tierIdx = nearestTier(alpha);
      float tierAlpha = AdventureModuleClient.GHOST_ALPHA_TIERS[tierIdx] / 255.0F;
      ItemStack copy = stack.copy();
      copy.set(AscEq.Components.RENDER_ALPHA, tierAlpha);
      gfx.renderFakeItem(copy, x, y);
   }

   public static void ghostFakeItem(GuiGraphics gfx, ItemStack stack, int x, int y) {
      ghostFakeItem(gfx, stack, x, y, 0.26666668F);
   }

   public static void grayFakeItem(GuiGraphics gfx, ItemStack stack, int x, int y) {
      ItemStack copy = stack.copy();
      copy.set(AscEq.Components.RENDER_ALPHA, Float.NaN);
      gfx.renderFakeItem(copy, x, y);
   }

   public static int nearestTier(float alpha) {
      int[] tiers = AdventureModuleClient.GHOST_ALPHA_TIERS;
      int target = Math.round(alpha * 255.0F);
      int best = 0;
      int bestDist = Integer.MAX_VALUE;

      for (int i = 0; i < tiers.length; i++) {
         int d = Math.abs(tiers[i] - target);
         if (d < bestDist) {
            bestDist = d;
            best = i;
         }
      }

      return best;
   }

   private PipelinedRenderer() {
   }
}
