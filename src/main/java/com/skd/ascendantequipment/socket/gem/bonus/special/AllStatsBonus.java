package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AllStatsBonus extends GemBonus {
   public static Codec<AllStatsBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            gemClass(),
            CommonToolkitCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            Purity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(a -> a.values),
            RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).fieldOf("attributes").forGetter(a -> a.attributes)
         )
         .apply(inst, AllStatsBonus::new)
   );
   protected final Operation operation;
   protected final Map<Purity, Float> values;
   protected final HolderSet<Attribute> attributes;

   public AllStatsBonus(GemClass gemClass, Operation op, Map<Purity, Float> values, HolderSet<Attribute> attributes) {
      super(gemClass);
      this.operation = op;
      this.values = values;
      this.attributes = attributes;
   }

   @Override
   public void addModifiers(GemInstance inst, StackAttributeModifiersEvent event) {
      int idx = 0;

      for (Holder<Attribute> attr : this.attributes) {
         Identifier id = makeUniqueId(inst, idx++ + "");
         AttributeModifier modif = new AttributeModifier(id, this.values.get(inst.purity()).floatValue(), this.operation);
         event.addModifier(attr, modif, inst.category().getSlots());
      }
   }

   @Override
   public void skipModifierIds(GemInstance inst, Consumer<Identifier> skip) {
      for (int i = 0; i < this.attributes.size(); i++) {
         skip.accept(makeUniqueId(inst, i + ""));
      }
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      float value = this.values.get(inst.purity());
      return Component.translatable("bonus." + this.getTypeKey() + ".desc", new Object[]{Affix.fmt(value * 100.0F)}).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   public static AllStatsBonus.Builder builder() {
      return new AllStatsBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private final Map<Purity, Float> values = new LinkedHashMap<>();
      private final List<Holder<Attribute>> attributes = new ArrayList<>();
      private Operation operation;

      private Builder() {
      }

      @SafeVarargs
      public final AllStatsBonus.Builder attributes(Holder<Attribute>... attributes) {
         for (Holder<Attribute> a : attributes) {
            this.attributes.add(a);
         }

         return this;
      }

      public AllStatsBonus.Builder op(Operation operation) {
         this.operation = operation;
         return this;
      }

      public AllStatsBonus.Builder value(Purity purity, float value) {
         this.values.put(purity, value);
         return this;
      }

      public AllStatsBonus build(GemClass gClass) {
         return new AllStatsBonus(gClass, this.operation, this.values, HolderSet.direct(this.attributes));
      }
   }
}
