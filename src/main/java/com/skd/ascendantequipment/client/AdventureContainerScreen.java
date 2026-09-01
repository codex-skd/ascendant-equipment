package com.skd.ascendantequipment.client;

import com.skd.commontoolkit.screen.CommonToolkitContainerScreen;
import com.skd.commontoolkit.util.DrawsOnLeft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AdventureContainerScreen<T extends AbstractContainerMenu> extends CommonToolkitContainerScreen<T> implements DrawsOnLeft {

    public AdventureContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {}

    @Override
    public int getSlotColor(int index) {
        return 0x40FFFFFF;
    }

}
