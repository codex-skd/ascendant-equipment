package com.skd.ascendantequipment.socket.gem;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.tabs.ITabFiller;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.Nullable;

public class GemItem extends Item implements ITabFiller {
   public static final String HAS_REFRESHED = "has_refreshed";
   public static final String UUID_ARRAY = "uuids";
   public static final String GEM = "gem";

   public GemItem(Properties pProperties) {
      super(pProperties);
   }

   @Override
   public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      UnsocketedGem inst = UnsocketedGem.of(stack);
      if (!inst.isValid()) {
         tooltip.add(Component.literal("Errored gem with no bonus!").withStyle(ChatFormatting.GRAY));
         return;
      }
      inst.addInformation(tooltip::add, AttributeTooltipContext.of(ApothMiscUtil.getClientPlayer(), ctx, flag));
   }

   @Override
   public Component getName(ItemStack pStack) {
      UnsocketedGem inst = UnsocketedGem.of(pStack);
      if (!inst.isValid()) {
         return super.getName(pStack);
      }

      MutableComponent comp = Component.translatable(this.getDescriptionId(pStack));
      comp = Component.translatable("item.ascendant_equipment.gem." + inst.purity().getSerializedName(), comp);
      return comp.withStyle(Style.EMPTY.withColor(inst.purity().getColor()));
   }

   @Override
   public String getDescriptionId(ItemStack pStack) {
      DynamicHolder<Gem> gem = getGem(pStack);
      if (!gem.isBound()) {
         return super.getDescriptionId();
      }
      return super.getDescriptionId(pStack) + "." + gem.getId();
   }

   @Override
   public boolean isFoil(ItemStack pStack) {
      UnsocketedGem inst = UnsocketedGem.of(pStack);
      return inst.isValid() && inst.isPerfect();
   }

   @Override
   public boolean canBeHurtBy(ItemStack stack, DamageSource src) {
      return super.canBeHurtBy(stack, src) && !src.is(DamageTypes.FALLING_ANVIL);
   }

   @Override
   public void fillItemCategory(CreativeModeTab group, BuildCreativeModeTabContentsEvent out) {
      GemRegistry.INSTANCE.getValues().stream().sorted(Comparator.comparing(Gem::getId)).forEach(gem -> {
         Arrays.stream(Purity.values()).forEach(purity -> {
            if (purity.isAtLeast(gem.getMinPurity())) {
               ItemStack stack = gem.toStack(purity);
               out.accept(stack);
            }
         });
      });
   }

   @Override
   @Nullable
   public String getCreatorModId(ItemStack stack) {
      UnsocketedGem inst = UnsocketedGem.of(stack);
      if (inst.isValid()) {
         return inst.gem().getId().getNamespace();
      }
      return super.getCreatorModId(stack);
   }

   public static DynamicHolder<Gem> getGem(ItemStack gem) {
      return gem.getOrDefault(AscEq.Components.GEM, GemRegistry.INSTANCE.emptyHolder());
   }

   public static void setGem(ItemStack gemStack, Gem gem) {
      gemStack.set(AscEq.Components.GEM, GemRegistry.INSTANCE.holder(gem));
   }

   public static Purity getPurity(ItemStack stack) {
      return stack.getOrDefault(AscEq.Components.PURITY, Purity.CRACKED);
   }

   public static void setPurity(ItemStack stack, Purity purity) {
      stack.set(AscEq.Components.PURITY, purity);
   }

   public static ItemStack createStack(Gem gem, Purity purity, int count) {
      ItemStack stack = gem.toStack(purity);
      stack.setCount(count);
      return stack;
   }

   public static ItemStack createStack(Gem gem, Purity purity) {
      return createStack(gem, purity, 1);
   }
}
