package com.skd.ascendantequipment.compat;

import com.skd.vellumli.api.VellumliAPI;
import com.skd.vellumli.common.item.VellumliDataComponents;
import com.skd.vellumli.common.item.VellumliItems;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class VellumliCompat {
   public static ItemStack createGuideBookStack() {
      ItemStack stack = new ItemStack(VellumliItems.BOOK.value());
      stack.set(VellumliDataComponents.BOOK, Identifier.fromNamespaceAndPath("ascendant_equipment", "apoth_chronicle"));
      return stack;
   }

   public static void register() {
      VellumliAPI.IVellumliAPI api = VellumliAPI.get();
      if (!api.isStub()) {
         api.setConfigFlag("ascendant_equipment:enchanting", ModList.get().isLoaded("ascendant_enchanting"));
         api.setConfigFlag("ascendant_equipment:adventure", true);
         api.setConfigFlag("ascendant_equipment:spawner", ModList.get().isLoaded("ascendant_spawners"));
         api.setConfigFlag("ascendant_equipment:garden", true);
         api.setConfigFlag("ascendant_equipment:potion", ModList.get().isLoaded("ascendant_attributes"));
         api.setConfigFlag("ascendant_equipment:village", false);
         api.setConfigFlag("ascendant_equipment:wstloaded", ModList.get().isLoaded("wstweaks"));
         api.setConfigFlag("ascendant_equipment:curiosloaded", ModList.get().isLoaded("curios"));
      }
   }
}
