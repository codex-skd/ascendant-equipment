package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantattributes.api.AscendantAttributesObjects.Attributes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class FrozenDropsBonus extends GemBonus {
   public static Codec<FrozenDropsBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(gemClass(), Purity.mapCodec(Codec.floatRange(0.0F, 100.0F)).fieldOf("values").forGetter(a -> a.values))
         .apply(inst, FrozenDropsBonus::new)
   );
   protected final Map<Purity, Float> values;

   public FrozenDropsBonus(GemClass gemClass, Map<Purity, Float> values) {
      super(gemClass);
      this.values = values;
   }

   public Codec<? extends GemBonus> getCodec() {
      return CODEC;
   }

   @Override
   public boolean supports(Purity purity) {
      return this.values.containsKey(purity);
   }

   @Override
   public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
      float value = this.values.get(inst.purity());
      Component dmgName = Component.translatable(((Attribute)Attributes.COLD_DAMAGE.value()).getDescriptionId()).withStyle(ChatFormatting.BLUE);
      return Component.translatable("bonus." + this.getTypeKey() + ".desc", new Object[]{dmgName, Affix.fmt(value * 100.0F)}).withStyle(ChatFormatting.YELLOW);
   }

   @Override
   public void modifyLoot(GemInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
      Entity entity = (Entity)ctx.getOptionalParameter(LootContextParams.THIS_ENTITY);
      if (entity instanceof Mob mob && ctx.getOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY) instanceof ServerPlayer) {
         float coldDmgTaken = (Float)mob.getData(AscEq.Attachments.COLD_DAMAGE_TAKEN);
         if (coldDmgTaken / mob.getMaxHealth() >= 0.55F) {
            increaseLootDrops(loot, this.values.get(inst.purity()), ctx.getRandom());
         }
      }
   }

   private static void increaseLootDrops(ObjectArrayList<ItemStack> loot, float percent, RandomSource rand) {
      for (int i = 0; i < loot.size(); i++) {
         ItemStack stack = (ItemStack)loot.get(i);
         int max = stack.getMaxStackSize();
         float target = stack.getCount() * percent;
         int newCount = (int)target;
         if (target - newCount > 0.001F && rand.nextFloat() <= target - newCount) {
            newCount++;
         }

         if (stack.getCount() < max) {
            int added = Math.min(newCount, max - stack.getCount());
            stack.grow(added);
            newCount -= added;
         }

         while (newCount > 0) {
            int added = Math.min(newCount, max);
            ItemStack newStack = stack.copyWithCount(added);
            loot.add(i, newStack);
            i++;
            newCount -= added;
         }
      }
   }
}
