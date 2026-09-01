package com.skd.ascendantequipment.commands;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.affix.AffixType;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mobs.registries.EliteRegistry;
import com.skd.ascendantequipment.mobs.registries.InvaderRegistry;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;

public class DebugWeightCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX_TYPE = (ctx, builder) -> SharedSuggestionProvider.suggest(
      Arrays.stream(AffixType.values()).map(StringRepresentable::getSerializedName), builder
   );
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_CATEGORY = (ctx, builder) -> SharedSuggestionProvider.suggest(
      AscEq.BuiltInRegs.LOOT_CATEGORY.keySet().stream().map(ResourceLocation::toString), builder
   );
   private static final DynamicCommandExceptionType UNKNOWN_RARITY = new DynamicCommandExceptionType(str -> () -> "Unknown Rarity: " + str);
   private static final DynamicCommandExceptionType UNKNOWN_AFFIX_TYPE = new DynamicCommandExceptionType(str -> () -> "Unknown Affix Type: " + str);

   public static void register(LiteralArgumentBuilder<CommandSourceStack> root, CommandBuildContext ctx) {
      LiteralArgumentBuilder<CommandSourceStack> weights = Commands.literal("weights");
      weights.then(Commands.literal("affix_loot_entries").executes(c -> dumpWeights(c, AffixLootRegistry.INSTANCE)));
      weights.then(
         Commands.literal("affixes")
            .then(
               Commands.argument("item", ItemArgument.item(ctx))
                  .then(
                     Commands.argument("type", StringArgumentType.word())
                        .suggests(SUGGEST_AFFIX_TYPE)
                        .then(
                           Commands.argument("rarity", ResourceLocationArgument.id())
                              .suggests(RarityCommand.SUGGEST_RARITY)
                              .executes(
                                 c -> dumpAffixWeights(
                                    c, ItemArgument.getItem(c, "item"), StringArgumentType.getString(c, "type"), ResourceLocationArgument.getId(c, "rarity")
                                 )
                              )
                        )
                  )
            )
      );
      weights.then(Commands.literal("elites").executes(c -> dumpWeights(c, EliteRegistry.INSTANCE)));
      weights.then(Commands.literal("gems").executes(c -> dumpWeights(c, GemRegistry.INSTANCE)));
      weights.then(Commands.literal("invaders").executes(c -> dumpWeights(c, InvaderRegistry.INSTANCE)));
      weights.then(Commands.literal("rarities").executes(c -> dumpWeights(c, RarityRegistry.INSTANCE)));
      root.then(weights);
   }

   public static <T extends TieredWeights.Weighted> void dumpWeightsFor(GenContext ctx, DynamicRegistry<T> registry) {
      dumpWeightsFor(ctx, registry, Predicates.alwaysTrue());
   }

   public static <T extends TieredWeights.Weighted> void dumpWeightsFor(GenContext ctx, DynamicRegistry<T> registry, Predicate<T> filter) {
      Collection<T> values = registry.getValues();
      List<DebugWeightCommand.ItemAndWeight<T>> list = new ArrayList<>(values.size());
      values.stream().filter(filter).map(t -> wrapWithConstraints(ctx, (T)t)).forEach(list::add);
      float total = list.stream().mapToInt(DebugWeightCommand.ItemAndWeight::weight).sum();
      AscendantEquipment.LOGGER.info("Starting dump of all {} weights...", registry.getId());
      AscendantEquipment.LOGGER.info("Current GenContext: {}", ctx);
      Comparator<DebugWeightCommand.ItemAndWeight<T>> comparator = Comparator.comparing(w -> -w.weight());
      comparator = comparator.thenComparing(Comparator.comparing(w -> registry.getKey(w.item()).toString()));
      list.sort(comparator);

      for (DebugWeightCommand.ItemAndWeight<T> entry : list) {
         ResourceLocation key = registry.getKey(entry.item());
         float chance = entry.weight() / total;
         AscendantEquipment.LOGGER.info("{} : {}% ({} / {}}", new Object[]{key, Affix.fmt(chance * 100.0F), entry.weight(), (int)total});
      }
   }

   public static <T extends TieredWeights.Weighted> int dumpWeights(CommandContext<CommandSourceStack> c, DynamicRegistry<T> registry) throws CommandSyntaxException {
      GenContext ctx = GenContext.forPlayer(((CommandSourceStack)c.getSource()).getPlayerOrException());
      dumpWeightsFor(ctx, registry);
      ((CommandSourceStack)c.getSource()).sendSuccess(() -> Component.literal("Weight values have been dumped to the log file."), true);
      return 0;
   }

   private static <T extends TieredWeights.Weighted> DebugWeightCommand.ItemAndWeight<T> wrapWithConstraints(GenContext ctx, T t) {
      return t instanceof Constraints.Constrained c && !c.constraints().test(ctx)
         ? new DebugWeightCommand.ItemAndWeight<>(t, 0)
         : new DebugWeightCommand.ItemAndWeight<>(t, t.weights().getWeight(ctx));
   }

   private static int dumpAffixWeights(CommandContext<CommandSourceStack> c, ItemInput item, String typeStr, ResourceLocation rarityId) throws CommandSyntaxException {
      LootRarity rarity = (LootRarity)RarityRegistry.INSTANCE.getValue(rarityId);
      if (rarity == null) {
         throw UNKNOWN_RARITY.create(rarityId);
      }

      AffixType type = (AffixType)((Pair)AffixType.CODEC
            .decode(JsonOps.INSTANCE, new JsonPrimitive(typeStr))
            .getOrThrow(s -> UNKNOWN_AFFIX_TYPE.create(typeStr)))
         .getFirst();
      ItemStack stack = item.createItemStack(1, false);
      LootCategory cat = LootCategory.forItem(stack);
      AscendantEquipment.LOGGER.info("Affix weight dump target item: " + stack.toString());
      GenContext ctx = GenContext.forPlayer(((CommandSourceStack)c.getSource()).getPlayerOrException());
      dumpWeightsFor(ctx, AffixRegistry.INSTANCE, afx -> afx.canApplyTo(stack, cat, rarity) && afx.definition().type() == type);
      ((CommandSourceStack)c.getSource()).sendSuccess(() -> Component.literal("Weight values have been dumped to the log file."), true);
      return 0;
   }

   private record ItemAndWeight<T>(T item, int weight) {
   }
}
