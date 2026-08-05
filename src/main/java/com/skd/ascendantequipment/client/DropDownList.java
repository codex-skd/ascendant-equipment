package com.skd.ascendantequipment.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class DropDownList<T> extends AbstractWidget {
   public static final int NO_SELECTION = -1;
   protected final int maxDisplayedEntries;
   protected final int baseHeight;
   protected List<T> entries;
   protected float scrollOffs;
   protected boolean scrolling;
   protected int startIndex;
   protected boolean isOpen = false;
   protected int selected = -1;

   public DropDownList(int x, int y, int width, int height, Component narrationMsg, List<T> entries, int maxDisplayedEntries) {
      super(x, y, width, height, narrationMsg);
      this.entries = entries;
      this.maxDisplayedEntries = maxDisplayedEntries;
      this.baseHeight = height;
   }

   protected void extractWidgetRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      int selected = this.getSelected();
      if (selected != -1) {
         this.renderEntry(gfx, this.getX(), this.getY(), mouseX, mouseY, this.entries.get(selected));
      }

      if (this.isOpen) {
         for (int i = this.startIndex; i < this.startIndex + this.maxDisplayedEntries && i < this.entries.size(); i++) {
            this.renderEntry(gfx, this.getX(), this.getY() + this.baseHeight * (1 + i - this.startIndex), mouseX, mouseY, this.entries.get(i));
         }
      }
   }

   public void onClick(MouseButtonEvent event, boolean doubleClick) {
      if (!this.isOpen) {
         if (this.entries.isEmpty()) {
            return;
         }

         this.isOpen = true;
         this.height = this.baseHeight * (1 + Math.min(this.entries.size(), this.maxDisplayedEntries));
      } else {
         this.selected = this.getHoveredSlot(event.x(), event.y());
         this.height = this.baseHeight;
         this.isOpen = false;
      }
   }

   public static boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
   }

   public int getHoveredSlot(double mouseX, double mouseY) {
      if (isHovering(this.getX(), this.getY(), this.width, this.baseHeight, mouseX, mouseY)) {
         return this.getSelected();
      }

      if (this.isOpen) {
         for (int i = 0; i < this.maxDisplayedEntries; i++) {
            if (this.startIndex + i < this.entries.size()
               && isHovering(this.getX(), this.getY() + (i + 1) * this.baseHeight, this.width, this.baseHeight, mouseX, mouseY)) {
               return this.startIndex + i;
            }
         }
      }

      return -1;
   }

   public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
      if (this.scrolling && this.isScrollBarActive()) {
         double pMouseY = event.y();
         int barTop = this.getX() + 14;
         int barBot = barTop + 103;
         this.scrollOffs = ((float)pMouseY - barTop - 6.0F) / (barBot - barTop - 12.0F) - 0.12F;
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * this.getOffscreenRows() + 0.5);
         return true;
      } else {
         return super.mouseDragged(event, pDragX, pDragY);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.isScrollBarActive()) {
         int i = this.getOffscreenRows();
         this.scrollOffs = (float)(this.scrollOffs - scrollY / i);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * i + 0.5);
      }

      return true;
   }

   private boolean isScrollBarActive() {
      return this.isOpen() && this.entries.size() > this.maxDisplayedEntries;
   }

   protected int getOffscreenRows() {
      return this.entries.size() - this.maxDisplayedEntries;
   }

   public int getSelected() {
      return Mth.clamp(this.selected, -1, this.entries.size() - 1);
   }

   public void setSelected(int selected) {
      this.selected = Mth.clamp(selected, -1, this.entries.size() - 1);
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   protected abstract void renderEntry(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, T var6);

   protected void updateWidgetNarration(NarrationElementOutput output) {
   }

   public void setEntries(List<T> entries) {
      this.entries = entries;
      this.selected = this.entries.isEmpty() ? -1 : 0;
      this.height = this.baseHeight;
      this.startIndex = 0;
      this.isOpen = false;
   }
}
