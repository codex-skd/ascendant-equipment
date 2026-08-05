package com.skd.ascendantequipment.affix.augmenting;

import com.google.common.collect.Lists;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.DropDownList;
import com.skd.ascendantequipment.client.SimpleTexButton;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AugmentingScreen extends AdventureContainerScreen<AugmentingMenu> {
   public static final Identifier TEXTURE = AscendantEquipment.loc("textures/gui/augmenting.png");
   public static final int ALTERNATIVE_TEXT_WIDTH = 150;
   public static final int ALTERNATIVE_MAX_LINES = 15;
   protected ItemStack lastMainItem = ItemStack.EMPTY;
   protected int lastSelection = -1;
   protected List<AffixInstance> currentItemAffixes = Collections.emptyList();
   protected int alternativePage = -1;
   protected List<List<FormattedText>> alternativePages = Collections.emptyList();
   protected int alternativeXPos = 0;
   protected int alternativeWidth = 0;
   protected AugmentingScreen.AffixDropList list;
   protected SimpleTexButton upgradeBtn;
   protected SimpleTexButton rerollBtn;
   protected final AttributeTooltipContext tooltipCtx;

   public AugmentingScreen(AugmentingMenu menu, Inventory inv, Component pTitle) {
      super(menu, inv, pTitle, 176, 222);
      this.tooltipCtx = AttributeTooltipContext.of(
         inv.player, TooltipContext.of(inv.player.level()), TooltipDisplay.DEFAULT, AscendantAttributes.getTooltipFlag()
      );
   }

   protected void init() {
      super.init();
      int left = this.getLeftPos();
      int top = this.getTopPos();
      int selected = this.getSelectedAffix();
      this.upgradeBtn = (SimpleTexButton)this.addRenderableWidget(new AugmentingScreen.FatTexButton(left + 60, top + 111, 29, 13, 186, 135, btn -> {
         if (this.getSelectedAffix() != -1) {
            this.minecraft.gameMode.handleInventoryButtonClick(((AugmentingMenu)this.menu).containerId, 0 | this.getSelectedAffix() << 1);
         }
      }, Component.translatable("button.ascendant_equipment.augmenting.upgrade")));
      this.rerollBtn = (SimpleTexButton)this.addRenderableWidget(new AugmentingScreen.FatTexButton(left + 112, top + 111, 29, 13, 223, 135, btn -> {
         if (this.getSelectedAffix() != -1) {
            this.minecraft.gameMode.handleInventoryButtonClick(((AugmentingMenu)this.menu).containerId, 1 | this.getSelectedAffix() << 1);
         }
      }, Component.translatable("button.ascendant_equipment.augmenting.reroll")));
      this.list = (AugmentingScreen.AffixDropList)this.addRenderableWidget(
         new AugmentingScreen.AffixDropList(left + 39, top + 17, 123, 14, Component.empty(), this.currentItemAffixes, 6)
      );
      this.list.setSelected(selected);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
         ? true
         : this.getChildAt(mouseX, mouseY).filter(child -> child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
   }

   public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(gfx, mouseX, mouseY, partialTick);
      this.updateCachedState();
      int left = this.getLeftPos();
      int top = this.getTopPos();
      int xCenter = (this.width - this.imageWidth) / 2;
      int yCenter = (this.height - this.imageHeight) / 2;
      gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xCenter, yCenter, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 307);
      int selected = this.getSelectedAffix();
      if (selected != -1 && !this.list.isOpen()) {
         AffixInstance inst = this.currentItemAffixes.get(selected);
         Component comp = inst.getAugmentingText(this.tooltipCtx);
         List<FormattedCharSequence> split = this.font.split(comp, 117);
         drawPanel(gfx, left + 42, top + 39, 117, 65, -267386864, -13220529);

         for (int i = 0; i < split.size(); i++) {
            gfx.text(this.font, split.get(i), left + 43, top + 40 + i * 11, 0xFF000000 | TextColor.YELLOW.getValue(), true);
         }
      } else {
         drawPanel(gfx, left + 42, top + 39, 117, 65, -1441787888, -1439283889);
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(gfx, mouseX, mouseY, partialTick);
      int selected = this.getSelectedAffix();
      if (selected != -1 && this.rerollBtn.isHovered() && this.rerollBtn.isActive() && this.alternativePage != -1) {
         List<FormattedText> page = this.alternativePages.get(this.alternativePage);
         List<ClientTooltipComponent> list = page.stream()
            .map(Language.getInstance()::getVisualOrder)
            .<ClientTooltipComponent>map(ClientTooltipComponent::create)
            .collect(Collectors.toCollection(Lists::newArrayList));
         if (this.alternativePages.size() > 1) {
            list.set(list.size() - 2, new AugmentingScreen.FakeWidthComponent(this.alternativeWidth));
         }

         gfx.tooltip(this.font, list, this.alternativeXPos, this.getTopPos() + 33, DefaultTooltipPositioner.INSTANCE, null);
      }

      if (selected != -1 && this.upgradeBtn.isActive() && this.upgradeBtn.isHovered()) {
         AffixInstance inst = this.currentItemAffixes.get(selected);
         AffixInstance upgraded = new AffixInstance(inst.affix(), Math.min(1.0F, inst.level() + 0.25F), inst.rarity(), inst.stack());
         List<Component> altText = new ArrayList<>();
         altText.add(Component.translatable("text.ascendant_equipment.upgraded_form").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.UNDERLINE}));
         altText.add(Component.translatable("%s", new Object[]{upgraded.getAugmentingText(this.tooltipCtx)}).withStyle(ChatFormatting.YELLOW));
         this.drawOnLeft(gfx, altText, this.getTopPos() + 33, 150);
      }
   }

   private static void drawPanel(GuiGraphicsExtractor gfx, int x, int y, int w, int h, int bgColor, int borderColor) {
      gfx.fill(x, y, x + w, y + h, bgColor);
      gfx.fill(x - 1, y, x, y + h, borderColor);
      gfx.fill(x + w, y, x + w + 1, y + h, borderColor);
      gfx.fill(x, y - 1, x + w, y, borderColor);
      gfx.fill(x, y + h, x + w, y + h + 1, borderColor);
   }

   protected void updateCachedState() {
      ItemStack mainItem = ((AugmentingMenu)this.menu).getMainItem();
      if (!ItemStack.isSameItemSameComponents(mainItem, this.lastMainItem)) {
         List<AffixInstance> newAffixes = AugmentingMenu.computeItemAffixes(mainItem);
         if (ItemStack.isSameItem(mainItem, this.lastMainItem) && this.currentItemAffixes.size() == newAffixes.size()) {
            this.list.setEntries(newAffixes);
            this.list.setSelected(this.lastSelection);
         } else {
            this.list.setEntries(newAffixes);
         }

         this.currentItemAffixes = newAffixes;
         this.lastMainItem = mainItem.copy();
         this.computeAlternatives(this.list.getSelected());
      }

      if (this.lastSelection != this.list.getSelected()) {
         this.lastSelection = this.list.getSelected();
         this.computeAlternatives(this.lastSelection);
      }

      int selected = this.getSelectedAffix();
      if (selected == -1) {
         Component comp = Component.translatable("button.ascendant_equipment.augmenting.no_selection").withStyle(ChatFormatting.RED);
         this.upgradeBtn.active = false;
         this.upgradeBtn.setInactiveMessage(comp);
         this.rerollBtn.active = false;
         this.rerollBtn.setInactiveMessage(comp);
      } else {
         this.upgradeBtn.active = true;
         this.rerollBtn.active = true;
         AffixInstance current = this.currentItemAffixes.get(selected);
         if (!AugmentingMenu.canAugment(current)) {
            this.upgradeBtn.active = false;
            this.upgradeBtn.setInactiveMessage(Component.translatable("button.ascendant_equipment.augmenting.max_level").withStyle(ChatFormatting.RED));
         }

         if (this.alternativePages.isEmpty()) {
            this.rerollBtn.active = false;
            this.rerollBtn.setInactiveMessage(Component.translatable("button.ascendant_equipment.augmenting.no_alternatives").withStyle(ChatFormatting.RED));
         }

         if (this.upgradeBtn.isActive() && !((AugmentingMenu)this.menu).hasUpgradeCost() && !((AugmentingMenu)this.menu).player.isCreative()) {
            this.upgradeBtn.active = false;
            this.upgradeBtn.setInactiveMessage(CommonComponents.EMPTY);
         }

         if (this.rerollBtn.isActive() && !((AugmentingMenu)this.menu).hasRerollCost() && !((AugmentingMenu)this.menu).player.isCreative()) {
            this.rerollBtn.active = false;
            this.rerollBtn.setInactiveMessage(CommonComponents.EMPTY);
         }
      }
   }

   protected void computeAlternatives(int selected) {
      if (selected == -1) {
         this.alternativePages = Collections.emptyList();
         this.alternativePage = -1;
      } else {
         AffixInstance current = this.currentItemAffixes.get(selected);
         List<DynamicHolder<Affix>> alternatives = LootController.getAlternativeAffixes(
               Minecraft.getInstance().player, this.lastMainItem, current.getRarity(), current.affix()
            )
            .toList();
         if (alternatives.isEmpty()) {
            this.alternativePages = Collections.emptyList();
            this.alternativePage = -1;
         } else {
            StringSplitter splitter = this.font.getSplitter();
            int maxWidth = 0;
            Component heading = Component.translatable("text.ascendant_equipment.potential_rerolls")
               .withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.UNDERLINE});
            List<List<FormattedText>> pages = new ArrayList<>();
            List<FormattedText> page = new ArrayList<>();
            page.add(heading);
            boolean first = true;

            for (DynamicHolder<Affix> afx : alternatives) {
               AffixInstance inst = new AffixInstance(afx, current.level(), current.rarity(), current.stack());
               Component augTxt = inst.getAugmentingText(this.tooltipCtx);
               List<FormattedText> split = splitter.splitLines(
                  Component.translatable("%s", new Object[]{augTxt}).withStyle(ChatFormatting.YELLOW), 150, augTxt.getStyle()
               );
               maxWidth = Math.max(maxWidth, split.stream().<Integer>map(this.font::width).max(Integer::compare).get());
               if (page.size() + split.size() + 1 > 15) {
                  pages.add(page);
                  page = new ArrayList<>();
                  page.add(heading);
                  page.addAll(split);
               } else {
                  if (!first) {
                     page.add(CommonComponents.SPACE);
                  }

                  page.addAll(split);
                  first = false;
               }

               if (afx == alternatives.get(alternatives.size() - 1)) {
                  pages.add(page);
               }
            }

            this.alternativePage = 0;
            this.alternativePages = pages;
            this.alternativeXPos = this.getLeftPos() - 16 - maxWidth;
            this.alternativeWidth = maxWidth;
            maxWidth = this.alternativePages.size();
            if (maxWidth > 1) {
               for (int i = 0; i < maxWidth; i++) {
                  page = this.alternativePages.get(i);
                  page.add(CommonComponents.SPACE);
                  page.add(Component.translatable("text.ascendant_equipment.alternative_page", new Object[]{i + 1, maxWidth}).withStyle(ChatFormatting.DARK_GRAY));
               }
            }
         }
      }
   }

   protected int getSelectedAffix() {
      return this.list == null ? -1 : this.list.getSelected();
   }

   public static void handleRerollResult(DynamicHolder<Affix> newAffix) {
      if (Minecraft.getInstance().gui.screen() instanceof AugmentingScreen scn) {
         scn.updateCachedState();

         for (int i = 0; i < scn.currentItemAffixes.size(); i++) {
            AffixInstance inst = scn.currentItemAffixes.get(i);
            if (inst.affix().equals(newAffix)) {
               scn.list.setSelected(i);
               return;
            }
         }
      }
   }

   public class AffixDropList extends DropDownList<AffixInstance> {
      public AffixDropList(int x, int y, int width, int height, Component narrationMsg, List<AffixInstance> entries, int maxDisplayedEntries) {
         super(x, y, width, height, narrationMsg, entries, maxDisplayedEntries);
      }

      @Override
      protected void extractWidgetRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
         if (this.entries.isEmpty()) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, AugmentingScreen.TEXTURE, this.getX(), this.getY(), 0.0F, 267.0F, this.width, this.baseHeight, 256, 307);
         }

         super.extractWidgetRenderState(gfx, mouseX, mouseY, partialTick);
         int hovered = this.getHoveredSlot(mouseX, mouseY);
         if (this.isOpen && hovered != -1) {
            AffixInstance inst = this.entries.get(hovered);
            List<Component> list = new ArrayList<>();
            list.add(inst.getName(true).copy().withStyle(Style.EMPTY.withColor(16777088).withUnderlined(true)));
            list.add(Component.translatable("%s", new Object[]{inst.getAugmentingText(AugmentingScreen.this.tooltipCtx)}).withStyle(ChatFormatting.YELLOW));
            AugmentingScreen.this.drawOnLeft(gfx, list, AugmentingScreen.this.getTopPos() + 33, 150);
         }

         gfx.blit(
            RenderPipelines.GUI_TEXTURED,
            AugmentingScreen.TEXTURE,
            this.getX() + this.width - 15,
            this.getY(),
            123 + (this.isOpen ? 15 : 0),
            239.0F,
            15,
            14,
            256,
            307
         );
      }

      protected void renderEntry(GuiGraphicsExtractor gfx, int x, int y, int mouseX, int mouseY, AffixInstance entry) {
         int hovered = this.getHoveredSlot(mouseX, mouseY);
         int idx = this.entries.indexOf(entry);
         gfx.blit(
            RenderPipelines.GUI_TEXTURED,
            AugmentingScreen.TEXTURE,
            x,
            y,
            0.0F,
            239 + (hovered == idx ? this.baseHeight : 0),
            this.width,
            this.baseHeight,
            256,
            307
         );
         Component name = entry.getName(true);
         if (!AugmentingMenu.canAugment(entry)) {
            name = ApothMiscUtil.starPrefix(name);
         }

         gfx.text(AugmentingScreen.this.font, name, x + 2, y + 3, -128, false);
      }
   }

   public record FakeWidthComponent(int width) implements ClientTooltipComponent {
      public int getHeight(Font font) {
         return 9;
      }

      public int getWidth(Font font) {
         return this.width;
      }
   }

   public class FatTexButton extends SimpleTexButton {
      private final Component sigilName = Component.translatable("item.ascendant_equipment.sigil_of_enhancement").withStyle(ChatFormatting.YELLOW);

      public FatTexButton(int x, int y, int width, int height, int u, int v, OnPress press, Component message) {
         super(x, y, width, height, u, v, AugmentingScreen.TEXTURE, 256, 307, press, message);
      }

      @Override
      public void extractContents(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY, float pPartialTick) {
         int yTex = this.yTexStart - 2;
         if (!this.isActive()) {
            yTex += this.height + 4;
         } else if (this.isHovered()) {
            yTex += (this.height + 4) * 2;
         }

         gfx.blit(
            RenderPipelines.GUI_TEXTURED,
            (Identifier)this.texture.left().orElseThrow(),
            this.getX() - 2,
            this.getY() - 2,
            this.xTexStart - 2,
            yTex,
            this.width + 4,
            this.height + 4,
            this.textureWidth,
            this.textureHeight
         );
         if (this.isHovered()) {
            this.renderToolTip(gfx, pMouseX, pMouseY);
         }
      }

      @Override
      public void renderToolTip(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY) {
         if (this.getMessage() != CommonComponents.EMPTY && this.isHovered()) {
            Component primary = this.getMessage();
            if (!this.active) {
               primary = primary.copy().withStyle(ChatFormatting.GRAY);
            }

            int sigilCost = this == AugmentingScreen.this.rerollBtn ? EquipmentConfig.rerollSigilCost : EquipmentConfig.upgradeSigilCost;
            int levelCost = this == AugmentingScreen.this.rerollBtn ? EquipmentConfig.rerollLevelCost : EquipmentConfig.upgradeLevelCost;
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(primary);
            MutableComponent sigilCostMsg = AscendantEquipment.lang("button", "augmenting.upgrade.cost", sigilCost, this.sigilName);
            MutableComponent levelCostMsg = AscendantEquipment.lang("button", "augmenting.upgrade.exp_cost", levelCost);
            if (this.isActive()) {
               tooltips.add(sigilCostMsg);
               tooltips.add(levelCostMsg);
            } else if (!this.inactiveMessage.isEmpty()) {
               tooltips.addAll(this.inactiveMessage);
            } else {
               if (((AugmentingMenu)AugmentingScreen.this.menu).getSigils().getCount() < sigilCost) {
                  sigilCostMsg.withStyle(ChatFormatting.RED);
               } else {
                  sigilCostMsg.withStyle(ChatFormatting.GRAY);
               }

               if (Minecraft.getInstance().player.experienceLevel < levelCost) {
                  levelCostMsg.withStyle(ChatFormatting.RED);
               } else {
                  levelCostMsg.withStyle(ChatFormatting.GRAY);
               }

               tooltips.add(sigilCostMsg);
               tooltips.add(levelCostMsg);
            }

            gfx.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltips, pMouseX, pMouseY);
         }
      }

      public boolean mouseScrolled(double pMouseX, double pMouseY, double scrollX, double scrollY) {
         if (this == AugmentingScreen.this.rerollBtn && this.isActive() && this.isHovered()) {
            int change = scrollY < 0.0 ? 1 : -1;
            int page = AugmentingScreen.this.alternativePage;
            page = Math.floorMod(page + change, AugmentingScreen.this.alternativePages.size());
            AugmentingScreen.this.alternativePage = page;
            return true;
         } else {
            return super.mouseScrolled(pMouseX, pMouseY, scrollX, scrollY);
         }
      }
   }
}
