package com.skd.ascendantequipment.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Entity.class, remap = false)
public interface EntityInvoker {
   @Invoker
   void callReadAdditionalSaveData(ValueInput var1);
}
