package com.skd.ascendantequipment.affix;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public interface AttributeProvidingAffix {
   void gatherModifierTooltips(AffixInstance var1, AttributeTooltipContext var2, Consumer<Component> var3);

   void skipModifierIds(AffixInstance var1, AttributeTooltipContext var2, Consumer<ResourceLocation> var3);
}
