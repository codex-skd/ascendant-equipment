package com.skd.ascendantequipment.mobs.registries;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.types.Elite;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class EliteRegistry extends TieredDynamicRegistry<Elite> {
   public static final EliteRegistry INSTANCE = new EliteRegistry();

   public EliteRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("apothic_elites"), RegistrySerializer.simple(Elite.CODEC));
   }

   @Nullable
   public Elite getRandomItem(GenContext ctx, Entity target) {
      return this.getRandomItem(ctx, Constraints.eval(ctx), EliteRegistry.IEntityMatch.matches(target));
   }

   public interface IEntityMatch {
      HolderSet<EntityType<?>> getEntities();

      static <T extends EliteRegistry.IEntityMatch> Predicate<T> matches(EntityType<?> type) {
         return obj -> {
            HolderSet<EntityType<?>> types = obj.getEntities();
            return types == null || types.size() == 0 || types.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type));
         };
      }

      static <T extends EliteRegistry.IEntityMatch> Predicate<T> matches(Entity entity) {
         return matches(entity.getType());
      }
   }
}
