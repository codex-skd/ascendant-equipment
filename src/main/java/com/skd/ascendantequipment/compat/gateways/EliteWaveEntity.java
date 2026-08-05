package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.registries.EliteRegistry;
import com.skd.ascendantequipment.mobs.types.Elite;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public record EliteWaveEntity(WaveEntity base, DynamicHolder<Elite> elite, Optional<String> desc) implements WaveEntity {
   public static Codec<EliteWaveEntity> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            WaveEntity.CODEC.fieldOf("base_entity").forGetter(EliteWaveEntity::base),
            EliteRegistry.INSTANCE.holderCodec().fieldOf("elite").forGetter(EliteWaveEntity::elite),
            Codec.STRING.optionalFieldOf("desc").forGetter(EliteWaveEntity::desc)
         )
         .apply(inst, EliteWaveEntity::new)
   );

   public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
      LivingEntity baseEntity = this.base.createEntity(level, gate);
      if (baseEntity == null) {
         return null;
      } else if (!this.elite.isBound()) {
         AscendantEquipment.LOGGER.error("An EliteWaveEntity has an ubound elite holder {}!", this.elite);
         return null;
      } else if (baseEntity instanceof Mob mob) {
         mob.getPersistentData().putString("apoth.miniboss", this.elite.getId().toString());
         mob.getPersistentData().putString("apoth.miniboss.player", gate.summonerOrClosest().getUUID().toString());
         return baseEntity;
      } else {
         AscendantEquipment.LOGGER.error("An EliteWaveEntity tried to apply an elite to a non-Mob entity: {}!", baseEntity);
         return baseEntity;
      }
   }

   public MutableComponent getDescription() {
      return this.desc.isPresent()
         ? Component.translatable(this.desc.get(), new Object[]{this.base.getDescription()})
         : AscendantEquipment.lang("wave_entity", "elite", this.base.getDescription());
   }

   public boolean shouldFinalizeSpawn() {
      return false;
   }

   public Codec<? extends WaveEntity> getCodec() {
      return CODEC;
   }

   public int getCount() {
      return this.base.getCount();
   }
}
