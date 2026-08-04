package com.skd.ascendantequipment.affix;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public interface AttributeProvidingAffix {
   void gatherModifierTooltips(AffixInstance var1, AttributeTooltipContext var2, Consumer<Component> var3);

   void skipModifierIds(AffixInstance var1, AttributeTooltipContext var2, Consumer<Identifier> var3);
}
