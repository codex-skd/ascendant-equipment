package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.skd.ascendantattributes.modifiers.EntitySlotGroup;
import com.skd.ascendantequipment.loot.LootCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CategoryCheckCommand {
   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      root.then(((LiteralArgumentBuilder)Commands.literal("loot_category").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(c -> {
         Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
         ItemStack stack = p.getMainHandItem();
         LootCategory cat = LootCategory.forItem(stack);
         EntitySlotGroup slots = cat == null ? null : cat.getSlots();
         p.sendSystemMessage(Component.literal("Loot Category - " + (cat == null ? "null" : cat.getKey())));
         p.sendSystemMessage(Component.literal("Equipment Slot - " + (slots == null ? "null" : slots.id())));
         return 0;
      }));
   }
}
