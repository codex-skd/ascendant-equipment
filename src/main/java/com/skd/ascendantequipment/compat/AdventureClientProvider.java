package com.skd.ascendantequipment.compat;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.util.CommonTooltipUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance.Packed;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AdventureClientProvider implements IEntityComponentProvider {
   public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      if (accessor.getEntity() instanceof LivingEntity living && accessor.getServerData().getBooleanOr("apoth.boss", false)) {
         ListTag bossAttribs = accessor.getServerData().getListOrEmpty("apoth.modifiers");
         AttributeMap map = living.getAttributes();

         for (Tag t : bossAttribs) {
            CompoundTag tag = (CompoundTag)t;
            Packed packed = (Packed)Packed.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
            AttributeInstance inst = map.getInstance(packed.attribute());
            if (inst != null) {
               inst.apply(packed);
            }
         }

         accessor.getServerData().remove("apoth.modifiers");
         living.getPersistentData().merge(accessor.getServerData());
         CommonTooltipUtil.appendBossData(living.level(), living, tooltip::add);
      }
   }

   public Identifier getUid() {
      return AscendantEquipment.loc("adventure");
   }
}
