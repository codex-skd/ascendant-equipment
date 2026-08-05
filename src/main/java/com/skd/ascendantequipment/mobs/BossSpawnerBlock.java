package com.skd.ascendantequipment.mobs;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.block_entity.TickingBlockEntity;
import com.skd.commontoolkit.block_entity.TickingEntityBlock;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BossSpawnerBlock extends Block implements TickingEntityBlock {
   private static final VoxelShape OCC_SHAPE = Shapes.box(0.0, 0.0, 0.0, 0.0, 15.99, 0.0);

   public BossSpawnerBlock(Properties properties) {
      super(properties);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BossSpawnerBlock.BossSpawnerTile(pPos, pState);
   }

   protected VoxelShape getOcclusionShape(BlockState pState) {
      return OCC_SHAPE;
   }

   public static class BossSpawnerTile extends BlockEntity implements TickingBlockEntity {
      protected DynamicHolder<Invader> item = InvaderRegistry.INSTANCE.emptyHolder();
      protected int ticks = 0;

      public BossSpawnerTile(BlockPos pos, BlockState state) {
         super(AscEq.Tiles.BOSS_SPAWNER, pos, state);
      }

      public void serverTick(Level pLevel, BlockPos pos, BlockState pState) {
         if (this.ticks++ % 40 == 0) {
            Optional<Player> opt = this.level
               .getEntities(EntityTypes.PLAYER, new AABB(this.worldPosition).inflate(8.0, 8.0, 8.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR)
               .stream()
               .findFirst();
            opt.ifPresent(
               player -> {
                  this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
                  GenContext ctx = GenContext.forPlayerAtPos(this.level.getRandom(), player, pos);
                  Invader bossItem = !this.item.isBound() ? InvaderRegistry.INSTANCE.getRandomItem(ctx) : this.item.get();
                  if (bossItem == null) {
                     AscendantEquipment.LOGGER
                        .error(
                           "A boss spawner attempted to spawn a boss at {} in {}, but no bosses were available!",
                           this.getBlockPos(),
                           this.level.dimension().identifier()
                        );
                  } else {
                     Mob entity = bossItem.createBoss((ServerLevel)this.level, pos, ctx);
                     entity.setTarget(player);
                     entity.setPersistenceRequired();
                     ((ServerLevel)this.level).addFreshEntityWithPassengers(entity);
                  }
               }
            );
         }
      }

      public void setBossItem(DynamicHolder<Invader> item) {
         this.item = item;
      }

      protected void saveAdditional(ValueOutput output) {
         if (this.item != null) {
            output.putString("boss_item", this.item.getId().toString());
         }

         super.saveAdditional(output);
      }

      protected void loadAdditional(ValueInput input) {
         input.getString("boss_item").ifPresent(id -> this.item = InvaderRegistry.INSTANCE.holder(Identifier.tryParse(id)));
         super.loadAdditional(input);
      }
   }
}
