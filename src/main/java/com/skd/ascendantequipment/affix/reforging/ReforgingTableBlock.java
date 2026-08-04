package com.skd.ascendantequipment.affix.reforging;

import com.skd.commontoolkit.block_entity.TickingEntityBlock;
import com.skd.commontoolkit.menu.MenuUtil;
import com.skd.commontoolkit.menu.SimplerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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

public class ReforgingTableBlock extends Block implements TickingEntityBlock {
   public static final Component TITLE = Component.translatable("container.ascendant_equipment.reforge");
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

   public ReforgingTableBlock(Properties properties) {
      super(properties);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return MenuUtil.openGui(player, pos, ReforgingMenu::new);
   }

   public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
      return new SimplerMenuProvider(world, pos, ReforgingMenu::new);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new ReforgingTableTile(pPos, pState);
   }
}
