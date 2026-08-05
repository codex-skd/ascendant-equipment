package com.skd.ascendantequipment.gen;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.spawner.RogueSpawner;
import com.skd.ascendantequipment.spawner.RogueSpawnerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class RogueSpawnerFeature extends Feature<SuccessChanceFeatureConfig> {
   public static final RuleTest STONE_TEST = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);

   public RogueSpawnerFeature() {
      super(SuccessChanceFeatureConfig.CODEC);
   }

   public boolean place(FeaturePlaceContext<SuccessChanceFeatureConfig> ctx) {
      WorldGenLevel world = ctx.level();
      BlockPos pos = ctx.origin();
      RandomSource rand = ctx.random();
      if (EquipmentConfig.canGenerateIn(world) && !(rand.nextFloat() > ((SuccessChanceFeatureConfig)ctx.config()).successChance())) {
         BlockState state = world.getBlockState(pos);
         BlockState downState = world.getBlockState(pos.below());
         BlockState upState = world.getBlockState(pos.above());
         if (STONE_TEST.test(downState, rand) && upState.isAir() && (state.isAir() || STONE_TEST.test(state, rand))) {
            RogueSpawner item = RogueSpawnerRegistry.INSTANCE.getRandomItem(rand);
            if (item == null) {
               return false;
            }

            item.place(world, pos, rand);
            AscendantEquipment.debugLog(pos, "Rogue Spawner - " + RogueSpawnerRegistry.INSTANCE.getKey(item));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
