package com.skd.ascendantequipment.client;

import com.google.common.collect.UnmodifiableIterator;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.SocketedGems;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SocketTooltipRenderer implements ClientTooltipComponent {
   public static final Identifier SOCKET = AscendantEquipment.loc("textures/gui/socket.png");
   private final SocketTooltipRenderer.SocketComponent comp;

   public SocketTooltipRenderer(SocketTooltipRenderer.SocketComponent comp) {
      this.comp = comp;
   }

   public int getHeight(Font font) {
      return spacing(font) * this.comp.gems.size();
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

   public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor gfx) {
      int spacing = spacing(font);

      for (int i = 0; i < this.comp.gems.size(); i++) {
         gfx.blit(RenderPipelines.GUI_TEXTURED, SOCKET, x, y + spacing * i, 0.0F, 0.0F, 9, 9, 9, 9);
      }

      for (GemInstance inst : this.comp.gems()) {
         if (inst.isValid()) {
            gfx.pose().pushMatrix();
            gfx.pose().scale(0.5F, 0.5F);
            gfx.fakeItem(inst.gemStack(), 2 * x + 1, 2 * y + 1);
            gfx.pose().popMatrix();
         }

         y += spacing;
      }
   }

   public void extractText(GuiGraphicsExtractor gfx, Font font, int x, int y) {
      int spacing = spacing(font);

      for (int i = 0; i < this.comp.gems.size(); i++) {
         gfx.text(font, getSocketDesc(this.comp.gems.get(i)), x + 12, y + 1 + spacing * i, -5588020, true);
      }
   }

   private static int spacing(Font font) {
      return 9 + 2;
   }

   public static Component getSocketDesc(GemInstance inst) {
      return (Component)(!inst.isValid() ? Component.translatable("socket.ascendant_equipment.empty") : inst.getSocketBonusTooltip(AdventureModuleClient.tooltipCtx()));
   }

   public record SocketComponent(ItemStack socketed, SocketedGems gems) implements TooltipComponent {
   }
}
