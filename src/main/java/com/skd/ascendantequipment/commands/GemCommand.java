package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.GenContext;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GemCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> SharedSuggestionProvider.suggest(
      Arrays.stream(Operation.values()).map(Enum::name), builder
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) -> SharedSuggestionProvider.suggest(
      BuiltInRegistries.ATTRIBUTE.keySet().stream().map(Identifier::toString), builder
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_GEM = (ctx, builder) -> SharedSuggestionProvider.suggest(
      GemRegistry.INSTANCE.getKeys().stream().map(Identifier::toString), builder
   );

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      root.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gem").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(Commands.literal("fromPreset").then(Commands.argument("gem", IdentifierArgument.id()).suggests(SUGGEST_GEM).executes(c -> {
                  Gem gem = (Gem)GemRegistry.INSTANCE.getValue(IdentifierArgument.getId(c, "gem"));
                  Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
                  GenContext ctx = GenContext.forPlayer(p);
                  ItemStack stack = gem.toStack(Purity.random(ctx));
                  p.addItem(stack);
                  return 0;
               }))))
            .then(Commands.literal("random").executes(c -> {
               Player p = ((CommandSourceStack)c.getSource()).getPlayerOrException();
               GenContext ctx = GenContext.forPlayer(p);
               ItemStack gem = GemRegistry.createRandomGemStack(ctx);
               p.addItem(gem);
               return 0;
            }))
      );
   }
}
