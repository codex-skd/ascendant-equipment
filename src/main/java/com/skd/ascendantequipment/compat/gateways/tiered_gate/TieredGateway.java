package com.skd.ascendantequipment.compat.gateways.tiered_gate;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.WorldTier;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.BossEventSettings;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.GateRules;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.Gateway.Size;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms.SpawnAlgorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record TieredGateway(
   TieredGateSettings settings, List<Wave> waves, List<Reward> rewards, List<Failure> failures, GateRules rules, BossEventSettings bossSettings
) implements Gateway {
   public static final Codec<TieredGateway> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            TieredGateSettings.CODEC.fieldOf("settings").forGetter(TieredGateway::settings),
            Wave.CODEC.listOf().fieldOf("waves").forGetter(TieredGateway::waves),
            Reward.CODEC.listOf().optionalFieldOf("rewards", Collections.emptyList()).forGetter(TieredGateway::rewards),
            Failure.CODEC.listOf().optionalFieldOf("failures", Collections.emptyList()).forGetter(TieredGateway::failures),
            GateRules.CODEC.optionalFieldOf("rules", GateRules.DEFAULT).forGetter(TieredGateway::rules),
            BossEventSettings.CODEC.optionalFieldOf("boss_event", BossEventSettings.DEFAULT).forGetter(TieredGateway::bossSettings)
         )
         .apply(inst, TieredGateway::new)
   );

   public SpawnAlgorithm spawnAlgo() {
      return this.settings.spawnAlgo();
   }

   public TextColor color() {
      return this.settings.color();
   }

   public Size size() {
      return this.settings.size();
   }

   @Nullable
   public Component canOpen(Player player) {
      WorldTier tier = WorldTier.getTier(player);
      return this.settings.tier() != tier ? AscendantEquipment.lang("error", "gate_tier_incorrect", this.settings.tier().toComponent()) : null;
   }

   public Holder<SoundEvent> soundtrack() {
      return this.settings.soundtrack();
   }

   public GatewayEntity createEntity(Level level, Player summoner) {
      return new TieredGatewayEntity(level, summoner, GatewayRegistry.INSTANCE.holder(this));
   }

   public void appendPearlTooltip(TooltipContext ctx, Consumer<Component> tooltips, TooltipFlag flag) {
      TieredGateClient.appendPearlTooltip(this, ctx, tooltips, flag);
   }

   public void renderBossBar(GatewayEntity gate, Object gfx, int x, int y, boolean isInWorld) {
      TieredGateClient.renderBossBar(gate, gfx, x, y, isInWorld);
   }

   public int getNumWaves() {
      return this.waves.size();
   }

   public Wave getWave(int n) {
      return this.waves.get(n);
   }

   public Codec<TieredGateway> getCodec() {
      return CODEC;
   }

   public static TieredGateway.Builder builder() {
      return new TieredGateway.Builder();
   }

   public static class Builder {
      private TieredGateSettings settings = null;
      private final List<Wave> waves = new ArrayList<>();
      private final List<Reward> rewards = new ArrayList<>();
      private final List<Failure> failures = new ArrayList<>();
      private GateRules rules = GateRules.DEFAULT;
      private BossEventSettings bossSettings = BossEventSettings.DEFAULT;

      public TieredGateway.Builder settings(UnaryOperator<TieredGateSettings.Builder> config) {
         this.settings = config.apply(new TieredGateSettings.Builder()).build();
         return this;
      }

      public TieredGateway.Builder wave(Wave wave) {
         this.waves.add(wave);
         return this;
      }

      public TieredGateway.Builder wave(UnaryOperator<dev.shadowsoffire.gateways.gate.Wave.Builder> config) {
         return this.wave(config.apply(new dev.shadowsoffire.gateways.gate.Wave.Builder()).build());
      }

      public TieredGateway.Builder waves(List<Wave> waves) {
         this.waves.addAll(waves);
         return this;
      }

      public TieredGateway.Builder keyReward(Reward reward) {
         this.rewards.add(reward);
         return this;
      }

      public TieredGateway.Builder keyRewards(List<Reward> rewards) {
         this.rewards.addAll(rewards);
         return this;
      }

      public TieredGateway.Builder failure(Failure failure) {
         this.failures.add(failure);
         return this;
      }

      public TieredGateway.Builder failures(List<Failure> failures) {
         this.failures.addAll(failures);
         return this;
      }

      public TieredGateway.Builder rules(GateRules rules) {
         this.rules = rules;
         return this;
      }

      public TieredGateway.Builder rules(UnaryOperator<dev.shadowsoffire.gateways.gate.GateRules.Builder> config) {
         return this.rules(config.apply(GateRules.builder()).build());
      }

      public TieredGateway.Builder bossSettings(BossEventSettings bossSettings) {
         this.bossSettings = bossSettings;
         return this;
      }

      public TieredGateway build() {
         Preconditions.checkNotNull(this.settings, "Settings must be set before building a TieredGateway");
         if (this.waves.isEmpty()) {
            throw new IllegalStateException("Gateway must have at least one wave");
         } else {
            return new TieredGateway(
               this.settings,
               Collections.unmodifiableList(new ArrayList<>(this.waves)),
               Collections.unmodifiableList(new ArrayList<>(this.rewards)),
               Collections.unmodifiableList(new ArrayList<>(this.failures)),
               this.rules,
               this.bossSettings
            );
         }
      }
   }
}
