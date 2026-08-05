package com.skd.ascendantequipment.compat.gateways;

import com.mojang.serialization.Codec;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.mobs.util.AffixData;
import com.skd.ascendantequipment.tiers.GenContext;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item.TooltipContext;

public record AffixWaveModifier(AffixData data) implements WaveModifier {
   public static final Codec<AffixWaveModifier> CODEC = AffixData.CODEC.xmap(AffixWaveModifier::new, AffixWaveModifier::data);

   public AffixWaveModifier(float chance, Set<LootRarity> rarities) {
      this(new AffixData(chance, rarities));
   }

   public Codec<? extends WaveModifier> getCodec() {
      return CODEC;
   }

   public void apply(LivingEntity entity, GatewayEntity gate) {
      if (entity instanceof Mob mob) {
         GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
         this.data.applyTo(mob, ctx, 0, false);
      }
   }

   public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
      list.accept(AscendantEquipment.lang("modifier", "affix"));
   }

   public static AffixWaveModifier create(float chance, Set<LootRarity> rarities) {
      return new AffixWaveModifier(chance, rarities);
   }

   public static AffixWaveModifier create() {
      return create(1.0F, Set.of());
   }
}
