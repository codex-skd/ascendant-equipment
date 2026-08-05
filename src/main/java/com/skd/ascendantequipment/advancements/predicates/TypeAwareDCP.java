package com.skd.ascendantequipment.advancements.predicates;

import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;

public interface TypeAwareDCP<T extends DataComponentPredicate> extends DataComponentPredicate {
   Type<T> type();
}
