package com.skd.ascendantequipment.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.ascendantattributes.api.AttributeHelper;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AffixCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX = (ctx, builder) -> SharedSuggestionProvider.suggest(
      AffixRegistry.INSTANCE.getKeys().stream().map(Identifier::toString), builder
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_APPLICABLE_AFFIX = (ctx, builder) -> {
      if (((CommandSourceStack)ctx.getSource()).getEntity() instanceof LivingEntity living) {
         ItemStack held = living.getMainHandItem();
         if (!held.isEmpty()) {
            LootCategory cat = LootCategory.forItem(held);
            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
            if (!cat.isNone() && rarity.isBound()) {
               Stream<String> suggestions = AffixRegistry.INSTANCE
                  .getValues()
                  .stream()
                  .filter(a -> a.canApplyTo(held, cat, (LootRarity)rarity.get()))
                  .map(AffixRegistry.INSTANCE::getKey)
                  .map(Identifier::toString);
               return SharedSuggestionProvider.suggest(suggestions, builder);
            }
         }
      }

      return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
   };
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX_ON_ITEM = (ctx, builder) -> {
      if (((CommandSourceStack)ctx.getSource()).getEntity() instanceof LivingEntity living) {
         ItemStack held = living.getMainHandItem();
         if (!held.isEmpty()) {
            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
            return SharedSuggestionProvider.suggest(affixes.keySet().stream().map(DynamicHolder::getId).map(Identifier::toString), builder);
         }
      }

      return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
   };

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
      LiteralArgumentBuilder<CommandSourceStack> builder = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("affix")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
      builder.then(
         Commands.literal("apply")
            .then(
               ((RequiredArgumentBuilder)Commands.argument("affix", IdentifierArgument.id())
                     .suggests(SUGGEST_APPLICABLE_AFFIX)
                     .then(
                        Commands.argument("level", FloatArgumentType.floatArg(0.0F, 2.0F))
                           .executes(c -> applyAffix(c, IdentifierArgument.getId(c, "affix"), FloatArgumentType.getFloat(c, "level")))
                     ))
                  .executes(c -> applyAffix(c, IdentifierArgument.getId(c, "affix"), ((CommandSourceStack)c.getSource()).getLevel().getRandom().nextFloat()))
            )
      );
      builder.then(Commands.literal("list").executes(AffixCommand::listAffixes));
      builder.then(
         Commands.literal("list_alternatives")
            .then(
               Commands.argument("affix", IdentifierArgument.id())
                  .suggests(SUGGEST_AFFIX_ON_ITEM)
                  .executes(c -> listAlternatives(c, IdentifierArgument.getId(c, "affix")))
            )
      );
      root.then(builder);
   }

   public static int applyAffix(CommandContext<CommandSourceStack> c, Identifier affixId, float level) {
      DynamicHolder<Affix> afx = AffixRegistry.INSTANCE.holder(affixId);
      if (!afx.isBound()) {
         return fail(c, "Unknown affix: " + affixId, -1);
      }

      if (((CommandSourceStack)c.getSource()).getEntity() instanceof LivingEntity living) {
         ItemStack held = living.getMainHandItem();
         if (held.isEmpty()) {
            return fail(c, "The target entity must have an item in their main hand.", -2);
         }

         DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
         if (!rarity.isBound()) {
            return fail(c, "The target item must have a set rarity.", -3);
         }

         LootCategory cat = LootCategory.forItem(held);
         if (cat.isNone()) {
            return fail(c, "The target item must have a valid loot category", -4);
         }

         if (!((Affix)afx.get()).canApplyTo(held, cat, (LootRarity)rarity.get())) {
            return fail(c, "The selected affix cannot be applied to the target item.", -5);
         }

         AffixHelper.applyAffix(held, new AffixInstance(afx, level, rarity, held));
         ((CommandSourceStack)c.getSource())
            .sendSuccess(
               () -> Component.translatable("Successfully applied affix %s with level %s to %s", new Object[]{affixId.toString(), level, held.getDisplayName()}),
               true
            );
         return 0;
      } else {
         return fail(c, "/apoth affix must be executed by a living entity.", -10);
      }
   }

   public static int listAffixes(CommandContext<CommandSourceStack> c) {
      if (((CommandSourceStack)c.getSource()).getEntity() instanceof LivingEntity living) {
         ItemStack held = living.getMainHandItem();
         if (held.isEmpty()) {
            return fail(c, "The target entity must have an item in their main hand.", -2);
         }

         DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
         if (!rarity.isBound()) {
            return fail(c, "The target item must have a set rarity.", -3);
         }

         Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
         AttributeTooltipContext ctx = AttributeTooltipContext.of(
            living instanceof Player p ? p : null,
            TooltipContext.of(((CommandSourceStack)c.getSource()).getLevel()),
            TooltipDisplay.DEFAULT,
            AscendantAttributes.getTooltipFlag()
         );
         ((CommandSourceStack)c.getSource()).sendSystemMessage(Component.translatable("Affixes present on %s:", new Object[]{held.getDisplayName()}));
         affixes.forEach((afx, inst) -> {
            MutableComponent name = Component.translatable("[%s]", new Object[]{inst.getName(true)});
            name.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new ShowText(inst.getAugmentingText(ctx))));
            ((CommandSourceStack)c.getSource()).sendSystemMessage(Component.translatable("%s - %s%%", new Object[]{name, Affix.fmt(100.0F * inst.level())}));
         });
         return 0;
      } else {
         return fail(c, "/apoth affix must be executed by a living entity.", -10);
      }
   }

   public static int listAlternatives(CommandContext<CommandSourceStack> c, Identifier affixId) throws CommandSyntaxException {
      DynamicHolder<Affix> afx = AffixRegistry.INSTANCE.holder(affixId);
      if (!afx.isBound()) {
         return fail(c, "Unknown affix: " + affixId, -1);
      }

      if (((CommandSourceStack)c.getSource()).getEntity() instanceof LivingEntity living) {
         ItemStack held = living.getMainHandItem();
         if (held.isEmpty()) {
            return fail(c, "The target entity must have an item in their main hand.", -2);
         }

         DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
         if (!rarity.isBound()) {
            return fail(c, "The target item must have a set rarity.", -3);
         }

         Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
         if (!affixes.containsKey(afx)) {
            return fail(c, "The target item does not contain the selected affix.", -4);
         }

         Stream<DynamicHolder<Affix>> alternatives = LootController.getAlternativeAffixes(
            ((CommandSourceStack)c.getSource()).getPlayerOrException(), held, (LootRarity)rarity.get(), afx
         );
         ((CommandSourceStack)c.getSource())
            .sendSystemMessage(Component.translatable("Possible alternatives to %s:", new Object[]{((Affix)afx.get()).getName(true)}));
         AttributeTooltipContext ctx = AttributeTooltipContext.of(
            living instanceof Player p ? p : null,
            TooltipContext.of(((CommandSourceStack)c.getSource()).getLevel()),
            TooltipDisplay.DEFAULT,
            AscendantAttributes.getTooltipFlag()
         );
         alternatives.forEach(a -> {
            MutableComponent name = Component.translatable("[%s]", new Object[]{((Affix)a.get()).getName(true)});
            AffixInstance inst = new AffixInstance((DynamicHolder<Affix>)a, 0.5F, rarity, held);
            Component augTxt = inst.getAugmentingText(ctx);
            name.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new ShowText(augTxt)));
            ((CommandSourceStack)c.getSource()).sendSystemMessage(AttributeHelper.list().append(name));
         });
         return 0;
      } else {
         return fail(c, "/apoth affix must be executed by a living entity.", -10);
      }
   }

   public static int fail(CommandContext<CommandSourceStack> c, String msg, int code) {
      ((CommandSourceStack)c.getSource()).sendFailure(Component.translatable(msg));
      return code;
   }
}
