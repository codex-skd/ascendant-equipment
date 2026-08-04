package com.skd.ascendantequipment.affix.salvaging;

import com.skd.ascendantequipment.AscEq;
import com.skd.commontoolkit.cap.InternalItemHandler;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SalvagingTableTile extends BlockEntity implements Clearable {
   protected final InternalItemHandler output = new InternalItemHandler(6);
   protected final ResourceHandler<ItemResource> itemHandler = new SalvagingTableTile.SalvagingItemHandler();

   public SalvagingTableTile(BlockPos pPos, BlockState pBlockState) {
      super(AscEq.Tiles.SALVAGING_TABLE, pPos, pBlockState);
   }

   public ResourceHandler<ItemResource> getItemHandler() {
      return this.itemHandler;
   }

   protected void saveAdditional(ValueOutput out) {
      super.saveAdditional(out);
      out.putChild("output", this.output);
   }

   public void loadAdditional(ValueInput in) {
      super.loadAdditional(in);
      in.readChild("output", this.output);
   }

   public void clearContent() {
      for (int i = 0; i < this.output.size(); i++) {
         this.output.set(i, ItemResource.EMPTY, 0);
      }
   }

   protected class SalvagingItemHandler implements ResourceHandler<ItemResource> {
      public int size() {
         return 1 + SalvagingTableTile.this.output.size();
      }

      public ItemResource getResource(int index) {
         return index == 0 ? ItemResource.EMPTY : (ItemResource)SalvagingTableTile.this.output.getResource(index - 1);
      }

      public long getAmountAsLong(int index) {
         return index == 0 ? 0L : SalvagingTableTile.this.output.getAmountAsLong(index - 1);
      }

      public long getCapacityAsLong(int index, ItemResource resource) {
         if (index == 0) {
            return this.isValid(index, resource) ? 1L : 0L;
         } else {
            return SalvagingTableTile.this.output.getCapacityAsLong(index - 1, resource);
         }
      }

      public boolean isValid(int index, ItemResource resource) {
         return index == 0 ? !SalvagingMenu.findMatch(SalvagingTableTile.this.level, resource.toStack()).isEmpty() : false;
      }

      public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
         if (index == 0 && amount > 0) {
            ItemStack inStack = resource.toStack(1);
            List<ItemStack> outputs = SalvagingMenu.getSalvageResults(SalvagingTableTile.this.level, inStack);
            if (outputs.isEmpty()) {
               return 0;
            }

            Transaction probe = Transaction.open(transaction);

            try {
               for (ItemStack out : outputs) {
                  int remaining = out.getCount();
                  ItemResource outRes = ItemResource.of(out);

                  for (int i = 0; i < 6 && remaining > 0; i++) {
                     remaining -= SalvagingTableTile.this.output.insert(i, outRes, remaining, probe);
                  }

                  if (remaining > 0) {
                     return 0;
                  }
               }

               probe.commit();
               return 1;
            } finally {
               probe.close();
            }
         } else {
            return 0;
         }
      }

      public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
         return index == 0 ? 0 : SalvagingTableTile.this.output.extract(index - 1, resource, amount, transaction);
      }
   }
}
