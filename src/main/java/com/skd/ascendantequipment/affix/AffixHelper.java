package com.skd.ascendantequipment.affix;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.loot.LootRarity;
import com.skd.ascendantequipment.loot.RarityRegistry;
import com.skd.ascendantequipment.tiers.WorldTier;
import com.skd.ascendantattributes.AscendantAttributes;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.util.CachedObject;
import com.skd.commontoolkit.util.CachedObject.CachedObjectSource;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AffixHelper {
   public static final Identifier AFFIX_CACHED_OBJECT = AscendantEquipment.loc("affixes");
   public static final String SOURCE_WEAPON = "apoth.source_weapon";

   public static void applyAffix(ItemStack stack, AffixInstance inst) {
      ItemAffixes.Builder builder = ((ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY)).toBuilder();
      builder.put(inst.affix(), inst.level());
      setAffixes(stack, builder.build());
   }

   public static void setAffixes(ItemStack stack, ItemAffixes affixes) {
      stack.set(AscEq.Components.AFFIXES, affixes);
   }

   public static void setName(ItemStack stack, Component name) {
      stack.set(AscEq.Components.AFFIX_NAME, name.copy());
   }

   @Nullable
   public static Component getName(ItemStack stack) {
      return (Component)stack.get(AscEq.Components.AFFIX_NAME);
   }

   @Nullable
   public static Component getModifiedStackName(ItemStack stack, Component currentName) {
      if (stack.has(AscEq.Components.AFFIX_NAME)) {
         if (FMLEnvironment.getDist().isClient()) {
            Component hidden = AffixHelper.ClientAccess.getHiddenAffixName(currentName);
            if (hidden != null) {
               return hidden;
            }
         }

         try {
            Component component = getName(stack);
            TranslatableContents contents = copyContents(component);
            int idx = "misc.ascendant_equipment.affix_name.four".equals(contents.getKey()) ? 2 : 1;
            contents.getArgs()[idx] = currentName;
            MutableComponent ret = MutableComponent.create(contents).withStyle(component.getStyle());

            for (Component sibling : component.getSiblings()) {
               ret.append(sibling);
            }

            return ret;
         } catch (Exception exception) {
            stack.remove(AscEq.Components.AFFIX_NAME);
         }
      }

      return null;
   }

   public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(ItemStack stack) {
      return AffixRegistry.INSTANCE.getValues().isEmpty()
         ? Collections.emptyMap()
         : (Map)CachedObjectSource.getOrCreate(
            stack,
            AFFIX_CACHED_OBJECT,
            AffixHelper::getAffixesImpl,
            CachedObject.hashComponents(new DataComponentType[]{AscEq.Components.AFFIXES, AscEq.Components.RARITY})
         );
   }

   public static Map<DynamicHolder<Affix>, AffixInstance> getAffixesImpl(ItemStack stack) {
      if (stack.isEmpty()) {
         return Collections.emptyMap();
      }

      DynamicHolder<LootRarity> rarity = getRarity(stack);
      if (!rarity.isBound()) {
         return Collections.emptyMap();
      }

      Map<DynamicHolder<Affix>, AffixInstance> map = new HashMap<>();
      ItemAffixes affixes = (ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      if (!affixes.isEmpty()) {
         LootCategory cat = LootCategory.forItem(stack);
         ObjectIterator var5 = affixes.keySet().iterator();

         while (var5.hasNext()) {
            DynamicHolder<Affix> affix = (DynamicHolder<Affix>)var5.next();
            if (affix.isBound() && ((Affix)affix.get()).canApplyTo(stack, cat, (LootRarity)rarity.get())) {
               float lvl = affixes.getLevel(affix);
               map.put(affix, new AffixInstance(affix, lvl, rarity, stack));
            }
         }
      }

      return Collections.unmodifiableMap(map);
   }

   public static Stream<AffixInstance> streamAffixes(ItemStack stack) {
      return getAffixes(stack).values().stream().filter(AffixInstance::isValid);
   }

   public static Stream<AffixInstance> streamAffixes(Projectile proj) {
      return getAffixes(proj).values().stream().filter(AffixInstance::isValid);
   }

   public static boolean hasAffixes(ItemStack stack) {
      return !getAffixes(stack).isEmpty();
   }

   public static void setRarity(ItemStack stack, LootRarity rarity) {
      stack.set(AscEq.Components.RARITY, RarityRegistry.INSTANCE.holder(rarity));
   }

   public static void copyToProjectile(ItemStack stack, Entity entity) {
      ItemAffixes affixes = (ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      ItemContainerContents gems = (ItemContainerContents)stack.getOrDefault(AscEq.Components.SOCKETED_GEMS, ItemContainerContents.EMPTY);
      if (!affixes.isEmpty() || gems.nonEmptyItemCopyStream().findAny().isPresent()) {
         Tag tag = (Tag)ItemStack.CODEC.encodeStart(entity.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
         entity.getPersistentData().put("apoth.source_weapon", tag);
      }
   }

   public static ItemStack getSourceWeapon(Entity entity) {
      return entity.getPersistentData().contains("apoth.source_weapon")
         ? entity.getPersistentData()
            .getCompound("apoth.source_weapon")
            .flatMap(t -> ItemStack.CODEC.parse(entity.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), t).result())
            .orElse(ItemStack.EMPTY)
         : ItemStack.EMPTY;
   }

   public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(Projectile proj) {
      ItemStack stack = getSourceWeapon(proj);
      return getAffixes(stack);
   }

   public static DynamicHolder<LootRarity> getRarity(ItemStack stack) {
      return (DynamicHolder<LootRarity>)stack.getOrDefault(AscEq.Components.RARITY, RarityRegistry.INSTANCE.emptyHolder());
   }

   public static Collection<DynamicHolder<Affix>> byType(AffixType type) {
      return AffixRegistry.INSTANCE.getTypeMap().get(type);
   }

   public static void applyMalice(Player player, ItemStack stack) {
      Map<DynamicHolder<Affix>, AffixInstance> affixes = getAffixes(stack);
      if (!affixes.isEmpty() && affixes.size() >= 2) {
         int seed = player.getPersistentData().getIntOr("apoth_reforge_seed", 0);
         RandomSource rand = new XoroshiroRandomSource(seed);
         ItemAffixes.Builder builder = ((ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY)).toBuilder();
         List<DynamicHolder<Affix>> afxList = new ArrayList<>(affixes.keySet());
         int size = afxList.size();
         int firstIndex = rand.nextInt(size);

         int secondIndex;
         do {
            secondIndex = rand.nextInt(size);
         } while (secondIndex == firstIndex);

         DynamicHolder<Affix> buffed = afxList.get(firstIndex);
         DynamicHolder<Affix> removed = afxList.get(secondIndex);
         builder.upgrade(buffed, 1.5F);
         float oldLevel = builder.getLevel(removed);
         builder.remove(removed);
         setAffixes(stack, builder.build());
         stack.set(AscEq.Components.TOUCHED_BY_MALICE, true);
         player.getPersistentData().putInt("apoth_reforge_seed", player.getRandom().nextInt());
         AttributeTooltipContext ctx = AttributeTooltipContext.of(
            player, TooltipContext.of(player.level()), TooltipDisplay.DEFAULT, AscendantAttributes.getTooltipFlag()
         );
         AffixInstance buff = new AffixInstance(buffed, 1.5F, getRarity(stack), stack);
         AffixInstance rem = new AffixInstance(removed, oldLevel, getRarity(stack), stack);
         MutableComponent buffedName = Component.translatable("[%s]", new Object[]{buff.getName(true)});
         buffedName.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new ShowText(buff.getAugmentingText(ctx))));
         MutableComponent removedName = Component.translatable("[%s]", new Object[]{rem.getName(true)});
         removedName.setStyle(Style.EMPTY.withColor(ChatFormatting.RED).withHoverEvent(new ShowText(rem.getAugmentingText(ctx))));
         Component msg = AscendantEquipment.lang("text", "malice_notice", buffedName, removedName);
         player.sendSystemMessage(msg);
      }
   }

   public static void applySupremacy(ItemStack stack) {
      ItemAffixes affixes = (ItemAffixes)stack.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY);
      if (!affixes.isEmpty()) {
         ItemAffixes.Builder builder = affixes.toBuilder();

         for (DynamicHolder<Affix> affix : new ArrayList<>(affixes.keySet())) {
            builder.upgrade(affix, 1.5F);
         }

         setAffixes(stack, builder.build());
      }
   }

   private static TranslatableContents copyContents(Component comp) {
      TranslatableContents tContents = (TranslatableContents)comp.getContents();
      Object[] args = tContents.getArgs();
      Object[] clone = Arrays.copyOf(args, args.length);
      return new TranslatableContents(tContents.getKey(), tContents.getFallback(), clone);
   }

   private static class ClientAccess {
      @Nullable
      private static Component getHiddenAffixName(Component currentName) {
         LocalPlayer player = Minecraft.getInstance().player;
         return player != null && WorldTier.isTutorialActive(player) ? AscendantEquipment.lang("text", "unidentified", currentName) : null;
      }
   }
}
