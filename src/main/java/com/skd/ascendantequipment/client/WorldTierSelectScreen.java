package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.net.WorldTierPayload;
import com.skd.ascendantequipment.tiers.WorldTier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class WorldTierSelectScreen extends Screen {
   public static final ResourceLocation TEXTURE = AscendantEquipment.loc("textures/gui/mountain.png");
   public static final ResourceLocation SEPARATOR_LINE = AscendantEquipment.loc("textures/gui/separator_line.png");
   public static final ResourceLocation SWORD_EMPTY = AscendantEquipment.loc("textures/gui/sword_empty.png");
   public static final ResourceLocation SWORD_FULL = AscendantEquipment.loc("textures/gui/sword_full.png");
   public static final WorldTierSelectScreen.AnimationData HAVEN_ANIMATION = new WorldTierSelectScreen.AnimationData(
      138, 156, 21, 320, 10, AscendantEquipment.loc("textures/gui/animations/haven.png")
   );
   public static final WorldTierSelectScreen.AnimationData FRONTIER_ANIMATION = new WorldTierSelectScreen.AnimationData(
      210, 236, 45, 588, 21, AscendantEquipment.loc("textures/gui/animations/frontier.png")
   );
   public static final WorldTierSelectScreen.AnimationData ASCENT_ANIMATION = new WorldTierSelectScreen.AnimationData(
      251, 106, 42, 1380, 30, AscendantEquipment.loc("textures/gui/animations/ascent.png")
   );
   public static final WorldTierSelectScreen.AnimationData SUMMIT_ANIMATION = new WorldTierSelectScreen.AnimationData(
      349, 41, 5, 640, 20, AscendantEquipment.loc("textures/gui/animations/summit.png")
   );
   public static final WorldTierSelectScreen.AnimationData PINNACLE_ANIMATION = new WorldTierSelectScreen.AnimationData(
      356, -2, 47, 960, 12, AscendantEquipment.loc("textures/gui/animations/pinnacle.png")
   );
   public static final int GUI_WIDTH = 480;
   public static final int GUI_HEIGHT = 270;
   public static final int IMAGE_WIDTH = 498;
   public static final int IMAGE_HEIGHT = 286;
   protected SimpleTexButton activateButton;
   protected SimpleTexButton detailButton;
   protected SimpleTexButton tutorialButton;
   protected WorldTier displayedTier = WorldTier.getTier(Minecraft.getInstance().player);
   protected int leftPos;
   protected int topPos;
   protected Map<WorldTier, SimpleTexButton> tierButtons = new EnumMap<>(WorldTier.class);
   protected int animTicks = 0;

   public WorldTierSelectScreen() {
      super(AscendantEquipment.lang("title", "select_world_tier"));
   }

   @Override
   public void init() {
      super.init();
      this.leftPos = Math.max(0, (this.width - 480) / 2);
      this.topPos = Math.max(0, (this.height - 270) / 2);
      if (this.tierButtons.isEmpty()) {
         this.addTierButton(WorldTier.HAVEN, b -> b.pos(this.leftPos + 100, this.topPos + 215));
         this.addTierButton(WorldTier.FRONTIER, b -> b.pos(this.leftPos + 210, this.topPos + 205));
         this.addTierButton(WorldTier.ASCENT, b -> b.pos(this.leftPos + 230, this.topPos + 115));
         this.addTierButton(WorldTier.SUMMIT, b -> b.pos(this.leftPos + 315, this.topPos + 60));
         this.addTierButton(WorldTier.PINNACLE, b -> b.pos(this.leftPos + 395, this.topPos));
         this.activateButton = (SimpleTexButton)this.addRenderableWidget(
            SimpleTexButton.builder()
               .size(60, 24)
               .pos(this.leftPos + 198, this.topPos + 15)
               .texture(SimpleTexButton.ASC_EQ_SPRITES)
               .action(this.activateSelectedTier())
               .buttonText(AscendantEquipment.lang("button", "activate_tier"))
               .build()
         );
         this.detailButton = (SimpleTexButton)this.addRenderableWidget(
            SimpleTexButton.builder()
               .size(80, 20)
               .pos(this.leftPos + 178, this.topPos + 75)
               .texture(SimpleTexButton.ASC_EQ_SPRITES)
               .action(this.openDetailedInfoScreen())
               .buttonText(AscendantEquipment.lang("button", "show_detailed_info"))
               .message(AscendantEquipment.lang("button", "show_detailed_info.desc"))
               .build()
         );
         this.tutorialButton = (SimpleTexButton)this.addRenderableWidget(
            SimpleTexButton.builder()
               .size(12, 15)
               .pos(this.leftPos + 480 - 14, this.topPos + 270 - 17)
               .texture(SimpleTexButton.ASC_EQ_SPRITES)
                .action(btn -> Minecraft.getInstance().setScreen(new WorldTierTutorialScreen(this, AscendantEquipment.lang("title", "world_tier_tutorial"))))
               .buttonText(Component.literal("?"))
               .message(AscendantEquipment.lang("button", "open_world_tier_tutorial"))
               .build()
         );
      } else {
         this.tierButtons.get(WorldTier.HAVEN).setPosition(this.leftPos + 100, this.topPos + 215);
         this.tierButtons.get(WorldTier.FRONTIER).setPosition(this.leftPos + 210, this.topPos + 205);
         this.tierButtons.get(WorldTier.ASCENT).setPosition(this.leftPos + 230, this.topPos + 115);
         this.tierButtons.get(WorldTier.SUMMIT).setPosition(this.leftPos + 315, this.topPos + 60);
         this.tierButtons.get(WorldTier.PINNACLE).setPosition(this.leftPos + 395, this.topPos);
         this.activateButton.setPosition(this.leftPos + 198, this.topPos + 15);
         this.detailButton.setPosition(this.leftPos + 178, this.topPos + 75);
         this.tutorialButton.setPosition(this.leftPos + 480 - 14, this.topPos + 270 - 17);
      }
      this.updateButtonStatus();
       if (this.minecraft.screen == this && WorldTier.isTutorialActive(this.minecraft.player) && WorldTier.isUnlocked(this.minecraft.player, WorldTier.HAVEN)) {
          Minecraft.getInstance().setScreen(new WorldTierTutorialScreen(this, AscendantEquipment.lang("title", "world_tier_tutorial")));
      }
   }

    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
       super.renderBackground(gfx, mouseX, mouseY, partialTick);
      int imgLeft = (this.width - 498) / 2;
      int imgTop = (this.height - 286) / 2;
      gfx.blit(TEXTURE, imgLeft, imgTop, 0, 0, 0, 498, 286, 498, 286);
      gfx.blit(SEPARATOR_LINE, this.leftPos, this.topPos + 50, 0, 0, 0, 275, 30, 275, 30);
      gfx.pose().pushPose();
      float scale = 0.5F;
      gfx.pose().scale(scale, scale, 1.0F);
      Component diffText = AscendantEquipment.lang("text", "world_tier.difficulty").withStyle(new ChatFormatting[]{ChatFormatting.BOLD, ChatFormatting.RED});

      for (int i = 0; i < 5; i++) {
         ResourceLocation tex = this.displayedTier.ordinal() >= i ? SWORD_FULL : SWORD_EMPTY;
         int swordLeft = this.leftPos + this.font.width(diffText) + 20 + i * (int)(30.0F * scale);
         gfx.blit(tex, (int)(swordLeft / scale), (int)((this.topPos + 77) / scale), 0, 0, 0, 30, 30, 30, 30);
      }

      gfx.pose().popPose();

      WorldTierSelectScreen.AnimationData anim = switch (this.displayedTier) {
         case HAVEN -> HAVEN_ANIMATION;
         case FRONTIER -> FRONTIER_ANIMATION;
         case ASCENT -> ASCENT_ANIMATION;
         case SUMMIT -> SUMMIT_ANIMATION;
         case PINNACLE -> PINNACLE_ANIMATION;
      };
      anim.render(gfx, this.leftPos, this.topPos, this.animTicks, partialTick);
   }

   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      super.render(gfx, mouseX, mouseY, partialTick);
      gfx.pose().pushPose();
      float scale = 3.0F;
      gfx.pose().scale(scale, scale, 1.0F);
      Component title = AscendantEquipment.lang("text", "world_tier." + this.displayedTier.getSerializedName());
      gfx.drawString(this.font, title.getVisualOrderText(), (int)((this.leftPos + 15) / scale), (int)((this.topPos + 15) / scale), 0xFFFFFF, true);
      gfx.pose().popPose();
      Component desc = AscendantEquipment.lang("text", "world_tier." + this.displayedTier.getSerializedName() + ".desc");
      gfx.drawString(this.font, desc, this.leftPos + 15, this.topPos + 45, 0xC8C86E);
      Component diffText = AscendantEquipment.lang("text", "world_tier.difficulty").withStyle(new ChatFormatting[]{ChatFormatting.BOLD, ChatFormatting.RED});
      gfx.drawString(this.font, diffText.getVisualOrderText(), this.leftPos + 15, this.topPos + 80, 0xFFFFFF, true);
   }

   public void tick() {
      this.animTicks++;
   }

   protected OnPress displayTier(WorldTier tier) {
      return btn -> {
         this.displayedTier = tier;
         this.updateButtonStatus();
         this.animTicks = 0;
      };
   }

   private OnPress activateSelectedTier() {
      return btn -> {
         WorldTier tier = this.displayedTier;
         if (WorldTier.getTier(Minecraft.getInstance().player) != tier || WorldTier.isTutorialActive(Minecraft.getInstance().player)) {
            PacketDistributor.sendToServer(new WorldTierPayload(tier));
            this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
         }

         btn.active = false;
         this.activateButton.setButtonText(AscendantEquipment.lang("button", "activated").withColor(0x9A669C));
         this.activateButton.setMessage(AscendantEquipment.lang("button", "already_activated").withStyle(ChatFormatting.RED));
      };
   }

   protected OnPress openDetailedInfoScreen() {
       return btn -> Minecraft.getInstance().setScreen(new WorldTierDetailScreen(this.displayedTier));
   }

   void closeTutorial() {
      if (this.activateButton.isActive() || WorldTier.isTutorialActive(Minecraft.getInstance().player)) {
         this.activateButton.onPress();
      }
   }

   protected void updateButtonStatus() {
      LocalPlayer player = Minecraft.getInstance().player;

      for (WorldTier tier : WorldTier.values()) {
         SimpleTexButton button = this.tierButtons.get(tier);
         if (WorldTier.isUnlocked(player, tier)) {
            button.active = true;
            button.setMessage(AscendantEquipment.lang("button", tier.getSerializedName()));
         } else {
            button.active = false;
            button.setMessage(AscendantEquipment.lang("button", "tier_locked", AscendantEquipment.lang("button", tier.getSerializedName())).withStyle(ChatFormatting.RED));
         }

         button.forceHovered = this.displayedTier == tier;
      }

      this.activateButton.active = WorldTier.getTier(player) != this.displayedTier;
      if (WorldTier.isTutorialActive(player) && this.displayedTier == WorldTier.HAVEN) {
         this.activateButton.active = WorldTier.isUnlocked(player, this.displayedTier);
      }

      if (this.activateButton.active) {
         this.activateButton.setButtonText(AscendantEquipment.lang("button", "activate").withColor(0xFAA8FF));
         Component tierName = AscendantEquipment.lang("text", "world_tier." + this.displayedTier.getSerializedName()).withStyle(ChatFormatting.GOLD);
         this.activateButton.setMessage(AscendantEquipment.lang("button", "activate_tier", tierName));
         if (!EquipmentConfig.enableManualWorldTierChanges) {
            this.activateButton.active = false;
            this.activateButton.setButtonText(AscendantEquipment.lang("button", "disabled").withStyle(ChatFormatting.RED));
            this.activateButton.setMessage(AscendantEquipment.lang("button", "tier_changes_disabled").withStyle(ChatFormatting.RED));
         }
      } else if (WorldTier.isTutorialActive(player) && !WorldTier.isUnlocked(player, this.displayedTier)) {
         this.activateButton.setButtonText(AscendantEquipment.lang("button", "inactive").withStyle(ChatFormatting.RED));
         this.activateButton.setMessage(AscendantEquipment.lang("button", "locked").withStyle(ChatFormatting.RED));
      } else {
         this.activateButton.setButtonText(AscendantEquipment.lang("button", "activated").withColor(0x9A669C));
         this.activateButton.setMessage(AscendantEquipment.lang("button", "already_activated").withStyle(ChatFormatting.GOLD));
      }
   }

   private void addTierButton(WorldTier tier, UnaryOperator<SimpleTexButton.Builder> config) {
      SimpleTexButton button = config.apply(
            SimpleTexButton.builder()
               .size(30, 30)
               .texture(AscendantEquipment.loc("textures/gui/buttons/" + tier.getSerializedName() + ".png"))
               .texSize(30, 90)
               .action(this.displayTier(tier))
               .message(AscendantEquipment.lang("button", tier.getSerializedName()))
               .inactiveMessage(tierLocked(tier))
         )
         .build();
      this.tierButtons.put(tier, button);
      this.addRenderableWidget(button);
   }

   private static List<Component> tierLocked(WorldTier tier) {
      ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
      AdvancementHolder advancement = advancements.get(AscendantEquipment.loc("progression/" + tier.getSerializedName()));
      List<Component> list = new ArrayList<>();
      MutableComponent advName = AscendantEquipment.lang("advancements", "progression." + tier.getSerializedName() + ".title").withStyle(ChatFormatting.GOLD);
      if (advancement != null) {
         list.add(AscendantEquipment.lang("button", "tier_advancement", advName).withStyle(ChatFormatting.RED));
         list.add(CommonComponents.SPACE);
         AdvancementProgress progress = advancements.progress.get(advancement);

         for (String criteria : progress.criteria.keySet()) {
            CriterionProgress critProg = progress.criteria.get(criteria);
            if (critProg.isDone()) {
               Component critDesc = AscendantEquipment.lang("advancements", "progression." + tier.getSerializedName() + ".criteria." + criteria)
                  .withStyle(ChatFormatting.GREEN);
               list.add(AscendantEquipment.lang("info", "criteria_done", critDesc));
            } else {
               Component critDesc = AscendantEquipment.lang("advancements", "progression." + tier.getSerializedName() + ".criteria." + criteria)
                  .withStyle(ChatFormatting.GRAY);
               list.add(AscendantEquipment.lang("info", "criteria_unfinished", critDesc));
            }
         }

         return list;
      } else {
         list.add(AscendantEquipment.lang("button", "tier_advancement", advName.withStyle(ChatFormatting.OBFUSCATED)).withStyle(ChatFormatting.RED));
         list.add(CommonComponents.SPACE);

         for (int i = 0; i < 3; i++) {
            list.add(
               AscendantEquipment.lang("info", "criteria_unknown", Component.literal("Do something, idk").withStyle(ChatFormatting.OBFUSCATED))
                  .withStyle(ChatFormatting.GRAY)
            );
         }

         return list;
      }
   }

   @Nullable
   private static AdvancementHolder getTierAdvancement(WorldTier tier) {
      return Minecraft.getInstance().getConnection().getAdvancements().get(AscendantEquipment.loc("progression/" + tier.getSerializedName()));
   }

   private record AnimationData(int x, int y, int width, int height, int frames, ResourceLocation texture) {
      private void render(GuiGraphics gfx, int left, int top, int time, float partialTick) {
         int frameHeight = this.height / this.frames;
         int frame = (int)((time + partialTick) / 2.0F);
         if (frame < this.frames) {
            gfx.blit(
               this.texture,
               left + this.x,
               top + this.y,
               0,
               (int)((frame + 1.0F) * frameHeight),
               this.width,
               frameHeight,
               this.width,
               this.height
            );
         }
      }
   }
}
