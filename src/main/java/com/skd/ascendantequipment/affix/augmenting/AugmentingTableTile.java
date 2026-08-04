package com.skd.ascendantequipment.affix.augmenting;

import com.skd.ascendantequipment.AscEq;
import com.skd.commontoolkit.block_entity.TickingBlockEntity;
import com.skd.commontoolkit.cap.InternalItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class AugmentingTableTile extends BlockEntity implements TickingBlockEntity, Clearable {
   public static int RISE_TIME = 30;
   public static int SPIN_CYCLE_TIME = 90;
   public int time = 0;
   public AugmentingTableTile.AnimationStage stage = AugmentingTableTile.AnimationStage.HIDING;
   protected InternalItemHandler inv = new InternalItemHandler(1) {
      public boolean isValid(int index, ItemResource resource) {
         return resource.is(AscEq.Items.SIGIL_OF_ENHANCEMENT);
      }

      protected void onContentsChanged(int index, ItemStack previousContents) {
         AugmentingTableTile.this.setChanged();
      }
   };

   public AugmentingTableTile(BlockPos pPos, BlockState pBlockState) {
      super(AscEq.Tiles.AUGMENTING_TABLE, pPos, pBlockState);
   }

   public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
      Player player = pLevel.getNearestPlayer(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, 4.0, false);
      switch (this.stage) {
         case HIDING:
            if (player != null) {
               this.stage = AugmentingTableTile.AnimationStage.RISING;
               this.time = 0;
            }
            break;
         case RISING:
            if (player != null) {
               this.time++;
               if (this.time >= RISE_TIME) {
                  this.stage = AugmentingTableTile.AnimationStage.SPINNING;
                  this.time = 0;
               }
            } else {
               this.stage = AugmentingTableTile.AnimationStage.FALLING;
            }
            break;
         case FALLING:
            if (player != null) {
               this.stage = AugmentingTableTile.AnimationStage.RISING;
            } else {
               this.time--;
               if (this.time <= 0) {
                  this.stage = AugmentingTableTile.AnimationStage.HIDING;
                  this.time = 0;
               }
            }
            break;
         case SPINNING:
            this.time++;
            if (player == null && this.time % SPIN_CYCLE_TIME == 0) {
               this.stage = AugmentingTableTile.AnimationStage.FALLING;
               this.time = RISE_TIME;
            }
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putChild("inventory", this.inv);
   }

   protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.readChild("inventory", this.inv);
   }

   public ResourceHandler<ItemResource> getInventory() {
      return this.inv;
   }

   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      if (this.level != null) {
         for (int i = 0; i < this.inv.size(); i++) {
            ItemResource res = (ItemResource)this.inv.getResource(i);
            int amount = this.inv.getAmountAsInt(i);
            if (!res.isEmpty() && amount > 0) {
               Block.popResource(this.level, pos, res.toStack(amount));
            }
         }
      }
   }

   public void clearContent() {
      for (int i = 0; i < this.inv.size(); i++) {
         this.inv.set(i, ItemResource.EMPTY, 0);
      }
   }

   public enum AnimationStage {
      HIDING,
      RISING,
      FALLING,
      SPINNING;
   }
}
