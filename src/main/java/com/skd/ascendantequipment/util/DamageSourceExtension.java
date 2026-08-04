package com.skd.ascendantequipment.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public interface DamageSourceExtension {
    void addTag(TagKey<DamageType> tag);

    void removeTag(TagKey<DamageType> tag);
}
