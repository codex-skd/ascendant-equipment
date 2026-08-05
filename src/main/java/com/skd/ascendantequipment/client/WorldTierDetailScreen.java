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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class WorldTierDetailScreen extends Screen {
   public static final Identifier TEXTURE = AscendantEquipment.loc("textures/gui/detail_column.png");
   public static final int BOX_WIDTH = 138;
   public static final int BOX_HEIGHT = 225;
   protected final WorldTier tier;

   protected WorldTierDetailScreen(WorldTier tier) {
      super(AscendantEquipment.lang("title", "world_tier_details"));
      this.tier = tier;
   }

   protected void init() {
      int leftPos = (this.width - 480) / 2;
      int topPos = (this.height - 270) / 2;
      this.addRenderableWidget(
         SimpleTexButton.builder()
            .size(20, 20)
            .pos(leftPos + 480 - 15, topPos - 5)
            .texture(SimpleTexButton.ASC_EQ_SPRITES)
            .action(btn -> Minecraft.getInstance().gui.popScreenLayer())
            .buttonText(AscendantEquipment.lang("button", "return"))
            .message(AscendantEquipment.lang("button", "return.desc"))
            .build()
      );
   }

   public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      int leftPos = (this.width - 498) / 2 + 26;
      int topPos = (this.height - 286) / 2 + 30;

      for (int i = 0; i < 3; i++) {
         gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + i * 154, topPos, 0.0F, 0.0F, 138, 225, 138, 225);
      }
   }

   public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(gfx, mouseX, mouseY, partialTick);
      int leftPos = (this.width - 498) / 2 + 26;
      int topPos = (this.height - 286) / 2 + 30;
      LocalPlayer player = Minecraft.getInstance().player;
      AttributeTooltipContext ctx = AttributeTooltipContext.of(
         player, TooltipContext.of(player.level()), TooltipDisplay.DEFAULT, AscendantAttributes.getTooltipFlag()
      );

      for (int i = 0; i < 3; i++) {
         if (i == 2) {
            int x = leftPos + i * 154;
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "monster_augments").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD});
            drawCenteredString(gfx, this.font, header, x + 69, y + 12, -16777216);
            y += 20;

            for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, TierAugment.Target.MONSTERS)) {
               y += 12;
               Component comp = aug.getDescription(ctx).plainCopy().withStyle(ChatFormatting.RED);
               this.drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + 138 - 12, y, -16777216);
            }
         } else if (i == 1) {
            int x = leftPos + i * 154;
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "player_augments").withColor(43775).withStyle(ChatFormatting.BOLD);
            drawCenteredString(gfx, this.font, header, x + 69, y + 12, -16777216);
            y += 20;

            for (TierAugment aug : TierAugmentRegistry.getAugments(this.tier, TierAugment.Target.PLAYERS)) {
               y += 12;
               Component comp = aug.getDescription(ctx).plainCopy().withColor(43775);
               this.drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + 138 - 12, y, -16777216);
            }
         } else if (i == 0) {
            int x = leftPos + i * 154;
            int y = topPos;
            Component header = AscendantEquipment.lang("text", "drop_chances").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD);
            drawCenteredString(gfx, this.font, header, x + 69, y + 12, -16777216);
            Component rarityHeader = AscendantEquipment.lang("text", "rarities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
            drawCenteredString(gfx, this.font, rarityHeader, x + 69, y + 33, -16777216);
            y += 35;
            int totalWeight = RarityRegistry.INSTANCE.getValues().stream().mapToInt(r -> r.weights().getWeight(this.tier, 0.0F)).sum();

            for (LootRarity rarity : RarityRegistry.getSortedRarities()) {
               y += 12;
               float percent = (float)rarity.weights().getWeight(this.tier, 0.0F) / totalWeight;
               MutableComponent comp = rarity.toComponent();
               comp.append(Component.translatable(": %s", new Object[]{Affix.fmt(100.0F * percent) + "%"}).withStyle(s -> s.withColor(rarity.color())));
               this.drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + 138 - 12, y, -1);
            }

            Component purityHeader = AscendantEquipment.lang("text", "purities").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.UNDERLINE);
            drawCenteredString(gfx, this.font, purityHeader, x + 69, y + 13, -16777216);
            y += 15;
            Purity[] values = Purity.values();
            totalWeight = Arrays.stream(values).mapToInt(r -> r.weights().getWeight(this.tier, 0.0F)).sum();

            for (Purity purity : values) {
               y += 12;
               float percent = (float)purity.weights().getWeight(this.tier, 0.0F) / totalWeight;
               MutableComponent comp = purity.toComponent();
               comp.append(Component.translatable(": %s", new Object[]{Affix.fmt(100.0F * percent) + "%"}).withStyle(s -> s.withColor(purity.getColor())));
               this.drawScrollingStringWithoutMoving(gfx, this.font, comp, x + 12, x + 138 - 12, y, -1);
            }
         }
      }
   }

   private static void drawCenteredString(GuiGraphicsExtractor gfx, Font font, Component text, int centerX, int y, int color) {
      gfx.text(font, text.getVisualOrderText(), centerX - font.width(text) / 2, y, color, false);
   }

   void drawScrollingStringWithoutMoving(GuiGraphicsExtractor gfx, Font font, Component text, int minX, int maxX, int y, int color) {
      gfx.drawScrollingString(gfx.textRenderer(), font, text, minX, maxX, y);
   }
}
