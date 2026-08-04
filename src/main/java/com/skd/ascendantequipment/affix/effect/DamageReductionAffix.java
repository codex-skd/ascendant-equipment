package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantattributes.util.AttributesUtil;
import com.skd.commontoolkit.util.StepFunction;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags.DamageTypes;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class DamageReductionAffix extends Affix {
   public static final Codec<DamageReductionAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            DamageReductionAffix.DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories)
         )
         .apply(inst, DamageReductionAffix::new)
   );
   protected final DamageReductionAffix.DamageType type;
   protected final Map<LootRarity, StepFunction> values;
   protected final Set<LootCategory> categories;

   public DamageReductionAffix(AffixDefinition def, DamageReductionAffix.DamageType type, Map<LootRarity, StepFunction> values, Set<LootCategory> categories) {
      super(def);
      this.type = type;
      this.values = values;
      this.categories = categories;
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return !cat.isNone() && (this.categories.isEmpty() || this.categories.contains(cat)) && this.values.containsKey(rarity);
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable(
         "affix.ascendant_equipment:damage_reduction.desc",
         new Object[]{Component.translatable("misc.ascendant_equipment." + this.type.id), fmt(100.0F * this.getTrueLevel(inst.getRarity(), inst.level()))}
      );
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      Component minComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 0.0F))});
      Component maxComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 1.0F))});
      return comp.append(valueBounds(minComp, maxComp));
   }

   @Override
   public float onHurt(AffixInstance inst, DamageSource src, LivingEntity ent, float amount) {
      return !src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !src.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && this.type.test(src)
         ? amount * (1.0F - this.getTrueLevel(inst.getRarity(), inst.level()))
         : super.onHurt(inst, src, ent, amount);
   }

   private float getTrueLevel(LootRarity rarity, float level) {
      return this.values.get(rarity).get(level);
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).isConstant();
   }

   public static class Builder extends AffixBuilder.ValuedAffixBuilder<DamageReductionAffix.Builder> {
      protected final DamageReductionAffix.DamageType type;
      protected final Set<LootCategory> categories = new LinkedHashSet<>();

      public Builder(DamageReductionAffix.DamageType type) {
         this.type = type;
      }

      public DamageReductionAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public DamageReductionAffix build() {
         Preconditions.checkArgument(!this.values.isEmpty());
         return new DamageReductionAffix(this.definition, this.type, this.values, this.categories);
      }
   }

   public enum DamageType implements Predicate<DamageSource>, StringRepresentable {
      PHYSICAL("physical", AttributesUtil::isPhysicalDamage),
      MAGIC("magic", d -> d.is(DamageTypes.IS_MAGIC)),
      FIRE("fire", d -> d.is(DamageTypeTags.IS_FIRE)),
      FALL("fall", d -> d.is(DamageTypeTags.IS_FALL)),
      EXPLOSION("explosion", d -> d.is(DamageTypeTags.IS_EXPLOSION)),
      PROJECTILE("projectile", d -> d.is(DamageTypeTags.IS_PROJECTILE)),
      LIGHTNING("lightning", d -> d.is(DamageTypeTags.IS_LIGHTNING));

      public static final IntFunction<DamageReductionAffix.DamageType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.CLAMP);
      public static final Codec<DamageReductionAffix.DamageType> CODEC = StringRepresentable.fromValues(DamageReductionAffix.DamageType::values);
      public static final StreamCodec<ByteBuf, DamageReductionAffix.DamageType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
      private final String id;
      private final Predicate<DamageSource> predicate;

      DamageType(String id, Predicate<DamageSource> predicate) {
         this.id = id;
         this.predicate = predicate;
      }

      public boolean test(DamageSource t) {
         return this.predicate.test(t);
      }

      public String getSerializedName() {
         return this.id;
      }
   }
}
