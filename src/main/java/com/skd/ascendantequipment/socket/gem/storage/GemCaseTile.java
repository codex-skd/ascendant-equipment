package com.skd.ascendantequipment.socket.gem.storage;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemItem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import com.skd.commontoolkit.block_entity.TickingBlockEntity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.network.VanillaPacketDispatcher;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public abstract class GemCaseTile extends BlockEntity implements TickingBlockEntity {
   protected final Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> gems = new Object2ObjectLinkedOpenHashMap();
   protected final Set<GemCaseMenu> activeContainers = new HashSet<>();
   protected final ResourceHandler<ItemResource> itemHandler = new GemCaseTile.GemCaseItemHandler();
   protected final int maxCount;
   private final Int2ObjectMap<UnsocketedGem> slotIndicies = new Int2ObjectOpenHashMap();
   private GemCaseAnimationState animationState;

   public GemCaseTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCount) {
      super(type, pos, state);
      this.maxCount = maxCount;
   }

   public void depositGem(ItemStack stack) {
      UnsocketedGem gem = UnsocketedGem.of(stack);
      if (gem.isValid()) {
         Purity purity = gem.purity();
         EnumMap<Purity, Integer> map = this.getGems(gem.gem());
         map.put(purity, Math.min(this.maxCount, map.get(purity) + stack.getCount()));
         if (!this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
         }

         this.setChanged();
      }
   }

   public ItemStack extractGem(DynamicHolder<Gem> gem, Purity purity, int count) {
      EnumMap<Purity, Integer> map = this.getGems(gem);
      int stored = map.get(purity);
      if (stored < count) {
         count = stored;
      }

      if (count > 0 && gem.isBound()) {
         map.put(purity, stored - count);
         ItemStack stack = GemItem.createStack((Gem)gem.get(), purity, count);
         if (!this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
         }

         this.setChanged();
         return stack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean upgradeGem(DynamicHolder<Gem> gem, Purity purity, Container matInv) {
      GemUpgradeMatch match = this.getUpgradeMatch(gem, purity, matInv);
      if (match != null) {
         match.execute(matInv, this.getGems(gem));
         if (!this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
         }

         this.setChanged();
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public GemUpgradeMatch getUpgradeMatch(DynamicHolder<Gem> gem, Purity purity, Container matInv) {
      EnumMap<Purity, Integer> map = this.getGems(gem);
      return map.get(purity) >= this.maxCount ? null : GemUpgradeMatch.findMatch(this.level, purity, map, matInv);
   }

   public int getCount(DynamicHolder<Gem> gem, Purity purity) {
      return this.getGems(gem).get(purity);
   }

   public int getCount(Gem gem, Purity purity) {
      return this.getCount(GemRegistry.INSTANCE.holder(gem), purity);
   }

   protected final EnumMap<Purity, Integer> getGems(DynamicHolder<Gem> gem) {
      return (EnumMap<Purity, Integer>)this.gems.computeIfAbsent(gem, g -> {
         EnumMap<Purity, Integer> map = new EnumMap<>(Purity.class);

         for (Purity p : Purity.values()) {
            map.put(p, 0);
         }

         return map;
      });
   }

   public GemCaseAnimationState getAnimationState() {
      if (this.animationState == null) {
         this.animationState = new GemCaseAnimationState(this.level.getRandom());
      }

      return this.animationState;
   }

   public void saveGemData(CompoundTag tag) {
      CompoundTag gems = new CompoundTag();
      ObjectIterator var3 = this.gems.keySet().iterator();

      while (var3.hasNext()) {
         DynamicHolder<Gem> gem = (DynamicHolder<Gem>)var3.next();
         EnumMap<Purity, Integer> map = (EnumMap<Purity, Integer>)this.gems.get(gem);
         CompoundTag purityTag = new CompoundTag();

         for (Purity p : Purity.values()) {
            int count = map.get(p);
            if (count > 0) {
               purityTag.putInt(p.getSerializedName(), count);
            }
         }

         gems.put(gem.getId().toString(), purityTag);
      }

      tag.put("gems", gems);
   }

   public void loadGemData(CompoundTag tag) {
      CompoundTag gems = tag.getCompoundOrEmpty("gems");

      for (String key : gems.keySet()) {
         Identifier res = Identifier.tryParse(key);
         DynamicHolder<Gem> gem = GemRegistry.INSTANCE.holder(res);
         if (gem.isBound()) {
            CompoundTag purityTag = gems.getCompoundOrEmpty(key);
            if (purityTag.isEmpty()) {
               this.gems.remove(gem);
            } else {
               EnumMap<Purity, Integer> map = new EnumMap<>(Purity.class);

               for (Purity p : Purity.values()) {
                  map.put(p, purityTag.getIntOr(p.getSerializedName(), 0));
               }

               this.gems.put(gem, map);
            }
         }
      }
   }

   public void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      CompoundTag gemTag = new CompoundTag();
      this.saveGemData(gemTag);
      output.store(gemTag);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.read("gems", CompoundTag.CODEC).ifPresent(gems -> {
         CompoundTag wrapper = new CompoundTag();
         wrapper.put("gems", gems);
         this.loadGemData(wrapper);
      });
   }

   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = super.getUpdateTag(registries);
      this.saveGemData(tag);
      return tag;
   }

   public void onDataPacket(Connection net, ValueInput valueInput) {
      valueInput.read("gems", CompoundTag.CODEC).ifPresent(gems -> {
         CompoundTag wrapper = new CompoundTag();
         wrapper.put("gems", gems);
         this.loadGemData(wrapper);
      });
      this.activeContainers.forEach(GemCaseMenu::onChanged);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void clientTick(Level level, BlockPos pos, BlockState state) {
      GemCaseAnimationState animState = this.getAnimationState();
      int uniqueGems = 0;
      ObjectIterator player = this.gems.keySet().iterator();

      while (player.hasNext()) {
         DynamicHolder<Gem> gem = (DynamicHolder<Gem>)player.next();
         int count = 0;

         for (Purity p : Purity.ALL_PURITIES) {
            count += this.getCount(gem, p);
         }

         if (count > 0) {
            uniqueGems++;
         }
      }

      Player playerx = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0, false);
      animState.tick(Math.min(uniqueGems, 16), playerx != null);
   }

   public void addListener(GemCaseMenu ctr) {
      this.activeContainers.add(ctr);
   }

   public void removeListener(GemCaseMenu ctr) {
      this.activeContainers.remove(ctr);
   }

   public ResourceHandler<ItemResource> getItemHandler(Direction dir) {
      return this.itemHandler;
   }

   private UnsocketedGem getGemForSlot(int slot) {
      if (this.slotIndicies.size() != this.gems.size() * Purity.values().length) {
         this.slotIndicies.clear();
         int index = 0;
         ObjectIterator var3 = this.gems.keySet().iterator();

         while (var3.hasNext()) {
            DynamicHolder<Gem> gem = (DynamicHolder<Gem>)var3.next();

            for (Purity p : Purity.values()) {
               this.slotIndicies.put(index++, new UnsocketedGem(gem, p));
            }
         }
      }

      return (UnsocketedGem)this.slotIndicies.getOrDefault(slot, new UnsocketedGem(GemRegistry.INSTANCE.emptyHolder(), Purity.CRACKED));
   }

   public static class BasicGemCaseTile extends GemCaseTile {
      public BasicGemCaseTile(BlockPos pos, BlockState state) {
         super(AscEq.Tiles.GEM_CASE, pos, state, 32767);
      }
   }

   public static class EnderGemCaseTile extends GemCaseTile {
      public EnderGemCaseTile(BlockPos pos, BlockState state) {
         super(AscEq.Tiles.ENDER_GEM_CASE, pos, state, Integer.MAX_VALUE);
      }
   }

   private class GemCaseItemHandler extends SnapshotJournal<GemCaseTile.Snapshot> implements ResourceHandler<ItemResource> {
      public int size() {
         return 1 + GemCaseTile.this.gems.size() * Purity.values().length;
      }

      public ItemResource getResource(int index) {
         if (index > 0 && index < this.size()) {
            UnsocketedGem gem = GemCaseTile.this.getGemForSlot(index - 1);
            if (!gem.gem().isBound()) {
               return ItemResource.EMPTY;
            }

            int count = GemCaseTile.this.getCount(gem.gem(), gem.purity());
            return count <= 0 ? ItemResource.EMPTY : ItemResource.of(GemItem.createStack((Gem)gem.gem().get(), gem.purity(), 1));
         } else {
            return ItemResource.EMPTY;
         }
      }

      public long getAmountAsLong(int index) {
         if (index > 0 && index < this.size()) {
            UnsocketedGem gem = GemCaseTile.this.getGemForSlot(index - 1);
            return !gem.gem().isBound() ? 0L : GemCaseTile.this.getCount(gem.gem(), gem.purity());
         } else {
            return 0L;
         }
      }

      public long getCapacityAsLong(int index, ItemResource resource) {
         if (resource.isEmpty()) {
            return GemCaseTile.this.maxCount;
         } else {
            return !UnsocketedGem.of(resource.toStack()).isValid() ? 0L : GemCaseTile.this.maxCount;
         }
      }

      public boolean isValid(int index, ItemResource resource) {
         return UnsocketedGem.of(resource.toStack()).isValid();
      }

      public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
         if (amount > 0 && index == 0) {
            ItemStack stack = resource.toStack(amount);
            UnsocketedGem gem = UnsocketedGem.of(stack);
            if (!gem.isValid()) {
               return 0;
            }

            EnumMap<Purity, Integer> map = GemCaseTile.this.getGems(gem.gem());
            int stored = map.get(gem.purity());
            int inserted = Math.min(amount, GemCaseTile.this.maxCount - stored);
            if (inserted <= 0) {
               return 0;
            }

            this.updateSnapshots(transaction);
            map.put(gem.purity(), stored + inserted);
            return inserted;
         } else {
            return 0;
         }
      }

      public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
         if (index > 0 && index < this.size() && amount > 0) {
            UnsocketedGem slotGem = GemCaseTile.this.getGemForSlot(index - 1);
            if (!slotGem.gem().isBound()) {
               return 0;
            }

            UnsocketedGem reqGem = UnsocketedGem.of(resource.toStack());
            if (reqGem.isValid() && reqGem.gem() == slotGem.gem() && reqGem.purity() == slotGem.purity()) {
               EnumMap<Purity, Integer> map = GemCaseTile.this.getGems(slotGem.gem());
               int stored = map.get(slotGem.purity());
               int extracted = Math.min(amount, stored);
               if (extracted <= 0) {
                  return 0;
               }

               this.updateSnapshots(transaction);
               map.put(slotGem.purity(), stored - extracted);
               return extracted;
            } else {
               return 0;
            }
         } else {
            return 0;
         }
      }

      protected GemCaseTile.Snapshot createSnapshot() {
         Object2ObjectLinkedOpenHashMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> copy = new Object2ObjectLinkedOpenHashMap();
         ObjectIterator var2 = GemCaseTile.this.gems.object2ObjectEntrySet().iterator();

         while (var2.hasNext()) {
            Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>> e = (Entry<DynamicHolder<Gem>, EnumMap<Purity, Integer>>)var2.next();
            copy.put((DynamicHolder)e.getKey(), new EnumMap((EnumMap)e.getValue()));
         }

         return new GemCaseTile.Snapshot(copy);
      }

      protected void revertToSnapshot(GemCaseTile.Snapshot snapshot) {
         GemCaseTile.this.gems.clear();
         GemCaseTile.this.gems.putAll(snapshot.gems());
      }

      protected void onRootCommit(GemCaseTile.Snapshot originalState) {
         if (!GemCaseTile.this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(GemCaseTile.this);
         }

         GemCaseTile.this.setChanged();
      }
   }

   private record Snapshot(Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> gems) {
   }
}
