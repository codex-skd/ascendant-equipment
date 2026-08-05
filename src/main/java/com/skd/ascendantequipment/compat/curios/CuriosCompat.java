package com.skd.ascendantequipment.compat.curios;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantattributes.api.AscendantAttributesObjects.BuiltInRegs;
import com.skd.ascendantattributes.compat.CurioEquipmentSlot;
import com.skd.ascendantattributes.modifiers.EntityEquipmentSlot;
import com.skd.ascendantattributes.modifiers.EntitySlotGroup;
import com.skd.commontoolkit.registry.DeferredHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;

public class CuriosCompat {
   private static DeferredHelper R = DeferredHelper.create("ascendant_equipment");
   public static final TagKey<Item> CHARM_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("curios", "charm"));
   public static final Holder<EntityEquipmentSlot> CHARM = R.customDH("charm", BuiltInRegs.ENTITY_EQUIPMENT_SLOT.key(), () -> new CurioEquipmentSlot("charm"));
   public static final EntitySlotGroup CHARM_G = (EntitySlotGroup)R.custom(
      "charm", BuiltInRegs.ENTITY_SLOT_GROUP.key(), new EntitySlotGroup(AscendantEquipment.loc("charm"), HolderSet.direct(new Holder[]{CHARM}))
   );
   public static final LootCategory CHARM_C = (LootCategory)R.custom(
      "charm", AscEq.BuiltInRegs.LOOT_CATEGORY.key(), new LootCategory(s -> s.is(CHARM_TAG), CHARM_G)
   );

   public static void register(IEventBus bus) {
      bus.register(R);
   }
}
