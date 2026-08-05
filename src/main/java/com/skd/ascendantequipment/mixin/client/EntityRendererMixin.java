package com.skd.ascendantequipment.mixin.client;

import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, remap = false)
public class EntityRendererMixin {
   @Inject(at = @At("TAIL"), method = "finalizeRenderState")
   private void apoth_finalizeRenderState(Entity entity, EntityRenderState state, CallbackInfo ci) {
      if (entity instanceof ItemEntity item) {
         ItemStack stack = item.getItem();
         DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
         if (rarity.isBound()) {
            state.shadowRadius = 0.0F;
         }
      }
   }
}
