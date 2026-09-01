package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import java.util.Set;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AffixData(float chance, Set<LootRarity> rarities) {
   public static final AffixData DEFAULT = new AffixData(-1.0F, Set.of());
   public static final Codec<AffixData> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            Codec.FLOAT.fieldOf("affix_chance").forGetter(AffixData::chance),
            CommonToolkitCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(AffixData::rarities)
         )
         .apply(inst, AffixData::new)
   );

   @Nullable
   public EquipmentSlot applyTo(Mob mob, GenContext ctx, int enchLevel, boolean guaranteeDrop) {
      RandomSource rand = ctx.rand();
      if (rand.nextFloat() > this.chance()) {
         return null;
      }

      EquipmentSlot[] slots = getRandomSlots(rand);
      ItemStack temp = ItemStack.EMPTY;
      EquipmentSlot selectedSlot = null;

      for (EquipmentSlot slot : slots) {
         selectedSlot = slot;
         temp = mob.getItemBySlot(slot);
         if (!LootCategory.forItem(temp).isNone()) {
            break;
         }
      }

      if (LootCategory.forItem(temp).isNone()) {
         return null;
      }

      LootRarity rarity = LootRarity.random(ctx, this.rarities());
      if (mob.hasCustomName()) {
         mob.setCustomName(mob.getCustomName().plainCopy().withStyle(Style.EMPTY.withColor(rarity.color())));
      }

      Invader.modifyBossItem(temp, mob.getName(), ctx, rarity, enchLevel, mob.level().registryAccess());
      if (guaranteeDrop) {
         mob.setDropChance(selectedSlot, 2.0F);
      }

      return selectedSlot;
   }

   private static EquipmentSlot[] getRandomSlots(RandomSource rand) {
      EquipmentSlot[] slots = EquipmentSlot.values();

      for (int i = slots.length - 1; i > 0; i--) {
         int index = rand.nextInt(i + 1);
         EquipmentSlot v = slots[index];
         slots[index] = slots[i];
         slots[i] = v;
      }

      return slots;
   }
}
