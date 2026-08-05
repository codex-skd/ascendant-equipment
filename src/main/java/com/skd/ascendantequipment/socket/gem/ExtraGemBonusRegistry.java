package com.skd.ascendantequipment.socket.gem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.gem.bonus.GemBonus;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.DynamicRegistry;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExtraGemBonusRegistry extends DynamicRegistry<ExtraGemBonusRegistry.ExtraGemBonus> {
   public static final ExtraGemBonusRegistry INSTANCE = new ExtraGemBonusRegistry();
   protected Multimap<DynamicHolder<Gem>, ExtraGemBonusRegistry.ExtraGemBonus> extraBonuses = HashMultimap.create();

   public ExtraGemBonusRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("extra_gem_bonuses"), RegistrySerializer.synced(ExtraGemBonusRegistry.ExtraGemBonus.CODEC));
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.extraBonuses = HashMultimap.create();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);

      for (ExtraGemBonusRegistry.ExtraGemBonus extraBonus : this.getValues()) {
         this.extraBonuses.put(extraBonus.gem, extraBonus);
      }
   }

   public static Collection<ExtraGemBonusRegistry.ExtraGemBonus> getBonusesFor(DynamicHolder<Gem> gem) {
      return INSTANCE.extraBonuses.get(gem);
   }

   public record ExtraGemBonus(DynamicHolder<Gem> gem, List<GemBonus> bonuses) {
      public static final Codec<ExtraGemBonusRegistry.ExtraGemBonus> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               GemRegistry.INSTANCE.holderCodec().fieldOf("gem").forGetter(ExtraGemBonusRegistry.ExtraGemBonus::gem),
               GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(ExtraGemBonusRegistry.ExtraGemBonus::bonuses)
            )
            .apply(inst, ExtraGemBonusRegistry.ExtraGemBonus::new)
      );

      public static ExtraGemBonusRegistry.ExtraGemBonus.Builder builder(DynamicHolder<Gem> gem) {
         return new ExtraGemBonusRegistry.ExtraGemBonus.Builder(gem);
      }

      public static class Builder {
         protected final DynamicHolder<Gem> gem;
         protected List<GemBonus> bonuses = new ArrayList<>();

         public Builder(DynamicHolder<Gem> gem) {
            this.gem = gem;
         }

         public ExtraGemBonusRegistry.ExtraGemBonus.Builder bonus(LootCategory cat, GemBonus.Builder builder) {
            return this.bonus(new GemClass(cat), builder);
         }

         public ExtraGemBonusRegistry.ExtraGemBonus.Builder bonus(GemClass gClass, GemBonus.Builder builder) {
            this.bonuses.add(builder.build(gClass));
            return this;
         }

         public ExtraGemBonusRegistry.ExtraGemBonus.Builder bonus(GemBonus bonus) {
            this.bonuses.add(bonus);
            return this;
         }

         public ExtraGemBonusRegistry.ExtraGemBonus build() {
            return new ExtraGemBonusRegistry.ExtraGemBonus(this.gem, this.bonuses);
         }
      }
   }
}
