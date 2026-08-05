package com.skd.ascendantequipment.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.Gem;
import com.skd.ascendantequipment.socket.gem.GemRegistry;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.ascendantequipment.util.LootPatternMatcher;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.Nullable;

public class GemLootModifier extends ContextualLootModifier {
   public static final MapCodec<GemLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst).and(GemLootModifier.GemTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries)).apply(inst, GemLootModifier::new)
   );
   protected final List<GemLootModifier.GemTableEntry> entries;

   public GemLootModifier(LootItemCondition[] conditions, int priority, List<GemLootModifier.GemTableEntry> entries) {
      super(conditions, priority);
      this.entries = entries;
   }

   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
      for (GemLootModifier.GemTableEntry entry : this.entries) {
         if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
            if (!(ctx.getRandom().nextFloat() <= entry.chance())) {
               break;
            }

            Purity purity = Purity.random(gCtx, entry.purities);
            Gem gem;
            if (!entry.gems.isEmpty()) {
               List<Weighted<Gem>> resolved = entry.gems.stream().mapMulti(TieredWeights.wrapFilterHolders(gCtx)).toList();
               gem = WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight).get().value();
            } else {
               gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
            }

            if (gem != null) {
               generatedLoot.add(gem.toStack(purity));
               break;
            }

            AscendantEquipment.LOGGER.error("A GemLootModifier (entry {}) failed to resolve a gem for table {}!", entry.toString(), ctx.getQueriedLootTableId());
         }
      }

      return generatedLoot;
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   @Nullable
   private Gem unwrap(DynamicHolder<Gem> holder) {
      if (!holder.isBound()) {
         AscendantEquipment.LOGGER.error("A GemLootModifier failed to resolve the Gem {}!", holder.getId());
         return null;
      } else {
         return holder.get();
      }
   }

   public record GemTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<Gem>> gems, Set<Purity> purities) {
      public static final Codec<GemLootModifier.GemTableEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(GemLootModifier.GemTableEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(GemLootModifier.GemTableEntry::chance),
               CommonToolkitCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(GemLootModifier.GemTableEntry::gems),
               CommonToolkitCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(GemLootModifier.GemTableEntry::purities)
            )
            .apply(inst, GemLootModifier.GemTableEntry::new)
      );
   }
}
