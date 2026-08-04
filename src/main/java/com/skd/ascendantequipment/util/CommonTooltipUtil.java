package com.skd.ascendantequipment.util;

import com.skd.ascendantattributes.AscendantAttributes;

import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.mobs.util.BossStats;
import com.skd.commontoolkit.dynreg.DynamicHolder;

import com.google.common.base.Predicates;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class CommonTooltipUtil {

    public static void appendBossData(Level level, LivingEntity entity, Consumer<Component> tooltip) {
        DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(Identifier.tryParse(entity.getPersistentData().getString("apoth.boss.rarity").orElse("")));
        if (rarity.isBound()) {
            tooltip.accept(Component.translatable("info.ascendant_equipment.boss", rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                tooltip.accept(CommonComponents.EMPTY);
                tooltip.accept(Component.translatable("info.ascendant_equipment.boss_modifiers").withStyle(ChatFormatting.GRAY));
                AttributeMap map = entity.getAttributes();
                BuiltInRegistries.ATTRIBUTE.listElements().<AttributeInstance>map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
                    for (AttributeModifier modif : inst.getModifiers()) {
                        if (modif.id().getPath().startsWith(BossStats.MODIFIER_BASE.getPath())) {
                            tooltip.accept(inst.getAttribute().value().toComponent(modif, AscendantAttributes.getTooltipFlag()));
                        }
                    }
                });
            }
        }
    }
}
