package com.skd.ascendantequipment.mobs.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.mobs.util.EntityModifier;
import com.skd.ascendantequipment.mobs.util.SpawnCondition;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

public record Augmentation(float chance, Constraints constraints, List<SpawnCondition> conditions, List<EntityModifier> modifiers) {
   public static final Codec<Augmentation> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("application_chance").forGetter(Augmentation::chance),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Augmentation::constraints),
            SpawnCondition.CODEC.listOf().optionalFieldOf("conditions", Collections.emptyList()).forGetter(Augmentation::conditions),
            EntityModifier.CODEC.listOf().fieldOf("modifiers").forGetter(Augmentation::modifiers)
         )
         .apply(inst, Augmentation::new)
   );

   public boolean canApply(ServerLevelAccessor level, Mob mob, EntitySpawnReason type, GenContext ctx) {
      return !this.constraints.test(ctx) ? false : SpawnCondition.checkAll(this.conditions, mob, level, type);
   }

   public void apply(Mob mob, GenContext ctx) {
      for (EntityModifier em : this.modifiers) {
         em.apply(mob, ctx);
      }
   }

   public static Augmentation.Builder builder() {
      return new Augmentation.Builder();
   }

   public static class Builder {
      private float chance = 1.0F;
      private Constraints constraints = Constraints.EMPTY;
      private List<SpawnCondition> conditions = new ArrayList<>();
      private List<EntityModifier> modifiers = new ArrayList<>();

      public Augmentation.Builder chance(float chance) {
         this.chance = chance;
         return this;
      }

      public Augmentation.Builder constraints(Constraints constraints) {
         this.constraints = constraints;
         return this;
      }

      public Augmentation.Builder conditions(SpawnCondition... condition) {
         this.conditions.addAll(Arrays.asList(condition));
         return this;
      }

      public Augmentation.Builder modifiers(EntityModifier... modifiers) {
         this.modifiers.addAll(Arrays.asList(modifiers));
         return this;
      }

      public Augmentation build() {
         if (this.modifiers.isEmpty()) {
            throw new IllegalStateException("At least one modifier must be added");
         } else {
            return new Augmentation(this.chance, this.constraints, this.conditions, this.modifiers);
         }
      }
   }
}
