package com.skd.ascendantequipment.affix.salvaging;

import com.skd.commontoolkit.menu.MenuUtil;
import com.skd.commontoolkit.menu.SimplerMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class SalvagingTableBlock extends Block implements EntityBlock {
   public SalvagingTableBlock(Properties properties) {
      super(properties);
   }

   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      return new SimplerMenuProvider(pLevel, pPos, SalvagingMenu::new);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return MenuUtil.openGui(player, pos, SalvagingMenu::new);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SalvagingTableTile(pPos, pState);
   }
}
