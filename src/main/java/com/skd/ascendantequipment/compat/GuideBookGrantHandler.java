package com.skd.ascendantequipment.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// Gives every player the Ascendant Equipment guide book (via Vellumli) the first time they ever
// log in after this feature was added — tracked per-player in persistent data so it survives
// death/respawn and reconnects, but only fires once: if the player loses the book afterward, they
// have to craft a replacement (book + gold ingot) instead of getting another free one.
public class GuideBookGrantHandler {
    private static final String GIVEN_TAG = "ascendant_equipment_guide_book_given";

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !ModList.get().isLoaded("vellumli")) {
            return;
        }

        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(GIVEN_TAG)) {
            return;
        }
        persisted.putBoolean(GIVEN_TAG, true);
        root.put(Player.PERSISTED_NBT_TAG, persisted);

        ItemStack stack = VellumliCompat.createGuideBookStack();
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
