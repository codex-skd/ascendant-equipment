package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GemCaseSlot extends Slot {
   private static Container emptyContainer = new SimpleContainer(0);
   private final GemCaseMenu menu;
   final Purity purity;

   public GemCaseSlot(GemCaseMenu menu, Purity purity, int x, int y) {
      super(emptyContainer, -1, x, y);
      this.menu = menu;
      this.purity = purity;
   }

   public void onTake(Player player, ItemStack stack) {
      if (!stack.isEmpty()) {
         DynamicHolder<Gem> gem = GemItem.getGem(stack);
         Purity purity = GemItem.getPurity(stack);
         if (!gem.isBound() || gem.get() != this.menu.selectedGem || purity != this.purity) {
            AscendantEquipment.LOGGER
               .warn(
                  "Player {} tried to take a gem that doesn't match the selected gem or purity! (gem: {}, purity: {})",
                  new Object[]{player.getName().getString(), gem.getId(), purity}
               );
            return;
         }

         this.menu.extractGem(this.purity, stack.getCount());
      }

      this.setChanged();
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   public ItemStack getItem() {
      Gem gem = this.menu.selectedGem;
      if (gem == null) {
         return ItemStack.EMPTY;
      }

      int count = this.menu.getGemCount(gem, this.purity);
      return GemItem.createStack(gem, this.purity, Math.min(count, 64));
   }

   public boolean hasItem() {
      Gem gem = this.menu.selectedGem;
      return gem == null ? false : this.menu.getGemCount(gem, this.purity) > 0;
   }

   public void setByPlayer(ItemStack stack) {
   }

   public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
   }

   public void set(ItemStack stack) {
   }

   public void setChanged() {
      this.container.setChanged();
   }

   public int getMaxStackSize() {
      return this.container.getMaxStackSize();
   }

   public int getMaxStackSize(ItemStack stack) {
      return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
   }

   public ItemStack remove(int amount) {
      Gem gem = this.menu.selectedGem;
      if (gem == null) {
         return ItemStack.EMPTY;
      }

      int count = this.menu.getGemCount(gem, this.purity);
      int toExtract = Math.min(count, amount);
      return toExtract <= 0 ? ItemStack.EMPTY : GemItem.createStack(gem, this.purity, toExtract);
   }

   public boolean mayPickup(Player player) {
      return this.hasItem();
   }

   public boolean isActive() {
      Gem gem = this.menu.selectedGem;
      return gem == null ? false : this.purity.isAtLeast(gem.getMinPurity());
   }

   public boolean isSameInventory(Slot other) {
      return false;
   }

   public boolean allowModification(Player player) {
      return false;
   }

   public boolean isHighlightable() {
      return true;
   }

   public boolean isFake() {
      return false;
   }
}
