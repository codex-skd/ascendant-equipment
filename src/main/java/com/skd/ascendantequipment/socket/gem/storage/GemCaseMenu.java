package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingMenu;
import com.skd.ascendantequipment.socket.gem.cutting.GemCuttingRecipe;
import com.skd.ascendantequipment.socket.gem.cutting.PurityUpgradeRecipe;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.menu.BlockEntityMenu;
import com.skd.commontoolkit.payloads.ButtonClickPayload.IButtonContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class GemCaseMenu extends BlockEntityMenu<GemCaseTile> implements IButtonContainer {
   public static final int INPUT_SLOT = 0;
   public static final int FILTER_SLOT = 1;
   public static final int FIRST_GEM_SLOT = 2;
   public static final int FIRST_UPGRADE_MAT_SLOT = 8;
   protected SimpleContainer ioInv = new SimpleContainer(2);
   protected SimpleContainer upgradeMatInv = new SimpleContainer(6) {
      public void setChanged() {
         super.setChanged();
         GemCaseMenu.this.onChanged();
      }
   };
   protected Runnable notifier = null;
   @Nullable
   protected Gem selectedGem = null;

   public GemCaseMenu(int id, Inventory inv, BlockPos pos) {
      super(AscEq.Menus.GEM_CASE, id, inv, pos);
      ((GemCaseTile)this.tile).addListener(this);
      this.initCommon(inv);
   }

   public void setSelectedGem(DynamicHolder<Gem> gem) {
      this.selectedGem = gem.isBound() ? (Gem)gem.get() : null;
      this.onChanged();
   }

   public void removed(Player player) {
      super.removed(player);
      if (!this.level.isClientSide()) {
         ((GemCaseTile)this.tile).removeListener(this);
      }

      this.clearContainer(player, this.ioInv);
      this.clearContainer(player, this.upgradeMatInv);
   }

   void initCommon(final Inventory inv) {
      this.addSlot(new Slot(this.ioInv, 0, 142, 99) {
         public boolean mayPlace(ItemStack stack) {
            return stack.is(AscEq.Items.GEM);
         }

         public int getMaxStackSize() {
            return 64;
         }

         public void setChanged() {
            super.setChanged();
            if (!GemCaseMenu.this.level.isClientSide() && !this.getItem().isEmpty()) {
               ((GemCaseTile)GemCaseMenu.this.tile).depositGem(this.getItem());
            }

            if (!this.getItem().isEmpty() && GemCaseMenu.this.level.isClientSide()) {
               inv.player.level().playSound(inv.player, GemCaseMenu.this.pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 0.5F, 0.7F);
            }

            GemCaseMenu.this.ioInv.setItem(0, ItemStack.EMPTY);
         }
      });
      this.addSlot(new Slot(this.ioInv, 1, 142, 18) {
         public boolean mayPlace(ItemStack stack) {
            return !LootCategory.forItem(stack).isNone();
         }

         public int getMaxStackSize() {
            return 1;
         }

         public void setChanged() {
            GemCaseMenu.this.onChanged();
         }
      });

      for (Purity p : Purity.ALL_PURITIES) {
         this.addSlot(new GemCaseSlot(this, p, 21 + p.ordinal() * 18, 91));
      }

      for (int i = 0; i < this.upgradeMatInv.getContainerSize(); i++) {
         this.addSlot(new Slot(this.upgradeMatInv, i, -45 + 18 * (i % 2), 37 + 18 * (i / 2)) {
            public boolean mayPlace(ItemStack stack) {
               return GemCaseMenu.this.isValidUpgradeMaterial(stack);
            }

            public int getMaxStackSize() {
               return 64;
            }

            public void setChanged() {
               super.setChanged();
               GemCaseMenu.this.onChanged();
            }
         });
      }

      this.addPlayerSlots(inv, 8, 148);
      this.mover.registerRule((stack, slot) -> slot == 1, this.playerInvStart, this.slots.size());
      this.mover.registerRule((stack, slot) -> slot >= 2 && slot < 8, this.playerInvStart, this.slots.size());
      this.mover.registerRule((stack, slot) -> slot >= 8 && slot < 14, this.playerInvStart, this.slots.size());
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(AscEq.Items.GEM), 0, 1);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidUpgradeMaterial(stack), 8, 14);
      this.mover.registerRule((stack, slot) -> !LootCategory.forItem(stack).isNone(), 1, 2);
      this.registerInvShuffleRules();
   }

   public boolean stillValid(Player player) {
      return player.distanceToSqr(this.pos.getX(), this.pos.getY(), this.pos.getZ()) < 256.0 && this.tile != null && !((GemCaseTile)this.tile).isRemoved();
   }

   public void setNotifier(Runnable r) {
      this.notifier = r;
   }

   public void onChanged() {
      if (this.notifier != null) {
         this.notifier.run();
      }
   }

   public int getGemCount(Gem gem) {
      int sum = 0;

      for (Purity p : Purity.ALL_PURITIES) {
         sum += ((GemCaseTile)this.tile).getCount(gem, p);
      }

      return sum;
   }

   public int getGemCount(Gem gem, Purity p) {
      return ((GemCaseTile)this.tile).getCount(gem, p);
   }

   public ItemStack extractGem(Purity p, int count) {
      if (this.selectedGem == null) {
         return ItemStack.EMPTY;
      }

      DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.selectedGem);
      return ((GemCaseTile)this.tile).extractGem(holder, p, count);
   }

   @Nullable
   public GemUpgradeMatch getUpgradeMatch(Purity purity) {
      return this.selectedGem == null
         ? null
         : ((GemCaseTile)this.tile).getUpgradeMatch(GemRegistry.INSTANCE.holder(this.selectedGem), purity, this.upgradeMatInv);
   }

   public void onQuickMove(ItemStack original, ItemStack remaining, Slot slot) {
      if (slot instanceof GemCaseSlot gss) {
         int amount = original.getCount() - remaining.getCount();
         ((GemCaseTile)this.tile).extractGem(GemRegistry.INSTANCE.holder(this.selectedGem), gss.purity, amount);
      }

      slot.setChanged();
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      Slot slot = this.getSlot(pIndex);
      if (slot instanceof GemCaseSlot) {
         this.mover.quickMoveStack(this, pPlayer, pIndex);
         return ItemStack.EMPTY;
      } else {
         return this.mover.quickMoveStack(this, pPlayer, pIndex);
      }
   }

   public boolean isValidUpgradeMaterial(ItemStack stack) {
      for (RecipeHolder<GemCuttingRecipe> rec : GemCuttingMenu.getRecipes(this.level)) {
         if (rec.value() instanceof PurityUpgradeRecipe purRec && (purRec.isValidLeftItem(null, stack) || purRec.isValidRightItem(null, stack))) {
            return true;
         }
      }

      return false;
   }

   public void onButtonClick(int id) {
      boolean shift = (id & 4096) != 0;
      Purity purity = Purity.BY_ID.apply(id & 4095);
      if (this.selectedGem != null && purity != Purity.CRACKED) {
         DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(this.selectedGem);
         int tries = shift ? 64 : 1;

         while (tries-- > 0) {
            boolean result = ((GemCaseTile)this.tile).upgradeGem(holder, purity, this.upgradeMatInv);
            if (!result) {
               break;
            }

            this.level
               .playSound(
                  null, this.pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 1.5F + 0.35F * (1.0F - 2.0F * this.level.getRandom().nextFloat())
               );
         }
      }
   }
}
