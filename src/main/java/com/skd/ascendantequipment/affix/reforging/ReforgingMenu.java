package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.cap.InternalItemHandler;
import com.skd.commontoolkit.menu.BlockEntityMenu;
import com.skd.commontoolkit.util.EnchantmentUtils;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ReforgingMenu extends BlockEntityMenu<ReforgingTableTile> {
   public static final String REFORGE_SEED = "apoth_reforge_seed";
   protected final Player player;
   protected InternalItemHandler itemInv = new InternalItemHandler(1);
   protected InternalItemHandler choicesInv = new InternalItemHandler(3);
   protected final RandomSource random = new XoroshiroRandomSource(0L);
   protected final int[] costs = new int[3];
   protected int seed = -1;

   public ReforgingMenu(int id, Inventory inv, BlockPos pos) {
      super(AscEq.Menus.REFORGING, id, inv, pos);
      this.player = inv.player;
      this.addSlot(new UpdatingSlot(this.itemInv, 0, 81, 62, stack -> !LootCategory.forItem(stack).isNone()) {
         public int getMaxStackSize() {
            return 1;
         }

         public int getMaxStackSize(ItemStack pStack) {
            return 1;
         }
      });
      this.addSlot(new UpdatingSlot(((ReforgingTableTile)this.tile).inv, 0, 39, 40, ((ReforgingTableTile)this.tile)::isValidRarityMat));
      this.addSlot(new UpdatingSlot(((ReforgingTableTile)this.tile).inv, 1, 123, 86, stack -> stack.is(AscEq.Items.SIGIL_OF_REBIRTH)));
      this.addSlot(new ReforgingMenu.ReforgingResultSlot(this.choicesInv, 0, 27, 135));
      this.addSlot(new ReforgingMenu.ReforgingResultSlot(this.choicesInv, 1, 81, 135));
      this.addSlot(new ReforgingMenu.ReforgingResultSlot(this.choicesInv, 2, 135, 135));
      this.addPlayerSlots(inv, 8, 184);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && !LootCategory.forItem(stack).isNone(), 0, 1);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && ((ReforgingTableTile)this.tile).isValidRarityMat(stack), 1, 2);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(AscEq.Items.SIGIL_OF_REBIRTH), 2, 3);
      this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
      this.registerInvShuffleRules();
      this.updateSeed();
      this.addDataSlot(DataSlot.shared(this.costs, 0));
      this.addDataSlot(DataSlot.shared(this.costs, 1));
      this.addDataSlot(DataSlot.shared(this.costs, 2));
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.clearContainer(pPlayer, this.itemInv);
   }

   protected void updateSeed() {
      int seed = this.player.getPersistentData().getInt(REFORGE_SEED);
      if (seed == 0) {
         seed = this.player.getRandom().nextInt();
         this.player.getPersistentData().putInt(REFORGE_SEED, seed);
      }

      this.seed = seed;
   }

   public int getMatCount() {
      return this.getSlot(1).getItem().getCount();
   }

   public int getSigilCount() {
      return this.getSlot(2).getItem().getCount();
   }

   @Nullable
   public LootRarity getRarity() {
      ItemStack s = this.getSlot(1).getItem();
      return s.isEmpty() ? null : (LootRarity)RarityRegistry.getMaterialRarity(s.getItem()).getOptional().orElse(null);
   }

   public int getSigilCost(int slot) {
      return this.costs[0] * ++slot;
   }

   public int getMatCost(int slot) {
      return this.costs[1] * ++slot;
   }

   public int getLevelCost(int slot) {
      return this.costs[2] * ++slot;
   }

   public void slotsChanged(Container pContainer) {
      LootRarity rarity = this.getRarity();
      if (rarity != null) {
         ReforgingRecipe recipe = ((ReforgingTableTile)this.tile).getRecipeFor(rarity);
         if (recipe != null) {
            this.costs[0] = recipe.sigilCost();
            this.costs[1] = recipe.matCost();
            this.costs[2] = recipe.levelCost();
         }
      }

      ItemStack input = this.getSlot(0).getItem();

      for (int slot = 0; slot < 3; slot++) {
         if (!input.isEmpty() && rarity != null) {
            RandomSource rand = this.random;
            rand.setSeed(this.seed ^ BuiltInRegistries.ITEM.getKey(input.getItem()).hashCode() + slot);
            GenContext ctx = GenContext.forPlayer(rand, this.player);
            ItemStack output = LootController.createLootItem(input.copy(), rarity, ctx);
            this.choicesInv.setStackInSlot(slot, output);
         } else {
            this.choicesInv.setStackInSlot(slot, ItemStack.EMPTY);
         }
      }

      super.slotsChanged(pContainer);
      ((ReforgingTableTile)this.tile).setChanged();
   }

   public class ReforgingResultSlot extends SlotItemHandler {
      public ReforgingResultSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@NotNull ItemStack stack) {
         return false;
      }

      public boolean mayPickup(Player playerIn) {
         ItemStack input = ReforgingMenu.this.getSlot(0).getItem();
         LootRarity rarity = ReforgingMenu.this.getRarity();
         ReforgingRecipe recipe = ((ReforgingTableTile)ReforgingMenu.this.tile).getRecipeFor(rarity);
         if (recipe != null && !input.isEmpty()) {
            int sigils = ReforgingMenu.this.getSigilCount();
            int sigilCost = ReforgingMenu.this.getSigilCost(this.getSlotIndex());
            int mats = ReforgingMenu.this.getMatCount();
            int matCost = ReforgingMenu.this.getMatCost(this.getSlotIndex());
            int levels = ReforgingMenu.this.player.experienceLevel;
            int levelCost = ReforgingMenu.this.getLevelCost(this.getSlotIndex());
            return (sigils < sigilCost || mats < matCost || levels < levelCost) && !ReforgingMenu.this.player.isCreative() ? false : super.mayPickup(playerIn);
         } else {
            return false;
         }
      }

      public void onTake(Player player, ItemStack stack) {
         if (!player.level().isClientSide) {
            ReforgingMenu.this.getSlot(0).set(ItemStack.EMPTY);
            if (!player.isCreative()) {
               int sigilCost = ReforgingMenu.this.getSigilCost(this.getSlotIndex());
               int matCost = ReforgingMenu.this.getMatCost(this.getSlotIndex());
               int levelCost = ReforgingMenu.this.getLevelCost(this.getSlotIndex());
               ReforgingMenu.this.getSlot(1).getItem().shrink(matCost);
               ReforgingMenu.this.getSlot(2).getItem().shrink(sigilCost);
               EnchantmentUtils.chargeExperience(player, ApothMiscUtil.getExpCostForSlot(levelCost, this.getSlotIndex()));
            }

            player.getPersistentData().putInt(REFORGE_SEED, player.getRandom().nextInt());
            ReforgingMenu.this.updateSeed();
         }

         player.playSound(AscEq.Sounds.REFORGE_ITEM_REFORGED.value(), 0.25F, player.getRandom().nextFloat() * 0.15F + 1.0F);
      }
   }
}
