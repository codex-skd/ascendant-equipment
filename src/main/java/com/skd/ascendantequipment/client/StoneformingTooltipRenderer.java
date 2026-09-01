package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.effect.StoneformingAffix;
import com.skd.commontoolkit.CommonToolkitClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record StoneformingTooltipRenderer(StoneformingTooltipRenderer.StoneformingComponent comp) implements ClientTooltipComponent {
   public static final ResourceLocation SOCKET = AscendantEquipment.loc("textures/gui/socket.png");
   public static final Component EMPTY_SPACE_PLACEHOLDER = Component.literal(" ".repeat(8));

   public int getHeight() {
      return 11;
   }

   public int getWidth(Font font) {
      return font.width(getText(this.comp.inst));
   }

   public void renderImage(Font font, int x, int y, GuiGraphics gfx) {
      if (this.affix().getCandidates().size() != 0) {
         String text = I18n.get("affix.ascendant_equipment.stoneforming.desc", new Object[]{"<M1>", "<M2>"});
         int xPos = font.width(text.substring(0, text.indexOf("<M1>") + 1));
         StoneformingAffix affix = this.affix();
         Block[] selected = new Block[3];
         int start = (int)(CommonToolkitClient.ticks / 20L) * 3;

         for (int i = 0; i < 3; i++) {
            selected[i] = (Block)affix.getCandidates().get((start + i) % affix.getCandidates().size()).value();
         }

         gfx.pose().pushPose();
         gfx.pose().translate(0.0F, -0.25F, 0.0F);
         gfx.pose().scale(0.5F, 0.5F, 1.0F);

         for (Block block : selected) {
            ItemStack stack = new ItemStack(block);
            gfx.renderFakeItem(stack, (x + xPos) * 2 + 8, y * 2);
            xPos += 10;
         }

         gfx.pose().popPose();
      }
   }

   public void renderText(Font font, int x, int y, org.joml.Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
      font.drawInBatch(getText(this.comp.inst()), x, y, 0xAABBCC, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
   }

   private StoneformingAffix affix() {
      return (StoneformingAffix)this.comp.inst.getAffix();
   }

   public static Component getText(AffixInstance inst) {
      Component blockName = ((StoneformingAffix)inst.getAffix()).getTarget(inst).getName().withStyle(ChatFormatting.BLUE);
      Component afxDesc = AscendantEquipment.lang("affix", "stoneforming.desc", Component.literal(" ".repeat(8)), blockName);
      return AscendantEquipment.lang("text", "dot_prefix", afxDesc).withStyle(ChatFormatting.YELLOW);
   }

   public record StoneformingComponent(AffixInstance inst) implements TooltipComponent {
   }
}
