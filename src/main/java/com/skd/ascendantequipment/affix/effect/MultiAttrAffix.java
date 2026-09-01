package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.AttributeProvidingAffix;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.util.StepFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.spongepowered.include.com.google.common.base.Preconditions;

public class MultiAttrAffix extends Affix implements AttributeProvidingAffix {
   public static final Codec<MultiAttrAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            MultiAttrAffix.ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories)
         )
         .apply(inst, MultiAttrAffix::new)
   );
   protected final List<MultiAttrAffix.ModifierInst> modifiers;
   protected final String desc;
   protected final Set<LootCategory> categories;
   protected final transient Set<LootRarity> rarities;

   public MultiAttrAffix(AffixDefinition def, List<MultiAttrAffix.ModifierInst> modifiers, String desc, Set<LootCategory> categories) {
      super(def);
      this.modifiers = modifiers;
      this.desc = desc;
      this.categories = categories;
      Set<LootRarity> rarities = new HashSet<>();

      for (int i = 0; i < modifiers.size(); i++) {
         MultiAttrAffix.ModifierInst inst = modifiers.get(i);
         if (rarities.isEmpty()) {
            rarities.addAll(inst.values.keySet());
         }

         if (!rarities.equals(inst.values.keySet())) {
            throw new IllegalArgumentException(
               "Disjoint rarity sets at modifier index " + i + "! Specified set: " + rarities + " but found: " + inst.values.keySet()
            );
         }
      }

      this.rarities = rarities;
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.empty();
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      Object[] values = new Object[this.modifiers.size()];

      for (int i = 0; i < this.modifiers.size(); i++) {
         MultiAttrAffix.ModifierInst modif = this.modifiers.get(i);
         Attribute attr = (Attribute)modif.attr().value();
         MutableComponent comp = attr.toComponent(modif.build(inst, i), ctx.flag());
         StepFunction valueFactory = modif.values.get(inst.getRarity());
         if (valueFactory.get(0.0F) != valueFactory.get(1.0F)) {
            Component minComp = attr.toValueComponent(modif.op, valueFactory.get(0.0F), ctx.flag());
            Component maxComp = attr.toValueComponent(modif.op, valueFactory.get(1.0F), ctx.flag());
            comp.append(valueBounds(minComp, maxComp));
         }

         values[i] = comp;
      }

      return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public void addModifiers(AffixInstance inst, StackAttributeModifiersEvent event) {
      LootCategory cat = inst.category();
      if (cat.isNone()) {
         AscendantEquipment.LOGGER
            .debug(
               "Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!",
               this.id(),
               inst.stack().getHoverName().getString()
            );
      } else {
         for (int i = 0; i < this.modifiers.size(); i++) {
            MultiAttrAffix.ModifierInst modif = this.modifiers.get(i);
            if (modif.attr == null) {
               AscendantEquipment.LOGGER
                  .debug("The affix {} has attempted to apply a null attribute modifier to {}!", this.id(), inst.stack().getHoverName().getString());
               return;
            }

            event.addModifier(modif.attr(), modif.build(inst, i), cat.getSlots());
         }
      }
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return cat.isNone() ? false : (this.categories.isEmpty() || this.categories.contains(cat)) && this.rarities.contains(rarity);
   }

   @Override
   public void gatherModifierTooltips(AffixInstance inst, AttributeTooltipContext ctx, Consumer<Component> list) {
      for (int i = 0; i < this.modifiers.size(); i++) {
         MultiAttrAffix.ModifierInst modif = this.modifiers.get(i);
         Attribute attr = (Attribute)modif.attr.value();
         list.accept(attr.toComponent(modif.build(inst, i), ctx.flag()));
      }
   }

   @Override
   public void skipModifierIds(AffixInstance inst, AttributeTooltipContext ctx, Consumer<ResourceLocation> skip) {
      if (ctx.player() != null && WorldTier.isTutorialActive(ctx.player())) {
         for (int i = 0; i < this.modifiers.size(); i++) {
            skip.accept(inst.makeUniqueId(i + ""));
         }
      }
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      for (MultiAttrAffix.ModifierInst modif : this.modifiers) {
         if (!modif.values.get(inst.getRarity()).isConstant()) {
            return false;
         }
      }

      return true;
   }

   public static MultiAttrAffix.Builder builder() {
      return new MultiAttrAffix.Builder();
   }

   public static class Builder extends AffixBuilder<MultiAttrAffix.Builder> {
      protected final Set<LootCategory> categories = new HashSet<>();
      protected final List<MultiAttrAffix.ModifierInst> modifiers = new ArrayList<>();
      protected String desc;

      public MultiAttrAffix.Builder modifier(UnaryOperator<MultiAttrAffix.ModifierInst.Builder> config) {
         this.modifiers.add(config.apply(new MultiAttrAffix.ModifierInst.Builder()).build());
         return this;
      }

      public MultiAttrAffix.Builder desc(String desc) {
         this.desc = desc;
         return this;
      }

      public MultiAttrAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public MultiAttrAffix build() {
         Preconditions.checkArgument(!this.modifiers.isEmpty());
         Preconditions.checkArgument(this.desc != null);
         return new MultiAttrAffix(this.definition, this.modifiers, this.desc, this.categories);
      }
   }

   public record ModifierInst(Holder<Attribute> attr, Operation op, Map<LootRarity, StepFunction> values) {
      public static Codec<MultiAttrAffix.ModifierInst> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(MultiAttrAffix.ModifierInst::attr),
               CommonToolkitCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(MultiAttrAffix.ModifierInst::op),
               LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(MultiAttrAffix.ModifierInst::values)
            )
            .apply(inst, MultiAttrAffix.ModifierInst::new)
      );

      public AttributeModifier build(AffixInstance inst, int idx) {
         return new AttributeModifier(inst.makeUniqueId(idx + ""), this.values.get(inst.getRarity()).get(inst.level()), this.op);
      }

      public static class Builder {
         private Holder<Attribute> attr;
         private Operation op;
         protected final Map<LootRarity, StepFunction> values = new HashMap<>();
         protected float step = 0.01F;

         public MultiAttrAffix.ModifierInst.Builder attr(Holder<Attribute> attr) {
            this.attr = attr;
            return this;
         }

         public MultiAttrAffix.ModifierInst.Builder op(Operation op) {
            this.op = op;
            return this;
         }

         public MultiAttrAffix.ModifierInst.Builder step(float step) {
            this.step = step;
            return this;
         }

         public MultiAttrAffix.ModifierInst.Builder value(LootRarity rarity, float min, float max) {
            return this.value(rarity, StepFunction.fromBounds(min, max, this.step));
         }

         public MultiAttrAffix.ModifierInst.Builder value(LootRarity rarity, float value) {
            return this.value(rarity, StepFunction.constant(value));
         }

         public MultiAttrAffix.ModifierInst.Builder value(LootRarity rarity, StepFunction function) {
            this.values.put(rarity, function);
            return this;
         }

         public MultiAttrAffix.ModifierInst build() {
            return new MultiAttrAffix.ModifierInst(this.attr, this.op, this.values);
         }
      }
   }
}
