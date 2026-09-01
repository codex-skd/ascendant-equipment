package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscendantEquipment;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class WorldTierTutorialScreen extends Screen {
   private final WorldTierSelectScreen parent;
   private WorldTierTutorialScreen.TutorialStage stage = WorldTierTutorialScreen.TutorialStage.INTRODUCTION;
   private SimpleTexButton skipButton;
   private SimpleTexButton prevButton;
   private SimpleTexButton nextButton;

   public WorldTierTutorialScreen(WorldTierSelectScreen parent, Component title) {
      super(title);
      this.parent = parent;
   }

   protected void init() {
      super.init();
      int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
      int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;
      this.skipButton = (SimpleTexButton)this.addRenderableWidget(
         SimpleTexButton.builder()
            .size(80, 20)
            .pos(imgLeft + 15, imgTop + 250)
            .texture(SimpleTexButton.ASC_EQ_SPRITES)
            .action(btn -> this.closeTutorial())
            .buttonText(AscendantEquipment.lang("button", "skip_tutorial"))
            .build()
      );
      this.prevButton = (SimpleTexButton)this.addRenderableWidget(
         SimpleTexButton.builder().size(60, 20).pos(imgLeft + 340, imgTop + 250).texture(SimpleTexButton.ASC_EQ_SPRITES).action(btn -> {
            this.stage = this.stage.prev();
            this.updateButtons();
         }).buttonText(AscendantEquipment.lang("button", "prev_tutorial")).build()
      );
      this.nextButton = (SimpleTexButton)this.addRenderableWidget(
         SimpleTexButton.builder().size(60, 20).pos(imgLeft + 420, imgTop + 250).texture(SimpleTexButton.ASC_EQ_SPRITES).action(btn -> {
            this.stage = this.stage.next();
            this.updateButtons();
            if (this.stage == null) {
               this.closeTutorial();
            }
         }).buttonText(AscendantEquipment.lang("button", "next_tutorial")).build()
      );
      this.updateButtons();
   }

   public void renderBg(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
      int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;
      gfx.blit(this.stage.overlay, imgLeft, imgTop, 0, 0, 0, WorldTierSelectScreen.IMAGE_WIDTH, WorldTierSelectScreen.IMAGE_HEIGHT, WorldTierSelectScreen.IMAGE_WIDTH, WorldTierSelectScreen.IMAGE_HEIGHT);
   }

   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      super.render(gfx, mouseX, mouseY, partialTick);
      int imgLeft = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2;
      int imgTop = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2;
      gfx.pose().pushPose();
      float scale = 2.0F;
      gfx.pose().scale(scale, scale, 1.0F);
      Component title = this.stage.title;
      gfx.drawString(
         this.font, title.getVisualOrderText(), (int)((imgLeft + 380 - this.font.width(title) * scale / 2.0F) / scale), (int)((imgTop + 107) / scale), 0xFFFFFF, true
      );
      gfx.pose().popPose();
      Component desc = this.stage.description;
      if (this.stage == WorldTierTutorialScreen.TutorialStage.ACTIVATE && !EquipmentConfig.enableManualWorldTierChanges) {
         desc = AscendantEquipment.lang("tutorial", "world_tier.activate_disabled.desc").withStyle(ChatFormatting.DARK_AQUA);
      }

      List<FormattedCharSequence> split = this.font.split(desc, 200);

      for (int i = 0; i < split.size(); i++) {
         FormattedCharSequence line = split.get(i);
         gfx.drawString(this.font, line, imgLeft + 280, imgTop + 100 + this.font.lineHeight * 3 + (2 + this.font.lineHeight) * i, 0xFFFFFF, true);
      }

      if (this.stage == WorldTierTutorialScreen.TutorialStage.WORLD_TIERS) {
         for (SimpleTexButton btn : this.parent.tierButtons.values()) {
            btn.render(gfx, mouseX, mouseY, partialTick);
         }
      } else if (this.stage == WorldTierTutorialScreen.TutorialStage.DETAILED_INFO) {
         this.parent.detailButton.render(gfx, mouseX, mouseY, partialTick);
      } else if (this.stage == WorldTierTutorialScreen.TutorialStage.ACTIVATE) {
         this.parent.activateButton.render(gfx, mouseX, mouseY, partialTick);
      }
   }

   private void updateButtons() {
      this.skipButton.active = true;
      this.prevButton.active = this.stage != WorldTierTutorialScreen.TutorialStage.INTRODUCTION;
      if (this.stage == WorldTierTutorialScreen.TutorialStage.ACTIVATE) {
         this.nextButton.setButtonText(AscendantEquipment.lang("button", "done"));
      } else {
         this.nextButton.setButtonText(AscendantEquipment.lang("button", "next_tutorial"));
      }
   }

   private void closeTutorial() {
       this.minecraft.setScreen(this.parent);
      this.parent.closeTutorial();
   }

   private enum TutorialStage {
      INTRODUCTION("introduction"),
      WORLD_TIERS("world_tiers"),
      TIER_NAME("tier_name"),
      TIER_DIFFICULTY("tier_difficulty"),
      DETAILED_INFO("detailed_info"),
      ACTIVATE("activate");

      private final ResourceLocation overlay;
      private final Component title;
      private final Component description;

      TutorialStage(String name) {
         this.overlay = AscendantEquipment.loc("textures/gui/tutorial/" + name + ".png");
         this.title = AscendantEquipment.lang("tutorial", "world_tier." + name + ".title");
         this.description = AscendantEquipment.lang("tutorial", "world_tier." + name + ".desc");
      }

      @Nullable
      public WorldTierTutorialScreen.TutorialStage next() {
         return switch (this) {
            case INTRODUCTION -> WORLD_TIERS;
            case WORLD_TIERS -> TIER_NAME;
            case TIER_NAME -> TIER_DIFFICULTY;
            case TIER_DIFFICULTY -> DETAILED_INFO;
            case DETAILED_INFO -> ACTIVATE;
            case ACTIVATE -> null;
         };
      }

      @Nullable
      public WorldTierTutorialScreen.TutorialStage prev() {
         return switch (this) {
            case INTRODUCTION -> null;
            case WORLD_TIERS -> INTRODUCTION;
            case TIER_NAME -> WORLD_TIERS;
            case TIER_DIFFICULTY -> TIER_NAME;
            case DETAILED_INFO -> TIER_DIFFICULTY;
            case ACTIVATE -> DETAILED_INFO;
         };
      }
   }
}
