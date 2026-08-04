package com.skd.ascendantequipment.affix.augmenting;

import com.skd.ascendantequipment.EquipmentConfig;
import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.affix.Affix;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.affix.AffixInstance;
import com.skd.ascendantequipment.affix.AffixRegistry;
import com.skd.ascendantequipment.affix.ItemAffixes;
import com.skd.ascendantequipment.loot.LootController;
import com.skd.ascendantequipment.net.RerollResultPayload;
import com.skd.ascendantequipment.tiers.GenContext;
import com.skd.ascendantequipment.tiers.TieredWeights;
import com.skd.commontoolkit.cap.InternalItemHandler;
import com.skd.commontoolkit.dynreg.DynamicHolder;
import com.skd.commontoolkit.menu.BlockEntityMenu;
import com.skd.commontoolkit.util.EnchantmentUtils;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class AugmentingMenu extends BlockEntityMenu<AugmentingTableTile> {
   public static final int UPGRADE_BTN = 0;
   public static final int REROLL_BTN = 1;
   public static final int MAIN_SLOT = 0;
   public static final int SIGIL_SLOT = 1;
   protected final Player player;
   protected InternalItemHandler itemInv = new InternalItemHandler(1);

   public AugmentingMenu(int id, Inventory inv, BlockPos pos) {
      super(AscEq.Menus.AUGMENTING, id, inv, pos);
      this.player = inv.player;
      this.addSlot(new UpdatingSlot(this.itemInv, 0, 16, 16, AffixHelper::hasAffixes) {
         public int getMaxStackSize() {
            return 1;
         }

         public int getMaxStackSize(ItemStack pStack) {
            return 1;
         }
      });
      this.addSlot(new UpdatingSlot(((AugmentingTableTile)this.tile).inv, 0, 16, 41, stack -> stack.is(AscEq.Items.SIGIL_OF_ENHANCEMENT)));
      this.addPlayerSlots(inv, 8, 140);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && AffixHelper.hasAffixes(stack), 0, 1);
      this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && stack.is(AscEq.Items.SIGIL_OF_ENHANCEMENT), 1, 2);
      this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9, true);
      this.registerInvShuffleRules();
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.clearContainer(pPlayer, this.itemInv);
   }

   public boolean clickMenuButton(Player player, int id) {
      int selected = id >> 1;
      ItemStack mainItem = this.getMainItem();
      if (mainItem.isEmpty()) {
         return false;
      }

      List<AffixInstance> affixes = computeItemAffixes(mainItem);
      if (!affixes.isEmpty() && selected < affixes.size()) {
         switch (id & 1) {
            case 0:
               AffixInstance instx = affixes.get(selected);
               if (!canAugment(instx)) {
                  return false;
               } else {
                  if (!this.player.isCreative()) {
                     if (!this.hasUpgradeCost()) {
                        return false;
                     }

                     ((Slot)this.slots.get(1)).remove(EquipmentConfig.upgradeSigilCost);
                     EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(EquipmentConfig.upgradeLevelCost));
                  }

                  AffixHelper.applyAffix(mainItem, instx.withNewLevel(Math.min(instx.level() + 0.25F, 1.0F)));
                  ((Slot)this.slots.get(0)).set(mainItem);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.25F + 1.0F);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.getRandom().nextFloat() * 0.2F + 0.8F);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.getRandom().nextFloat() * 0.75F + 0.5F);
                  return true;
               }
            case 1:
               AffixInstance inst = affixes.get(selected);
               List<DynamicHolder<Affix>> alternatives = computeAlternatives(player, mainItem, inst);
               if (alternatives.isEmpty()) {
                  return false;
               } else {
                  if (!this.player.isCreative()) {
                     if (!this.hasRerollCost()) {
                        return false;
                     }

                     ((Slot)this.slots.get(1)).remove(EquipmentConfig.rerollSigilCost);
                     EnchantmentUtils.chargeExperience(player, EnchantmentUtils.getTotalExperienceForLevel(EquipmentConfig.rerollLevelCost));
                  }

                  ItemAffixes.Builder builder = ((ItemAffixes)mainItem.getOrDefault(AscEq.Components.AFFIXES, ItemAffixes.EMPTY)).toBuilder();
                  builder.remove(inst.affix());
                  GenContext ctx = GenContext.forPlayer(player);
                  List<Weighted<Affix>> weighted = getWeightedAffixes(alternatives, ctx);
                  DynamicHolder<Affix> newAffix = WeightedRandom.getRandomItem(player.getRandom(), weighted, Weighted::weight)
                     .map(Weighted::value)
                     .<DynamicHolder<Affix>>map(AffixRegistry.INSTANCE::holder)
                     .orElse(null);
                  if (newAffix == null) {
                     newAffix = alternatives.get(player.getRandom().nextInt(alternatives.size()));
                  }

                  builder.upgrade(newAffix, player.getRandom().nextFloat());
                  AffixHelper.setAffixes(mainItem, builder.build());
                  ((Slot)this.slots.get(0)).set(mainItem);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, player.getRandom().nextFloat() * 0.25F + 1.0F);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 0.34F, player.getRandom().nextFloat() * 0.2F + 0.8F);
                  player.level()
                     .playSound(null, this.pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.PLAYERS, 0.45F, player.getRandom().nextFloat() * 0.75F + 0.5F);
                  this.broadcastChanges();
                  PacketDistributor.sendToPlayer((ServerPlayer)this.player, new RerollResultPayload(newAffix), new CustomPacketPayload[0]);
                  ((AugmentingTableTile)this.tile).setChanged();
                  return true;
               }
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public ItemStack getMainItem() {
      return ((Slot)this.slots.get(0)).getItem();
   }

   public ItemStack getSigils() {
      return ((Slot)this.slots.get(1)).getItem();
   }

   public boolean hasUpgradeCost() {
      return this.getSigils().getCount() >= EquipmentConfig.upgradeSigilCost && this.player.experienceLevel >= EquipmentConfig.upgradeLevelCost;
   }

   public boolean hasRerollCost() {
      return this.getSigils().getCount() >= EquipmentConfig.rerollSigilCost && this.player.experienceLevel >= EquipmentConfig.rerollLevelCost;
   }

   public static List<AffixInstance> computeItemAffixes(ItemStack stack) {
      return !stack.has(AscEq.Components.AFFIXES)
         ? List.of()
         : AffixHelper.streamAffixes(stack).sorted(Comparator.comparing(inst -> inst.affix().getId())).toList();
   }

   public static boolean canAugment(AffixInstance inst) {
      return !inst.isLevelIndependent() && inst.level() < 1.0F;
   }

   protected static List<DynamicHolder<Affix>> computeAlternatives(Player player, ItemStack stack, AffixInstance selected) {
      return LootController.getAlternativeAffixes(player, stack, selected.getRarity(), selected.affix()).toList();
   }

   protected static List<Weighted<Affix>> getWeightedAffixes(List<DynamicHolder<Affix>> affixes, GenContext ctx) {
      return affixes.stream().mapMulti(TieredWeights.wrapFilterHolders(ctx)).toList();
   }
}
