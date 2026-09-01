package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantequipment.util.OmneticUtil;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticBonus extends GemBonus {
   public static final Codec<OmneticBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(gemClass(), Purity.mapCodec(OmneticUtil.OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, OmneticBonus::new)
   );
   protected final Map<Purity, OmneticUtil.OmneticData> values;

   public OmneticBonus(GemClass gemClass, Map<Purity, OmneticUtil.OmneticData> values) {
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
   public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
      return Component.translatable(
            "affix.ascendant_equipment:breaker/effect/omnetic.desc", new Object[]{Component.translatable("misc.ascendant_equipment." + this.values.get(gem.purity()).name())}
         )
         .withStyle(ChatFormatting.YELLOW);
   }

   public static void harvest(HarvestCheck e) {
      ItemStack stack = e.getEntity().getMainHandItem();
      if (!stack.isEmpty()) {
         GemInstance inst = SocketHelper.getGems(stack)
            .streamValidGems()
            .filter(g -> g.getBonus().orElse(null) instanceof OmneticBonus)
            .findFirst()
            .orElse(null);
         if (inst != null && inst.isValid()) {
            OmneticUtil.OmneticData data = ((OmneticBonus)inst.getBonus().get()).values.get(inst.purity());
            OmneticUtil.applyOmneticData(e, data);
         }
      }
   }

   public static void speed(BreakSpeed e) {
      ItemStack stack = e.getEntity().getMainHandItem();
      if (!stack.isEmpty()) {
         GemInstance inst = SocketHelper.getGems(stack)
            .streamValidGems()
            .filter(g -> g.getBonus().orElse(null) instanceof OmneticBonus)
            .findFirst()
            .orElse(null);
         if (inst != null && inst.isValid()) {
            OmneticUtil.OmneticData data = ((OmneticBonus)inst.getBonus().get()).values.get(inst.purity());
            OmneticUtil.applyOmneticData(e, data);
         }
      }
   }

   public static OmneticBonus.Builder builder() {
      return new OmneticBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private final Map<Purity, OmneticUtil.OmneticData> values = new LinkedHashMap<>();

      public OmneticBonus.Builder value(Purity rarity, String name, Item... items) {
         OmneticUtil.OmneticData data = new OmneticUtil.OmneticData(name, Arrays.stream(items).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
         this.values.put(rarity, data);
         return this;
      }

      public OmneticBonus build(GemClass gClass) {
         return new OmneticBonus(gClass, this.values);
      }
   }
}
