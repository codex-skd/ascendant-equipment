package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import com.skd.ascendantequipment.net.GemCaseSelectPayload;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

public class GemCaseSelectButton extends AbstractButton {
   protected final GemCaseScreen screen;
   protected final int index;

   public GemCaseSelectButton(GemCaseScreen screen, int index, int x, int y) {
      super(x, y, 16, 16, CommonComponents.EMPTY);
      this.screen = screen;
      this.index = index;
   }

   protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      GemCaseScreen.SafeSlot slot = this.getSafeSlot();
      if (slot != null) {
         int count = ((GemCaseMenu)this.screen.getMenu()).getGemCount(slot.gem());
         if (count == 0) {
            PipelinedRenderer.ghostFakeItem(gfx, slot.displayStack(), this.getX(), this.getY());
         } else {
            gfx.fakeItem(slot.displayStack(), this.getX(), this.getY());
         }

         if (count > 1) {
            String countStr = GemCaseBlock.format(count);
            float scale = 1.0F;
            if (countStr.length() > 2) {
               scale = 2.0F / countStr.length();
            }

            gfx.pose().pushMatrix();
            gfx.pose().scale(scale, scale);
            float textX = (this.getX() + 16 - (mc.font.width(countStr) - 1) * scale) / scale;
            float textY = (this.getY() + 16 - (9 - 2) * scale) / scale;
            gfx.text(mc.font, countStr, (int)textX, (int)textY, -1426063361, true);
            gfx.pose().popMatrix();
         }

         if (this.isHovered()) {
            gfx.fill(this.getX(), this.getY(), this.getX() + 16, this.getY() + 16, 1090519039);
            Component desc = Component.translatable(((GemItem)AscEq.Items.GEM.value()).getGemDescriptionId(slot.displayStack()));
            gfx.setTooltipForNextFrame(mc.font, desc, mouseX, mouseY);
         }
      }
   }

   public void onPress(InputWithModifiers input) {
      GemCaseScreen.SafeSlot slot = this.getSafeSlot();
      if (slot != null) {
         DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.getSafeSlot().gem());
         ((GemCaseMenu)this.screen.getMenu()).setSelectedGem(holder);
         ClientPacketDistributor.sendToServer(new GemCaseSelectPayload(holder), new CustomPacketPayload[0]);
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   @Nullable
   private GemCaseScreen.SafeSlot getSafeSlot() {
      int idx = this.screen.startIndex * 6 + this.index;
      return idx >= 0 && idx < this.screen.data.size() ? this.screen.data.get(idx) : null;
   }
}
