package com.skd.ascendantequipment.compat;

import com.google.common.base.Predicates;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.util.BossStats;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance.Packed;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class AdventureServerDataProvider implements IServerDataProvider<EntityAccessor> {
   public void appendServerData(CompoundTag tag, EntityAccessor access) {
      if (access.getEntity() instanceof LivingEntity living && living.getPersistentData().getBooleanOr("apoth.boss", false)) {
         tag.putBoolean("apoth.boss", true);
         tag.putString("apoth.boss.rarity", living.getPersistentData().getStringOr("apoth.boss.rarity", ""));
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            AttributeMap map = living.getAttributes();
            ListTag bossAttribs = new ListTag();
            BuiltInRegistries.ATTRIBUTE.listElements().<AttributeInstance>map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
               for (AttributeModifier modif : inst.getModifiers()) {
                  if (modif.id().getPath().startsWith(BossStats.MODIFIER_BASE.getPath())) {
                     Packed packed = inst.pack();
                     bossAttribs.add((Tag)Packed.CODEC.encodeStart(NbtOps.INSTANCE, packed).getOrThrow());
                     break;
                  }
               }
            });
            tag.put("apoth.modifiers", bossAttribs);
         }
      }
   }

   public Identifier getUid() {
      return AscendantEquipment.loc("adventure");
   }
}
