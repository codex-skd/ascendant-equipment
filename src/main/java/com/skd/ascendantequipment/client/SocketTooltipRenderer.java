package com.skd.ascendantequipment.client;

import com.google.common.collect.UnmodifiableIterator;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.SocketedGems;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipRenderer implements ClientTooltipComponent {
   public static final ResourceLocation SOCKET = AscendantEquipment.loc("textures/gui/socket.png");
   private final SocketTooltipRenderer.SocketComponent comp;

   public SocketTooltipRenderer(SocketTooltipRenderer.SocketComponent comp) {
      this.comp = comp;
   }

   public int getHeight() {
      int spacing = 11;
      return spacing * this.comp.gems.size();
   }

   public int getWidth(Font font) {
      int maxWidth = 0;
      UnmodifiableIterator var3 = this.comp.gems.gems().iterator();

      while (var3.hasNext()) {
         GemInstance inst = (GemInstance)var3.next();
         maxWidth = Math.max(maxWidth, font.width(getSocketDesc(inst)) + 12);
      }

      return maxWidth;
   }

   public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
      int spacing = 11;

      for (int i = 0; i < this.comp.gems.size(); i++) {
         gfx.blit(SOCKET, x, y + spacing * i, 0, 0, 0, 9, 9, 9, 9);
      }

      for (GemInstance inst : this.comp.gems()) {
         if (inst.isValid()) {
            gfx.pose().pushPose();
            gfx.pose().scale(0.5F, 0.5F, 1);
            gfx.renderFakeItem(inst.gemStack(), 2 * x + 1, 2 * y + 1);
            gfx.pose().popPose();
         }

         y += spacing;
      }
   }

   public void renderText(Font pFont, int pX, int pY, org.joml.Matrix4f pMatrix4f, net.minecraft.client.renderer.MultiBufferSource.BufferSource pBufferSource) {
      int spacing = 11;

      for (int i = 0; i < this.comp.gems.size(); i++) {
         pFont.drawInBatch(getSocketDesc(this.comp.gems.get(i)), pX + 12, pY + 1 + spacing * i, 0xAABBCC, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
      }
   }

   public static Component getSocketDesc(GemInstance inst) {
      return (Component)(!inst.isValid() ? Component.translatable("socket.ascendant_equipment.empty") : inst.getSocketBonusTooltip(AdventureModuleClient.tooltipCtx()));
   }

   public record SocketComponent(ItemStack socketed, SocketedGems gems) implements TooltipComponent {
   }
}
