package com.skd.ascendantequipment.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import java.util.Optional;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.ItemPredicate.Builder;
import net.minecraft.advancements.predicates.MinMaxBounds.Ints;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class EquippedItemTrigger extends SimpleCriterionTrigger<EquippedItemTrigger.TriggerInstance> {
   public Codec<EquippedItemTrigger.TriggerInstance> codec() {
      return EquippedItemTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
      this.trigger(player, inst -> inst.matches(slot, stack));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, EquipmentSlotGroup slots, ItemPredicate items) implements SimpleInstance {
      public static final Codec<EquippedItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EquippedItemTrigger.TriggerInstance::player),
               EquipmentSlotGroup.CODEC.optionalFieldOf("slots", EquipmentSlotGroup.ANY).forGetter(EquippedItemTrigger.TriggerInstance::slots),
               ItemPredicate.CODEC.optionalFieldOf("items", Builder.item().build()).forGetter(EquippedItemTrigger.TriggerInstance::items)
            )
            .apply(inst, EquippedItemTrigger.TriggerInstance::new)
      );

      public static Criterion<EquippedItemTrigger.TriggerInstance> hasItems(EquipmentSlotGroup slots, Builder items) {
         return hasItems(slots, items.build());
      }

      public static Criterion<EquippedItemTrigger.TriggerInstance> hasItems(EquipmentSlotGroup slots, ItemPredicate items) {
         return AscEq.Triggers.EQUIPPED_ITEM.createCriterion(new EquippedItemTrigger.TriggerInstance(Optional.empty(), slots, items));
      }

      public static Criterion<EquippedItemTrigger.TriggerInstance> hasItems(EquipmentSlotGroup slots, ItemLike item) {
         ItemPredicate predicate = new ItemPredicate(
            Optional.of(HolderSet.direct(new Holder[]{item.asItem().builtInRegistryHolder()})), Ints.ANY, DataComponentMatchers.ANY
         );
         return hasItems(slots, predicate);
      }

      public boolean matches(EquipmentSlot slot, ItemStack stack) {
         return !this.slots.test(slot) ? false : !stack.isEmpty() && this.items.test(stack);
      }
   }
}
