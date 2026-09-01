package com.skd.ascendantequipment.affix.reforging;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.commontoolkit.block_entity.TickingBlockEntity;
import com.skd.commontoolkit.cap.InternalItemHandler;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ReforgingTableTile extends BlockEntity implements TickingBlockEntity, Clearable {
   public int time = 0;
   public boolean step1 = true;
   protected InternalItemHandler inv = new InternalItemHandler(2) {
      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
         return slot == 0 ? ReforgingTableTile.this.isValidRarityMat(stack) : stack.is(AscEq.Items.SIGIL_OF_REBIRTH);
      }

      @Override
      protected void onContentsChanged(int slot) {
         ReforgingTableTile.this.setChanged();
      }
   };

   public ReforgingTableTile(BlockPos pWorldPosition, BlockState pBlockState) {
      super(AscEq.Tiles.REFORGING_TABLE, pWorldPosition, pBlockState);
   }

   public boolean isValidRarityMat(ItemStack stack) {
      DynamicHolder<LootRarity> rarity = RarityRegistry.getMaterialRarity(stack.getItem());
      return rarity.isBound() && this.getRecipeFor(rarity.get()) != null;
   }

   @Nullable
   @SuppressWarnings("deprecation")
   public ReforgingRecipe getRecipeFor(LootRarity rarity) {
      if (this.level == null) {
         return null;
      }

      Collection<RecipeHolder<ReforgingRecipe>> recipes;
      if (this.level.isClientSide) {
         recipes = ReforgingRecipeCache.all();
      } else {
         recipes = this.level.getRecipeManager().getAllRecipesFor(AscEq.RecipeTypes.REFORGING);
      }

      return recipes.stream()
         .<ReforgingRecipe>map(RecipeHolder::value)
         .filter(r -> r.rarity().get() == rarity && r.tables().contains(this.getBlockState().getBlock().builtInRegistryHolder()))
         .findFirst()
         .orElse(null);
   }

   @Override
   public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
      Player player = pLevel.getNearestPlayer(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, 4.0, false);
      if (player != null) {
         this.time++;
      } else {
         if (this.time == 0 && this.step1) {
            return;
         }

         this.time++;
      }

      if (this.step1 && this.time == 59) {
         this.step1 = false;
         this.time = 0;
      } else if (this.time == 4 && !this.step1) {
         RandomSource rand = pLevel.random;

         for (int i = 0; i < 6; i++) {
            pLevel.addParticle(
               ParticleTypes.CRIT,
               pPos.getX() + 0.5 - 0.1 * rand.nextDouble(),
               pPos.getY() + 13 / 16.0,
               pPos.getZ() + 0.5 + 0.1 * rand.nextDouble(),
               0.0,
               0.0,
               0.0
            );
         }

         pLevel.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.03F, 1.7F + rand.nextFloat() * 0.2F, true);
         this.step1 = true;
         this.time = 0;
      }
   }

   @Override
   protected void saveAdditional(CompoundTag tag, Provider regs) {
      super.saveAdditional(tag, regs);
      tag.put("inventory", this.inv.serializeNBT(regs));
   }

   @Override
   protected void loadAdditional(CompoundTag tag, Provider regs) {
      super.loadAdditional(tag, regs);
      this.inv.deserializeNBT(regs, tag.getCompound("inventory"));
   }

   public IItemHandler getInventory() {
      return this.inv;
   }

   @Override
   public void clearContent() {
      for (int i = 0; i < this.inv.getSlots(); i++) {
         this.inv.setStackInSlot(i, ItemStack.EMPTY);
      }
   }
}
