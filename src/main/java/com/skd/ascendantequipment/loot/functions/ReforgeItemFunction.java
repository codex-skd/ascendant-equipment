package com.skd.ascendantequipment.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ReforgeItemFunction extends ContextualLootFunction {
   public static final MapCodec<ReforgeItemFunction> CODEC = RecordCodecBuilder.mapCodec(
      inst -> commonFields(inst)
         .and(CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities))
         .apply(inst, ReforgeItemFunction::new)
   );
   private final Set<DynamicHolder<LootRarity>> rarities;

   public ReforgeItemFunction(List<LootItemCondition> predicates, Set<DynamicHolder<LootRarity>> rarities) {
      super(predicates);
      this.rarities = rarities;
   }

   public MapCodec<ReforgeItemFunction> codec() {
      return CODEC;
   }

   @Override
   public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
      return AscEq.LootFunctions.REFORGE_ITEM;
   }

   @Override
   protected ItemStack run(ItemStack stack, LootContext ctx, GenContext gCtx) {
      LootCategory cat = LootCategory.forItem(stack);
      if (!cat.isNone()) {
         LootRarity rarity = LootRarity.randomFromHolders(gCtx, this.rarities);
         return LootController.createLootItem(stack, rarity, gCtx);
      } else {
         return stack;
      }
   }
}
