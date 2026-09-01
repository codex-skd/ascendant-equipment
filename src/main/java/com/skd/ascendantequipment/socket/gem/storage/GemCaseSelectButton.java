package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import com.skd.ascendantequipment.net.GemCaseSelectPayload;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class GemCaseSelectButton extends AbstractButton {
   protected final GemCaseScreen screen;
   protected final int index;

   public GemCaseSelectButton(GemCaseScreen screen, int index, int x, int y) {
      super(x, y, 16, 16, CommonComponents.EMPTY);
      this.screen = screen;
      this.index = index;
   }

   @Override
   protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      GemCaseScreen.SafeSlot slot = this.getSafeSlot();
      if (slot != null) {
         int count = ((GemCaseMenu)this.screen.getMenu()).getGemCount(slot.gem());
         if (count == 0) {
            PipelinedRenderer.ghostFakeItem(gfx, slot.displayStack(), this.getX(), this.getY());
         } else {
            gfx.renderFakeItem(slot.displayStack(), this.getX(), this.getY());
         }

         if (count > 1) {
            String countStr = GemCaseBlock.format(count);
            float scale = 1.0F;
            if (countStr.length() > 2) {
               scale = 2.0F / countStr.length();
            }

            gfx.pose().pushPose();
            gfx.pose().scale(scale, scale, 1);
            gfx.pose().translate(0.0F, 0.0F, 200.0F);
            float textX = (this.getX() + 16 - (mc.font.width(countStr) - 1) * scale) / scale;
            float textY = (this.getY() + 16 - (mc.font.lineHeight - 2) * scale) / scale;
            gfx.drawString(mc.font, countStr, textX, textY, 0xAAFFFFFF, true);
            gfx.pose().popPose();
         }

         if (this.isHovered()) {
            gfx.pose().pushPose();
            gfx.pose().translate(0.0F, 0.0F, 200.0F);
            gfx.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 0x40FFFFFF);
            gfx.pose().popPose();
            Component desc = Component.translatable(slot.displayStack().getDescriptionId());
            gfx.renderTooltip(mc.font, desc, mouseX, mouseY);
         }
      }
   }

   @Override
   public void onPress() {
      GemCaseScreen.SafeSlot slot = this.getSafeSlot();
      if (slot != null) {
         DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.getSafeSlot().gem());
         ((GemCaseMenu)this.screen.getMenu()).setSelectedGem(holder);
         PacketDistributor.sendToServer(new GemCaseSelectPayload(holder));
      }
   }

   @Override
   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   @Nullable
   private GemCaseScreen.SafeSlot getSafeSlot() {
      int idx = this.screen.startIndex * GemCaseScreen.SLOTS_PER_ROW + this.index;
      return idx >= 0 && idx < this.screen.data.size() ? this.screen.data.get(idx) : null;
   }
}
