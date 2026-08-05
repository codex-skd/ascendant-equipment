package com.skd.ascendantequipment.socket.gem;

import com.google.common.base.Preconditions;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantequipment.tiers.Constraints;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GemRegistry extends TieredDynamicRegistry<Gem> {
   public static final GemRegistry INSTANCE = new GemRegistry();

   public GemRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("gems"), RegistrySerializer.synced(Gem.CODEC));
   }

   protected void validateItem(Identifier key, Gem item) {
      super.validateItem(key, item);

      for (Purity p : Purity.values()) {
         if (p.isAtLeast(item.getMinPurity())) {
            boolean atLeastOne = false;

            for (GemBonus bonus : item.bonuses) {
               if (bonus.supports(p)) {
                  atLeastOne = true;
               }
            }

            Preconditions.checkArgument(
               atLeastOne,
               "No bonuses provided for supported purity %s. At least one bonus must be provided, or the minimum purity should be raised.",
               p.getName()
            );
         }
      }
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);
      if (type != ReloadType.INTEGRATED_CLIENT) {
         for (Gem gem : this.getValues()) {
            DynamicHolder<Gem> holder = this.holder(gem);

            for (ExtraGemBonusRegistry.ExtraGemBonus extraBonus : ExtraGemBonusRegistry.getBonusesFor(holder)) {
               for (GemBonus bonus : extraBonus.bonuses()) {
                  try {
                     gem.appendExtraBonus(bonus);
                  } catch (Exception ex) {
                     Identifier extraBonusKey = ExtraGemBonusRegistry.INSTANCE.getKey(extraBonus);
                     this.logger.warn("Failed to apply extra gem bonus for class {} to gem {}.", bonus.getGemClass().key(), holder.getId());
                     this.logger.warn("Exception while applying ExtraGemBonus %s: ".formatted(extraBonusKey), ex);
                  }
               }
            }
         }
      }
   }

   @Nullable
   public Gem getRandomItem(GenContext ctx) {
      return this.getRandomItem(ctx, Constraints.eval(ctx));
   }

   @Deprecated
   public static ItemStack createGemStack(Gem gem, Purity purity) {
      return gem.toStack(purity);
   }

   public static ItemStack createRandomGemStack(GenContext ctx) {
      Gem gem = INSTANCE.getRandomItem(ctx);
      if (gem == null) {
         return ItemStack.EMPTY;
      }

      Purity purity = Purity.random(ctx);
      return gem.toStack(purity);
   }
}
