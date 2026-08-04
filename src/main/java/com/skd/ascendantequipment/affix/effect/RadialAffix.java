package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.util.RadialUtil;
import com.skd.commontoolkit.util.CachedObject;
import com.skd.commontoolkit.util.CachedObject.CachedObjectSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class RadialAffix extends Affix {
   public static final Codec<RadialAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(Codec.list(RadialUtil.RadialData.CODEC)).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, RadialAffix::new)
   );
   public static final Identifier AFFIX_RADIAL_DATA_CACHED_OBJECT = AscendantEquipment.loc("afx_radial_data");
   protected final Set<LootCategory> categories;
   protected final Map<LootRarity, List<RadialUtil.RadialData>> values;

   public RadialAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, List<RadialUtil.RadialData>> values) {
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
      RadialUtil.RadialData data = this.getTrueLevel(inst);
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{data.x(), data.y()});
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      RadialUtil.RadialData min = this.getTrueLevel(inst.getRarity(), 0.0F);
      RadialUtil.RadialData max = this.getTrueLevel(inst.getRarity(), 1.0F);
      if (min != max) {
         Component minComp = Component.translatable("%sx%s", new Object[]{min.x(), min.y()});
         Component maxComp = Component.translatable("%sx%s", new Object[]{max.x(), max.y()});
         comp.append(valueBounds(minComp, maxComp));
      }

      return comp;
   }

   public static void onBreak(BreakBlockEvent e) {
      Player player = e.getPlayer();
      RadialUtil.RadialData data = getRadialData(player.getMainHandItem());
      if (data != null) {
         RadialUtil.attemptRadialMining(e, data);
      }
   }

   @Nullable
   public static RadialUtil.RadialData getRadialData(ItemStack tool) {
      return (RadialUtil.RadialData)CachedObjectSource.getOrCreate(
         tool, AFFIX_RADIAL_DATA_CACHED_OBJECT, RadialAffix::getRadialDataImpl, CachedObject.hashComponents(new DataComponentType[]{AscEq.Components.AFFIXES})
      );
   }

   @Nullable
   private static RadialUtil.RadialData getRadialDataImpl(ItemStack tool) {
      if (tool.has(AscEq.Components.AFFIXES)) {
         AffixInstance inst = AffixHelper.streamAffixes(tool).filter(i -> i.getAffix() instanceof RadialAffix).findFirst().orElse(null);
         if (inst != null && inst.isValid()) {
            return ((RadialAffix)inst.getAffix()).getTrueLevel((LootRarity)inst.rarity().get(), inst.level());
         }
      }

      return null;
   }

   private RadialUtil.RadialData getTrueLevel(AffixInstance inst) {
      return this.getTrueLevel(inst.getRarity(), inst.level());
   }

   private RadialUtil.RadialData getTrueLevel(LootRarity rarity, float level) {
      List<RadialUtil.RadialData> list = this.values.get(rarity);
      return list.get(Math.min(list.size() - 1, (int)Mth.lerp(level, 0.0F, list.size())));
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).size() == 1;
   }

   public static class Builder extends AffixBuilder<RadialAffix.Builder> {
      protected final Map<LootRarity, List<RadialUtil.RadialData>> values = new HashMap<>();
      protected final Set<LootCategory> categories = new LinkedHashSet<>();

      public RadialAffix.Builder value(LootRarity rarity, UnaryOperator<RadialAffix.Builder.DataListBuilder> config) {
         final List<RadialUtil.RadialData> list = new ArrayList<>();
         config.apply(new RadialAffix.Builder.DataListBuilder() {
            @Override
            public RadialAffix.Builder.DataListBuilder radii(int x, int y, int xOffset, int yOffset) {
               list.add(new RadialUtil.RadialData(x, y, xOffset, yOffset));
               return this;
            }
         });
         this.values.put(rarity, list);
         return this;
      }

      public RadialAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public RadialAffix build() {
         Preconditions.checkNotNull(this.definition);
         Preconditions.checkArgument(this.values.size() > 0);
         Preconditions.checkArgument(this.categories.size() > 0);
         return new RadialAffix(this.definition, this.categories, this.values);
      }

      public interface DataListBuilder {
         RadialAffix.Builder.DataListBuilder radii(int var1, int var2, int var3, int var4);

         default RadialAffix.Builder.DataListBuilder radii(int x, int y) {
            return this.radii(x, y, 0, 0);
         }
      }
   }
}
