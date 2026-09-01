package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record TrueRandomInvaderWaveEntity(int count, Optional<String> desc) implements WaveEntity {
   public static Codec<TrueRandomInvaderWaveEntity> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.intRange(1, 256).optionalFieldOf("count", 1).forGetter(TrueRandomInvaderWaveEntity::count),
            Codec.STRING.optionalFieldOf("desc").forGetter(TrueRandomInvaderWaveEntity::desc)
         )
         .apply(inst, TrueRandomInvaderWaveEntity::new)
   );

   public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
      GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
      Invader realBoss = getTrulyRandomItem(InvaderRegistry.INSTANCE, ctx);
      if (realBoss == null) {
         AscendantEquipment.LOGGER.error("Failed to resolve a random invader when generating a TrueRandomInvaderWaveEntity!");
         return null;
      } else {
         return realBoss.createBoss(level, BlockPos.ZERO, ctx);
      }
   }

   public MutableComponent getDescription() {
      Component desc = AscendantEquipment.lang("wave_entity", "true_random_invader");
      return Gateways.lang("tooltip", "with_count", new Object[]{this.getCount(), desc});
   }

   public boolean shouldFinalizeSpawn() {
      return false;
   }

   public Codec<? extends WaveEntity> getCodec() {
      return CODEC;
   }

   public int getCount() {
      return this.count;
   }

   public static TrueRandomInvaderWaveEntity createRandom(int count) {
      return new TrueRandomInvaderWaveEntity(count, Optional.empty());
   }

   @Nullable
   public static <T extends TieredWeights.Weighted & Constraints.Constrained> T getTrulyRandomItem(DynamicRegistry<T> registry, GenContext ctx) {
      Collection<T> items = registry.getValues();
      List<T> list = new ArrayList<>(items.size());

      for (T item : items) {
         int weight = Math.max(0, item.weights().getWeight(ctx.tier(), ctx.luck()));
         Set<WorldTier> tiers = item.constraints().tiers();
         if (weight > 0 && (tiers.isEmpty() || tiers.contains(ctx.tier()))) {
            list.add(item);
         }
      }

      return list.isEmpty() ? null : list.get(ctx.rand().nextInt(list.size()));
   }
}
