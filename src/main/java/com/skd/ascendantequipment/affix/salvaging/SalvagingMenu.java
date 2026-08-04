package com.skd.ascendantequipment.affix.salvaging;

import com.google.common.base.Predicates;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.cap.InternalItemHandler;
import com.skd.commontoolkit.menu.BlockEntityMenu;
import com.skd.commontoolkit.menu.FilteredSlot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class SalvagingMenu extends BlockEntityMenu<SalvagingTableTile> {
   protected final Player player;
   protected final InternalItemHandler inputInv = new InternalItemHandler(12);

   public SalvagingMenu(int id, Inventory inv, BlockPos pos) {
      super(AscEq.Menus.SALVAGE, id, inv, pos);
      this.player = inv.player;
      int leftOffset = 17;
      int topOffset = 17;

      for (int i = 0; i < 12; i++) {
         this.addSlot(
            new UpdatingSlot(
               this.inputInv, i, leftOffset + i % 4 * 19, topOffset + i / 4 * 19, s -> this.level.isClientSide() || !findMatch(this.level, s).isEmpty()
            ) {
               public int getMaxStackSize() {
                  return 1;
               }

               public int getMaxStackSize(ItemStack stack) {
                  return 1;
               }
            }
         );
      }

      for (int i = 0; i < 6; i++) {
         this.addSlot(new FilteredSlot(((SalvagingTableTile)this.tile).output, i, 124 + i % 2 * 19, 17 + i / 2 * 19, Predicates.alwaysFalse()));
      }

      this.addPlayerSlots(inv, 8, 92);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && !findMatch(this.level, stack).isEmpty(), 0, 12);
      this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
      this.registerInvShuffleRules();
   }

   public boolean stillValid(Player player) {
      return this.level.isClientSide() ? true : this.level.getBlockState(this.pos).is(AscEq.Blocks.SALVAGING_TABLE);
   }

   public void removed(Player player) {
      super.removed(player);
      if (!this.level.isClientSide()) {
         this.clearContainer(player, this.inputInv);
      }
   }

   public boolean clickMenuButton(Player player, int id) {
      if (id == 0) {
         this.salvageAll();
         this.level
            .playSound(
               null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.getRandom().nextFloat() * 0.25F + 1.0F
            );
         this.level
            .playSound(
               null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.getRandom().nextFloat() * 0.2F + 0.8F
            );
         this.level
            .playSound(
               null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.getRandom().nextFloat() * 0.5F + 0.75F
            );
         return true;
      } else {
         return super.clickMenuButton(player, id);
      }
   }

   protected void giveItem(Player player, ItemStack stack) {
      if (player.isAlive() && (!(player instanceof ServerPlayer) || !((ServerPlayer)player).hasDisconnected())) {
         Inventory inventory = player.getInventory();
         if (inventory.player instanceof ServerPlayer) {
            inventory.placeItemBackInInventory(stack);
         }
      } else {
         player.drop(stack, false);
      }
   }

   protected void salvageAll() {
      for (int inSlot = 0; inSlot < 12; inSlot++) {
         Slot s = this.getSlot(inSlot);
         ItemStack stack = s.getItem();
         List<ItemStack> outputs = getSalvageResults(this.level, stack);
         s.set(ItemStack.EMPTY);

         try (Transaction tx = Transaction.openRoot()) {
            for (ItemStack out : outputs) {
               ItemResource outRes = ItemResource.of(out);
               int remaining = out.getCount();

               for (int outSlot = 0; outSlot < 6 && remaining > 0; outSlot++) {
                  remaining -= ((SalvagingTableTile)this.tile).output.insert(outSlot, outRes, remaining, tx);
               }

               if (remaining > 0) {
                  this.giveItem(this.player, outRes.toStack(remaining));
               }
            }

            tx.commit();
         }
      }
   }

   public static int getSalvageCount(SalvagingRecipe.OutputData output, ItemStack stack, RandomSource rand) {
      int[] counts = getSalvageCounts(output, stack);
      return rand.nextInt(counts[0], counts[1] + 1);
   }

   public static int[] getSalvageCounts(SalvagingRecipe.OutputData output, ItemStack stack) {
      int[] out = new int[]{output.min(), output.max()};
      if (stack.isDamageableItem()) {
         int maxDmg = stack.getMaxDamage();
         if (maxDmg <= 0) {
            AscendantEquipment.LOGGER
               .warn(
                  "Item {} returned true to ItemStack#isDamageableItem, but returned {} from ItemStack#getMaxDamage, when the value should be positive!",
                  BuiltInRegistries.ITEM.getKey(stack.getItem()),
                  maxDmg
               );
            return out;
         }

         out[1] = Math.max(out[0], Math.round(out[1] * (maxDmg - stack.getDamageValue()) / maxDmg));
      }

      return out;
   }

   public static List<ItemStack> getSalvageResults(Level level, ItemStack stack) {
      List<ItemStack> outputs = new ArrayList<>();

      for (RecipeHolder<SalvagingRecipe> recipe : findMatch(level, stack)) {
         for (SalvagingRecipe.OutputData d : ((SalvagingRecipe)recipe.value()).getOutputs()) {
            ItemStack out = d.stack().create();
            out.setCount(getSalvageCount(d, stack, level.getRandom()));
            outputs.add(out);
         }
      }

      return outputs;
   }

   public static List<ItemStack> getBestPossibleSalvageResults(Level level, ItemStack stack) {
      List<ItemStack> outputs = new ArrayList<>();

      for (RecipeHolder<SalvagingRecipe> recipe : findMatch(level, stack)) {
         for (SalvagingRecipe.OutputData d : ((SalvagingRecipe)recipe.value()).getOutputs()) {
            ItemStack out = d.stack().create();
            out.setCount(getSalvageCounts(d, stack)[1]);
            outputs.add(out);
         }
      }

      return outputs;
   }

   public static Collection<RecipeHolder<SalvagingRecipe>> findMatch(Level level, ItemStack stack) {
      return level.isClientSide()
         ? SalvagingRecipeCache.findMatch(stack)
         : level.getServer()
            .getRecipeManager()
            .recipeMap()
            .byType(AscEq.RecipeTypes.SALVAGING)
            .stream()
            .filter(r -> ((SalvagingRecipe)r.value()).getInput().test(stack))
            .toList();
   }
}
