package com.skd.ascendantequipment.loot.functions;

import com.skd.ascendantequipment.tiers.GenContext;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class ContextualLootFunction extends LootItemConditionalFunction {
   protected ContextualLootFunction(List<LootItemCondition> predicates) {
      super(predicates);
   }

   protected final ItemStack run(ItemStack stack, LootContext ctx) {
      GenContext gCtx = GenContext.forLoot(ctx);
      return gCtx != null ? this.run(stack, ctx, gCtx) : stack;
   }

   protected abstract ItemStack run(ItemStack var1, LootContext var2, GenContext var3);
}
