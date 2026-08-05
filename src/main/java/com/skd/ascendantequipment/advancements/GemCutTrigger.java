package com.skd.ascendantequipment.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import java.util.Optional;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GemCutTrigger extends SimpleCriterionTrigger<GemCutTrigger.Instance> {
   public Codec<GemCutTrigger.Instance> codec() {
      return GemCutTrigger.Instance.CODEC;
   }

   public void trigger(ServerPlayer player, ItemStack stack) {
      UnsocketedGem gem = UnsocketedGem.of(stack);
      if (gem.isValid()) {
         this.trigger(player, inst -> inst.test(stack, gem));
      }
   }

   public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate gem, Optional<Purity> purity) implements SimpleInstance {
      public static final Codec<GemCutTrigger.Instance> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(GemCutTrigger.Instance::player),
               ItemPredicate.CODEC.fieldOf("item").forGetter(GemCutTrigger.Instance::gem),
               Purity.CODEC.optionalFieldOf("purity").forGetter(GemCutTrigger.Instance::purity)
            )
            .apply(inst, GemCutTrigger.Instance::new)
      );

      public boolean test(ItemStack stack, UnsocketedGem inst) {
         return this.gem.test(stack) && (this.purity.isEmpty() || this.purity.get() == inst.purity());
      }
   }
}
