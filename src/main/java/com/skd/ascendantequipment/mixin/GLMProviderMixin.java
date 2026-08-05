package com.skd.ascendantequipment.mixin;

import java.util.LinkedHashMap;
import java.util.Map;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GlobalLootModifierProvider.class, remap = false)
public class GLMProviderMixin {
   @Shadow
   private final Map<String, WithConditions<IGlobalLootModifier>> toSerialize = new LinkedHashMap<>();
}
