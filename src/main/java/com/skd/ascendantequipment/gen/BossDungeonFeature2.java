package com.skd.ascendantequipment.gen;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.EquipmentConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class BossDungeonFeature2 extends Feature<SuccessChanceFeatureConfig> {
   public static final Identifier TEMPLATE_ID = AscendantEquipment.loc("boss_1");
   protected static int xRadius = 4;
   protected static int floor = -1;
   protected static int roof = 3;
   protected static int roofTop = 6;
   protected static int zRadius = 4;

   public BossDungeonFeature2() {
      super(SuccessChanceFeatureConfig.CODEC);
   }

   public boolean place(FeaturePlaceContext<SuccessChanceFeatureConfig> ctx) {
      WorldGenLevel world = ctx.level();
      BlockPos pos = ctx.origin();
      RandomSource rand = ctx.random();
      if (EquipmentConfig.canGenerateIn(world) && !(rand.nextFloat() > ((SuccessChanceFeatureConfig)ctx.config()).successChance())) {
         BlockState[][][] states = new BlockState[9][8][9];
         int doors = 0;

         for (int x = -xRadius; x <= xRadius; x++) {
            for (int y = floor; y <= roofTop; y++) {
               for (int z = -zRadius; z <= zRadius; z++) {
                  BlockPos blockpos = pos.offset(x, y, z);
                  BlockState state = world.getBlockState(blockpos);
                  boolean flag = state.isSolid();
                  if (y == floor && !flag || y == roof && !flag) {
                     return false;
                  }

                  if (y == roof + 1 && Math.abs(x) < xRadius && Math.abs(z) < zRadius && !flag) {
                     return false;
                  }

                  if (isDoorSpace(x, z) && y == 1 && state.isAir() && states[x + xRadius][y - 1 + 1][z + zRadius].isAir()) {
                     doors++;
                  }

                  states[x + xRadius][y + 1][z + zRadius] = state;
               }
            }
         }

         if (doors >= 3) {
            StructureTemplate template = (StructureTemplate)ServerLifecycleHooks.getCurrentServer().getStructureManager().get(TEMPLATE_ID).get();
            template.placeInWorld(world, pos.offset(-4, -1, -4), pos.offset(-4, -1, -4), new StructurePlaceSettings(), rand, 4);
            boolean rand1 = rand.nextBoolean();
            boolean rand2 = rand.nextBoolean();
            BlockPos chest1 = pos.offset(rand1 ? xRadius - 1 : -xRadius + 1, 0, rand2 ? zRadius - 1 : -zRadius + 1);
            BlockPos chest2 = pos.offset(!rand1 ? xRadius - 1 : -xRadius + 1, 0, !rand2 ? zRadius - 1 : -zRadius + 1);
            world.setBlock(chest1, StructurePiece.reorient(world, chest1, Blocks.CHEST.defaultBlockState()), 2);
            RandomizableContainer.setBlockEntityLootTable(world, rand, chest1, BuiltInLootTables.SIMPLE_DUNGEON);
            world.setBlock(chest2, StructurePiece.reorient(world, chest2, Blocks.CHEST.defaultBlockState()), 2);
            RandomizableContainer.setBlockEntityLootTable(world, rand, chest2, BuiltInLootTables.SIMPLE_DUNGEON);
            world.setBlock(pos, AscEq.Blocks.BOSS_SPAWNER.value().defaultBlockState(), 2);
            AscendantEquipment.debugLog(pos, "Boss Dungeon (Variant 2)");
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean isDoorSpace(int x, int z) {
      return Math.abs(z) == zRadius && x >= -1 && x <= 1 || Math.abs(x) == xRadius && z >= -1 && z <= 1;
   }
}
