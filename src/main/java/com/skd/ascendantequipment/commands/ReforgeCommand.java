package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ReforgeCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = RarityCommand.SUGGEST_RARITY;

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      root.then(
         ((LiteralArgumentBuilder)Commands.literal("reforge").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(Commands.argument("rarity", IdentifierArgument.id()).suggests(SUGGEST_RARITY).executes(c -> {
               Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
               GenContext ctx = GenContext.forPlayer(p);
               LootRarity rarity = (LootRarity)RarityRegistry.INSTANCE.getValue(IdentifierArgument.getId(c, "rarity"));
               ItemStack stack = p.getMainHandItem();
               AffixHelper.setAffixes(stack, ItemAffixes.EMPTY);
               LootController.createLootItem(stack, rarity, ctx);
               return 0;
            }))
      );
   }
}
