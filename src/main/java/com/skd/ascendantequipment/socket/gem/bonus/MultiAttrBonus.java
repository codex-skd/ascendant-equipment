package com.skd.ascendantequipment.socket.gem.bonus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class MultiAttrBonus extends GemBonus {
   public static Codec<MultiAttrBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            gemClass(),
            MultiAttrBonus.ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc)
         )
         .apply(inst, MultiAttrBonus::new)
   );
   protected final List<MultiAttrBonus.ModifierInst> modifiers;
   protected final String desc;

   public MultiAttrBonus(GemClass gemClass, List<MultiAttrBonus.ModifierInst> modifiers, String desc) {
      super(gemClass);
      this.modifiers = modifiers;
      this.desc = desc;
   }

   @Override
   public void addModifiers(GemInstance inst, StackAttributeModifiersEvent event) {
      int i = 0;

      for (MultiAttrBonus.ModifierInst modifier : this.modifiers) {
         event.addModifier(modifier.attr, modifier.build(makeUniqueId(inst, String.valueOf(i++)), inst.purity()), inst.category().getSlots());
      }
   }

   @Override
   public void skipModifierIds(GemInstance inst, Consumer<ResourceLocation> skip) {
      for (int i = 0; i < this.modifiers.size(); i++) {
         skip.accept(makeUniqueId(inst, String.valueOf(i)));
      }
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      Object[] values = new Object[this.modifiers.size() * 2];
      int i = 0;

      for (MultiAttrBonus.ModifierInst modifier : this.modifiers) {
         values[i] = ((Attribute)modifier.attr.value()).toComponent(modifier.build(makeUniqueId(inst, i + ""), inst.purity()), ctx.flag());
         values[this.modifiers.size() + i] = ((Attribute)modifier.attr.value()).toValueComponent(modifier.op, i, ctx.flag());
         i++;
      }

      return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.modifiers.get(0).values.containsKey(purity);
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   public static MultiAttrBonus.Builder builder() {
      return new MultiAttrBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private List<MultiAttrBonus.ModifierInst> modifiers = new ArrayList<>();
      private String desc;

      public MultiAttrBonus.Builder modifier(UnaryOperator<MultiAttrBonus.ModifierInst.Builder> config) {
         this.modifiers.add(config.apply(new MultiAttrBonus.ModifierInst.Builder()).build());
         return this;
      }

      public MultiAttrBonus.Builder desc(String desc) {
         this.desc = desc;
         return this;
      }

      public MultiAttrBonus build(GemClass gemClass) {
         return new MultiAttrBonus(gemClass, this.modifiers, this.desc);
      }
   }

   public record ModifierInst(Holder<Attribute> attr, Operation op, Map<Purity, Float> values) {
      public static Codec<MultiAttrBonus.ModifierInst> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(MultiAttrBonus.ModifierInst::attr),
               CommonToolkitCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(MultiAttrBonus.ModifierInst::op),
               Purity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(MultiAttrBonus.ModifierInst::values)
            )
            .apply(inst, MultiAttrBonus.ModifierInst::new)
      );

      public AttributeModifier build(ResourceLocation id, Purity purity) {
         return new AttributeModifier(id, this.values.get(purity).floatValue(), this.op);
      }

      public static class Builder {
         private Holder<Attribute> attr;
         private Operation op;
         private Map<Purity, Float> values = new HashMap<>();

         public MultiAttrBonus.ModifierInst.Builder attr(Holder<Attribute> attr) {
            this.attr = attr;
            return this;
         }

         public MultiAttrBonus.ModifierInst.Builder op(Operation op) {
            this.op = op;
            return this;
         }

         public MultiAttrBonus.ModifierInst.Builder value(Purity purity, float value) {
            this.values.put(purity, value);
            return this;
         }

         public MultiAttrBonus.ModifierInst build() {
            return new MultiAttrBonus.ModifierInst(this.attr, this.op, this.values);
         }
      }
   }
}
