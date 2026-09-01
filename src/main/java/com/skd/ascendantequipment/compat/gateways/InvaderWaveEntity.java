package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record InvaderWaveEntity(DynamicHolder<Invader> invader, int count, Optional<String> desc) implements WaveEntity {
   public static Codec<InvaderWaveEntity> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            InvaderRegistry.INSTANCE.holderCodec().optionalFieldOf("invader", InvaderRegistry.INSTANCE.emptyHolder()).forGetter(InvaderWaveEntity::invader),
            Codec.intRange(1, 256).optionalFieldOf("count", 1).forGetter(InvaderWaveEntity::count),
            Codec.STRING.optionalFieldOf("desc").forGetter(InvaderWaveEntity::desc)
         )
         .apply(inst, InvaderWaveEntity::new)
   );

   public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
      GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
      Invader realBoss = this.resolveInvader(ctx);
      if (realBoss == null) {
         if (this.usingRandomInvader()) {
            AscendantEquipment.LOGGER.error("Failed to resolve a random invader when generating an InvaderWaveEntity!");
         } else {
            String type = this.invader.getId().toString();
            AscendantEquipment.LOGGER.error("Failed to resolve the invader '{}' when generating an InvaderWaveEntity!", type);
         }

         return null;
      } else {
         return realBoss.createBoss(level, BlockPos.ZERO, ctx);
      }
   }

   public MutableComponent getDescription() {
      Component desc = AscendantEquipment.lang("wave_entity", "invader", Component.translatable(this.desc.orElse(resolveInvaderDesc(this.invader))));
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

   protected boolean usingRandomInvader() {
      return this.invader.equals(InvaderRegistry.INSTANCE.emptyHolder());
   }

   @Nullable
   protected Invader resolveInvader(GenContext ctx) {
      return this.usingRandomInvader() ? InvaderRegistry.INSTANCE.getRandomItem(ctx) : this.invader().getOptional().orElse(null);
   }

   public static InvaderWaveEntity create(DynamicHolder<Invader> invader, int count, @Nullable String desc) {
      return new InvaderWaveEntity(invader, count, Optional.ofNullable(desc));
   }

   public static InvaderWaveEntity createRandom(int count) {
      return new InvaderWaveEntity(InvaderRegistry.INSTANCE.emptyHolder(), count, Optional.empty());
   }

   private static String resolveInvaderDesc(DynamicHolder<Invader> invader) {
      return invader.isBound() ? invader.get().entity().getDescriptionId() : "misc.ascendant_equipment.random";
   }
}
