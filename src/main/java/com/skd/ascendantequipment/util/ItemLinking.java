package com.skd.ascendantequipment.util;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.net.LinkItemToChatPayload;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.UUID;

public class ItemLinking {
    private static final Object2LongMap<UUID> LAST_LINK_TIMES = new Object2LongOpenHashMap<>();

    public static void sendHoveredItem() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Client.sendHoveredItem();
        }
    }

    public static boolean isOnCooldown(UUID id, long gameTime) {
        return LAST_LINK_TIMES.getOrDefault(id, -EquipmentConfig.itemLinkingCooldown) + EquipmentConfig.itemLinkingCooldown >= gameTime;
    }

    public static void startCooldown(UUID id, long gameTime) {
        LAST_LINK_TIMES.put(id, gameTime);
    }

    public static class Client {
        public static void sendHoveredItem() {
            if (Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen scn) {
                Slot slot = scn.getHoveredSlot();
                if (slot != null && slot.hasItem()) {
                    ClientPacketDistributor.sendToServer(new LinkItemToChatPayload(scn.getMenu().containerId, slot.index, slot.getItem().getItem()), new CustomPacketPayload[0]);
                }
            }
        }
    }
}
