package com.skd.ascendantequipment.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.LootPatternMatcher;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AffixConvertLootModifier extends ContextualLootModifier {
   public static final MapCodec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst)
         .and(AffixConvertLootModifier.AffixConversionEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
         .apply(inst, AffixConvertLootModifier::new)
   );
   protected final List<AffixConvertLootModifier.AffixConversionEntry> entries;

   public AffixConvertLootModifier(LootItemCondition[] conditions, int priority, List<AffixConvertLootModifier.AffixConversionEntry> entries) {
      super(conditions, priority);
      this.entries = entries;
   }

   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, GenContext gCtx) {
      for (AffixConvertLootModifier.AffixConversionEntry entry : this.entries) {
         if (entry.pattern.matches(context.getQueriedLootTableId())) {
            RandomSource rand = context.getRandom();
            if (!(entry.chance() <= 0.0F)) {
               for (ItemStack s : generatedLoot) {
                  if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= entry.chance()) {
                     LootRarity rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
                     LootController.createLootItem(s, rarity, gCtx);
                     NameHelper.setItemName(rand, s);
                  }
               }
            }
            break;
         }
      }

      return generatedLoot;
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   public record AffixConversionEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<LootRarity>> rarities) {
      public static final Codec<AffixConvertLootModifier.AffixConversionEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixConvertLootModifier.AffixConversionEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(AffixConvertLootModifier.AffixConversionEntry::chance),
               CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("rarities", Set.of())
                  .forGetter(AffixConvertLootModifier.AffixConversionEntry::rarities)
            )
            .apply(inst, AffixConvertLootModifier.AffixConversionEntry::new)
      );
   }
}
