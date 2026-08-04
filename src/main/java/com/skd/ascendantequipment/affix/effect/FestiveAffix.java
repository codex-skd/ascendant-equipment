package com.skd.ascendantequipment.affix.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixBuilder;
import com.skd.ascendantequipment.affix.AffixDefinition;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.util.IFestiveMarker;
import com.skd.commontoolkit.util.StepFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.Item;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class FestiveAffix extends Affix {
   public static Codec<FestiveAffix> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(FestiveAffix.FestiveData.CODEC).fieldOf("values").forGetter(a -> a.values)
         )
         .apply(inst, FestiveAffix::new)
   );
   protected final Set<LootCategory> categories;
   protected final Map<LootRarity, FestiveAffix.FestiveData> values;

   public FestiveAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, FestiveAffix.FestiveData> values) {
      super(def);
      this.categories = categories;
      this.values = values;
   }

   @Override
   public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
      return Component.translatable("affix." + this.id() + ".desc", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), inst.level()))});
   }

   @Override
   public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
      MutableComponent comp = this.getDescription(inst, ctx);
      Component minComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 0.0F))});
      Component maxComp = Component.translatable("%s%%", new Object[]{fmt(100.0F * this.getTrueLevel(inst.getRarity(), 1.0F))});
      return comp.append(valueBounds(minComp, maxComp));
   }

   @Override
   public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
      return this.categories.contains(cat) && this.values.containsKey(rarity);
   }

   private float getTrueLevel(LootRarity rarity, float level) {
      return this.values.get(rarity).chance().get(level);
   }

   public static void markEquipment(LivingDeathEvent e) {
      if (!(e.getEntity() instanceof Player) && !e.getEntity().getPersistentData().getBooleanOr("apoth.no_pinata", false)) {
         ResourceHandler<ItemResource> inv = (ResourceHandler<ItemResource>)e.getEntity().getCapability(Item.ENTITY);
         if (inv != null) {
            for (int i = 0; i < inv.size(); i++) {
               ItemResource res = (ItemResource)inv.getResource(i);
               int amount = inv.getAmountAsInt(i);
               if (!res.isEmpty() && amount > 0) {
                  ItemStack stack = res.toStack(amount);
                  ((IFestiveMarker)(Object)stack).setMarked(true);
               }
            }
         }

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack i = e.getEntity().getItemBySlot(slot);
            if (!i.isEmpty()) {
               ((IFestiveMarker)(Object)i).setMarked(true);
            }
         }
      }
   }

   @Override
   public void modifyEntityLoot(AffixInstance inst, LivingDropsEvent e) {
      LivingEntity dead = e.getEntity();
      if (!(dead instanceof Player) && !dead.getPersistentData().getBooleanOr("apoth.no_pinata", false)) {
         if (e.getSource().getEntity() instanceof Player player
            && !e.getDrops().isEmpty()
            && inst != null
            && inst.isValid()
            && player.level().getRandom().nextFloat() < this.getTrueLevel((LootRarity)inst.rarity().get(), inst.level())) {
            player.level()
               .playSound(
                  null,
                  dead.getX(),
                  dead.getY(),
                  dead.getZ(),
                  SoundEvents.GENERIC_EXPLODE,
                  SoundSource.BLOCKS,
                  4.0F,
                  (1.0F + (player.level().getRandom().nextFloat() - player.level().getRandom().nextFloat()) * 0.2F) * 0.7F
               );
            ((ServerLevel)player.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0, 0.0, 0.0, 0.0);

            for (ItemEntity item : new ArrayList<>(e.getDrops())) {
               if (!((IFestiveMarker)(Object)item.getItem()).isMarked()) {
                  int rolls = this.values.get(inst.rarity().get()).rolls();

                  for (int i = 0; i < rolls; i++) {
                     e.getDrops().add(new ItemEntity(player.level(), item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
                  }
               }
            }

            for (ItemEntity item : e.getDrops()) {
               item.setPos(dead.getX(), dead.getY(), dead.getZ());
               item.setDeltaMovement(
                  -0.3 + dead.level().getRandom().nextDouble() * 0.6,
                  0.3 + dead.level().getRandom().nextDouble() * 0.3,
                  -0.3 + dead.level().getRandom().nextDouble() * 0.6
               );
            }
         }
      }
   }

   public static void removeMarker(LivingDropsEvent e) {
      e.getDrops().stream().forEach(ent -> {
         ItemStack s = ent.getItem();
         ((IFestiveMarker)(Object)s).setMarked(false);
         ent.setItem(s);
      });
   }

   public Codec<? extends Affix> getCodec() {
      return CODEC;
   }

   @Override
   public boolean isLevelIndependent(AffixInstance inst) {
      return this.values.get(inst.getRarity()).chance.isConstant();
   }

   public static FestiveAffix.Builder builder() {
      return new FestiveAffix.Builder();
   }

   public static class Builder extends AffixBuilder<FestiveAffix.Builder> {
      protected final Set<LootCategory> categories = new LinkedHashSet<>();
      protected final Map<LootRarity, FestiveAffix.FestiveData> values = new HashMap<>();

      public FestiveAffix.Builder categories(LootCategory... cats) {
         for (LootCategory cat : cats) {
            this.categories.add(cat);
         }

         return this;
      }

      public FestiveAffix.Builder value(LootRarity rarity, StepFunction chance, int rolls) {
         this.values.put(rarity, new FestiveAffix.FestiveData(chance, rolls));
         return this;
      }

      public FestiveAffix build() {
         return new FestiveAffix(this.definition, this.categories, this.values);
      }
   }

   public record FestiveData(StepFunction chance, int rolls) {
      public static final Codec<FestiveAffix.FestiveData> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               StepFunction.CODEC.fieldOf("chance").forGetter(FestiveAffix.FestiveData::chance),
               Codec.INT.fieldOf("rolls").forGetter(FestiveAffix.FestiveData::rolls)
            )
            .apply(inst, FestiveAffix.FestiveData::new)
      );
   }
}
