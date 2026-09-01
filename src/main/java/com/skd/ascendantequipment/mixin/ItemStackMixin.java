package com.skd.ascendantequipment.mixin;

import com.google.common.base.Predicates;
import com.llamalad7.mixinextras.sugar.Local;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.util.IFestiveMarker;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class, priority = 500, remap = false)
public abstract class ItemStackMixin implements IFestiveMarker {
   @Unique
   private boolean apoth_isFestiveMarked = false;

   @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
   public void apoth_affixItemName(CallbackInfoReturnable<Component> cir) {
      ItemStack ths = (ItemStack)(Object)this;
      Component afxName = AffixHelper.getModifiedStackName(ths, (Component)cir.getReturnValue());
      if (afxName != null) {
         cir.setReturnValue(afxName);
      }

      DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(ths);
      if (rarity.isBound()) {
         Component recolored = ((Component)cir.getReturnValue()).copy().withStyle(s -> s.withColor(((LootRarity)rarity.get()).color()));
         cir.setReturnValue(recolored);
      }
   }

   @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
   public void apoth_useItemOnBlockPost(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir, @Local UseItemOnBlockEvent event) {
      if (!((InteractionResult)cir.getReturnValue()).consumesAction() && !event.isCanceled()) {
         ItemStack s = (ItemStack)(Object)this;
         InteractionResult socketRes = SocketHelper.getGems(s).onItemUse(ctx);
         if (socketRes != null) {
            cir.setReturnValue(socketRes);
            return;
         }

         InteractionResult afxRes = AffixHelper.streamAffixes(s).map(afx -> afx.onItemUse(ctx)).filter(Predicates.notNull()).findFirst().orElse(null);
         if (afxRes != null) {
            cir.setReturnValue(afxRes);
            return;
         }
      }
   }

   @Shadow
   public abstract boolean isEmpty();

   @Override
   public boolean isMarked() {
      return !this.isEmpty() && this.apoth_isFestiveMarked;
   }

   @Override
   public void setMarked(boolean marked) {
      this.apoth_isFestiveMarked = marked;
   }

   @Inject(method = "copy", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
   public void apoth_copyFestiveMarker(CallbackInfoReturnable<ItemStack> cir) {
      ItemStack copy = (ItemStack)cir.getReturnValue();
      if (this.isMarked()) {
         ((IFestiveMarker)(Object)copy).setMarked(true);
      }
   }

   @Inject(method = "inventoryTick", at = @At("HEAD"))
   public void apoth_tryTickMalice(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
      ItemStack ths = (ItemStack)(Object)this;
      if (!level.isClientSide() && ths.has(AscEq.Components.MALICE_MARKER) && entity instanceof Player player) {
         AffixHelper.applyMalice(player, ths);
         ths.remove(AscEq.Components.MALICE_MARKER);
      }
   }
}
