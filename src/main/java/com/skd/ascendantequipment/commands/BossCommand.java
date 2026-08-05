package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mobs.AscEqMobEvents;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class BossCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS = (ctx, builder) -> SharedSuggestionProvider.suggest(
      InvaderRegistry.INSTANCE.getKeys().stream().map(Identifier::toString), builder
   );

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("spawn_boss")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
      builder.then(
         ((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3())
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("boss", IdentifierArgument.id())
                        .suggests(SUGGEST_BOSS)
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("rarity", IdentifierArgument.id())
                                 .suggests(RarityCommand.SUGGEST_RARITY)
                                 .then(
                                    Commands.argument("send_notification", BoolArgumentType.bool())
                                       .executes(
                                          c -> spawnBoss(
                                             c,
                                             Vec3Argument.getVec3(c, "pos"),
                                             IdentifierArgument.getId(c, "boss"),
                                             IdentifierArgument.getId(c, "rarity"),
                                             BoolArgumentType.getBool(c, "send_notification")
                                          )
                                       )
                                 ))
                              .executes(
                                 c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), IdentifierArgument.getId(c, "boss"), IdentifierArgument.getId(c, "rarity"))
                              )
                        ))
                     .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), IdentifierArgument.getId(c, "boss"), null))
               ))
            .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), null, null))
      );
      builder.then(
         ((RequiredArgumentBuilder)Commands.argument("entity", EntityArgument.entity())
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("boss", IdentifierArgument.id())
                        .suggests(SUGGEST_BOSS)
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("rarity", IdentifierArgument.id())
                                 .suggests(RarityCommand.SUGGEST_RARITY)
                                 .then(
                                    Commands.argument("send_notification", BoolArgumentType.bool())
                                       .executes(
                                          c -> spawnBoss(
                                             c,
                                             Vec3Argument.getVec3(c, "pos"),
                                             IdentifierArgument.getId(c, "boss"),
                                             IdentifierArgument.getId(c, "rarity"),
                                             BoolArgumentType.getBool(c, "send_notification")
                                          )
                                       )
                                 ))
                              .executes(
                                 c -> spawnBoss(
                                    c,
                                    EntityArgument.getEntity(c, "entity").position(),
                                    IdentifierArgument.getId(c, "boss"),
                                    IdentifierArgument.getId(c, "rarity")
                                 )
                              )
                        ))
                     .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), IdentifierArgument.getId(c, "boss"), null))
               ))
            .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), null, null))
      );
      root.then(builder);
   }

   public static int spawnBoss(CommandContext<CommandSourceStack> c, Vec3 pos, @Nullable Identifier bossId, @Nullable Identifier rarityId) {
      return spawnBoss(c, pos, bossId, rarityId, false);
   }

   public static int spawnBoss(
      CommandContext<CommandSourceStack> c, Vec3 pos, @Nullable Identifier bossId, @Nullable Identifier rarityId, boolean sendNotification
   ) {
      Player summoner = ((CommandSourceStack)c.getSource()).getEntity() instanceof Player p
         ? p
         : ((CommandSourceStack)c.getSource()).getLevel().getNearestPlayer(pos.x(), pos.y(), pos.z(), 64.0, false);
      if (summoner == null) {
         ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("No available player context!"));
         return -1;
      }

      GenContext ctx = GenContext.forPlayer(summoner);
      Invader boss = bossId == null ? InvaderRegistry.INSTANCE.getRandomItem(ctx) : (Invader)InvaderRegistry.INSTANCE.getValue(bossId);
      if (boss == null) {
         if (bossId != null) {
            ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("Unknown boss: " + bossId));
         } else {
            ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("No bosses available for the current context!"));
         }

         return -2;
      } else {
         Mob bossEntity;
         if (rarityId != null) {
            DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(rarityId);
            if (!rarity.isBound()) {
               ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("Unknown rarity: " + rarityId));
               return -3;
            }

            bossEntity = boss.createBoss((ServerLevelAccessor)summoner.level(), BlockPos.containing(pos), ctx, (LootRarity)rarity.get());
         } else {
            bossEntity = boss.createBoss((ServerLevelAccessor)summoner.level(), BlockPos.containing(pos), ctx);
         }

         ((CommandSourceStack)c.getSource()).getLevel().addFreshEntityWithPassengers(bossEntity);
         if (sendNotification) {
            AscEqMobEvents.sendInvaderSpawnNotification(((CommandSourceStack)c.getSource()).getLevel(), bossEntity);
         }

         return 0;
      }
   }
}
