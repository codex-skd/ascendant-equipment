package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class StoneformingAffix extends Affix {
   public static final Codec<StoneformingAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("candidates").forGetter(a -> a.candidates)
         )
         .apply(inst, StoneformingAffix::new)
   );
   public static final Component TOOLTIP_MARKER = Component.literal("ASCENDANT_EQUIPMENT_STONEFORMING_MARKER");
   protected final Set<LootCategory> categories;
   protected final HolderSet<Block> candidates;

   public StoneformingAffix(AffixDefinition definition, Set<LootCategory> categories, HolderSet<Block> candidates) {
      super(definition);
      this.categories = categories;
      this.candidates = candidates;
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return true;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return this.categories.contains(cat);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return TOOLTIP_MARKER.copy();
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      return super.getDescription(inst, ctx);
   }

   @Override
   public void modifyLoot(AffixInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
      if (ctx.hasParameter(LootContextParams.BLOCK_STATE)) {
         Block block = ((BlockState)ctx.getParameter(LootContextParams.BLOCK_STATE)).getBlock();
         if (this.isCandidate(block)) {
            for (int i = 0; i < loot.size(); i++) {
               ItemStack stack = (ItemStack)loot.get(i);
               if (stack.getItem() instanceof BlockItem bi) {
                  Block lootBlock = bi.getBlock();
                  if (this.isCandidate(lootBlock)) {
                     loot.set(i, stack.transmuteCopy(this.getTarget(inst)));
                  }
               }
            }
         }
      }
   }

   @Override
   public InteractionResult onItemUse(AffixInstance inst, UseOnContext ctx) {
      BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
      Block block = state.getBlock();
      if (this.isCandidate(block) && this.getTarget(inst) != block && ctx.getPlayer().isShiftKeyDown()) {
         if (!ctx.getLevel().isClientSide()) {
            inst.stack().set(AscEq.Components.STONEFORMING_TARGET, block);
            ctx.getPlayer().sendSystemMessage(AscendantEquipment.lang("affix", "stoneforming.target_updated", block.getName()));
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.onItemUse(inst, ctx);
      }
   }

   protected boolean isCandidate(Block block) {
      return this.candidates.contains(BuiltInRegistries.BLOCK.wrapAsHolder(block));
   }

   public Block getTarget(AffixInstance inst) {
      Block target = (Block)inst.stack().get(AscEq.Components.STONEFORMING_TARGET);
      if (target != null && this.isCandidate(target)) {
         return target;
      } else {
         return this.candidates.size() > 0 ? (Block)this.candidates.get(0).value() : Blocks.AIR;
      }
   }

   public HolderSet<Block> getCandidates() {
      return this.candidates;
   }
}
