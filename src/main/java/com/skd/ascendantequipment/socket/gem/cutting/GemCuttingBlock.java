package com.skd.ascendantequipment.socket.gem.cutting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GemCuttingBlock extends HorizontalDirectionalBlock {
   public static final Component NAME = Component.translatable("menu.ascendant_equipment.gem_cutting");
   public static final VoxelShape SHAPE = Shapes.or(
      box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
      new VoxelShape[]{
         box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0), box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0), box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0), box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0)
      }
   );

   public GemCuttingBlock(Properties props) {
      super(props);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      player.openMenu(state.getMenuProvider(level, pos));
      return InteractionResult.CONSUME;
   }

   public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
      return new SimpleMenuProvider((id, pInv, player) -> new GemCuttingMenu(id, pInv, ContainerLevelAccess.create(world, pos)), NAME);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return null;
   }
}
