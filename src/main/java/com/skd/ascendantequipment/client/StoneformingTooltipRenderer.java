package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.effect.StoneformingAffix;
import com.skd.commontoolkit.CommonToolkitClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record StoneformingTooltipRenderer(StoneformingTooltipRenderer.StoneformingComponent comp) implements ClientTooltipComponent {
   public static final Identifier SOCKET = AscendantEquipment.loc("textures/gui/socket.png");
   public static final Component EMPTY_SPACE_PLACEHOLDER = Component.literal(" ".repeat(8));

   public int getHeight(Font font) {
      return 9 + 2;
   }

   public int getWidth(Font font) {
      return font.width(getText(this.comp.inst));
   }

   public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor gfx) {
      if (this.affix().getCandidates().size() != 0) {
         String text = I18n.get("affix.ascendant_equipment.stoneforming.desc", new Object[]{"<M1>", "<M2>"});
         int xPos = font.width(text.substring(0, text.indexOf("<M1>") + 1));
         StoneformingAffix affix = this.affix();
         Block[] selected = new Block[3];
         int start = (int)(CommonToolkitClient.ticks / 20L) * 3;

         for (int i = 0; i < 3; i++) {
            selected[i] = (Block)affix.getCandidates().get((start + i) % affix.getCandidates().size()).value();
         }

         gfx.pose().pushMatrix();
         gfx.pose().translate(0.0F, -0.25F);
         gfx.pose().scale(0.5F, 0.5F);

         for (Block block : selected) {
            ItemStack stack = new ItemStack(block);
            gfx.fakeItem(stack, (x + xPos) * 2 + 8, y * 2);
            xPos += 10;
         }

         gfx.pose().popMatrix();
      }
   }

   public void extractText(GuiGraphicsExtractor gfx, Font font, int x, int y) {
      gfx.text(font, getText(this.comp.inst()), x, y, -5588020, true);
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
