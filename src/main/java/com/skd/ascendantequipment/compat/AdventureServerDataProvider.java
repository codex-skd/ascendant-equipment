package com.skd.ascendantequipment.compat;

import com.google.common.base.Predicates;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.types.Invader;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public class AdventureServerDataProvider implements IServerDataProvider<EntityAccessor> {
   public void appendServerData(CompoundTag tag, EntityAccessor access) {
      if (access.getEntity() instanceof LivingEntity living && living.getPersistentData().getBoolean(Invader.BOSS_KEY)) {
         tag.putBoolean(Invader.BOSS_KEY, true);
         tag.putString(Invader.RARITY_KEY, living.getPersistentData().getString(Invader.RARITY_KEY));
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            AttributeMap map = living.getAttributes();
            ListTag bossAttribs = new ListTag();
            BuiltInRegistries.ATTRIBUTE.holders().map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
               for (AttributeModifier modif : inst.getModifiers()) {
                  if (modif.id().getPath().startsWith("apothic_invader_")) {
                     bossAttribs.add(inst.save());
                     break;
                  }
               }
            });
            tag.put("apoth.modifiers", bossAttribs);
         }
      }
   }

   public ResourceLocation getUid() {
      return AscendantEquipment.loc("adventure");
   }
}
