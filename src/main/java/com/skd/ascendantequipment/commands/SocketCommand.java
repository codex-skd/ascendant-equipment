package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.SocketHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SocketCommand {
   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      root.then(
         ((LiteralArgumentBuilder)Commands.literal("set_sockets").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(Commands.argument("sockets", IntegerArgumentType.integer(0, 16)).executes(c -> {
               Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
               ItemStack stack = p.getMainHandItem();
               LootCategory cat = LootCategory.forItem(stack);
               if (cat.isNone()) {
                  ((CommandSourceStack)c.getSource()).sendFailure(Component.literal("The target item cannot receive sockets!"));
                  return 1;
               } else {
                  int sockets = IntegerArgumentType.getInteger(c, "sockets");
                  SocketHelper.setSockets(stack, sockets);
                  return 0;
               }
            }))
      );
   }
}
