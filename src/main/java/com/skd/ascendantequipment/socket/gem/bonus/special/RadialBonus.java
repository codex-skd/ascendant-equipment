package com.skd.ascendantequipment.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.socket.SocketHelper;
import com.skd.ascendantequipment.socket.gem.GemClass;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.GemView;
import com.skd.ascendantequipment.socket.gem.Purity;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.ascendantequipment.util.RadialUtil;
import com.skd.commontoolkit.util.CachedObject;
import com.skd.commontoolkit.util.CachedObject.CachedObjectSource;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public class RadialBonus extends GemBonus {
   public static final Codec<RadialBonus> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(gemClass(), Purity.mapCodec(RadialUtil.RadialData.CODEC).fieldOf("values").forGetter(a -> a.values)).apply(inst, RadialBonus::new)
   );
   public static final ResourceLocation GEM_RADIAL_DATA_CACHED_OBJECT = AscendantEquipment.loc("gem_radial_data");
   protected final Map<Purity, RadialUtil.RadialData> values;

   public RadialBonus(GemClass gemClass, Map<Purity, RadialUtil.RadialData> values) {
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
      RadialUtil.RadialData data = this.values.get(gem.purity());
      return Component.translatable("affix.ascendant_equipment:breaker/effect/radial.desc", new Object[]{data.x(), data.y()}).withStyle(ChatFormatting.YELLOW);
   }

   public static void onBreak(BlockEvent.BreakEvent e) {
      Player player = e.getPlayer();
      RadialUtil.RadialData data = getRadialData(player.getMainHandItem());
      if (data != null) {
         RadialUtil.attemptRadialMining(e, data);
      }
   }

   @Nullable
   public static RadialUtil.RadialData getRadialData(ItemStack tool) {
      return (RadialUtil.RadialData)CachedObjectSource.getOrCreate(
         tool,
         GEM_RADIAL_DATA_CACHED_OBJECT,
         RadialBonus::getRadialDataImpl,
         CachedObject.hashComponents(AscEq.Components.SOCKETED_GEMS, AscEq.Components.SOCKETS)
      );
   }

   @Nullable
   private static RadialUtil.RadialData getRadialDataImpl(ItemStack tool) {
      if (tool.has(AscEq.Components.SOCKETED_GEMS)) {
         GemInstance inst = SocketHelper.getGems(tool).streamValidGems().filter(g -> g.getBonus().orElse(null) instanceof RadialBonus).findFirst().orElse(null);
         if (inst != null && inst.isValid()) {
            return ((RadialBonus)inst.getBonus().get()).values.get(inst.purity());
         }
      }

      return null;
   }

   public static RadialBonus.Builder builder() {
      return new RadialBonus.Builder();
   }

   public static class Builder extends GemBonus.Builder {
      private final Map<Purity, RadialUtil.RadialData> values = new LinkedHashMap<>();

      public RadialBonus.Builder value(Purity rarity, int x, int y, int xOff, int yOff) {
         RadialUtil.RadialData data = new RadialUtil.RadialData(x, y, xOff, yOff);
         this.values.put(rarity, data);
         return this;
      }

      public RadialBonus build(GemClass gClass) {
         return new RadialBonus(gClass, this.values);
      }
   }
}
