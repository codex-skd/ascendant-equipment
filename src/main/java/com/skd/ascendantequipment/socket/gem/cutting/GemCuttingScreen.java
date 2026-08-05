package com.skd.ascendantequipment.socket.gem.cutting;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.SimpleTexButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class GemCuttingScreen extends AdventureContainerScreen<GemCuttingMenu> {
   public static final Identifier TEXTURE = AscendantEquipment.loc("textures/gui/gem_cutting.png");
   protected SimpleTexButton upgradeBtn;

   public GemCuttingScreen(GemCuttingMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
      super(pMenu, pPlayerInventory, pTitle);
      ((GemCuttingMenu)this.menu).slotChangedCallback = this::updateBtnStatus;
      this.imageHeight = 180;
      this.titleLabelY = 5;
      this.inventoryLabelY = 86;
   }

   protected void init() {
      super.init();
      int left = this.getLeftPos();
      int top = this.getTopPos();
      this.upgradeBtn = (SimpleTexButton)this.addRenderableWidget(
         new SimpleTexButton(
               left + 135, top + 44, 18, 18, 238, 0, TEXTURE, 256, 256, this::clickUpgradeBtn, Component.translatable("button.ascendant_equipment.upgrade")
            )
            .setInactiveMessage(Component.translatable("button.ascendant_equipment.upgrade.no").withStyle(ChatFormatting.RED))
      );
      this.updateBtnStatus();
   }

   protected void clickUpgradeBtn(Button btn) {
      this.minecraft.gameMode.handleInventoryButtonClick(((GemCuttingMenu)this.menu).containerId, 0);
      GemCuttingScreen.GemUpgradeSound.start(((GemCuttingMenu)this.menu).player.blockPosition());
   }

   protected void updateBtnStatus() {
      for (RecipeHolder<GemCuttingRecipe> holder : GemCuttingMenu.getRecipes(Minecraft.getInstance().level)) {
         GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
         if (r.matches(((GemCuttingMenu)this.menu).rInput, Minecraft.getInstance().level)) {
            this.upgradeBtn.active = true;
            return;
         }
      }

      if (this.upgradeBtn != null) {
         this.upgradeBtn.active = false;
      }
   }

   public void extractBackground(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY, float pPartialTick) {
      super.extractBackground(gfx, pMouseX, pMouseY, pPartialTick);
      int xCenter = (this.width - this.imageWidth) / 2;
      int yCenter = (this.height - this.imageHeight) / 2;
      gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xCenter, yCenter, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }

   protected static class GemUpgradeSound extends AbstractTickableSoundInstance {
      protected int ticks = 0;
      protected float pitchOff;

      public GemUpgradeSound(BlockPos pos) {
         super(SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, Minecraft.getInstance().level.getRandom());
         this.x = pos.getX() + 0.5F;
         this.y = pos.getY();
         this.z = pos.getZ() + 0.5F;
         this.volume = 1.5F;
         this.pitch = 1.5F + 0.35F * (1.0F - 2.0F * this.random.nextFloat());
         this.pitchOff = 0.35F * (1.0F - 2.0F * this.random.nextFloat());
         this.delay = 999;
      }

      public void tick() {
         if (this.ticks == 4 || this.ticks == 9) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_BREAK, this.pitch + this.pitchOff, 1.5F));
            this.pitchOff = -this.pitchOff;
         }

         if (this.ticks++ > 8) {
            this.stop();
         }
      }

      public static void start(BlockPos pos) {
         Minecraft.getInstance().getSoundManager().play(new GemCuttingScreen.GemUpgradeSound(pos));
      }
   }
}
