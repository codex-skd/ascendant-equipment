package com.skd.ascendantequipment.socket.gem;

import com.skd.commontoolkit.dynreg.DynamicHolder;

public sealed interface GemView permits GemInstance, UnsocketedGem {
   DynamicHolder<Gem> gem();

   Purity purity();
}
