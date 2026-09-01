package com.skd.ascendantequipment.compat.gateways.tiered_gate;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantattributes.api.AttributeHelper;
import com.skd.commontoolkit.CommonToolkitClient;
import dev.shadowsoffire.gateways.client.GatewaysClient;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import org.joml.Matrix3x2fStack;

public class TieredGateClient {
   public static void appendPearlTooltip(TieredGateway gate, TooltipContext ctx, Consumer<Component> tooltips, TooltipFlag flag) {
      int waveIdx = CommonToolkitClient.getTooltipScrollIndex(gate.getNumWaves());
      Wave wave = gate.getWave(waveIdx);
      WorldTier tier = gate.settings().tier();
      if (WorldTier.getTier(Minecraft.getInstance().player) != tier) {
         tooltips.accept(AscendantEquipment.lang("tooltip", "requires_world_tier", tier.toComponent()).withStyle(ChatFormatting.RED));
      }

      if (Minecraft.getInstance().hasShiftDown()) {
         MutableComponent comp = Component.translatable("tooltip.gateways.wave", new Object[]{waveIdx + 1, gate.getNumWaves()}).withStyle(ChatFormatting.GRAY);
         comp.append(CommonComponents.SPACE);
         comp.append(Component.translatable("tooltip.gateways.scroll").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false)));
         tooltips.accept(comp);
         comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.entities").withStyle(Style.EMPTY.withColor(8900331)));
         tooltips.accept(comp);

         for (WaveEntity entity : wave.entities()) {
            comp = AttributeHelper.list()
               .append(Component.translatable("tooltip.gateways.dot", new Object[]{entity.getDescription()}).withStyle(Style.EMPTY.withColor(8900331)));
            tooltips.accept(comp);
         }

         if (!wave.modifiers().isEmpty()) {
            comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.modifiers").withStyle(ChatFormatting.RED));
            tooltips.accept(comp);

            for (WaveModifier modif : wave.modifiers()) {
               modif.appendHoverText(
                  ctx,
                  c -> tooltips.accept(
                     AttributeHelper.list()
                        .append(
                           Component.translatable("tooltip.gateways.dot", new Object[]{c.withStyle(ChatFormatting.RED)})
                              .withStyle(s -> s.withColor(ChatFormatting.RED))
                        )
                  )
               );
            }
         }

         comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.rewards").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
         tooltips.accept(comp);

         for (Reward r : wave.rewards()) {
            r.appendHoverText(
               ctx,
               c -> tooltips.accept(
                  AttributeHelper.list()
                     .append(Component.translatable("tooltip.gateways.dot", new Object[]{c}).withStyle(s -> s.withColor(ChatFormatting.GOLD)))
               )
            );
         }
      } else {
         MutableComponent comp = Component.translatable("tooltip.gateways.num_wave" + (gate.getNumWaves() == 1 ? "" : "s"), new Object[]{gate.getNumWaves()})
            .withStyle(ChatFormatting.GRAY);
         comp.append(CommonComponents.SPACE);
         comp.append(Component.translatable("tooltip.gateways.shift").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
         tooltips.accept(comp);
      }

      List<Failure> failures = gate.failures();
      if (!failures.isEmpty()) {
         if (Minecraft.getInstance().hasControlDown()) {
            MutableComponent var18 = Component.translatable("tooltip.gateways.failures").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
            tooltips.accept(var18);

            for (Failure f : failures) {
               f.appendHoverText(ctx, c -> tooltips.accept(AttributeHelper.list().append(c.withStyle(Style.EMPTY.withColor(ChatFormatting.RED)))));
            }
         } else {
            MutableComponent var19 = Component.translatable("tooltip.gateways.num_failure" + (failures.size() == 1 ? "" : "s"), new Object[]{failures.size()})
               .withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
            var19.append(CommonComponents.SPACE);
            var19.append(Component.translatable("tooltip.gateways.ctrl").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
            tooltips.accept(var19);
         }
      }

      List<MutableComponent> deviations = gate.rules().buildDeviations();
      if (!deviations.isEmpty()) {
         if (Minecraft.getInstance().hasAltDown()) {
            MutableComponent var20 = Component.translatable("tooltip.gateways.rules", new Object[]{deviations.size()})
               .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
            tooltips.accept(var20);
            deviations.forEach(c -> tooltips.accept(AttributeHelper.list().append(c.withStyle(ChatFormatting.DARK_GREEN))));
         } else {
            MutableComponent var21 = Component.translatable("tooltip.gateways.num_rule" + (deviations.size() == 1 ? "" : "s"), new Object[]{deviations.size()})
               .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
            var21.append(CommonComponents.SPACE);
            var21.append(Component.translatable("tooltip.gateways.alt").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
            tooltips.accept(var21);
         }
      }

      List<Reward> rewards = gate.rewards();
      if (!rewards.isEmpty()) {
         MutableComponent var22 = Component.translatable("tooltip.gateways.key_rewards").withStyle(Style.EMPTY.withColor(3385888));
         tooltips.accept(var22);

         for (Reward r : rewards) {
            r.appendHoverText(ctx, c -> tooltips.accept(AttributeHelper.list().append(c.withStyle(Style.EMPTY.withColor(3385888)))));
         }
      }
   }

   public static void renderBossBar(GatewayEntity gateEntity, Object guiGfx, int x, int y, boolean isInWorld) {
      TieredGatewayEntity gate = (TieredGatewayEntity)gateEntity;
      GuiGraphicsExtractor gfx = (GuiGraphicsExtractor)guiGfx;
      Matrix3x2fStack pose = gfx.pose();
      int color = gate.getGateway().color().getValue();
      int tintColor = 0xFF000000 | color;
      int wave = gate.getWave() + 1;
      int maxWave = gate.getGateway().getNumWaves();
      int enemies = gate.getActiveEnemies();
      int maxEnemies = gate.getCurrentWave().entities().stream().mapToInt(WaveEntity::getCount).sum();
      int y2 = y + 10 + 9;
      pose.pushMatrix();
      gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_BACKGROUND, x, y, 182, 5, tintColor);
      gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_BACKGROUND, x, y2, 182, 5, tintColor);
      pose.popMatrix();
      float waveProgress = 1.0F / maxWave;
      float progress = waveProgress * (maxWave - wave + 1);
      if (gate.isWaveActive()) {
         progress -= waveProgress * ((float)(maxEnemies - enemies) / maxEnemies);
      }

      int i = (int)(progress * 183.0F);
      if (i > 0) {
         gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y, i, 5, tintColor);
      }

      float maxTime = gate.getMaxWaveTime();
      if (gate.isWaveActive()) {
         i = (int)((maxTime - gate.getTicksActive()) / maxTime * 183.0F);
         if (i > 0) {
            gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y2, i, 5, tintColor);
         }
      } else {
         maxTime = gate.getSetupTime();
         i = (int)(gate.getTicksActive() / maxTime * 183.0F);
         if (i > 0) {
            gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y2, i, 5, tintColor);
         }
      }

      Font font = Minecraft.getInstance().font;
      Component component = Component.literal(gate.getCustomName().getString()).withStyle(ChatFormatting.GOLD);
      int strWidth = font.width(component);
      int textX = x + 91 - strWidth / 2;
      int textY = y - 9;
      if (isInWorld) {
         GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
      } else {
         gfx.text(font, component, textX, textY, -1, true);
      }

      textY = y2 - 9;
      int time = (int)maxTime - gate.getTicksActive();
      float tps = gateEntity.level().tickRateManager().tickrate();
      String str = I18n.get("boss.gateways.wave", new Object[]{wave, maxWave, StringUtil.formatTickDuration(time, tps), enemies});
      if (!gate.isWaveActive()) {
         if (gate.isLastWave()) {
            str = I18n.get("boss.gateways.done", new Object[0]);
         } else {
            str = I18n.get("boss.gateways.starting", new Object[]{wave, StringUtil.formatTickDuration(time, tps)});
         }
      }

      Component var29 = Component.literal(str).withStyle(ChatFormatting.GREEN);
      strWidth = font.width(var29);
      textX = x + 91 - strWidth / 2;
      if (isInWorld) {
         GatewaysClient.drawReversedDropShadow(gfx, font, var29, textX, textY);
      } else {
         gfx.text(font, var29, textX, textY, -1, true);
      }
   }
}
