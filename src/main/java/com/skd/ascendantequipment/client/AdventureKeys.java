package com.skd.ascendantequipment.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.net.RadialStatePayload;
import com.skd.ascendantequipment.util.ItemLinking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class AdventureKeys {
   public static final Category CATEGORY = new Category(AscendantEquipment.loc("keys"));
   public static final KeyMapping TOGGLE_RADIAL = new KeyMapping(
      AscendantEquipment.langKey("key", "toggle_radial_mining"), KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM, 79, CATEGORY
   );
   public static final KeyMapping OPEN_WORLD_TIER_SELECT = new KeyMapping(
      AscendantEquipment.langKey("key", "open_world_tier_select"), KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM, 84, CATEGORY
   );
   public static final KeyMapping LINK_ITEM_TO_CHAT = new KeyMapping(
      AscendantEquipment.langKey("key", "link_item_to_chat"), KeyConflictContext.GUI, KeyModifier.SHIFT, Type.KEYSYM, 84, CATEGORY
   );
   public static final KeyMapping COMPARE_EQUIPMENT = new KeyMapping(
      AscendantEquipment.langKey("key", "compare_equipment"), KeyConflictContext.GUI, KeyModifier.NONE, Type.KEYSYM, 340, CATEGORY
   );

   @SubscribeEvent
   public static void handleKeys(Post e) {
      if (Minecraft.getInstance().player != null) {
         while (TOGGLE_RADIAL.consumeClick() && TOGGLE_RADIAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().gui.screen() == null) {
               ClientPacketDistributor.sendToServer(new RadialStatePayload(), new CustomPacketPayload[0]);
            }
         }

         while (OPEN_WORLD_TIER_SELECT.consumeClick() && OPEN_WORLD_TIER_SELECT.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().gui.screen() == null) {
               Minecraft.getInstance().gui.setScreen(new WorldTierSelectScreen());
            }
         }
      }
   }

   @SubscribeEvent
   public static void handleGuiKeys(Key e) {
      if (e.getAction() == 1 && LINK_ITEM_TO_CHAT.isActiveAndMatches(InputConstants.getKey(e.getKeyEvent()))) {
         ItemLinking.sendHoveredItem();
      }
   }
}
