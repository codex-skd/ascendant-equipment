package com.skd.ascendantequipment.socket.gem.storage;

import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.block_entity.TickingEntityBlock;
import com.skd.commontoolkit.menu.MenuUtil;
import com.skd.commontoolkit.menu.SimplerMenuProvider;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GemCaseBlock extends HorizontalDirectionalBlock implements TickingEntityBlock {
   public static final Component NAME = AscendantEquipment.lang("menu", "gem_safe");
   public static final VoxelShape SHAPE = Shapes.join(box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0), box(1.0, 13.0, 1.0, 15.0, 15.0, 15.0), BooleanOp.ONLY_FIRST);
   protected final BlockEntitySupplier<? extends GemCaseTile> tileSupplier;
   protected final int maxCount;
   private static DecimalFormat f = new DecimalFormat("##.#");

   public GemCaseBlock(BlockEntitySupplier<? extends GemCaseTile> tileSupplier, Properties props, int maxCount) {
      super(props);
      this.tileSupplier = tileSupplier;
      this.maxCount = maxCount;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return MenuUtil.openGui(player, pos, GemCaseMenu::new);
   }

   public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
      return new SimplerMenuProvider(world, pos, GemCaseMenu::new);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return (BlockState)this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return this.tileSupplier.create(pPos, pState);
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
      ItemStack s = new ItemStack(this);
      BlockEntity te = level.getBlockEntity(pos);
      if (te != null && includeData) {
         saveBlockEntityToItem(te, s, level.registryAccess());
      }

      return s;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      TypedEntityData<BlockEntityType<?>> data = (TypedEntityData<BlockEntityType<?>>)stack.get(DataComponents.BLOCK_ENTITY_DATA);
      if (data != null && level.getBlockEntity(pos) instanceof GemCaseTile lib) {
         data.loadInto(lib, level.registryAccess());
      }
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder ctx) {
      ItemStack s = new ItemStack(this);
      BlockEntity te = (BlockEntity)ctx.getParameter(LootContextParams.BLOCK_ENTITY);
      if (te != null) {
         saveBlockEntityToItem(te, s, ctx.getLevel().registryAccess());
      }

      return Arrays.asList(s);
   }

   private static void saveBlockEntityToItem(BlockEntity be, ItemStack stack, Provider registries) {
      ScopedCollector reporter = new ScopedCollector(be.problemPath(), AscendantEquipment.LOGGER);

      try {
         TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
         be.saveCustomOnly(output);
         BlockItem.setBlockEntityData(stack, be.getType(), output);
         stack.applyComponents(be.collectComponents());
      } catch (Throwable var7) {
         try {
            reporter.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      reporter.close();
   }

   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return null;
   }

   static String format(int n) {
      int log = (int)StrictMath.log10(n);
      if (log <= 3) {
         return String.valueOf(n);
      } else if (log <= 6) {
         return f.format(n / 1000.0) + "K";
      } else {
         return log <= 8 ? f.format(n / 1000000.0) + "M" : f.format(n / 1.0E9) + "B";
      }
   }
}
