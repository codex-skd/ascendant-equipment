package com.skd.ascendantequipment.client;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantequipment.tiers.augments.AttributeAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugment;
import com.skd.ascendantequipment.tiers.augments.TierAugmentRegistry;
import com.skd.ascendantattributes.client.ModifierSource;
import com.skd.ascendantattributes.client.ModifierSourceType;
import java.util.Comparator;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class TierAugmentModifierSource extends ModifierSource<TierAugment> {
   private static final Comparator<TierAugment> COMPARATOR = Comparator.<TierAugment, Integer>comparing(t -> t.tier().ordinal())
      .thenComparingInt(TierAugment::sortIndex);
   public static final ModifierSourceType<TierAugment> TIER_AUGMENT = ModifierSourceType.register(new ModifierSourceType<TierAugment>() {
      public void extract(LivingEntity entity, BiConsumer<AttributeModifier, ModifierSource<?>> map) {
         if (entity instanceof Player player) {
            WorldTier tier = WorldTier.getTier(player);

            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, TierAugment.Target.PLAYERS)) {
               if (aug instanceof AttributeAugment attr) {
                   AttributeModifier modif = attr.modifier().createDeterministic();
                  map.accept(modif, new TierAugmentModifierSource(aug));
               }
            }
         }
      }

      public int getPriority() {
         return 50;
      }
   });

   public TierAugmentModifierSource(TierAugment data) {
      super(TIER_AUGMENT, COMPARATOR, data);
   }

   public void render(GuiGraphics gfx, Font font, int x, int y) {
      ResourceLocation tex = AscendantEquipment.loc("textures/gui/buttons/" + ((TierAugment)this.data).tier().getSerializedName() + ".png");
      gfx.pose().pushPose();
      float scale = 9F / 30F;
      gfx.pose().scale(scale, scale, 1.0F);
      gfx.pose().translate(x / scale, y / scale, 0.0F);
      gfx.blit(tex, 0, 0, 0, 0, 30, 30, 30, 90);
      gfx.pose().popPose();
   }

   public static void bootstrap() {
   }
}
