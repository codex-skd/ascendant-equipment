package com.skd.ascendantequipment.socket.gem.storage;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class GemCaseAnimationState {
   private static final int SWITCH_INTERVAL_MIN = 80;
   private static final int SWITCH_INTERVAL_MAX = 240;
   private static final int ANIMATION_DURATION = 40;
   private final RandomSource random;
   private final int[] slotPositions;
   private int ticksUntilNextSwitch;
   private int animationTicks;
   private int swappingIndex1 = -1;
   private int swappingIndex2 = -1;
   private boolean isAnimating = false;

   public GemCaseAnimationState(RandomSource random) {
      this.random = random;
      this.slotPositions = new int[16];
      int i = 0;

      while (i < 16) {
         this.slotPositions[i] = i++;
      }

      this.shuffleInitial();
      this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
   }

   private void shuffleInitial() {
      for (int i = 0; i < 16; i++) {
         int j = this.random.nextInt(16);
         int temp = this.slotPositions[i];
         this.slotPositions[i] = this.slotPositions[j];
         this.slotPositions[j] = temp;
      }
   }

   public void tick(int activeGemCount, boolean isPlayerNearby) {
      if (activeGemCount >= 4) {
         if (this.isAnimating) {
            this.animationTicks++;
            if (this.animationTicks >= 40) {
               this.completeSwap();
               this.isAnimating = false;
               this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
            }
         } else if (isPlayerNearby) {
            this.ticksUntilNextSwitch--;
            if (this.ticksUntilNextSwitch <= 0) {
               this.startRandomSwap(activeGemCount);
            }
         }
      }
   }

   private void startRandomSwap(int activeGemCount) {
      this.swappingIndex1 = this.random.nextInt(activeGemCount);
      this.swappingIndex2 = this.random.nextInt(activeGemCount);
      if (this.swappingIndex1 == this.swappingIndex2) {
         this.swappingIndex2 = (this.swappingIndex2 + 1) % activeGemCount;
      }

      this.isAnimating = true;
      this.animationTicks = 0;
   }

   private void completeSwap() {
      if (this.swappingIndex1 >= 0 && this.swappingIndex2 >= 0) {
         int temp = this.slotPositions[this.swappingIndex1];
         this.slotPositions[this.swappingIndex1] = this.slotPositions[this.swappingIndex2];
         this.slotPositions[this.swappingIndex2] = temp;
      }
   }

   public GemCaseAnimationState.PositionInfo getPosition(int gemIndex, float partialTicks) {
      int baseSlot = this.slotPositions[gemIndex];
      if (this.isAnimating && (gemIndex == this.swappingIndex1 || gemIndex == this.swappingIndex2)) {
         float progress = (this.animationTicks + partialTicks) / 40.0F;
         progress = Mth.clamp(progress, 0.0F, 1.0F);
         progress = smoothStep(progress);
         int targetSlot;
         if (gemIndex == this.swappingIndex1) {
            targetSlot = this.slotPositions[this.swappingIndex2];
         } else {
            targetSlot = this.slotPositions[this.swappingIndex1];
         }

         int baseX = baseSlot % 4;
         int baseZ = baseSlot / 4;
         int targetX = targetSlot % 4;
         int targetZ = targetSlot / 4;
         float offsetX = (targetX - baseX) * progress;
         float offsetZ = (targetZ - baseZ) * progress;
         return new GemCaseAnimationState.PositionInfo(baseSlot, offsetX, offsetZ);
      } else {
         return new GemCaseAnimationState.PositionInfo(baseSlot, 0.0F, 0.0F);
      }
   }

   private static float smoothStep(float t) {
      return t * t * (3.0F - 2.0F * t);
   }

   private int getRandomSwitchInterval() {
      return 80 + this.random.nextInt(160);
   }

   public record PositionInfo(int baseSlot, float offsetX, float offsetZ) {
   }
}
