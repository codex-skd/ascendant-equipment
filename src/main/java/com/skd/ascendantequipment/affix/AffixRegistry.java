package com.skd.ascendantequipment.affix;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.effect.CatalyzingAffix;
import com.skd.ascendantequipment.affix.effect.CleavingAffix;
import com.skd.ascendantequipment.affix.effect.DamageReductionAffix;
import com.skd.ascendantequipment.affix.effect.EnchantmentAffix;
import com.skd.ascendantequipment.affix.effect.EnlightenedAffix;
import com.skd.ascendantequipment.affix.effect.ExecutingAffix;
import com.skd.ascendantequipment.affix.effect.FestiveAffix;
import com.skd.ascendantequipment.affix.effect.MagicalArrowAffix;
import com.skd.ascendantequipment.affix.effect.MobEffectAffix;
import com.skd.ascendantequipment.affix.effect.MultiAttrAffix;
import com.skd.ascendantequipment.affix.effect.OmneticAffix;
import com.skd.ascendantequipment.affix.effect.PsychicAffix;
import com.skd.ascendantequipment.affix.effect.RadialAffix;
import com.skd.ascendantequipment.affix.effect.RetreatingAffix;
import com.skd.ascendantequipment.affix.effect.SpectralShotAffix;
import com.skd.ascendantequipment.affix.effect.StoneformingAffix;
import com.skd.ascendantequipment.affix.effect.TelepathicAffix;
import com.skd.ascendantequipment.affix.effect.ThunderstruckAffix;
import com.skd.ascendantequipment.client.AdventureModuleClient;
import com.skd.ascendantequipment.tiers.TieredDynamicRegistry;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.RegistrySerializer;
import com.skd.commontoolkit.dynreg.SubtypedSerializer;
import com.skd.commontoolkit.dynreg.DynamicRegistry.ReloadType;
import net.neoforged.fml.loading.FMLEnvironment;

public class AffixRegistry extends TieredDynamicRegistry<Affix> {
   public static final SubtypedSerializer<Affix> SERIALIZER = RegistrySerializer.<Affix>subtypedSynced("affixes")
      .register(AscendantEquipment.loc("attribute"), AttributeAffix.CODEC)
      .register(AscendantEquipment.loc("multi_attr"), MultiAttrAffix.CODEC)
      .register(AscendantEquipment.loc("mob_effect"), MobEffectAffix.CODEC)
      .register(AscendantEquipment.loc("damage_reduction"), DamageReductionAffix.CODEC)
      .register(AscendantEquipment.loc("catalyzing"), CatalyzingAffix.CODEC)
      .register(AscendantEquipment.loc("cleaving"), CleavingAffix.CODEC)
      .register(AscendantEquipment.loc("enlightened"), EnlightenedAffix.CODEC)
      .register(AscendantEquipment.loc("executing"), ExecutingAffix.CODEC)
      .register(AscendantEquipment.loc("festive"), FestiveAffix.CODEC)
      .register(AscendantEquipment.loc("magical"), MagicalArrowAffix.CODEC)
      .register(AscendantEquipment.loc("omnetic"), OmneticAffix.CODEC)
      .register(AscendantEquipment.loc("psychic"), PsychicAffix.CODEC)
      .register(AscendantEquipment.loc("radial"), RadialAffix.CODEC)
      .register(AscendantEquipment.loc("retreating"), RetreatingAffix.CODEC)
      .register(AscendantEquipment.loc("spectral"), SpectralShotAffix.CODEC)
      .register(AscendantEquipment.loc("telepathic"), TelepathicAffix.CODEC)
      .register(AscendantEquipment.loc("thunderstruck"), ThunderstruckAffix.CODEC)
      .register(AscendantEquipment.loc("enchantment"), EnchantmentAffix.CODEC)
      .register(AscendantEquipment.loc("stoneforming"), StoneformingAffix.CODEC);
   public static final AffixRegistry INSTANCE = new AffixRegistry();
   private Multimap<AffixType, DynamicHolder<Affix>> byType = ImmutableMultimap.of();

   public AffixRegistry() {
      super(AscendantEquipment.LOGGER, AscendantEquipment.loc("affixes"), SERIALIZER);
   }

   protected void beginReload(ReloadType type) {
      super.beginReload(type);
      this.byType = ImmutableMultimap.of();
   }

   protected void onReload(ReloadType type) {
      super.onReload(type);
      Builder<AffixType, DynamicHolder<Affix>> builder = ImmutableMultimap.builder();
      this.registry.values().forEach(a -> builder.put(a.definition().type(), this.holder(a)));
      this.byType = builder.build();
      if (!FMLEnvironment.production && FMLEnvironment.dist.isClient()) {
         AdventureModuleClient.checkAffixLangKeys();
      }

      if (type == ReloadType.SERVER) {
         this.validateAffixExclusiveSets();
      }
   }

   public Multimap<AffixType, DynamicHolder<Affix>> getTypeMap() {
      return this.byType;
   }

   protected void validateAffixExclusiveSets() {
      for (Affix a : this.registry.values()) {
         for (DynamicHolder<Affix> other : a.definition.exclusiveSet()) {
            if (!other.isBound()) {
               this.logger.error("The affix {} contains the unknown affix {} in its exclusive set!", a.id(), other.getId());
            }
         }
      }
   }
}
