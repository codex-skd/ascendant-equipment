package com.skd.ascendantequipment.socket.gem.cutting;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.commontoolkit.cap.InternalItemHandler;
import com.skd.commontoolkit.menu.CommonToolkitContainerMenu;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class GemCuttingMenu extends CommonToolkitContainerMenu {
   public static final int BASE_SLOT = 0;
   public static final int TOP_SLOT = 1;
   public static final int LEFT_SLOT = 2;
   public static final int RIGHT_SLOT = 3;
   protected final Player player;
   protected final ContainerLevelAccess access;
   protected final InternalItemHandler inv = new InternalItemHandler(4) {
      protected int getCapacity(int index, ItemResource resource) {
         return index == 0 ? 1 : super.getCapacity(index, resource);
      }
   };
   protected final GemCuttingRecipe.CuttingRecipeInput rInput = new GemCuttingRecipe.CuttingRecipeInput(this.inv);
   @Nullable
   Runnable slotChangedCallback = null;

   public GemCuttingMenu(int id, Inventory playerInv) {
      this(id, playerInv, ContainerLevelAccess.NULL);
   }

   public GemCuttingMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
      super(AscEq.Menus.GEM_CUTTING, id, playerInv);
      this.player = playerInv.player;
      this.access = access;
      this.addSlot(new UpdatingSlot(this.inv, 0, 62, 45, this::isValidBase));
      this.addSlot(new UpdatingSlot(this.inv, 1, 62, 12, this::isValidTop));
      this.addSlot(new UpdatingSlot(this.inv, 2, 33, 64, this::isValidLeft));
      this.addSlot(new UpdatingSlot(this.inv, 3, 90, 64, this::isValidRight));
      this.addPlayerSlots(playerInv, 8, 98);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidBase(stack) && !this.getSlot(0).hasItem(), 0, 1);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidTop(stack), 1, 2);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidLeft(stack), 2, 3);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && this.isValidRight(stack), 3, 4);
      this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
      this.registerInvShuffleRules();
   }

   public boolean clickMenuButton(Player player, int id) {
      if (id == 0) {
         for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
            GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
            if (r.matches(this.rInput, player.level())) {
               ItemStack out = r.assemble(this.rInput);
               r.decrementInputs(this.rInput, player.level());
               this.inv.set(0, ItemResource.of(out), out.getCount());
               this.level
                  .playSound(
                     player,
                     player.blockPosition(),
                     SoundEvents.AMETHYST_BLOCK_BREAK,
                     SoundSource.BLOCKS,
                     1.0F,
                     1.5F + 0.35F * (1.0F - 2.0F * this.level.getRandom().nextFloat())
                  );
               AscEq.Triggers.GEM_CUTTING.trigger((ServerPlayer)player, out);
               return true;
            }
         }
      }

      return false;
   }

   public boolean isValidBase(ItemStack stack) {
      for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
         GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
         if (r.isValidBaseItem(this.rInput, stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean isValidTop(ItemStack stack) {
      for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
         GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
         if (r.isValidTopItem(this.rInput, stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean isValidLeft(ItemStack stack) {
      for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
         GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
         if (r.isValidLeftItem(this.rInput, stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean isValidRight(ItemStack stack) {
      for (RecipeHolder<GemCuttingRecipe> holder : getRecipes(this.level)) {
         GemCuttingRecipe r = (GemCuttingRecipe)holder.value();
         if (r.isValidRightItem(this.rInput, stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean stillValid(Player pPlayer) {
      return (Boolean)this.access.evaluate((level, pos) -> level.getBlockState(pos).is(AscEq.Blocks.GEM_CUTTING_TABLE), true);
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((level, pos) -> this.clearContainer(pPlayer, this.inv));
   }

   public void slotsChanged(Container container) {
      super.slotsChanged(container);
      if (this.slotChangedCallback != null) {
         this.slotChangedCallback.run();
      }
   }

   public static int getDustCost(Purity purity) {
      return 1 + purity.ordinal() * 2;
   }

   public static List<RecipeHolder<GemCuttingRecipe>> getRecipes(Level level) {
      return level.isClientSide()
         ? GemCuttingRecipeCache.all()
         : List.copyOf(level.getServer().getRecipeManager().recipeMap().byType(AscEq.RecipeTypes.GEM_CUTTING));
   }
}
