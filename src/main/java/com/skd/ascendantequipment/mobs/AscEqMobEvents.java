package com.skd.ascendantequipment.mobs;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mobs.registries.AugmentRegistry;
import com.skd.ascendantequipment.mobs.registries.EliteRegistry;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.types.Augmentation;
import com.skd.ascendantequipment.mobs.types.Elite;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.mobs.util.SurfaceType;
import com.skd.ascendantequipment.net.BossSpawnPayload;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.tiers.augments.TierAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AscEqMobEvents {
   public static final String APOTH_MINIBOSS = "apoth.miniboss";
   public static final String APOTH_MINIBOSS_PLAYER = "apoth.miniboss.player";
   private static final Marker MARKER = MarkerFactory.getMarker(AscEqMobEvents.class.getSimpleName());

   @SubscribeEvent(priority = EventPriority.LOW)
   public void finalizeMobSpawns(FinalizeSpawnEvent e) {
      debugLog("Finalizing spawn for: {}", e.getEntity().getName().getString());
      if (!e.isCanceled() && !e.isSpawnCancelled()) {
         Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1.0, false);
         if (player == null) {
            debugLog("Discarding due to lack of player context.");
         } else {
            Mob mob = e.getEntity();
            RandomSource rand = e.getLevel().getRandom();
            GenContext ctx = GenContext.forPlayerAtPos(rand, player, mob.blockPosition());
            if (this.trySpawnInvader(e, mob, ctx, player)) {
               debugLog("Successfully spawned an invader. Skipping Augmentations and Elites.");
            } else {
               this.tryAugmentations(e.getLevel(), mob, e.getSpawnType(), ctx);
               if (!this.trySpawnElite(e, mob, ctx, player)) {
                  ;
               }
            }
         }
      } else {
         debugLog("Discarding due to cancellation.");
      }
   }

   private boolean trySpawnInvader(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
      if ((e.getSpawnType() == EntitySpawnReason.NATURAL || e.getSpawnType() == EntitySpawnReason.CHUNK_GENERATION) && mob instanceof Monster) {
         ServerLevelAccessor sLevel = e.getLevel();
         long gameTime = sLevel.getGameTime();
         if (player.getData(AscEq.Attachments.INVADER_COOLDOWN) > gameTime) {
            debugLog("[Invaders]: Spawn cooldown is active for the context player {}.", player.getName().getString());
            return false;
         }

         ResourceKey<DimensionType> dimId = sLevel.getLevel().dimensionTypeRegistration().getKey();
         InvaderSpawnRules rules = sLevel.registryAccess()
            .lookupOrThrow(Registries.DIMENSION_TYPE)
            .getData(AscEq.DataMaps.INVADER_SPAWN_RULES, dimId);
         if (rules == null) {
            debugLog("[Invaders]: No invader spawn rules present for dimension {}", dimId);
            return false;
         }

         float chance = rules.spawnChances().get(ctx.tier());
         SurfaceType surface = rules.surfaceType();
         if (ctx.rand().nextFloat() > chance) {
            debugLog("[Invaders]: Failed random chance roll.");
            return false;
         }

         if (surface.test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {
            debugLog("[Invaders]: Succeeded at random chance roll and surface test.");
            Invader item = InvaderRegistry.INSTANCE.getRandomItem(ctx);
            if (item == null) {
               AscendantEquipment.LOGGER
                  .error("Attempted to spawn an Invader in dimension {} using configured spawn rules {} but no bosses were made available.", dimId, rules);
               return false;
            }

            if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
               debugLog("[Invaders]: Failed invader spawn conditions.");
               return false;
            }

            Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), ctx);
            if (EquipmentConfig.bossAutoAggro && !player.isCreative()) {
               boss.setTarget(player);
            }

            if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
               sLevel.addFreshEntityWithPassengers(boss);
               e.setCanceled(true);
               e.setSpawnCancelled(true);
               sendInvaderSpawnNotification(sLevel.getLevel(), boss);
               long end = gameTime + rules.cooldown().orElse(EquipmentConfig.bossSpawnCooldown).intValue();
               applyClusteredCooldown(sLevel.getLevel(), player, ctx.tier(), boss, end);
               debugLog("[Invaders]: Successfully spawned an invader {} at {}", boss.getName().getString(), boss.blockPosition());
               return true;
            }

            debugLog("Failed entity spawn checks.");
         } else {
            debugLog("[Invaders]: Failed surface test " + surface);
         }

         return false;
      } else {
         debugLog("[Invaders]: Failed invader preconditions.");
         return false;
      }
   }

   public static void sendInvaderSpawnNotification(ServerLevel sLevel, Mob invader) {
      Component name = getName(invader);
      DynamicHolder<LootRarity> rarity = getRarity(invader);
      if (name != null && rarity.isBound()) {
         sLevel.players()
            .forEach(
               p -> {
                  if (isWithinAnnounceRange(p, invader)) {
                     p.connection
                        .send(
                           new ClientboundSetActionBarTextPacket(
                              Component.translatable("info.ascendant_equipment.boss_spawn", new Object[]{name, (int)invader.getX(), (int)invader.getY()})
                           )
                        );
                     PacketDistributor.sendToPlayer(p, new BossSpawnPayload(invader.blockPosition(), rarity), new CustomPacketPayload[0]);
                  }
               }
            );
      } else {
         AscendantEquipment.LOGGER
            .warn(
               "An Invader {} ({}) has spawned without a name ({}) or rarity ({})!",
               new Object[]{invader.getName().getString(), EntityType.getKey(invader.getType()), name, rarity}
            );
      }
   }

   private static void applyClusteredCooldown(ServerLevel level, Player trigger, WorldTier tier, Mob boss, long end) {
      applyCooldown(trigger, end);
      int clustered = 0;

      for (ServerPlayer p : level.players()) {
         if (p != trigger && WorldTier.getTier(p) == tier && isWithinAnnounceRange(p, boss)) {
            applyCooldown(p, end);
            clustered++;
         }
      }

      debugLog("[Invaders]: Applied spawn cooldown ending at {} to {} and {} clustered player(s).", end, trigger.getName().getString(), clustered);
   }

   private static void applyCooldown(Player player, long end) {
      if (end > player.getData(AscEq.Attachments.INVADER_COOLDOWN)) {
         player.setData(AscEq.Attachments.INVADER_COOLDOWN, end);
      }
   }

   private static boolean isWithinAnnounceRange(Player player, Entity target) {
      Vec3 tPos = new Vec3(target.getX(), player.getY(), target.getZ());
      return player.distanceToSqr(tPos) <= EquipmentConfig.bossAnnounceRange * EquipmentConfig.bossAnnounceRange;
   }

   private void tryAugmentations(ServerLevelAccessor level, Mob mob, EntitySpawnReason type, GenContext ctx) {
      float healthPct = mob.getHealth() / mob.getMaxHealth();

      for (TierAugment aug : TierAugmentRegistry.getAugments(ctx.tier(), TierAugment.Target.MONSTERS)) {
         aug.apply(level, mob);
      }

      mob.setData(AscEq.Attachments.TIER_AUGMENTS_APPLIED, true);

      for (Augmentation aug : AugmentRegistry.getAll()) {
         if (aug.canApply(level, mob, type, ctx)) {
            if (ctx.rand().nextFloat() <= aug.chance()) {
               debugLog("Applying augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
               aug.apply(mob, ctx);
            } else {
               debugLog("Roll failed for augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
            }
         } else {
            debugLog("Skipped augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
         }
      }

      mob.setHealth(healthPct * mob.getMaxHealth());
   }

   private boolean trySpawnElite(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
      ServerLevelAccessor sLevel = e.getLevel();
      Elite item = EliteRegistry.INSTANCE.getRandomItem(ctx, mob);
      if (item == null) {
         debugLog("No Elites were available for {} and {}", ctx, mob);
         return false;
      }

      if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
         debugLog("The elite {} was selected but could not spawn based on spawn conditions.", EliteRegistry.INSTANCE.getKey(item));
         return false;
      }

      if (ctx.rand().nextFloat() <= item.getChance()) {
         mob.getPersistentData().putString("apoth.miniboss", EliteRegistry.INSTANCE.getKey(item).toString());
         mob.getPersistentData().putString("apoth.miniboss.player", player.getUUID().toString());
         if (!item.basicData().finalizeSpawn()) {
            e.setCanceled(true);
         }

         debugLog("Successfully spawned the elite {} at {}", EliteRegistry.INSTANCE.getKey(item), mob.blockPosition());
         return true;
      } else {
         return false;
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void delayedEliteMobs(EntityJoinLevelEvent e) {
      if (!e.getLevel().isClientSide() && e.getEntity() instanceof Mob mob) {
         CompoundTag data = mob.getPersistentData();
         if (data.contains("apoth.miniboss") && data.contains("apoth.miniboss.player")) {
            String key = data.getString("apoth.miniboss").orElse("");

            try {
               UUID playerId = UUID.fromString(data.getString("apoth.miniboss.player").orElseThrow());
               Player player = e.getLevel().getPlayerByUUID(playerId);
               if (player == null) {
                  player = e.getLevel().getNearestPlayer(mob, -1.0);
               }

               if (player != null) {
                  GenContext ctx = GenContext.forPlayerAtPos(e.getLevel().getRandom(), player, mob.blockPosition());
                  Elite item = EliteRegistry.INSTANCE.getValue(Identifier.tryParse(key));
                  if (item != null) {
                     item.transformMiniboss((ServerLevel)e.getLevel(), mob, ctx);
                  }
               }
            } catch (Exception ex) {
               AscendantEquipment.LOGGER.error("Failure while initializing the Apothic Elite " + key, ex);
            }
         }
      }
   }

   private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
      return playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance()
            && entity.removeWhenFarAway(playerDist)
         ? false
         : entity.checkSpawnRules(world, EntitySpawnReason.NATURAL) && entity.checkSpawnObstruction(world);
   }

   @Nullable
   private static Component getName(Mob boss) {
      return boss.getSelfAndPassengers()
         .filter(e -> e.getPersistentData().contains("apoth.boss"))
         .findFirst()
         .<Component>map(Entity::getCustomName)
         .orElse(null);
   }

   @Nullable
   private static DynamicHolder<LootRarity> getRarity(Mob boss) {
      return boss.getSelfAndPassengers()
         .filter(e -> e.getPersistentData().contains("apoth.boss"))
         .findFirst()
         .map(AscEqMobEvents::getRarityHolder)
         .orElse(RarityRegistry.INSTANCE.emptyHolder());
   }

   private static DynamicHolder<LootRarity> getRarityHolder(Entity entity) {
      Identifier id = Identifier.tryParse(entity.getPersistentData().getString("apoth.boss.rarity").orElse(""));
      return RarityRegistry.INSTANCE.holder(id);
   }

   private static void debugLog(String msg, Object... args) {
      if (AscendantEquipment.DEBUG_MOBS) {
         AscendantEquipment.LOGGER.debug(MARKER, msg, args);
      }
   }
}
