package com.skd.ascendantequipment.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.affix.AffixType;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.codec.CodecMap;
import com.skd.commontoolkit.codec.CodecProvider;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;

public interface LootRule extends CodecProvider<LootRule> {
   CodecMap<LootRule> CODEC = new CodecMap("Loot Rule");

   void execute(ItemStack var1, LootRarity var2, GenContext var3);

   static void initCodecs() {
      register("component", LootRule.ComponentLootRule.CODEC);
      register("affix", LootRule.AffixLootRule.CODEC);
      register("socket", LootRule.SocketLootRule.CODEC);
      register("durability", LootRule.DurabilityLootRule.CODEC);
      register("chanced", LootRule.ChancedLootRule.CODEC);
      register("combined", LootRule.CombinedLootRule.CODEC);
      register("select", LootRule.SelectLootRule.CODEC);
   }

   private static void register(String id, Codec<? extends LootRule> codec) {
      CODEC.register(AscendantEquipment.loc(id), codec);
   }

   record AffixLootRule(AffixType type) implements LootRule {
      public static Codec<LootRule.AffixLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(AffixType.CODEC.fieldOf("affix_type").forGetter(LootRule.AffixLootRule::type)).apply(inst, LootRule.AffixLootRule::new)
      );

      public Codec<LootRule.AffixLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         List<Weighted<Affix>> available = LootController.getWeightedAffixes(stack, rarity, this.type, ctx);
         int weight = WeightedRandom.getTotalWeight(available, Weighted::weight);
         if (available.size() != 0 && weight != 0) {
            Affix selected = WeightedRandom.getRandomItem(ctx.rand(), available, weight, Weighted::weight).get().value();
            ItemAffixes.Builder builder = ((ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY)).toBuilder();
            builder.upgrade(AffixRegistry.INSTANCE.holder(selected), ctx.rand().nextFloat());
            AffixHelper.setAffixes(stack, builder.build());
         } else {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            AscendantEquipment.LOGGER
               .error(
                  "Failed to execute AffixLootRule (no affixes available) {}/{}/{}/{}!",
                  new Object[]{id, RarityRegistry.INSTANCE.getKey(rarity), this.type, LootCategory.forItem(stack)}
               );
         }
      }
   }

   record ChancedLootRule(float chance, LootRule rule) implements LootRule {
      public static Codec<LootRule.ChancedLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.FLOAT.fieldOf("chance").forGetter(LootRule.ChancedLootRule::chance),
               LootRule.CODEC.fieldOf("rule").forGetter(LootRule.ChancedLootRule::rule)
            )
            .apply(inst, LootRule.ChancedLootRule::new)
      );

      public Codec<LootRule.ChancedLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         if (ctx.rand().nextFloat() <= this.chance) {
            this.rule.execute(stack, rarity, ctx);
         }
      }
   }

   record CombinedLootRule(List<LootRule> rules) implements LootRule {
      public static Codec<LootRule.CombinedLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(LootRule.CODEC.listOf().fieldOf("rules").forGetter(LootRule.CombinedLootRule::rules)).apply(inst, LootRule.CombinedLootRule::new)
      );

      public Codec<LootRule.CombinedLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         for (LootRule rule : this.rules) {
            rule.execute(stack, rarity, ctx);
         }
      }
   }

   record ComponentLootRule(DataComponentPatch components) implements LootRule {
      public static Codec<LootRule.ComponentLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(DataComponentPatch.CODEC.fieldOf("components").forGetter(LootRule.ComponentLootRule::components))
            .apply(inst, LootRule.ComponentLootRule::new)
      );

      public Codec<LootRule.ComponentLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         stack.applyComponents(this.components);
      }
   }

   record DurabilityLootRule(float min, float max) implements LootRule {
      public static Codec<LootRule.DurabilityLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.floatRange(0.0F, 1.0F).fieldOf("min").forGetter(LootRule.DurabilityLootRule::min),
               Codec.floatRange(0.0F, 1.0F).fieldOf("max").forGetter(LootRule.DurabilityLootRule::max)
            )
            .apply(inst, LootRule.DurabilityLootRule::new)
      );

      public Codec<LootRule.DurabilityLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         stack.set(AscEq.Components.DURABILITY_BONUS, Mth.lerp(ctx.rand().nextFloat(), this.min, this.max));
      }
   }

   record SelectLootRule(float chance, LootRule ifTrue, LootRule ifFalse) implements LootRule {
      public static Codec<LootRule.SelectLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.FLOAT.fieldOf("chance").forGetter(LootRule.SelectLootRule::chance),
               LootRule.CODEC.fieldOf("if_true").forGetter(LootRule.SelectLootRule::ifTrue),
               LootRule.CODEC.fieldOf("if_false").forGetter(LootRule.SelectLootRule::ifFalse)
            )
            .apply(inst, LootRule.SelectLootRule::new)
      );

      public Codec<LootRule.SelectLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         if (ctx.rand().nextFloat() <= this.chance) {
            this.ifTrue.execute(stack, rarity, ctx);
         } else {
            this.ifFalse.execute(stack, rarity, ctx);
         }
      }
   }

   record SocketLootRule(int min, int max) implements LootRule {
      public static Codec<LootRule.SocketLootRule> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               Codec.intRange(0, 16).fieldOf("min").forGetter(LootRule.SocketLootRule::min),
               Codec.intRange(1, 16).fieldOf("max").forGetter(LootRule.SocketLootRule::max)
            )
            .apply(inst, LootRule.SocketLootRule::new)
      );

      public Codec<LootRule.SocketLootRule> getCodec() {
         return CODEC;
      }

      @Override
      public void execute(ItemStack stack, LootRarity rarity, GenContext ctx) {
         int sockets = SocketHelper.getSockets(stack);
         int newSockets = ctx.rand().nextIntBetweenInclusive(this.min, this.max);
         if (newSockets > sockets) {
            SocketHelper.setSockets(stack, newSockets);
         }
      }
   }
}
