package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.tiers.augments.TierAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.ascendantattributes.AscendantAttributes;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class WorldTierDetailScreen extends Screen {
   public static final ResourceLocation TEXTURE = AscendantEquipment.loc("textures/gui/detail_column.png");
   public static final int BOX_WIDTH = 138;
   public static final int BOX_HEIGHT = 225;
   protected final WorldTier tier;

   protected WorldTierDetailScreen(WorldTier tier) {
      super(AscendantEquipment.lang("title", "world_tier_details"));
      this.tier = tier;
   }

   protected void init() {
      int leftPos = (this.width - WorldTierSelectScreen.GUI_WIDTH) / 2;
      int topPos = (this.height - WorldTierSelectScreen.GUI_HEIGHT) / 2;
      this.addRenderableWidget(
         SimpleTexButton.builder()
            .size(20, 20)
            .pos(leftPos + WorldTierSelectScreen.GUI_WIDTH - 15, topPos - 5)
            .texture(SimpleTexButton.ASC_EQ_SPRITES)
            .action(btn -> Minecraft.getInstance().setScreen(null))
            .buttonText(AscendantEquipment.lang("button", "return"))
            .message(AscendantEquipment.lang("button", "return.desc"))
            .build()
      );
   }

   public void renderBg(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
       super.render(gfx, mouseX, mouseY, partialTick);
      int leftPos = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2 + 26;
      int topPos = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2 + 30;

      for (int i = 0; i < 3; i++) {
         gfx.blit(TEXTURE, leftPos + i * (BOX_WIDTH + 16), topPos, 0, 0, 0, BOX_WIDTH, BOX_HEIGHT, BOX_WIDTH, BOX_HEIGHT);
      }
   }

   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      super.render(gfx, mouseX, mouseY, partialTick);
      int leftPos = (this.width - WorldTierSelectScreen.IMAGE_WIDTH) / 2 + 26;
      int topPos = (this.height - WorldTierSelectScreen.IMAGE_HEIGHT) / 2 + 30;
      LocalPlayer player = Minecraft.getInstance().player;
      AttributeTooltipContext ctx = AttributeTooltipContext.of(
         player, TooltipContext.of(player.level()), AscendantAttributes.getTooltipFlag()
      );

      for (int i = 0; i < 3; i++) {
         if (i == 2) {
            int x = leftPos + i * (BOX_WIDTH + 16);
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "monster_augments").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
            gfx.drawCenteredString(this.font, header, x + BOX_WIDTH / 2, y + 12, 0);
            y += 20;

            for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, TierAugment.Target.MONSTERS)) {
               y += 12;
               Component comp = aug.getDescription(ctx).plainCopy().withStyle(ChatFormatting.RED);
               drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + BOX_WIDTH - 12, y, 0);
            }
         } else if (i == 1) {
            int x = leftPos + i * (BOX_WIDTH + 16);
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "player_augments").withColor(0x00AAFF).withStyle(ChatFormatting.BOLD);
            gfx.drawCenteredString(this.font, header, x + BOX_WIDTH / 2, y + 12, 0);
            y += 20;

            for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, TierAugment.Target.PLAYERS)) {
               y += 12;
               Component comp = aug.getDescription(ctx).plainCopy().withColor(0x00AAFF);
               drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + BOX_WIDTH - 12, y, 0);
            }
         } else if (i == 0) {
            int x = leftPos + i * (BOX_WIDTH + 16);
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "drop_chances").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
            gfx.drawCenteredString(this.font, header, x + BOX_WIDTH / 2, y + 12, 0);
            Component rarityHeader = AscendantEquipment.lang("text", "rarities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
            gfx.drawCenteredString(this.font, rarityHeader, x + BOX_WIDTH / 2, y + 33, 0);
            y += 35;
            int totalWeight = RarityRegistry.INSTANCE.getValues().stream().mapToInt(r -> r.weights().getWeight(this.tier, 0.0F)).sum();

            for (LootRarity rarity : RarityRegistry.getSortedRarities()) {
               y += 12;
               float percent = (float)rarity.weights().getWeight(this.tier, 0.0F) / totalWeight;
               MutableComponent comp = rarity.toComponent();
               comp.append(Component.translatable(": %s", new Object[]{Affix.fmt(100.0F * percent) + "%"}).withStyle(s -> s.withColor(rarity.color())));
               drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + BOX_WIDTH - 12, y, 0xFFFFFF);
            }

            Component purityHeader = AscendantEquipment.lang("text", "purities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
            gfx.drawCenteredString(this.font, purityHeader, x + BOX_WIDTH / 2, y + 13, 0);
            y += 15;
            Purity[] values = Purity.values();
            totalWeight = Arrays.stream(values).mapToInt(r -> r.weights().getWeight(this.tier, 0.0F)).sum();

            for (Purity purity : values) {
               y += 12;
               float percent = (float)purity.weights().getWeight(this.tier, 0.0F) / totalWeight;
               MutableComponent comp = purity.toComponent();
               comp.append(Component.translatable(": %s", new Object[]{Affix.fmt(100.0F * percent) + "%"}).withStyle(s -> s.withColor(purity.getColor())));
               drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + BOX_WIDTH - 12, y, 0xFFFFFF);
            }
         }
      }
   }

   void drawScrollingStringWithoutMoving(GuiGraphics gfx, Font font, Component text, int minX, int maxX, int y, int color) {
      int maxWidth = maxX - minX;
      int textWidth = font.width(text.getVisualOrderText());
      if (textWidth <= maxWidth) {
         gfx.drawString(font, text, minX, y, color);
      }
      else {
         AbstractWidget.renderScrollingString(gfx, font, text, minX, y - 1, maxX, y + font.lineHeight, color);
      }
   }
}
