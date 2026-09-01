package com.skd.ascendantequipment.socket.gem;

import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public record UnsocketedGem(DynamicHolder<Gem> gem, Purity purity) implements GemView {
   public static UnsocketedGem of(ItemStack gemStack) {
      DynamicHolder<Gem> gem = GemItem.getGem(gemStack);
      Purity purity = GemItem.getPurity(gemStack);
      if (gem.isBound()) {
         purity = Purity.max(((Gem)gem.get()).getMinPurity(), purity);
      }

      return new UnsocketedGem(gem, purity);
   }

   public boolean isValid() {
      return this.gem.isBound();
   }

   public boolean isPerfect() {
      return this.purity == Purity.PERFECT;
   }

   public boolean canApplyTo(ItemStack stack) {
      return ((Gem)this.gem.get()).canApplyTo(stack, this.purity);
   }

   public void addInformation(Consumer<Component> list, AttributeTooltipContext ctx) {
      ((Gem)this.gem().get()).addInformation(this, list, ctx);
   }

   @Override
   public final boolean equals(Object arg0) {
      if (this == arg0) {
         return true;
      } else if (arg0 instanceof UnsocketedGem other) {
         return !this.isValid() ? !other.isValid() : other.isValid() && other.gem.equals(this.gem) && other.purity == this.purity;
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return this.isValid() ? Objects.hash(this.gem, this.purity) : -1;
   }
}
