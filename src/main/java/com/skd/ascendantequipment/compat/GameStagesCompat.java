package com.skd.ascendantequipment.compat;

import com.skd.ascendantequipment.AscendantEquipment;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;

public class GameStagesCompat {
   public static boolean hasStage(Player player, Set<String> stages) {
      return !AscendantEquipment.STAGES_LOADED || stages.isEmpty() || GameStagesCompat.Inner.hasStage(player, stages);
   }

   public static Set<String> getStages(Player player) {
      return AscendantEquipment.STAGES_LOADED ? GameStagesCompat.Inner.getStages(player) : Set.of();
   }

   public interface IStaged {
      @Nullable
      Set<String> getStages();

      static <T extends GameStagesCompat.IStaged> Predicate<T> matches(Player player) {
         return obj -> GameStagesCompat.hasStage(player, obj.getStages());
      }
   }

   private static class Inner {
      private static boolean hasStage(Player player, Set<String> stages) {
         return false;
      }

      private static Set<String> getStages(Player player) {
         return Set.of();
      }
   }
}
