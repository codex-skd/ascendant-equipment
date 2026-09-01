package com.skd.ascendantequipment.compat;

import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.mobs.types.Invader;
import com.skd.ascendantequipment.util.CommonTooltipUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class AdventureClientProvider implements IEntityComponentProvider {
   public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      if (accessor.getEntity() instanceof LivingEntity living && accessor.getServerData().getBoolean(Invader.BOSS_KEY)) {
         ListTag bossAttribs = accessor.getServerData().getList("apoth.modifiers", Tag.TAG_COMPOUND);
         AttributeMap map = living.getAttributes();

         for (Tag t : bossAttribs) {
            CompoundTag tag = (CompoundTag)t;
            Holder<Attribute> attrib = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.tryParse(tag.getString("id"))).get();
            map.getInstance(attrib).load(tag);
         }

         accessor.getServerData().remove("apoth.modifiers");
         living.getPersistentData().merge(accessor.getServerData());
         CommonTooltipUtil.appendBossData(living.level(), living, tooltip::add);
      }
   }

   public ResourceLocation getUid() {
      return AscendantEquipment.loc("adventure");
   }
}
