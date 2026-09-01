package com.skd.ascendantequipment.affix.salvaging;

import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SalvageItem extends Item {

    protected final DynamicHolder<LootRarity> rarity;

    public SalvageItem(DynamicHolder<LootRarity> rarity, Properties pProperties) {
        super(pProperties);
        this.rarity = rarity;
    }

    @Override
    public Component getName(ItemStack pStack) {
        if (!this.rarity.isBound()) {
            return super.getName(pStack);
        }
        return Component.translatable(this.getDescriptionId(pStack)).withStyle(Style.EMPTY.withColor(this.rarity.get().color()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag tooltipFlag) {
        if (this.rarity.isBound()) {
            list.add(Component.translatable("info.ascendant_equipment.rarity_material", this.rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        }
    }

}
