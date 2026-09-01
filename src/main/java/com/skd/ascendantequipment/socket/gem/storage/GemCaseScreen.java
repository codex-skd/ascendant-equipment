package com.skd.ascendantequipment.socket.gem.storage;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import com.skd.ascendantequipment.client.SimpleTexButton;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.payloads.ButtonClickPayload;
import com.skd.commontoolkit.util.DrawsOnLeft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class GemCaseScreen extends AdventureContainerScreen<GemCaseMenu> implements DrawsOnLeft {
   public static final ResourceLocation TEXTURES = AscendantEquipment.loc("textures/gui/gem_case.png");
   public static final int MAX_ROWS = 3;
   public static final int SLOTS_PER_ROW = 6;
   public static final int LEFT_PANEL_WIDTH = 65;
   public static final int LEFT_PANEL_HEIGHT = 193;
   public static final int LEFT_PANEL_Y_OFFSET = 16;
   protected float scrollOffs;
   protected boolean scrolling;
   protected int startIndex;
   protected List<GemCaseScreen.SafeSlot> data = new ArrayList<>();
   protected List<SimpleTexButton> upgradeButtons = new ArrayList<>();
   protected @Nullable EditBox filter = null;

   public GemCaseScreen(GemCaseMenu container, Inventory inv, Component title) {
      super(container, inv, title);
      this.imageHeight = 230;
      this.containerChanged();
      container.setNotifier(this::containerChanged);
   }

   @Override
   protected void init() {
      super.init();
      this.filter = this.addRenderableWidget(
         new EditBox(this.font, this.getGuiLeft() + 16, this.getGuiTop() + 16, 110, 11, this.filter, Component.literal(""))
      );
      this.filter.setBordered(false);
      this.filter.setTextColor(0x97714F);
      this.filter.setResponder(t -> this.containerChanged());
      this.setFocused(this.filter);

      for (int i = 0; i < MAX_ROWS * SLOTS_PER_ROW; i++) {
         GemCaseSelectButton btn = new GemCaseSelectButton(this, i, this.getGuiLeft() + 21 + i % SLOTS_PER_ROW * 18, this.getGuiTop() + 31 + i / SLOTS_PER_ROW * 19);
         this.addRenderableWidget(btn);
      }

      this.upgradeButtons.clear();
      Purity[] purities = Purity.values();

      for (int i = 1; i < purities.length; i++) {
         Purity prev = purities[i - 1];
         Purity purity = purities[i];
         SimpleTexButton btn = SimpleTexButton.builder()
            .size(16, 16)
            .texture(TEXTURES)
            .texSize(307, 256)
            .texPos(291, 29)
            .pos(this.getGuiLeft() + 30 + (i - 1) * 18, this.getGuiTop() + 109)
            .message(AscendantEquipment.lang("button", "gem_case.upgrade", prev.toComponent(), purity.toComponent()))
            .inactiveMessage(AscendantEquipment.lang("button", "gem_case.upgrade_no_materials"))
            .action(this.tryUpgrade(purity))
            .build();
         this.upgradeButtons.add(btn);
         this.addRenderableWidget(btn);
      }

      this.containerChanged();
   }

   @Override
   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
      if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.getFocused() == this.filter) {
         return true;
      }
      return super.keyPressed(pKeyCode, pScanCode, pModifiers);
   }

   @Override
   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
      super.render(gfx, mouseX, mouseY, partialTicks);
      this.renderTooltip(gfx, mouseX, mouseY);
   }

   @Override
   protected void renderTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
      super.renderTooltip(gfx, mouseX, mouseY);
      if (this.getSelectedGem() != null) {
         for (Purity p : Purity.ALL_PURITIES) {
            if (!p.isAtLeast(this.getSelectedGem().getMinPurity())) continue;
            int count = ((GemCaseMenu)this.menu).getGemCount(this.getSelectedGem(), p);
            if (count == 0) {
               int slotIndex = p.ordinal();
                int x = this.getGuiLeft() + 21 + slotIndex * 18;
                int y = this.getGuiTop() + 91;
                if (this.isHovering(x - this.getGuiLeft(), y - this.getGuiTop(), 16, 16, mouseX, mouseY)
                   && ((GemCaseMenu)this.menu).getCarried().isEmpty()) {
                  ItemStack stack = this.getSelectedGem().toStack(p);
                  List<Component> tooltip = new ArrayList<>();
                  tooltip.add(stack.getHoverName());
                  tooltip.add(AscendantEquipment.lang("tooltip", "gem_case.none_owned").withStyle(ChatFormatting.RED));
                  tooltip.add(CommonComponents.SPACE);
                  stack.getItem().appendHoverText(stack, TooltipContext.of(Minecraft.getInstance().level), tooltip, TooltipFlag.NORMAL);
                  gfx.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
               }
            }
         }
      }
   }

   @Override
   protected void renderBg(GuiGraphics gfx, float partial, int mouseX, int mouseY) {
      int left = this.leftPos;
      int top = this.topPos;
      gfx.blit(TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight, 307, 256);
      int scrollbarPos = (int)(90.0F * this.scrollOffs);
      gfx.blit(TEXTURES, left + 13, top + 29 + scrollbarPos, 303, this.isScrollBarActive() ? 0 : 12, 4, 12, 307, 256);
      gfx.blit(TEXTURES, left - LEFT_PANEL_WIDTH, top + LEFT_PANEL_Y_OFFSET, 198, 0, LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT, 307, 256);
      if (this.getSelectedGem() != null) {
         for (Purity p : Purity.ALL_PURITIES) {
            if (!p.isAtLeast(this.getSelectedGem().getMinPurity())) continue;
            int count = ((GemCaseMenu)this.menu).getGemCount(this.getSelectedGem(), p);
            if (count == 0) {
               ItemStack stack = this.getSelectedGem().toStack(p);
               int slotIndex = p.ordinal();
                int x = this.getGuiLeft() + 21 + slotIndex * 18;
                int y = this.getGuiTop() + 91;
               PipelinedRenderer.ghostFakeItem(gfx, stack, x, y);
            }
         }
      }
   }

   @Override
   protected void renderSlotContents(GuiGraphics gfx, ItemStack stack, Slot slot, @Nullable String stackCount) {
      if (slot instanceof GemCaseSlot gss) {
         Gem gem = ((GemCaseMenu)this.menu).selectedGem;
         int count = 0;
         if (gem != null) {
            count = ((GemCaseMenu)this.menu).getGemCount(gem, gss.purity);
         }
         if (count > 0) {
            gfx.renderFakeItem(stack, slot.x, slot.y);
         } else {
            PipelinedRenderer.ghostFakeItem(gfx, stack, slot.x, slot.y);
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
            float textX = (slot.x + 16 - (this.font.width(countStr) - 1) * scale) / scale;
            float textY = (slot.y + 16 - (this.font.lineHeight - 2) * scale) / scale;
            gfx.drawString(this.font, countStr, textX, textY, 0xFFFFFF, true);
            gfx.pose().popPose();
         }
      } else {
         super.renderSlotContents(gfx, stack, slot, stackCount);
      }
   }

   @Override
   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      this.scrolling = false;
      if (this.isHovering(14, 29, 4, 103, pMouseX, pMouseY)) {
         this.scrolling = true;
         this.mouseDragged(pMouseX, pMouseY, pButton, 0, 0);
         return true;
      } else if (this.filter.isHovered() && pButton == 1) {
         this.filter.setValue("");
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   @Override
   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (this.scrolling && this.isScrollBarActive()) {
         int barTop = this.topPos + 14;
         int barBot = barTop + 103;
         this.scrollOffs = ((float)pMouseY - barTop - 6.0F) / (barBot - barTop - 12.0F) - 0.12F;
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * this.getOffscreenRows() + 0.5);
         return true;
      } else {
         return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
      }
   }

   @Override
   public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
      if (this.isScrollBarActive()) {
         int i = this.getOffscreenRows();
         this.scrollOffs = (float)(this.scrollOffs - pScrollY / i);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)(this.scrollOffs * i + 0.5);
      }

      return true;
   }

   @Nullable
   public Gem getSelectedGem() {
      return ((GemCaseMenu)this.menu).selectedGem;
   }

   public List<Rect2i> getExclusionAreas() {
      return List.of(new Rect2i(this.leftPos - LEFT_PANEL_WIDTH, this.topPos + LEFT_PANEL_Y_OFFSET, LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT));
   }

   private boolean isScrollBarActive() {
      return this.data.size() > MAX_ROWS * SLOTS_PER_ROW;
   }

   protected int getOffscreenRows() {
      return Math.ceilDiv(this.data.size() - MAX_ROWS * SLOTS_PER_ROW, SLOTS_PER_ROW);
   }

   private void containerChanged() {
      this.data.clear();

      for (Gem gem : GemRegistry.INSTANCE.getValues()) {
         GemCaseScreen.SafeSlot slot = new GemCaseScreen.SafeSlot(gem, ((GemCaseMenu)this.menu).getGemCount(gem), new ItemStack(AscEq.Items.GEM));
         GemItem.setGem(slot.displayStack, gem);
         GemItem.setPurity(slot.displayStack, Purity.NORMAL);
         this.data.add(slot);
      }

      this.data = this.filter(this.data);
      if (!this.isScrollBarActive()) {
         this.scrollOffs = 0.0F;
         this.startIndex = 0;
      }

      Collections.sort(
         this.data,
         Comparator.<GemCaseScreen.SafeSlot, Boolean>comparing(slot -> slot.count <= 0)
            .thenComparing(Comparator.comparing(slot -> slot.gem.getId().toString()))
      );

      for (int i = 0; i < this.upgradeButtons.size(); i++) {
         Purity prev = Purity.values()[i];
         Purity purity = prev.next();
         GemUpgradeMatch match = ((GemCaseMenu)this.menu).getUpgradeMatch(purity);
         SimpleTexButton button = this.upgradeButtons.get(i);
         if (match != null) {
            button.active = true;
            ItemStack leftMat = ((GemCaseMenu)this.menu).upgradeMatInv.getItem(match.leftSlot());
            ItemStack rightMat = ((GemCaseMenu)this.menu).upgradeMatInv.getItem(match.rightSlot());
            int leftCount = match.leftIng().count();
            int rightCount = match.rightIng().count();
            button.setTooltipProvider((btn, tooltip) -> {
               tooltip.accept(AscendantEquipment.lang("button", "gem_case.upgrade_cost", leftCount, leftMat.getHoverName(), rightCount, rightMat.getHoverName()));
               if (Screen.hasShiftDown()) {
                  tooltip.accept(AscendantEquipment.lang("button", "gem_case.upgrade_all").withStyle(ChatFormatting.YELLOW));
               }
            });
         } else {
            button.active = false;
            if (((GemCaseMenu)this.menu).getGemCount(this.getSelectedGem(), prev) < 2) {
               button.setInactiveMessage(AscendantEquipment.lang("button", "gem_case.upgrade_no_gems").withStyle(ChatFormatting.RED));
            } else {
               button.setInactiveMessage(AscendantEquipment.lang("button", "gem_case.upgrade_no_materials").withStyle(ChatFormatting.RED));
            }
         }
      }
   }

   private List<GemCaseScreen.SafeSlot> filter(List<GemCaseScreen.SafeSlot> list) {
      Iterator<GemCaseScreen.SafeSlot> iter = list.iterator();

      while (iter.hasNext()) {
         GemCaseScreen.SafeSlot slot = iter.next();
         if (!this.isAllowedByItem(slot) || !this.isAllowedBySearch(slot)) {
            iter.remove();
         }
      }

      return list;
   }

   private boolean isAllowedByItem(GemCaseScreen.SafeSlot slot) {
      ItemStack stack = ((GemCaseMenu)this.menu).ioInv.getItem(1);
      return stack.isEmpty() || slot.gem.getBonus(LootCategory.forItem(stack)) != null;
   }

   private boolean isAllowedBySearch(GemCaseScreen.SafeSlot slot) {
      String name = slot.displayStack.getDisplayName().getString().toLowerCase(Locale.ROOT);
      String search = this.filter == null ? "" : this.filter.getValue().trim().toLowerCase(Locale.ROOT);
      return Strings.isNullOrEmpty(search) || ChatFormatting.stripFormatting(name).contains(search);
   }

   private OnPress tryUpgrade(Purity purity) {
      return btn -> {
         boolean shift = Screen.hasShiftDown();
         int value = purity.ordinal() | (shift ? 0x1000 : 0x0000);
         PacketDistributor.sendToServer(new ButtonClickPayload(value));
      };
   }

   @Override
   protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
   }

   @Override
   public int getSlotColor(int index) {
      return 0x40FFFFFF;
   }

   public static void handleSelectedGem(DynamicHolder<Gem> gem) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen instanceof GemCaseScreen screen) {
         ((GemCaseMenu)screen.menu).setSelectedGem(gem);
      }
   }

   record SafeSlot(Gem gem, int count, ItemStack displayStack) {
   }
}
