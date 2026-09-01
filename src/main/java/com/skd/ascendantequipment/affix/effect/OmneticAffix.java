package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.util.OmneticUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class OmneticAffix extends Affix {
   public static final Codec<OmneticAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(OmneticUtil.OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, OmneticAffix::new)
   );
   protected final Set<LootCategory> categories;
   protected final Map<LootRarity, OmneticUtil.OmneticData> values;

   public OmneticAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, OmneticUtil.OmneticData> values) {
      super(def);
      this.categories = categories;
      this.values = values;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return this.categories.contains(cat) && this.values.containsKey(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable(
         "affix." + this.id() + ".desc", new Object[]{Component.translatable("misc.ascendant_equipment." + this.values.get(inst.getRarity()).name())}
      );
   }

   public static void harvest(HarvestCheck e) {
      ItemStack stack = e.getEntity().getMainHandItem();
      if (!stack.isEmpty()) {
         AffixInstance inst = AffixHelper.streamAffixes(stack).filter(i -> i.getAffix() instanceof OmneticAffix).findFirst().orElse(null);
         if (inst != null && inst.isValid()) {
            OmneticUtil.OmneticData data = ((OmneticAffix)inst.getAffix()).values.get(inst.rarity().get());
            OmneticUtil.applyOmneticData(e, data);
         }
      }
   }

   public static void speed(BreakSpeed e) {
      ItemStack stack = e.getEntity().getMainHandItem();
      if (!stack.isEmpty()) {
         AffixInstance inst = AffixHelper.streamAffixes(stack).filter(i -> i.getAffix() instanceof OmneticAffix).findFirst().orElse(null);
         if (inst != null && inst.isValid()) {
            OmneticUtil.OmneticData data = ((OmneticAffix)inst.getAffix()).values.get(inst.rarity().get());
            OmneticUtil.applyOmneticData(e, data);
         }
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return true;
   }

   public static class Builder extends AffixBuilder<OmneticAffix.Builder> {
      protected final Set<LootCategory> categories = new LinkedHashSet<>();
      private final Map<LootRarity, OmneticUtil.OmneticData> values = new HashMap<>();

      public OmneticAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public OmneticAffix.Builder value(LootRarity rarity, String name, Item... items) {
         OmneticUtil.OmneticData data = new OmneticUtil.OmneticData(name, Arrays.stream(items).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
         this.values.put(rarity, data);
         return this;
      }

      public OmneticAffix build() {
         Preconditions.checkNotNull(this.definition);
         return new OmneticAffix(this.definition, this.categories, this.values);
      }
   }
}
