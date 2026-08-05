package com.skd.ascendantequipment.socket;

import com.skd.ascendantequipment.AscEq;
import com.skd.ascendantequipment.AscendantEquipment;
import com.skd.ascendantequipment.affix.AffixHelper;
import com.skd.ascendantequipment.event.CanSocketGemEvent;
import com.skd.ascendantequipment.event.GetItemSocketsEvent;
import com.skd.ascendantequipment.event.ItemSocketingEvent;
import com.skd.ascendantequipment.loot.LootCategory;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantequipment.socket.gem.UnsocketedGem;
import com.skd.commontoolkit.util.CachedObject;
import com.skd.commontoolkit.util.CachedObject.CachedObjectSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.NeoForge;

public class SocketHelper {
   public static final Identifier GEMS_CACHED_OBJECT = AscendantEquipment.loc("gems");
   private static final ToIntFunction<ItemStack> SOCKET_DEPENDENT_COMPONENTS_HASHER = CachedObject.hashComponents(
      new DataComponentType[]{AscEq.Components.GEM, AscEq.Components.PURITY, AscEq.Components.SOCKETED_GEMS}
   );

   public static int getSockets(ItemStack stack) {
      int sockets = (Integer)stack.getOrDefault(AscEq.Components.SOCKETS, 0);
      GetItemSocketsEvent event = new GetItemSocketsEvent(stack, sockets);
      NeoForge.EVENT_BUS.post(event);
      return event.getSockets();
   }

   public static void setSockets(ItemStack stack, int sockets) {
      stack.set(AscEq.Components.SOCKETS, Mth.clamp(sockets, 0, 16));
   }

   public static SocketedGems getGems(ItemStack stack) {
      return (SocketedGems)CachedObjectSource.getOrCreate(stack, GEMS_CACHED_OBJECT, SocketHelper::getGemsImpl, SocketHelper::hashSockets);
   }

   private static int hashSockets(ItemStack stack) {
      return Objects.hash(SOCKET_DEPENDENT_COMPONENTS_HASHER.applyAsInt(stack), getSockets(stack));
   }

   private static SocketedGems getGemsImpl(ItemStack stack) {
      int size = getSockets(stack);
      if (size > 0 && !stack.isEmpty()) {
         LootCategory cat = LootCategory.forItem(stack);
         if (cat.isNone()) {
            return SocketedGems.EMPTY;
         }

         NonNullList<GemInstance> list = NonNullList.withSize(size, GemInstance.EMPTY);
         ItemContainerContents socketedGems = (ItemContainerContents)stack.getOrDefault(AscEq.Components.SOCKETED_GEMS, ItemContainerContents.EMPTY);

         for (int i = 0; i < Math.min(size, socketedGems.getSlots()); i++) {
            ItemStack gem = socketedGems.getStackInSlot(i);
            if (!gem.isEmpty()) {
               gem.setCount(1);
               GemInstance inst = GemInstance.socketed(stack, gem, i);
               list.set(i, inst);
            }
         }

         return new SocketedGems(list);
      } else {
         return SocketedGems.EMPTY;
      }
   }

   public static void setGems(ItemStack stack, SocketedGems gems) {
      ItemContainerContents contents = ItemContainerContents.fromItems(gems.stream().map(GemInstance::gemStack).toList());
      stack.set(AscEq.Components.SOCKETED_GEMS, contents);
   }

   public static boolean hasEmptySockets(ItemStack stack) {
      return getGems(stack).gems().stream().anyMatch(g -> !g.isValid());
   }

   public static int getFirstEmptySocket(ItemStack stack) {
      SocketedGems gems = getGems(stack);

      for (int socket = 0; socket < gems.size(); socket++) {
         if (!gems.get(socket).isValid()) {
            return socket;
         }
      }

      return 0;
   }

   public static boolean canSocketGemInItem(ItemStack stack, ItemStack gemStack) {
      UnsocketedGem gem = UnsocketedGem.of(gemStack);
      if (gem.isValid() && hasEmptySockets(stack)) {
         CanSocketGemEvent event = (CanSocketGemEvent)NeoForge.EVENT_BUS.post(new CanSocketGemEvent(stack, gemStack));
         return !event.isCanceled() && gem.canApplyTo(stack);
      } else {
         return false;
      }
   }

   public static ItemStack socketGemInItem(ItemStack stack, ItemStack gemStack) {
      if (!canSocketGemInItem(stack, gemStack)) {
         return ItemStack.EMPTY;
      }

      ItemStack result = stack.copy();
      result.setCount(1);
      int socket = getFirstEmptySocket(result);
      List<GemInstance> gems = new ArrayList<>(getGems(result).gems());
      ItemStack gemToInsert = gemStack.copy();
      gemToInsert.setCount(1);
      gems.set(socket, GemInstance.socketed(result, gemStack.copy(), socket));
      setGems(result, new SocketedGems(gems));
      ItemSocketingEvent event = (ItemSocketingEvent)NeoForge.EVENT_BUS.post(new ItemSocketingEvent(stack, gemToInsert, result));
      return event.getOutput();
   }

   public static Stream<GemInstance> getGemInstances(Projectile proj) {
      ItemStack stack = AffixHelper.getSourceWeapon(proj);
      return getGems(stack).stream().filter(GemInstance::isValid);
   }
}
