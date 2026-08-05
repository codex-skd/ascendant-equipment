package com.skd.ascendantequipment.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.AffixLootEntry;
import com.skd.ascendantequipment.loot.AffixLootRegistry;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.util.NameHelper;
import com.skd.ascendantattributes.modifiers.EquipmentSlotCompat;
import com.skd.commontoolkit.codec.CodecMap;
import com.skd.commontoolkit.codec.CodecProvider;
import com.skd.commontoolkit.codec.CommonToolkitCodecs;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.dynreg.tag.DynamicHolderSet;
import com.skd.commontoolkit.json.ChancedEffectInstance;
import com.skd.commontoolkit.json.RandomAttributeModifier;
import com.skd.commontoolkit.systems.gear.GearSet;
import com.skd.commontoolkit.systems.gear.GearSetRegistry;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;

public interface EntityModifier extends CodecProvider<EntityModifier> {
   CodecMap<EntityModifier> CODEC = new CodecMap("Apothic Entity Modifier");

   void apply(Mob var1, GenContext var2);

   @Deprecated(forRemoval = true)
   default void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
   }

   static void initCodecs() {
      register("mob_effect", EntityModifier.EffectModifier.CODEC);
      register("attribute", EntityModifier.AttributeModifier.CODEC);
      register("gear_set", EntityModifier.GearSetModifier.CODEC);
      register("random_affix_item", EntityModifier.RandomAffixItemModifier.CODEC);
   }

   private static void register(String id, Codec<? extends EntityModifier> codec) {
      CODEC.register(AscendantEquipment.loc(id), codec);
   }

   record AttributeModifier(RandomAttributeModifier modifier) implements EntityModifier {
      public static Codec<EntityModifier.AttributeModifier> CODEC = RandomAttributeModifier.generatedCodec(AscendantEquipment.loc("entity_modifier"))
         .xmap(EntityModifier.AttributeModifier::new, EntityModifier.AttributeModifier::modifier);

      public Codec<? extends EntityModifier> getCodec() {
         return CODEC;
      }

      @Override
      public void apply(Mob mob, GenContext ctx) {
         this.modifier.apply(ctx.rand(), mob);
      }
   }

   record EffectModifier(ChancedEffectInstance effect) implements EntityModifier {
      public static Codec<EntityModifier.EffectModifier> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(ChancedEffectInstance.CONSTANT_CODEC.fieldOf("effect").forGetter(EntityModifier.EffectModifier::effect))
            .apply(inst, EntityModifier.EffectModifier::new)
      );

      public Codec<? extends EntityModifier> getCodec() {
         return CODEC;
      }

      @Override
      public void apply(Mob mob, GenContext ctx) {
         int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;
         mob.addEffect(this.effect.createDeterministic(duration));
      }
   }

   record GearSetModifier(DynamicHolderSet<GearSet> gearSets) implements EntityModifier {
      public static Codec<EntityModifier.GearSetModifier> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(DynamicHolderSet.codec(GearSetRegistry.INSTANCE).fieldOf("valid_gear_sets").forGetter(EntityModifier.GearSetModifier::gearSets))
            .apply(inst, EntityModifier.GearSetModifier::new)
      );

      public Codec<? extends EntityModifier> getCodec() {
         return CODEC;
      }

      @Override
      public void apply(Mob mob, GenContext ctx) {
         GearSet set = GearSetRegistry.INSTANCE.getRandomSet(ctx.rand(), ctx.luck(), this.gearSets);
         if (set != null) {
            set.apply(mob);
         }
      }
   }

   record RandomAffixItemModifier(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) implements EntityModifier {
      public static Codec<EntityModifier.RandomAffixItemModifier> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               CommonToolkitCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
               CommonToolkitCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries)
            )
            .apply(inst, EntityModifier.RandomAffixItemModifier::new)
      );

      public RandomAffixItemModifier() {
         this(Set.of(), Set.of());
      }

      public Codec<? extends EntityModifier> getCodec() {
         return CODEC;
      }

      @Override
      public void apply(Mob mob, GenContext ctx) {
         ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, ctx);
         if (!stack.isEmpty()) {
            NameHelper.setItemName(mob.getRandom(), stack);
            stack.set(AscEq.Components.FROM_MOB, true);
            LootCategory cat = LootCategory.forItem(stack);
            EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
               .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
               .findAny()
               .orElse(EquipmentSlot.MAINHAND);
            mob.setItemSlot(slot, stack);
            mob.setGuaranteedDrop(slot);
         }
      }
   }
}
