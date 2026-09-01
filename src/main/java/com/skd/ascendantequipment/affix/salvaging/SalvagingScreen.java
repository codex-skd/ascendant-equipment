package com.skd.ascendantequipment.affix.salvaging;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.SimpleTexButton;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SalvagingScreen extends AdventureContainerScreen<SalvagingMenu> {
   public static final Component TITLE = Component.translatable("container.ascendant_equipment.salvage");
   public static final ResourceLocation TEXTURE = AscendantEquipment.loc("textures/gui/salvage.png");
   protected List<SalvagingRecipe.OutputData> results = new ArrayList<>();
   protected SimpleTexButton salvageBtn;

   public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
      super(menu, inv, TITLE);
      this.menu.addSlotListener((id, stack) -> this.computeResults());
      this.imageHeight = 174;
   }

   protected void init() {
      super.init();
      int left = this.getGuiLeft();
      int top = this.getGuiTop();
      this.salvageBtn = (SimpleTexButton)this.addRenderableWidget(
         new SimpleTexButton(
               left + 98,
               top + 34,
               18,
               18,
               238,
               0,
               TEXTURE,
               256,
               256,
               btn -> this.minecraft.gameMode.handleInventoryButtonClick(((SalvagingMenu)this.menu).containerId, 0),
               Component.translatable("button.ascendant_equipment.salvage")
            )
            .setInactiveMessage(Component.translatable("button.ascendant_equipment.no_salvage").withStyle(ChatFormatting.RED))
      );
      this.computeResults();
   }

   public void computeResults() {
      if (this.salvageBtn != null) {
         ArrayList<SalvagingRecipe.OutputData> matches = new ArrayList<>();

         for (int i = 0; i < 15; i++) {
            Slot s = ((SalvagingMenu)this.menu).getSlot(i);
            ItemStack stack = s.getItem();

            for (RecipeHolder<SalvagingRecipe> recipe : SalvagingMenu.findMatch(Minecraft.getInstance().level, stack)) {
               if (recipe != null) {
                  for (SalvagingRecipe.OutputData d : ((SalvagingRecipe)recipe.value()).getOutputs()) {
                     int[] counts = SalvagingMenu.getSalvageCounts(d, stack);
                     matches.add(new SalvagingRecipe.OutputData(d.stack(), counts[0], counts[1]));
                  }
               }
            }
         }

         ArrayList<SalvagingRecipe.OutputData> compressed = new ArrayList<>();

         for (SalvagingRecipe.OutputData data : matches) {
            if (data != null) {
               boolean success = false;

               for (int i = 0; i < compressed.size(); i++) {
                  SalvagingRecipe.OutputData existing = compressed.get(i);
                   if (ItemStack.isSameItemSameComponents(data.stack(), existing.stack())) {
                     compressed.set(i, new SalvagingRecipe.OutputData(existing.stack(), existing.min() + data.min(), existing.max() + data.max()));
                     success = true;
                     break;
                  }
               }

               if (!success) {
                  compressed.add(data);
               }
            }
         }

         this.results = compressed;
         this.salvageBtn.active = !this.results.isEmpty();
      }
   }

   @Override
   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
      super.render(gfx, mouseX, mouseY, partialTick);

      RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
      RenderSystem.enableBlend();

      int maxDisplay = Math.min(6, this.results.size());
      IntSet skipSlots = new IntOpenHashSet();

      for (int i = 0; i < maxDisplay; i++) {
         ItemStack display = this.results.get(i).stack();
         int displaySlot = -1;

         for (int slot = 12; slot < 18; slot++) {
            if (skipSlots.contains(slot)) {
               continue;
            }
            ItemStack outStack = ((SalvagingMenu)this.menu).slots.get(slot).getItem();
            if (outStack.isEmpty()) {
               displaySlot = slot;
               skipSlots.add(slot);
               break;
            }

            if (outStack.is(display.getItem())) {
               break;
            }
         }

         if (displaySlot != -1) {
            Slot slot = ((SalvagingMenu)this.menu).getSlot(displaySlot);
            renderGuiItem(gfx, display, this.getGuiLeft() + slot.x, this.getGuiTop() + slot.y);
         }
      }

      this.renderTooltip(gfx, mouseX, mouseY);
   }

   public static void renderGuiItem(GuiGraphics gfx, ItemStack pStack, int pX, int pY) {
      Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
      RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      PoseStack posestack = gfx.pose();
      posestack.pushPose();
      posestack.translate(pX, pY, 100.0F);
      posestack.translate(8.0D, 8.0D, 0.0D);
      posestack.scale(1.0F, -1.0F, 1.0F);
      posestack.scale(16.0F, 16.0F, 16.0F);
      Minecraft mc = Minecraft.getInstance();
      BakedModel model = mc.getItemRenderer().getModel(pStack, mc.level, mc.player, pX ^ pY);
      boolean flag = !model.usesBlockLight();
      if (flag) {
         Lighting.setupForFlatItems();
      }

      MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
      Minecraft.getInstance().getItemRenderer().render(pStack, ItemDisplayContext.GUI, false, posestack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
      buffer.endBatch();
      RenderSystem.enableDepthTest();
      if (flag) {
         Lighting.setupFor3DItems();
      }

      posestack.popPose();
   }

   @Override
   protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
      gfx.blit(TEXTURE, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
   }

   @Override
   protected void renderTooltip(GuiGraphics gfx, int x, int y) {
      PoseStack stack = gfx.pose();
      stack.pushPose();
      stack.translate(0, 0, -100);

      List<Component> tooltip = new ArrayList<>();
      tooltip.add(Component.translatable("text.ascendant_equipment.salvage_results").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));

      for (SalvagingRecipe.OutputData data : this.results) {
         tooltip.add(Component.translatable("%s-%s %s", data.min(), data.max(), data.stack().getHoverName()));
      }

      if (tooltip.size() > 1) {
         this.drawOnLeft(gfx, tooltip, this.getGuiTop() + 29);
      }

      stack.popPose();
      super.renderTooltip(gfx, x, y);
   }
}
