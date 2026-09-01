package com.skd.ascendantequipment.compat.gateways;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.compat.gateways.tiered_gate.TieredGateway;
import com.skd.ascendantequipment.compat.gateways.tiered_gate.TieredGatewayEntity;
import com.skd.commontoolkit.registry.DeferredHelper;
import dev.shadowsoffire.gateways.client.GatewayRenderer;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;

public class GatewaysCompat {
   public static final DeferredHelper R = DeferredHelper.create("ascendant_equipment");
   public static final Supplier<EntityType<TieredGatewayEntity>> TIERED_GATEWAY = R.entity(
      "tiered_gateway",
      () -> Builder.of(TieredGatewayEntity::new, MobCategory.MISC)
         .setTrackingRange(5)
         .setUpdateInterval(20)
         .sized(2.0F, 3.0F)
         .build(ResourceKey.create(Registries.ENTITY_TYPE, AscendantEquipment.loc("tiered_gateway")))
   );

   public static void register(IEventBus bus) {
      WaveEntity.CODEC.register(AscendantEquipment.loc("invader"), InvaderWaveEntity.CODEC);
      WaveEntity.CODEC.register(AscendantEquipment.loc("elite"), EliteWaveEntity.CODEC);
      WaveEntity.CODEC.register(AscendantEquipment.loc("true_random_invader"), TrueRandomInvaderWaveEntity.CODEC);
      Reward.CODEC.register(AscendantEquipment.loc("affix_item"), AffixItemReward.CODEC);
      Reward.CODEC.register(AscendantEquipment.loc("gem"), GemReward.CODEC);
      Reward.CODEC.register(AscendantEquipment.loc("true_random_gem"), TrueRandomGemReward.CODEC);
      WaveModifier.CODEC.register(AscendantEquipment.loc("affix"), AffixWaveModifier.CODEC);
      WaveModifier.CODEC.register(AscendantEquipment.loc("passenger"), PassengerWaveModifier.CODEC);
      GatewayRegistry.SERIALIZER.register(AscendantEquipment.loc("tiered"), TieredGateway.CODEC);
      bus.register(R);
      if (FMLEnvironment.dist.isClient()) {
         bus.register(GatewaysCompat.ClientInternal.class);
      }
   }

   private static class ClientInternal {
      @SubscribeEvent
      public static void eRenders(RegisterRenderers e) {
         e.registerEntityRenderer(GatewaysCompat.TIERED_GATEWAY.get(), GatewayRenderer::new);
      }
   }
}
