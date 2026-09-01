package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RarityCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> SharedSuggestionProvider.suggest(
      RarityRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder
   );

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      root.then(
         ((LiteralArgumentBuilder)Commands.literal("set_rarity").requires(s -> s.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(SUGGEST_RARITY).executes(c -> {
               Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
               LootRarity rarity = (LootRarity)RarityRegistry.INSTANCE.getValue(ResourceLocationArgument.getId(c, "rarity"));
               ItemStack stack = p.getMainHandItem();
               AffixHelper.setRarity(stack, rarity);
               return 0;
            }))
      );
   }
}
