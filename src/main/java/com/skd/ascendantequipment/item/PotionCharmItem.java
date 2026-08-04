package com.skd.ascendantequipment.item;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.commontoolkit.tabs.ITabFiller;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.Nullable;

public class PotionCharmItem extends Item implements ITabFiller {
   public PotionCharmItem(Properties props) {
      super(props.stacksTo(1).durability(192).setNoCombineRepair().component(AscEq.Components.CHARM_ENABLED, false));
   }

   public ItemStack getDefaultInstance() {
      return PotionContents.createItemStack(this, Potions.LONG_INVISIBILITY);
   }

   public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
      if (hasEffect(stack) && (!EquipmentConfig.charmsInCuriosOnly || slot == null)) {
         if ((Boolean)stack.get(AscEq.Components.CHARM_ENABLED) && entity instanceof ServerPlayer player) {
            MobEffectInstance contained = getEffect(stack);
            MobEffectInstance active = player.getEffect(contained.getEffect());
            if (active == null || active.getDuration() < getCriticalDuration(active.getEffect())) {
               int durationOffset = getCriticalDuration(contained.getEffect());
               if (contained.getEffect() == MobEffects.REGENERATION) {
                  durationOffset += 50 >> contained.getAmplifier();
               }

               MobEffectInstance newEffect = new MobEffectInstance(
                  contained.getEffect(), (int)Math.ceil(contained.getDuration() / 24.0) + durationOffset, contained.getAmplifier(), false, false
               );
               player.addEffect(newEffect);
               int damage = contained.getEffect() == MobEffects.REGENERATION ? 2 : 1;
               if (slot == EquipmentSlot.MAINHAND) {
                  stack.hurtAndBreak(damage, player, EquipmentSlot.MAINHAND);
               } else {
                  stack.hurtAndBreak(damage, level, player, item -> {});
               }
            }
         }
      }
   }

   private static int getCriticalDuration(Holder<MobEffect> effect) {
      return effect.is(AscEq.Tags.EXTENDED_CHARM_DURATION) ? 210 : 5;
   }

   public boolean isFoil(ItemStack stack) {
      return (Boolean)stack.get(AscEq.Components.CHARM_ENABLED);
   }

   public InteractionResult use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!world.isClientSide()) {
         stack.set(AscEq.Components.CHARM_ENABLED, !(Boolean)stack.get(AscEq.Components.CHARM_ENABLED));
      } else if (!(Boolean)stack.get(AscEq.Components.CHARM_ENABLED)) {
         world.playSound(player, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.3F);
      }

      return InteractionResult.SUCCESS;
   }

   public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
      return false;
   }

   public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
      return false;
   }

   public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      if (EquipmentConfig.charmsInCuriosOnly) {
         tooltip.accept(Component.translatable(this.getDescriptionId() + ".curios_only").withStyle(ChatFormatting.RED));
      }

      if (hasEffect(stack)) {
         MobEffectInstance inst = getEffect(stack);
         MutableComponent potionCmp = Component.translatable(inst.getDescriptionId());
         if (inst.getAmplifier() > 0) {
            potionCmp = Component.translatable("potion.withAmplifier", new Object[]{potionCmp, Component.translatable("potion.potency." + inst.getAmplifier())});
         }

         MobEffect effect = (MobEffect)inst.getEffect().value();
         potionCmp.withStyle(effect.getCategory().getTooltipFormatting());
         tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc", new Object[]{potionCmp}).withStyle(ChatFormatting.GRAY));
         boolean enabled = (Boolean)stack.get(AscEq.Components.CHARM_ENABLED);
         MutableComponent enabledCmp = Component.translatable(this.getDescriptionId() + (enabled ? ".enabled" : ".disabled"));
         enabledCmp.withStyle(enabled ? ChatFormatting.BLUE : ChatFormatting.RED);
         if (inst.getDuration() > 20) {
            potionCmp = Component.translatable("potion.withDuration", new Object[]{potionCmp, MobEffectUtil.formatDuration(inst, 1.0F, ctx.tickRate())});
         }

         potionCmp.withStyle(effect.getCategory().getTooltipFormatting());
         tooltip.accept(Component.translatable(this.getDescriptionId() + ".desc3", new Object[]{potionCmp}).withStyle(ChatFormatting.GRAY));
      }
   }

   public int getMaxDamage(ItemStack stack) {
      return !hasEffect(stack) ? 1 : 192;
   }

   public Component getName(ItemStack stack) {
      if (!hasEffect(stack)) {
         return Component.translatable("item.ascendant_equipment.potion_charm_broke");
      }

      MobEffectInstance effect = getEffect(stack);
      MutableComponent potionCmp = Component.translatable(effect.getDescriptionId());
      if (effect.getAmplifier() > 0) {
         potionCmp = Component.translatable("potion.withAmplifier", new Object[]{potionCmp, Component.translatable("potion.potency." + effect.getAmplifier())});
      }

      return Component.translatable("item.ascendant_equipment.potion_charm", new Object[]{potionCmp});
   }

   public static boolean hasEffect(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      return contents.getAllEffects().iterator().hasNext();
   }

   public static MobEffectInstance getEffect(ItemStack stack) {
      PotionContents contents = (PotionContents)stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      return (MobEffectInstance)contents.getAllEffects().iterator().next();
   }

   public void fillItemCategory(CreativeModeTab group, BuildCreativeModeTabContentsEvent out) {
      BuiltInRegistries.POTION
         .listElements()
         .filter(PotionCharmItem::isValidPotion)
         .forEach(potion -> out.accept(PotionContents.createItemStack(this, potion)));
   }

   public String getCreatorModId(Provider registries, ItemStack itemStack) {
      Identifier potionKey = ((PotionContents)itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY))
         .potion()
         .map(IHolderExtension::getKey)
         .<Identifier>map(ResourceKey::identifier)
         .orElse(null);
      return potionKey != null ? potionKey.getNamespace() : BuiltInRegistries.ITEM.getKey(this).getNamespace();
   }

   public static boolean isValidPotion(Holder<Potion> holder) {
      Potion potion = (Potion)holder.value();
      if (potion.getEffects().size() != 1) {
         return false;
      }

      MobEffect effect = (MobEffect)((MobEffectInstance)potion.getEffects().get(0)).getEffect().value();
      return effect.isInstantaneous() ? false : !holder.is(AscEq.Tags.POTION_CHARM_BLACKLIST);
   }
}
