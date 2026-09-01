package com.skd.ascendantequipment.affix.augmenting;

import com.skd.commontoolkit.block_entity.TickingEntityBlock;
import com.skd.commontoolkit.menu.MenuUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AugmentingTableBlock extends Block implements TickingEntityBlock {
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

   public AugmentingTableBlock(Properties pProperties) {
      super(pProperties);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return MenuUtil.openGui(player, pos, AugmentingMenu::new);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new AugmentingTableTile(pPos, pState);
   }
}
