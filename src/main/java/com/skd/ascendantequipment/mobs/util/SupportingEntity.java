package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.commontoolkit.json.NBTAdapter;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import org.jspecify.annotations.Nullable;

public class SupportingEntity {
   public static Codec<SupportingEntity> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(t -> t.entity),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(t -> Optional.ofNullable(t.nbt)),
            Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(t -> t.x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(t -> t.y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(t -> t.z)
         )
         .apply(inst, SupportingEntity::new)
   );
   public final EntityType<?> entity;
   protected final CompoundTag nbt;
   protected final double x;
   protected final double y;
   protected final double z;

   public SupportingEntity(EntityType<?> entity, Optional<CompoundTag> nbt, double x, double y, double z) {
      this.entity = entity;
      this.nbt = nbt.orElse(null);
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public @Nullable Mob create(Level level, double x, double y, double z) {
      Mob ent = (Mob)this.entity.create(level, EntitySpawnReason.EVENT);
      if (ent != null && this.nbt != null) {
         ScopedCollector reporter = new ScopedCollector(ent.problemPath(), AscendantEquipment.LOGGER);

         try {
            ent.load(TagValueInput.create(reporter, level.registryAccess(), this.nbt));
         } catch (Throwable var13) {
            try {
               reporter.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }

            throw var13;
         }

         reporter.close();
      }

      if (ent != null) {
         ent.setPos(this.x + x, this.y + y, this.z + z);
      }

      return ent;
   }

   public static SupportingEntity.Builder builder() {
      return new SupportingEntity.Builder();
   }

   public static class Builder {
      private EntityType<? extends Mob> entity;
      private CompoundTag nbt;
      private double x = 0.0;
      private double y = 0.0;
      private double z = 0.0;

      public SupportingEntity.Builder entity(EntityType<? extends Mob> entity) {
         this.entity = entity;
         return this;
      }

      public SupportingEntity.Builder nbt(CompoundTag nbt) {
         this.nbt = nbt;
         return this;
      }

      public SupportingEntity.Builder nbt(Consumer<CompoundTag> nbt) {
         CompoundTag current = this.nbt == null ? new CompoundTag() : this.nbt;
         nbt.accept(current);
         this.nbt = current;
         return this;
      }

      public SupportingEntity.Builder x(double x) {
         this.x = x;
         return this;
      }

      public SupportingEntity.Builder y(double y) {
         this.y = y;
         return this;
      }

      public SupportingEntity.Builder z(double z) {
         this.z = z;
         return this;
      }

      public SupportingEntity.Builder position(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
         return this;
      }

      public SupportingEntity build() {
         if (this.entity == null) {
            throw new IllegalStateException("Entity type must be set");
         } else {
            return new SupportingEntity(this.entity, Optional.ofNullable(this.nbt), this.x, this.y, this.z);
         }
      }
   }
}
