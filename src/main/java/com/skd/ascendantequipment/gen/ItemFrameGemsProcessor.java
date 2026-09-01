package com.skd.ascendantequipment.gen;

import com.mojang.serialization.MapCodec;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;

public class ItemFrameGemsProcessor extends StructureProcessor {
   public static final MapCodec<ItemFrameGemsProcessor> CODEC = CommonToolkitCodecs.setOf(Purity.CODEC)
      .optionalFieldOf("purities", Set.of())
      .xmap(ItemFrameGemsProcessor::new, i -> i.purities);
   protected final Set<Purity> purities;

   public ItemFrameGemsProcessor(Set<Purity> purities) {
      this.purities = purities;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return AscEq.Features.ITEM_FRAME_GEMS;
   }

   public StructureEntityInfo processEntity(
      LevelReader world,
      BlockPos seedPos,
      StructureEntityInfo rawEntityInfo,
      StructureEntityInfo entityInfo,
      StructurePlaceSettings placementSettings,
      StructureTemplate template
   ) {
      CompoundTag entityNBT = entityInfo.nbt;
       String id = entityNBT.getString("id");
      if (world instanceof ServerLevelAccessor sla && "minecraft:item_frame".equals(id)) {
         this.writeEntityNBT(sla.getLevel(), entityInfo.blockPos, placementSettings.getRandom(entityInfo.blockPos), entityNBT, placementSettings);
      }

      return entityInfo;
   }

   protected void writeEntityNBT(ServerLevel level, BlockPos pos, RandomSource rand, CompoundTag nbt, StructurePlaceSettings settings) {
      Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1.0, false);
      GenContext ctx = player != null ? GenContext.forPlayerAtPos(rand, player, pos) : GenContext.standalone(rand, WorldTier.HAVEN, 0.0F, level, pos);
      Gem gem = GemRegistry.INSTANCE.getRandomItem(ctx);
      if (gem != null) {
         Purity purity = Purity.random(ctx, this.purities);
         ItemStack stack = gem.toStack(purity);
         nbt.put("Item", stack.save(level.registryAccess()));
      }

      nbt.putInt("TileX", pos.getX());
      nbt.putInt("TileY", pos.getY());
      nbt.putInt("TileZ", pos.getZ());
   }
}
