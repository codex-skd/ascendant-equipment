package com.skd.ascendantequipment.affix.salvaging;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.client.AdventureContainerScreen;
import com.skd.ascendantequipment.client.PipelinedRenderer;
import com.skd.ascendantequipment.client.SimpleTexButton;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class SalvagingScreen extends AdventureContainerScreen<SalvagingMenu> {
   public static final Component TITLE = Component.translatable("container.ascendant_equipment.salvage");
   public static final Identifier TEXTURE = AscendantEquipment.loc("textures/gui/salvage.png");
   protected List<SalvagingRecipe.OutputData> results = new ArrayList<>();
   protected SimpleTexButton salvageBtn;

   public SalvagingScreen(SalvagingMenu menu, Inventory inv, Component title) {
      super(menu, inv, TITLE, 176, 174);
      ((SalvagingMenu)this.menu).addSlotListener(new ContainerListener() {
         public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack stack) {
            SalvagingScreen.this.computeResults();
         }

         public void dataChanged(AbstractContainerMenu container, int id, int value) {
         }
      });
   }

   protected void init() {
      super.init();
      int left = this.getLeftPos();
      int top = this.getTopPos();
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
                  if (data.stack().item().equals(existing.stack().item()) && data.stack().components().equals(existing.stack().components())) {
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

   public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(gfx, mouseX, mouseY, partialTick);
      int maxDisplay = Math.min(6, this.results.size());
      IntSet skipSlots = new IntOpenHashSet();

      for (int i = 0; i < maxDisplay; i++) {
         ItemStack display = this.results.get(i).stack().create();
         int displaySlot = -1;

         for (int slot = 12; slot < 18; slot++) {
            if (!skipSlots.contains(slot)) {
               ItemStack outStack = ((Slot)((SalvagingMenu)this.menu).slots.get(slot)).getItem();
               if (outStack.isEmpty()) {
                  displaySlot = slot;
                  skipSlots.add(slot);
                  break;
               }

               if (outStack.is(display.getItem())) {
                  break;
               }
            }
         }

         if (displaySlot != -1) {
            Slot slot = ((SalvagingMenu)this.menu).getSlot(displaySlot);
            int sx = this.getLeftPos() + slot.x;
            int sy = this.getTopPos() + slot.y;
            PipelinedRenderer.grayFakeItem(gfx, display, sx, sy);
         }
      }

      List<Component> tooltip = new ArrayList<>();
      tooltip.add(Component.translatable("text.ascendant_equipment.salvage_results").withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.UNDERLINE}));

      for (SalvagingRecipe.OutputData data : this.results) {
         tooltip.add(Component.translatable("%s-%s %s", new Object[]{data.min(), data.max(), data.stack().create().getHoverName()}));
      }

      if (tooltip.size() > 1) {
         this.drawOnLeft(gfx, tooltip, this.getTopPos() + 29);
      }
   }

   public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
      super.extractBackground(gfx, mouseX, mouseY, partialTick);
      gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getLeftPos(), this.getTopPos(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
   }
}
