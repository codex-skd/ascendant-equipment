package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.codec.CodecMap;
import com.skd.commontoolkit.codec.CodecProvider;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.json.NBTAdapter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;

public interface SpawnCondition extends CodecProvider<SpawnCondition> {
   CodecMap<SpawnCondition> CODEC = new CodecMap("Apothic Spawn Conditions");

   boolean test(Mob var1, ServerLevelAccessor var2, MobSpawnType var3, @Nullable CompoundTag var4);

   default boolean requiresNbtAccess() {
      return false;
   }

   static boolean checkAll(List<SpawnCondition> conditions, Mob mob, ServerLevelAccessor level, MobSpawnType type) {
      if (conditions.isEmpty()) {
         return true;
      }

      boolean requiresNbt = false;

      for (SpawnCondition ex : conditions) {
         requiresNbt |= ex.requiresNbtAccess();
      }

      CompoundTag nbt = requiresNbt ? mob.saveWithoutId(new CompoundTag()) : null;

      boolean success = true;

      for (SpawnCondition ex : conditions) {
         success &= ex.test(mob, level, type, nbt);
      }

      return success;
   }

   static void initCodecs() {
      register("spawn_type", SpawnCondition.SpawnTypeCondition.CODEC);
      register("surface_type", SpawnCondition.SurfaceTypeCondition.CODEC);
      register("has_tag", SpawnCondition.EntityTagCondition.CODEC);
      register("is_monster", SpawnCondition.IsMonsterCondition.CODEC);
      register("nbt", SpawnCondition.NbtCondition.CODEC);
      register("and", SpawnCondition.AndCondition.CODEC);
      register("or", SpawnCondition.OrCondition.CODEC);
      register("not", SpawnCondition.NotCondition.CODEC);
      register("xor", SpawnCondition.XorCondition.CODEC);
   }

   private static void register(String id, Codec<? extends SpawnCondition> codec) {
      CODEC.register(AscendantEquipment.loc(id), codec);
   }

   record AndCondition(List<SpawnCondition> spawnConditions) implements SpawnCondition {
      public static Codec<SpawnCondition.AndCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(SpawnCondition.CODEC.listOf().fieldOf("spawn_conditions").forGetter(SpawnCondition.AndCondition::spawnConditions))
            .apply(inst, SpawnCondition.AndCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
         boolean success = true;

         for (SpawnCondition cond : this.spawnConditions) {
            success &= cond.test(mob, level, type, nbt);
         }

         return success;
      }

      @Override
      public boolean requiresNbtAccess() {
         boolean requiresNbt = false;

         for (SpawnCondition ex : this.spawnConditions) {
            requiresNbt |= ex.requiresNbtAccess();
         }

         return requiresNbt;
      }
   }

   record EntityTagCondition(TagKey<EntityType<?>> tag) implements SpawnCondition {
      public static Codec<SpawnCondition.EntityTagCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(SpawnCondition.EntityTagCondition::tag))
            .apply(inst, SpawnCondition.EntityTagCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
         return mob.getType().builtInRegistryHolder().is(this.tag);
      }
   }

   record IsMonsterCondition() implements SpawnCondition {
      public static final SpawnCondition.IsMonsterCondition INSTANCE = new SpawnCondition.IsMonsterCondition();
      public static Codec<SpawnCondition.IsMonsterCondition> CODEC = MapCodec.unit(INSTANCE).codec();

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
         return mob instanceof Monster;
      }
   }

   record NbtCondition(CompoundTag nbt) implements SpawnCondition {
      public static Codec<SpawnCondition.NbtCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(NBTAdapter.EITHER_CODEC.fieldOf("nbt").forGetter(SpawnCondition.NbtCondition::nbt)).apply(inst, SpawnCondition.NbtCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
         return NbtUtils.compareNbt(this.nbt, entityNbt, true);
      }

      @Override
      public boolean requiresNbtAccess() {
         return true;
      }
   }

   record NotCondition(SpawnCondition spawnCondition) implements SpawnCondition {
      public static Codec<SpawnCondition.NotCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(SpawnCondition.CODEC.fieldOf("spawn_condition").forGetter(SpawnCondition.NotCondition::spawnCondition))
            .apply(inst, SpawnCondition.NotCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
         return !this.spawnCondition.test(mob, level, type, nbt);
      }

      @Override
      public boolean requiresNbtAccess() {
         return this.spawnCondition.requiresNbtAccess();
      }
   }

   record OrCondition(List<SpawnCondition> spawnConditions) implements SpawnCondition {
      public static Codec<SpawnCondition.OrCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(SpawnCondition.CODEC.listOf().fieldOf("spawn_conditions").forGetter(SpawnCondition.OrCondition::spawnConditions))
            .apply(inst, SpawnCondition.OrCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
         for (SpawnCondition ex : this.spawnConditions) {
            if (ex.test(mob, level, type, nbt)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean requiresNbtAccess() {
         boolean requiresNbt = false;

         for (SpawnCondition ex : this.spawnConditions) {
            requiresNbt |= ex.requiresNbtAccess();
         }

         return requiresNbt;
      }
   }

   record SpawnTypeCondition(Set<MobSpawnType> types) implements SpawnCondition {
      public static Codec<SpawnCondition.SpawnTypeCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               CommonToolkitCodecs.setOf(CommonToolkitCodecs.enumCodec(MobSpawnType.class)).fieldOf("spawn_types").forGetter(SpawnCondition.SpawnTypeCondition::types)
            )
            .apply(inst, SpawnCondition.SpawnTypeCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
         return this.types.contains(spawnType);
      }

      public static SpawnCondition.SpawnTypeCondition of(MobSpawnType... types) {
         return new SpawnCondition.SpawnTypeCondition(new LinkedHashSet<>(Arrays.asList(types)));
      }
   }

   record SurfaceTypeCondition(SurfaceType type) implements SpawnCondition {
      public static Codec<SpawnCondition.SurfaceTypeCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(SurfaceType.CODEC.fieldOf("surface_type").forGetter(SpawnCondition.SurfaceTypeCondition::type))
            .apply(inst, SpawnCondition.SurfaceTypeCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
         return this.type.test(level, mob.blockPosition());
      }
   }

   record XorCondition(SpawnCondition left, SpawnCondition right) implements SpawnCondition {
      public static Codec<SpawnCondition.XorCondition> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               SpawnCondition.CODEC.fieldOf("left").forGetter(SpawnCondition.XorCondition::left),
               SpawnCondition.CODEC.fieldOf("right").forGetter(SpawnCondition.XorCondition::right)
            )
            .apply(inst, SpawnCondition.XorCondition::new)
      );

      public Codec<? extends SpawnCondition> getCodec() {
         return CODEC;
      }

      @Override
      public boolean test(Mob mob, ServerLevelAccessor level, MobSpawnType type, CompoundTag nbt) {
         return this.left.test(mob, level, type, nbt) ^ this.right.test(mob, level, type, nbt);
      }

      @Override
      public boolean requiresNbtAccess() {
         return this.left.requiresNbtAccess() || this.right.requiresNbtAccess();
      }
   }
}
