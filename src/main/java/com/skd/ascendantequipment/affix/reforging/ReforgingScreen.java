package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ReforgingScreen extends AdventureContainerScreen<ReforgingMenu> {
   public static final Identifier TEXTURE = AscendantEquipment.loc("textures/gui/reforge.png");
   public static final Identifier ANIMATED_TEXTURE = AscendantEquipment.loc("textures/gui/reforge_animation.png");
   public static final int MAX_ANIMATION_TIME = 8;
   protected boolean hasMainItem = false;
   protected int animationTick = 0;
   protected int maxSlot = -1;
   protected int opacityTick = 0;
   protected int availableOpacity = 170;

   public ReforgingScreen(ReforgingMenu menu, Inventory inv, Component title) {
      super(menu, inv, title, 176, 266);
   }

   public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(gfx, mouseX, mouseY, partialTick);
      int sigils = ((ReforgingMenu)this.menu).getSigilCount();
      int mats = ((ReforgingMenu)this.menu).getMatCount();
      int levels = ((ReforgingMenu)this.menu).player.experienceLevel;

      for (int idx = 0; idx < 3; idx++) {
         Slot slot = ((ReforgingMenu)this.getMenu()).getSlot(3 + idx);
         if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            ItemStack choice = slot.getItem();
            if (!choice.isEmpty()) {
               List<Component> tooltips = new ArrayList<>();
               int sigilCost = ((ReforgingMenu)this.menu).getSigilCost(idx);
               int matCost = ((ReforgingMenu)this.menu).getMatCost(idx);
               int levelCost = ((ReforgingMenu)this.menu).getLevelCost(idx);
               boolean creative = this.minecraft.player.isCreative();
               tooltips.add(
                  Component.translatable("text.ascendant_equipment.reforge_cost").withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.UNDERLINE})
               );
               tooltips.add(CommonComponents.EMPTY);
               if (sigilCost > 0) {
                  tooltips.add(
                     Component.translatable("%s %s", new Object[]{sigilCost, new ItemStack(AscEq.Items.SIGIL_OF_REBIRTH).getHoverName()})
                        .withStyle(!creative && sigils < sigilCost ? ChatFormatting.RED : ChatFormatting.GRAY)
                  );
               }

               if (matCost > 0) {
                  tooltips.add(
                     Component.translatable("%s %s", new Object[]{matCost, ((ReforgingMenu)this.menu).getSlot(1).getItem().getHoverName().getString()})
                        .withStyle(!creative && mats < matCost ? ChatFormatting.RED : ChatFormatting.GRAY)
                  );
               }

               String key = idx == 0 ? "container.enchant.level.one" : "container.enchant.level.many";
               tooltips.add(
                  Component.translatable(key, new Object[]{idx + 1}).withStyle(!creative && levels < levelCost ? ChatFormatting.RED : ChatFormatting.GRAY)
               );
               tooltips.add(Component.literal(" "));
               tooltips.add(
                  Component.translatable("container.enchant.level.requirement", new Object[]{levelCost})
                     .withStyle(!creative && levels < levelCost ? ChatFormatting.RED : ChatFormatting.GRAY)
               );
               this.drawOnLeft(gfx, tooltips, this.getTopPos() + 45);
               break;
            }
         }
      }
   }

   public void extractBackground(GuiGraphicsExtractor gfx, int x, int y, float partials) {
      super.extractBackground(gfx, x, y, partials);
      int left = this.getLeftPos();
      int top = this.getTopPos();
      int xCenter = (this.width - this.imageWidth) / 2;
      int yCenter = (this.height - this.imageHeight) / 2;
      gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xCenter, yCenter, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 384);

      for (int idx = 0; idx < 3; idx++) {
         if (this.maxSlot >= idx && this.animationTick == 0) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left + 20 + 46 * idx, top + 129, 20 + 46 * idx, 273.0F, 46, 35, 256, 384);
         }
      }

      boolean hadItem = this.hasMainItem;
      this.hasMainItem = ((ReforgingMenu)this.menu).getSlot(0).hasItem();
      if (!hadItem && this.hasMainItem) {
         this.animationTick = 8;
         this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AscEq.Sounds.REFORGE_ITEM_PLACED, 1.0F, 2.0F));
      }

      if (this.hasMainItem) {
         float delta = Mth.clamp((8.0F - this.animationTick) / 8.0F, 0.0F, 1.0F);
         int frame = Mth.lerpInt(delta, 0, 19);
         gfx.blit(RenderPipelines.GUI_TEXTURED, ANIMATED_TEXTURE, left + 26, top + 15, 0.0F, frame * 112, 127, 112, 127, 2240);
      }

      int sigils = ((ReforgingMenu)this.menu).getSigilCount();
      int mats = ((ReforgingMenu)this.menu).getMatCount();
      int levels = ((ReforgingMenu)this.menu).player.experienceLevel;
      this.maxSlot = -1;

      for (int idx = 0; idx < 3; idx++) {
         Slot slot = ((ReforgingMenu)this.getMenu()).getSlot(3 + idx);
         if (!slot.hasItem()) {
            break;
         }

         int sigilCost = ((ReforgingMenu)this.menu).getSigilCost(idx);
         int matCost = ((ReforgingMenu)this.menu).getMatCost(idx);
         int levelCost = ((ReforgingMenu)this.menu).getLevelCost(idx);
         if (sigils >= sigilCost && levels >= levelCost && mats >= matCost || this.minecraft.player.getAbilities().instabuild) {
            this.maxSlot++;
         }
      }
   }

   protected int darken(int rColor, int factor) {
      int r = rColor >> 16 & 0xFF;
      int g = rColor >> 8 & 0xFF;
      int b = rColor & 0xFF;
      r /= factor;
      g /= factor;
      b /= factor;
      return r << 16 | g << 8 | b;
   }

   protected void drawBorderedString(GuiGraphicsExtractor gfx, String str, int x, int y, int color, int shadowColor) {
      Component comp = Component.literal(str);
      gfx.text(this.font, comp, x, y - 1, shadowColor, false);
      gfx.text(this.font, comp, x - 1, y, shadowColor, false);
      gfx.text(this.font, comp, x, y + 1, shadowColor, false);
      gfx.text(this.font, comp, x + 1, y, shadowColor, false);
      gfx.text(this.font, comp, x, y, color, false);
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;

      for (int k = 0; k < 3; k++) {
         double d0 = event.x() - (i + 60);
         double d1 = event.y() - (j + 14 + 19 * k);
         if (d0 >= 0.0 && d1 >= 0.0 && d0 < 108.0 && d1 < 19.0 && ((ReforgingMenu)this.menu).clickMenuButton(this.minecraft.player, k)) {
            this.minecraft.gameMode.handleInventoryButtonClick(((ReforgingMenu)this.menu).containerId, k);
            return true;
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   protected void containerTick() {
      this.opacityTick++;
      if (this.animationTick > 0) {
         this.animationTick--;
         if (this.animationTick == 0) {
            this.opacityTick = 0;
         }
      }

      float sin = Mth.sin(this.opacityTick / 60.0F * (float) Math.PI);
      float delta = sin * sin;
      this.availableOpacity = Mth.lerpInt(delta, 136, 221);
   }

   protected void extractSlot(GuiGraphicsExtractor gfx, Slot slot, int mouseX, int mouseY) {
      if (slot instanceof ReforgingMenu.ReforgingResultSlot) {
         if (this.animationTick == 0) {
            int alpha = this.maxSlot >= slot.getContainerSlot() ? this.availableOpacity : 64;
            PipelinedRenderer.ghostFakeItem(gfx, slot.getItem(), slot.x, slot.y, alpha / 255.0F);
         }
      } else {
         super.extractSlot(gfx, slot, mouseX, mouseY);
      }
   }
}
