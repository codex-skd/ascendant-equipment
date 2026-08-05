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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3x2fStack;

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

   public void render(GuiGraphicsExtractor gfx, Font font, int x, int y) {
      Identifier tex = AscendantEquipment.loc("textures/gui/buttons/" + ((TierAugment)this.data).tier().getSerializedName() + ".png");
      Matrix3x2fStack pose = gfx.pose();
      pose.pushMatrix();
      float scale = 0.3F;
      pose.scale(scale, scale);
      pose.translate(x / scale, y / scale);
      gfx.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0.0F, 0.0F, 30, 30, 30, 90);
      pose.popMatrix();
   }

   public static void bootstrap() {
   }
}
