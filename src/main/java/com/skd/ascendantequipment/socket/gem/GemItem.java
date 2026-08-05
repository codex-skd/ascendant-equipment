package com.skd.ascendantequipment.socket.gem;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.util.ApothMiscUtil;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.tabs.ITabFiller;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
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

   public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      UnsocketedGem inst = UnsocketedGem.of(stack);
      if (!inst.isValid()) {
         tooltip.accept(Component.literal("Errored gem with no bonus!").withStyle(ChatFormatting.GRAY));
      } else {
         inst.addInformation(tooltip, AttributeTooltipContext.of(ApothMiscUtil.getClientPlayer(), ctx, display, flag));
      }
   }

   public Component getName(ItemStack pStack) {
      UnsocketedGem inst = UnsocketedGem.of(pStack);
      if (!inst.isValid()) {
         return super.getName(pStack);
      }

      MutableComponent comp = Component.translatable(this.getGemDescriptionId(pStack));
      comp = Component.translatable("item.ascendant_equipment.gem." + inst.purity().getSerializedName(), new Object[]{comp});
      return comp.withStyle(Style.EMPTY.withColor(inst.purity().getColor()));
   }

   public String getGemDescriptionId(ItemStack pStack) {
      DynamicHolder<Gem> gem = getGem(pStack);
      return !gem.isBound() ? super.getDescriptionId() : super.getDescriptionId() + "." + gem.getId();
   }

   public boolean isFoil(ItemStack pStack) {
      UnsocketedGem inst = UnsocketedGem.of(pStack);
      return inst.isValid() && inst.isPerfect();
   }

   public boolean canBeHurtBy(ItemStack stack, DamageSource src) {
      return super.canBeHurtBy(stack, src) && !src.is(DamageTypes.FALLING_ANVIL);
   }

   public void fillItemCategory(CreativeModeTab group, BuildCreativeModeTabContentsEvent out) {
      GemRegistry.INSTANCE.getValues().stream().sorted(Comparator.comparing(Gem::getId)).forEach(gem -> Arrays.stream(Purity.values()).forEach(purity -> {
         if (purity.isAtLeast(gem.getMinPurity())) {
            ItemStack stack = gem.toStack(purity);
            out.accept(stack);
         }
      }));
   }

   @Nullable
   public String getCreatorModId(Provider registries, ItemStack stack) {
      UnsocketedGem inst = UnsocketedGem.of(stack);
      return inst.isValid() ? inst.gem().getId().getNamespace() : super.getCreatorModId(registries, stack);
   }

   public static DynamicHolder<Gem> getGem(ItemStack gem) {
      return (DynamicHolder<Gem>)gem.getOrDefault(AscEq.Components.GEM, GemRegistry.INSTANCE.emptyHolder());
   }

   public static void setGem(ItemStack gemStack, Gem gem) {
      gemStack.set(AscEq.Components.GEM, GemRegistry.INSTANCE.holder(gem));
   }

   public static Purity getPurity(ItemStack stack) {
      return (Purity)stack.getOrDefault(AscEq.Components.PURITY, Purity.CRACKED);
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
