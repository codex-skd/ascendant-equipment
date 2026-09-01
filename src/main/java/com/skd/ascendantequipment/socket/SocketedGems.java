package com.skd.ascendantequipment.socket;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.skd.ascendantequipment.socket.gem.GemInstance;
import com.skd.ascendantattributes.modifiers.StackAttributeModifiersEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

public record SocketedGems(ImmutableList<GemInstance> gems) implements List<GemInstance> {
   public static final SocketedGems EMPTY = new SocketedGems(ImmutableList.of());

   public SocketedGems(List<GemInstance> gems) {
      this(ImmutableList.copyOf(gems));
   }

   public void addModifiers(StackAttributeModifiersEvent event) {
      this.streamValidGems().forEach(inst -> inst.addModifiers(event));
   }

   public float getDamageProtection(DamageSource source) {
      return this.streamValidGems().map(inst -> inst.getDamageProtection(source)).reduce(0.0F, Float::sum);
   }

   public float getDamageBonus(Entity entity) {
      return this.streamValidGems().map(inst -> inst.getDamageBonus(entity)).reduce(Float::sum).orElse(0.0F);
   }

   public void doPostAttack(LivingEntity user, Entity target) {
      this.streamValidGems().forEach(inst -> {
         int old = target.invulnerableTime;
         target.invulnerableTime = 0;
         inst.doPostAttack(user, target);
         target.invulnerableTime = old;
      });
   }

   public void doPostHurt(LivingEntity user, DamageSource source) {
      this.streamValidGems().forEach(inst -> inst.doPostHurt(user, source));
   }

   public void onProjectileFired(LivingEntity user, Projectile proj) {
      this.streamValidGems().forEach(inst -> inst.onProjectileFired(user, proj));
   }

   @Nullable
   public InteractionResult onItemUse(UseOnContext useinst) {
      return this.streamValidGems().map(inst -> inst.onItemUse(useinst)).filter(Predicates.notNull()).findFirst().orElse(null);
   }

   public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
      UnmodifiableIterator var4 = this.gems.iterator();

      while (var4.hasNext()) {
         GemInstance inst = (GemInstance)var4.next();
         if (inst.isValid()) {
            amount = inst.onShieldBlock(entity, source, amount);
         }
      }

      return amount;
   }

   public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
      this.streamValidGems().forEach(inst -> inst.onBlockBreak(player, world, pos, state));
   }

   public DoubleStream getDurabilityBonusPercentage() {
      return this.streamValidGems().mapToDouble(GemInstance::getDurabilityBonusPercentage);
   }

   public float onHurt(DamageSource src, LivingEntity ent, float amount) {
      UnmodifiableIterator var4 = this.gems.iterator();

      while (var4.hasNext()) {
         GemInstance inst = (GemInstance)var4.next();
         if (inst.isValid()) {
            amount = inst.onHurt(src, ent, amount);
         }
      }

      return amount;
   }

   public void getEnchantmentLevels(GetEnchantmentLevelEvent event) {
      this.streamValidGems().forEach(inst -> inst.getEnchantmentLevels(event));
   }

   public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
      this.streamValidGems().forEach(inst -> inst.modifyLoot(loot, ctx));
   }

   public Stream<GemInstance> streamValidGems() {
      return this.gems.stream().filter(GemInstance::isValid);
   }

   @Override
   public int size() {
      return this.gems.size();
   }

   @Override
   public boolean isEmpty() {
      return this.gems.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return this.gems.contains(o);
   }

   @Override
   public Iterator<GemInstance> iterator() {
      return this.gems.iterator();
   }

   @Override
   public Object[] toArray() {
      return this.gems.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a) {
      return (T[])this.gems.toArray(a);
   }

   @Deprecated
   public boolean add(GemInstance e) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      return this.gems.containsAll(c);
   }

   @Deprecated
   @Override
   public boolean addAll(Collection<? extends GemInstance> c) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public boolean addAll(int index, Collection<? extends GemInstance> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      return false;
   }

   @Deprecated
   @Override
   public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   public GemInstance get(int index) {
      return (GemInstance)this.gems.get(index);
   }

   @Deprecated
   public GemInstance set(int index, GemInstance element) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public void add(int index, GemInstance element) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   public GemInstance remove(int index) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int indexOf(Object o) {
      return this.gems.indexOf(o);
   }

   @Override
   public int lastIndexOf(Object o) {
      return this.gems.lastIndexOf(o);
   }

   @Override
   public ListIterator<GemInstance> listIterator() {
      return this.gems.listIterator();
   }

   @Override
   public ListIterator<GemInstance> listIterator(int index) {
      return this.gems.listIterator(index);
   }

   @Override
   public List<GemInstance> subList(int fromIndex, int toIndex) {
      return this.gems.subList(fromIndex, toIndex);
   }
}
